package FSDS

import java.io.File
import java.net.Socket
import java.net.ServerSocket

import scala.util.Random

class MRSE_SocketServer(settings:Settings,mrse_server:SDS_Server) {


  def start(): Unit = {
    val ss = new ServerSocket(settings.port)
    mrse_server.StartServer()
    println("++SERVER STARTED")
    while(true){
      println("++WAITING FOR INCOMING CONNECTIONS")
      val s = ss.accept()
      println("++NEW CONNECTION")
      val instream=s.getInputStream
      val outstream=s.getOutputStream

      //Read input from socket

      val decodedQuery=mrse_server.LoadQuery(instream)
      val tHash=decodedQuery.hashCode()

      println("++THASH: "+tHash)
      for(i<-0 until mrse_server.indexParts()){
        val t0 = System.nanoTime()
        val res=mrse_server.SimilaritySearch(decodedQuery,i,tHash)
        val t1 = System.nanoTime()
        println("real calc: "+(t1 - t0)/1000000)

        res.Save(outstream)
      }
      instream.close()
      outstream.close()
      s.close()
      println("++DONE")
      println()
    }
  }
}




object Test{
  def main(args: Array[String]): Unit = {
    val settings=new Settings
    settings.SlotCount=2048
    settings.plainMod=12289
    settings.Security=128
    settings.docnum=512
    settings.setname="enron_512"
    settings.dic=715
    settings.Security=128
    val client=new MRSE_FHE_SkNN_User(settings)
    val server=new FSDS_Server(settings)

    client.KeyGen()
    client.LoadClientKeys()
    client.BuildIndex()

    val ss=new MRSE_SocketServer(settings,server)
    ss.start()
    val sc=new SDS_SocketClient(settings,client)
    Thread.sleep(2000)
    sc.Search(2)


  }
}
