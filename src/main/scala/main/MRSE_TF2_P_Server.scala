package main

import org.apache.commons.math3.linear.ArrayRealVector

class MRSE_TF2_P_Server(settings: Settings) extends MRSE_TF2_Server(settings) {


  override def SimilaritySearch(trapdoor: Trapdoor): Result = {

    val ChunkSize=settings.docnum/settings.chunkNum


    val threads = new Array[Thread](settings.chunkNum)
    val allRres=new Array[Array[(Int,Double)]](settings.chunkNum)

    for (i <- 0 until settings.chunkNum) {

      threads(i)=new Thread(){
        override def run(): Unit = {
          val ID=i
          val indexPart=index.slice(ID*ChunkSize,(ID+1)*ChunkSize)
          val partRes=(0 until ChunkSize).map(u=>(u+ID*ChunkSize,indexPart(u).dotProduct(trapdoor.get().
            asInstanceOf[ArrayRealVector]))).toArray
          val sortedRes=Util.Insert(partRes,settings.K)
          allRres(ID)=sortedRes
        }
      }

    }
    threads.foreach(u=>u.start())
    threads.foreach(u=>u.join())
    val cummRes=allRres.reduce((a,b)=>Util.Insert(a,b,settings.K))

    new MRSE_TF2_Result(cummRes)
  }

}
