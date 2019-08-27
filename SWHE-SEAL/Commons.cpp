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

#include "seal/seal.h"

using namespace std;
using namespace seal;

/*
User is responsible for padding with zeros
*/
vector<uint64_t> ReadQUERY(int slot_count){

	vector<uint64_t> returner;
	for(int i=0;i<slot_count;i++){
		int d;
		cin >> d;
		returner.push_back(d);
	}
	return returner;
}

void DecryptAndPrint(const Ciphertext & ct,Decryptor & decryptor,BatchEncoder & encoder){

	Plaintext plain;
	vector<uint64_t> res;		
	decryptor.decrypt(ct, plain);
	encoder.decode(plain, res);
	for(int i=0;i<res.size()-1;i++)
		cout << res[i]<<",";
	cout << res[res.size()-1]<<endl;
}



void DecodeAndPrint(const Plaintext & pt,BatchEncoder & encoder){

	vector<uint64_t> res;		
	encoder.decode(pt, res);
	for(int i=0;i<res.size()-1;i++)
		cout << res[i]<<",";
	cout << res[res.size()-1]<<endl;
}

void DecodeAndPrintSparse(const Plaintext & pt,BatchEncoder & encoder){

	vector<uint64_t> res;		
	encoder.decode(pt, res);
	for(int i=0;i<res.size();i++)if(res[i]!=0)
		cout << i<<":"<<res[i]<<endl;
}


void DecryptAndPrintSparse(const Ciphertext & ct,Decryptor & decryptor,BatchEncoder & encoder){

	Plaintext plain;
	vector<uint64_t> res;		
	decryptor.decrypt(ct, plain);
	DecodeAndPrintSparse(plain,encoder);
}


/*

CKKS below

*/

void DecryptAndPrint(const Ciphertext & ct,Decryptor & decryptor,CKKSEncoder & encoder){

	Plaintext plain;
	vector<double> res;		
	decryptor.decrypt(ct, plain);
	encoder.decode(plain, res);
	for(int i=0;i<res.size()-1;i++)
		cout << res[i]<<",";
	cout << res[res.size()-1]<<endl;
}



void DecodeAndPrint(const Plaintext & pt,CKKSEncoder & encoder){

	vector<double> res;		
	encoder.decode(pt, res);
	for(int i=0;i<res.size()-1;i++)
		cout << res[i]<<",";
	cout << res[res.size()-1]<<endl;
}

void DecodeAndPrintSparse(const Plaintext & pt,CKKSEncoder & encoder){

	vector<double> res;		
	encoder.decode(pt, res);
	for(int i=0;i<res.size();i++)if(res[i]>0.01 || res[i]<-0.01)
		cout << i<<":"<<res[i]<<endl;
	
}


void DecryptAndPrintSparse(const Ciphertext & ct,Decryptor & decryptor,CKKSEncoder & encoder){

	Plaintext plain;
	vector<double> res;		
	decryptor.decrypt(ct, plain);
	DecodeAndPrintSparse(plain,encoder);
}


/*
returns the size of the cipher text in terms of KB
*/
double Size(const Ciphertext & ct){

	stringstream is;
	ct.save(is);
	string temp=is.str();
	return temp.size()/1024;
}

double Size(const Plaintext & pt){

	stringstream is;
	pt.save(is);
	string temp=is.str();
	return temp.size()/1024;
}

Plaintext Mask(CKKSEncoder & encoder,int scale,int slot_count,int ID,double mask_div)
{
	vector<double> init(slot_count,0);
	init[ID]=1.0;
	Plaintext plain;
	double scaler = pow(2.0,scale/mask_div);
	encoder.encode(init, scaler, plain);
	return plain;
}


vector<Plaintext> CreateMasks(CKKSEncoder & encoder,int scale,int slot_count,int vert_file,double mask_div)
{
	vector<Plaintext> init;
	for(int i=0;i<vert_file;i++){
		Plaintext temp=Mask(encoder,scale,slot_count,i,mask_div);
		init.push_back(temp);
	}
	return init;
}



Plaintext Mask(BatchEncoder & encoder,int slot_count,int ID)
{
	vector<uint64_t> init(slot_count,0);
	init[ID]=1;
	Plaintext plain;
	encoder.encode(init, plain);
	return plain;
}


vector<Plaintext> CreateMasks(BatchEncoder & encoder,int slot_count,int vert_file)
{
	vector<Plaintext> init;
	for(int i=0;i<vert_file;i++){
		Plaintext temp=Mask(encoder,slot_count,i);
		init.push_back(temp);
	}
	return init;
}


