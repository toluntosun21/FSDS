package FSDS

import java.io.{File, FileInputStream, InputStream}


class FSDS_0_Server(settings: Settings) extends SDS_Server(settings) {
  var instance=new SWHE(settings)
  instance.method=MethodName()

  override def MethodName(): String = "MRSE_FHE"

  var masks:Array[Byte]=null
  var bytesIndex:Array[Byte]=null

  override def SimilaritySearch(query: Query): Result = {
    val num=if(settings.dic<settings.SlotCount)settings.dic else settings.SlotCount

    /*
    for simulation purposes, calculate numParts-1 more
     */
    val numParts=settings.docnum/settings.SlotCount

    for(i<-1 until numParts)
      instance.CalcSim(num,bytesIndex,query.get().asInstanceOf[Array[Byte]],masks)


    val res=instance.CalcSim(num,bytesIndex,query.get().asInstanceOf[Array[Byte]],masks)
    new MRSE_FHE_Result(res)
  }

  override def StartServer(): Unit = {
    val indexData=IndexFiles()(0)
    val inputStream=new FileInputStream(indexData)
    bytesIndex=Util.Functions.CollectAllInput(inputStream)
    val inputStreamParam=new FileInputStream(new File(settings.Keydir(MethodName())+"param"))
    instance.param=Util.Functions.CollectAllInput(inputStreamParam)
    val inputStreamGal=new FileInputStream(new File(settings.Keydir(MethodName())+"galois"))
    instance.galoisKeys=Util.Functions.CollectAllInput(inputStreamGal)
    masks=instance.MaskGen()
  }

  override def IndexSize(): Int = {
    val inputStreamGal=new FileInputStream(new File(settings.Keydir(MethodName())+"galois"))
    val data=Util.Functions.CollectAllInput(inputStreamGal)
    super.IndexSize()+data.length/(1024*1024)
  }

  /*
  non-implemented method
   */
  override def DecodeQuery(s: Array[Byte]): Query = null

}
