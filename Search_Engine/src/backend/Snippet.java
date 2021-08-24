package backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//Time Complexity: O(w + ws)
//Space Complexity: O(s)
//Where w is the number of words in the document, and s is the number of search terms
public class Snippet {
	
	private Map<String, Integer> snippetDataPoints = new HashMap<String, Integer>();
	private Map<String, Integer> smallestsnippetDataPoints = new HashMap<String, Integer>();
	private String[] words;
	Set<String> searchTerms = new HashSet<>();
	private int shortestSnippetStart = 0, shortestSnippetEnd, currentSnippetStart = 0;

	public static ArrayList<String> getSnippet(String document, Set<String> searchTerms) {
		String document2=  Indexer.cleanOutput(document);
	// remove punctuation

		Set<String> searchTerms2 = new HashSet<>();
		Set<String> missingTerms = new HashSet<>();
		StringBuilder stemBuilder = new StringBuilder();
		String[] words = document2.split("\\s+");
		for (String s : words) {
			Stemmer stemmer = new Stemmer();
			char[] chars = s.toCharArray();
			stemmer.add(chars, s.length());
			stemmer.stem();
			stemBuilder.append(stemmer.toString());
			stemBuilder.append(" ");
			// System.out.println(stemmer.toString() + " original: "+s);
		}

		String stemmedOutput = stemBuilder.toString();
		stemmedOutput = stemmedOutput.replace("  ", " ");
		String notexistingterms = "";
		for (String ss: searchTerms) {
			if (stemmedOutput.contains(ss)) {
				searchTerms2.add(ss);
			}
			else missingTerms.add(ss) ;
		}
		double score = (double)searchTerms2.size()/(double)searchTerms.size();
		ArrayList<String> results2 = new ArrayList<String>();
		results2.add(0, ""+score);
		Snippet solution = new Snippet (document2.split("\\s+"), searchTerms2);//document.split isnt the most efficient, but we are already over O(n), and this keeps it simple
		if (missingTerms.size()>0) {
			String resultsnip = solution.solve();
			String s= resultsnip +"\r\n"+ "{"+ missingTerms.toString() +" are/is not exist"+ "}";
			results2.add(1, s);					
			return results2;
		
		}else {
			String resultsnip = solution.solve();
			String s= resultsnip;
			results2.add(1, s);	
			return results2;
		}
		
	}
	
	public Snippet(String[] words,Set<String> searchTerms){
		this.words = words;
		this.searchTerms = searchTerms;
		shortestSnippetEnd=words.length-1;
	}

	private String solve(){
		int minsnippetlength= words.length-1;
		int currentsnippetlength ;

		for(int i=0;i<words.length;i++){
			for(String searchTerm : searchTerms){
				Stemmer stemmer = new Stemmer();
				char[] chars = words[i].toCharArray();
				stemmer.add(chars, words[i].length());
				stemmer.stem();	
				String stemmed = stemmer.toString();
				if(searchTerm.equals(stemmed)){
					// add To Snippet
					Integer previousPosition = snippetDataPoints.put(stemmer.toString().replace(" ", ""), i);
					if(previousPosition == null || previousPosition <= currentSnippetStart){
						currentSnippetStart = Collections.min(snippetDataPoints.values());
					}
					if(snippetDataPoints.size() == searchTerms.size()){
						currentsnippetlength = i - currentSnippetStart;
						//  determine the ShortestSnippet
						if(minsnippetlength > currentsnippetlength ){
							smallestsnippetDataPoints.putAll(snippetDataPoints);
							minsnippetlength = currentsnippetlength;
							shortestSnippetStart = currentSnippetStart;
							shortestSnippetEnd = i;
						
						}
					}
				}
			}
		}
		if(smallestsnippetDataPoints.size()==0) {
			smallestsnippetDataPoints.putAll(snippetDataPoints);
		}

		StringBuilder snippet = new StringBuilder();
		if (minsnippetlength< 32) {		
			shortestSnippetEnd = shortestSnippetEnd + 32-minsnippetlength;
			if(shortestSnippetEnd <= words.length-1) {
				for(int i = shortestSnippetStart; i<=shortestSnippetEnd; i++){
					snippet.append(words[i] + " ");
				}	
			}else{
				for(int i = shortestSnippetStart; i<=words.length-1; i++){
					snippet.append(words[i] + " ");}
			}

		}
		if (minsnippetlength> 32) {

			int paritiallength = 32/smallestsnippetDataPoints.size();
			smallestsnippetDataPoints.entrySet().removeIf(key -> key.getValue() ==shortestSnippetStart);
			int snipstartpoint=shortestSnippetStart;
			int snippendpoint = snipstartpoint	+paritiallength;
			for(int i = snipstartpoint; i<=snippendpoint; i++){
				snippet.append(words[i] + " ");}
			snippet.append("....");
			System.out.println(snippet.toString());
			smallestsnippetDataPoints.entrySet().removeIf(key -> key.getValue() ==shortestSnippetEnd);
			int end =shortestSnippetEnd;
			
			//for(Map.Entry<String, Integer> entry :snippetDataPoints.entrySet() ) {
			while(smallestsnippetDataPoints.size()>0 ) {
				int nextindex =Collections.min(smallestsnippetDataPoints.values());
				smallestsnippetDataPoints.entrySet().removeIf(key -> key.getValue() ==Collections.min(smallestsnippetDataPoints.values()));
		
				if (nextindex <=snippendpoint) {
					snippet.delete(snippet.length()-4, snippet.length());
					for(int i = snippendpoint+1; i<=snippendpoint + paritiallength; i++){
						snippet.append(words[i] + " ");}
					snippet.append("....");
					snippendpoint = snippendpoint + paritiallength;
				}else {
					snipstartpoint = nextindex;
					snippendpoint = snipstartpoint + paritiallength;
					for(int i = snipstartpoint; i<=snippendpoint ; i++){
						if(i< words.length) {
							snippet.append(words[i] + " ");
						}
						}
					snippet.append("....");
					System.out.println(snippet.toString());
					System.out.println(snipstartpoint);
					System.out.println(snippendpoint);
				}
				
			}
			//snippet.append("....");
			for(int i = end-paritiallength; i<= end; i++){
				System.out.println(words[i]);
				snippet.append(words[i] + " ");}
		}

		snippet.deleteCharAt(snippet.length()-1);
		return snippet.toString();
	}
	public static void main(String[] args) {
		String myCon =   "opposition parties and injured students blamed sunday night’s violence on a student organisation linked to narendra modi’s bharatiya janata party \r\n" + 
				" floods in indonesia s capital city have left more than 60 people dead and forced tens of thousands to flee their homes \r\n" + 
				" police beat protesters with batons as they storm hong kong shopping centre \r\n" + 
				" indian police clashed with thousands of protesters who demonstrated nbsp  to oppose a new law they say discriminates against muslims \r\n" + 
				" students have condemned as  barbaric  the tactics of delhi police after they stormed a university campus to break up a peaceful protest  injuring dozens \r\n" + 
				" indian police storm main library of new delhi s jamia millia university on sunday  firing teargas at students barricaded inside  footage shot inside the library shows students scrambling over desks and climbing through smashed windows to escape \r\n" + 
				"";  
		Set<String>  term1 = new HashSet<>();
		term1.add("opposit");
		term1.add("blame");
		term1.add("storm");
		//String s= getSnippet(myCon,term1);
		//System.out.println(s);
	}
}