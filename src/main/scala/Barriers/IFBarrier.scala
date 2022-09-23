package FiveStage
import chisel3._

class IFBundle extends Bundle {
  val pc = UInt(32.W)
  val instruction = new Instruction
}

class IFBarrier extends Module {
    val io = IO(new Bundle{
        val in = Input(new IFBundle)
        val out = Output(new IFBundle)
    })
    //instruction
    io.out.instruction := io.in.instruction

    //program counter
    val pc = RegInit(0.U(32.W))
    pc := io.in.pc
    io.out.pc := pc
}
