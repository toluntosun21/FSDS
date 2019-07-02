package main

import java.io._
import java.math.BigInteger
import java.nio.ByteBuffer

import org.apache.commons.math3.linear._

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object Util {

  def readData(file:File,docNum:Int,kwNum:Int): Array[RealVector] ={
    val arrBuff=new Array[RealVector](docNum)
    for(i<-0 until docNum)arrBuff(i)=new ArrayRealVector((0 until kwNum).map(u=>0.toDouble).toArray)

    val iter=scala.io.Source.fromFile(file).getLines()

    var c=0
    while(iter.hasNext){
      val line=iter.next()
      line.split(' ').foreach(u=>{
        val key=u.split(':')(0).toInt
        val value=u.split(':')(1).toDouble
        if(key<kwNum)arrBuff(c).setEntry(key,value)
      })
      if(arrBuff(c).getNorm==0 || arrBuff(c).getNorm==Double.NaN)
        arrBuff(c).setEntry(kwNum-1,1)
      else
        arrBuff(c)=arrBuff(c).mapDivide(arrBuff(c).getNorm)
      c+=1
    }
    arrBuff
  }

  def readData(file:File,kwNum:Int): Array[RealVector] ={
    val arrBuff=new ArrayBuffer[RealVector]

    val iter=scala.io.Source.fromFile(file).getLines()

    var c=0
    while(iter.hasNext){
      var init:RealVector=new ArrayRealVector((0 until kwNum).map(u=>0.0).toArray)
      val line=iter.next()
      line.split(' ').foreach(u=>{
        val key=u.split(':')(0).toInt
        val value=u.split(':')(1).toDouble
        if(key<kwNum)init.setEntry(key,value)
      })
      if(init.getNorm==0 || init.getNorm==Double.NaN)
        init.setEntry(kwNum-1,1)
      else
        init=init.mapDivide(init.getNorm)
      arrBuff+=init
    }
    arrBuff.toArray
  }




  def WriteMatrix(file:File,mat:RealMatrix):Unit={
    val pw=new FileOutputStream(file)
    val buf = ByteBuffer.allocate(8) // creating a buffer that is suited for data you are reading
    for(i<- 0 until mat.getRowDimension)for(j<-0 until mat.getColumnDimension){
      buf.putDouble(0,mat.getEntry(i,j))
      pw.write(buf.array())
    }
    pw.close()
  }

  def WriteMatrix(file:File,mat:ModularMatrix):Unit={
    val pw=new FileOutputStream(file)
    val buf = ByteBuffer.allocate(4) // creating a buffer that is suited for data you are reading
    for(i<- 0 until mat.getRownum)for(j<-0 until mat.getColnum){
      buf.putInt(0,mat.get(i,j).longValue().toInt)
      pw.write(buf.array())
    }
    pw.close()
  }

  def ReadModMatrix(file:File,size:Int):ModularMatrix={
    val M=new ModularMatrix(size,size)
    val stream=new FileInputStream(file)
    val buf = ByteBuffer.allocate(4) // creating a buffer that is suited for data you are reading
    val bytes=new Array[Byte](4)
    for(i<-0 until size)for(j<-0 until size){
      stream.read(bytes)
      buf.put(bytes)
      buf.position(0)
      M.set(i,j,BigInteger.valueOf(buf.getInt(0)))
    }
    M


  }

  def WriteArrayofVector(file:File,mat:Array[RealVector]):Unit={
    val pw=new FileOutputStream(file)
    val buf = ByteBuffer.allocate(8) // creating a buffer that is suited for data you are reading

    for(i<- 0 until mat.length)for(j<-0 until mat(0).getDimension){
      buf.putDouble(0,mat(i).getEntry(j))
      pw.write(buf.array())
    }
    pw.close()
  }

  def WriteVector(file: File,vec:RealVector): Unit ={

    val pw=new FileOutputStream(file)
    val buf = ByteBuffer.allocate(8) // creating a buffer that is suited for data you are reading

    for(i<-0 until vec.getDimension){
      buf.putDouble(0,vec.getEntry(i))
      pw.write(buf.array())
    }
    pw.close()
  }


  def ReadArrayofVector(file:File,size:Int,num:Int):Array[ArrayRealVector]={

    var arr=new Array[ArrayRealVector](num)
    val M=new Array2DRowRealMatrix(size,size)
    val stream=new FileInputStream(file)
    val buf = ByteBuffer.allocate(8) // creating a buffer that is suited for data you are reading
    val bytes=new Array[Byte](8)
    for(i<-0 until num) {
      arr(i)=new ArrayRealVector(size)
      for (j <- 0 until size) {
        stream.read(bytes)
        buf.put(bytes)
        buf.position(0)
        arr(i).setEntry(j, buf.getDouble(0))
      }
    }
    arr
  }

  def ReadMatrix(file:File,size:Int):RealMatrix={

    val M=new Array2DRowRealMatrix(size,size)
    val stream=new FileInputStream(file)
    val buf = ByteBuffer.allocate(8) // creating a buffer that is suited for data you are reading
    val bytes=new Array[Byte](8)
    for(i<-0 until size)for(j<-0 until size){
      stream.read(bytes)
      buf.put(bytes)
      buf.position(0)
      M.setEntry(i,j,buf.getDouble(0))
    }
    M
  }



  def ReadVector(file:File,size:Int):RealVector={
    val vec=new ArrayRealVector(size)
    val stream=new FileInputStream(file)
    val buf = ByteBuffer.allocate(8) // creating a buffer that is suited for data you are reading
    val bytes=new Array[Byte](8)
    for(i<-0 until size){
      stream.read(bytes)
      buf.put(bytes)
      buf.position(0)
      vec.setEntry(i,buf.getDouble(0))
    }
    vec
  }


  def BinaryVector(length:Int,nonzero:Int)={
    var init=(0 until length).map(u=>0).toArray
    val rand=new Random
    var counter=0
    while(counter<nonzero){
      val choice=rand.nextInt(length)
      if(init(choice)==0) {
        init(choice) = 1
        counter += 1
      }
    }
    init
  }


  def RoundNearest(d:Double):Long={
    val opt1=d.toLong.toDouble
    val opt2=opt1+1.0
    val opt3=opt1-1.0

    val diff1=math.abs(d-opt1)
    val diff2=math.abs(d-opt2)
    val diff3=math.abs(d-opt3)

    if(diff1<=diff2 && diff1<=diff3)opt1.toLong
    else if(diff2<=diff1 && diff2<=diff3)opt2.toLong
    else opt3.toLong

  }

  def DecodeDoubleArrayResult(str:String):Array[Double]={
    str.filter(u=>u!='[' && u!=']').split(",").map(u=>{

      java.lang.Double.parseDouble(u)
    })
  }

  def PrintSparse(vec:RealVector):Unit={
    for(i<-0 until vec.getDimension)if(true || vec.getEntry(i)>0.01 || vec.getEntry(i)<(-0.01) )print(i+":"+vec.getEntry(i)+",")
    println
  }

  def GenerateRandomQuery(dic:Int):RealVector={
    var query=new ArrayRealVector(dic)
    val rand=new Random()
    for(i<-0 until 5)
      query.setEntry(rand.nextInt(dic),0.2)
    for(i<-0 until 3)
      query.setEntry(rand.nextInt(dic),0.3)
    for(i<-0 until 2)
      query.setEntry(rand.nextInt(dic),0.5)
    query=query.mapMultiply(1/query.getNorm).asInstanceOf[ArrayRealVector]
    query
  }

















  def Tester(mrse:MRSEserver, mrse_user:MRSEuser,settings: Settings, testcount1:Int, testcount2:Int)={

    var indexGenTime:Long=0
    var keygenTime:Long=0
    var indexSize:Long=0
    var TrapGenKeySize:Long=0
    var ResDecKeySize:Long=0
    var t0 = System.nanoTime()
    var t1 = System.nanoTime()



    for(i<- 0 until testcount1) {

      t0 = System.nanoTime()
      mrse_user.KeyGen()
      t1 = System.nanoTime()
      keygenTime+=(t1 - t0)/1000000
      mrse_user.LoadClientKeys()
      TrapGenKeySize+=mrse_user.TrapGenKeySize()
      ResDecKeySize+=mrse_user.ResDecKeySize()

      t0 = System.nanoTime()
      mrse_user.BuildIndex()
      t1 = System.nanoTime()
      indexGenTime+=(t1 - t0)/1000000
      indexSize+=mrse.IndexSize()
    }
    mrse.StartServer()
    if(testcount1>0) {
      indexSize /= testcount1
      indexGenTime /= testcount1
      keygenTime /= testcount1
      TrapGenKeySize /= testcount1
      ResDecKeySize /=testcount1
    }else{
      mrse_user.LoadClientKeys()
    }
    var suc:Long=0

    var trapdoorTime:Long=0
    var similaritySearchTime:Long=0

    var simresDecTime:Long=0
    var PIRtime:Long=0

    var trapdoorSize:Long=0
    var simresSize:Long=0
    var respTime:Long=0

    val plainData=Util.readData(new File(settings.PlainData()),settings.docnum,settings.dic)
    val querySet=Util.readData(new File(settings.PlainQuerySet()),settings.dic)
    val numQuery=querySet.length
    var totalPassingThreshold=0.0

    for(i<-0 until testcount2){

      /*
      Plain Comp
       */

      val index=new Random().nextInt(numQuery)
      val query=querySet(index)




      val plainRes=(0 until settings.docnum).map(u=>(u,plainData(u).cosine(query))).
        sortBy(u=>u._2*(-1)).
        take(settings.K).toArray

      val r0=System.nanoTime()

      t0 = System.nanoTime()
      val trapdoor=mrse_user.GenerateTrapdoor(query)
      t1 = System.nanoTime()
      trapdoorTime+=(t1 - t0)/1000000
      trapdoorSize+=trapdoor.Size()
      val trapHash=trapdoor.hashCode()//search ID

      var clientThreads=new Array[Thread](mrse_user.indexParts())
      for(j<-0 until mrse_user.indexParts()) {
        t0 = System.nanoTime()
        val res = mrse.SimilaritySearch(trapdoor,j,trapHash)
        t1 = System.nanoTime()
        similaritySearchTime += (t1 - t0) / 1000000
        simresSize += res.Size()

        clientThreads(i)=new Thread(){
          override def run(): Unit = {
            t0 = System.nanoTime()
            mrse_user.Decrypt(res)
            t1 = System.nanoTime()
            simresDecTime += (t1 - t0) / 1000000
          }
        }
        clientThreads(i).start()
      }
      clientThreads.foreach(u=>u.join())
      val r1=System.nanoTime()
      respTime += (r1 - r0) / 1000000

      val dec_res =mrse_user.resultMap.get(trapHash)
      //      val res2=mrse_user.PIR(dec_res.map(u=>u._1))

      var localSucc=0
      plainRes.foreach(u=>{
        if(dec_res.filter(v=>v._1==u._1).length>0) {
          suc += 1
          localSucc+=1
        }
      })
      totalPassingThreshold+=dec_res.filter(u=>u._2>settings.threshold).length
      if(localSucc==0) {
        println("ERROR:::::::::::::::::::::::::::::::::")
        plainRes.foreach(u => println(u._1 + " " + u._2))
        println()
        dec_res.foreach(u => println(u._1 + " " + u._2))
      }
    }

    trapdoorTime/=testcount2
    similaritySearchTime/=testcount2
    simresDecTime/=testcount2

    trapdoorSize/=testcount2
    simresSize/=testcount2
    totalPassingThreshold/=testcount2

    println("Method: "+mrse.MethodName())
    println("docnum: "+settings.docnum)
    println("T0: "+testcount1)
    println("T1: "+testcount2)
    println("variance: "+settings.variance)
    println("threshold: "+settings.threshold)
    println("slot count: "+settings.SlotCount)
    println("plain mod: "+settings.plainMod)
    println("Success: "+suc.toDouble/testcount2/settings.K)
    println("KEYGEN(MS): "+keygenTime)
    println("TRAPDOOR GEN KEY SIZE(KB): "+TrapGenKeySize)
    println("RESULT DECRYPTION KEY SIZE(KB): "+ResDecKeySize)
    println("BUILD INDEX(MS): "+indexGenTime)
    println("BUILD INDEX(MB): "+indexSize)

    println("TRAPDOOR(MS): "+trapdoorTime)
    println("TRAPDOOR(KB): "+trapdoorSize)
    println("SIM_SEARCH(MS): "+similaritySearchTime)
    println("SIM_RESULT(KB): "+simresSize)
    println("SIM_RESULT_DEC(MS): "+simresDecTime)
    println("MEAN PIRs: "+totalPassingThreshold)
    println()
    println("RESPONSE TIME(MS): "+respTime)
    println("BANDWIDTH USAGE(KB): "+(trapdoorSize+simresSize))
    println("PLAIN INDEX SIZE(KB): "+(plainData.map(u=>u.toArray.filter(u=>u>0.001).length*12).reduce((a,b)=>a+b+1)))
    println()
    println()


  }



  def Insert(init:Array[(Int,Double)],arr:Array[(Int,Double)],k:Int):Array[(Int,Double)]={
    var list=init

    for(tuple<-arr){
      var i=9
      while(i>=0 && tuple._2>list(i)._2){
        if(i==9)list(i)=tuple
        else{
          val temp=list(i+1)
          list(i+1)=list(i)
          list(i)=temp
        }
        i-=1;
      }
    }
    return  list
  }

  def Insert(arr:Array[(Int,Double)],k:Int):Array[(Int,Double)]={
    var list=(0 until k).map(u=>(u,0.0)).toArray
    Insert(list,arr,k)
  }



  def CollectAllInput(stream:InputStream): Array[Byte] ={
    val buffer=Array.fill(10000){Byte.MinValue}
    var length=stream.read(buffer)
    var bytsBuffer=new ArrayBuffer[Byte]()
    while ( length!= -1)
    {
      bytsBuffer++=buffer.take(length)
      length=stream.read(buffer)
    }

    bytsBuffer.toArray
  }


  def SubVector(vec:ModularMatrix,start:Int,end:Int):ModularMatrix={
    val returner=new ModularMatrix(1,end-start)
    for(i<-0 until end-start)
      returner.set(0,i,vec.get(0,start+i))
    returner
  }

}
