package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class InstructionFetch extends MultiIOModule {

  // Don't touch
  val testHarness = IO(
    new Bundle {
      val IMEMsetup = Input(new IMEMsetupSignals)
      val PC        = Output(UInt())
    }
  )


  /**
    * TODO: Add input signals for handling events such as jumps

    * TODO: Add output signal for the instruction. 
    */
  val io = IO(
    new Bundle {
      val inJ = Input(new JumpBundle)
      // val stall = Input(Bool())

      val out = Output(new IFBundle)
    })


  val IMEM = Module(new IMEM)
  val pc   = RegInit(UInt(32.W), 0.U)
  val instruction = Wire(new Instruction)

  /** testHarness initialization DO NOT TOUCH*/
  IMEM.testHarness.setupSignals := testHarness.IMEMsetup
  testHarness.PC := IMEM.testHarness.requestedAddress


  //Handling the program counter
  io.out.pc := pc
  // when( !io.stall ){
    pc := Mux(io.inJ.jump, io.inJ.nextPC, pc + 4.U)
  // }.otherwise {
    // pc := pc
  // }

  //Handling the instruction fetch <=> instruction
  IMEM.io.instructionAddress := pc
  instruction := IMEM.io.instruction.asTypeOf(new Instruction)
  io.out.instruction := instruction

  /**Setup */
  when(testHarness.IMEMsetup.setup) {
    pc := 0.U
    instruction := Instruction.NOP
  }
}
