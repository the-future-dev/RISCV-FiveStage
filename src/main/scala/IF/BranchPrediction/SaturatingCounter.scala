package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class SaturatingCounter extends MultiIOModule {
    val io = IO(
        new Bundle{
            val we      = Input(Bool())
            val taken   = Input(Bool())

            val bet     = Output(Bool())
        }
    )
    val state           = RegInit(UInt(2.W), 2.U)
    io.bet             := Mux(state >= 2.U, true.B, false.B)
    state              := Mux(io.we, Mux(io.taken & state=/= 3.U, state+1.U, Mux(!io.taken & state =/= 0.U, state-1.U, state)),state)
}