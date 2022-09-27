package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase }
import chisel3.experimental.MultiIOModule

class WriteBack extends MultiIOModule {
    val io = IO(
    new Bundle {
      val in = Input(new MEMBundle)
      val out = Output(new WriteBackBundle)
    }
  )
  io.out.pc   := io.in.pc

  io.out.writeEnable  := io.in.regWrite
  io.out.writeData    := io.in.writeData
  io.out.writeAddress := io.in.writeAddress
}