package FSDS

import java.io.{InputStream, OutputStream}
import java.util

import Util.Functions
import org.apache.commons.math3.linear.{ArrayRealVector, RealVector}

import scala.collection.mutable.ArrayBuffer

abstract class MRSEuser(settings: Settings) {

  def indexParts():Int=1

  def DeleteKeys():Unit={}

  def MethodName(): String

  /*
writes to file
 */
  def KeyGen()


  /*
writes to file
 */
  def BuildIndex()

  def MapToNormlizedVector(map:Array[(Int,Double)]): RealVector ={
    val vec=new ArrayRealVector(settings.dic)
    for (tuple<-map)
      vec.setEntry(tuple._1,tuple._2)
    vec.mapMultiply(1/vec.getNorm)
  }

  def GenerateQuery(vec:RealVector):Query

  def GenerateQuery(vec:Array[(Int,Double)]):Query={
    val query=MapToNormlizedVector(vec)
    GenerateQuery(query)
  }

  val resultMap=new util.HashMap[Int,Array[(Int,Double)]]

  def Decrypt(ct:Result):Array[(Int,Double)]
  def Decrypt(ct:Result,searchID:Int):Array[(Int,Double)]={
    val res=Decrypt(ct)
    resultMap.put(searchID,res)
    res
  }


  def LoadClientKeys():Unit

  /*
  can be implemented here
   */
  def KeySize():Int={
    0
  }

  /*
  size of the keys that should be stored in the client side to generate queries
   */
  def QueryGenKeySize():Int

  def ResDecKeySize():Int=0

  def DecodeResult(s:Array[Byte]):Result


  def LoadResult(str:InputStream):Result={
    val temp=Functions.CollectInput(str)
    DecodeResult(temp)
  }

}


trait Query extends Serializable{

  def get():Any
  //IN KB
  def Size():Int=Encode().length/1024

  def Encode():Array[Byte]

  def Save(str:OutputStream):Unit={
    val encoded=Encode()
    val buffer=java.nio.ByteBuffer.allocate(4)
    buffer.putInt(encoded.length)

    str.write(buffer.array())
    str.write(encoded)
    str.flush()
  }

}


trait Result extends Serializable{
  def get():Any
  //IN KB
  def Size():Int=Encode().length/1024

  def Encode():Array[Byte]

  def Save(str:OutputStream):Unit={
    val encoded=Encode()
    val buffer=java.nio.ByteBuffer.allocate(4)
    buffer.putInt(encoded.length)
    str.write(buffer.array())
    str.write(encoded)
    str.flush()
  }

}