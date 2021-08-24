package backend;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.la4j.Vector;
import org.la4j.matrix.sparse.CRSMatrix;
import org.la4j.vector.sparse.CompressedVector;

public class PageRank {

	// random jump prob
	private double alpha = 0.10;

	// Stopping criteria
	// Option 1: fixed number of iterations
	private int no_of_iterations = 50;
	// Option 2: threshold for the difference between matrices between iterations
	// (not yet implemented)
	private double epsilon = 0.00001;

	private DBconnection myCon;

	public PageRank(DBconnection con) {
		this.myCon = con;
	}

	public void calculatePageRank(boolean verbose) throws SQLException {

		// calculate the total number of Documents
		PreparedStatement count = myCon.con.prepareStatement("SELECT COUNT(docid) FROM documents");
		ResultSet result = count.executeQuery();
		result.next();
		int doc_count = result.getInt("count");
		if (verbose)
			System.out.println("doc_count: " + doc_count);

		// create matrix
		CRSMatrix sparseMatrix = new CRSMatrix(doc_count, doc_count);
		int[] outdegree = new int[doc_count];

		/*
		 * Filling the matrix where a link exists. index i corresponds to the document
		 * with docid i-1
		 */
		for (int i = 0; i < outdegree.length; i++) {
			
			PreparedStatement getLinks = myCon.con.prepareStatement("SELECT * FROM links WHERE from_docid=?");
			getLinks.setInt(1, i + 1);

			ResultSet links = getLinks.executeQuery();
			int degree = 0;
			List<Integer> to_docids = new ArrayList<Integer>();
			while (links.next()) {
				degree++;
				int to_docid = links.getInt("to_docid");
				to_docids.add(to_docid);

			}
			outdegree[i] = degree;
			double value =((1.0-alpha) / degree);
			for (Integer e : to_docids) {
				sparseMatrix.set(i, (e - 1), value);
			}

		}
		if (verbose) {
			System.out.println("Density of the matrix: " + sparseMatrix.density());
			System.out.println("Rows of the matrix: " + sparseMatrix.rows());
		}

		if(sparseMatrix.rows() < 100)System.out.println("Matrix: \n" + sparseMatrix.toString());
		// Setting random jump prob
		double leap_prob_nonzero = (alpha / doc_count);
		double leap_prob_zero = (1.0 / doc_count);

		// Power method
		double[] ranks = new double[doc_count];
		double[] newRanks = new double[doc_count];

		// first move: first element is one rest is zero
		Arrays.fill(ranks, 0);
		ranks[0] = 1;

		//iteration until convergence
		for (int i = 0; i < no_of_iterations; i++) {
			//Vector * Matrix multiplication
			for (int j = 0; j < doc_count; j++) {
				Vector current_row = sparseMatrix.getColumn(j);
				double jvalue=0;
				for (int k = 0; k < doc_count; k++) {
					double kvalue = current_row.get(k);
					//add random jump prob depending on the outdegree
					if(outdegree[k]==0) {
						kvalue = kvalue + leap_prob_zero;
					}
					else {
						kvalue = kvalue + leap_prob_nonzero;
					}
					//System.out.println(i+"  "+j+ " kvalue: " + kvalue);
					//System.out.println(i+"  "+j+ " ranks[k]: " + ranks[k]);
					//System.out.println(i+"  "+j+ " kvalue*ranks[k]: " + kvalue *ranks[k]);
					jvalue += kvalue *ranks[k];
				}
				//System.out.println(i+"  "+j+ " jvalue: " + jvalue);
				newRanks[j]= jvalue;
			}
			//at the end of the iteration
			
			ranks= newRanks.clone();
			//System.out.println(i+" ranks: " + Arrays.toString(ranks));
			//System.out.println(i+" newRanks: " + Arrays.toString(newRanks));
		}

		
		
		//add values to database
		for(int i = 0; i < ranks.length; i++) {
			PreparedStatement setPR = myCon.con.prepareStatement("UPDATE documents SET pagerank = ? WHERE docid=?");
			setPR.setDouble(1, ranks[i]);
			setPR.setInt(2, i + 1);
			setPR.executeUpdate();
		}
		
	}
}
