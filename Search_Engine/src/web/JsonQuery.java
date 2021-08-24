package web;

import backend.*;
import net.sf.extjwnl.JWNLException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JsonQuery extends HttpServlet {
/*	public int[] getparquery( String k, String score) {
		int [] parquery = new int[2];
		 parquery[0] = 20;
		 parquery[1] = 1;
		if (k != null) {
			parquery[0] = Integer.parseInt(k);
		}
		else {
			parquery[0] = 20;
		}
		if (score != null) {
			parquery[1]= Integer.parseInt(score);
		}
		else {
			parquery[1] = 1;
		}
		return parquery;
	}
	public void printjson(HttpServletResponse res , Connection con,String term,List<QueryResults> qresults, Set<String> stemmedDisjunctive) throws SQLException, IOException {

		JSONObject jsonResult= new JSONObject();
		
		//resultList part of JSON object
		JSONArray resultList = new JSONArray();
		
		for(int i =0; i< qresults.size(); i++ ) {
			JSONObject entry = new JSONObject();
			entry.put("rank",qresults.get(i).getRank());
			entry.put("score",qresults.get(i).getScore());
			entry.put("url", qresults.get(i).getUrl());
			resultList.add(entry);
		}
		
		jsonResult.put("resultList", resultList);
		
		//TODO:
		
		//query part of JSON object
		JSONObject jquery = new JSONObject();
		jquery.put("k", 5);
		jquery.put("query", term);
		jsonResult.put("query", jquery);
		
		//stat part of JSON object
		JSONArray stat = new JSONArray();
		// get df
		int[] tcount = new int[stemmedDisjunctive.size()];
		for (String ss: stemmedDisjunctive) {
			JSONObject entry = new JSONObject();
			entry.put("term",ss);
			PreparedStatement df = con.prepareStatement(
					"SELECT COUNT(docid) as Cdocid FROM features WHERE term = '" + ss + "'");
			// System.out.println("SELECT COUNT(docid) as Cdocid FROM features WHERE term =
			// '"+ stemmedWords[i] + "'");
			ResultSet dfs = df.executeQuery();
			dfs.next();
			entry.put("df",dfs.getInt("Cdocid"));
			stat.add(entry);
		}
		jsonResult.put("stat", stat);
		
		// get cw
		PreparedStatement cw = con.prepareStatement("SELECT COUNT(DISTINCT(term)) as countterms FROM features");
		ResultSet cwResults = cw.executeQuery();
		cwResults.next();
		jsonResult.put("cw", cwResults.getInt("countterms"));
		
		PrintWriter out = res.getWriter();
		out.println(jsonResult.toJSONString().replace("\\", ""));
	}
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
		List<QueryResults> qresults;
		try {	Class.forName("org.postgresql.Driver");
		Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/"+"postgres", "postgres",
				"postgres");
	

		
		if(req.getParameter("query")!=null) {
			
		String term = req.getParameter("query");
		int [] parquery = getparquery(req.getParameter("k"),req.getParameter("score"));		
		HttpSession session = req.getSession();
		Query query=new Query(con,term,parquery[0],parquery[1]);	
		qresults = query.createQuery(false);

		
		
			
			//until here it is the same as RunQuery. Can be combined 
			
			
			JSONObject jsonResult= new JSONObject();
			
			//resultList part of JSON object
			JSONArray resultList = new JSONArray();
			
			for(int i =0; i< qresults.size(); i++ ) {
				JSONObject entry = new JSONObject();
				entry.put("rank",qresults.get(i).getRank());
				entry.put("score",qresults.get(i).getScore());
				entry.put("url", qresults.get(i).getUrl());
				resultList.add(entry);
			}
			
			jsonResult.put("resultList", resultList);
			
			//TODO:
			
			//query part of JSON object
			JSONObject jquery = new JSONObject();
			jquery.put("k", 5);
			jquery.put("query", term);
			jsonResult.put("query", jquery);
			
			//stat part of JSON object
			JSONArray stat = new JSONArray();
			// get df
			int[] tcount = new int[query.stemmedDisjunctive.size()];
			for (String ss: query.stemmedDisjunctive) {
				JSONObject entry = new JSONObject();
				entry.put("term",ss);
				PreparedStatement df = con.prepareStatement(
						"SELECT COUNT(docid) as Cdocid FROM features WHERE term = '" + ss + "'");
				// System.out.println("SELECT COUNT(docid) as Cdocid FROM features WHERE term =
				// '"+ stemmedWords[i] + "'");
				ResultSet dfs = df.executeQuery();
				dfs.next();
				entry.put("df",dfs.getInt("Cdocid"));
				stat.add(entry);
			}
			jsonResult.put("stat", stat);
			
			// get cw
			PreparedStatement cw = con.prepareStatement("SELECT COUNT(DISTINCT(term)) as countterms FROM features");
			ResultSet cwResults = cw.executeQuery();
			cwResults.next();
			jsonResult.put("cw", cwResults.getInt("countterms"));
			
			PrintWriter out = res.getWriter();
			out.println(jsonResult.toJSONString().replace("\\", ""));
			//out.println("The search term was: " + term + " \n");
			//out.println("Results:  \n");
		
		} 
		
		} catch (JWNLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

		doGet(request, response);

	}*/
}
