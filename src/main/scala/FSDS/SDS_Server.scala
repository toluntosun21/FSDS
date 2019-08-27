package FSDS

import java.io.{File, InputStream}

import Util.Functions





abstract class  SDS_Server(settings: Settings) {

  def indexParts():Int=1

  def MethodName():String

  protected def IndexFiles():Array[File]={
    val dir=new File(settings.datadir)
    println("searching for: "+MethodName()+settings.Index())
    dir.listFiles().filter(u=>u.getName.contains(MethodName()+settings.Index()))
  }

  def deleteIndexFiles():Unit={
    IndexFiles().foreach(u=>u.delete())
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
  def SimilaritySearch(query:Query):Result

  /*
returns SEAL cipher texts as array
*/
  def SimilaritySearch(query:Query, index_part:Int, searchID:Int):Result=SimilaritySearch(query)

  def PIR(query:Array[String]):Array[String]={
    null
  }

  def DecodeQuery(s:Array[Byte]):Query

  def LoadQuery(str:InputStream):Query={
    val temp=Functions.CollectInput(str)
    DecodeQuery(temp)
  }
}
