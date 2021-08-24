package backend;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Shingling {
	

	public static List<String[]> calculateShingles(String text, int k) {
		String[] words = text.split("\\s+");
		List<String[]> shingles = new ArrayList<>();
		for (int i = 0; i < (words.length - k + 1); i++) {
			String[] shingle = Arrays.copyOfRange(words, i, i + k);
			shingles.add(shingle);
		}

		return shingles;

	}

	public static void createShinglesTable(Credentials cred, int k, int minhash_value, int limit) {
		DBconnection myCon = new DBconnection();
		System.out.println("creating Shingles Table...");
		try {
			myCon.initialize(cred);
			Statement stmt = myCon.con.createStatement();

			String drop = "DROP TABLE IF EXISTS shingles";
			stmt.execute(drop);

			String create = String.format(
					"CREATE TABLE IF NOT EXISTS shingles(docid INT, shingles text[][%d], minhash INT[], PRIMARY KEY(docid))",
					k);
			stmt.execute(create);

			PreparedStatement getText = myCon.con
					.prepareStatement("SELECT docid,txt FROM documents WHERE crawled_on_date IS NOT NULL ORDER BY docid LIMIT " + limit);
			ResultSet result = getText.executeQuery();
			while (result.next()) {
				int docid = result.getInt("docid");
				String txt = result.getString("txt");
				List<String[]> shingles;
				if (txt == null) {
					//System.out.println("txt is null");
					shingles = new ArrayList();
				} else {
					txt = Indexer.cleanOutput(txt);
					shingles = calculateShingles(txt, k);
				}
				int max;
				
				//max size for the number of shingles else the database locks up
				if(shingles.size()>1000) {
					//System.out.println("more then 100 shingles");
					max=1000;
				}else{
					max=shingles.size();
				}
				if (!shingles.isEmpty()) {
					String insert = "INSERT INTO shingles VALUES(" + docid + ", ARRAY[";
					for (int j = 0; j < max; j++) {
						String[] kshingle = shingles.get(j);
						String newShingle = "[";
						for (int i = 0; i < k; i++) {
							newShingle += "'" + kshingle[i] + "'";
							if (i != (k - 1))
								newShingle += ",";
						}

						insert += newShingle + "] ";
						if (j != (max - 1))
							insert += ",";
					}
					insert += "],ARRAY[";
					int[] minhash = minHash(shingles, minhash_value, "MD5");
					String minhashString = "";
					for (int j = 0; j < minhash.length; j++) {
						minhashString += minhash[j];
						if (j != (minhash.length - 1)) {
							minhashString += ",";
						}

					}
					insert += minhashString + "])";

					System.out.println(docid);
					PreparedStatement insertDoc = myCon.con.prepareStatement(insert);
					
					insertDoc.executeUpdate();

				}
			}
			result.close();
			System.out.println("All docs converted to shingles");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//brute-force Jaccard
	public static void createJaccardTable(Credentials cred, int k) {
		DBconnection myCon = new DBconnection();
		System.out.println("creating Jaccard Table...");
		try {
			myCon.initialize(cred);
			Statement stmt = myCon.con.createStatement();

			// taken from postgresql wiki
			String reduceDim = "CREATE OR REPLACE FUNCTION public.reduce_dim(anyarray)\r\n"
					+ "RETURNS SETOF anyarray AS\r\n" + "$function$\r\n" + "DECLARE\r\n" + "    s $1%TYPE;\r\n"
					+ "BEGIN\r\n" + "    FOREACH s SLICE 1  IN ARRAY $1 LOOP\r\n" + "        RETURN NEXT s;\r\n"
					+ "    END LOOP;\r\n" + "    RETURN;\r\n" + "END;\r\n" + "$function$\r\n"
					+ "LANGUAGE plpgsql IMMUTABLE;";
			stmt.execute(reduceDim);
			
			String drop_reduced = "DROP TABLE IF EXISTS shingles_reduced Cascade";
			stmt.execute(drop_reduced);
			
			String create_reduced="CREATE TABLE shingles_reduced AS (SELECT docid,reduce_dim(shingles) FROM shingles)";
			stmt.execute(create_reduced);
			System.out.println("table reduced...");
			
	        String index = "CREATE INDEX IF NOT EXISTS idx_shingles_reduced ON shingles_reduced(docid,reduce_dim);";
	        stmt.execute(index);
	        System.out.println("index created...");

			String drop = "DROP TABLE IF EXISTS pairwise_jaccard;";
			stmt.execute(drop);

			String create = "CREATE TABLE pairwise_jaccard\r\n" + "  AS \r\n" + "(SELECT\r\n" + 
					"  a.docid AS docid1,  b.docid  AS docid2,\r\n" + 
					"  (SELECT  COUNT(1) FROM (SELECT DISTINCT(reduce_dim) FROM shingles_reduced WHERE docid=a.docid) AS auniques) AS acount,\r\n" + 
					"  (SELECT  COUNT(1) FROM  (SELECT DISTINCT(reduce_dim) FROM shingles_reduced WHERE docid=b.docid) AS buniques) AS bcount,\r\n" + 
					"  (SELECT COUNT(1) FROM ((SELECT reduce_dim FROM shingles_reduced WHERE docid=a.docid) INTERSECT (SELECT reduce_dim FROM shingles_reduced WHERE docid=b.docid)) AS intersect_table) AS intersect_count,\r\n" + 
					"  (SELECT COUNT(1) FROM ((SELECT reduce_dim FROM shingles_reduced WHERE docid=a.docid) UNION (SELECT reduce_dim FROM shingles_reduced WHERE docid=b.docid)) AS union_table) AS union_count\r\n" + 
					"					FROM shingles a JOIN shingles b ON a.docid < b.docid );";
			stmt.execute(create);
			
			System.out.println("calculating...");

			String alter = "ALTER TABLE pairwise_jaccard ADD jaccard FLOAT;";
			stmt.execute(alter);

			String update = "UPDATE pairwise_jaccard SET jaccard = (Cast(intersect_count as float)  /Cast(union_count as float ));";
			stmt.execute(update);

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	//min Hash Jaccard
	public static void createMinHashJaccardTable(Credentials cred, int k) {
		DBconnection myCon = new DBconnection();
		System.out.println("creating min hash Jaccard Table...");
		try {
			myCon.initialize(cred);
			Statement stmt = myCon.con.createStatement();

			
			String drop_reduced = "DROP TABLE IF EXISTS mshingles_reduced Cascade";
			stmt.execute(drop_reduced);
			
			String create_reduced="CREATE TABLE mshingles_reduced AS (SELECT docid,UNNEST(minhash) FROM shingles)";
			stmt.execute(create_reduced);
			System.out.println("table reduced...");
			
	        String index = "CREATE INDEX IF NOT EXISTS idx_mshingles_reduced ON mshingles_reduced(docid,unnest);";
	        stmt.execute(index);
	        System.out.println("index created...");

			String drop = "DROP TABLE IF EXISTS mpairwise_jaccard;";
			stmt.execute(drop);

			String create = "CREATE TABLE mpairwise_jaccard\r\n" + "  AS \r\n" + "(SELECT\r\n" + 
					"  a.docid AS docid1,  b.docid  AS docid2,\r\n" + 
					"  (SELECT  COUNT(1) FROM (SELECT DISTINCT(unnest) FROM mshingles_reduced WHERE docid=a.docid) AS auniques) AS acount,\r\n" + 
					"  (SELECT  COUNT(1) FROM  (SELECT DISTINCT(unnest) FROM mshingles_reduced WHERE docid=b.docid) AS buniques) AS bcount,\r\n" + 
					"  (SELECT COUNT(1) FROM ((SELECT unnest FROM mshingles_reduced WHERE docid=a.docid) INTERSECT (SELECT unnest FROM mshingles_reduced WHERE docid=b.docid)) AS intersect_table) AS intersect_count,\r\n" + 
					"  (SELECT COUNT(1) FROM ((SELECT unnest FROM mshingles_reduced WHERE docid=a.docid) UNION (SELECT unnest FROM mshingles_reduced WHERE docid=b.docid)) AS union_table) AS union_count\r\n" + 
					"					FROM shingles a JOIN shingles b ON a.docid < b.docid );";
			stmt.execute(create);
			
			System.out.println("calculating...");

			String alter = "ALTER TABLE mpairwise_jaccard ADD jaccard FLOAT;";
			stmt.execute(alter);

			String update = "UPDATE mpairwise_jaccard SET jaccard = (Cast(intersect_count as float) /Cast(union_count as float ));";
			stmt.execute(update);

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static int[] minHash(List<String[]> shingles, int numOfminValues, String algorithm) {
		int[] minhash = new int[numOfminValues];
		Arrays.fill(minhash, Integer.MAX_VALUE);

		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			for (String[] shingle : shingles) {
				Arrays.sort(minhash);
				String shingleString = Arrays.deepToString(shingle);
				md.update(shingleString.getBytes());
				byte[] digest = md.digest();
				int myHash = new BigInteger(digest).intValue();
				if (myHash < minhash[minhash.length - 1]) {
					minhash[minhash.length - 1] = myHash;
				}
				Arrays.sort(minhash);
			}

		} catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException in minHash with algorithm: " + algorithm);
			e.printStackTrace();
		}
		return minhash;

	}
	
	public static void similarityThreshold(Credentials cred, int docid, double threshold) {
		
		String udf = "CREATE OR REPLACE FUNCTION similar_documents (docid INT, threshold FLOAT)\r\n" + 
				"RETURNS TABLE (docid1 INT, docid2 INT,jaccard FLOAT)\r\n" + 
				"AS $$\r\n" + 
				"BEGIN\r\n" + 
				" RETURN QUERY\r\n" + 
				" SELECT p.docid1 AS docid1,p.docid2 AS docid2,p.jaccard AS jaccard FROM pairwise_jaccard AS p WHERE (p.docid1=$1 OR p.docid2=$1) AND p.jaccard > $2;\r\n" + 
				"END; $$\r\n" + 
				"LANGUAGE 'plpgsql';";
		
		DBconnection myCon = new DBconnection();
		System.out.println("creating Shingles Table...");
		try {
			myCon.initialize(cred);
			Statement stmt = myCon.con.createStatement();
			stmt.execute(udf);
			
			String query = "SELECT similar_documents("+docid+","+threshold+")";
			ResultSet result = stmt.executeQuery(query);
			while(result.next()) {
				System.out.println(result.getString(1));
			}
			result.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	String average_error= "SELECT (SUM(ABS((p.jaccard -m.jaccard))) / (SELECT COUNT(*) FROM pairwise_jaccard)) as difference FROM pairwise_jaccard p JOIN mpairwise_jaccard m ON m.docid1=p.docid1 AND m.docid2=p.docid2";
;
}
