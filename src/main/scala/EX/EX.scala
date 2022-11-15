package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase, MuxLookup}
import chisel3.experimental.MultiIOModule

import ALUOps._
import Op1Select._
import Op2Select._

class Execute extends MultiIOModule {
    val io = IO(
    new Bundle {
      val in          = Input(new IDBundle)
      val wb          = Input(new WriteBackBundle)

      val out         = Output(new EXBundle)
    }
  )

  //forwarding
  val x = Mux(io.wb.writeEnable && (io.wb.writeAddress === io.in.address1), io.wb.writeData,
              MuxLookup(io.in.op1sel, 0.U(32.W), Array(
                rs1          -> io.in.op1,
                PC           -> io.in.pc
              ))
            )
  
  val y = Mux(io.wb.writeEnable && (io.wb.writeAddress === io.in.address2), io.wb.writeData,
              MuxLookup(io.in.op2sel, 0.U(32.W), Array(
                rs2   -> io.in.op2,
                imm   -> io.in.imm
              ))
            )

  //ALU execution
  val resultAlu = MuxLookup(io.in.aluOP, 0.U(32.W), Array(
    ADD               -> (x + y),
    SUB               -> (x - y),
    AND               -> (x & y),
    OR                -> (x | y),
    XOR               -> (x ^ y),
    SLL               -> (x << y(4, 0)),
    SRL               -> (x >> y(4, 0)),
    SRA               -> (x.asSInt >> y(4, 0)).asUInt,
    SLT               -> (x.asSInt < y.asSInt),
    SLTU              -> (x < y),
    COPY_A            -> x,
    COPY_B            -> y,
    ALUOps.DC         -> 0.U
  )).asTypeOf(UInt(32.W))

  //TO -> MEMORY FETCH
  io.out.pc           := io.in.pc
  io.out.regWrite     := io.in.regWrite && io.in.writeAddress =/= 0.U
  io.out.memRead      := io.in.memRead
  io.out.memWrite     := io.in.memWrite
  io.out.writeAddress := io.in.writeAddress

  io.out.writeData    := resultAlu
}