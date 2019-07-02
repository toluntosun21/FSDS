package main

import org.apache.commons.math3.linear.ArrayRealVector

class MRSE_TF2_P_Server(settings: Settings) extends MRSE_TF2_Server(settings) {
/*
TODO: apply insertion of K elemens and complete this func
 */
  override def SimilaritySearch(trapdoor: Trapdoor): Result = {
/*
    val ChunkSize=settings.dic/settings.chunkNum


    var threads = new Array[Thread](settings.chunkNum)
    var chunksRes=new Array[Array[Byte]](settings.chunkNum)

    for (i <- 0 until settings.chunkNum) {

      threads(i)=new Thread(){
        override def run(): Unit = {
          val indexPart=index.slice(i*settings.chunkNum,(i+1)*settings.chunkNum)
          (0 until ChunkSize).map(u=>(u,indexPart(u).dotProduct(trapdoor.get().
            asInstanceOf[ArrayRealVector])))
        }
      }

    }
      new MRSE_TF2_Result((0 until settings.docnum).
      map(u=>(u,index(u).dotProduct(trapdoor.get().
        asInstanceOf[ArrayRealVector]))).sortBy(u=>u._2*(-1)).take(settings.K).toArray)
  */null}

}
