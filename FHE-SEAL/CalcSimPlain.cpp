

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


int main(int argc, char* argv[])
{
	char *pt11;//dummy
	int num=1;

	if(argc>1)
	num= strtol(argv[1], &pt11, 10);//file_num

	

	EncryptionParameters parms=EncryptionParameters::Load(cin);
	auto context = SEALContext::Create(parms);

	SecretKey secret_key;

	Evaluator evaluator(context);
	IntegerEncoder encoder(context);

	

	vector<Plaintext> trapdoor_encoded(num);
	for(int i=0;i<num;i++)trapdoor_encoded[i].unsafe_load(cin);

	Ciphertext result;
	bool assigned=false;
	for(int i=0;i<num;i++){
		Ciphertext subindex;
		subindex.unsafe_load(cin);
		evaluator.multiply_plain(subindex,trapdoor_encoded[i],subindex);
		if(!assigned)result=subindex;
		else evaluator.add(result,subindex,result);
		assigned=true;
	}

		
	result.save(cout);
	return 0;
}

