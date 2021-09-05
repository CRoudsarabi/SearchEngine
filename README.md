## Authors
Ibraheem Barakat 
Constantin Roudsarabi

## Your Project Name Here
Search_Engine

## Build instructions

In folder Search_Engine

mvn compile

mvn clean install

Before you run the crawler you need to reset the datatbase since we modfied the table 

mvn exec:java -Dexec.mainClass=frontend.ResetDatabase -Dexec.args="host port databasename username password"  

You can run the crawler:  
mvn exec:java -Dexec.mainClass=frontend.WebCrawl -Dexec.args="host port databasename username password parallelism number_of_documents_to_crawl"


To run a Query:  
mvn exec:java -Dexec.mainClass=frontend.QueryDB -Dexec.args="host port databasename username password" (you will be prompted for additional parameters like search terms etc.)  

You can run additional methods in the class Tests if you want to run only specific componnents (e.g. indexer)  

To create the shingles table:  
mvn exec:java -Dexec.mainClass=frontend.CreateShingles -Dexec.args="host port databasename username password k minhash limit"  

## Usage instructions
Website:  
http://131.246.117.37:8080/Search_Engine

Task 4:  
MultiSearch:  
Calculations in backend.MultiSearchQuery and web.RunMultiQuery  

Ads:  
Calculations in backend.Adquery and web.AdRedirect  

Task 3: 
Imagesearch:  
Calculation in backend.Indexer   
getLinks()
  
Snippet:  
Calculation in backend.Snippet  
  
Query Expansion:  
Calculation in backend.Synonyms  
  
Shingling and Near-Duplicate Detection:  
Calculation in backend.Shingling  
to see the table in the DB run frontend.CreateShingles  


Task 2:  
Pagerank:  
Calculation in backend.Pagerank  
Runs automatically after running frontend.WebCrawl  

BM25:  
Calculation in backend.DBconnection  
Runs automatically after running frontend.WebCrawl  

Language:  
Part of backend.Indexer  

Spellchecking:  
Method for calcualtion in backend.Query  
uses backend.Levenshtein  
method is called in web.RunQuery  

Task 1:  
Indexer and Crawler are in backend.Indexer and backend.Crawler  
TFIDF calculation done in backend.DBconnection  

