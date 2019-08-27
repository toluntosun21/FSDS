package FSDS

import java.io.{File, FileInputStream}
import java.math.BigInteger
import java.util
import java.util.Base64

import Util.ModularMatrix
import org.apache.commons.math3.linear.ArrayRealVector

import scala.collection.mutable.ArrayBuffer

class FSDS_Server(settings: Settings) extends SDS_Server(settings) {

  var fheInstance=new SWHE(settings)
  override def MethodName(): String = "MRSE_FHE_SkNN"
  var bytesIndex:Array[Array[Array[Byte]]]=null

  override def indexParts(): Int =
    if (settings.docnum % settings.SlotCount == 0) settings.docnum / settings.SlotCount
    else settings.docnum / settings.SlotCount + 1


  override def SimilaritySearch(query: Query): Result = {

    val num=settings.dic*2
    val queryCast=query.get().asInstanceOf[ModularMatrix]
    val queryInt=(0 until queryCast.getColnum).map(u=>queryCast.get(0,u).longValue().toInt).toArray
    val ChunkSize=num/settings.chunkNum
    val Remainder=num%settings.chunkNum
    val lastChunkSize=Remainder+ChunkSize



    val queryParts=(0 until settings.chunkNum).map(u=>{
      if(u==(settings.chunkNum-1))
        queryInt.slice(u*ChunkSize,(u)*ChunkSize+lastChunkSize)
      else
        queryInt.slice(u*ChunkSize,(u+1)*ChunkSize)
    })


    var resBuffer=new ArrayBuffer[(Int,Array[Byte])]

    for(i<-0 until bytesIndex.length){
      val res=(0 until settings.chunkNum).map(u=>{
        fheInstance.CalcSimPlain(bytesIndex(i)(u), queryParts(u))
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
        bytesIndex(i)(j) = Util.Functions.CollectAllInput(inputStream)
      }
    }

    val inputStreamParam=new FileInputStream(new File(settings.Keydir(MethodName())+"param"))
    fheInstance.param=Util.Functions.CollectAllInput(inputStreamParam)
  }

  override def DecodeQuery(s: Array[Byte]): Query = {
    var buffer=java.nio.ByteBuffer.wrap(s)
    val mat=new ModularMatrix(1,settings.dic*2)
    for(i<-0 until settings.dic*2)mat.set(0,i,BigInteger.valueOf(buffer.getInt()))
    new MRSE_FHE_SkNN_Query(mat)
  }
}
