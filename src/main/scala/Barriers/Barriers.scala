package FiveStage
import chisel3._


class IFBarrier extends Module {
    val io = IO(new Bundle{
        val in = Input(new IFBundle)
        val out = Output(new IFBundle)
    })
    io.out.instruction := io.in.instruction

    val pc = RegInit(0.U(32.W))
    pc := io.in.pc
    io.out.pc := pc
}

class IDBarrier extends Module {
    val io = IO(new Bundle{
        val in = Input(new IDBundle)
        val out = Output(new IDBundle)
    })
    val regs = Reg(new IDBundle)

    regs   := io.in
    io.out := regs
}

class EXBarrier extends Module {
    val io = IO(new Bundle{
        val in = Input(new EXBundle)
        val out = Output(new EXBundle)
    })
    val regs = Reg(new EXBundle)

    regs   := io.in
    io.out := regs
}


class MEMBarrier extends Module {
    val io = IO(new Bundle {
        val in = Input(new MEMBundle)
        val out = Output(new MEMBundle)
    })
    val regs = Reg(new MEMBundle)
    regs   := io.in
    io.out := regs
}

class WBBarrier extends Module {
    val io = IO(new Bundle{
        val in = Input(new WriteBackBundle)
        val out = Output(new WriteBackBundle)
    })
    val regs = Reg(new WriteBackBundle)

    regs   := io.in
    io.out := regs
}

class JBarrier extends Module {
    val io = IO(new Bundle{
        val in = Input(new JumpBundle)
        val out = Output(new JumpBundle)
    })
    // val regs = Reg(new JumpBundle)
    // regs := io.in
    // io.out := regs

    io.out := io.in
}