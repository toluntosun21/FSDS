package main

import java.math.BigInteger

import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector

import scala.util.Random

class mSkNN(settings: Settings) {

  ModularMatrix.mod=BigInteger.valueOf(settings.plainMod);
  val lambda =settings.lambda
  var M:ModularMatrix=null
  var M_Inv:ModularMatrix=null
  val dim=settings.dic*2
  val num_range=dim/lambda

  private def SplitData(vec:ModularMatrix):ModularMatrix={
    val splitted=new ModularMatrix(1,vec.getColnum*2)
    for(i<-0 until vec.getColnum)
    {
      splitted.set(0,2*i,vec.get(0,i))
      splitted.set(0,2*i+1,vec.get(0,i))
    }
    splitted
  }

  private def SplitQuery(vec:ModularMatrix):ModularMatrix={
    val splitted=new ModularMatrix(1,vec.getColnum*2)
    val rand=new Random()
    for(i<-0 until vec.getColnum)
    {
      val randdum=rand.nextInt(settings.plainMod)
      val remaining=vec.get(0,i).subtract(BigInteger.valueOf(randdum)).mod(ModularMatrix.mod)
      splitted.set(0,2*i,BigInteger.valueOf(randdum))
      splitted.set(0,2*i+1,remaining)
    }
    splitted
  }

  def GenerateSecretMatrix():ModularMatrix={

    val rand=new Random()
    val mat=(0 until lambda).map(u=>{
      (0 until lambda).map(v=>rand.nextInt(settings.plainMod-1)+1).toArray
    }).toArray

    try {
      val init = new ModularMatrix(mat)
      M=init
      return init;
    }catch{
      case e:Exception=>return GenerateSecretMatrix()
    }
  }

  def GenerateInverseMatrix():ModularMatrix={

    M_Inv=M.Transpose().Inverse()

    M_Inv
  }




  private def MultiplyRanges(sec:ModularMatrix,vec:ModularMatrix):ModularMatrix={
    val returner=new ModularMatrix(1,vec.getColnum)

    (0 until num_range).map(u=>{
      val size=lambda
      val start=(u)*lambda
      val end=start+size
      val subVector=Util.SubVector(vec,start,end)
      val multiplied=subVector.Multiply(sec)
      for(i<-0 until multiplied.getColnum)
        returner.set(0,start+i,multiplied.get(0,i))
    })
    returner
  }

  def EncryptQuery(vector: RealVector):ModularMatrix={
    EncryptQuery(new ModularMatrix(Array(vector.toArray.map(u=>(u*settings.Scale).toInt))))
  }

  def EncryptData(vector: RealVector):ModularMatrix={
    EncryptData(new ModularMatrix(Array(vector.toArray.map(u=>(u*settings.Scale).toInt))))
  }

  def EncryptQuery(vec:ModularMatrix):ModularMatrix={
    val splitted=SplitQuery(vec)


    MultiplyRanges(M,splitted)
  }

  def EncryptData(vec:ModularMatrix):ModularMatrix={
    val splitted=SplitData(vec)

    MultiplyRanges(M_Inv,splitted)
  }

  def Score(data:ModularMatrix,query:ModularMatrix):Int={
    data.DotProduct(query).longValue().toInt
  }

}

object mSkNN {
  def main(args: Array[String]): Unit = {
    val settings=new Settings
    settings.dic=512
    val doc1=new ArrayRealVector(512)
    doc1.setEntry(5,1)

    val doc2=new ArrayRealVector(512)
    doc2.setEntry(3,1)

    val query=new ArrayRealVector(512)
    query.setEntry(5,1)

    val inst=new mSkNN(settings)

    inst.GenerateSecretMatrix()
    inst.GenerateInverseMatrix()
    val i1=inst.EncryptData(doc1)
    val i2=inst.EncryptData(doc2)
    val t=inst.EncryptQuery(query)
    println(inst.Score(t,i1))
    println(inst.Score(t,i2))

  }


}
