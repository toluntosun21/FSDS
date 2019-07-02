// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

#include <iostream>
#include <vector>
#include <string>
#include <fstream>
#include <sstream>

#include "seal/seal.h"
#include "Commons.cpp"



using namespace std;
using namespace seal;



vector<Plaintext> EncodeQUERY(IntegerEncoder & encoder,const vector<uint64_t> & init)
{
	vector<Plaintext> returner;
	for(int i=0;i<init.size();i++){
		
		Plaintext plain;
		plain=init[i];
		returner.push_back(plain);	
	}

	return returner;
}





int main(int argc, char* argv[])
{
	char *pt11;//dummy
	int slot_count_in=4096;
	int security_in=128;
	int plain_mod=40961;
	int scheme=0;
	string keydir="./";
	if(argc>1)
	scheme=strtol(argv[1], &pt11, 10);
	if(argc>2)
	slot_count_in= strtol(argv[2], &pt11, 10);
	if(argc>3)
	security_in= strtol(argv[3], &pt11, 10);
	if(argc>4)
	plain_mod= strtol(argv[4], &pt11, 10);
	if(argc>5)
	keydir= argv[5];;
	
	scheme_type scheme_=scheme_type::BFV;
		if(scheme==0)
		scheme_=(scheme_type::BFV);
		else 
		scheme_= (scheme_type::CKKS);

	EncryptionParameters parms(scheme_);


	if(scheme==0)
    	parms.set_plain_modulus(plain_mod);
	
	int coeff_count=slot_count_in;
	if(scheme==1)coeff_count*=2;
	parms.set_poly_modulus_degree(coeff_count);

	if(security_in==128)
		parms.set_coeff_modulus(DefaultParams::coeff_modulus_128(coeff_count));
	else if(security_in==256)
		parms.set_coeff_modulus(DefaultParams::coeff_modulus_256(coeff_count));
	else
		parms.set_coeff_modulus(DefaultParams::coeff_modulus_192(coeff_count));

	auto context = SEALContext::Create(parms);
	ofstream param(keydir+"param");
	ofstream pub(keydir+"pubkey");
	ofstream priv(keydir+"privatekey");
	ofstream relin(keydir+"relin");
	ofstream galois(keydir+"galois");	
	stringstream iss;
	
	EncryptionParameters::Save(parms, param);
	KeyGenerator keygen(context);
	auto public_key = keygen.public_key();
	public_key.save(pub);
	auto secret_key = keygen.secret_key();
	secret_key.save(priv);
	auto relin_keys = keygen.relin_keys(DefaultParams::dbc_max());
	relin_keys.save(relin);
	GaloisKeys gal_keys15 = keygen.galois_keys(60);
	gal_keys15.save(galois);

	param.close();
	pub.close();
	priv.close();
	relin.close();
	galois.close();
	return 0;
}

