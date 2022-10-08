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
      //stall | jump | stopped
      val next_pc = Input(UInt(32.W))
      val jump = Input(Bool())

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
  pc := Mux(io.jump, io.next_pc, pc + 4.U)

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
