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

  //TO -> EXECUTE
  io.out.writeAddress         := Mux(decoder.controlSignals.regWrite, decoder.instruction.registerRd, 0.U)
  io.out.aluOP                := decoder.ALUop
  io.out.memWrite             := decoder.controlSignals.memWrite
  io.out.memRead              := decoder.controlSignals.memRead
  io.out.regWrite             := decoder.controlSignals.regWrite
  io.out.memData              := registers.io.readData2

    //setup
  val immediate = MuxLookup(decoder.immType, 0.S(12.W), Array(
    ITYPE             -> decoder.instruction.immediateIType,
    STYPE             -> decoder.instruction.immediateSType,
    BTYPE             -> decoder.instruction.immediateBType,
    UTYPE             -> decoder.instruction.immediateUType,
    JTYPE             -> decoder.instruction.immediateJType,
    IMFDC -> 0.S(12.W)
  )).asTypeOf(SInt(32.W)).asUInt

  val a = MuxLookup(decoder.op1Select, 0.U(32.W), Array(
    rs1              -> Mux(io.ex.regWrite && (io.ex.writeAddress === rs1address), io.ex.writeData,
                          Mux(io.mem.regWrite && (io.mem.writeAddress === rs1address), io.mem.writeData,
                            Mux(io.wb.writeEnable && (io.wb.writeAddress === rs1address), io.wb.writeData, registers.io.readData1
                            )
                          )
                        ),
    PC              -> 0.U(5.W),
    IMFDC           -> 0.U(5.W)
  ))

  val b = MuxLookup(decoder.op2Select, 0.U(32.W), Array(
    rs2             ->  Mux(io.ex.regWrite && (io.ex.writeAddress === rs2address), io.ex.writeData,
                          Mux(io.mem.regWrite && (io.mem.writeAddress === rs2address), io.mem.writeData,
                            Mux(io.wb.writeEnable && (io.wb.writeAddress === rs2address), io.wb.writeData, registers.io.readData2
                            )
                          )
                        ),
    imm             -> immediate,
    IMFDC           -> 0.U(32.W)
  ))
  //TO -> EXECUTE bis :>
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

  io.outJ.jump                := jumping
  io.outJ.nextPC              := 0.U

  when(branching){
    io.outJ.jump              := MuxLookup(decoder.branchType, false.B, branchTypeMap)
    io.outJ.nextPC            := io.in.pc + immediate
  }

  when(jumping && decoder.controlSignals.regWrite){
    io.outJ.nextPC            := Mux(decoder.immType === JTYPE, io.in.pc + immediate, ((registers.io.readData1 + immediate) & "hFFFF_FFFE".U(32.W)))
    io.out.regWrite           := false.B
  }
  when(jumping && !branching){
    when(io.ex.regWrite || io.mem.regWrite || io.wb.writeEnable){
      stalled := true.B
      savedInstruction := io.in.instruction
      io.stall := true.B
    }.otherwise{
      registers.io.writeEnable  := true.B
      registers.io.writeAddress := Mux(decoder.controlSignals.regWrite, decoder.instruction.registerRd, 0.U)
      registers.io.writeData    := io.in.pc + 4.U
    }
  }

  //Nothing more to do for this instruction: kind of a bubble
  when(io.stall || (jumping && decoder.controlSignals.regWrite)){
    io.out.op1                := 0.U
    io.out.op2                := 0.U
    io.out.aluOP              := ALUOps.DC
    io.out.memData            := 0.U
    io.out.writeAddress       := 0.U
    io.out.regWrite           := false.B
    io.out.memRead            := false.B
    io.out.memWrite           := false.B
  }
}

