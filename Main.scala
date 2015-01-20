package AXIChisel
import Chisel._

object MainObj {
  def main(args: Array[String]): Unit = {
    //chiselMain(args, () => Module(new ConstRegBlock(5, 32)))
    //chiselMain(args, () => Module(new SimpleReg(8, 5, 32)))
    //chiselMain(args, () => Module(new SumAccel()))
    //chiselMain(args, () => Module(new HPSumAccel()))
    chiselMain(args, () => Module(new AXIStreamMonitor()))
  }
}

