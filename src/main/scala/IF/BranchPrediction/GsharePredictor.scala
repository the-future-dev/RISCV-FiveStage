package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class GsharePredictor extends MultiIOModule {
    val io = IO(
        new Bundle{
            val address      = Input(UInt(32.W))
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
    
    val a1 = io.address(0)
    val a2 = io.address(1)
    val a3 = io.address(2)
    val a4 = io.address(3)
    val a5 = io.address(4)
    
    // Branch History Register 
    var b1 = RegInit(false.B)
    var b2 = RegInit(false.B)
    var b3 = RegInit(false.B)

    val addressHash = Mux(a1^b1, 1.U, 0.U) + Mux(a2^b2, 2.U, 0.U)+ Mux(a3^b3, 4.U, 0.U)+ Mux(a4, 8.U, 0.U)+ Mux(a5, 16.U, 0.U) 

    io.taken := PHT(addressHash).bet

    PHT(addressHash).we    := io.we
    PHT(addressHash).taken := io.actualTaken

    when(io.we){
        b1      := io.actualTaken
        b2      := b1
        b3      := b2
    }
}