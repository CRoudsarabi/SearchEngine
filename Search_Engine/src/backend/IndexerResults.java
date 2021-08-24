package backend;

import java.util.Map;
import java.util.Set;

public class IndexerResults {
	private Map<String, Integer> termFrequency;
	private Set<String> links;
	private String language;
	private Map<String, Map<String, Double>> images;
	private String txt;

	public IndexerResults(Map<String, Integer> term_frequency, Set<String> links, String lang,
			Map<String, Map<String, Double>> images, String txt) {
		this.setTermFrequency(term_frequency);
		this.setLinks(links);
		this.setLanguage(lang);
		this.setImages(images);
		this.setTxt(txt);
	}

	public Map<String, Integer> getTermFrequency() {
		return termFrequency;
	}

	public void setTermFrequency(Map<String, Integer> termFrequency) {
		this.termFrequency = termFrequency;
	}

	public Set<String> getLinks() {
		return links;
	}

	public void setLinks(Set<String> links) {
		this.links = links;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getTxt() {
		return txt;
	}

	public void setTxt(String txt) {
		this.txt = txt;
	}

	public Map<String, Map<String, Double>> getImages() {
		return images;
	}

	public void setImages(Map<String, Map<String, Double>> images) {
		this.images = images;
	}
}
