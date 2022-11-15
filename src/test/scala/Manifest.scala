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

  val singleTest = "memoFib.s"

  val nopPadded = false

  val singleTestOptions = TestOptions(
    printIfSuccessful  = true,
    printErrors        = true,
    printParsedProgram = false,
    printVMtrace       = false,
    printVMfinal       = false,
    printMergedTrace   = false,
    printBinary        = false,
    nopPadded          = nopPadded,
    breakPoints        = Nil, // not implemented
    testName           = singleTest,
    // maxSteps           = 200)
    // maxSteps           = 700)
    // maxSteps           = 1000)
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

class Milestone1 extends FlatSpec with Matchers {
  val parts = Array(
    //Milestone 1
    "arith.s",
    "addi.s",
    "arithImm.s",
    "forward1.s",
    "forward2.s",
    "load.s",
    "load2.s",
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

class Milestone2 extends FlatSpec with Matchers {
  val parts = Array(
    //Milestone 2
    "add.s",
    "BTreeManyO3.s",
    "BTreeO3.s",
    "constants.s",
    "memoFib.s",
    "naiveFib.s",
    "palindrome.s",
    "palindromeO3.s",
    "searchRegularO0.s",
    "square.s", 
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
