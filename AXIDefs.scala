package AXIDefs
{

import Chisel._
import Literal._
import Node._

// Part I: Definitions for the actual data carried over AXI channels
// in part II we will provide definitions for the actual AXI interfaces
// by wrapping the part I types in Decoupled (ready/valid) bundles

// AXI channel data definitions

class AXIAddress(addrWidthBits: Int, idBits: Int) extends Bundle {
  // address for the transaction, should be burst aligned if bursts are used
  val addr    = UInt(width = addrWidthBits)
  // size of data beat in bytes
  // set to UInt(log2Up((dataBits/8)-1)) for full-width bursts
  val size    = UInt(width = 3) 
  // number of data beats -1 in burst: max 255 for incrementing, 15 for wrapping
  val len     = UInt(width = 8)
  // burst mode: 0 for fixed, 1 for incrementing, 2 for wrapping
  val burst   = UInt(width = 2)
  // transaction ID for multiple outstanding requests
  val id      = UInt(width = idBits)
  // set to 1 for exclusive access
  val lock    = Bool()
  // cachability, set to 0010 or 0011
  val cache   = UInt(width = 4)
  // generally ignored, set to to all zeroes
  val prot    = UInt(width = 3)
  // not implemented, set to zeroes
  val qos     = UInt(width = 4)
  override def clone = { new AXIAddress(addrWidthBits, idBits).asInstanceOf[this.type] }
}

class AXIWriteData(dataWidthBits: Int) extends Bundle {
  val data    = UInt(width = dataWidthBits)
  val strb    = UInt(width = dataWidthBits/8)
  val last    = Bool()
  override def clone = { new AXIWriteData(dataWidthBits).asInstanceOf[this.type] }
}

class AXIWriteResponse(idBits: Int) extends Bundle {
  val id      = UInt(width = idBits)
  val resp    = UInt(width = 2)
  override def clone = { new AXIWriteResponse(idBits).asInstanceOf[this.type] }
}

class AXIReadData(dataWidthBits: Int, idBits: Int) extends Bundle {
  val data    = UInt(width = dataWidthBits)
  val id      = UInt(width = idBits)
  val last    = Bool()
  val resp    = UInt(width = 2)
  override def clone = { new AXIReadData(dataWidthBits, idBits).asInstanceOf[this.type] }
}



// Part II: Definitions for the actual AXI interfaces

// TODO add full slave interface definition

class AXIMasterIF(addrWidthBits: Int, dataWidthBits: Int, idBits: Int) extends Bundle {  
  // write address channel
  val writeAddr   = Decoupled(new AXIAddress(addrWidthBits, idBits))
  // write data channel
  val writeData   = Decoupled(new AXIWriteData(dataWidthBits))
  // write response channel (for memory consistency)
  val writeResp   = Decoupled(new AXIWriteResponse(idBits)).flip
  
  // read address channel
  val readAddr    = Decoupled(new AXIAddress(addrWidthBits, idBits))
  // read data channel
  val readData    = Decoupled(new AXIReadData(dataWidthBits, idBits)).flip
  
  // rename signals to be compatible with those in the Xilinx template
  def renameSignals() {
    // write address channel
    writeAddr.bits.addr.setName("M_AXI_AWADDR")
    writeAddr.bits.prot.setName("M_AXI_AWPROT")
    writeAddr.bits.size.setName("M_AXI_AWSIZE")
    writeAddr.bits.len.setName("M_AXI_AWLEN")
    writeAddr.bits.burst.setName("M_AXI_AWBURST")
    writeAddr.bits.lock.setName("M_AXI_AWLOCK")
    writeAddr.bits.cache.setName("M_AXI_AWCACHE")
    writeAddr.bits.qos.setName("M_AXI_AWQOS")
    writeAddr.bits.id.setName("M_AXI_AWID")
    writeAddr.valid.setName("M_AXI_AWVALID")
    writeAddr.ready.setName("M_AXI_AWREADY")
    // write data channel
    writeData.bits.data.setName("M_AXI_WDATA")
    writeData.bits.strb.setName("M_AXI_WSTRB")
    writeData.bits.last.setName("M_AXI_WLAST")
    writeData.valid.setName("M_AXI_WVALID")
    writeData.ready.setName("M_AXI_WREADY")
    // write response channel
    writeResp.bits.resp.setName("M_AXI_BRESP")
    writeResp.bits.id.setName("M_AXI_BID")
    writeResp.valid.setName("M_AXI_BVALID")
    writeResp.ready.setName("M_AXI_BREADY")
    // read address channel
    readAddr.bits.addr.setName("M_AXI_ARADDR")
    readAddr.bits.prot.setName("M_AXI_ARPROT")
    readAddr.bits.size.setName("M_AXI_ARSIZE")
    readAddr.bits.len.setName("M_AXI_ARLEN")
    readAddr.bits.burst.setName("M_AXI_ARBURST")
    readAddr.bits.lock.setName("M_AXI_ARLOCK")
    readAddr.bits.cache.setName("M_AXI_ARCACHE")
    readAddr.bits.qos.setName("M_AXI_ARQOS")
    readAddr.bits.id.setName("M_AXI_ARID")
    readAddr.valid.setName("M_AXI_ARVALID")
    readAddr.ready.setName("M_AXI_ARREADY")
    // read data channel
    readData.bits.id.setName("M_AXI_RID")
    readData.bits.data.setName("M_AXI_RDATA")
    readData.bits.resp.setName("M_AXI_RRESP")
    readData.bits.last.setName("M_AXI_RLAST")
    readData.valid.setName("M_AXI_RVALID")
    readData.ready.setName("M_AXI_RREADY")
  }
  
  override def clone = { new AXIMasterIF(addrWidthBits, dataWidthBits, idBits).asInstanceOf[this.type] }
}

}
