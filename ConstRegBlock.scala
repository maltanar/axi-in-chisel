package AXIChisel

import Chisel._
import Literal._
import Node._
import AXIDefs._

// a simple read-only register block AXI lite slave example
// the returned data value from a read is the internal register address

class ConstRegBlock(addrBits: Int, dataBits: Int) extends Module {
  val io = new AXILiteSlaveIF(addrBits, dataBits)
  
  // as this is a read-only block, write response logic and such is constant
  // -- we do acknowledge writes and don't signal any errors, but writes have
  // simply no effect
  io.writeAddr.ready  := Bool(true)
  io.writeData.ready  := Bool(true)
  io.writeResp.bits   := UInt(0)
  io.writeResp.valid  := Bool(true)
  
  // response follows request after 1 cycle, both for address and data.
  io.readAddr.ready := Reg(next = io.readAddr.valid)
  io.readData.valid := Reg(next = io.readData.ready)
  
  // loopback request address as data
  io.readData.bits.data   := Reg(next = io.readAddr.bits.addr)
  // read response always returns OK in our case  
  io.readData.bits.resp   := UInt(0)
}

