

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
	RelinKeys relin_keys;

	Evaluator evaluator(context);
	GaloisKeys gal_keys15;
	gal_keys15.unsafe_load(cin);	

	BatchEncoder encoder(context);
	size_t slot_count = encoder.slot_count();

	/*
	INITIAL STEPS
	*/

	vector<Plaintext> masks(num);
	for(int i=0;i<num;i++)masks[i].unsafe_load(cin);
		


	Ciphertext Query;
	Query.unsafe_load(cin);

	Ciphertext result;
	int steps=log2(slot_count)-1;
	for(int i=0;i<num;i++){

		Ciphertext subindex;
		subindex.unsafe_load(cin);
		Ciphertext mask;

		evaluator.multiply_plain(Query,masks[i],mask);
		Ciphertext temp;
		evaluator.rotate_columns(mask, gal_keys15,temp);
		evaluator.add(temp,mask,mask);
		for(int j=0;j<steps;j++){	
			int amount=pow(2,j);
			Ciphertext temp;
			evaluator.rotate_rows(mask, amount, gal_keys15, temp);
			evaluator.add(mask,temp,mask);		
		}

		evaluator.multiply(subindex,mask,subindex);	

		if(i==0)result=subindex;
		else evaluator.add(result,subindex,result);

	}
/*	for(int i=0;i<num;i++){
		evaluator.multiply(Index[i],masks_processed[i],Index[i]);
//		evaluator.relinearize_inplace(Index[i],relin_keys);
	}


	Ciphertext result;
	for(int i=0;i<num;i++){
		if(i==0)result=Index[0];
		else evaluator.add(result,Index[i],result);
	}
*/		
	result.save(cout);
	return 0;
}

