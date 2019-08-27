package FSDS

import java.io.{File, FileInputStream}
import java.util.Base64

import org.apache.commons.math3.linear.RealVector

class MRSE_FHE_User(settings: Settings) extends MRSEuser(settings) {
  var instance=new SWHE(settings)
  instance.method=MethodName()



  override def MethodName(): String = "FSDS0"

  override def QueryGenKeySize(): Int = (instance.pubKey.length)/1024
  override def ResDecKeySize(): Int = (instance.privateKey.length)/1024

  override def KeyGen(): Unit = {
    instance.KeyGen(0)
  }

  override def LoadClientKeys(): Unit = {
    val inputStreamPub=new FileInputStream(new File(settings.Keydir(MethodName())+"pubkey"))
    instance.pubKey=Util.Functions.CollectAllInput(inputStreamPub)
    val inputStreamPriv=new FileInputStream(new File(settings.Keydir(MethodName())+"privatekey"))
    instance.privateKey=Util.Functions.CollectAllInput(inputStreamPriv)
    val inputStreamParam=new FileInputStream(new File(settings.Keydir(MethodName())+"param"))
    instance.param=Util.Functions.CollectAllInput(inputStreamParam)
  }


  override def BuildIndex(): Unit = {
    val data=Util.Functions.readData(new File(settings.PlainData()),settings.docnum,settings.dic)
    instance.IndexGen(data,0)//0 for now, this method is creted for test purposes
  }

  override def Decrypt(ct: Result): Array[(Int, Double)] = {
    val fhe_res=ct.get().asInstanceOf[Array[Byte]]
    var res:Array[(Int,Double)]=null

    val decres=instance.DecryptResult(fhe_res)
    Util.Functions.Insert(decres,settings.K)
  }

  override def GenerateQuery(query:RealVector): Query = {
    val ct=instance.QueryGen(query.mapDivide(query.getNorm))
    new MRSE_FHE_Query(ct)
  }

  /*
  non-implemented method
   */
  override def DecodeResult(s: Array[Byte]): Result = null

}


class MRSE_FHE_Query(ct:Array[Byte]) extends Query{
  override def get(): Any = {
    ct.asInstanceOf[Any]
  }

  override def Size(): Int = {
    ct.length/1024
  }

  override def Encode={
    null
  }
}



class MRSE_FHE_Result(ct:Array[Byte]) extends Result{
  override def get(): Any = {
    ct.asInstanceOf[Any]
  }

  override def Size(): Int = {
    ct.length/1024
  }

  override def Encode={
    null
  }
}