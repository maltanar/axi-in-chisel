package AXIChisel

import Chisel._
import Literal._
import Node._
import AXIDefs._

// a summation accelerator using AXI Lite Slave for control/status and AXI Master for reads
// hardcoded to use 32-bit data width

class SumAccel() extends Module {
  val io = new Bundle {
    val slave = new AXILiteSlaveIF(8, 32)
    val master = new AXILiteMasterIF(32, 32)
    val pulse = Bits(OUTPUT, 1)
  }
  
  io.slave.renameSignals()
  io.master.renameSignals()
  
  val sInit :: sSendAW :: sSendW :: sWaitWResp :: Nil = Enum(UInt(), 4)
  val state = Reg(init = UInt(sInit))
  
  // drive default outputs
  // slave IF
  io.slave.writeAddr.ready  := Bool(false)
  io.slave.writeData.ready  := Bool(false)
  io.slave.writeResp.valid  := Bool(false)
  io.slave.writeResp.bits   := UInt(0)
  io.slave.readAddr.ready   := Bool(false)
  io.slave.readData.valid   := Bool(false)
  io.slave.readData.bits.data := UInt(0)
  io.slave.readData.bits.resp := UInt(0)
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
  
  // clock counter register
  val regClkCount = Reg(init=UInt(0,32))
  val regTickCount = Reg(init=UInt(0,32))
  val regPulse = Reg(init=Bits(0))
  
  io.pulse := regPulse
  
  when (regClkCount === UInt(1*100000000)) 
  {
    regClkCount := UInt(0)
    regTickCount := regTickCount + UInt(1)
    regPulse := ~regPulse
  }
  .otherwise
  {
    regClkCount := regClkCount + UInt(1)
  }
  
  when (state === sInit)
  {
    //when (regTickCount(0) === UInt(1) ) { state := sSendAW }
    state := sSendAW
  }
  .elsewhen ( state === sSendAW )
  {
    io.master.writeAddr.bits.addr := UInt("h10000000")
    io.master.writeAddr.valid := Bool(true)
    when (io.master.writeAddr.ready) { state := sSendW}
  }
  .elsewhen (state === sSendW)
  {
    io.master.writeData.bits.data := UInt("hdeadbeef") + regTickCount
    io.master.writeData.valid := Bool(true)
    when (io.master.writeData.ready) { state := sWaitWResp}
  }
  .elsewhen (state === sWaitWResp)
  {
    io.master.writeResp.ready := Bool(true)
    when (io.master.writeResp.valid) { state := sInit }
  }

  
  // TODO implement slave interface with status and control:
  // - status register (idle / busy)
  // - result register (sum)
  // - cycle count register (for performance measurement)
  // - start address
  // - element count
  // - control register (start/stop)
  
  // TODO implement master interface for requesting data
  // and summing the result
  
}

