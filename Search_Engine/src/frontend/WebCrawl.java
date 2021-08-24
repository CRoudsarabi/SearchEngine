package frontend;

import backend.*;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class WebCrawl {
	//example:
	//mvn exec:java -Dexec.mainClass=WebCrawl -Dexec.args="postgres postgres letmein 5 10"
	
	public static void main(String[] args)  {
		
	    String bbc = "https://www.bbc.com/news/world-us-canada-50395015";
	    String guardian = "https://www.theguardian.com/world/2019/nov/09/freed-brazilian-ex-president-lula-speaks-to-jubilant-supporters";
	    String zeit = "https://www.zeit.de/politik/ausland/2019-11/recep-tayyip-erdogan-donald-trump-washington-gespraeche";
	    String nyt = "https://www.nytimes.com/2019/11/13/world/asia/hong-kong-protests-students.html";

		Set<String> startingURLs = new HashSet<String>();
		startingURLs.add(guardian);
		startingURLs.add(bbc);
		startingURLs.add(nyt);
		
		if(args.length == 7) {
			String host = args[0];
			String port = args[1];
			String database = args[2];
			String username = args[3];
			String password = args[4];

			Credentials cred= new Credentials(host,port,database,username,password);
			
			int parallelism = Integer.parseInt(args[5]);
			int maximum_docs = Integer.parseInt(args[6]);
			int docs_per_crawler= maximum_docs/parallelism;
			Thread threads[] = new Thread[parallelism];
		
			DBconnection myCon=new DBconnection();
			try {

				myCon.initialize(cred);
				myCon.createTables();
				myCon.createViewCterms();
				myCon.createscoreviews();
				for (int i = 0; i < parallelism; i++) {
					Crawler crawler = new Crawler(docs_per_crawler, true, false,cred);
					if(i==0) crawler.addToQueue(startingURLs, myCon, -1);
					Thread t1 = new Thread(crawler);
					t1.start();
					threads[i]=t1;
					Thread.sleep(500);
				}
				for(Thread t: threads) {
					t.join();
				}
	            System.out.println("Calculating tf..");
	            myCon.updateTF();
	            System.out.println("Calculating idf..");
	            myCon.updateIDF();
	            System.out.println("Calculating score...");
	            myCon.updateScore();
	            System.out.println("count of terms in document");        
	            myCon.updatecountterms();	 
	            myCon.refreshViewCterms();
	            System.out.println("Calculating new tf...");
	            myCon.updatenewIDF();	            
	            System.out.println("Calculating new idf...");
	            myCon.updatenewTF();	            
	            System.out.println("Calculating new score...");
	            myCon.updatenewScore();
	            System.out.println("Calculating pagerank...");
				PageRank pr =new PageRank(myCon);
				pr.calculatePageRank(false);
	            System.out.println("refresh the views");
	            myCon.refreshscoreviews();
	            
			} catch (SQLException e) {
				System.out.println("Could not connect to database :" + e.getMessage());
			} catch (InterruptedException e) {

			} catch (Exception e) {
			
			}

		}
		
	}

}
