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
    var resBuffer=new ArrayBuffer[(Int,Array[Byte])]
    val trapdoorCast=trapdoor.get().asInstanceOf[ModularMatrix]
    val trapdoorInt=(0 until trapdoorCast.getColnum).map(u=>trapdoorCast.get(0,u).longValue().toInt).toArray
    val ChunkSize=num/settings.chunkNum

    val trapdoorParts=(0 until settings.chunkNum).map(u=>{
      trapdoorInt.slice(u*ChunkSize,(u+1)*ChunkSize)
    })

    val trapdoorEncodedParts=trapdoorParts.map(u=>{
      fheInstance.EncodeTrapdoor(ChunkSize,u)
    })

    for(i<-0 until bytesIndex.length){
      val res=(0 until settings.chunkNum).map(u=>{
        fheInstance.CalcSimPlain(ChunkSize, bytesIndex(i)(u), trapdoorEncodedParts(u))
      }).reduce((a,b)=>fheInstance.Add(a,b))
      resBuffer+=((i,res))
    }
    new MRSE_FHE_SkNN_Result(resBuffer.toArray)
  }


  val trapdoorMap=new util.HashMap[Int,IndexedSeq[Array[Byte]]]
  /*
  TODO:create a logic to systematically delete trapdoors from the map
   */
  override def SimilaritySearch(trapdoor: Trapdoor, index_part: Int,searchID:Int): Result = {
    val num=settings.dic*2
    val ChunkSize=num/settings.chunkNum

    /*
    encode if new query exists,
     */
    val trapdoorEncodedParts = if(trapdoorMap.containsKey(searchID)==false) {
      val trapdoorCast=trapdoor.get().asInstanceOf[ModularMatrix]
      val trapdoorInt=(0 until trapdoorCast.getColnum).map(u=>trapdoorCast.get(0,u).longValue().toInt).toArray
      val trapdoorParts = (0 until settings.chunkNum).map(u => {
        trapdoorInt.slice(u * ChunkSize, (u + 1) * ChunkSize)
      })
      val temp=trapdoorParts.map(u => {
        fheInstance.EncodeTrapdoor(ChunkSize, u)
      })
      trapdoorMap.put(searchID,temp)
      temp
    }else trapdoorMap.get(searchID)

    val t0=System.nanoTime()
      val res=(0 until settings.chunkNum).map(u=>{
        fheInstance.CalcSimPlain(ChunkSize, bytesIndex(index_part)(u), trapdoorEncodedParts(u))
      }).reduce((a,b)=>fheInstance.Add(a,b))
    val t1=System.nanoTime()
    println("real calc: "+(t1-t0)/1000000)
    new MRSE_FHE_SkNN_Result(Array((index_part,res)))
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
