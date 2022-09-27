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
    })

  val io = IO(
    new Bundle {
      val in              = Input(new EXBundle)
      val out             = Output(new MEMBundle)
    })


  val DMEM = Module(new DMEM)


  /**
    * Setup. You should not change this code
    */
  DMEM.testHarness.setup  := testHarness.DMEMsetup
  testHarness.DMEMpeek    := DMEM.io.dataOut
  testHarness.testUpdates := DMEM.testHarness.testUpdates


  /**
    * Your code here.
    */
  io.out.pc           := io.in.pc
  
  //DMEM handling     : now disabled
  DMEM.io.dataIn      := 0.U   //writeEnable ? regData : writeData (32.W) [check disegno prof]
  DMEM.io.dataAddress := 0.U      //writeAddress: DMEM
  DMEM.io.writeEnable := false.B  //io.in.memWrite

  //to WB
  io.out.regWrite     := io.in.regWrite
  io.out.writeData    := io.in.writeData        //? io.in.memRead
  io.out.writeAddress := io.in.writeAddress
}
