package frontend;

import java.sql.SQLException;

import backend.Credentials;
import backend.DBconnection;

public class ResetDatabase {

	public static void main(String[] args) {
		String host = args[0];
		String port = args[1];
		String database = args[2];
		String username = args[3];
		String password = args[4];

		Credentials cred= new Credentials(host,port,database,username,password);
		DBconnection myCon=new DBconnection();
		try {
		    myCon.initialize(cred);
            myCon.dropTables();
            System.out.println("Tables dropped");
		}
	   catch (SQLException e) {
		System.out.println("Could not connect to database :" + e.getMessage());
	   }
	}

}
