package FSDS

import java.io.{File, PrintWriter}
import java.util.Base64

import org.apache.commons.math3.linear.RealVector
import org.apache.commons.math3.linear.{ArrayRealVector, LUDecomposition}

class MRSE_TF2_Server(settings: Settings) extends SDS_Server(settings) {

  var sknnInstance=new SecureKNN(settings,0,settings.variance)

  override def MethodName(): String = "MRSE_TF2_"+settings.variance


  protected var index:Array[ArrayRealVector]=null



  override def StartServer(): Unit = {
    index=Util.Functions.ReadArrayofVector(new File(settings.IndexPath(MethodName(),0)),sknnInstance.Mdim()*2,settings.docnum)
  }

  override def SimilaritySearch(query: Query): Result = {
    val resInit=(0 until settings.docnum).
      map(u=>(u,index(u).dotProduct(query.get().
        asInstanceOf[ArrayRealVector]))).toArray
    new MRSE_TF2_Result(Util.Functions.Insert(resInit,settings.K))
  }

  override def DecodeQuery(s: Array[Byte]): Query = {
    var buffer=java.nio.ByteBuffer.wrap(s)
    val vec=new ArrayRealVector((0 until s.length/8).map(u=>buffer.getDouble()).toArray)
    new MRSE_TF2_Query(vec)
  }



}


object MRSE_TF2_Server{
  def main(args: Array[String]): Unit = {

  }
}