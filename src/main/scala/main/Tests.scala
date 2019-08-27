package main

import java.io.File
import java.util.Random

import FSDS.{SecureKNN, _}
import org.apache.commons.math3.linear.ArrayRealVector


object Tests {
  /*
  0 for SkNN
  1 for mSkNN
   */
  def QueryGenerationTests(mode:Int, testCount:Int=100): Unit ={
    {
      val random=new Random()
      Array(/*2010,4005,*/6000,8010,10005).foreach(u=>{
        val settings=new Settings()
        settings.dic=u
        settings.docnum=16384
        val querySet=Util.Functions.readData(new File(settings.PlainQuerySet()),settings.dic)
        val numQuery=querySet.length
        val res=new Array[Long](testCount)
        if(mode==0) {
          println("Testing SkNN")
          val knn = new SecureKNN(settings, 0, 0.1)
        //  knn.GenerateSecretMatrices()
          knn.GenerateS()
          knn.setInversesRandom()
          for(i<-0 until testCount){
            print(i+"::")
            val query=querySet(random.nextInt(numQuery))
            val t0=System.nanoTime()
            knn.EncryptQuery(query)
            val t1=System.nanoTime()
            val resThis=(t1-t0)/1000000
            res(i)=resThis
          }
          println()
          println("SkNN")
          println("DONE")
          println("w: "+settings.dic)
          println("Time: "+Util.Functions.BestKres(res))
          val tempMat=new File("tempKey")
          val tempS=new File("tempS")
          Util.Functions.WriteMatrix(tempMat,knn.Minv._1)
          Util.Functions.WriteVector(tempS,knn.S)
          println("KeySize: "+(2*tempMat.length()/1024+tempS.length()/1024))
          tempMat.delete()
          tempS.delete()
          println()
          println()

        }else{
          settings.Scale=200
          settings.plainMod=40961
          val mSkNN=new mSkNN(settings)
          mSkNN.GenerateSecretMatrix()
          mSkNN.GenerateInverseMatrix()
          for(i<-0 until testCount){
            print(i+"::")
            val query=querySet(random.nextInt(numQuery))
            val t0=System.nanoTime()
            mSkNN.EncryptQuery(query)
            val t1=System.nanoTime()
            val resThis=(t1-t0)/1000000
            res(i)=resThis
          }
          println()
          println("mSkNN")
          println("DONE")
          println("w: "+settings.dic)
          println("SecureKnn: "+Util.Functions.BestKres(res))
          println()
          println()
        }



      })
    }



  }


  def TF2CloudTimingAndAccuracyTest(settings: Settings, testcount1:Int,testcount2:Int,mode:Int=0): Unit ={
    val similaritySearchTime=new Array[Long](testcount1*testcount2)

    val plainData=Util.Functions.readData(new File(settings.PlainData()),settings.docnum,settings.dic)
    val querySet=Util.Functions.readData(new File(settings.PlainQuerySet()),settings.dic)
    val numQuery=querySet.length
    var totalPassingThreshold=0.0

    var totalSuccReal=0.0
    var algSuccTotal=0.0
    val user=new MRSE_TF2_User(settings)
    val server=new MRSE_TF2_Server(settings)
    val knn=new SecureKNN(settings,0,settings.variance)

    for(j<-0 until testcount1) {
      knn.GenerateS()
      val dataExtended = plainData.map(u => knn.SplitDoc(knn.ExtendDoc(u), knn.S))
      println("DATA SET EXTENDED")

      for (i <- 0 until testcount2) {
        val index = new Random().nextInt(numQuery)
        val queryDoc = if(mode==0)Util.Functions.GenerateRandomQuerySet(settings.dic)else querySet(index)

        val plainResInit = (0 until settings.docnum).map(u => (u, plainData(u).cosine(queryDoc)))

        val plainRes = plainResInit.
          sortBy(u => u._2 * (-1)).take(settings.K).toArray

        val query = knn.SplitQuery(knn.ExtendQuery(queryDoc), knn.S)

        val t0 = System.nanoTime()
        val cipherResInit = (0 until settings.docnum).map(u => (u, dataExtended(u).dotProduct(query)))
        val ranked = Util.Functions.Insert(cipherResInit.toArray, settings.K)
        val t1 = System.nanoTime()
        similaritySearchTime(j*testcount2+i) = ((t1 - t0) / 1000000)

        totalSuccReal += plainRes.map(u => u._2).sum
        algSuccTotal += ranked.map(u => plainResInit(u._1)._2).sum
      }
    }


    println("Method: MRSETF2")
    println("docnum: "+settings.docnum)
    println("dic: "+settings.dic)
    println("T0: "+testcount1)
    println("variance: "+settings.variance)
    println("Sim Success Ratio: "+algSuccTotal/totalSuccReal)
    println("SIM_SEARCH(MS): "+Util.Functions.DiscardDeviatedResultAndComputeMean(similaritySearchTime))
    println()
    println()

  }


  def SetupTester(mrse:SDS_Server, mrse_user:MRSEuser, settings: Settings, testcount1:Int, testcount2:Int)={

    val indexGenTime=new Array[Long](testcount1)
    val keygenTime=new Array[Long](testcount1)

    var indexSize:Long=0
    var QueryGenKeySize:Long=0
    var ResDecKeySize:Long=0
    var t0 = System.nanoTime()
    var t1 = System.nanoTime()



    for(i<- 0 until testcount1) {

      t0 = System.nanoTime()
      mrse_user.KeyGen()
      t1 = System.nanoTime()
      keygenTime(i)=(t1 - t0)/1000000
      mrse_user.LoadClientKeys()
      QueryGenKeySize+=mrse_user.QueryGenKeySize()
      ResDecKeySize+=mrse_user.ResDecKeySize()

      t0 = System.nanoTime()
      mrse_user.BuildIndex()
      t1 = System.nanoTime()
      indexGenTime(i)=(t1 - t0)/1000000
      indexSize+=mrse.IndexSize()
    }

    if(testcount1>0) {
      indexSize /= testcount1
      QueryGenKeySize /= testcount1
      ResDecKeySize /=testcount1
    }else{
      mrse_user.LoadClientKeys()
    }
    var suc:Long=0

    val queryTime=new Array[Long](testcount2)


    mrse_user.DeleteKeys()
    println("Method: "+mrse.MethodName())
    println("docnum: "+settings.docnum)
    println("dic: "+settings.dic)
    println("T0: "+testcount1)
    println("T1: "+testcount2)
    println("variance: "+settings.variance)
    println("threshold: "+settings.threshold)
    println("scale: "+settings.Scale)
    println("slot count: "+settings.SlotCount)
    println("plain mod: "+settings.plainMod)
    println("Success: "+suc.toDouble/testcount2/settings.K)
    println("KEYGEN(MS): "+Util.Functions.BestKres(keygenTime,1))
    println("QUERY GEN KEY SIZE(KB): "+QueryGenKeySize)
    println("RESULT DECRYPTION KEY SIZE(KB): "+ResDecKeySize)
    println("BUILD INDEX(MS): "+Util.Functions.BestKres(indexGenTime,1))
    println("INDEX SIZE(MB): "+indexSize)
    mrse.deleteIndexFiles()

    println()
    println()

  }







  def AllTester(mrse:SDS_Server, mrse_user:MRSEuser, settings: Settings, testcount1:Int, testcount2:Int)={

    val indexGenTime=new Array[Long](testcount1)
    val keygenTime=new Array[Long](testcount1)

    var indexSize:Long=0
    var QueryGenKeySize:Long=0
    var ResDecKeySize:Long=0
    var t0 = System.nanoTime()
    var t1 = System.nanoTime()



    for(i<- 0 until testcount1) {

      t0 = System.nanoTime()
      mrse_user.KeyGen()
      t1 = System.nanoTime()
      keygenTime(i)=(t1 - t0)/1000000
      mrse_user.LoadClientKeys()
      QueryGenKeySize+=mrse_user.QueryGenKeySize()
      ResDecKeySize+=mrse_user.ResDecKeySize()

      t0 = System.nanoTime()
      mrse_user.BuildIndex()
      t1 = System.nanoTime()
      indexGenTime(i)=(t1 - t0)/1000000
      indexSize+=mrse.IndexSize()
    }
    mrse.StartServer()

    if(testcount1>0) {
      indexSize /= testcount1
      QueryGenKeySize /= testcount1
      ResDecKeySize /=testcount1
    }else{
      mrse_user.LoadClientKeys()
    }
    var suc:Long=0

    val queryTime=new Array[Long](testcount2)
    val similaritySearchTime=new Array[Long](testcount2)

    val simresDecTime=new Array[Long](testcount2)

    var querySize:Long=0
    var simresSize:Long=0

    val plainData=Util.Functions.readData(new File(settings.PlainData()),settings.docnum,settings.dic)
    val querySet=Util.Functions.readData(new File(settings.PlainQuerySet()),settings.dic)
    val numQuery=querySet.length
    var totalPassingThreshold=0.0

    var totalSuccReal=0.0
    var algSuccTotal=0.0

    for(i<-0 until testcount2){

      /*
      Plain Comp
       */

      val index=new Random().nextInt(numQuery)
      val queryDoc=querySet(index)
      val plainResInit=(0 until settings.docnum).map(u=>(u,plainData(u).cosine(queryDoc)))

      val plainRes=plainResInit.
        sortBy(u=>u._2*(-1)).take(settings.K).toArray

      t0 = System.nanoTime()
      val query=mrse_user.GenerateQuery(queryDoc)
      t1 = System.nanoTime()
      queryTime(i)=(t1 - t0)/1000000
      querySize+=query.Size()

      t0 = System.nanoTime()
      val res=mrse.SimilaritySearch(query)
      t1 = System.nanoTime()
      similaritySearchTime(i)=(t1 - t0)/1000000
      simresSize+=res.Size()

      t0 = System.nanoTime()
      val dec_res=mrse_user.Decrypt(res)
      t1 = System.nanoTime()
      simresDecTime(i)=(t1 - t0)/1000000

      //      val res2=mrse_user.PIR(dec_res.map(u=>u._1))

      var localSucc=0
      plainRes.foreach(u=>{
        if(dec_res.filter(v=>v._1==u._1).length>0) {
          suc += 1
          localSucc+=1
        }
      })

      totalSuccReal+=plainRes.map(u=>u._2).sum
      algSuccTotal+=dec_res.map(u=>plainResInit(u._1)._2).sum

      totalPassingThreshold+=dec_res.filter(u=>u._2>settings.threshold).length
      if(localSucc==0) {
        println("ERROR:::::::::::::::::::::::::::::::::")
        plainRes.foreach(u => println(u._1 + " " + u._2))
        println()
        dec_res.foreach(u => println(u._1 + " " + u._2))
        println()

      }
    }


    querySize/=testcount2
    simresSize/=testcount2
    totalPassingThreshold/=testcount2

    println("Method: "+mrse.MethodName())
    println("docnum: "+settings.docnum)
    println("dic: "+settings.dic)
    println("T0: "+testcount1)
    println("T1: "+testcount2)
    println("variance: "+settings.variance)
    println("threshold: "+settings.threshold)
    println("scale: "+settings.Scale)
    println("slot count: "+settings.SlotCount)
    println("plain mod: "+settings.plainMod)
    println("Sim Success Ratio: "+algSuccTotal/totalSuccReal)
    println("Success: "+suc.toDouble/testcount2/settings.K)
    println("KEYGEN(MS): "+Util.Functions.DiscardDeviatedResultAndComputeMean(keygenTime))
    println("QUERY GEN KEY SIZE(KB): "+QueryGenKeySize)
    println("RESULT DECRYPTION KEY SIZE(KB): "+ResDecKeySize)
    println("BUILD INDEX(MS): "+Util.Functions.DiscardDeviatedResultAndComputeMean(indexGenTime))
    println("INDEX SIZE(MB): "+indexSize)

    println("QUERY(MS): "+Util.Functions.DiscardDeviatedResultAndComputeMean(queryTime))
    println("QUERY(KB): "+querySize)
    println("SIM_SEARCH(MS): "+Util.Functions.DiscardDeviatedResultAndComputeMean(similaritySearchTime))
    println("SIM_RESULT(KB): "+simresSize)
    println("SIM_RESULT_DEC(MS): "+Util.Functions.DiscardDeviatedResultAndComputeMean(simresDecTime))
    println("MEAN PIRs: "+totalPassingThreshold)
    println()
    println("BANDWIDTH USAGE(KB): "+(querySize+simresSize))
    println("PLAIN INDEX SIZE(KB): "+(plainData.map(u=>u.toArray.filter(u=>u>0.001).length*12).reduce((a,b)=>a+b+1)/1024))
    println()
    println()

  }



  def QueryIndTest(): Unit ={
    val settings=new Settings
    settings.dic=15
    val testCount=1000000
    settings.Scale=200
    settings.plainMod=40961
    val mSkNN=new mSkNN(settings)
    mSkNN.GenerateSecretMatrix()
    mSkNN.GenerateInverseMatrix()
    val Q1=new ArrayRealVector((0 until settings.dic).map(u=>0.0).toArray)
    val Q2=new ArrayRealVector((0 until settings.dic).map(u=>1.0/math.sqrt(settings.dic)).toArray)
    val encQuery=mSkNN.EncryptQuery(Q1)


    var min=Int.MaxValue
    var minAlg=""

    for(i<-0 until testCount){

      val query=new ArrayRealVector((0 until settings.dic).map(u=>0.0).toArray)
      val encQuery1=mSkNN.EncryptQuery(Q1)
      val encQuery2=mSkNN.EncryptQuery(Q2)

      val m1=Util.Functions.ManhattanDistance(encQuery,encQuery1)
      val m2=Util.Functions.ManhattanDistance(encQuery,encQuery2)

      if(m1<min){
        min=m1
        minAlg="Q1"
      }else if(m2<min){
        min=m2
        minAlg="Q2"
      }

      if(i%10000==0){
        println(i)
        println(min)
        println(minAlg)
        println()
      }

    }
  }


  def main(args: Array[String]): Unit = {



    val settings=new Settings
    settings.Scale=100
    settings.docnum=args(0).toInt
    settings.dic=args(1).toInt
    settings.setname="enron_"+settings.docnum
    settings.Security=128
    settings.Scale=100

    val method=args(2).toInt
    val t1=args(3).toInt
    val t2=args(4).toInt

    if(method==0){
      settings.variance=args(5).toDouble
      val server=new MRSE_TF2_Server(settings)
      val client=new MRSE_TF2_User(settings)
      Tests.AllTester(server,client,settings,t1,t2)
    }else if(method==10){
      settings.variance=args(5).toDouble
      val server=new MRSE_TF2_Server(settings)
      val client=new MRSE_TF2_User(settings)
      Tests.SetupTester(server,client,settings,t1,t2)
    }else if(method==20){
      settings.variance=args(5).toDouble
      val mode=args(6).toInt
      Tests.TF2CloudTimingAndAccuracyTest(settings,t1,t2,mode)
    }
    else if(method==1){
      settings.SlotCount=args(5).toInt
      settings.Security=args(6).toInt
      settings.plainMod=args(7).toInt
      val server=new FSDS_0_Server(settings)
      val client=new MRSE_FHE_User(settings)
      Tests.AllTester(server,client,settings,t1,t2)
    }else if(method==2){
      settings.SlotCount=args(5).toInt
      settings.Security=args(6).toInt
      settings.plainMod=args(7).toInt
      settings.chunkNum=args(8).toInt
      settings.Scale=args(9).toInt
      val server=new FSDS_Server(settings)
      val client=new MRSE_FHE_SkNN_User(settings)
      Tests.AllTester(server,client,settings,t1,t2)
    }
    else if(method==12){
      settings.SlotCount=args(5).toInt
      settings.Security=args(6).toInt
      settings.plainMod=args(7).toInt
      settings.chunkNum=args(8).toInt
      settings.Scale=args(9).toInt
      val server=new FSDS_Server(settings)
      val client=new MRSE_FHE_SkNN_User(settings)
      Tests.SetupTester(server,client,settings,t1,t2)
    }
    else if(method==3){
      settings.SlotCount=args(5).toInt
      settings.Security=args(6).toInt
      settings.plainMod=args(7).toInt
      settings.chunkNum=args(8).toInt
      val server=new FSDS_P_Server(settings)
      val client=new FSDS_P_User(settings)
      Tests.AllTester(server,client,settings,t1,t2)
    }else if(method==4){
      settings.variance=args(5).toDouble
      settings.chunkNum=args(6).toInt
      val server=new MRSE_TF2_P_Server(settings)
      val client=new MRSE_TF2_User(settings)
      Tests.AllTester(server,client,settings,t1,t2)
    }else if (method==5){
      settings.variance=args(5).toDouble
      settings.chunkNum=args(6).toInt
      settings.port=args(7).toInt
      val server=new MRSE_TF2_P_Server(settings)
      val socketserver=new MRSE_SocketServer(settings,server)
      socketserver.start()
    }else if (method==6){
      settings.SlotCount=args(5).toInt
      settings.Security=args(6).toInt
      settings.plainMod=args(7).toInt
      settings.chunkNum=args(8).toInt
      settings.port=args(9).toInt
      val server=new FSDS_P_Server(settings)
      val socketserver=new MRSE_SocketServer(settings,server)
      socketserver.start()
    }else if (method==7){
      settings.SlotCount=args(5).toInt
      settings.Security=args(6).toInt
      settings.plainMod=args(7).toInt
      settings.chunkNum=args(8).toInt
      settings.port=args(9).toInt
      val client=new FSDS_P_User(settings)
      client.LoadClientKeys()
      val sc=new SDS_SocketClient(settings,client)
      sc.Search(t1)
    }
    else if (method==8){
      settings.variance=args(5).toDouble
      settings.port=args(7).toInt
      val client=new MRSE_TF2_User(settings)
      client.LoadClientKeys()
      val sc=new SDS_SocketClient(settings,client)
      sc.Search(t1)
    }else if(method==90){
      Tests.QueryGenerationTests(t1,t2)
    }

  }



}
