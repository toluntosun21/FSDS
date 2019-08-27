

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

	EncryptionParameters parms=EncryptionParameters::Load(cin);
	auto context = SEALContext::Create(parms);

	SecretKey secret_key;

	Evaluator evaluator(context);
	Ciphertext ct1,ct2,res;
	ct1.unsafe_load(cin);
	ct2.unsafe_load(cin);
	evaluator.add(ct1,ct2,res);
		
	res.save(cout);
	return 0;
}

