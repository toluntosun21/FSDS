package FSDS

import java.io.File
import java.net.Socket

import scala.util.Random

class SDS_SocketClient(settings: Settings, mrse_user:MRSEuser){
  val querySet=Util.Functions.readData(new File(settings.PlainQuerySet()),settings.dic)
  val numQuery=querySet.length
  def Search(t:Int): Unit ={
    var totalResp=new Array[Long](t)
    for(i<-0 until t) {
      val socket = new Socket(settings.IP, settings.port)
      val outstream=socket.getOutputStream
      val instream=socket.getInputStream
      val index = new Random().nextInt(numQuery)
      println("--SEARCH INDEX: " + index)
      val queryDoc = querySet(index)
      val t0 = System.nanoTime()


      val t2 = System.nanoTime()

      val query = mrse_user.GenerateQuery(queryDoc)
      val t3 = System.nanoTime()
      println("--QUERY GENERATION: "+(t3-t2)/1000000)
      val tHash = queryDoc.hashCode()

      query.Save(outstream)
      for (i <- 0 until mrse_user.indexParts()) {
        val encodedres = Util.Functions.CollectInput(instream)
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
    println("--MEAN RESPONSE: "+Util.Functions.BestKres(totalResp))
  }


}


object SDS_SocketClient {



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
    val client=new FSDS_P_User(settings)

    client.LoadClientKeys()
    println("KEYS LOADED")
    val sc=new SDS_SocketClient(settings,client)
    sc.Search(10)
  }
}
