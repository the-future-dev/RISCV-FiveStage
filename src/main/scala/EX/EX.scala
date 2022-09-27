package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase, MuxLookup}
import chisel3.experimental.MultiIOModule

import ALUOps._

class Execute extends MultiIOModule {
    val io = IO(
    new Bundle {
      val in = Input(new IDBundle)
      val out = Output(new EXBundle)
    }
  )

  val x = io.in.op1
  val y = io.in.op2

  val resultAlu = MuxLookup(io.in.aluOP, 0.U(32.W), Array(
    ADD -> (x + y),
    SUB -> (x - y),
  )).asTypeOf(UInt(32.W))

  io.out.pc           := io.in.pc
  io.out.writeData    := resultAlu

  io.out.writeReg     := io.in.writeReg && io.in.writeAddress =/= 0.U
  io.out.regData      := io.in.regData

  io.out.memRead      := io.in.memRead
  io.out.memWrite     := io.in.memWrite
  io.out.writeAddress := io.in.writeAddress
}