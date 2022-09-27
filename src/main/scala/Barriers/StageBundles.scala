package FiveStage
import chisel3._

class IFBundle extends Bundle {
  val pc = UInt(32.W)
  val instruction = new Instruction
}

class IDBundle extends Bundle {
  val pc = UInt(32.W)
  val op1 = UInt(32.W)
  val op2 = UInt(32.W)
  val aluOP = UInt(4.W)

  val regWrite      = Bool()
  val memData       = UInt(32.W)

  val memRead       = Bool()
  val memWrite      = Bool()

  val writeAddress  = UInt(12.W) // address DMEM 12.W    Registers 5.W

}

class EXBundle extends Bundle {
  val pc = UInt(32.W)

  val regWrite      = Bool()
  val memData       = UInt(32.W)  //from reading the register
  val writeData     = UInt(32.W)  //result of the ALU op

  val memRead       = Bool()
  val memWrite      = Bool()

  val writeAddress  = UInt(12.W) // address DMEM 12.W    Registers 5.W
}

class MEMBundle extends Bundle {
  val pc            = UInt(32.W)

  val regWrite      = Bool()
  val writeData     = UInt(32.W)
  val writeAddress  = UInt(5.W)    //registers has 5.W lenght of address
}

class WriteBackBundle extends Bundle {
  val pc            = UInt(32.W)
  val writeEnable   = Bool()
  val writeAddress  = UInt(5.W)
  val writeData     = UInt(32.W)
}
