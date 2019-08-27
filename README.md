# MRSE_FHE_SkNN

For secret/public keys, a folder named "key" should be created. The program automatically generates and stores the keys under the folder and read from the same spot. 
All the SEAL/c++ sources under SWHE must be compiled and stored under a folder named "/exe". The program looks under the folder /exe, with specific namings for each operation such as key generation, index genereation, query generation, search.. The namings can be found under the below example
tf-idf data and secure indexes must be stored under a folder named "/data". Moreover, they should be marked with the document number as in the example. 

An example of the directory which the target jar of the project is executed is:
  -FSDS.jar
  /exe:
   -SWHEDecryptor
   -SWHEKeyGen
   -SWHEBatchAdder
   -SWHEAdder
   -SWHEIndexGen
   -SWHEQueryGen
   -SWHEMaskGen
   -SWHECalcSim
   -SWHECalcSimPlain
  /key:
   -FSDS_enron_8192_2010_pubkey
   -FSDS_enron_8192_2010_privatekey
   -FSDS_enron_8192_2010_param
   -FSDS_enron_8192_2010_M
   -FSDS_enron_8192_2010_Minv
  /data:
   -tf_idf_data_enron_8192
   -query_tf_idf_data_enron_8192
   -FSDS_enron_8192_2010_1_INDEX_0_0_
