package FiveStage
import chisel3._

class IFBundle extends Bundle {
  val pc            = UInt(32.W)
  val instruction   = new Instruction
}

class IDBundle extends Bundle {
  val pc            = UInt(32.W)
  val op1           = UInt(32.W)
  val op2           = UInt(32.W)
  val imm           = UInt(32.W)
  val aluOP         = UInt(4.W)

  val regWrite      = Bool()
  val memData       = UInt(32.W)

  val memRead       = Bool()
  val memWrite      = Bool()

  val writeAddress  = UInt(5.W)   //address of the destination register
  
  //Forwarding
  val address1      = UInt(32.W)
  val address2      = UInt(32.W)
  val op1sel        = UInt(1.W)
  val op2sel        = UInt(1.W)
  val memDSrc       = UInt(32.W)
}

class EXBundle extends Bundle {
  val pc = UInt(32.W)

  // val memData       = UInt(32.W)  //from reading the register
  val writeData     = UInt(32.W)  //result of the ALU op

  val writeAddress  = UInt(5.W)

  val regWrite      = Bool()
  val memRead       = Bool()
  val memWrite      = Bool()

}

// class MEMBundle extends Bundle {
//   val pc            = UInt(32.W)

//   val memRead       = Bool()
//   val regWrite      = Bool()
//   val writeData     = UInt(32.W)
//   val writeAddress  = UInt(5.W)
// }

class WriteBackBundle extends Bundle {
  val pc            = UInt(32.W)
  val writeEnable   = Bool()
  val writeAddress  = UInt(5.W)
  val writeData     = UInt(32.W)
}

class JumpBundle extends Bundle {
  val jump          = Bool()
  val nextPC        = UInt(32.W)
}
