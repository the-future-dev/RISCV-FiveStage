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

      val outJ  = Output(new JumpBundle)
      val stall = Output(Bool())
      val out   = Output(new IDBundle)

      val br    = Output(Bool())
      val taken = Output(Bool())
    }
  )

  //STATE
  val stalled                  = RegInit(false.B)
  val savedInstruction         = Reg(new Instruction)
  val stopped                  = RegInit(false.B)
  
  val registers                = Module(new Registers)
  val decoder                  = Module(new Decoder).io

  /** Setup. You should not change this code */
  registers.testHarness.setup := testHarness.registerSetup
  testHarness.registerPeek    := registers.io.readData1
  testHarness.testUpdates     := registers.testHarness.testUpdates

  //SETUP:
  decoder.instruction         :=  Mux(stalled, savedInstruction, io.in.instruction.asTypeOf(new Instruction))

  val rs1address               = decoder.instruction.registerRs1
  val rs2address               = decoder.instruction.registerRs2
  val rdaddress                = decoder.instruction.registerRd
  val jumping                  = decoder.controlSignals.jump
  val branching                = decoder.controlSignals.branch

  val immediate = MuxLookup(decoder.immType, 0.S(12.W), Array(
    ITYPE                     -> decoder.instruction.immediateIType,
    STYPE                     -> decoder.instruction.immediateSType,
    BTYPE                     -> decoder.instruction.immediateBType,
    UTYPE                     -> decoder.instruction.immediateUType,
    JTYPE                     -> decoder.instruction.immediateJType,
    IMFDC                     -> 0.S(12.W)
  )).asTypeOf(SInt(32.W)).asUInt

  io.out.pc                   :=  io.in.pc
  io.out.imm                  := immediate
  io.out.op1sel               := decoder.op1Select
  io.out.op2sel               := decoder.op2Select

  //RAW: stalling
  val sigEX_STALL             = io.ex.regWrite  && io.ex.memRead  && (io.ex.writeAddress  === io.in.instruction.registerRs1 || io.ex.writeAddress  === io.in.instruction.registerRs2)
  // chisel3.experimental.dontTouch(sigEX_STALL)
  // val sigMEM_STALL_BR         = branching && io.mem.regWrite && io.mem.memRead && (io.mem.writeAddress === io.in.instruction.registerRs1 || io.mem.writeAddress === io.in.instruction.registerRs2)

  io.stall                    := sigEX_STALL
  stalled                     := sigEX_STALL
  savedInstruction            := Mux(sigEX_STALL, io.in.instruction, 0.U.asTypeOf(new Instruction))
  
  //forwarding
  io.out.address1             := Mux((decoder.op1Select === rs1), rs1address, 0.U)
  io.out.address2             := Mux((decoder.op2Select === rs2), rs2address, 0.U)  
  io.out.memDSrc              := Mux(io.out.memWrite, rs2address, 0.U)

  //to -> REGISTERS                                                                                            
  registers.io.readAddress1   := rs1address
  registers.io.readAddress2   := rs2address

  //to -> WB
  registers.io.writeEnable    := io.wb.writeEnable
  registers.io.writeAddress   := io.wb.writeAddress
  registers.io.writeData      := io.wb.writeData


  val regsource1 = Mux(io.ex.regWrite && (io.ex.writeAddress === rs1address), io.ex.writeData,
                            Mux(io.wb.writeEnable && (io.wb.writeAddress === rs1address), io.wb.writeData,
                              registers.io.readData1
                            )
                          )

  val regsource2 = Mux(io.ex.regWrite && (io.ex.writeAddress === rs2address), io.ex.writeData,
                        Mux(io.wb.writeEnable && (io.wb.writeAddress === rs2address), io.wb.writeData,
                          registers.io.readData2
                        )
                      )
  
  //to -> EXECUTE
  io.out.writeAddress         := Mux(decoder.controlSignals.regWrite, decoder.instruction.registerRd, 0.U)
  io.out.aluOP                := decoder.ALUop
  io.out.memWrite             := decoder.controlSignals.memWrite
  io.out.memRead              := decoder.controlSignals.memRead
  io.out.regWrite             := decoder.controlSignals.regWrite
  io.out.memData              := Mux(io.out.memWrite, regsource2, 0.U)
  io.out.op1                  := Mux(io.wb.writeEnable && (io.wb.writeAddress === rs1address), io.wb.writeData, registers.io.readData1)
  io.out.op2                  := Mux(io.wb.writeEnable && (io.wb.writeAddress === rs2address), io.wb.writeData, registers.io.readData2)
  
  val a = regsource1
  val b = regsource2
  
  //to -> INSTRUCTION FETCH
  val branchTypeMap = Array(
    branchType.beq            -> (a === b),
    branchType.neq            -> (a =/= b),
    branchType.gte            -> (a.asSInt >= b.asSInt),
    branchType.gteu           -> (a >= b),
    branchType.lt             -> (a.asSInt < b.asSInt),
    branchType.ltu            -> (a < b)
  )

  //Jumping and Branching
  io.outJ.jump                := Mux(io.stall, false.B, Mux(branching, MuxLookup(decoder.branchType, false.B, branchTypeMap), jumping))
  val pcAfterStalling          = Mux((!io.stall && (stalled)), io.in.pc-4.U, io.in.pc)
  io.outJ.nextPC              := Mux(io.stall || !jumping, 0.U,
                                  Mux(branching, pcAfterStalling + immediate,
                                    Mux(decoder.immType === JTYPE, pcAfterStalling + immediate,
                                      ((regsource1 + immediate) & "hFFFF_FFFE".U(32.W))
                                    )
                                  )
                                )
  
  when(jumping && decoder.controlSignals.regWrite && !io.stall){
    io.out.pc                 := pcAfterStalling
    io.out.imm                := 4.U
    io.out.aluOP              := ALUOps.ADD
    io.out.writeAddress       := decoder.instruction.registerRd
  }
  
  //Nothing more to do for this instruction: bubble in the pipeline
  when(io.stall || stopped){
    io.out.op1                := 0.U
    io.out.op2                := 0.U
    io.out.imm                := 0.U
    io.out.aluOP              := ALUOps.DC
    io.out.memData            := 0.U
    io.out.writeAddress       := 0.U
    io.out.regWrite           := false.B
    io.out.memRead            := false.B
    io.out.memWrite           := false.B
    io.out.address1           := 0.U
    io.out.address2           := 0.U
    io.out.memDSrc            := 0.U
  }

  //Branch prediction "managing"
  io.br     := branching
  io.taken  := Mux(branching, MuxLookup(decoder.branchType, false.B, branchTypeMap), false.B)

  when((io.in.instruction.asUInt === 0x13.U || io.in.instruction.asUInt === 0x00.U) && (io.in.pc.asTypeOf(SInt(32.W)) >= 0.S) && (io.in.pc =/= 0.U) && !stalled){
    stopped                   := true.B
  }
}