package main

class MRSE_FHE_SkNN_SparkUser(settings: Settings) extends MRSE_FHE_SkNN_User(settings) {
  override def MethodName(): String = super.MethodName()+"_SPARK_"+settings.chunkNum
}
