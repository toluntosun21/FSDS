

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


Ciphertext EncodeAndEncryptQUERY(Encryptor & enc,BatchEncoder & encoder,const vector<uint64_t> & init)
{
	
	Plaintext plain;
	encoder.encode(init, plain);
	Ciphertext returner;
	enc.encrypt(plain,returner);
	return returner;
}



int main(int argc, char* argv[])
{
	
	EncryptionParameters parms=EncryptionParameters::Load(cin);

	auto context = SEALContext::Create(parms);

	PublicKey public_key;
	public_key.unsafe_load(cin);
	

	Encryptor encryptor(context, public_key);
	BatchEncoder encoder(context);
	size_t slot_count = encoder.slot_count();

	vector<uint64_t> Query_0=ReadQUERY(slot_count);
	Ciphertext EncQuery=EncodeAndEncryptQUERY(encryptor,encoder,Query_0);
	EncQuery.save(cout);
	return 0;
}

