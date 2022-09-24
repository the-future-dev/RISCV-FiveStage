package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase, MuxLookup }
import chisel3.experimental.MultiIOModule

class Execute extends MultiIOModule {
    val io = IO(
    new Bundle {
      val in = Input(new IDBundle)

    //   val out = Output(new EXBundle)
    }

  )
}