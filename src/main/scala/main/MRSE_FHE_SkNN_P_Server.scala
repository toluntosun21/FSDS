package main

import java.util
import java.util.Random
import java.util.concurrent.Semaphore

import scala.collection.mutable.ArrayBuffer

class MRSE_FHE_SkNN_P_Server(settings: Settings) extends MRSE_FHE_SkNN_Server(settings) {

  override def MethodName(): String = super.MethodName()+"_PARALLEL_"+settings.chunkNum

  override def SimilaritySearch(trapdoor: Trapdoor): Result = {
    val tHash=trapdoor.hashCode()
    val res=(0 until indexParts()).
      map(u=>SimilaritySearch(trapdoor,u,tHash).asInstanceOf[MRSE_FHE_SkNN_Result].get().
      asInstanceOf[Array[(Int,Array[Byte])]]).reduce((a,b)=>a++b)
    new MRSE_FHE_SkNN_Result(res)
  }

  val trapdoorMap=new util.HashMap[Int,Array[Array[Int]]]

  def GetOrCreateTrapdoor(searchID:Int,trapdoor: Trapdoor):Array[Array[Int]]={
    if(trapdoorMap.containsKey(searchID))return trapdoorMap.get(searchID)

    val num=settings.dic*2
    val ChunkSize=num/settings.chunkNum
    val Remainder=num%settings.chunkNum
    val lastChunkSize=Remainder+ChunkSize

    val trapdoorCast=trapdoor.get().asInstanceOf[ModularMatrix]

    val trapdoorInt=(0 until trapdoorCast.getColnum).map(u=>trapdoorCast.get(0,u).longValue().toInt).toArray

    val trapdoorParts = (0 until settings.chunkNum).map(u => {
      if(u==(settings.chunkNum-1))
        trapdoorInt.slice(u * ChunkSize, (u) * ChunkSize+lastChunkSize)
      else
        trapdoorInt.slice(u * ChunkSize, (u + 1) * ChunkSize)
    }).toArray
    trapdoorMap.put(searchID,trapdoorParts)
    trapdoorParts
  }

  override def SimilaritySearch(trapdoor: Trapdoor, index_part: Int,searchID:Int): Result = {

    val trapdoorParts = GetOrCreateTrapdoor(searchID,trapdoor)
    val threads = new Array[Thread](settings.chunkNum)
    val res = new Array[Array[Byte]](settings.chunkNum)


    for (j <- 0 until settings.chunkNum) {
      threads(j) = new Thread {
        override def run(): Unit = {
          val ID=j
          val chunksRes=fheInstance.CalcSimPlain(bytesIndex(index_part)(ID), trapdoorParts(ID))
          res(ID)=chunksRes
        }
      }
    }
    threads.foreach(u=>u.start())
    threads.foreach(u=>u.join())
    val allRes=fheInstance.BatchAdd(res)
    new MRSE_FHE_SkNN_Result(Array((index_part,allRes)))
  }

}
