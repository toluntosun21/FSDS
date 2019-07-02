from gensim import corpora, models, similarities
import gensim
from nltk.stem import WordNetLemmatizer, SnowballStemmer
from nltk.stem.porter import *
import numpy as np
np.random.seed(2018)
from collections import defaultdict
import sys

import nltk
nltk.download('wordnet')
stemmer = SnowballStemmer("english")


def lemmatize_stemming(text):
    return stemmer.stem(WordNetLemmatizer().lemmatize(text, pos='v'))

def preprocess(token):
	if token not in gensim.parsing.preprocessing.STOPWORDS and len(token) > 2:
		return (lemmatize_stemming(token))
	else: return " "

def group(current_list):
	docMap={}
	
	for d,w,c in current_list:
		if (str(d)+"_"+str(w)) in docMap: 
			docMap[str(d)+"_"+str(w)] = docMap[str(d)+"_"+str(w)] + c
		else:		
			docMap[str(d)+"_"+str(w)] = c
	returner = list(map(lambda x:(int(x.split("_")[0]),str(x.split("_")[1]),docMap[x]),docMap))
	return returner

with open("vocab."+str(sys.argv[1])+".txt") as f:
    dic = f.readlines()
dic = [x.strip() for x in dic] 

with open("docword."+str(sys.argv[1])+".txt") as f:
    content = f.readlines()

content = [x.strip() for x in content] 

content=list(map(lambda x: x.split(" "),content))
#counting starts from 0
content=list(map(lambda x: (int(x[0])-1,int(x[1])-1,float(x[2])),content))

#take the needed portion for data set
datacontent=list(filter(lambda x: x[0]<int(sys.argv[2]),content))
#take the needed portion for querying
querycontent=list(filter(lambda x: x[0]>int(sys.argv[2]) and x[0]<int(sys.argv[3])+int(sys.argv[2]),content))

#map to words
datacontent=list(map(lambda x: (int(x[0]),dic[x[1]],float(x[2])),datacontent))
querycontent=list(map(lambda x: (int(x[0]),dic[x[1]],float(x[2])),querycontent))

#stemming; filtering stop words
datacontent=list(map(lambda x: (int(x[0]),preprocess(x[1]),float(x[2])),datacontent))
datacontent=list(filter(lambda x: x[1]!=" ",datacontent))
querycontent=list(map(lambda x: (int(x[0]),preprocess(x[1]),float(x[2])),querycontent))
querycontent=list(filter(lambda x: x[1]!=" ",querycontent))

#group the same IDs
datacontent=group(datacontent)
querycontent=group(querycontent)

print("PRE PROCESS DONE")

dicmap={}
counter=0
for v,k,t in datacontent:
	if( k not in dicmap):
		dicmap[k]=counter
		counter+=1

#map data set
datacontent=list(map(lambda x:(x[0],dicmap[x[1]],x[2]),datacontent))
#filter query set
#querycontent=list(map(lambda x:(x[0],dicmap[x[1]],x[2]),querycontent))
querycontent=list(filter(lambda x:x[1] in dicmap,querycontent))
querycontent=list(map(lambda x:(x[0],dicmap[x[1]],x[2]),querycontent))

print("FILTERING DONE")

corpus = defaultdict(list)
for v, k, t in datacontent: 
	corpus[v].append((k,t))
corpus=list(map(lambda x: corpus[x],corpus))



dictionary=gensim.corpora.Dictionary.from_corpus(corpus)
dictionary.filter_extremes(no_below=10,no_above=0.3, keep_tokens=None)

print ("Num Docs: ",dictionary.num_docs)
print ("Num Vocab: ",len(dictionary.keys()))


keys={}
for t in dictionary.keys():
	keys[t]=True

#filter lines that is not in dictionary
datacontent=list(filter(lambda x: x[1] in keys,datacontent))
querycontent=list(filter(lambda x: x[1] in keys,querycontent))


#create filtered corpus
corpusfiltered = defaultdict(list)
for v, k, t in datacontent: 
	corpusfiltered[v].append((k,t))
corpusfiltered=list(map(lambda x: corpusfiltered[x],corpusfiltered))



#create filtered query set
querycorpus = defaultdict(list)
for v, k, t in querycontent: 
	querycorpus[v].append((k,t))
querycorpus=list(map(lambda x: querycorpus[x],querycorpus))


tfidf = models.TfidfModel(corpusfiltered)
corpus_tfidf = tfidf[corpusfiltered]
doc_num=len(corpus_tfidf)
print ("TF IDF DATA CREATED: ",doc_num)


with open('tf_idf_data_'+str(sys.argv[1])+'_'+str(sys.argv[2]), 'w') as the_file:
	for i in range(doc_num):
		for j in range(0,len(corpus_tfidf[i])):
			the_file.write(str(corpus_tfidf[i][j][0]))
			the_file.write(':')		
			the_file.write(str(corpus_tfidf[i][j][1]))
			if(j!=len(corpus_tfidf[i])-1):
				the_file.write(' ')		
		the_file.write('\n')
	the_file.close()

print ("TF IDF DATA WRITTEN; STARTING TO WRITE SAMPLE QUERIES")

with open('query_tf_idf_data_'+str(sys.argv[1])+'_'+str(sys.argv[2]), 'w') as the_file:
	for i in range(0,len(querycorpus)):
		tf_idf_data=tfidf[querycorpus[i]]
		for j in range(0,len(tf_idf_data)):
			the_file.write(str(tf_idf_data[j][0]))
			the_file.write(':')		
			the_file.write(str(tf_idf_data[j][1]))
			if(j!=len(tf_idf_data)-1):
				the_file.write(' ')		
		the_file.write('\n')	
	the_file.close()
