package main

class MRSE_FHE_SkNN_P_User(settings: Settings) extends MRSE_FHE_SkNN_User(settings) {
  override def MethodName(): String = super.MethodName()+"_PARALLEL_"+settings.chunkNum
}
