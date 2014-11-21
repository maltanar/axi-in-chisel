package AXIDefs
{

import Chisel._
import Literal._
import Node._

class AXILiteSlaveIF(addrWidthBits: Int, dataWidthBits: Int) extends Bundle
{
  // write address channel
  val axi_awaddr  = UInt(INPUT, addrWidthBits)    // write address
  val axi_awprot  = UInt(INPUT, 3)                // write address protect bits
  val axi_awvalid = Bool(INPUT)                   // write address valid
  val axi_awready = Bool(OUTPUT)                  // write address ready
  // write data channel
  val axi_wdata   = UInt(INPUT, dataWidthBits)    // write data
  val axi_wstrb   = UInt(INPUT, dataWidthBits/8)  // write strobe (byte enables?)
  val axi_wvalid  = Bool(INPUT)                   // write valid
  val axi_wready  = Bool(OUTPUT)                  // write ready
  // write response channel (for memory consistency)
  val axi_bresp   = UInt(OUTPUT, 2)               // write response, 0 is OK
  val axi_bvalid  = Bool(OUTPUT)                  // write response valid
  val axi_bready  = Bool(INPUT)                   // write response ready
  // read address channel
  val axi_araddr  = UInt(INPUT, addrWidthBits)    // read address
  val axi_arprot  = UInt(INPUT, 3)                // read address protect bits
  val axi_arvalid = Bool(INPUT)                   // read address valid
  val axi_arready = Bool(OUTPUT)                  // read address ready
  // read data channel
  val axi_rdata   = UInt(OUTPUT, dataWidthBits)   // read data
  val axi_rresp   = UInt(OUTPUT, 2)               // read data status response, 0 is OK
  val axi_rvalid  = Bool(OUTPUT)                  // read data valid
  val axi_rready  = Bool(INPUT)                   // read data ready
}

class AXILiteMasterIF(addrWidthBits: Int, dataWidthBits: Int) extends Bundle
{
  // metadata channel
  val axi_init_axi_txn  = Bool(INPUT)             // start transaction
  val axi_error         = Bool(OUTPUT)            // error indicator
  val axi_txn_done      = Bool(OUTPUT)            // transaction done
  // write address channel
  val axi_awaddr        = UInt(OUTPUT, addrWidthBits)     // write address
  val axi_awprot        = UInt(OUTPUT, 3)                 // write address protect bits
  val axi_awvalid       = Bool(OUTPUT)                    // write address valid
  val axi_awready       = Bool(INPUT)                     // write address ready
  // write data channel
  val axi_wdata         = UInt(OUTPUT, dataWidthBits)    // write data
  val axi_wstrb         = UInt(OUTPUT, dataWidthBits/8)  // write strobe (byte enables?)
  val axi_wvalid        = Bool(OUTPUT)                   // write valid
  val axi_wready        = Bool(INPUT)                    // write ready
  // write response channel
  val axi_bresp         = UInt(INPUT, 2)                // write response, 0 is OK
  val axi_bvalid        = Bool(INPUT)                   // write response valid
  val axi_bready        = Bool(OUTPUT)                  // write response ready
  // read address channel
  val axi_araddr        = UInt(OUTPUT, addrWidthBits)   // read address
  val axi_arprot        = UInt(OUTPUT, 3)               // read address protect bits
  val axi_arvalid       = Bool(OUTPUT)                  // read address valid
  val axi_arready       = Bool(INPUT)                   // read address ready
  // read data channel
  val axi_rdata         = UInt(INPUT, dataWidthBits)   // read data
  val axi_rresp         = UInt(INPUT, 2)               // read data status response, 0 is OK
  val axi_rvalid        = Bool(INPUT)                  // read data valid
  val axi_rready        = Bool(OUTPUT)                 // read data ready
}

}
