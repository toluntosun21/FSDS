package main

import java.io.{File, PrintWriter}
import java.util

import org.apache.commons.math3.analysis.MultivariateFunction
import org.apache.commons.math3.linear._

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.Random

class SecureKNN(settings: Settings,mean:Double,sigma:Double){



  var splitRange=1.0

  val rand=new Random()
  private def NormalSample():Double={
    val meanP=mean/w.toDouble
    val c=Math.sqrt(3.0/w.toDouble)*sigma

    val window=2*c
    val init=window* rand.nextDouble()
    init+(meanP-c)
  }


  val maxInM=4
  //val randTotal=100
  val scaleMax=4

  var w=64
  def U=w*2
  def T=U+1

  def Mdim():Int=settings.dic+T



  private def Split(arr:RealVector,S:RealVector,isQuery:Boolean=true):(RealVector)={
    val arr1=new ArrayRealVector(Mdim)
    val arr2=new ArrayRealVector(Mdim)
    val rand=new Random()
    for(i<- 0 until S.getDimension){
      val key=S.getEntry(i)
      if((key==1.0 && isQuery) || (key==0.0&&(!isQuery))){
        val randomNum=rand.nextDouble()*splitRange
        val diff=arr.getEntry(i)-randomNum

        arr1.setEntry(i,randomNum)
        arr2.setEntry(i,diff)
      }else{
        arr1.setEntry(i,arr.getEntry(i))
        arr2.setEntry(i,arr.getEntry(i))
      }
    }
    arr1.append(arr2)
  }

  def SplitDoc(arr:RealVector,S:RealVector):(RealVector)=Split(arr,S,false)
  def SplitQuery(arr:RealVector,S:RealVector):(RealVector)=Split(arr,S,true)

  def GenerateSecretMatrix():RealMatrix={
    println(  "Creating an M   ")
    val rand=new Random()
    val init=(0 until (Mdim)).map(u=>{
      (0 until (Mdim)).map(v=>{
        val init=(rand.nextInt(maxInM-1)+1).toDouble
        init
      }).toArray
    }).toArray

    val trial=new Array2DRowRealMatrix(init)
    if(new LUDecomposition(trial).getSolver.isNonSingular)
      trial else GenerateSecretMatrix()
  }

  def GenerateSecretMatrices(): (RealMatrix,RealMatrix) ={
    M= (GenerateSecretMatrix(),GenerateSecretMatrix())
    M
  }

  def setInverseMs(): Unit ={
    val Minv1=new LUDecomposition(M._1).getSolver.getInverse
    val Minv2=new LUDecomposition(M._2).getSolver.getInverse
    Minv=(Minv1,Minv2)

  }

  /*
  this is for test purpose, will result in incorrect search results
   */
  def setInversesRandom(): Unit ={
    Minv=(GenerateSecretMatrix(),GenerateSecretMatrix())

  }

  def GenerateS():RealVector={
    val rand=new Random()
    val init=(0 until (Mdim)).map(u=>rand.nextInt(2)).toArray
    S=new ArrayRealVector(init.map(u=>u.toDouble))
    S
  }

  def extendQuery(arr:RealVector):RealVector={
    val rand=new Random()
    val scaler=1//rand.nextDouble()*scaleMax
    val toAdd=rand.nextDouble()*scaleMax
    val randomBinaryVector=Util.BinaryVector(U,w)

    arr.
      append(new ArrayRealVector(randomBinaryVector.map(u=>u.toDouble)))
      .mapMultiply(scaler).append(toAdd)
  }

  def EncryptQuery(M1inv:RealMatrix, M2inv:RealMatrix, S:RealVector,query:RealVector):RealVector={

    val scaled=extendQuery(query)
    val splitted=SplitQuery(scaled,S)
//    val M1inv= new LUDecomposition(M1).getSolver.getInverse
//    val M2inv= new LUDecomposition(M2).getSolver.getInverse
    val part1=M1inv.preMultiply(splitted.getSubVector(0,Mdim))
    val part2=M2inv.preMultiply(splitted.getSubVector(Mdim,Mdim))
    part1.append(part2)


  }

  def EncryptQuery(query:RealVector):RealVector={
    EncryptQuery(Minv._1, Minv._2, S,query)
  }


  def ExtendDoc(doc:RealVector):RealVector={
    doc.append(new ArrayRealVector((0 until U).map(u=>NormalSample()).toArray)).append(1.0)
  }


  def EncryptDoc(M1:RealMatrix, M2:RealMatrix, S:RealVector,doc:RealVector):RealVector={
    val splitted=SplitDoc(ExtendDoc(doc),S)
    val M1tr= M1.transpose()
    val M2tr= M2.transpose()
    if(splitted.getDimension!=(2*Mdim()))throw new Error
    val part1=M1tr.preMultiply(splitted.getSubVector(0,Mdim))
    val part2=M2tr.preMultiply(splitted.getSubVector(Mdim,Mdim))
    part1.append(part2)

  }

  def EncryptDoc(doc:RealVector):RealVector=EncryptDoc(M._1,M._2,S,doc)


  def readS():RealVector={
    val file=settings.S()
    val lines=scala.io.Source.fromFile(file)
    var max=0.0
    val u=lines.getLines().toList.last

    S={
      val arr=u.filter(u=>(u!='}' && u!='{' && u!=',')).split("; ").map(u=>u.toDouble.toDouble)
      val vec=new ArrayRealVector(arr)
      vec
    }
    S
  }

  def readM():(RealMatrix,RealMatrix)={
    val fileM1=settings.M1()
    val fileM2=settings.M2()
    val lines1=scala.io.Source.fromFile(fileM1)
    val lines2=scala.io.Source.fromFile(fileM2)
    val M1=new Array2DRowRealMatrix(settings.dic+T,settings.dic+T)
    val M2=new Array2DRowRealMatrix(settings.dic+T,settings.dic+T)
    var i=0
    lines1.getLines().foreach(u=>{
      var c=0
      val arr=u.filter(u=>{(u!='}' && u!='{')}).split("; ").map(u=>u.toDouble.toDouble)
      arr.foreach(v=>{
        M1.setEntry(i,c,v)
        c+=1
      })
      i+=1
    })
    i=0
    lines2.getLines().foreach(u=>{
      var c=0
      val arr=u.filter(u=>(u!='}' && u!='{' )).split("; ").map(u=>u.toDouble.toDouble)
      arr.foreach(v=>{
        M2.setEntry(i,c,v)
        c+=1
      })
      i+=1
    })
    M=(M1,M2)
    M
  }

  def readMinv():(RealMatrix,RealMatrix)={
    val fileM1=settings.M1inv()
    val fileM2=settings.M2inv()
    val lines1=scala.io.Source.fromFile(fileM1)
    val lines2=scala.io.Source.fromFile(fileM2)
    val M1=new Array2DRowRealMatrix(settings.dic+T,settings.dic+T)
    val M2=new Array2DRowRealMatrix(settings.dic+T,settings.dic+T)
    var i=0
    lines1.getLines().foreach(u=>{
      var c=0
      val arr=u.filter(u=>{(u!='}' && u!='{')}).split("; ").map(u=>u.toDouble.toDouble)
      arr.foreach(v=>{
        M1.setEntry(i,c,v)
        c+=1
      })
      i+=1
    })
    i=0
    lines2.getLines().foreach(u=>{
      var c=0
      val arr=u.filter(u=>(u!='}' && u!='{' )).split("; ").map(u=>u.toDouble.toDouble)
      arr.foreach(v=>{
        M2.setEntry(i,c,v)
        c+=1
      })
      i+=1
    })
    Minv=(M1,M2)
    Minv
  }

  def readData():(Array[RealVector])={/*
    val file=settings.knnEncryptedData()
    val lines=scala.io.Source.fromFile(file)
    lines.getLines().map(u=>{
      val arr=u.filter(u=>(u!='}' && u!='{' && u!=',')).split("; ").map(u=>u.toDouble.toDouble)
      val vec=new ArrayRealVector(arr)
      vec

    }).toArray*/null
  }


  def KeyGenAndKNNenc(): Unit ={
    val S=GenerateS()
    val M=GenerateSecretMatrices()
    println("starting inversion")
    val M1inv=new LUDecomposition(M._1).getSolver.getInverse
    val M2inv=new LUDecomposition(M._2).getSolver.getInverse
    println("inverted")
    val pwS=new PrintWriter(settings.S())
    pwS.println(S)
    val pwM1=new PrintWriter(settings.M1())
    for(i<- 0 until M._1.getRowDimension){
      pwM1.println(M._1.getRowVector(i))
    }
    val pwM2=new PrintWriter(settings.M2())
    for(i<- 0 until M._2.getRowDimension){
      pwM2.println(M._2.getRowVector(i))
    }
    pwS.close()
    pwM1.close()
    pwM2.close()

    val pwM1inv=new PrintWriter(settings.M1inv())
    for(i<- 0 until M1inv.getRowDimension){
      pwM1inv.println(M1inv.getRowVector(i))
    }
    val pwM2inv=new PrintWriter(settings.M2inv())
    for(i<- 0 until M2inv.getRowDimension){
      pwM2inv.println(M2inv.getRowVector(i))
    }
    pwM1inv.close()
    pwM2inv.close()

    val plain_data=Util.readData(new File(settings.PlainData()),settings.docnum,settings.dic)

    val pww=null//new PrintWriter(null)
    val enc_data=(0 until plain_data.length).map(u=>{
      println(u)
      val enc=EncryptDoc(M._1,M._2,S,plain_data(u))
      enc
    }).toArray


    (0 until settings.docnum).foreach(d=>{
      println(d)
     // pww.println(enc_data(d))
    })
//    pww.close()
  }

  var S:RealVector=null
  var M:(RealMatrix,RealMatrix)=null
  var Minv:(RealMatrix,RealMatrix)=null
}

