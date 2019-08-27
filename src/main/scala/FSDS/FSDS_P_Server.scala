package FSDS

import java.util
import java.util.Random
import java.util.concurrent.Semaphore

import Util.ModularMatrix

import scala.collection.mutable.ArrayBuffer

class FSDS_P_Server(settings: Settings) extends FSDS_Server(settings) {

  override def MethodName(): String = super.MethodName()+"_PARALLEL_"+settings.chunkNum

  override def SimilaritySearch(query: Query): Result = {
    val tHash=query.hashCode()
    val res=(0 until indexParts()).
      map(u=>SimilaritySearch(query,u,tHash).asInstanceOf[MRSE_FHE_SkNN_Result].get().
      asInstanceOf[Array[(Int,Array[Byte])]]).reduce((a,b)=>a++b)
    new MRSE_FHE_SkNN_Result(res)
  }

  val queryMap=new util.HashMap[Int,Array[Array[Int]]]

  def GetOrCreateQuery(searchID:Int, query: Query):Array[Array[Int]]={
    if(queryMap.containsKey(searchID))return queryMap.get(searchID)

    val num=settings.dic*2
    val ChunkSize=num/settings.chunkNum
    val Remainder=num%settings.chunkNum
    val lastChunkSize=Remainder+ChunkSize

    val queryCast=query.get().asInstanceOf[ModularMatrix]

    val queryInt=(0 until queryCast.getColnum).map(u=>queryCast.get(0,u).longValue().toInt).toArray

    val queryParts = (0 until settings.chunkNum).map(u => {
      if(u==(settings.chunkNum-1))
        queryInt.slice(u * ChunkSize, (u) * ChunkSize+lastChunkSize)
      else
        queryInt.slice(u * ChunkSize, (u + 1) * ChunkSize)
    }).toArray
    queryMap.put(searchID,queryParts)
    queryParts
  }

  override def SimilaritySearch(query: Query, index_part: Int, searchID:Int): Result = {

    val queryParts = GetOrCreateQuery(searchID,query)
    val threads = new Array[Thread](settings.chunkNum)
    val res = new Array[Array[Byte]](settings.chunkNum)


    for (j <- 0 until settings.chunkNum) {
      threads(j) = new Thread {
        override def run(): Unit = {
          val ID=j
          val chunksRes=fheInstance.CalcSimPlain(bytesIndex(index_part)(ID), queryParts(ID))
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
