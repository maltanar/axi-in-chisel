package AXIDefs
{

import Chisel._
import Literal._
import Node._

// Part I: Definitions for the actual data carried over AXI channels
// in part II we will provide definitions for the actual AXI interfaces
// by wrapping the part I types in Decoupled (ready/valid) bundles

// TODO these are actually AXILite types -- rename appropriately
// TODO add support for full AXI IFs with burst

class AXIAddress(addrWidthBits: Int) extends Bundle {
  val addr    = UInt(width = addrWidthBits)
  val prot    = UInt(width = 3)
  override def clone = { new AXIAddress(addrWidthBits).asInstanceOf[this.type] }
}

class AXIWriteData(dataWidthBits: Int) extends Bundle {
  val data    = UInt(width = dataWidthBits)
  val strb    = UInt(width = dataWidthBits/8)
  override def clone = { new AXIWriteData(dataWidthBits).asInstanceOf[this.type] }
}

class AXIReadData(dataWidthBits: Int) extends Bundle {
  val data    = UInt(width = dataWidthBits)
  val resp    = UInt(width = 2)
  override def clone = { new AXIReadData(dataWidthBits).asInstanceOf[this.type] }
}

// Part II: Definitions for the actual AXI interfaces

class AXILiteSlaveIF(addrWidthBits: Int, dataWidthBits: Int) extends Bundle {
  // write address channel
  val writeAddr   = Decoupled(new AXIAddress(addrWidthBits)).flip
  // write data channel
  val writeData   = Decoupled(new AXIWriteData(dataWidthBits)).flip
  // write response channel (for memory consistency)
  val writeResp   = Decoupled(UInt(width = 2))
  
  // read address channel
  val readAddr    = Decoupled(new AXIAddress(addrWidthBits)).flip
  // read data channel
  val readData    = Decoupled(new AXIReadData(dataWidthBits))
  
  override def clone = { new AXILiteSlaveIF(addrWidthBits, dataWidthBits).asInstanceOf[this.type] }
}

class AXILiteMasterIF(addrWidthBits: Int, dataWidthBits: Int) extends Bundle {
  // metadata channel
  val axi_init_axi_txn  = Bool(INPUT)             // start transaction
  val axi_error         = Bool(OUTPUT)            // error indicator
  val axi_txn_done      = Bool(OUTPUT)            // transaction done
  
  // write address channel
  val writeAddr   = Decoupled(new AXIAddress(addrWidthBits))
  // write data channel
  val writeData   = Decoupled(new AXIWriteData(dataWidthBits))
  // write response channel (for memory consistency)
  val writeResp   = Decoupled(UInt(width = 2)).flip
  
  // read address channel
  val readAddr    = Decoupled(new AXIAddress(addrWidthBits))
  // read data channel
  val readData    = Decoupled(new AXIReadData(dataWidthBits)).flip
  
  override def clone = { new AXILiteMasterIF(addrWidthBits, dataWidthBits).asInstanceOf[this.type] }
}

}
