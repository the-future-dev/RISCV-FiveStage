package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase, MuxLookup, Cat, Fill}
import chisel3.experimental.MultiIOModule

import Op1Select._
import Op2Select._
import ImmFormat._

class InstructionDecode extends MultiIOModule {

  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val registerSetup = Input(new RegisterSetupSignals)
      val registerPeek  = Output(UInt(32.W))

      val testUpdates   = Output(new RegisterUpdates)
    }
  )

  val io = IO(
    new Bundle {
      val in    = Input(new IFBundle)
      val wb    = Input(new WriteBackBundle)
      val ex    = Input(new EXBundle)
      val mem   = Input(new MEMBundle)

      val outJ  = Output(new JumpBundle)
      val stall = Output(Bool())
      val out   = Output(new IDBundle)
    }
  )

  //state
  val stalled                 = RegInit(false.B)
  val stalled2                = RegInit(false.B)
  val savedInstruction        = Reg(new Instruction)
  
  val registers               = Module(new Registers)
  val decoder                 = Module(new Decoder).io

  val launched = io.in.pc.asTypeOf(SInt(32.W)) >= 0.S

  /** Setup. You should not change this code */
  registers.testHarness.setup := testHarness.registerSetup
  testHarness.registerPeek    := registers.io.readData1
  testHarness.testUpdates     := registers.testHarness.testUpdates

  //SETUP:
  io.out.pc                   :=  io.in.pc
  decoder.instruction         :=  Mux(stalled, savedInstruction, io.in.instruction.asTypeOf(new Instruction))

  val rs1address              = decoder.instruction.registerRs1
  val rs2address              = decoder.instruction.registerRs2
  val rdaddress               = decoder.instruction.registerRd

  //RAW: stalling
  val sigEX_STALL             = io.ex.regWrite  && io.ex.memRead  && (io.in.instruction.registerRs1 === io.ex.writeAddress || io.in.instruction.registerRs2 === io.ex.writeAddress)
  val sigMEM_STALL            = io.mem.regWrite && io.mem.memRead && (io.mem.writeAddress === io.in.instruction.registerRs1 || io.mem.writeAddress === io.in.instruction.registerRs2)

  io.stall                    := sigEX_STALL || sigMEM_STALL || stalled2
  stalled                     := sigEX_STALL || sigMEM_STALL
  stalled2                    := sigEX_STALL
  savedInstruction            := Mux(sigEX_STALL || sigMEM_STALL, io.in.instruction, 0.U.asTypeOf(new Instruction))

  //to REGISTERS                                                                                            
  registers.io.readAddress1   := rs1address
  registers.io.readAddress2   := rs2address

    //-> WB
  registers.io.writeEnable    := io.wb.writeEnable
  registers.io.writeAddress   := io.wb.writeAddress
  registers.io.writeData      := io.wb.writeData

    //setup
  val immediate = MuxLookup(decoder.immType, 0.S(12.W), Array(
    ITYPE             -> decoder.instruction.immediateIType,
    STYPE             -> decoder.instruction.immediateSType,
    BTYPE             -> decoder.instruction.immediateBType,
    UTYPE             -> decoder.instruction.immediateUType,
    JTYPE             -> decoder.instruction.immediateJType,
    IMFDC -> 0.S(12.W)
  )).asTypeOf(SInt(32.W)).asUInt

  val regsource1 = Mux(io.ex.regWrite && (io.ex.writeAddress === rs1address), io.ex.writeData,
                          Mux(io.mem.regWrite && (io.mem.writeAddress === rs1address), io.mem.writeData,
                            Mux(io.wb.writeEnable && (io.wb.writeAddress === rs1address), io.wb.writeData,
                              registers.io.readData1
                            )
                          )
                        )

  val regsource2 = Mux(io.ex.regWrite && (io.ex.writeAddress === rs2address) && (rs2address=/=0.U), io.ex.writeData,
                      Mux(io.mem.regWrite && (io.mem.writeAddress === rs2address) && (rs2address=/=0.U), io.mem.writeData,
                        Mux(io.wb.writeEnable && (io.wb.writeAddress === rs2address) && (rs2address=/=0.U), io.wb.writeData,
                          registers.io.readData2
                        )
                      )
                    )

  val a = MuxLookup(decoder.op1Select, 0.U(32.W), Array(
    rs1              -> regsource1,
    PC              -> 0.U(5.W),
    IMFDC           -> 0.U(5.W)
  ))

  val b = MuxLookup(decoder.op2Select, 0.U(32.W), Array(
    rs2             -> regsource2,
    imm             -> immediate,
    IMFDC           -> 0.U(32.W)
  ))

    //TO -> EXECUTE
  io.out.writeAddress         := Mux(decoder.controlSignals.regWrite && launched, decoder.instruction.registerRd, 0.U)
  io.out.aluOP                := decoder.ALUop
  io.out.memWrite             := decoder.controlSignals.memWrite
  io.out.memRead              := decoder.controlSignals.memRead
  io.out.regWrite             := decoder.controlSignals.regWrite
  io.out.memData              := Mux(io.out.memWrite, regsource2, 0.U)
  io.out.op1 := a
  io.out.op2 := b
  
  //TO -> INSTRUCTION FETCH
  val branchTypeMap = Array(
    branchType.beq  -> (a === b),
    branchType.neq  -> (a =/= b),
    branchType.gte  -> (a.asSInt >= b.asSInt),
    branchType.gteu -> (a >= b),
    branchType.lt   -> (a.asSInt < b.asSInt),
    branchType.ltu  -> (a < b)
  )

  //Jumping and Branching
  val jumping = decoder.controlSignals.jump
  val branching = decoder.controlSignals.branch

  io.outJ.jump   := Mux(branching, MuxLookup(decoder.branchType, false.B, branchTypeMap), jumping)
  io.outJ.nextPC := Mux(branching, io.in.pc + immediate,
                      Mux(decoder.immType === JTYPE, io.in.pc + immediate,
                        ((regsource1 + immediate) & "hFFFF_FFFE".U(32.W))
                      )
                    )

  when(jumping && decoder.controlSignals.regWrite){
      io.out.op1                := io.in.pc
      io.out.op2                := 4.U
      io.out.aluOP              := ALUOps.ADD
      io.out.memData            := 0.U
      io.out.memRead            := false.B
      io.out.memWrite           := false.B
      io.out.regWrite           := true.B
      io.out.writeAddress       := decoder.instruction.registerRd
  }

  //Nothing more to do for this instruction: kind of a bubble
  when(io.stall){
    io.out.op1                := 0.U
    io.out.op2                := 0.U
    io.out.aluOP              := ALUOps.DC
    io.out.memData            := 0.U
    io.out.writeAddress       := 0.U
    io.out.regWrite           := false.B
    io.out.memRead            := false.B
    io.out.memWrite           := false.B

    // io.out.writeData          := 0.U
  }

  chisel3.experimental.dontTouch(sigEX_STALL)
  chisel3.experimental.dontTouch(sigMEM_STALL)

  // printf("%d: stall {ex: %d | mem1: %d |mem2: %d }\n", io.out.pc, sigEX_STALL, sigMEM_STALL, sigMEM_DELAY)
  //printf("%d:> %d\n", io.out.pc, (sigMEM_DELAY && !sigMEM_STALL))

  //LOOK for SW pipelined
  // printf("%d | MEMWRITE:%d | stall: %d: ", io.in.pc, io.out.memWrite, io.stall)
  // printf("FWD ex: %d", io.ex.regWrite && (io.ex.writeAddress === rs1address || io.ex.writeAddress ===rs2address))
  // printf("FWD mem: %d", io.mem.regWrite && (io.mem.writeAddress === rs1address|| io.mem.writeAddress ===rs2address))
  // printf("FWD wb: %d", io.wb.writeEnable && (io.wb.writeAddress ===rs1address))
  // printf(" | WB: %d | out: %d\n", io.wb.writeData, io.out.op1+io.out.op2)
  // printf(" memData: %d | %d \n", io.out.memData, a)

  //LOOK for Jumping pipelined
  // when(io.in.pc >= 0.U){
  //   printf("%d| jump: %d | in pc: %d | j pc: %d\n", io.in.pc, jumping, io.in.pc, io.outJ.nextPC)
  // }
}

