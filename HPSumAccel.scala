package AXIChisel

import Chisel._
import Literal._
import Node._
import AXILiteDefs._
import AXIDefs._

// a summation accelerator using AXI Lite Slave for control/status and AXI Master for reads
// hardcoded to use 32 bit address, 64 bit data, 1 bit ID
// tested with the 64-bit High-Performance port on the Zynq (on a ZedBoard RevC)

class HPSumAccel() extends Module {
  val io = new Bundle {
    val slave = new AXILiteSlaveIF(8, 32)
    val master = new AXIMasterIF(32, 64, 1)
    val debug = Bits(OUTPUT, width = 8)
  }
  
  io.slave.renameSignals()
  io.master.renameSignals()
  
  // constants
  val dataBits = 64
  val addrBits = 32
  val wordSize = 32
  val burstBeatCount = 256
  val wordsPerBeat = dataBits/wordSize
  
  // states for the control FSM
  val sIdle :: sReadStart :: sReadCount :: sActive :: sWaitData :: sFinished :: sError :: Nil = Enum(UInt(), 7)
  val state = Reg(init = UInt(sIdle))
  
  // "magic word" definitions
  // control transition from idle->active and finished->idle
  val cmdProceed = UInt("hf00dfeed")
  
  io.debug := UIntToOH(state)
  
  // configuration registers for read operation
  val regStartAddress = Reg(init = UInt(0, 32))
  val regTotalWords = Reg(init = UInt(0, 32))
  
  // benchmarking registers
  val regTime = Reg(init = UInt(0,32))
  val clkCount = Reg(init = UInt(0,32))
  clkCount := clkCount + UInt(1)
  
  // status registers for read operation
  val regWordCount = Reg(init = UInt(0, 32))
  val regSumResult = Reg(init = UInt(0, 32))
  
  // **************************** <default outputs> ***************************************
  // master read address & data
  io.master.readAddr.valid := Bool(false)
  io.master.readAddr.bits.addr := regStartAddress
  io.master.readAddr.bits.size := UInt(log2Up((dataBits/8)-1)) // full-datawidth bursts
  io.master.readAddr.bits.burst := UInt(1) // incrementing burst
  io.master.readAddr.bits.len := UInt(burstBeatCount-1) // defined to be burst beats minus 1
  io.master.readAddr.bits.cache := UInt("b0010") // no alloc, modifiable, no buffer
  io.master.readAddr.bits.prot := UInt(0)
  io.master.readAddr.bits.qos := UInt(0)
  io.master.readAddr.bits.lock := Bool(false)
  io.master.readAddr.bits.id := UInt(1) // use constant id (no reordering of transactions)
  io.master.readData.ready := Bool(false)
  // master write address & data
  io.master.writeData.valid := Bool(false)
  io.master.writeAddr.valid := Bool(false)
  io.master.writeResp.ready := Bool(false)
  io.master.writeAddr.bits.addr := UInt(0)
  io.master.writeAddr.bits.prot := UInt(0)
  io.master.writeAddr.bits.size := UInt(0)
  io.master.writeAddr.bits.len := UInt(0)
  io.master.writeAddr.bits.burst := UInt(0)
  io.master.writeAddr.bits.lock := Bool(false)
  io.master.writeAddr.bits.cache := UInt(0)
  io.master.writeAddr.bits.qos := UInt(0)
  io.master.writeAddr.bits.id := UInt(0)
  io.master.writeData.bits.data := UInt(0)
  io.master.writeData.bits.strb := UInt(0)
  io.master.writeData.bits.last := Bool(false)
  
  // TODO let's see if these automatically get zeroed out
  // slave write IF
  io.slave.writeAddr.ready  := Bool(true)
  io.slave.writeData.ready  := Bool(true)
  io.slave.writeResp.valid  := Reg(init=Bool(false), next=io.slave.writeAddr.valid)
  io.slave.writeResp.bits   := UInt(0)
  // **************************** </default outputs> **************************************
  
  // ****************** <result reading (slave IF read channel)> ***********************
  val readValidReg = Reg(init=Bool(false))
  when ( !readValidReg ) { readValidReg := io.slave.readAddr.valid }
  .otherwise { readValidReg := ~io.slave.readData.ready }
  io.slave.readAddr.ready := Bool(true)
  io.slave.readData.valid := readValidReg
  io.slave.readData.bits.resp   := UInt(0)
  val readAddrReg = Reg(init=UInt(0,8))
  when (io.slave.readAddr.valid) { readAddrReg := io.slave.readAddr.bits.addr }
  io.slave.readData.bits.data   := Mux(readAddrReg(2), regTime, regSumResult) 
  // ****************** </result reading (slave IF read channel)> ***********************
  
  
  // **************************** <control state machine> **********************************
  
  
  val readError = io.master.readData.bits.resp != UInt(0)
  //when (readError) { state := sError}
  
  val proceed = io.slave.writeData.valid && (io.slave.writeData.bits.data === UInt("hf00dfeed"))
  
  when ( state === sIdle )
  {
    regWordCount := UInt(0)
    regSumResult := UInt(0)
    when ( proceed) { state := sReadStart }
  }
  .elsewhen ( state === sReadStart )
  {
    // read and register start address
    when ( io.slave.writeData.valid )
    {
      regStartAddress := io.slave.writeData.bits.data
      state := sReadCount
    }
  }
  .elsewhen ( state === sReadCount )
  {
    // read and register read count
    when ( io.slave.writeData.valid )
    {
      regTime := clkCount
      regTotalWords := io.slave.writeData.bits.data
      state := sActive
    }
  }
  .elsewhen ( state === sActive )
  {
    // enable reads    
    io.master.readAddr.valid := Bool(true)
    when ( io.master.readAddr.ready ) { state := sWaitData}
  }
  .elsewhen ( state === sWaitData )
  {
    io.master.readData.ready := Bool(true)
    
    // update sum whenever data is available
    when ( io.master.readData.valid )
    {
      regWordCount := regWordCount + UInt(wordsPerBeat)
      // increment sum
      val newDataBeat = io.master.readData.bits.data
      // TODO should be parametrizable depending on wordsPerBeat -- for now we
      // hardcode for 2 words per beat 
      val newDataBeatSum = UInt(newDataBeat(63,32)) + UInt(newDataBeat(31,0))
      regSumResult := regSumResult + newDataBeatSum
      
      // consider when to go on last data beat
      when ( io.master.readData.bits.last )
      {
        // increment start address by 1 burst 
        regStartAddress := regStartAddress + UInt((addrBits/8)*wordsPerBeat*burstBeatCount)
        // next state: either issue new request or go to finish
        when ( regWordCount === regTotalWords - UInt(wordsPerBeat) ) 
        { 
          state := sFinished
          regTime := clkCount - regTime
        }
        .otherwise { state := sActive}
      }
    }
  }
  .elsewhen ( state === sFinished )
  {
    when ( proceed) { state := sIdle }
  }
  .elsewhen ( state === sError )
  {
    // nothing to do
  }
}
