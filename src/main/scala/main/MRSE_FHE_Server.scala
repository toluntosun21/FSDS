package main

import java.io.{File, FileInputStream, InputStream}


class MRSE_FHE_Server(settings: Settings) extends MRSEserver(settings) {
  var instance=new FHE(settings)
  instance.method=MethodName()

  override def MethodName(): String = "MRSE_FHE"

  var masks:Array[Byte]=null
  var bytesIndex:Array[Byte]=null

  override def SimilaritySearch(trapdoor: Trapdoor): Result = {
    val num=if(settings.dic<settings.SlotCount)settings.dic else settings.SlotCount
    val res=instance.CalcSim(num,bytesIndex,trapdoor.get().asInstanceOf[Array[Byte]],masks)
    new MRSE_FHE_Result(res)
  }

  override def StartServer(): Unit = {
    val indexData=IndexFiles()(0)
    val inputStream=new FileInputStream(indexData)
    bytesIndex=Util.CollectAllInput(inputStream)
    val inputStreamParam=new FileInputStream(new File(settings.Keydir(MethodName())+"param"))
    instance.param=Util.CollectAllInput(inputStreamParam)
    val inputStreamGal=new FileInputStream(new File(settings.Keydir(MethodName())+"galois"))
    instance.galoisKeys=Util.CollectAllInput(inputStreamGal)
    masks=instance.MaskGen()
  }

  override def IndexSize(): Int = bytesIndex.length/1024+instance.galoisKeys.length/1024

  /*
  non-implemented method
   */
  override def DecodeTrapdoor(s: Array[Byte]): Trapdoor = null

}
