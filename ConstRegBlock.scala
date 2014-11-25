package AXIChisel

import Chisel._
import Literal._
import Node._
import AXILiteDefs._

// a simple read-only register block AXI lite slave example
// returns a constant value for all addresses
// TODO reading causes processor to freeze - what can be wrong?


class ConstRegBlock(addrBits: Int, dataBits: Int) extends Module {
  val io = new AXILiteSlaveIF(addrBits, dataBits)
  
  io.renameSignals()
  
  // as this is a read-only block, write response logic and such is constant
  // -- we do acknowledge writes and don't signal any errors, but writes have
  // simply no effect
  io.writeAddr.ready  := Bool(true)
  io.writeData.ready  := Bool(true)
  io.writeResp.bits   := UInt(0)
  io.writeResp.valid  := Bool(true)
  
  io.readAddr.ready := Bool(true)
  
  // loopback request address as data
  //io.readData.bits.data   := Reg(next = io.readAddr.bits.addr)
  io.readData.bits.data   := UInt(0xdeadbeef)
  io.readData.valid       := Bool(true)
  
  // read response always returns OK in our case  
  io.readData.bits.resp   := UInt(0)
}

