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
    })


  val io = IO(
    new Bundle {
      val in = Input(new IFBundle)
      val wbIn = Input(new WriteBackBundle)

      val out = Output(new IDBundle)
    }
  )

  //initizlization of registers, decoder, PC
  val registers = Module(new Registers)
  val decoder   = Module(new Decoder).io
  io.out.pc     :=  io.in.pc
  // val currentPC = RegInit(UInt(32.W), 0.U)
  // currentPC := io.in.pc
  // io.out.pc := currentPC

  /** Setup. You should not change this code */
  registers.testHarness.setup := testHarness.registerSetup
  testHarness.registerPeek    := registers.io.readData1
  testHarness.testUpdates     := registers.testHarness.testUpdates

  /**
    * DECODER SETUP
    */
  decoder.instruction := io.in.instruction.asTypeOf(new Instruction)

  val rs1address = decoder.instruction.registerRs1
  val rs2address = decoder.instruction.registerRs2
  val rdaddress = decoder.instruction.registerRd

  //        to REGISTERS
  registers.io.readAddress1 := Mux(decoder.op1Select === rs1, rs1address,Mux(decoder.op1Select === Op1Select.PC, 0xFD.U(8.W), 0.U))
  registers.io.readAddress2 := Mux(decoder.immType === ImmFormat.STYPE, rs2address, Mux(decoder.op2Select === rs2, rs2address, 0.U))
  registers.io.writeEnable  := io.wbIn.writeEnable
  registers.io.writeAddress := io.wbIn.writeAddress
  registers.io.writeData    := io.wbIn.writeData

  //Signals to Execute
  io.out.writeAddress   := Mux(decoder.controlSignals.regWrite, decoder.instruction.registerRd, 0.U)
  io.out.aluOP          := decoder.ALUop
  io.out.memWrite       := decoder.controlSignals.memWrite
  io.out.memRead        := decoder.controlSignals.memRead
  io.out.regWrite       := decoder.controlSignals.regWrite
  

  //TODO
  io.out.memData        := registers.io.readData2

  val immediate = MuxLookup(decoder.immType, 0.S(12.W), Array(
    ITYPE -> decoder.instruction.immediateIType,
    STYPE -> decoder.instruction.immediateSType,
    BTYPE -> decoder.instruction.immediateBType,
    UTYPE -> decoder.instruction.immediateUType,
    JTYPE -> decoder.instruction.immediateJType,
    IMFDC -> 0.S(12.W)
  )).asTypeOf(SInt(32.W)).asUInt

  // TODO:
  io.out.op1 := MuxLookup(decoder.op1Select, 0.U(32.W), Array(
    rs1          -> registers.io.readData1,
    PC           -> io.in.pc,
    IMFDC        -> 0.U(32.W)
  ))
  io.out.op2 := MuxLookup(decoder.op2Select, 0.U(32.W), Array(
      rs2          -> registers.io.readData2,
      imm          -> immediate,
      IMFDC        -> 0.U(32.W)
  ))
}

// /**
//   * DECODER to EX
//   */
// io.out.controlSignals := decoder.controlSignals
// io.out.branchType     := decoder.branchType
// io.out.op1Select      := decoder.op1Select
// io.out.op2Select      := decoder.op2Select
// io.out.immType        := decoder.immType
// io.out.ALUop          := decoder.ALUop
/**
  * DECODE IMMEDIATE
  */
// val immMap = Array(
// //KEY       value
//   ITYPE -> decoder.instruction.immediateIType,
//   STYPE -> decoder.instruction.immediateSType,
//   BTYPE -> decoder.instruction.immediateBType,
//   UTYPE -> decoder.instruction.immediateUType,
//   JTYPE -> decoder.instruction.immediateJType,
  
// )

// immediateValue := MuxLookup(decoder.immType, 0.S(32.W), immMap)
//SIGN EXTENSION OF THE IMMEDIATE?
//io.immData := Cat(Fill(16, immData(15)), immData(15,0)).asUInt
