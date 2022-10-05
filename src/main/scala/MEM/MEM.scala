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
      val dmemReadResult  = Output(UInt(32.W))
      val out             = Output(new MEMBundle)
    })


  val DMEM = Module(new DMEM)


  /**
    * Setup. You should not change this code
    */
  DMEM.testHarness.setup  := testHarness.DMEMsetup
  testHarness.DMEMpeek    := DMEM.io.dataOut
  testHarness.testUpdates := DMEM.testHarness.testUpdates


  // My code:
  io.out.pc           := io.in.pc
  
  //DMEM handling:
  DMEM.io.dataIn      := io.in.memData              //regData : writeData (32.W) [check disegno prof]
  DMEM.io.dataAddress := io.in.writeData
  DMEM.io.writeEnable := io.in.memWrite
  
  //to WB
  io.out.regWrite     := io.in.regWrite
  io.out.writeData    := io.in.writeData        //? io.in.memRead
  io.out.writeAddress := io.in.writeAddress
  io.out.memRead      := io.in.memRead
  io.dmemReadResult   := DMEM.io.dataOut
}


// DMEM.io.dataAddress := io.in.writeData

// DMEM.io.dataAddress := io.in.writeData
// DMEM.io.dataIn := io.in.swRs2
// io.out.dataOut := io.in.writeData

// when(!io.in.memRead){
  //   DMEM.io.writeEnable := Mux(io.in.regWrite, 0.U, io.in.memWrite)

  //   when(io.in.memWrite){
  //     DMEM.io.dataIn := io.in.memData
  //     DMEM.io.dataAddress := io.in.writeData
    
  //   }.otherwise{
  //     DMEM.io.dataIn      := io.in.writeData
  //     DMEM.io.dataAddress := io.in.writeAddress
  //   }
  // }