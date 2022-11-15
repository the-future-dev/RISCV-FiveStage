package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class GlobalHistoryPredictor extends MultiIOModule {
    val io = IO(
        new Bundle{
            val address      = Input(UInt(32.W))
            val globHistory  = Input(UInt(3.W))

            val we           = Input(Bool())
            val actualTaken  = Input(Bool())

            val taken        = Output(Bool())
        }
    )
    val PHT = VecInit(Seq.fill(2^5)(Module(new SaturatingCounter()).io))
    
    PHT.zipWithIndex.foreach({ case(x, i)=>
        x.taken := false.B
        x.we    := false.B
    })
    
    val addressHash = Mux(io.address(3), 8.U, 0.U) + Mux(io.address(4), 16.U, 0.U) 

    io.taken := PHT(addressHash+io.globHistory).bet

    PHT(addressHash+io.globHistory).we    := io.we
    PHT(addressHash+io.globHistory).taken := io.actualTaken
}