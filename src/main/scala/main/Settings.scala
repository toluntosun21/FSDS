package main

class Settings extends Serializable {
  var K=10
  var th_count=1
  var Security=256
  def lambda():Int=Security match{
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

  def Index():String="_"+setname+"_INDEX_"
  def IndexPath(method: String,i:Int):String=datadir+"/"+method+"_"+setname+"_INDEX_"+i
  def Keydir(method:String):String=keydir+"/"+method+"_"+setname+"_"

  def S():String="S"
  def M1():String="M1"
  def M2():String="M2"
  def M1inv():String="M1inv"
  def M2inv():String="M2inv"


  def FHEDecryptor=exedir+"/FHEDecryptor"
  def FHEKeygen=exedir+"/FHEKeyGen"
  def FHEBatchAdder=exedir+"/FHEAdder"
  def FHEAdder=exedir+"/FHEAdder"
  def FHEIndexgen=exedir+"/FHEIndexGen"
  def FHECKKSTrapgen=exedir+"/FHETrapdoorGen"
  def FHEMaskgen=exedir+"/FHEMaskGen"
  def FHECalcSim=exedir+"/FHECalcSim"
  def FHECalcSimPlain=exedir+"/FHECalcSimPlain"



  def FHEsecureKNNexe=exedir+"/LocalTimingTestFHEsecureKnn"
  def FHEsecureKNNexeInt=exedir+"/LocalTimingTestFHEsecureKnnInt"
  def FHEsecureKNNexeTH=exedir+"/LocalTimingTestFHEsecureKnnTH"
  def FHEsecureKNN2exe=exedir+"/LocalTimingTestFHEsecureKnn2"
  def FHEexe=exedir+"/LocalTimingTestFHE"
  def SecureKnnExe=exedir+"/LocalTimingTestFHE"


  /*
  256-> 234
  512-> 663
  2048->2408
  4096-> 3580
   */
}

