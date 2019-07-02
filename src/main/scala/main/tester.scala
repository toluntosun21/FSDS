
package main

object tester {
  def main(args: Array[String]): Unit = {



    val settings= new Settings
    settings.docnum=512
    settings.dic=709
    settings.setname="enron_512"
    settings.variance=0.1
    val server=new MRSE_TF2_Server(settings)
    val client=new MRSE_TF2_User(settings)
    Util.Tester(server,client,settings,1,10)
return/*
      val trapdoor=client.GenerateTrapdoor(Array((0,1.0),(230,1.0)))
      println(trapdoor.toString)

      val trapdoor2=server.DecodeTrapdoor(trapdoor.toString)
      val num1=trapdoor.get().asInstanceOf[ArrayRealVector].getEntry(10)
      val num2=trapdoor2.get().asInstanceOf[ArrayRealVector].getEntry(10)
      println(num1)
      println(num2)

      val res=server.SimilaritySearch(trapdoor)
      val encoded=res.toString
      println(encoded)
    val num3=res.get().asInstanceOf[Array[(Int,Double)]](3)._2
    val num4=client.DecodeResult(encoded).get().asInstanceOf[Array[(Int,Double)]](3)._2
    println(num3)
    println(num4)


          val settings=new Settings
          settings.docnum=4096
          settings.SlotCount=8192
          settings.plainMod=65537
          settings.Security=128
          settings.Scale=100
          settings.dic=4//580
          val client=new MRSE_FHE_User(settings)
          client.KeyGen()
          println("key generated")
          val server=new MRSE_FHE_Server(settings)
          client.StartClient()
          client.BuildIndex()
          println("index constructed")
          server.StartServer()
          println("started")
          val trapdoor=client.GenerateTrapdoor(Array((0,1.0)))
          println("trapdoor generated")
          val res=server.SimilaritySearch(trapdoor)
          println("starting dec")
          client.Decrypt(res).foreach(u=>println(u._1+": "+u._2))


          val settings=new Settings
          settings.SlotCount=2048
          settings.plainMod=12289
          settings.Scale=100
          settings.docnum=512
          settings.setname="enron_512"
          settings.dic=715
          settings.Security=128
          val client=new MRSE_FHE_SkNN_User(settings)
          val server=new MRSE_FHE_SkNN_Server(settings)

     //     Util.Tester(server,client,settings,1,1)
     //     return

                client.KeyGen()
                client.LoadClientKeys()
                client.BuildIndex()
                val trap=client.GenerateTrapdoor(Array((0,1.0)))
                server.StartServer()
                val res=server.SimilaritySearch(trap)
                println("Result Size: "+res.Size())
                val plain_res=client.Decrypt(res)
                plain_res.foreach(u=>println(u._1+": "+u._2))
*/
  }

}
