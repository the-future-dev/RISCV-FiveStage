package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase, MuxLookup}
import chisel3.experimental.MultiIOModule

import ALUOps._

class Execute extends MultiIOModule {
    val io = IO(
    new Bundle {
      val in          = Input(new IDBundle)
      val fwdIn       = Input(new FwdEx)
      val wb          = Input(new WriteBackBundle)
      val mem         = Input(new MEMBundle)
      val ex          = Input(new EXBundle)

      val out         = Output(new EXBundle)
    }
  )

  val x = Mux(io.ex.regWrite && (io.ex.writeAddress === io.fwdIn.address1), io.ex.writeData,
            Mux(io.mem.regWrite && (io.mem.writeAddress === io.fwdIn.address1), io.mem.writeData,
              Mux(io.wb.writeEnable && (io.wb.writeAddress === io.fwdIn.address1), io.wb.writeData, io.in.op1)))
  
  val y = Mux(io.ex.regWrite && (io.ex.writeAddress === io.fwdIn.address2), io.ex.writeData,
            Mux(io.mem.regWrite && (io.mem.writeAddress === io.fwdIn.address2), io.mem.writeData,
              Mux(io.wb.writeEnable && (io.wb.writeAddress === io.fwdIn.address2), io.wb.writeData, io.in.op2)))

  val resultAlu = MuxLookup(io.in.aluOP, 0.U(32.W), Array(
    ADD   -> (x + y),
    SUB   -> (x - y),
    AND   -> (x & y),
    OR    -> (x | y),
    XOR   -> (x ^ y),
    SLL   -> (x << y(4, 0)),
    SRL   -> (x >> y(4, 0)),
    SRA   -> (x.asSInt >> y(4, 0)).asUInt,
    SLT   -> (x.asSInt < y.asSInt),
    SLTU  -> (x < y),
    COPY_A-> x,
    COPY_B-> y,
    DC    -> 0.U
  )).asTypeOf(UInt(32.W))

  //TO -> MEMORY FETCH
  io.out.pc           := io.in.pc

  io.out.regWrite     := io.in.regWrite && io.in.writeAddress =/= 0.U
  io.out.memData      := Mux(io.ex.regWrite && (io.ex.writeAddress === io.fwdIn.memDSrc), io.ex.writeData,
                          Mux(io.mem.regWrite && (io.mem.writeAddress === io.fwdIn.memDSrc), io.mem.writeData,
                            Mux(io.wb.writeEnable && (io.fwdIn.memDSrc === io.wb.writeAddress), io.wb.writeData, io.in.memData)))
  
  io.out.writeData    := resultAlu

  io.out.memRead      := io.in.memRead
  io.out.memWrite     := io.in.memWrite
  
  io.out.writeAddress := io.in.writeAddress
}