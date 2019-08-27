package FSDS

import java.io.File
import java.util.Base64

import org.apache.commons.math3.linear.{ArrayRealVector, RealVector}

class MRSE_TF2_User(settings: Settings) extends MRSEuser(settings) {

  var sknnInstance=new SecureKNN(settings,0,settings.variance)

  override def MethodName(): String = "MRSE_TF2_"+settings.variance

  override def QueryGenKeySize(): Int = {
    var total=0
    total+=(new File(settings.Keydir(MethodName())+settings.M1inv()).length).toInt
    total+=(new File(settings.Keydir(MethodName())+settings.M2inv()).length).toInt
    total+=(new File(settings.Keydir(MethodName())+settings.S()).length).toInt
    return total/1024
  }

  override def KeyGen()={
    sknnInstance.GenerateSecretMatrices()
    /*
    for test purposes, it is not possible to revert matrices of size >4096
     */
    if(settings.dic<=4000)
      sknnInstance.setInverseMs()
    else
      sknnInstance.setInversesRandom()
    sknnInstance.GenerateS()

    Util.Functions.WriteMatrix(new File(settings.Keydir(MethodName())+settings.M1()),sknnInstance.M._1)
    Util.Functions.WriteMatrix(new File(settings.Keydir(MethodName())+settings.M2()),sknnInstance.M._2)
    Util.Functions.WriteMatrix(new File(settings.Keydir(MethodName())+settings.M1inv()),sknnInstance.Minv._1)
    Util.Functions.WriteMatrix(new File(settings.Keydir(MethodName())+settings.M2inv()),sknnInstance.Minv._2)

    Util.Functions.WriteVector(new File(settings.Keydir(MethodName())+settings.S()),sknnInstance.S)

  }



  override def LoadClientKeys(): Unit = {

    val M1=Util.Functions.ReadMatrix(new File(settings.Keydir(MethodName())+settings.M1()),sknnInstance.Mdim())
    val M2=Util.Functions.ReadMatrix(new File(settings.Keydir(MethodName())+settings.M2()),sknnInstance.Mdim())
    val M1inv=Util.Functions.ReadMatrix(new File(settings.Keydir(MethodName())+settings.M1inv()),sknnInstance.Mdim())
    val M2inv=Util.Functions.ReadMatrix(new File(settings.Keydir(MethodName())+settings.M2inv()),sknnInstance.Mdim())


    val S=Util.Functions.ReadVector(new File(settings.Keydir(MethodName())+settings.S()),sknnInstance.Mdim())
    sknnInstance.M=(M1,M2)
    sknnInstance.Minv=(M1inv,M2inv)
    sknnInstance.S=S
  }

  override def DeleteKeys()={
    new File(settings.Keydir(MethodName())+settings.M1()).delete()
    new File(settings.Keydir(MethodName())+settings.M2()).delete()
    new File(settings.Keydir(MethodName())+settings.M1inv()).delete()
    new File(settings.Keydir(MethodName())+settings.M2inv()).delete()
    new File(settings.Keydir(MethodName())+settings.S()).delete()
  }

  override def BuildIndex(): Unit = {
    val data=Util.Functions.readData(new File(settings.PlainData()),settings.docnum,settings.dic)

    val encdata=data.map(u=>sknnInstance.EncryptDoc(u))

    Util.Functions.WriteArrayofVector(new File(settings.IndexPath(MethodName(),0)),encdata)

  }


  override def GenerateQuery(query:RealVector): Query = {
    return new MRSE_TF2_Query(sknnInstance.EncryptQuery(query.mapDivide(query.getNorm)))
  }


  override def Decrypt(res: Result): Array[(Int, Double)] = {
    res.get().asInstanceOf[Array[(Int,Double)]]
  }

  override def DecodeResult(s: Array[Byte]): Result = {
    var buffer=java.nio.ByteBuffer.wrap(s)
    new MRSE_TF2_Result((0 until s.length/12).map(u=>{
      (buffer.getInt,buffer.getDouble)
    }).toArray)

  }

}

class MRSE_TF2_Query(data:RealVector) extends Query{

  override def get(): Any = data.asInstanceOf[Any]



  override def Encode={
    var buffer=java.nio.ByteBuffer.allocate(data.getDimension*8)
    data.toArray.foreach(u=>buffer.putDouble(u))
    buffer.array()
  }
}


class MRSE_TF2_Result(arr:Array[(Int,Double)]) extends Result{

  override def get(): Any = arr.asInstanceOf[Any]

  override def Encode={
    var buffer=java.nio.ByteBuffer.allocate(arr.length*12)
    arr.foreach(u=>{
      buffer.putInt(u._1)
      buffer.putDouble(u._2)
    })
    buffer.array()
  }

}
