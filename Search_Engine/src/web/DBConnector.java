package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.http.*;


public class DBConnector extends HttpServlet {
	Connection con;

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String username = req.getParameter("username");
		String pw = req.getParameter("pw");
		String database = req.getParameter("Database");
		PrintWriter out = res.getWriter();
		try {
			initialize(database,username,pw);
			out.println("established connection to database");
			HttpSession session = req.getSession();
			session.setAttribute("con",con);
			res.sendRedirect("LandingPage.html");
		} catch (SQLException e) {
			out.println("SQL Exception: "+e.getMessage() + " state: " + e.getSQLState());
		}catch (ClassNotFoundException e) {
			
		}
		
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
		doPost(req, res);
	}

	public void initialize(String database, String username, String password) throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/"+database, username,
				password);
		this.con = connection;

	}
}
