package frontend;

import backend.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class Tests {
	public static String bbc = "https://www.bbc.com/news/world-us-canada-50395015";
	public static String guardian = "https://www.theguardian.com/world/2019/nov/09/freed-brazilian-ex-president-lula-speaks-to-jubilant-supporters";
	public static String zeit = "https://www.zeit.de/politik/ausland/2019-11/recep-tayyip-erdogan-donald-trump-washington-gespraeche";
	public static String nyt = "https://www.nytimes.com/2019/11/13/world/asia/hong-kong-protests-students.html";

	private static String database = "postgres";
	private static String username = "postgres";
	private static String password = "postgres";
	private static String host = "localhost";
	private static String port = "5432";

	private static Credentials cred;

	public static void main(String[] args) {

		test_credentials();
		test_dropTables();
		test_databaseConnection();

		 test_crawler(50);
		 test_calculateTF();
		 test_pagerank();

		//test_query();
		// test_indexer();
		// test_levenshtein();
		// test_indexerWithInsert();
		// test_snippet();
		// test_shingles();
		// test_minhash();
		//test_shinglesTable(4,32);
		//test_minHashJaccardTable();
		//test_jaccardTable();
		//test_similarDocuments(10,0.8);
		// test_docid();
	}

	private static void test_credentials() {

		cred = new Credentials(host, port, database, username, password);
	}

	public static String text = "opposition parties and injured students blamed sunday night's violence on a student organisation linked to narendra modi's bharatiya janata party \r\n"
			+ " floods in indonesia s capital city have left more than 60 people dead and forced tens of thousands to flee their homes \r\n"
			+ " police beat protesters with batons as they storm hong kong shopping centre \r\n"
			+ " indian police clashed with thousands of protesters who demonstrated nbsp  to oppose a new law they say discriminates against muslims \r\n"
			+ " students have condemned as  barbaric  the tactics of delhi police after they stormed a university campus to break up a peaceful protest  injuring dozens \r\n"
			+ " indian police storm main library of new delhi s jamia millia university on sunday  firing teargas at students barricaded inside  footage shot inside the library shows students scrambling over desks and climbing through smashed windows to escape \r\n"
			+ "";

	private static void test_snippet() {
		Set<String> terms = new HashSet<>();
		terms.add("opposit");
		terms.add("blame");
		terms.add("storm");
		//String s = Snippet.getSnippet(text, terms);

		//System.out.println(s);
	}

	private static void test_shingles() {
		List<String[]> s = Shingling.calculateShingles(text, 4);
		for (String[] shingle : s) {
			System.out.println(Arrays.deepToString(shingle));
		}

	}
	
	private static void test_minhash() {
		List<String[]> s = Shingling.calculateShingles(text, 6);
		for (String[] shingle : s) {
			System.out.println(Arrays.deepToString(shingle));
		}
		
		int[] minhash = Shingling.minHash(s, 10,"MD5");
		System.out.println(Arrays.toString(minhash));

	}
	
	private static void test_shinglesTable(int k,int minhash) {
		Shingling.createShinglesTable(cred, k,minhash,100);

	}
	private static void test_jaccardTable() {
		Shingling.createJaccardTable(cred, 4);

	}
	
	private static void test_minHashJaccardTable() {
		Shingling.createMinHashJaccardTable(cred, 4);

	}

	private static void test_similarDocuments(int docid,double threshold) {
		Shingling.similarityThreshold(cred, docid, threshold);

	}
	private static void test_pagerank() {

		try {
			DBconnection myCon = new DBconnection();
			myCon.initialize(cred);
			PageRank pr = new PageRank(myCon);
			pr.calculatePageRank(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void test_levenshtein() {
		System.out.println(Levenshtein.calculateLevenshteinDistance("fil", "film") + "  should be 1");
		System.out.println(Levenshtein.calculateLevenshteinDistance("house", "houuse") + "  should be 1");
		System.out.println(Levenshtein.calculateLevenshteinDistance("Levenshtein", "Levenstein") + "  should be 1");
		System.out.println(Levenshtein.calculateLevenshteinDistance("cat", "kate") + "  should be 2");
		System.out.println(Levenshtein.calculateLevenshteinDistance("house", "rose") + "  should be 2");
		System.out.println(Levenshtein.calculateLevenshteinDistance("house", "hot") + "  should be 3");
		System.out.println(Levenshtein.calculateLevenshteinDistance("house", "ho") + "  should be 3");
		System.out.println(Levenshtein.calculateLevenshteinDistance("house", "") + "  should be 5");
	}

	private static void test_indexer() {
		Indexer index = new Indexer();
		try {
			IndexerResults result = index.indexURL(bbc, true);
			System.out.println("Language: " + result.getLanguage());
			// String links = index.findLinks(bbc,true);
		} catch (IOException e) {
			System.out.println("Something went wrong while indexing: IOException");
		}
	}

	private static void test_query() {
		try {
			DBconnection myCon = new DBconnection();
			myCon.initialize(cred);
			ArrayList<QueryResults> ss = QueryDB.createQuery(myCon.getCon());
			System.out.println(ss.get(0).getDocId());
			for (QueryResults rs : ss) {
				
				/* int docid = rs.getDocId(); String url;
				 
				 url = myCon.lookupDocUrl(docid);*/
				 
				System.out.println("Document ID = " + rs.getDocId() + " with url " + rs.getUrl() + " rank "
						+ rs.getRank() + " and total score " + rs.getScore());

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private static void test_calculateTF() {
		DBconnection myCon = new DBconnection();

		try {
			myCon.initialize(cred);
			System.out.println("tf");

			myCon.updateTF();
			System.out.println("idf");

			myCon.updateIDF();
			System.out.println("score");

			myCon.updateScore();

			System.out.println("count of terms in document");
			myCon.updatecountterms();
			myCon.refreshViewCterms();
			System.out.println("new tf");
			myCon.updatenewIDF();

			System.out.println("new idf");
			myCon.updatenewTF();

			System.out.println("new score");
			myCon.updatenewScore();
			System.out.println("create the views");
			myCon.refreshscoreviews();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void test_indexerWithInsert() {
		Indexer index = new Indexer();
		int docid = 0;
		DBconnection myCon = new DBconnection();

		try {
			myCon.initialize(cred);
			IndexerResults result = index.indexURL(
					"https://www.theguardian.com/world/2019/nov/09/freed-brazilian-ex-president-lula-speaks-to-jubilant-supporters",
					false);

			for (Map.Entry<String, Integer> entry : result.getTermFrequency().entrySet()) {
				myCon.insertIntoFeatures(0, entry.getKey(), entry.getValue());
			}

		} catch (IOException e) {
			System.out.println("Something went wrong while indexing: IOException");
		} catch (SQLException s) {
			System.out.println("Something went wrong while indexing: SQLException " + s.getMessage());

		}
	}

	private static void test_databaseConnection() {
		DBconnection myCon = new DBconnection();

		try {
			myCon.initialize(cred);
			myCon.createTables();
			myCon.createViewCterms();
			myCon.createscoreviews();
			System.out.println("Tables created");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void test_dropTables() {
		DBconnection myCon = new DBconnection();

		try {
			myCon.initialize(cred);
			myCon.dropTables();
			System.out.println("Tables dropped");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void test_crawler(int k) {

		DBconnection myCon = new DBconnection();
		try {
			myCon.initialize(cred);
			Set<String> startingURLs = new HashSet<String>();
			startingURLs.add(guardian);
			startingURLs.add(bbc);
			startingURLs.add(nyt);

			int parallelism = 5;
			Thread threads[] = new Thread[parallelism];

			for (int i = 0; i < parallelism; i++) {
				Crawler crawler = new Crawler(k, true, false, cred);
				if (i == 0)
					crawler.addToQueue(startingURLs, myCon, -1);
				Thread t1 = new Thread(crawler);
				t1.start();
				threads[i] = t1;
				Thread.sleep(500);
			}
			for (Thread t : threads) {
				t.join();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {

			e.printStackTrace();
		} catch (Exception e) {

		}

	}

	public static void test_docid() {
		DBconnection myCon = new DBconnection();

		try {
			myCon.initialize(cred);
			int result = myCon.getNextDocID();
			System.out.println(result);
			result = myCon.lookupDocID(bbc);
			System.out.println(result);
			result = myCon.lookupDocID(nyt);
			System.out.println(result);
			result = myCon.lookupDocID("nonesense");
			System.out.println(result);

		} catch (SQLException e) {
			System.out.println("SQL Exception: " + e.getMessage());
		}
	}

}
