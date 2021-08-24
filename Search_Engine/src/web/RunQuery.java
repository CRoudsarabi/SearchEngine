package web;

import backend.*;
import net.sf.extjwnl.JWNLException;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;



public class RunQuery extends HttpServlet {
	
	String error;
	List<QueryResults> qresults;
	Connection con ;
	int k,score;
	QueryType queryType;

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

		try {
			Class.forName("org.postgresql.Driver");
			con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + "postgres", "postgres",
					"postgres");
			// get the mapped url
			QueryType queryType = extractQueryType(req.getServletPath());
			if (req.getParameter("query") != null) {
				String term = req.getParameter("query");
				k=getK(req.getParameter("k"));
				score=getScore(req.getParameter("score"));
				Query query = new Query(con, term, k, score);

				qresults = query.createQuery(queryType);
				error = null;

				// spellcheck if query has no result
				if (qresults.isEmpty() && (queryType == QueryType.IMAGE || queryType == QueryType.SIMPLE)) {
					qresults=performSpellcheck(query);
				}
				// check the url pattern and redirect it
				switch (queryType) {
				case SIMPLE:
					printSimpleQuery(req, res, term, qresults, query.stemmedDisjunctive, error);
					break;
				case JSON:
					printJsonQuery(res, con, term, qresults, query.stemmedDisjunctive);
					break;
				case MULTI:
					printSimpleQuery(req, res, term, qresults, query.stemmedDisjunctive, error);
					break;
				case IMAGE:
					printImageQuery(req, res, term, qresults, query.stemmedDisjunctive, error);
					break;
				default:
					;
				}

			}
		} catch (JWNLException | SQLException | ClassNotFoundException e1) {
			e1.printStackTrace();
			qresults = null;
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		doPost(request, response);

	}

	public QueryType extractQueryType(String path) {
		switch (path) {
		case "/docsearch":
			return QueryType.SIMPLE;
		case "/json":
			return QueryType.JSON;
		case "/imagesearch":
			return QueryType.IMAGE;
		case "/multisearch":
			return QueryType.MULTI;
		default:
			return null;
		}

	}
	
	public int getK(String k) {
		if(k != null) {
			return Integer.parseInt(k);
		}
		else {
			 return 20;
		}
	}
	
	public int getScore(String score) {
		if(score != null) {
			return Integer.parseInt(score);
		}
		else {
			 return 1;
		}
	}

	public ArrayList<QueryResults> performSpellcheck(Query query) throws JWNLException, SQLException {
		
			String newTerms = "";
			for (String word : query.stemmedDisjunctive) {
				String stmt;
				if(queryType == QueryType.IMAGE) {
					stmt = "SELECT * FROM image_features WHERE term='" +word+"'";
				}
				else {
					stmt = "SELECT * FROM features WHERE term='" + word + "'";
				}
				PreparedStatement testIfExists = con.prepareStatement(stmt);
				ResultSet testRS = testIfExists.executeQuery();
				if (!testRS.next()) {
					// spellcheck each term that wasnt found in db
					error = " Term " + word + " found no results <br>";
					if (!newTerms.equals(""))
						newTerms += " ";
					newTerms += Query.spellcheck(con, word, queryType);
				} else {
					newTerms += word;
				}
			}
			error += "New Query with terms: " + newTerms;
			Query spellcheckedQuery = new Query(con, newTerms, k, score);
			return spellcheckedQuery.createQuery(queryType);
		
	}

	

	public void printSimpleQuery(HttpServletRequest req, HttpServletResponse res, String term,
			List<QueryResults> qresults, Set<String> stemmedDisjunctive, String error) {
		req.setAttribute("documents", qresults);
		req.setAttribute("term", term);
		req.setAttribute("words", stemmedDisjunctive);
		req.setAttribute("error", error);
		String nextjsp = "/docsearch.jsp";
		RequestDispatcher s = getServletContext().getRequestDispatcher(nextjsp);
		try {
			s.forward(req, res);
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
	}

	public void printImageQuery(HttpServletRequest req, HttpServletResponse res, String term,
			List<QueryResults> qresults, Set<String> stemmedDisjunctive, String error) {
		req.setAttribute("images", qresults);
		req.setAttribute("term", term);
		req.setAttribute("words", stemmedDisjunctive);
		req.setAttribute("error", error);
		String nextjsp = "/ImageQuery.jsp";
		RequestDispatcher s = getServletContext().getRequestDispatcher(nextjsp);
		try {
			s.forward(req, res);
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
	}

	// print the jsonfile
	public void printJsonQuery(HttpServletResponse res, Connection con, String term, List<QueryResults> qresults,
			Set<String> stemmedDisjunctive) throws SQLException, IOException {

		JSONObject jsonResult = new JSONObject();

		// resultList part of JSON object
		JSONArray resultList = new JSONArray();

		for (int i = 0; i < qresults.size(); i++) {
			JSONObject entry = new JSONObject();
			entry.put("rank", qresults.get(i).getRank());
			entry.put("score", qresults.get(i).getScore());
			entry.put("url", qresults.get(i).getUrl());
			resultList.add(entry);
		}

		jsonResult.put("resultList", resultList);

		// TODO:

		// query part of JSON object
		JSONObject jquery = new JSONObject();
		jquery.put("k", 5);
		jquery.put("query", term);
		jsonResult.put("query", jquery);

		// stat part of JSON object
		JSONArray stat = new JSONArray();
		// get df
		int[] tcount = new int[stemmedDisjunctive.size()];
		for (String ss : stemmedDisjunctive) {
			JSONObject entry = new JSONObject();
			entry.put("term", ss);
			PreparedStatement df = con
					.prepareStatement("SELECT COUNT(docid) as Cdocid FROM features WHERE term = '" + ss + "'");
			// System.out.println("SELECT COUNT(docid) as Cdocid FROM features WHERE term =
			// '"+ stemmedWords[i] + "'");
			ResultSet dfs = df.executeQuery();
			dfs.next();
			entry.put("df", dfs.getInt("Cdocid"));
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

}
