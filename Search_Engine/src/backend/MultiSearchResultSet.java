package backend;

public class MultiSearchResultSet implements Comparable<MultiSearchResultSet> {

	public double score;
	public String url;
	public String vm;
	
	
	public MultiSearchResultSet(double score, String url,String vm) {
		this.score=score;
		this.url=url;
		this.vm=vm;
	}
	
	@Override
	public int compareTo(MultiSearchResultSet o) {
		// TODO Auto-generated method stub
		return Double.compare(o.score,this.score);
	}

}
