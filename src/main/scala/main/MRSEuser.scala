package main

import java.util

import org.apache.commons.math3.linear.{ArrayRealVector, RealVector}

abstract class MRSEuser(settings: Settings) {

  def indexParts():Int=1


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

  def GenerateTrapdoor(vec:RealVector):Trapdoor

  def GenerateTrapdoor(vec:Array[(Int,Double)]):Trapdoor={
    val query=MapToNormlizedVector(vec)
    GenerateTrapdoor(query)
  }

  val resultMap=new util.HashMap[Int,Array[(Int,Double)]]

  def Decrypt(ct:Result):Array[(Int,Double)]
  def Decrypt(ct:Result,searchID:Int):Array[(Int,Double)]={
    val res=Decrypt(ct)
    resultMap.put(searchID,res)
    res
  }
  /*
  Implement Here
  Not neccesarry for now
   */
  def PIRquery(in:Array[Int]):Array[String]={
    null
  }

  def DecryptPIRresult(result:Result):Array[String]={
    null
  }

  def LoadClientKeys():Unit

  /*
  can be implemented here
   */
  def KeySize():Int={
    0
  }

  /*
  size of the keys that should be stored in the client side to generate trapdoors
   */
  def TrapGenKeySize():Int

  def ResDecKeySize():Int=0

  def DecodeResult(s:Array[Byte]):Result

}


trait Trapdoor extends Serializable{

  def get():Any
  //IN KB
  def Size():Int=Encode().length/1024

  def Encode():Array[Byte]

}


trait Result extends Serializable{
  def get():Any
  //IN KB
  def Size():Int=Encode().length/1024

  def Encode():Array[Byte]
}