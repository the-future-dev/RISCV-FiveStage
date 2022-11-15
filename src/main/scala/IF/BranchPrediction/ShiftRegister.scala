package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class ShiftRegister extends MultiIOModule {
    val io = IO(
        new Bundle{
            val in           = Input(Bool())
            val we           = Input(Bool())
            val out          = Output(UInt(3.W))
        }
    )
    var b1 = RegInit(false.B)
    var b2 = RegInit(false.B)
    var b3 = RegInit(false.B)

    when(io.we){
        b1      := io.in
        b2      := b1
        b3      := b2
    }
    io.out     :=   Mux(b1, 1.U, 0.U) + Mux(b2, 2.U, 0.U) + Mux(b3, 4.U, 0.U)
}
