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

  val io = IO(
    new Bundle {
      val inJ = Input(new JumpBundle)
      val stall = Input(Bool())

      val out = Output(new IFBundle)
    })


  val IMEM            = Module(new IMEM)
  val pc              = RegInit(UInt(32.W), 0.U)
  val instruction     = Wire(new Instruction)
  val lastInstruction = Reg(new Instruction)
  val stalled         = RegInit(false.B)
  val lastPC          = RegInit(UInt(32.W), 0.U)
 
  /** testHarness initialization DO NOT TOUCH*/
  IMEM.testHarness.setupSignals := testHarness.IMEMsetup
  testHarness.PC := IMEM.testHarness.requestedAddress

  //Handling the program counter
  io.out.pc                   := Mux(io.inJ.jump, io.inJ.nextPC, pc)
  pc                          := Mux(io.inJ.jump, io.inJ.nextPC, pc)+Mux(io.stall, 0.U, 4.U)

  //Handling the instruction fetch
  IMEM.io.instructionAddress  := io.out.pc
  instruction                 := IMEM.io.instruction.asTypeOf(new Instruction)

  //stalling
  io.out.instruction          := Mux(stalled, lastInstruction, instruction)
  stalled                     := io.stall
  lastInstruction             := io.out.instruction
  lastPC                      := io.out.pc

  /**Setup */
  when(testHarness.IMEMsetup.setup) {
    pc          := 0.U
    instruction := Instruction.NOP
  }
  //printf("\npc: %d|", io.out.pc)
}
