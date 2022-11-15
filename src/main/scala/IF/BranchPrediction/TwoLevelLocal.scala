package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class LocalHistoryPredictor extends MultiIOModule {
    val io = IO(
        new Bundle{
            val address      = Input(UInt(32.W))
            val we           = Input(Bool())
            val actualTaken  = Input(Bool())

            val taken        = Output(Bool())
        }
    )
    val PHT = VecInit(Seq.fill(2^5)(Module(new SaturatingCounter()).io))
    val BHT = VecInit(Seq.fill(2^3)(Module(new ShiftRegister()).io))

    PHT.zipWithIndex.foreach({ case(x, i)=>
        x.taken := false.B
        x.we    := false.B
    })

    BHT.zipWithIndex.foreach({ case(x, i)=>
        x.in    := false.B
        x.we    := false.B
    })
    
    val addressHash = Mux(io.address(17), 8.U, 0.U) + Mux(io.address(29), 16.U, 0.U) 
    val nBitHash    = Mux(io.address(0), 1.U, 0.U) + Mux(io.address(7), 2.U, 0.U) + Mux(io.address(9), 4.U, 0.U)

    io.taken := PHT(addressHash+BHT(nBitHash).out).bet

    PHT(nBitHash).we    := io.we
    PHT(nBitHash).taken := io.actualTaken
}