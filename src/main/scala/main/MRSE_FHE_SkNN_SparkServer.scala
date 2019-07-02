package main

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer

class MRSE_FHE_SkNN_SparkServer(settings: Settings) extends MRSE_FHE_SkNN_Server(settings) {

  override def MethodName(): String = super.MethodName()+"_SPARK_"+settings.chunkNum
  override def SimilaritySearch(trapdoor: Trapdoor): Result = {
    val num=settings.dic*2
    var resBuffer=new ArrayBuffer[(Int,Array[Byte])]
    val trapdoorCast=trapdoor.get().asInstanceOf[ModularMatrix]
    val trapdoorInt=(0 until trapdoorCast.getColnum).map(u=>trapdoorCast.get(0,u).longValue().toInt).toArray
    val ChunkSize=num/settings.chunkNum

    val trapdoorParts=(0 until settings.chunkNum).map(u=>{
      trapdoorInt.slice(u*ChunkSize,(u+1)*ChunkSize)
    })

    val param=fheInstance.param
    val encoderExe=settings.FHEEncodeTrapdoor
    val trapdoorEncodedParts=context.parallelize(trapdoorParts).map(u=>{
      FHE.EncodeTrapdoor(encoderExe,param,ChunkSize,u)

    }).collect()

    val calcSimExe=settings.FHECalcSimPlain
    val addExe=settings.FHEAdder
    for(i<-0 until rddINDEX.length){
      /*resBuffer+=((i,rddINDEX(i).mapPartitions(u=>{
        val temp=u.toArray.map(v=>FHE.CalcSimPlain(calcSimExe,param,ChunkSize,v,trapdoorEncodedParts(i))).reduce((a,b)=>FHE.Add(addExe,param,a,b))
        Array(temp).toIterator
      }).reduce((a,b)=>FHE.Add(addExe,param,a,b))))
      */resBuffer+=((i,rddINDEX(i).map(u=>FHE.CalcSimPlain(calcSimExe,param,ChunkSize,u,trapdoorEncodedParts(i))).
        reduce((a,b)=>FHE.Add(addExe,param,a,b))))
    }



    new MRSE_FHE_SkNN_Result(resBuffer.toArray)
  }

  var rddINDEX:Array[RDD[Array[Byte]]]=null
  var context:SparkContext=null
  override def StartServer(): Unit = {
    super.StartServer()
    val conf = new SparkConf().setAppName("MRSE_FHE_SkNN").setMaster("local[16]")
    context=new SparkContext(conf)
    var bytesIndexProcessed=new Array[Array[Array[Byte]]](bytesIndex(0).length)

    for(i<-0 until bytesIndex(0).length)
      bytesIndexProcessed(i)=new Array[Array[Byte]](bytesIndex.length)

    for(i<-0 until bytesIndex(0).length)for(j<-0 until bytesIndex.length)
      bytesIndexProcessed(i)(j)=bytesIndex(j)(i)

    rddINDEX=new Array[RDD[Array[Byte]]](bytesIndexProcessed.length)
    for(i<-0 until bytesIndexProcessed.length)
      rddINDEX(i)=context.parallelize(bytesIndexProcessed(i),settings.chunkNum).cache()

  }


}

