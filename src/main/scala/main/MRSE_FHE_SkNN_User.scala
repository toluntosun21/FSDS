package main

import java.io.{File, FileInputStream}
import java.math.BigInteger
import java.util.Base64

import org.apache.commons.math3.linear.RealVector
import org.apache.commons.math3.linear.{Array2DRowRealMatrix, RealMatrix}

class MRSE_FHE_SkNN_User(settings: Settings) extends MRSEuser(settings) {

  var fheInstance=new FHE(settings)
  var sknnInstance=new mSkNN(settings)//responsible for scaling
  fheInstance.method=MethodName()

  override def indexParts(): Int =
    if (settings.docnum % settings.SlotCount == 0) settings.docnum / settings.SlotCount
    else settings.docnum / settings.SlotCount + 1


  override def MethodName(): String = "MRSE_FHE_SkNN"

  override def KeyGen(): Unit = {
    fheInstance.KeyGen(0)
    val M=sknnInstance.GenerateSecretMatrix()
    val minv=sknnInstance.GenerateInverseMatrix()
    Util.WriteMatrix(new File(settings.Keydir(MethodName())+"M"),M)
    Util.WriteMatrix(new File(settings.Keydir(MethodName())+"Minv"),minv)
  }

  override def LoadClientKeys(): Unit = {
    val M=Util.ReadModMatrix(new File(settings.Keydir(MethodName())+"M"),settings.lambda())
    sknnInstance.M=M
    val inputStreamPub=new FileInputStream(new File(settings.Keydir(MethodName())+"pubkey"))
    fheInstance.pubKey=Util.CollectAllInput(inputStreamPub)
    val inputStreamPriv=new FileInputStream(new File(settings.Keydir(MethodName())+"privatekey"))
    fheInstance.privateKey=Util.CollectAllInput(inputStreamPriv)
    val inputStreamParam=new FileInputStream(new File(settings.Keydir(MethodName())+"param"))
    fheInstance.param=Util.CollectAllInput(inputStreamParam)

  }

  override def BuildIndex(): Unit = {
    val Minv=Util.ReadModMatrix(new File(settings.Keydir(MethodName())+"Minv"),settings.lambda())
    sknnInstance.M_Inv=Minv

    val data=Util.readData(new File(settings.PlainData()),settings.docnum,settings.dic)
    val encData=data.map(u=>sknnInstance.EncryptData(u))

    val vertChunkSize=sknnInstance.dim/settings.chunkNum
    val dataParts=encData.map(u=>{
      (0 until settings.chunkNum).map(v=>Util.SubVector(u,v*vertChunkSize,(v+1)*vertChunkSize))
    })

    for(j<-0 until settings.chunkNum) {
      val indexParts = if (settings.docnum % settings.SlotCount == 0) settings.docnum / settings.SlotCount else settings.docnum / settings.SlotCount + 1
      for (i <- 0 until indexParts)
        fheInstance.IndexGen(dataParts.slice(i * settings.SlotCount, (i + 1) * settings.SlotCount).map(u=>u(j)), i,j)
    }
  }

  override def TrapGenKeySize(): Int = {
    ((sknnInstance.lambda*sknnInstance.lambda*4))/1024
  }

  override def ResDecKeySize():Int={
    (fheInstance.privateKey.length)/1024
  }

  override def GenerateTrapdoor(query:RealVector): Trapdoor = {
    val sknnEnc=sknnInstance.EncryptQuery(query.mapDivide(query.getNorm))
    new MRSE_FHE_SkNN_Trapdoor(sknnEnc)
  }

  override def Decrypt(ct: Result): Array[(Int, Double)] = {
    val fh_sknn_res=ct.get().asInstanceOf[Array[(Int,Array[Byte])]]
    var res:Array[(Int,Double)]=null
    for(i<-0 until fh_sknn_res.length){
      val decres=fheInstance.DecryptResult(fh_sknn_res(i)._2)
      val decresID=decres.map(u=>(u._1+settings.SlotCount*fh_sknn_res(i)._1,u._2))
      if(i==0){
        res=Util.Insert(decresID,settings.K)
      }
      else{
        res=Util.Insert(res,decresID,settings.K)
      }
    }
    res
  }

  override def Decrypt(ct: Result,searchID:Int): Array[(Int, Double)] = {
    val fh_sknn_res=ct.get().asInstanceOf[Array[(Int,Array[Byte])]]
    var res:Array[(Int,Double)]=null
    for(i<-0 until fh_sknn_res.length){
      val decres=fheInstance.DecryptResult(fh_sknn_res(i)._2)
      val decresID=decres.map(u=>(u._1+settings.SlotCount*fh_sknn_res(i)._1,u._2))
      if(resultMap.containsKey(searchID)==false){
        res=Util.Insert(decresID,settings.K)
      }
      else{
        res=Util.Insert(resultMap.get(searchID),decresID,settings.K)
        resultMap.put(searchID,res)
      }
    }
    res
  }


  override def DecodeResult(s: Array[Byte]): Result = {
    var buffer=java.nio.ByteBuffer.wrap(s)
    val numRes=buffer.getInt()
    val data=(0 until numRes).map(u=>{
      val ID=buffer.getInt()
      val length=buffer.getInt()
      val cts=(0 until length).map(u=>buffer.get()).toArray
      (ID,cts)
    }).toArray
    new MRSE_FHE_SkNN_Result(data)
  }

}

class MRSE_FHE_SkNN_Trapdoor(ct:ModularMatrix) extends Trapdoor{
  override def get(): Any = {
    ct.asInstanceOf[Any]
  }


  override def Encode={
    var buffer=java.nio.ByteBuffer.allocate(ct.getColnum*ct.getRownum*4)
    for(i<-0 until ct.getRownum)
      for(j<-0 until ct.getColnum)
        buffer.putInt(ct.get(i,j).longValue().toInt)
    buffer.array()
  }

}



class MRSE_FHE_SkNN_Result(ct:Array[(Int,Array[Byte])]) extends Result{
  override def get(): Any = {
    ct.asInstanceOf[Any]
  }


  override def Encode={
    val size=ct.map(u=>8+u._2.length).reduce((a,b)=>a+b)+4
    val buffer=java.nio.ByteBuffer.allocate(size)
    buffer.putInt(ct.length)
    ct.foreach(u=>{
      buffer.putInt(u._1)
      buffer.putInt(u._2.length)
      for(i<-0 until u._2.length)buffer.put(u._2(i))
    })
    buffer.array()
  }

}
