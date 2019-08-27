

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


vector<int> ReadQuery(int num){
	vector<int> returner(num);

	for(int i=0;i<num;i++){
		int x;
        	cin.read(reinterpret_cast<char*>(&x), sizeof(int));
		returner[i]=x;
	}
	return returner;
}

int main(int argc, char* argv[])
{
	char *pt11;//dummy
	int num=1;

	if(argc>1)
	num= strtol(argv[1], &pt11, 10);//file_num

	

	EncryptionParameters parms=EncryptionParameters::Load(cin);
	auto context = SEALContext::Create(parms);


	Evaluator evaluator(context);

	

	vector<int> Query=ReadQuery(num);	

	Ciphertext result;
	bool assigned=false;
	for(int i=0;i<num;i++){
		Plaintext pt;
		pt=Query[i];
		Ciphertext subindex;
		subindex.unsafe_load(cin);
		evaluator.multiply_plain(subindex,pt,subindex);
		if(!assigned)result=subindex;
		else evaluator.add(result,subindex,result);
		assigned=true;
	}
	
	
	result.save(cout);
	return 0;
}

