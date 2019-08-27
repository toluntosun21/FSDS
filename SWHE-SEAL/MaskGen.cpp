

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
	char *pt11;//dummy
	int mask_len;

	if(argc>1)
	mask_len=strtol(argv[1], &pt11, 10);

	
	EncryptionParameters parms=EncryptionParameters::Load(cin);

	auto context = SEALContext::Create(parms);
	

	BatchEncoder encoder(context);
	size_t slot_count = encoder.slot_count();
	vector<Plaintext> masks=CreateMasks(encoder,slot_count, mask_len);
	for(int i=0;i<masks.size();i++)masks[i].save(cout);

	return 0;
}

