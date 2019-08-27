package FSDS

class Settings extends Serializable {
  var K=10
  var th_count=1
  var Security=256
  def lambda():Int= Security match{
      case 128=>15
      case 256=>24
      case 192=>20
      case _=>16
  }
  var threshold=0.4
  var plainMod=12289//40961;65537
  var Scale=200
  var SlotCount=2048
  var dic=3430
  var docnum=2048
  var fac=1
  var divisor=1
  var setname="enron_2048"
  var default_par=(-1 )
  var keydir="./key"
  var datadir="./data"
  var exedir="./exe"
  var variance=0.01
  var TestCount=10
  var server="localhost"
  var port=8080
  var chunkNum=1
  var IP="10.36.97.5"

  def PlainData():String=if(datadir!="")datadir+"/tf_idf_data_"+setname else "tf_idf_data_"+setname
  def PlainQuerySet():String=if(datadir!="")datadir+"/query_tf_idf_data_"+setname else "query_tf_idf_data_"+setname

  def Index():String="_"+setname+"_"+dic+"_"+chunkNum+"_INDEX_"
  def IndexPath(method: String,i:Int):String=datadir+"/"+method+Index()+i
  def Keydir(method:String):String=keydir+"/"+method+"_"+setname+"_"+dic+"_"

  def S():String="S"
  def M1():String="M1"
  def M2():String="M2"
  def M1inv():String="M1inv"
  def M2inv():String="M2inv"


  def SWHEDecryptor=exedir+"/SWHEDecryptor"
  def SWHEKeygen=exedir+"/SWHEKeyGen"
  def SWHEBatchAdder=exedir+"/SWHEBatchAdder"
  def SWHEAdder=exedir+"/SWHEAdder"
  def SWHEIndexgen=exedir+"/SWHEIndexGen"
  def SWHEQueryGen=exedir+"/SWHEQueryGen"
  def SWHEMaskgen=exedir+"/SWHEMaskGen"
  def SWHECalcSim=exedir+"/SWHECalcSim"
  def SWHECalcSimPlain=exedir+"/SWHECalcSimPlain"





}

