package frontend;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import backend.Credentials;
import backend.DBconnection;
import backend.Shingling;

public class CreateShingles {
	public static void main(String[] args) {
		String host = args[0];
		String port = args[1];
		String database = args[2];
		String username = args[3];
		String password = args[4];
		
		int k = Integer.parseInt(args[5]);
		int minhash = Integer.parseInt(args[6]);
		int limit = Integer.parseInt(args[7]);


		Credentials cred = new Credentials(host, port, database, username, password);
		DBconnection myCon = new DBconnection();
		try {
			myCon.initialize(cred);

			Shingling.createShinglesTable(cred, k, minhash, limit);
			Shingling.createJaccardTable(cred, k);
			Shingling.createMinHashJaccardTable(cred, k);
			
			System.out.println("Created Shingles Tables");

		} catch (SQLException e) {
			System.out.println("Could not connect to database :" + e.getMessage());
		}
	}
}
