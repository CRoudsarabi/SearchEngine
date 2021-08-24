package backend;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class QueryResults {


    private int docid;
    private int rank;
    private float score;
    private String url;
    private String snippet;
    private Set<String> searchedterm ; 
    private double snippetScore ; 
    private ArrayList<String> snip;
    private boolean ads = false;
    private String imagelink;
    public QueryResults(int docid, float score, int rank, String url) {
        this.docid = docid;
        this.rank = rank;
        this.score = score;
        this.url = url;
        this.ads = ads;
        this.snippet = snippet;
        this.searchedterm = searchedterm;
    }
    //adid,score,int rank,url,description,searchterm,imagelink,
    public QueryResults(int docid, float score, int rank, String url, String snippet,boolean ads,String imagelink) {
        this.docid = docid;
        this.rank = rank;
        this.score = score;
        this.url = url;
        this.snippet = snippet;
        this.searchedterm = searchedterm;
        this.ads = ads;
        this.imagelink= imagelink;
    }
    public QueryResults(int docid, float score, int rank, String url, String snippet, Set<String> searchedterm) {
        this.docid = docid;
        this.rank = rank;
        this.score = score;
        this.url = url;
        this.snippet = snippet;
        this.searchedterm = searchedterm;
        this.ads = ads;
        this.imagelink= imagelink;
    }

    public int getDocId() {
        return this.docid;
    }

    public int getRank() {
        return this.rank;
    }

    public float getScore() {
        return this.score;
    }
    public String getUrl() {
        return this.url;
    }

    public ArrayList<String> getSnippetArray() {
    	
    	ArrayList<String> snip = Snippet.getSnippet(snippet, searchedterm);
    	this.snip = snip;
    	return snip;
    			
   }
    public String getSnippet() {
    	if(!ads) {
    	  	if(this.snip == null) {
        		getSnippetArray();
        	}
        	return this.snip.get(1);
    	}else return snippet;
  
    			
   }
    public double getSnippetScore() {
    	if(!ads) {
    		if(this.snip == null) {
    	
    		getSnippetArray();
    	}
    	return Double.parseDouble(this.snip.get(0));
    	} else return 0;			
       }
    public boolean getAds() {
    		return this.ads;
	        			
	       }
    public String getImagelink() {
    	if (ads) {
    		return this.imagelink;
        			
       }else return "";
    }
}

