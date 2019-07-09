package main

import java.io._
import java.util.Random

import org.apache.commons.math3.linear.RealVector

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class FHE(settings: Settings) {
  var pubKey:Array[Byte]=null
  var privateKey:Array[Byte]=null
  var param:Array[Byte]=null
  var galoisKeys:Array[Byte]=null
  var relinKeys:Array[Byte]=null


  var method:String=""
/*
0 for BFV
1 for CKKS
 */
  def KeyGen(scheme:Int): Unit ={
    val process = Runtime.getRuntime.exec(settings.FHEKeygen+" "+(scheme)+" "+
      settings.SlotCount+" "+settings.Security+ " "+settings.plainMod+ " "+
      settings.Keydir(method))
    val out = new PrintWriter(process.getOutputStream)


    new Thread("stderr reader for ") {
      override def run() {
        for(line <- Source.fromInputStream(process.getErrorStream).getLines)
          System.err.println(line)
      }
    }.start()
    while(process.isAlive){}
  }



  def TrapGen(arr:RealVector):Array[Byte]={
    val process = Runtime.getRuntime.exec(settings.FHECKKSTrapgen)
    val out = new PrintWriter(process.getOutputStream)

    val stream=process.getOutputStream
    stream.write(param)
    stream.write(pubKey)

    for (i <- 0 until settings.SlotCount)if(i<settings.dic)
      out.println((arr.getEntry(i)*settings.Scale).toInt)
    else out.println(0)
    out.close()


    new Thread("stderr reader for ") {
      override def run() {
        for(line <- Source.fromInputStream(process.getErrorStream).getLines)
          System.err.println(line)
      }
    }.start()

    Util.CollectAllInput(process.getInputStream)
  }


  def MaskGen():Array[Byte]={
    val mask_num=if(settings.SlotCount<settings.dic)settings.SlotCount else settings.dic
    val process = Runtime.getRuntime.exec(settings.FHEMaskgen+" "+
      mask_num)

    val stream=process.getOutputStream
    stream.write(param)
    stream.close()
    new Thread("stderr reader for ") {
      override def run() {
        for(line <- Source.fromInputStream(process.getErrorStream).getLines)
          System.err.println(line)
      }
    }.start()
    Util.CollectAllInput(process.getInputStream)
  }


  def CalcSim(num:Int, index:Array[Byte], trapdoor:Array[Byte], masks:Array[Byte]):Array[Byte]={
    val process = Runtime.getRuntime.exec(settings.FHECalcSim+ " "+num)


    new Thread("stderr reader for ") {
      override def run() {
        for(line <- Source.fromInputStream(process.getErrorStream).getLines)
          System.err.println(line)
      }
    }.start()

    val stream=process.getOutputStream
    stream.write(param)
    stream.write(galoisKeys)
    stream.write(masks)
    stream.write(trapdoor)
    stream.write(index)
    stream.close()
    Util.CollectAllInput(process.getInputStream)
  }





  def CalcSimPlain(index:Array[Byte], trapdoor:Array[Int]):Array[Byte]={
    val process = Runtime.getRuntime.exec(settings.FHECalcSimPlain+ " "+trapdoor.length)
    new Thread("stderr reader for ") {
      override def run() {
        for(line <- Source.fromInputStream(process.getErrorStream).getLines)
          System.err.println(line)
      }
    }.start()

    val stream=process.getOutputStream
    stream.write(param)
    //    stream.write(trapdoor)
    trapdoor.foreach(u=>stream.write(Util.ToIntBytes(u)))
    stream.write(index)
    stream.close()
    Util.CollectAllInput(process.getInputStream)
  }


  def Add(ct1:Array[Byte], ct2:Array[Byte]):Array[Byte]={
    val exe=settings.FHEAdder
    val process = Runtime.getRuntime.exec(exe)
    new Thread("stderr reader for ") {
      override def run() {
        for(line <- Source.fromInputStream(process.getErrorStream).getLines)
          System.err.println(line)
      }
    }.start()

    val stream=process.getOutputStream
    stream.write(param)
    stream.write(ct1)
    stream.write(ct2)
    Util.CollectAllInput(process.getInputStream)

  }

  def BatchAdd(cts:Array[Array[Byte]]): Array[Byte] ={
    val exe=settings.FHEBatchAdder
    if(cts.length==1)return cts(0)

    val process = Runtime.getRuntime.exec(exe+ " "+cts.length)
    new Thread("stderr reader for ") {
      override def run() {
        for(line <- Source.fromInputStream(process.getErrorStream).getLines)
          System.err.println(line)
      }
    }.start()
    val stream=process.getOutputStream
    stream.write(param)
    /*
    writing all partial results causes an error, TODO: fix it
     */
    cts.take(2).foreach(u=>stream.write(u))
    stream.close()
    Util.CollectAllInput(process.getInputStream)

  }

  def IndexGen(list:Array[ModularMatrix],id:Int,id2:Int=(-1)):Unit={
    val process = Runtime.getRuntime.exec(settings.FHEIndexgen+" "+
      list(0).getColnum+" "+
      settings.IndexPath(method,id)+{if(id2==(-1))""else "_"+id2+"_"})
    val stream=process.getOutputStream
    stream.write(param)
    stream.write(pubKey)

    val out = new PrintWriter(stream)

    for (i <- 0 until list(0).getColnum) {
      for (j <- 0 until settings.SlotCount) {
        if(j<settings.docnum)
          out.println((list(j).get(0,i)).longValue())
        else
          out.println(0)
      }
    }
    out.close()
    new Thread("stderr reader for ") {
      override def run() {
        for(line <- Source.fromInputStream(process.getErrorStream).getLines)
          System.err.println(line)
      }
    }.start()

    while(process.isAlive){}
  }


  def IndexGen(list:Array[RealVector], id:Int): Unit ={
    val process = Runtime.getRuntime.exec(settings.FHEIndexgen+" "+
      list(0).getDimension+" "+
      settings.IndexPath(method,id))
    val stream=process.getOutputStream
    stream.write(param)
    stream.write(pubKey)

    val out = new PrintWriter(stream)

    for (i <- 0 until list(0).getDimension) {
      for (j <- 0 until settings.SlotCount) {
        if(j<settings.docnum)
          out.println((list(j+id*settings.SlotCount).getEntry(i)*settings.Scale).toInt)
        else
          out.println(0)
      }
    }
   out.close()
    new Thread("stderr reader for ") {
      override def run() {
        for(line <- Source.fromInputStream(process.getErrorStream).getLines)
          System.err.println(line)
      }
    }.start()

    while(process.isAlive){}
  }





  def DecryptResult(res:Array[Byte]):Array[(Int,Double)]={
    val process = Runtime.getRuntime.exec(settings.FHEDecryptor)

    new Thread("stderr reader for ") {
      override def run() {
        for(line <- Source.fromInputStream(process.getErrorStream).getLines)
          System.err.println(line)
      }
    }.start()

    val stream=process.getOutputStream
    stream.write(param)
    stream.write(privateKey)
    stream.write(res)
    stream.close()
    (Source.fromInputStream(process.getInputStream)).getLines().map(u=>{
      (u.split(":")(0).toInt,u.split(":")(1).toDouble)
    }).toArray
  }

}
