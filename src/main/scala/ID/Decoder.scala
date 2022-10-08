package FiveStage
import chisel3._
import chisel3.util.BitPat
import chisel3.util.ListLookup


/**
  * This module is mostly done, but you will have to fill in the blanks in opcodeMap.
  * You may want to add more signals to be decoded in this module depending on your
  * design if you so desire.
  *
  * In the "classic" 5 stage decoder signals such as op1select and immType
  * are not included, however I have added them to my design, and similarily you might
  * find it useful to add more
 */
class Decoder() extends Module {

  val io = IO(new Bundle {
                val instruction    = Input(new Instruction)

                val controlSignals = Output(new ControlSignals)
                val branchType     = Output(UInt(3.W))
                val op1Select      = Output(UInt(1.W))
                val op2Select      = Output(UInt(1.W))
                val immType        = Output(UInt(3.W))
                val ALUop          = Output(UInt(4.W))
              })

  import lookup._
  import Op1Select._
  import Op2Select._
  import branchType._
  import ImmFormat._

  val N = 0.asUInt(1.W)
  val Y = 1.asUInt(1.W)

  /**
    * In scala we sometimes (ab)use the `->` operator to create tuples.
    * The reason for this is that it serves as convenient sugar to make maps.
    *
    * This doesn't matter to you, just fill in the blanks in the style currently
    * used, I just want to demystify some of the scala magic.
    *
    * `a -> b` == `(a, b)` == `Tuple2(a, b)`
    */
  val opcodeMap: Array[(BitPat, List[UInt])] = Array(

    // signal      regWrite, memRead, memWrite, branch,  jump, branchType,    Op1Select, Op2Select, ImmSelect,    ALUOp
    ADD    -> List(Y, N, N, N, N, branchType.DC, rs1,    rs2, IMFDC, ALUOps.ADD),
    SUB    -> List(Y, N, N, N, N, branchType.DC, rs1,    rs2, IMFDC, ALUOps.SUB),
    AND    -> List(Y, N, N, N, N, branchType.DC, rs1,    rs2, IMFDC, ALUOps.AND),
    OR     -> List(Y, N, N, N, N, branchType.DC, rs1,    rs2, IMFDC, ALUOps.OR),
    XOR    -> List(Y, N, N, N, N, branchType.DC, rs1,    rs2, IMFDC, ALUOps.XOR),
    SLT    -> List(Y, N, N, N, N, branchType.DC, rs1,    rs2, IMFDC, ALUOps.SLT),
    SLTU   -> List(Y, N, N, N, N, branchType.DC, rs1,    rs2, IMFDC, ALUOps.SLTU),
    SRA    -> List(Y, N, N, N, N, branchType.DC, rs1,    rs2, IMFDC, ALUOps.SRA),
    SRL    -> List(Y, N, N, N, N, branchType.DC, rs1,    rs2, IMFDC, ALUOps.SRL),
    SLL    -> List(Y, N, N, N, N, branchType.DC, rs1,    rs2, IMFDC, ALUOps.SLL),
    ADDI   -> List(Y, N, N, N, N, branchType.DC, rs1,    imm, ITYPE, ALUOps.ADD),
    ANDI   -> List(Y, N, N, N, N, branchType.DC, rs1,    imm, ITYPE, ALUOps.AND),
    ORI    -> List(Y, N, N, N, N, branchType.DC, rs1,    imm, ITYPE, ALUOps.OR),
    XORI   -> List(Y, N, N, N, N, branchType.DC, rs1,    imm, ITYPE, ALUOps.XOR),
    SLTI   -> List(Y, N, N, N, N, branchType.DC, rs1,    imm, ITYPE, ALUOps.SLT),
    SLTIU  -> List(Y, N, N, N, N, branchType.DC, rs1,    imm, ITYPE, ALUOps.SLTU),
    SRAI   -> List(Y, N, N, N, N, branchType.DC, rs1,    imm, ITYPE, ALUOps.SRA),
    SRLI   -> List(Y, N, N, N, N, branchType.DC, rs1,    imm, ITYPE, ALUOps.SRL),
    SLLI   -> List(Y, N, N, N, N, branchType.DC, rs1,    imm, ITYPE, ALUOps.SLL),
    LUI    -> List(Y, N, N, N, N, branchType.DC, Op1Select.DC,  imm, UTYPE, ALUOps.COPY_B),
//AUIPC
    AUIPC  -> List(Y, N, N, N, N, branchType.DC, PC,     imm, UTYPE, ALUOps.ADD),
    LW     -> List(Y, Y, N, N, N, branchType.DC, rs1,    imm, ITYPE, ALUOps.ADD),
    SW     -> List(N, N, Y, N, N, branchType.DC, rs1,    imm, STYPE, ALUOps.ADD),
//JAL              (N in practice -> check)
    JAL    -> List(Y, N, N, N, Y, branchType.DC, PC,     imm, JTYPE, ALUOps.DC),
//JALR             (N in practice -> check)
    JALR   -> List(Y, N, N, N, Y, branchType.DC, rs1,    imm, ITYPE, ALUOps.DC),
//BEQ
    BEQ    -> List(N, N, N, Y, Y,  branchType.beq,rs1,   rs2, BTYPE, ALUOps.DC),
//BNE
    BNE    -> List(N, N, N, Y, Y, branchType.neq, rs1,   rs2, BTYPE, ALUOps.DC),
//BLT
    BLT    -> List(N, N, N, Y, Y, branchType.lt,  rs1,   rs2, BTYPE, ALUOps.DC),
//BGE
    BGE    -> List(N, N, N, Y, Y, branchType.gte, rs1,   rs2, BTYPE, ALUOps.DC),
//BLTU
    BLTU   -> List(N, N, N, Y, Y, branchType.ltu, rs1,   rs2, BTYPE, ALUOps.DC),
//BGEU
    BGEU   -> List(N, N, N, Y, Y, branchType.gteu,rs1,   rs2, BTYPE, ALUOps.DC),
    )


  val NOP = List(N, N, N, N, N, branchType.DC, rs1, rs2, IMFDC, ALUOps.DC)

  val decodedControlSignals = ListLookup(
    io.instruction.asUInt(),
    NOP,
    opcodeMap)

  io.controlSignals.regWrite   := decodedControlSignals(0)
  io.controlSignals.memRead    := decodedControlSignals(1)
  io.controlSignals.memWrite   := decodedControlSignals(2)
  io.controlSignals.branch     := decodedControlSignals(3)
  io.controlSignals.jump       := decodedControlSignals(4)

  io.branchType := decodedControlSignals(5)
  io.op1Select  := decodedControlSignals(6)
  io.op2Select  := decodedControlSignals(7)
  io.immType    := decodedControlSignals(8)
  io.ALUop      := decodedControlSignals(9)
}
