package main

import java.io.{File, FileInputStream}
import java.math.BigInteger
import java.util
import java.util.Base64

import org.apache.commons.math3.linear.ArrayRealVector

import scala.collection.mutable.ArrayBuffer

class MRSE_FHE_SkNN_Server(settings: Settings) extends MRSEserver(settings) {

  var fheInstance=new FHE(settings)
  override def MethodName(): String = "MRSE_FHE_SkNN"
  var bytesIndex:Array[Array[Array[Byte]]]=null

  override def indexParts(): Int =
    if (settings.docnum % settings.SlotCount == 0) settings.docnum / settings.SlotCount
    else settings.docnum / settings.SlotCount + 1


  override def SimilaritySearch(trapdoor: Trapdoor): Result = {

    val num=settings.dic*2
    val trapdoorCast=trapdoor.get().asInstanceOf[ModularMatrix]
    val trapdoorInt=(0 until trapdoorCast.getColnum).map(u=>trapdoorCast.get(0,u).longValue().toInt).toArray
    val ChunkSize=num/settings.chunkNum
    val Remainder=num%settings.chunkNum
    val lastChunkSize=Remainder+ChunkSize



    val trapdoorParts=(0 until settings.chunkNum).map(u=>{
      if(u==(settings.chunkNum-1))
        trapdoorInt.slice(u*ChunkSize,(u)*ChunkSize+lastChunkSize)
      else
        trapdoorInt.slice(u*ChunkSize,(u+1)*ChunkSize)
    })


    var resBuffer=new ArrayBuffer[(Int,Array[Byte])]

    for(i<-0 until bytesIndex.length){
      val res=(0 until settings.chunkNum).map(u=>{
        fheInstance.CalcSimPlain(bytesIndex(i)(u), trapdoorParts(u))
      })
      resBuffer+=((i,fheInstance.BatchAdd(res.toArray)))
    }
    new MRSE_FHE_SkNN_Result(resBuffer.toArray)
  }

  override def StartServer(): Unit = {
    val indexData=IndexFiles()
    val length=indexData.length

    bytesIndex=new Array[Array[Array[Byte]]](indexParts)

    for(i<-0 until indexParts) {
      bytesIndex(i)=new Array[Array[Byte]](settings.chunkNum)
      for (j <- 0 until settings.chunkNum) {
        val currFile=indexData.filter(u=>u.getName.contains(i+"_"+j+"_"))(0)
        println("curr file: "+currFile.getName)
        val inputStream = new FileInputStream(currFile)
        bytesIndex(i)(j) = Util.CollectAllInput(inputStream)
      }
    }

    val inputStreamParam=new FileInputStream(new File(settings.Keydir(MethodName())+"param"))
    fheInstance.param=Util.CollectAllInput(inputStreamParam)
  }

  override def DecodeTrapdoor(s: Array[Byte]): Trapdoor = {
    var buffer=java.nio.ByteBuffer.wrap(s)
    val mat=new ModularMatrix(1,settings.dic*2)
    for(i<-0 until settings.dic*2)mat.set(0,i,BigInteger.valueOf(buffer.getInt()))
    new MRSE_FHE_SkNN_Trapdoor(mat)
  }
}
