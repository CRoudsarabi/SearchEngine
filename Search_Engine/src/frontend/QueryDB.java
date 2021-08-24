package frontend;

import backend.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryDB {

	public static void main(String[] args) {

		if (args.length == 5) {
			String host = args[0];
			String port = args[1];
			String database = args[2];
			String username = args[3];
			String password = args[4];
			/*String host = "localhost";
			String port = "5432";
			String database = "postgres";
			String username = "postgres";
			String password = "postgres";*/
			Credentials cred= new Credentials(host,port,database,username,password);
			DBconnection myCon = new DBconnection();
			try {
				myCon.initialize(cred);
				Connection con = myCon.getCon();
				ArrayList<QueryResults> rs=	createQuery(con);
				 printresults(rs);
			} catch (SQLException e) {
				e.printStackTrace();
			}


		}
	}
	public static ArrayList<QueryResults> createQuery(Connection con){
		InputsPar ip = new InputsPar();
		Set<String> stemmedDisjunctive = new HashSet<>();
		ArrayList<QueryResults> qResults = new ArrayList<>();
		ip.setInputs();
		PreparedStatement search;
		try {
			search = con.prepareStatement(ip.setPreparedStatement());
	
		for (int i = 1; i <= ip.term().length; i++) {
			search.setString(i, ip.term()[i - 1]);
			System.out.println(i + " "+ip.term()[i - 1] );
		}
		System.out.println(search);
		ResultSet sResults = search.executeQuery();
	
		stemmedDisjunctive.add(ip.getInputs()[0]);
		while (sResults.next()) {
			QueryResults u = new QueryResults(sResults.getInt("docid"), sResults.getFloat("totalScore"), sResults.getInt("docrank"),sResults.getString("url"));
			qResults.add(u);
		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return qResults;
	}
	public static void printresults(ArrayList<QueryResults> qResults){
		if(qResults.isEmpty()) {

		}
		for (QueryResults rs : qResults) {

			System.out.println("Document ID = " + rs.getDocId() + " with url " + rs.getUrl() + " rank " + rs.getRank()
			+ " and total score " + rs.getScore());

		}		

	}

}


