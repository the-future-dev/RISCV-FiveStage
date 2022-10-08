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
      val in = Input(new IFBundle)
      val wbIn = Input(new WriteBackBundle)

      val next_pc = Output(UInt(32.W))
      val jump = Output(Bool())

      val out = Output(new IDBundle)
    }
  )

  val registers = Module(new Registers)
  val decoder   = Module(new Decoder).io
  io.out.pc     :=  io.in.pc

  /** Setup. You should not change this code */
  registers.testHarness.setup := testHarness.registerSetup
  testHarness.registerPeek    := registers.io.readData1
  testHarness.testUpdates     := registers.testHarness.testUpdates

  //DECODER SETUP
  decoder.instruction := io.in.instruction.asTypeOf(new Instruction)

  val rs1address = decoder.instruction.registerRs1
  val rs2address = decoder.instruction.registerRs2
  val rdaddress = decoder.instruction.registerRd

  //        to REGISTERS                                                                                            
  registers.io.readAddress1 := Mux(decoder.op1Select === rs1, rs1address,Mux(decoder.op1Select === Op1Select.PC, 0xFD.U(8.W), 0.U))
  registers.io.readAddress2 := Mux(decoder.immType === ImmFormat.STYPE, rs2address, Mux(decoder.op2Select === rs2, rs2address, 0.U))

    //WB
  registers.io.writeEnable  := io.wbIn.writeEnable
  registers.io.writeAddress := io.wbIn.writeAddress
  registers.io.writeData    := io.wbIn.writeData

  //TO -> EXECUTE
  io.out.writeAddress   := Mux(decoder.controlSignals.regWrite, decoder.instruction.registerRd, 0.U)
  io.out.aluOP          := decoder.ALUop
  io.out.memWrite       := decoder.controlSignals.memWrite
  io.out.memRead        := decoder.controlSignals.memRead
  io.out.regWrite       := decoder.controlSignals.regWrite
  io.out.memData        := registers.io.readData2

  //setup
  def sext(value: SInt, width: Int = 32) = value.asTypeOf(SInt(width.W)).asUInt

  val immediate = MuxLookup(decoder.immType, 0.S(12.W), Array(
    ITYPE -> decoder.instruction.immediateIType,
    STYPE -> decoder.instruction.immediateSType,
    BTYPE -> decoder.instruction.immediateBType,
    UTYPE -> decoder.instruction.immediateUType,
    JTYPE -> decoder.instruction.immediateJType,
    IMFDC -> 0.S(12.W)
  )).asTypeOf(SInt(32.W)).asUInt

  val a = MuxLookup(decoder.op1Select, 0.U(32.W), Array(
    rs1          -> registers.io.readData1,
    PC           -> 0.U(5.W),
    IMFDC        -> 0.U(5.W)
  ))

  val b = MuxLookup(decoder.op2Select, 0.U(32.W), Array(
      rs2          -> registers.io.readData2,
      imm          -> immediate,
      IMFDC        -> 0.U(32.W)
  ))
  io.out.op1 := a
  io.out.op2 := b

  //TO -> INSTRUCTION FETCH
  val branchTypeMap = Array(
    branchType.beq -> (a === b),
    branchType.neq -> (a =/= b),
    branchType.gte -> (a.asSInt >= b.asSInt),
    branchType.gteu -> (a >= b),
    branchType.lt -> (a.asSInt < b.asSInt),
    branchType.ltu -> (a < b)
  )

  val jumping = decoder.controlSignals.jump
  val branching = decoder.controlSignals.branch

  io.jump   := jumping
  io.next_pc := 0.U

  when(branching){
    io.jump := MuxLookup(decoder.branchType, false.B, branchTypeMap)
    io.next_pc := io.in.pc + immediate
  }

  when(jumping && decoder.controlSignals.regWrite){
    io.next_pc := io.in.pc + immediate
    io.out.regWrite := false.B

    registers.io.writeEnable  := true.B
    registers.io.writeAddress := decoder.instruction.registerRd
    registers.io.writeData    := io.in.pc + 4.U
  }

    // printf("[0x%x] imm: 0x%x, rs1S: %d, rs2S: %d, memRead: %d, memWrite: %d," +
    //   " INS: 0x%x\n",
    //         io.in.pc.asUInt, immediate, (decoder.op1Select === rs1).asUInt, (decoder.op2Select === rs2).asUInt,
    //         decoder.controlSignals.memRead.asUInt, decoder.controlSignals.memWrite.asUInt,
    //         // regSourceOp1, io.stall, stalled,
    //         decoder.instruction.instruction)
}