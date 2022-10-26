package FiveStage

import org.scalatest.{Matchers, FlatSpec}
import cats._
import cats.implicits._
import fileUtils._

import chisel3.iotesters._
import scala.collection.mutable.LinkedHashMap

import fansi.Str

import Ops._
import Data._
import VM._

import PrintUtils._
import LogParser._

object Manifest {

  // val singleTest = "jump.s"
  // val singleTest = "load.s"
  val singleTest = "memoFib.s"
  // val singleTest = "square.s"

  val nopPadded = false

  val singleTestOptions = TestOptions(
    printIfSuccessful  = true,
    printErrors        = true,
    printParsedProgram = false,
    printVMtrace       = false,
    printVMfinal       = false,
    printMergedTrace   = true,
    printBinary        = false,
    nopPadded          = nopPadded,
    breakPoints        = Nil, // not implemented
    testName           = singleTest,
    // maxSteps           = 200)
    maxSteps           = 15000)


  val allTestOptions: String => TestOptions = name => TestOptions(
    printIfSuccessful  = false,
    printErrors        = false,
    printParsedProgram = false,
    printVMtrace       = false,
    printVMfinal       = false,
    printMergedTrace   = false,
    printBinary        = false,
    nopPadded          = nopPadded,
    breakPoints        = Nil, // not implemented
    testName           = name,
    maxSteps           = 3000)

}



class ProfileBranching extends FlatSpec with Matchers {
  it should "profile some branches" in {
    BranchProfiler.profileBranching(
      Manifest.singleTestOptions.copy(testName = "branchProfiling.s", maxSteps = 150000)
    ) should be(true)
  }
}

class ProfileCache extends FlatSpec with Matchers {
  it should "profile a cache" in {
    CacheProfiler.profileCache(
      Manifest.singleTestOptions.copy(testName = "convolution.s", maxSteps = 150000)
    ) should be(true)
  }
}

class SingleTest extends FlatSpec with Matchers {
  it should "just werk" in {
    TestRunner.run(Manifest.singleTestOptions) should be(true)
  }
}


class AllTests extends FlatSpec with Matchers {
  it should "just werk" in {
    val werks = getAllTestNames.filterNot(_ == "convolution.s").filterNot(_ == "branchProfiling.s").map{testname => 
      say(s"testing $testname")
      val opts = Manifest.allTestOptions(testname)
      (testname, TestRunner.run(opts))
    }
    if(werks.foldLeft(true)(_ && _._2))
      say(Console.GREEN + "All tests successful!" + Console.RESET)
    else {
      val success = werks.map(x => if(x._2) 1 else 0).sum
      val total   = werks.size
      say(s"$success/$total tests successful")
      werks.foreach{ case(name, success) =>
        val msg = if(success) Console.GREEN + s"$name successful" + Console.RESET
        else Console.RED + s"$name failed" + Console.RESET
        say(msg)
      }
    }
  }
}

class PartsTests extends FlatSpec with Matchers {
  val parts = Array(
    //Milestone 1 - OK for now
    "arith.s",
    "addi.s",
    "arithImm.s",
    "forward1.s",
    "forward2.s",
    "load.s",
    "load2.s",
    "jump.s",
    "jump2.s",

    //Milestone 2
    // "add.s",
    // "BTreeManyO3.s",
    // "BTreeO3.s",
    // "constants.s",
    // "memoFib.s",
    // "naiveFib.s",
    // "palindrome.s",
    // "palindromeO3.s",
    // "searchRegularO0.s",
    // "square.s", 
  )
  it should "just werk" in {
    val werks = parts.filterNot(_ == "convolution.s").map{testname => 
      say(s"testing $testname")
      val opts = Manifest.allTestOptions(testname)
      (testname, TestRunner.run(opts))
    }
    if(werks.foldLeft(true)(_ && _._2))
      say(Console.GREEN + "All tests successful!" + Console.RESET)
    else {
      val success = werks.map(x => if(x._2) 1 else 0).sum
      val total   = werks.size
      say(s"$success/$total tests successful")
      werks.foreach{ case(name, success) =>
        val msg = if(success) Console.GREEN + s"$name successful" + Console.RESET
        else Console.RED + s"$name failed" + Console.RESET
        say(msg)
      }
    }
  }
}


/**
  * Not tested at all
  */
class AllTestsWindows extends FlatSpec with Matchers {
  it should "just werk" in {
    val werks = getAllWindowsTestNames.filterNot(_ == "convolution.s").map{testname => 
      say(s"testing $testname")
      val opts = Manifest.allTestOptions(testname)
      (testname, TestRunner.run(opts))
    }
    if(werks.foldLeft(true)(_ && _._2))
      say(Console.GREEN + "All tests successful!" + Console.RESET)
    else {
      val success = werks.map(x => if(x._2) 1 else 0).sum
      val total   = werks.size
      say(s"$success/$total tests successful")
      werks.foreach{ case(name, success) =>
        val msg = if(success) Console.GREEN + s"$name successful" + Console.RESET
        else Console.RED + s"$name failed" + Console.RESET
        say(msg)
      }
    }
  }
}



class MyTest extends FlatSpec with Matchers {
  behavior of "General structure: not pipelined"
  it should "desplay what is happening" in{
    say(s"\t STARTING")

    chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on", "--backend-name", "treadle"),
      () => new InstructionDecode())
      {
        c => new MyTestRunner(c)
    } should be(true)
  }
}

class MyTestRunner(c: InstructionDecode) extends chisel3.iotesters.PeekPokeTester(c){
  say(s"INIT:\n")

  for(i <- 0 until 45){
    say(s"\n\n\n\nClock: $i\n")
    // val input = scala.util.Random.nextInt(10)
    // poke(c.io.PCIn, (input))
    
    // val o = peek(c.IFBarrier.PCOut)
    // val o2 = peek(c.IF.io.PC)
    // say(s"\nPC IF:\t$o2"
    // say(s"\nPC barrier: $o")

    // expect(c.io.PCOut, input)
    step(1)
  }

  say("END:")
}