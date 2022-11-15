package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class BranchHistoryRegister extends MultiIOModule {
    val io = IO(
        new Bundle{
            val taken        = Input(Bool())
            val we           = Input(Bool())

            val history      = Output(UInt(3.W))
        }
    )
    val reg = Module(new ShiftRegister()).io
    reg.in := io.taken
    reg.we := io.we
    io.history := reg.out
}
