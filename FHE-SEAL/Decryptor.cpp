

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
#include <sstream>



#include "seal/seal.h"
#include "Commons.cpp"

using namespace std;
using namespace seal;





int main(int argc, char* argv[])
{
	
	EncryptionParameters parms=EncryptionParameters::Load(cin);
	auto context = SEALContext::Create(parms);
	SecretKey sec_key;
	sec_key.unsafe_load(cin);
	Decryptor decryptor(context, sec_key); 
	Ciphertext ct;
	ct.unsafe_load(cin);
	Plaintext pt;
	decryptor.decrypt(ct,pt);
	scheme_type scheme=parms.scheme();
	if(scheme==scheme_type::CKKS){	
		CKKSEncoder encoder(context);
		DecodeAndPrintSparse(pt,encoder);
	}
	else{
		BatchEncoder encoder(context);
		DecodeAndPrintSparse(pt,encoder);
	}
	return 0;
}

