package backend;

import org.postgresql.util.PSQLException;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Node;
import org.w3c.tidy.Tidy;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.*;

public class Crawler implements Runnable {

	private int maximum_number_of_documents;
	private boolean leave_domain;
	private boolean verbose;
	private Map<String, Integer> recentDomains;
	private Credentials cred;

	public Crawler(int maximum_number_of_documents, boolean leave_domain, boolean verbose, Credentials cred) {

		this.maximum_number_of_documents = maximum_number_of_documents;
		this.leave_domain = leave_domain;
		this.verbose = verbose;
		this.cred = cred;
		recentDomains = new HashMap<>();

	}

	@Override
	public void run() {
		Indexer index = new Indexer();

		System.out.println("Thread started");

		DBconnection myCon = new DBconnection();

		try {
			myCon.initialize(cred);

			for (int counter = 0; counter < maximum_number_of_documents; counter++) {

				// after X amount of websites indexed we can lift the domain restrictions
				if ((counter % 250) == 0) {
					recentDomains = new HashMap<>();
					if (verbose)
						System.out.println("reset Domain restrictions");
				}
				String entering = null;
				int from_docid = -1;
				URL url = null;

				while (from_docid == -1) {
					myCon.con.setAutoCommit(false);
					myCon.con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
					Savepoint save = myCon.con.setSavepoint();

					try {

						// search for an uncrawled URL while being nice (do not enter same domain too
						// often)

						boolean validUrlFound = false;
						ResultSet possibleUrls = myCon.uncrawledDocuments();
						while (!validUrlFound && possibleUrls.next()) {
							String uncrawledUrl = possibleUrls.getString("url");
							if (canIEnterThisDomain(getHost(uncrawledUrl))) {
								entering = uncrawledUrl;
								validUrlFound = true;
								crawledThisDomain(getHost(entering));

								// enter and index the URL
								url = new URL(entering);
								from_docid = myCon.lookupDocID(entering);
								// System.out.println("indexind doc: " + from_docid);
								if (verbose)
									System.out.println("Entering " + url);
								myCon.setCrawled(from_docid);
								crawledThisDomain(url.getHost());
							} else {
								// if(verbose) System.out.println("Entered " + getHost(uncrawledUrl)+ " too many
								// times");
							}
						}

					} catch (PSQLException e) {
						myCon.con.rollback(save);
						from_docid = -1;
						entering = null;
						url = null;
						if (verbose)
							System.out.println("had to rollback");
					} catch (MalformedURLException e) {
						// e.printStackTrace();
					}

					myCon.con.commit();
					myCon.con.setAutoCommit(true);

				}

				try {
					IndexerResults result = index.indexURL(url.toString(), false);
					// enter data into database
					myCon.setLanguage(from_docid, result.getLanguage());
					myCon.setText(from_docid, result.getTxt());

					insertFeatures(result, from_docid, myCon);
					insertImages(result, myCon);

					Set<String> links = result.getLinks();
					addToQueue(links, myCon, from_docid);
					if (verbose)
						System.out.println("counter at: " + counter);

				} catch (IOException e) {
					// e.printStackTrace();
				}
			}

			System.out.println("Thread completed after indexing " + maximum_number_of_documents + " links");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static String getHost(String s) throws MalformedURLException {
		URL url = new URL(s);
		return url.getHost();

	}

	public static String getPath(String s) throws MalformedURLException {
		URL url = new URL(s);
		return url.getPath();

	}

	public void addToQueue(Set<String> startURLs, DBconnection myCon, int from_docid) throws SQLException {
		for (String entry : startURLs) {
			if (entry.length() < 510) {
				myCon.insertDocument(from_docid, entry, null);
			}
		}

	}

	private void insertFeatures(IndexerResults result, int from_docid, DBconnection myCon) throws SQLException {
		for (Map.Entry<String, Integer> entry : result.getTermFrequency().entrySet()) {
			if (from_docid != -1) {
				myCon.insertIntoFeatures(from_docid, entry.getKey(), entry.getValue());
			} else {
				System.out.println("from_docid was not correctly set");
			}
		}
	}

	private void insertImages(IndexerResults result, DBconnection myCon) throws SQLException {
		for (Map.Entry<String, Map<String, Double>> image : result.getImages().entrySet()) {
			String imageUrl = image.getKey();
			if (imageUrl.length() < 512) {
				if (verbose)
					System.out.println("adding image: " + imageUrl);
				boolean needToAddFeatures = myCon.insertImage(imageUrl);
				if (needToAddFeatures) {
					int imageid = myCon.lookupImageID(imageUrl);
					Map<String, Double> terms = image.getValue();
					for (Map.Entry<String, Double> entry : terms.entrySet()) {
						myCon.insertIntoImageFeatures(imageid, entry.getKey(), entry.getValue());
					}
				}
			}

		}

	}

	private void crawledThisDomain(String s) {
		if (recentDomains.containsKey(s)) {
			int freq = recentDomains.get(s);
			recentDomains.put(s, freq + 1);
		} else {
			recentDomains.put(s, 1);
		}

	}

	private boolean canIEnterThisDomain(String s) {
		if (recentDomains.containsKey(s)) {
			int freq = recentDomains.get(s);
			if (freq > 10)
				return false;
		}
		return true;
	}

}
