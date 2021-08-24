package backend;

import java.util.ArrayList;
import java.util.Map;

import org.json.simple.JSONArray;

public class MultiSearchQueryResults {

	public ArrayList<String> links;
	public double avg_cw;
	public long[] cw;

	public ArrayList<Map<String,Integer>> dfList;
	public ArrayList<JSONArray> rLList;
	
	
	public MultiSearchQueryResults(ArrayList<String> links,double avg_cw, long[] cw, ArrayList<Map<String,Integer>> dfList, ArrayList<JSONArray> rLList) {
		this.links=links;
		this.avg_cw=avg_cw;
		this.cw = cw;
		this.dfList=dfList;
		this.rLList = rLList;
		
	}
}
