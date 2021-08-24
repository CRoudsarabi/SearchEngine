package web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.*;

public class RunMultiQuery extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
		boolean[] whichVM = {true,true,true,true};
		String term = req.getParameter("query");
		term = Indexer.cleanOutputterm(term);
		term = term.replaceAll("  ", " ");
		String[] terms = term.split("\\s+");
		
		ArrayList<String> links = MultiSearchQuery.constructLinks(terms,whichVM);
		MultiSearchQueryResults msqr = MultiSearchQuery.readJson(links);
		
		double[] totalScore= new double[msqr.dfList.size()];
		for(String s : terms) {
			double[] score = MultiSearchQuery.calculateCORI(s, msqr);
			for(int i =0; i<msqr.dfList.size(); i++) {
				totalScore[i] += score[i];
			}
		}
		
		ArrayList<MultiSearchResultSet> mergedResults= MultiSearchQuery.mergeResults(totalScore, msqr);
		Collections.sort(mergedResults);
		
		/*for(int i= 0; i<mergedResults.size(); i++ ) {
			System.out.println(mergedResults.get(i).score+ " " +mergedResults.get(i).url);
		}*/
		
		req.setAttribute("results", mergedResults);
		
		String nextjsp = "/MultiQuery.jsp";
		RequestDispatcher s = getServletContext().getRequestDispatcher(nextjsp);
		try {
			s.forward(req, res);
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		doPost(request, response);

	}
}
