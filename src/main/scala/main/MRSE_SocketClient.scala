package main

import java.io.File
import java.net.Socket

import scala.util.Random

class MRSE_SocketClient(settings: Settings,mrse_user:MRSEuser){
  val querySet=Util.readData(new File(settings.PlainQuerySet()),settings.dic)
  val numQuery=querySet.length
  def Search(t:Int): Unit ={
    var totalResp=new Array[Long](t)
    for(i<-0 until t) {
      val socket = new Socket(settings.IP, settings.port)
      val outstream=socket.getOutputStream
      val instream=socket.getInputStream
      val index = new Random().nextInt(numQuery)
      println("--SEARCH INDEX: " + index)
      val query = querySet(index)
      val t0 = System.nanoTime()


      val t2 = System.nanoTime()

      val trapdoor = mrse_user.GenerateTrapdoor(query)
      val t3 = System.nanoTime()
      println("--TRAPDOOR GENERATION: "+(t3-t2)/1000000)
      val tHash = trapdoor.hashCode()

      trapdoor.Save(outstream)
      for (i <- 0 until mrse_user.indexParts()) {
        val encodedres = Util.CollectInput(instream)
        val decodedres = mrse_user.DecodeResult(encodedres)
        mrse_user.Decrypt(decodedres, tHash)
      }
      val dec_res = mrse_user.resultMap.get(tHash)

      /*NO ACCURACY CHECK HERE*/
      val t1 = System.nanoTime()
      val responseTime = (t1 - t0) / 1000000
      println("--RESPONSE: " + responseTime)
      println()
      totalResp(i)=responseTime
      instream.close()
      outstream.close()
      socket.close()
    }
    println("--MEAN RESPONSE: "+Util.DiscardDeviatedResultAndComputeMean(totalResp))
  }


}


object MRSE_SocketClient {



  def main(args: Array[String]): Unit = {
    val settings=new Settings
    settings.SlotCount=2048
    settings.plainMod=12289
    settings.Security=128
    settings.docnum=2048//4096
    settings.chunkNum=16
    settings.setname="enron_"+settings.docnum
    settings.dic=2445//3615
    settings.Security=128
    settings.variance=0.02
    val client=new MRSE_FHE_SkNN_P_User(settings)

    client.LoadClientKeys()
    println("KEYS LOADED")
    val sc=new MRSE_SocketClient(settings,client)
    sc.Search(10)
  }
}
