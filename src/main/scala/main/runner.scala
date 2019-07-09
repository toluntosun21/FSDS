package main

object runner {
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
      Util.Tester(server,client,settings,t1,t2)
    }else if(method==1){
      settings.SlotCount=args(5).toInt
      settings.plainMod=args(6).toInt
      val server=new MRSE_FHE_Server(settings)
      val client=new MRSE_FHE_User(settings)
      Util.Tester(server,client,settings,t1,t2)
    }else if(method==2){
      settings.SlotCount=args(5).toInt
      settings.Security=args(6).toInt
      settings.plainMod=args(7).toInt
      settings.chunkNum=args(8).toInt
      val server=new MRSE_FHE_SkNN_Server(settings)
      val client=new MRSE_FHE_SkNN_User(settings)
      Util.Tester(server,client,settings,t1,t2)
    }
    else if(method==3){
      settings.SlotCount=args(5).toInt
      settings.Security=args(6).toInt
      settings.plainMod=args(7).toInt
      settings.chunkNum=args(8).toInt
      val server=new MRSE_FHE_SkNN_P_Server(settings)
      val client=new MRSE_FHE_SkNN_P_User(settings)
      Util.Tester(server,client,settings,t1,t2)
    }else if(method==4){
      settings.variance=args(5).toDouble
      settings.chunkNum=args(6).toInt
      val server=new MRSE_TF2_P_Server(settings)
      val client=new MRSE_TF2_User(settings)
      Util.Tester(server,client,settings,t1,t2)
    }else if (method==5){
      settings.variance=args(5).toDouble
      settings.chunkNum=args(6).toInt
      settings.port=args(7).toInt
      val server=new MRSE_TF2_Server(settings)
      val socketserver=new MRSE_SocketServer(settings,server)
      socketserver.start()
    }else if (method==6){
      settings.SlotCount=args(5).toInt
      settings.Security=args(6).toInt
      settings.plainMod=args(7).toInt
      settings.chunkNum=args(8).toInt
      settings.port=args(9).toInt
      val server=new MRSE_FHE_SkNN_P_Server(settings)
      val socketserver=new MRSE_SocketServer(settings,server)
      socketserver.start()
    }else if (method==7){
      settings.SlotCount=args(5).toInt
      settings.Security=args(6).toInt
      settings.plainMod=args(7).toInt
      settings.chunkNum=args(8).toInt
      settings.port=args(9).toInt
      val client=new MRSE_FHE_SkNN_P_User(settings)
      client.LoadClientKeys()
      println("--KEYS LOADED")
      val sc=new MRSE_SocketClient(settings,client)
      sc.Search(t1)
    }
    else if (method==8){
      settings.variance=args(5).toDouble
      settings.port=args(7).toInt
      val client=new MRSE_TF2_User(settings)
      client.LoadClientKeys()
      println("--KEYS LOADED")
      val sc=new MRSE_SocketClient(settings,client)
      sc.Search(t1)
    }
  }
}
