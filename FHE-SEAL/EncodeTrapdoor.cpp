

#include <iostream>
#include <string>

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


	for(int i=0;i<num;i++){
		int x;
		cin >> x;
		Plaintext plain;
		plain=x;
		plain.save(cout);	
	}
	return 0;
}

