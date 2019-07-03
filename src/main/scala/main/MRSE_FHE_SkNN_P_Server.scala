package main

import scala.collection.mutable.ArrayBuffer

class MRSE_FHE_SkNN_P_Server(settings: Settings) extends MRSE_FHE_SkNN_Server(settings) {

  override def MethodName(): String = super.MethodName()+"_PARALLEL_"+settings.chunkNum

  override def SimilaritySearch(trapdoor: Trapdoor): Result = {
    val num=settings.dic*2
    var resBuffer=new ArrayBuffer[(Int,Array[Byte])]
    val trapdoorCast=trapdoor.get().asInstanceOf[ModularMatrix]
    val trapdoorInt=(0 until trapdoorCast.getColnum).map(u=>trapdoorCast.get(0,u).longValue().toInt).toArray
    val ChunkSize=num/settings.chunkNum

    val trapdoorParts=(0 until settings.chunkNum).map(u=>{
      trapdoorInt.slice(u*ChunkSize,(u+1)*ChunkSize)
    })

    var trapdoorEncodedParts=new Array[Array[Byte]](settings.chunkNum)
    println("NUM CORES: "+Runtime.getRuntime().availableProcessors())

    val param=fheInstance.param
    val trapdoorExe=settings.FHEEncodeTrapdoor
    val calcSimExe=settings.FHECalcSimPlain
    val addExe=settings.FHEAdder


    for(i<-0 until bytesIndex.length) {
      var threads = new Array[Thread](settings.chunkNum)
      var chunksRes=new Array[Array[Byte]](settings.chunkNum)

      for (j <- 0 until settings.chunkNum) {
        threads(j) = new Thread {
          override def run(): Unit = {
            if(i==0)trapdoorEncodedParts(j)=FHE.EncodeTrapdoor(trapdoorExe,param,ChunkSize,trapdoorParts(j))
            chunksRes(j)=FHE.CalcSimPlain(calcSimExe,param,ChunkSize, bytesIndex(i)(j), trapdoorEncodedParts(j))
          }
        }
      }
      threads.foreach(u=>u.start())
      threads.foreach(u=>u.join())
      resBuffer+=((i,chunksRes.reduce((a,b)=>FHE.Add(addExe,param,a,b))))
    }

    new MRSE_FHE_SkNN_Result(resBuffer.toArray)
  }


  override def SimilaritySearch(trapdoor: Trapdoor, index_part: Int,searchID:Int): Result = {
    val t0=System.nanoTime()
    val num=settings.dic*2

    val ChunkSize=num/settings.chunkNum
    /*
    encode if new query exists,
     */
    val trapdoorEncodedParts = if(trapdoorMap.containsKey(searchID)==false) {

      val trapdoorCast=trapdoor.get().asInstanceOf[ModularMatrix]
      val trapdoorInt=(0 until trapdoorCast.getColnum).map(u=>trapdoorCast.get(0,u).longValue().toInt).toArray

      var threads = new Array[Thread](settings.chunkNum)
      var trapdoorEncoded=new Array[Array[Byte]](settings.chunkNum)
      val trapdoorParts = (0 until settings.chunkNum).map(u => {
        trapdoorInt.slice(u * ChunkSize, (u + 1) * ChunkSize)
      })

      for(i<-0 until settings.chunkNum)
        threads(i)=new Thread(){
          override def run(): Unit = {
            trapdoorEncoded(i)=fheInstance.EncodeTrapdoor(ChunkSize, trapdoorParts(i))
          }
        }
      threads.foreach(u=>u.start)
      threads.foreach(u=>u.join)
      trapdoorMap.put(searchID,trapdoorEncoded.toIndexedSeq)
      trapdoorEncoded.toIndexedSeq
    }else trapdoorMap.get(searchID)

    println("NUM CORES: "+Runtime.getRuntime().availableProcessors())


    var threads = new Array[Thread](settings.chunkNum)
    var chunksRes=new Array[Array[Byte]](settings.chunkNum)
    val param=fheInstance.param
    val calcSimExe=settings.FHECalcSimPlain
    val addExe=settings.FHEAdder

    for (j <- 0 until settings.chunkNum) {
      threads(j) = new Thread {
        override def run(): Unit = {
          val ID=j
          chunksRes(ID)=FHE.CalcSimPlain(calcSimExe,param,ChunkSize, bytesIndex(index_part)(ID), trapdoorEncodedParts(ID))
        }
      }
    }
    threads.foreach(u=>u.start())
    threads.foreach(u=>u.join())
    val res=((index_part,chunksRes.reduce((a,b)=>FHE.Add(addExe,param,a,b))))

    val t1=System.nanoTime()
    println("real calc: "+(t1-t0)/1000000)
    new MRSE_FHE_SkNN_Result(Array(res))
  }


}
