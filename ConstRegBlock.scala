package AXIChisel

import Chisel._
import Literal._
import Node._
import AXIDefs._

// a simple read-only register block AXI lite slave example
// the returned data values is the internal register address

class ConstRegBlock(addrBits: Int, dataBits: Int) extends Module {
  val io = new AXILiteSlaveIF(addrBits, dataBits)
  
  // as this is a read-only block, write response logic and such is constant
  // -- we do acknowledge writes and don't signal any errors, but writes have
  // simple no effect
  io.axi_awready := Bool(true)
  io.axi_wready := Bool(true)
  io.axi_bresp := UInt(0)
  io.axi_bvalid := Bool(true)
  
  // response follows request after 1 cycle, both for address and data.
  io.axi_arready := Reg(next = io.axi_arvalid)
  io.axi_rvalid := Reg(next = io.axi_rready)
  
  // loopback request address as data
  // note that we assume arvalid and rready are high at the same time --
  // otherwise the response data will be out of sync
  io.axi_rdata := Reg(next = io.axi_araddr)
  
  // read response always returns OK  
  io.axi_rresp := UInt(0) 
}

