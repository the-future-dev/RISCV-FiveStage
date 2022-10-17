package FiveStage

import chisel3._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._


class CPU extends MultiIOModule {

  //mine: addi passed

  val testHarness = IO(
    new Bundle {
      val setupSignals = Input(new SetupSignals)
      val testReadouts = Output(new TestReadouts)
      val regUpdates   = Output(new RegisterUpdates)
      val memUpdates   = Output(new MemUpdates)
      val currentPC    = Output(UInt(32.W))
    }
  )

  //Barriers init
  val IFBarrier  = Module(new IFBarrier).io
  val IDBarrier  = Module(new IDBarrier).io
  val EXBarrier  = Module(new EXBarrier).io
  val MEMBarrier = Module(new MEMBarrier).io

  val JBarrier = Module(new JBarrier).io

  //Modules init
  val ID  = Module(new InstructionDecode)
  val IF  = Module(new InstructionFetch)
  val EX  = Module(new Execute)
  val MEM = Module(new MemoryFetch)
  val WB  = Module(new WriteBack) //(I may not need this one?)


  //testHarness init NO CHANGE PLS
  IF.testHarness.IMEMsetup     := testHarness.setupSignals.IMEMsignals
  ID.testHarness.registerSetup := testHarness.setupSignals.registerSignals
  MEM.testHarness.DMEMsetup    := testHarness.setupSignals.DMEMsignals

  testHarness.testReadouts.registerRead := ID.testHarness.registerPeek
  testHarness.testReadouts.DMEMread     := MEM.testHarness.DMEMpeek

  testHarness.regUpdates := ID.testHarness.testUpdates
  testHarness.memUpdates := MEM.testHarness.testUpdates
  testHarness.currentPC  := IF.testHarness.PC


  //Wiring
  IF.io.out     <>    IFBarrier.in
  ID.io.in      <>    IFBarrier.out
  ID.io.out     <>    IDBarrier.in
  EX.io.in      <>    IDBarrier.out
  EX.io.out     <>    EXBarrier.in
  MEM.io.in     <>    EXBarrier.out
  MEM.io.out    <>    MEMBarrier.in

  WB.io.in      <>    MEMBarrier.out
  WB.io.dmemData<>    MEM.io.dmemReadResult   //the data memory gives the value coordinnated to the data exiting the MEMBarrier
  WB.io.out     <>    ID.io.wb                //to execute the WB

  //stalling managing
  IF.io.stall   <>    ID.io.stall

  //jump managing
  ID.io.outJ    <>    JBarrier.in
  JBarrier.out  <>    IF.io.inJ

  //ID enhancing
  EX.io.out     <>    ID.io.ex
  MEM.io.out    <>    ID.io.mem
}
