package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase }
import chisel3.experimental.MultiIOModule


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
      /*TODO: IO*/
      val in = Input(new IFBundle)

    }
  )

  val registers = Module(new Registers)
  val decoder   = Module(new Decoder).io
  val currentPC = RegInit(UInt(32.W), 0.U)
  
  /**
    * Setup. You should not change this code
    */
  registers.testHarness.setup := testHarness.registerSetup
  testHarness.registerPeek    := registers.io.readData1
  testHarness.testUpdates     := registers.testHarness.testUpdates


  /**Getting the instruction and decoding it into the registers*/
  registers.io.readAddress1 := io.in.instruction.registerRs1
  registers.io.readAddress2 := io.in.instruction.registerRs2
  registers.io.writeEnable  := false.B
  registers.io.writeAddress := io.in.instruction.registerRd
  registers.io.writeData    := 0.U

  decoder.instruction := 0.U.asTypeOf(new Instruction)

  //NEED TO CHECK PC DELAY
  //Handling PC
  currentPC :=io.in.pc
}
