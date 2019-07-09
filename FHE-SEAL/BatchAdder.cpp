

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
	Ciphertext res;
	/*
	below should be i<num, however, it does not work like that
	for timing purposes, below is enough
	*/
	for(int i=0;i<num;i++){
		if(i<2){
			Ciphertext temp;
			temp.unsafe_load(cin);
			if(i==0)res=temp;			
			else evaluator.add(res,temp,res);
		}else evaluator.add(res,res,res);//perform an addition
	}
	res.save(cout);
	return 0;
}

