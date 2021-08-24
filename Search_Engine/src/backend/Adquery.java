package backend;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

public class Adquery {
	private int adid;
	private String term;
	private String company;
    private String addescription;
    private String adurl;
    private double adbudget;
    private double costperclick;
    private String adimageurl;
    private double currentclicks; 
	 public Adquery(String term, String company, String addescription, String adurl, double adbudget, double costperclick) {
	        this.setTerm(term);
	        this.setCompany(company);
	        this.setAddescription(addescription);
	        this.setAdurl(adurl);
	        this.setAdbudget(adbudget);
	        this.setCostperclick(costperclick);
	    }
	 public Adquery(String term, String company, String addescription, String adurl, double adbudget, double costperclick,String imagelink) {
	        this.setTerm(term);
	        this.setCompany(company);
	        this.setAddescription(addescription);
	        this.setAdurl(adurl);
	        this.setAdbudget(adbudget);
	        this.setCostperclick(costperclick);
	        this.setAdimageurl(imagelink);
	    }
	 public Adquery(int adid, String term, String company, String addescription, String adurl, double adbudget, double costperclick,String imagelink,double currentclicks) {
	        this.setAdid(adid);
	        this.setTerm(term);
	        this.setCompany(company);
	        this.setAddescription(addescription);
	        this.setAdurl(adurl);
	        this.setAdbudget(adbudget);
	        this.setCostperclick(costperclick);
	        this.setAdimageurl(imagelink);
	        this.setCurrentclicks(currentclicks);
	    }
	 public static void insertnewad(Connection con,String term, String company, String addescription, String adurl, double adbudget, double costperclick,String imagelink){
	      String query = "INSERT INTO public.ads(\r\n" + 
	      		" terms,company, addescription,adlink,budget, clickcost,imagelinks)\r\n" + 
	      		"	VALUES (?, ?, ?, ?, ?, ?,?);";

	    	      // create the mysql insert preparedstatement
	    	      PreparedStatement preparedStmt;
				try {
					preparedStmt = con.prepareStatement(query);
				
	    	      preparedStmt.setString(1, term);
	    	      preparedStmt.setString(2, company);
	    	      preparedStmt.setString(3, addescription);
	    	      preparedStmt.setString(4, adurl);
	    	      preparedStmt.setDouble(5, adbudget);
	    	      preparedStmt.setDouble(6, costperclick);
	    	      preparedStmt.setString(7, imagelink);
	    	      // execute the preparedstatement
	    	     preparedStmt.execute() ;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	 public static ArrayList<Adquery>  viewad(Connection con,String term){
		 ArrayList<Adquery> ads= new ArrayList<Adquery>();
	      String query = "SELECT adlink, adsid, budget, clickcost, terms, company, addescription, currentclicks, imagelinks\r\n" + 
	      		"	FROM public.ads where terms like CONCAT('%'," + "?" + ",'%') ";

	    	      // create the mysql insert preparedstatement
	    	      PreparedStatement preparedStmt;
				try {
					preparedStmt = con.prepareStatement(query);
				
	    	      preparedStmt.setString(1, term);

	    	      ResultSet sResults = preparedStmt.executeQuery();	
	 //String term, String company, String addescription, String adurl, double adbudget, double costperclick,String imagelink
	  			while (sResults.next()) {
	  		//		int adid, String term, String company, String addescription, String adurl, double adbudget, double costperclick,String imagelink,double currentclicks
	  				Adquery u = new Adquery(sResults.getInt("adsid"),sResults.getString("terms"), sResults.getString("company"),sResults.getString("addescription"),sResults.getString("adlink"),sResults.getDouble("budget"),
	  						sResults.getDouble("clickcost"),  sResults.getString("imagelinks"),  sResults.getDouble("currentclicks")
	  						);
	  				ads.add(u);
	  			}
	  		

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return ads;
	        }
	public int getAdid() {
		return adid;
	}

	public void setAdid(int adid) {
		this.adid = adid;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getAddescription() {
		return addescription;
	}

	public void setAddescription(String addescription) {
		this.addescription = addescription;
	}

	public String getAdurl() {
		return adurl;
	}

	public void setAdurl(String adurl) {
		this.adurl = adurl;
	}

	public double getAdbudget() {
		return adbudget;
	}

	public void setAdbudget(double adbudget) {
		this.adbudget = adbudget;
	}

	public double getCostperclick() {
		return costperclick;
	}

	public void setCostperclick(double costperclick) {
		this.costperclick = costperclick;
	}

	public String getAdimageurl() {
		return adimageurl;
	}

	public void setAdimageurl(String adimageurl) {
		this.adimageurl = adimageurl;
	}
	public double getCurrentclicks() {
		return currentclicks;
	}
	public void setCurrentclicks(double currentclicks) {
		this.currentclicks = currentclicks;
	}
	

}
