

#include <iostream>
#include <iomanip>
#include <vector>
#include <string>
#include <chrono>
#include <random>
#include <thread>
#include <mutex>
#include <memory>
#include <limits>
#include <fstream>


#include "seal/seal.h"
#include "Commons.cpp"

using namespace std;
using namespace seal;



void ReadDB(scheme_type scheme, CKKSEncoder * encoder,BatchEncoder * encoder2,Encryptor & enc,int scale, int vert_file, int slot_count,ofstream & of){

	for(int i=0;i<vert_file;i++){
		Plaintext plain;
		if(scheme==scheme_type::CKKS){	
			vector<double> input;

			for(int j=0;j<slot_count;j++){
				double d;
				cin >> d;
				input.push_back(d);
			}

	
			double scaler = pow(2.0,scale);
			encoder->encode(input, scaler, plain);
		}
		else{
			vector<uint64_t> input;

			for(int j=0;j<slot_count;j++){
				int d;
				cin >> d;
				input.push_back(d);
			}

			encoder2->encode(input, plain);
		}
	Ciphertext encrypted;
	enc.encrypt(plain, encrypted);
	encrypted.save(of);
	}
}









int main(int argc, char* argv[])
{
	char *pt11;//dummy
	int vert_file=1;
	string data_name="./";

	if(argc>1)
	vert_file= strtol(argv[1], &pt11, 10);
	if(argc>2)
	data_name= argv[2];


	EncryptionParameters parms=EncryptionParameters::Load(cin);
	auto scheme=parms.scheme();
	auto context = SEALContext::Create(parms);

	PublicKey public_key;
	public_key.unsafe_load(cin);

	Encryptor encryptor(context, public_key);
	CKKSEncoder* encoder;
	BatchEncoder* encoder2;

	size_t slot_count;
	if(scheme==scheme_type::CKKS){
		encoder=new CKKSEncoder(context);
		slot_count=encoder->slot_count();
	}
	else {
		encoder2=new BatchEncoder(context);
		slot_count=encoder2->slot_count();
	}



	ofstream of(data_name);
	ReadDB(scheme,encoder,encoder2,encryptor,0.0, vert_file, slot_count,of);
	of.close();	
	return 0;
}

