package backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MultiSearchQuery {

	static String group_1_link = "http://isproj-vm01.informatik.uni-kl.de:8080/is-project/json";
	static String group_3_link = "http://isproj-vm06.informatik.uni-kl.de:8080/is-project/json";
	static String group_4_link = "http://isproj-vm07.informatik.uni-kl.de:8080/json";
	static String group_6_link = "http://isproj-vm08.informatik.uni-kl.de:8080/Search_Engine/json";

	static String part = "?query=";
	static String part2 = "&k=20&score=3";

	public static ArrayList<String> constructLinks(String[] terms, boolean[] whichVM) {

		String query = "";
		for (int i = 0; i < terms.length; i++) {
			query += terms[i];
			if (i < (terms.length - 1)) {
				query += "+";
			}
		}

		ArrayList<String> links = new ArrayList<>();
		if(whichVM[0]) {
			links.add(group_1_link + part + query + part2);
		}
		if(whichVM[1]) {
			links.add(group_3_link + part + query + part2);
		}
		if(whichVM[2]) {
			links.add(group_4_link + part + query + part2);
		}
		if(whichVM[3]) {
			links.add(group_6_link + part + query + part2);
		}

		return links;

	}

	public static MultiSearchQueryResults readJson(ArrayList<String>  links) {
		URL url;
		MultiSearchQueryResults msqr;
		double avg_cw;
		long[] cw = new long[links.size()];
		ArrayList<Map<String,Integer>> dfList= new ArrayList<>();
		ArrayList<JSONArray> rLList = new ArrayList<>();
		try {
			Thread threads[] = new Thread[links.size()];
			JSONRequest jsonRequests[] = new JSONRequest[links.size()];
			for (int i = 0; i < links.size(); i++) {
				JSONRequest jrequest =new JSONRequest(links.get(i));
				Thread t1 = new Thread(jrequest);
				t1.start();
				threads[i] = t1;
				jsonRequests[i]=jrequest;
			}	
			for (Thread t : threads) {
					t.join();
			}
			ArrayList<JSONObject> jsonList =new ArrayList<>();
			for (int i = 0; i < jsonRequests.length; i++) {
				jsonList.add(jsonRequests[i].json);
			}	
			
			for (int i = 0; i < links.size(); i++) {
				//System.out.println(json);
				JSONObject json = jsonList.get(i);
				JSONArray stat = (JSONArray) json.get("stat");
				
				
				Map<String,Integer> df = new HashMap<>();
				for (int j = 0; j < stat.size(); j++) {
					JSONObject entry = (JSONObject) stat.get(j);
					String key= (String) entry.get("term");
					long value1= (long) entry.get("df");
					Integer value2= Math.toIntExact(value1);
					df.put(key, value2);
				}
				dfList.add(df);


				JSONArray rL = (JSONArray) json.get("resultList");
				rLList.add(rL);

				cw[i] = (long) json.get("cw");
				

			}
			
			avg_cw = (Arrays.stream(cw).sum()) / cw.length;
			msqr = new MultiSearchQueryResults(links,avg_cw,cw,dfList,rLList);
			return msqr;
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static double[] calculateCORI(String term, MultiSearchQueryResults msqr) {
		//collection dependent
		int cf =0;
		double[] t = new double[msqr.dfList.size()];
		for(int i=0; i<t.length; i++) {
			if(msqr.dfList.get(i).get(term) != null) {
				double df = msqr.dfList.get(i).get(term);
				t[i] = df/(df+50+ 150*(msqr.cw[i]/msqr.avg_cw));
				cf++;
			}
			else {
				t[i] = 0;
			}
			
		}
		
		//collection independent
		double c = msqr.rLList.size();
		double bigI = Math.log((c+0.5)/cf)/Math.log(c+1.0);
		
		
		//calculate score
		double b = 0.4;
		

		double[] score=new double[msqr.dfList.size()]; 
		double rmin = b + (1-b)*bigI*0.0;
		double rmax = b + (1-b)*bigI*1.0;
		for(int i=0; i<score.length; i++) {
			if(t[i] > 0) {
				double ri=b + (1-b)*bigI*t[i];

				score[i] =(ri-rmin)/(rmax-rmin);
			}
			else {
				score[i] = 0;
			}
		}
		return score;
	}
	
	public static ArrayList<MultiSearchResultSet> mergeResults(double[] totalScore, MultiSearchQueryResults msqr){
		ArrayList<MultiSearchResultSet> results = new ArrayList<>();
		
		for(int i =0; i< msqr.rLList.size(); i++ ) {
			JSONArray rL = msqr.rLList.get(i);
			
			for(int j=0; j< rL.size(); j++) {
				JSONObject jo=(JSONObject) rL.get(j);
				String url = (String)  jo.get("url");
				double originalScore= (Double) jo.get("score");
				double newScore = ((originalScore+0.4*originalScore*totalScore[i])/1.4);
				results.add(new MultiSearchResultSet(newScore,url,msqr.links.get(i).replaceAll(":8080.*?score=3", "")));
			}
		}
		
		
		return results;
		
	}

	public static void main(String args[]) {

		boolean[] whichVM = {true,true,true,true};
		String term = "trump";
		term = Indexer.cleanOutputterm(term);
		term = term.replaceAll("  ", " ");
		String[] terms = term.split("\\s+");
		
		ArrayList<String> links = constructLinks(terms,whichVM);
		MultiSearchQueryResults msqr = readJson(links);
		
		double[] totalScore= new double[msqr.dfList.size()];
		for(String s : terms) {
			double[] score = MultiSearchQuery.calculateCORI(s, msqr);
			for(int i =0; i<msqr.dfList.size(); i++) {
				totalScore[i] += score[i];
			}
		}
		
		System.out.println(Arrays.toString(totalScore));
		
		ArrayList<MultiSearchResultSet> mergedResults= mergeResults(totalScore, msqr);
		Collections.sort(mergedResults);
		
		for(int i= 0; i<mergedResults.size(); i++ ) {
			System.out.println(mergedResults.get(i).score+ " " +mergedResults.get(i).url);
		}

	}
}
