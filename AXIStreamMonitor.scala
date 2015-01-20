package AXIChisel

import Chisel._
import AXIStreamDefs._
import Node._

// A simple module for measuring the throughput and latency of an AXI stream master interface
// Also provides the sum of the inputs for checking correctness

class AXIStreamMonitor extends Module {
  val io = new Bundle {
    // inputs for controlling the monitor
    val start = Bool(INPUT)
    val resetCounters = Bool(INPUT)
    val stopOnCount = UInt(INPUT, 32)
    // status and performance counter outputs
    val dataCount = UInt(OUTPUT, 32)
    val totalCycles = UInt(OUTPUT, 32)
    val idleCycles = UInt(OUTPUT, 32)
    val sum = UInt(OUTPUT, 32)
    // AXI stream slave interface for receiving data
    val data = new AXIStreamSlaveIF(UInt(width = 32))
  }
  // rename AXI stream signals for Vivado auto-inference
  io.data.renameSignals("data")
  
  // FSM definitions
  val sIdle :: sResetCtrs :: sActive :: Nil = Enum(UInt(), 3)
  val regState = Reg(init = UInt(sIdle))
  
  // registers
  val regCycleCount = Reg(init = UInt(0, width = 32))
  val regIdleCycleCount = Reg(init = UInt(0, width = 32))
  val regDataCount = Reg(init = UInt(0, width = 32))
  val regSum = Reg(init = UInt(0, width = 32))
  val regStopOnCount = Reg(init = UInt(0, width = 32))
  
  // default outputs
  io.dataCount := regDataCount
  io.totalCycles := regCycleCount
  io.idleCycles := regIdleCycleCount
  io.sum := regSum
  io.data.ready := Bool(false)
  
  when ( regState === sIdle )
  {
    when ( io.resetCounters ) { regState := sResetCtrs }
    .elsewhen ( io.start ) 
    { 
      regState := sActive 
      regStopOnCount := io.stopOnCount
    } 
  }
  .elsewhen ( regState === sResetCtrs )
  {
    regState := sIdle
    regCycleCount := UInt(0)
    regIdleCycleCount := UInt(0)
    regDataCount := UInt(0)
    regSum := UInt(0)
    regStopOnCount := UInt(0)
  }
  .elsewhen ( regState === sActive )
  {
    io.data.ready := Bool(true)
    regCycleCount := regCycleCount + UInt(1)
    
    when ( regDataCount === regStopOnCount ) { regState := sIdle }
    .elsewhen ( io.data.valid ) { 
      regDataCount := regDataCount + UInt(1) 
      regSum := regSum + io.data.bits
    } .otherwise {
      regIdleCycleCount := regIdleCycleCount + UInt(1)
    }
  }
  
}
