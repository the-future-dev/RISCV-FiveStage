package FiveStage
import chisel3._
import chisel3.util._
import chisel3.experimental.MultiIOModule


class MemoryFetch() extends MultiIOModule {
  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val DMEMsetup      = Input(new DMEMsetupSignals)
      val DMEMpeek       = Output(UInt(32.W))

      val testUpdates    = Output(new MemUpdates)
    }
  )

  val io = IO(
    new Bundle {
      val in              = Input(new IDBundle)
      val wb              = Input(new WriteBackBundle)

      val dmemReadResult  = Output(UInt(32.W))
    }
  )
  
  //io.in.pc
  val DMEM = Module(new DMEM)
  /** Setup. You should not change this code */
  DMEM.testHarness.setup  := testHarness.DMEMsetup
  testHarness.DMEMpeek    := DMEM.io.dataOut
  testHarness.testUpdates := DMEM.testHarness.testUpdates

  //DMEM handling:
  DMEM.io.dataIn      := Mux(io.wb.writeEnable && (io.wb.writeAddress === io.in.memDSrc), io.wb.writeData, io.in.memData)
  DMEM.io.dataAddress := Mux(io.wb.writeEnable && (io.wb.writeAddress === io.in.address1), io.wb.writeData, io.in.op1) + io.in.imm;
  DMEM.io.writeEnable := io.in.memWrite

  //Write Back
  io.dmemReadResult   := DMEM.io.dataOut
}
