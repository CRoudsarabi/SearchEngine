package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Calendar;

import javax.servlet.http.*;

//+<%=SS.getUrl()%>
public class AdRedirect extends HttpServlet {
	Connection con;

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String uri = req.getParameter("url");
		System.out.println(uri);
		try {
			Class.forName("org.postgresql.Driver");
			Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres",
					"postgres");
		
			//java.sql.Date date = new java.sql.Date(Calendar.getInstance().getTime().getTime());
			//TODO add click to database

			String query = "INSERT INTO public.adclicks(\r\n" + 
					"	clickdate, adid)\r\n" + 
					"	VALUES (current_timestamp, (select adsid from ads where adlink =?) );";
			String query2 = "UPDATE public.ads\r\n" + 
					"	SET  currentclicks=currentclicks+1 \r\n" + 
					"	WHERE adlink =?;";
			// create the mysql insert preparedstatement
			PreparedStatement insertclick = connection.prepareStatement(query);
			PreparedStatement updateclick = connection.prepareStatement(query2);
			insertclick.setString(1, uri);
			updateclick.setString(1, uri);
			insertclick.execute() ;
			updateclick.execute() ;
			res.sendRedirect(uri);
		} catch (ClassNotFoundException e) {
			res.sendRedirect(uri);
			e.printStackTrace();
		} catch (SQLException e) {
			res.sendRedirect(uri);
			e.printStackTrace();
		}



	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
		doPost(req, res);
	}

}
