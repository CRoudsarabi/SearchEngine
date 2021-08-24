package web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import backend.Adquery;

import net.sf.extjwnl.JWNLException;

public class Viewads  extends HttpServlet {

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
	
		try {
			Class.forName("org.postgresql.Driver");
			Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres",
					"postgres");
			String term = req.getParameter("term");
			String company = req.getParameter("company");
			String url = req.getParameter("url");
			String description = req.getParameter("description");
			double budget = Double.parseDouble(req.getParameter("budget"));
			double costperclick = Double.parseDouble(req.getParameter("costperclick"));
			String imageurl = req.getParameter("imageLink");

			// System.out.println(statement);
			HttpSession session = req.getSession();
			//Connection con = (Connection) session.getAttribute("con");
			Adquery.insertnewad(con, term, company, description, url, budget, costperclick, imageurl);
			res.sendRedirect("addingads.html");

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {

			e.printStackTrace();
		}

	}
	
	

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		doPost(request, response);

	}
}
