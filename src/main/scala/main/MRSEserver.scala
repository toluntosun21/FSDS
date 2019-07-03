package main

import java.io.File





abstract class  MRSEserver(settings: Settings) {

  def indexParts():Int=1

  def MethodName():String

  protected def IndexFiles():Array[File]={
    val dir=new File(settings.datadir)
    println("searching for: "+MethodName()+settings.Index())
    dir.listFiles().filter(u=>u.getName.contains(MethodName()+settings.Index()))
  }


/*
can be implemented here
returns in MB
 */
  def IndexSize():Int={
    return IndexFiles().map(u=>u.length().toInt/(1024*1024)).reduce((a,b)=>a+b)
  }

/*
makes the server ready to answer
 */
  def StartServer()


  /*
returns SEAL cipher texts as array
 */
  def SimilaritySearch(trapdoor:Trapdoor):Result

  /*
returns SEAL cipher texts as array
*/
  def SimilaritySearch(trapdoor:Trapdoor,index_part:Int,searchID:Int):Result=SimilaritySearch(trapdoor)

  def PIR(query:Array[String]):Array[String]={
    null
  }

  def DecodeTrapdoor(s:Array[Byte]):Trapdoor

}
