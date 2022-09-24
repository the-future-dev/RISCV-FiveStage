package FiveStage
import chisel3._

class IFBundle extends Bundle {
  val pc = UInt(32.W)
  val instruction = new Instruction
}

class IDBundle extends Bundle {
  val pc = UInt(32.W)               //OK
  val op1 = UInt(32.W)
  val op2 = UInt(32.W)
  val aluOP = UInt()                //OK

  val writeReg = Bool()             //OK
  val regData = UInt(32.W)
  val writeEnable   = Bool()        //OK
  val readEnable    = Bool()        //OK
  val writeAddress  = UInt(12.W)    //OK

  // val controlSignals       = Output(new ControlSignals)
  // val branchType           = Output(UInt(3.W))
  // val op1Select            = Output(UInt(1.W))
  // val op2Select            = Output(UInt(1.W))
  // val immType              = Output(UInt(3.W))
  // val immData              = Output(UInt())
  // val ALUop                = Output(UInt(4.W))
}
