package Util

import java.io._
import java.math.BigInteger
import java.nio.ByteBuffer

import org.apache.commons.math3.linear._

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object Functions {

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
      if(arrBuff(c).getL1Norm==0 || arrBuff(c).getNorm==0 || arrBuff(c).getNorm==Double.NaN)
        arrBuff(c).setEntry(kwNum-1,1.0)
      else
        arrBuff(c)=arrBuff(c).mapDivide(arrBuff(c).getNorm)
      c+=1
    }
    arrBuff.map(u=>{
      if(u.getL1Norm==0 || u.getNorm==0|| u.getNorm==Double.NaN)
      u.setEntry(kwNum-1,1.0)
      u
    })
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
        init.setEntry(kwNum-1,1.0)
      else
        init=init.mapDivide(init.getNorm)
      arrBuff+=init
    }
    val arr=arrBuff.toArray
    arr.map(u=>{
      if(u.getL1Norm==0 || u.getNorm==0|| u.getNorm==Double.NaN)
        u.setEntry(kwNum-1,1)
      u
    })

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
    for(i<-0 until vec.getDimension)if(vec.getEntry(i)>0.01 || vec.getEntry(i)<(-0.01) )print(i+":"+vec.getEntry(i)+",")
    println
  }

  def GenerateRandomQuerySet(dic:Int, L:Int=10):RealVector={
    var query=new ArrayRealVector(dic)
    val rand=new Random()
    for(i<-0 until L)
      query.setEntry(rand.nextInt(dic),1)
    query=query.mapMultiply(1/query.getNorm).asInstanceOf[ArrayRealVector]
    if(math.abs(query.getNorm-1)>0.02)throw new Exception("non-normalized query")
    query
  }


  def PartitionAndSort(arr:Array[(Int,Double)],kth:Double,k:Int):Array[(Int,Double)]={
    val returner=new Array[(Int,Double)](k)

    var j=0
    var i=0
    while(i<arr.length && j<k){//take the first best k
      if(arr(i)._2>=kth) {
        returner(j) = arr(i)
        j += 1
      }
      i+=1
    }

    val sorted=returner.sortBy(u=>u._2*(-1))
    return sorted

  }


  def Merge(init:Array[(Int,Double)],arr:Array[(Int,Double)],k:Int):Array[(Int,Double)]={
    val returner=new Array[(Int,Double)](k)
    var i=0
    var t=0
    var j=0


    while(i<k){
      if(init(t)._2>arr(j)._2) {
        returner(i) = init(t)
        t+=1

      }else
      {
        returner(i) = arr(j)
        j+=1

      }

      i+=1
    }
    return returner
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
    /*val kth=GFG.kthLargest(arr.map(u=>u._2),k)
    val sortedTopK=PartitionAndSort(arr,kth,k)
    val merged=Merge(init,sortedTopK,k)
    return merged*/
  }

  def Insert(arr:Array[(Int,Double)],k:Int):Array[(Int,Double)]={
    var list=(0 until k).map(u=>(u,0.0)).toArray
    Insert(list,arr,k)
  }

  def CollectInput(stream:InputStream,length:Int): Array[Byte] ={
    var buffer:Array[Byte]=null
    var readLength=0

    var bytsBuffer=new ArrayBuffer[Byte]()
    while ( readLength!=(-1) && bytsBuffer.size<length)
    {
      buffer=Array.fill(length-bytsBuffer.size){Byte.MinValue}
      readLength=stream.read(buffer)
      bytsBuffer++=buffer.take(readLength)

    }

    bytsBuffer.toArray
  }

  def CollectInput(stream:InputStream): Array[Byte] ={
    val sizeEncoded=CollectInput(stream,4)
    var buffer=java.nio.ByteBuffer.wrap(sizeEncoded)
    val sizeData=buffer.getInt()
    CollectInput(stream,sizeData)
  }

  def CollectAllInput(stream:InputStream): Array[Byte] ={
    val buffer=Array.fill(10000){Byte.MinValue}
    var length=stream.read(buffer)
    var bytsBuffer=new ArrayBuffer[Byte]()
    while ( length!= -1)
    {
//      println("l: "+length)
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

  def ToIntBytes(i:Int):Array[Byte]={
    val buffer=java.nio.ByteBuffer.allocate(4)
    buffer.putInt(i)
    var init=buffer.array()
    val returner=new Array[Byte](4)
    returner(0)=init(3)
    returner(1)=init(2)
    returner(2)=init(1)
    returner(3)=init(0)
    returner

  }


  def DiscardDeviatedResultAndComputeMean(data:Array[Long],margin:Double=0.1):Long={
    if(data.length==0)return 0
    else if(data.length==1)return data(0)
    val meanInit=data.sum/data.length.toDouble
    val upperBound=meanInit+meanInit*margin
    val lowerBound=meanInit-meanInit*margin

    val filtered=data.filter(u=>{
      u>=lowerBound && u<=upperBound
    })

    if(filtered.length.toDouble>=data.length.toDouble*0.8)
      return (filtered.sum/filtered.length.toDouble).toLong
    else return DiscardDeviatedResultAndComputeMean(data,margin+0.1)
  }

  def BestKres(data:Array[Long],num:Int=5):Long={
    if(data.length==0)return 0
    else if(data.length==1)return data(0)
    data.sortBy(u=>u).take(num).sum/num
  }


  def main(args: Array[String]): Unit = {
    val random=new Random()
    val k=5
    val testsize=1000
    val arr=(0 until testsize).map(u=>(u,random.nextDouble())).toArray

    val best=arr.sortBy(u=>u._2*(-1)).take(k)

    val inserted=Insert(arr,k)

    best.foreach(u=>println(u._1))

    println()
    inserted.foreach(u=>println(u._1))
  }

  /*
  Modular Manhattan Distance
   */
  def ManhattanDistance(arr1:ModularMatrix,arr2:ModularMatrix): Int ={
    var total:Int=0
    for(i<-0 until arr1.getColnum){
      var curr:Int=arr1.get(0,i).longValueExact().toInt-arr2.get(0,i).longValueExact().toInt
      if(curr<0)
        curr+=ModularMatrix.mod.longValueExact().toInt
      val opt2:Int=ModularMatrix.mod.longValueExact().toInt-curr

      if(curr<opt2)
        total+=curr
      else
        total+=opt2


    }
    return total
  }

}
