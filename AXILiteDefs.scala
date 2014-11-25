package AXILiteDefs
{

import Chisel._
import Literal._
import Node._

// Part I: Definitions for the actual data carried over AXI channels
// in part II we will provide definitions for the actual AXI interfaces
// by wrapping the part I types in Decoupled (ready/valid) bundles


// AXI Lite channel data definitions

class AXILiteAddress(addrWidthBits: Int) extends Bundle {
  val addr    = UInt(width = addrWidthBits)
  val prot    = UInt(width = 3)
  override def clone = { new AXILiteAddress(addrWidthBits).asInstanceOf[this.type] }
}

class AXILiteWriteData(dataWidthBits: Int) extends Bundle {
  val data    = UInt(width = dataWidthBits)
  val strb    = UInt(width = dataWidthBits/8)
  override def clone = { new AXILiteWriteData(dataWidthBits).asInstanceOf[this.type] }
}

class AXILiteReadData(dataWidthBits: Int) extends Bundle {
  val data    = UInt(width = dataWidthBits)
  val resp    = UInt(width = 2)
  override def clone = { new AXILiteReadData(dataWidthBits).asInstanceOf[this.type] }
}

// Part II: Definitions for the actual AXI interfaces

class AXILiteSlaveIF(addrWidthBits: Int, dataWidthBits: Int) extends Bundle {
  // write address channel
  val writeAddr   = Decoupled(new AXILiteAddress(addrWidthBits)).flip
  // write data channel
  val writeData   = Decoupled(new AXILiteWriteData(dataWidthBits)).flip
  // write response channel (for memory consistency)
  val writeResp   = Decoupled(UInt(width = 2))
  
  // read address channel
  val readAddr    = Decoupled(new AXILiteAddress(addrWidthBits)).flip
  // read data channel
  val readData    = Decoupled(new AXILiteReadData(dataWidthBits))
  
  // rename signals to be compatible with those in the Xilinx template
  def renameSignals() {
    writeAddr.bits.addr.setName("S_AXI_AWADDR")
    writeAddr.bits.prot.setName("S_AXI_AWPROT")
    writeAddr.valid.setName("S_AXI_AWVALID")
    writeAddr.ready.setName("S_AXI_AWREADY")
    writeData.bits.data.setName("S_AXI_WDATA")
    writeData.bits.strb.setName("S_AXI_WSTRB")
    writeData.valid.setName("S_AXI_WVALID")
    writeData.ready.setName("S_AXI_WREADY")
    writeResp.bits.setName("S_AXI_BRESP")
    writeResp.valid.setName("S_AXI_BVALID")
    writeResp.ready.setName("S_AXI_BREADY")
    readAddr.bits.addr.setName("S_AXI_ARADDR")
    readAddr.bits.prot.setName("S_AXI_ARPROT")
    readAddr.valid.setName("S_AXI_ARVALID")
    readAddr.ready.setName("S_AXI_ARREADY")
    readData.bits.data.setName("S_AXI_RDATA")
    readData.bits.resp.setName("S_AXI_RRESP")
    readData.valid.setName("S_AXI_RVALID")
    readData.ready.setName("S_AXI_RREADY")
  }
  
  override def clone = { new AXILiteSlaveIF(addrWidthBits, dataWidthBits).asInstanceOf[this.type] }
}



class AXILiteMasterIF(addrWidthBits: Int, dataWidthBits: Int) extends Bundle {  
  // write address channel
  val writeAddr   = Decoupled(new AXILiteAddress(addrWidthBits))
  // write data channel
  val writeData   = Decoupled(new AXILiteWriteData(dataWidthBits))
  // write response channel (for memory consistency)
  val writeResp   = Decoupled(UInt(width = 2)).flip
  
  // read address channel
  val readAddr    = Decoupled(new AXILiteAddress(addrWidthBits))
  // read data channel
  val readData    = Decoupled(new AXILiteReadData(dataWidthBits)).flip
  
  // rename signals to be compatible with those in the Xilinx template
  def renameSignals() {
    writeAddr.bits.addr.setName("M_AXI_AWADDR")
    writeAddr.bits.prot.setName("M_AXI_AWPROT")
    writeAddr.valid.setName("M_AXI_AWVALID")
    writeAddr.ready.setName("M_AXI_AWREADY")
    writeData.bits.data.setName("M_AXI_WDATA")
    writeData.bits.strb.setName("M_AXI_WSTRB")
    writeData.valid.setName("M_AXI_WVALID")
    writeData.ready.setName("M_AXI_WREADY")
    writeResp.bits.setName("M_AXI_BRESP")
    writeResp.valid.setName("M_AXI_BVALID")
    writeResp.ready.setName("M_AXI_BREADY")
    readAddr.bits.addr.setName("M_AXI_ARADDR")
    readAddr.bits.prot.setName("M_AXI_ARPROT")
    readAddr.valid.setName("M_AXI_ARVALID")
    readAddr.ready.setName("M_AXI_ARREADY")
    readData.bits.data.setName("M_AXI_RDATA")
    readData.bits.resp.setName("M_AXI_RRESP")
    readData.valid.setName("M_AXI_RVALID")
    readData.ready.setName("M_AXI_RREADY")
  }
  
  override def clone = { new AXILiteMasterIF(addrWidthBits, dataWidthBits).asInstanceOf[this.type] }
}

}
