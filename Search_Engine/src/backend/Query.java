package backend;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.extjwnl.JWNLException;

import java.sql.*;



public class Query {

	public Connection myCon;
	public String term;
	public int k;
	public int type;
	public Set<String> stemmedDisjunctive;

	public Query(Connection con, String term, int k, int type) {
		this.myCon = con;
		this.term = term;
		this.k = k;
		this.type = type;
	}
	
	public ArrayList<QueryResults> createQuery(QueryType queryType) throws JWNLException {
		// select the required view for doing the calc
		int limit;
		// select the required view for doing the scoring
		String scoretype = scoretype(type);
		if (k == 0) {
			limit = 20;
		} else {
			limit = k;
		}
		// clean the searchterm
		term = Indexer.cleanOutputterm(term);
		term = term.replaceAll("  ", " ");
		// add the domain
		ArrayList<String> domainurl = extractDomain();
		// sql term for domain
		String site = "";
		if (domainurl.size() > 0) {
			site = " and " + scoretype + ".url like CONCAT('%'," + "?" + ",'%') ";

		}
		// conjunctive terms
		ArrayList<String[]> conarrayterms = extractConTerms();

		// cojunctive sql term
		String conQueryTerm;
		if (conarrayterms.size() > 0) {
			if (queryType == QueryType.IMAGE) {
				conQueryTerm = createImageConQueryTerm(conarrayterms, scoretype);
			}
			else {
				conQueryTerm = createConQueryTerm(conarrayterms, scoretype);
			}
			
		} else {
			conQueryTerm = "";
		}

		// all terms
		stemmedDisjunctive = getStemmedDisjunctive();

		String whereStatement = "term = ?  ";
		for (int i = 1; i < stemmedDisjunctive.size(); i++) {
			whereStatement = whereStatement + " or term = ? ";
		}

		// Combination of sql statement

		String query;
		String adquery="";
		ArrayList<String> adterms = new ArrayList<String>();
		if (queryType == QueryType.IMAGE) {
			query = constructImageQuery(scoretype, limit, conQueryTerm, whereStatement, site);
		} else {
			adterms = getAdsterm(term);
			adquery =constracutadquery (adterms);
			query = constructQuery(scoretype, limit, conQueryTerm, whereStatement, site);
		}

		// System.out.print(query);
		try {
			ArrayList<QueryResults> qResults = new ArrayList<QueryResults>();
			if(!(queryType == QueryType.IMAGE)) {
				PreparedStatement adsearch = myCon.prepareStatement(adquery);
				int  adparametercounter= 1;
				for (String adterm : adterms) {
					adsearch.setString(adparametercounter  , adterm);
					adparametercounter++;
				}	
		
				ResultSet sResults = adsearch.executeQuery();
				while (sResults.next()) {
				QueryResults u = new QueryResults(sResults.getInt("adsid"), sResults.getFloat("cfoundterm"),
						sResults.getInt("adrank"), sResults.getString("adlink"), sResults.getString("addescription"),
						true, sResults.getString("imagelinks"));
				qResults.add(u);
				}
			}
		
		
			PreparedStatement search = myCon.prepareStatement(query);
			int parametercounter = 1;
			for (String stemmedword : stemmedDisjunctive) {
				search.setString(parametercounter, stemmedword);
				// System.out.println(parametercounter + " "+ stemmedword );
				parametercounter++;

			}
			if (conarrayterms.size() > 0) {
				for (int f = 0; f < conarrayterms.size(); f++) {
					for (int i = 0; i < conarrayterms.get(f).length; i++) {
						search.setString(parametercounter, conarrayterms.get(f)[i]);
						// System.out.println("next element site " +parametercounter + " "
						// +conarrayterms.get(f)[i] +" element "+ i+" of "+conarrayterms.get(f).length);
						parametercounter++;
					}
				}
			}
			if (domainurl.size() > 0) {
				for (int i = 1; i <= domainurl.size(); i++) {
					search.setString(parametercounter, domainurl.get(i - 1));
					/*
					 * System.out.println(stemmedDisjunctivecounter);
					 * System.out.println("next element site " + stemmedDisjunctivecounter);
					 */
					parametercounter++;

				}
			}

			ResultSet sResults = search.executeQuery();	


			if (queryType == QueryType.IMAGE) {
				while (sResults.next()) {
					QueryResults u = new QueryResults(sResults.getInt("imageid"), sResults.getFloat("totalScore"),
							sResults.getInt("imagerank"), sResults.getString("url"), "",
							stemmedDisjunctive);
					qResults.add(u);
				}
			}
			else {
				while (sResults.next()) {
					QueryResults u = new QueryResults(sResults.getInt("docid"), sResults.getFloat("totalScore"),
							sResults.getInt("docrank"), sResults.getString("url"), sResults.getString("txt"),
							stemmedDisjunctive);
					qResults.add(u);
				}
			}

			return qResults;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;

		}

	}

	// InputsPar ip = new InputsPar();
	private static String scoretype(int type) {
		String scoretype = "features_tfidf";
		if (type == 0) {
			scoretype = "features_tfidf";
		} else if (type == 1) {
			scoretype = "features_tfidf";
		} else if (type == 2) {
			scoretype = "features_bm25";
		} else if (type == 3) {
			scoretype = "features_combined";
		} else {
			scoretype = "features_tfidf";
		}
		return scoretype;
	}

	public static String cleanlamda(String term) {
		term = term.replaceAll("~", "");
		term = term.replaceAll(" ", "");
		return term;
	}

	public static String cleanqot(String term) {
		term = term.replaceAll("“", "");
		term = term.replaceAll("”", "");
		return term;
	}


	public ArrayList<String> extractDomain() {

		ArrayList<String> domainurl = new ArrayList<String>();
		String patternString1 = "((site:)(.*?)) ";
		Matcher sitematcher = Pattern.compile(patternString1).matcher(term);
		while (sitematcher.find()) {
			String ss = sitematcher.group();
			term = term.replaceAll(ss, "");
			ss = ss.replaceAll("site:", "");
			ss = ss.replaceAll(" ", "");
			domainurl.add(ss);

		}
		return domainurl;

	}

	public ArrayList<String[]> extractConTerms() throws JWNLException {
		ArrayList<String[]> conarrayterms = new ArrayList<String[]>();
		Matcher matcher = Pattern.compile("“(.*?)”").matcher(term);
		while (matcher.find()) {
			String conterm = cleanqot(matcher.group());
			// synonyms of conjunctive term
			if (conterm.charAt(0) == '~') {
				conterm = cleanlamda(conterm);
				Set<String> conTermSynonyms = Synonyms.getSynonyms(conterm);
				String[] conSynonymsword = new String[conTermSynonyms.size()];
				conTermSynonyms.toArray(conSynonymsword);
				for (int i = 0; i < conSynonymsword.length; i++) {
					Stemmer stemmer = new Stemmer();
					char[] chars = conSynonymsword[i].toCharArray();
					stemmer.add(chars, conSynonymsword[i].length());
					stemmer.stem();
					conSynonymsword[i] = stemmer.toString();
				}
				conarrayterms.add(conSynonymsword);
			} else {
				// only conjunctive term
				String[] conwords = { conterm };
				conarrayterms.add(conwords);
			}
		}
		return conarrayterms;
	}

	public String createImageConQueryTerm(ArrayList<String[]> conarrayterms, String scoretype) {
		String conQueryTerm = "";
		conQueryTerm = "AND image_features.imageid IN (SELECT DISTINCT imageid FROM image_features WHERE ( ";
		int allcontterm = conarrayterms.size();
		for (int f = 0; f < conarrayterms.size(); f++) {
			int termlenth = conarrayterms.get(f).length;
			for (int i = 0; i < termlenth; i++) {
				conQueryTerm = conQueryTerm + "term = ?";
				if (i < termlenth - 1) {
					conQueryTerm = conQueryTerm + " OR ";
				}
			}
			if (f < allcontterm - 1) {
				conQueryTerm = conQueryTerm + ") AND imageid IN \r\n"
						+ " (SELECT  imageid  FROM image_features WHERE (";
			} else {
				conQueryTerm = conQueryTerm + ")".repeat(allcontterm + 1);
			}
		}

		return conQueryTerm;

	}

	public String createConQueryTerm(ArrayList<String[]> conarrayterms, String scoretype) {
		String conQueryTerm = "";
		conQueryTerm = "AND " + scoretype + ".docid IN (SELECT DISTINCT docid FROM " + scoretype + " WHERE ( ";
		int allcontterm = conarrayterms.size();
		for (int f = 0; f < conarrayterms.size(); f++) {
			int termlenth = conarrayterms.get(f).length;
			for (int i = 0; i < termlenth; i++) {
				conQueryTerm = conQueryTerm + "term = ?";
				if (i < termlenth - 1) {
					conQueryTerm = conQueryTerm + " OR ";
				}
			}
			if (f < allcontterm - 1) {
				conQueryTerm = conQueryTerm + ") AND docid IN \r\n" + " (SELECT  docid  FROM " + scoretype + " WHERE (";
			} else {
				conQueryTerm = conQueryTerm + ")".repeat(allcontterm + 1);
			}
		}
		return conQueryTerm;

	}

	public Set<String> getStemmedDisjunctive() throws JWNLException {
		Set<String> disjunctive = new HashSet<>();
		term = cleanqot(term);
		// check the synonym
		Matcher matcher = Pattern.compile("~(.*?) ").matcher(term);
		while (matcher.find()) {
			String synonym = cleanlamda(matcher.group());
			disjunctive.addAll(Synonyms.getSynonyms(synonym));
		}
		term = term.replaceAll("~", "");
		// all disjunctive terms without synonyms
		Set<String> stemmed = new HashSet<>();
		String[] disjunctivewords = term.split("\\s+");
		for (String word : disjunctivewords) {
			disjunctive.add(word);
		}
		// stemming the disjunctive terms

		for (String chr : disjunctive) {
			Stemmer stemmer = new Stemmer();
			char[] chars = chr.toCharArray();
			stemmer.add(chars, chr.length());
			stemmer.stem();
			stemmed.add(stemmer.toString());
		}
		return stemmed;
	}

	public String constructQuery(String scoretype, int limit, String conQueryTerm, String whereStatement, String site) {

		String query = "SELECT a.docid, a.totalScore, rank() OVER (ORDER BY totalScore desc) AS docrank, url AS url, txt AS txt FROM \n"
				+ "(SELECT  " + scoretype
				+ ".docid, SUM(score) AS totalScore, documents.url AS url, documents.txt AS txt FROM " + scoretype
				+ " LEFT JOIN documents ON documents.docid= " + scoretype + ".docid \n" + "where (\n" + whereStatement
				+ ") " + conQueryTerm + site + "GROUP BY " + scoretype + ".docid,documents.url,documents.txt \n"
				+ ") AS a \n" + "ORDER BY docrank  LIMIT \n" + limit;
		return query;

	}
	

	public String constructImageQuery(String scoretype, int limit, String conQueryTerm, String whereStatement,
			String site) {

		String query = "SELECT a.imageid, a.totalScore ,rank() OVER (ORDER BY totalScore DESC) AS imagerank, url AS url FROM \r\n"
				+ "				(SELECT  image_features.imageid, sum(distance) AS totalScore, images.url AS url FROM image_features LEFT JOIN images ON images.imageid= image_features.imageid \r\n"
				+ "				 WHERE  (\n" + whereStatement + ") " + conQueryTerm + site + "\n"
				+ "				GROUP BY image_features.imageid,images.url ) AS a ORDER BY imagerank  LIMIT " + limit;
		return query;

	}
	public  String constracutadquery (ArrayList<String> adterms){
		String whereStatement = "adterms = ?  ";
		for (int i = 1; i < adterms.size(); i++) {
			whereStatement = whereStatement + " or adterms = ? ";
		}
		String query ="	with cta as (	select adsid, regexp_split_to_table(terms, ',')as adterms ,clickcost, addescription,adlink,imagelinks,currentclicks,budget from public.ads) \r\n" + 
				"	select  adsid, rank() over (order by count(adsid),clickcost desc)as adrank,count(adsid) as cfoundterm, addescription,\r\n" + 
				"	adlink,imagelinks ,currentclicks,budget from cta where (currentclicks * clickcost)<budget and (\r\n" + 
				whereStatement+
				"	)	group by adsid ,clickcost,addescription,adlink,imagelinks,currentclicks,budget limit 4"	 ;
		return query ;
			
	}
	public  ArrayList<String> getAdsterm(String term){
		ArrayList<String> cterms= new ArrayList<String>();	
		String[] queryterms= term.split("\\s+");
		for (int i=0 ; i< queryterms.length ; i++ ) {
			for(int f = 0; (f+i)<queryterms.length  ; f++) {
				String terms = "";			
				for(int s = f; s<= i+f ; s++) {
					if(s==f) {	terms =queryterms[s];}
					if(s>f) {terms = terms + " "+ queryterms[s];}	
				
				}
				cterms.add(terms);
			}
		}
		return cterms;
	}

	public static String spellcheck(Connection con, String input, QueryType queryType) {
		String output = null;
		int levenshtein = 1;
		while (output == null && levenshtein <= 2) {
			try {
				String query ;
				if(queryType == QueryType.IMAGE) {
					query = "SELECT Distinct(term) FROM image_features";
				}
				else {
					query = "SELECT Distinct(term) FROM features";

				}

				PreparedStatement features = con.prepareStatement(query);
				ResultSet testRS = features.executeQuery();
				while (testRS.next() && output == null) {
					String term = testRS.getString("term");
					// System.out.println("checking against "+ term);
					if (!(term.length() > (input.length() + levenshtein))
							&& !(term.length() < (input.length() - levenshtein))) {
						// System.out.println("checking closely against "+ term);
						if (Levenshtein.calculateLevenshteinDistance(input, term) <= levenshtein) {
							output = term;
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			levenshtein++;
		}

		if (output == null) {
			return input;
		}

		return output;

	}

}
