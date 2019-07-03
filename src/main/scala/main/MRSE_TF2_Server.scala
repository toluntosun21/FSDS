package main

import java.io.{File, PrintWriter}
import java.util.Base64

import org.apache.commons.math3.linear.RealVector
import org.apache.commons.math3.linear.{ArrayRealVector, LUDecomposition}

class MRSE_TF2_Server(settings: Settings) extends MRSEserver(settings) {

  var sknnInstance=new SecureKNN(settings,0,settings.variance)

  override def MethodName(): String = "MRSE_TF2_"+settings.variance


  protected var index:Array[ArrayRealVector]=null



  override def StartServer(): Unit = {
    index=Util.ReadArrayofVector(new File(settings.IndexPath(MethodName(),0)),sknnInstance.Mdim()*2,settings.docnum)
  }

  override def SimilaritySearch(trapdoor: Trapdoor): Result = {
    val resInit=(0 until settings.docnum).
      map(u=>(u,index(u).dotProduct(trapdoor.get().
        asInstanceOf[ArrayRealVector]))).toArray
    new MRSE_TF2_Result(Util.Insert(resInit,settings.K))
  }

  override def DecodeTrapdoor(s: Array[Byte]): Trapdoor = {
    var buffer=java.nio.ByteBuffer.wrap(s)
    val vec=new ArrayRealVector((0 until s.length/8).map(u=>buffer.getDouble()).toArray)
    new MRSE_TF2_Trapdoor(vec)
  }



}


object MRSE_TF2_Server{
  def main(args: Array[String]): Unit = {

  }
}