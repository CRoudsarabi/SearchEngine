package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Ngramquery {
	
	public static void main(String[] args)  {	
		String txt = "test1 test2 test3 test4 test5";	
		ArrayList<String> txt2= getAdsterm(txt);
		for (String s: txt2) {
			System.out.println(s);
		}
	}
	
	public static ArrayList<String> getAdsterm(String term){
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
	public static ArrayList<QueryResults>getads(Connection myCon ,ArrayList<String> adterms){
		String whereStatement = "term = ?  ";
		for (int i = 1; i < adterms.size(); i++) {
			whereStatement = whereStatement + " or term = ? ";
		}
		String query ="with cta as (\r\n" + 
				"	select adsid, regexp_split_to_table(terms, ',')as adterms ,clickcost,company from public.ads)\r\n" + 
				"	select clickcost ,adsid, rank() over (order by count(adsid),clickcost desc)as adrank,count(adsid) as cfoundterm,company from cta where\r\n" + 
				whereStatement + 
				"	group by adsid ,clickcost,company "	 ;
			
		try {
			PreparedStatement search = myCon.prepareStatement(query);
			int  parametercounter= 1;
			for (String stemmedword : adterms) {
				search.setString(parametercounter  , stemmedword);
				parametercounter++;

			}
				
			ResultSet sResults = search.executeQuery();
			ArrayList<QueryResults> qResults = new ArrayList<QueryResults>();
			while (sResults.next()) {
				QueryResults u = new QueryResults(sResults.getInt("adsid"),  sResults.getFloat("cfoundterm"), sResults.getInt("adrank"),sResults.getString("url"));
				qResults.add(u);
			}
			return qResults;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;

		}
	}
}
