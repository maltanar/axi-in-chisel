package AXIChisel

import Chisel._
import Literal._
import Node._
import AXILiteDefs._

// a summation accelerator using AXI Lite Slave for control/status and AXI Lite master for reads
// hardcoded to use 32-bit data width
// waits until each read is completed before issuing new read, which results in
// pretty horrible performance

class SumAccel() extends Module {
  val io = new Bundle {
    val slave = new AXILiteSlaveIF(8, 32)
    val master = new AXILiteMasterIF(32, 32)
    val pulse = Bits(OUTPUT, 8)
  }
  
  io.slave.renameSignals()
  io.master.renameSignals()
  
  val sIdle :: sActive :: sWaitComplete :: sFinished :: Nil = Enum(UInt(), 4)
  val state = Reg(init = UInt(sIdle))
  
  /*
  val regBank = Vec.fill(8) { Reg(init=UInt(0,32) }
  val regStatus = regBank(0)
  val regCmd = regBank(1)
  val regStartAddr = regBank(2)
  val regCount = regBank(3)
  val regStride = regBank(4)
  val regSumResult = regBank(5)
  */
  
  val regSumResult = Reg(init=UInt(0,32))
  val regCurrentAddr = Reg(init=UInt(0,32))
  val regDataCount = Reg(init=UInt(0,32))
  
  
  
  // --------------------- <default outputs> ---------------------------------------
  // slave IF
  io.slave.writeAddr.ready  := Bool(true)
  io.slave.writeData.ready  := Bool(true)
  io.slave.writeResp.valid  := Reg(init=Bool(false), next=io.slave.writeAddr.valid)
  io.slave.writeResp.bits   := UInt(0)
  
  // master IF
  // read addr & data
  io.master.readAddr.bits.addr  := UInt(0)
  io.master.readAddr.bits.prot  := UInt(0)
  io.master.readAddr.valid      := Bool(false)
  io.master.readData.ready      := Bool(false)
  // write addr, data and response
  io.master.writeAddr.valid     := Bool(false)
  io.master.writeAddr.bits.addr := UInt(0)
  io.master.writeAddr.bits.prot := UInt(0)
  io.master.writeData.valid     := Bool(false)
  io.master.writeData.bits.data := UInt(0)
  io.master.writeData.bits.strb := UInt("b1111")
  io.master.writeResp.ready     := Bool(false)
  
  // status indicator
  io.pulse := Cat(regDataCount(5,0), UIntToOH(state))
  // --------------------- </default outputs> --------------------------------------
  
  when ( state === sIdle )
  {
    // TODO read start command properly from register bank
    val startCommand = io.slave.writeData.valid && (io.slave.writeData.bits.data === UInt("hf00dfeed"))
    // TODO make start address parametrizable
    regCurrentAddr := UInt("h10000000")
    regSumResult := UInt(0)
    regDataCount := UInt(0)
    
    when ( startCommand ) { state := sActive }
  }
  .elsewhen ( state === sActive )
  {
    // send out read request
    io.master.readAddr.valid := Bool(true)          // address valid
    io.master.readAddr.bits.addr := regCurrentAddr  // address to read
    
    when ( io.master.readAddr.ready ) { state := sWaitComplete}
  }
  .elsewhen ( state === sWaitComplete )
  {
    // wait for read response
    io.master.readData.ready := Bool(true)          // ready to accept read data
    
    // increment sum when read data is available
    when ( io.master.readData.valid ) 
    { 
      regSumResult := regSumResult + io.master.readData.bits.data 
      regDataCount := regDataCount + UInt(1)
      regCurrentAddr := regCurrentAddr + UInt(4)
      state := Mux(regDataCount === UInt(3), sFinished, sActive)
    }    
  }
  .elsewhen (state === sFinished)
  {
    val startCommand = io.slave.writeData.valid && (io.slave.writeData.bits.data === UInt("hf00dfeed"))
    when ( startCommand ) { state := sIdle }
  }
  
  // result reading (slave IF read channel)
  val readValidReg = Reg(init=Bool(false))
  
  when (!readValidReg)
  {
    readValidReg := io.slave.readAddr.valid
  }
  .otherwise
  {
    readValidReg := ~io.slave.readData.ready
  }
  
  io.slave.readAddr.ready := Bool(true)
  io.slave.readData.valid := readValidReg

  io.slave.readData.bits.resp   := UInt(0)    // always OK
  io.slave.readData.bits.data   := regSumResult
}

