package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase }
import chisel3.experimental.MultiIOModule

class WriteBack extends MultiIOModule {
    val io = IO(
    new Bundle {
      val inEXE       = Input(new EXBundle)
      val dmemData    = Input(UInt(32.W))

      val out         = Output(new WriteBackBundle)
    }
  )
  io.out.pc           := io.inEXE.pc

  io.out.writeEnable  := io.inEXE.regWrite
  io.out.writeData    := Mux(io.inEXE.memRead, io.dmemData, io.inEXE.writeData)
  io.out.writeAddress := io.inEXE.writeAddress
}