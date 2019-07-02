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
      settings.threshold=args(5).toDouble
      settings.SlotCount=args(6).toInt
      settings.Security=args(7).toInt	
      settings.plainMod=args(8).toInt
      settings.chunkNum=args(9).toInt
      val server=new MRSE_FHE_SkNN_Server(settings)
      val client=new MRSE_FHE_SkNN_User(settings)
      Util.Tester(server,client,settings,t1,t2)
    }
    else if(method==3){
      settings.threshold=args(5).toDouble
      settings.SlotCount=args(6).toInt
      settings.Security=args(7).toInt
      settings.plainMod=args(8).toInt
      settings.chunkNum=args(9).toInt
      val server=new MRSE_FHE_SkNN_P_Server(settings)
      val client=new MRSE_FHE_SkNN_P_User(settings)
      Util.Tester(server,client,settings,t1,t2)
    }
  }
}
