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


  // My code:
  val io = IO(
    new Bundle {
      val in              = Input(new EXBundle)
      val dmemReadResult  = Output(UInt(32.W))
      val out             = Output(new MEMBundle)
    }
  )
  
  io.out.pc           := io.in.pc
  
  val DMEM = Module(new DMEM)
  /**
  * Setup. You should not change this code
  */
  DMEM.testHarness.setup  := testHarness.DMEMsetup
  testHarness.DMEMpeek    := DMEM.io.dataOut
  testHarness.testUpdates := DMEM.testHarness.testUpdates

  //DMEM handling:
  //DMEM handling:
  DMEM.io.dataIn      := 0.U
  DMEM.io.dataAddress := io.in.writeData
  DMEM.io.writeEnable := false.B
  
  io.out.writeData    := 0x5A.U

  when (!io.in.memRead) {
    io.out.writeData    := io.in.writeData

    DMEM.io.writeEnable := Mux(io.in.regWrite, 0.U, io.in.memWrite)
    when(io.in.memWrite){
      DMEM.io.dataIn      := io.in.memData
      DMEM.io.dataAddress := io.in.writeData
    }.otherwise{
      DMEM.io.dataIn      := io.in.writeData
      DMEM.io.dataAddress := io.in.writeAddress
    }
    
  }

  //to WB
  io.out.regWrite     := io.in.regWrite
  io.out.writeData    := io.in.writeData
  io.out.writeAddress := io.in.writeAddress
  io.out.memRead      := io.in.memRead
  io.dmemReadResult   := DMEM.io.dataOut
}
