package AXIChisel
import Chisel._

object MainObj {
  def main(args: Array[String]): Unit = {
    chiselMain(args, () => Module(new ConstRegBlock(8, 32)))
  }
}

