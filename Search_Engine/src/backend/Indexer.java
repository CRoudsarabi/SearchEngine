package backend;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

public class Indexer {

	/*
	 * indexes the url and returns a map for the term frequencies parameter
	 * verbose=true to show debug messages
	 */
	public IndexerResults indexURL(String urlString, boolean verbose) throws IOException {

		List<String> stopwords = Files.readAllLines(Paths.get("stopwords.txt"));
		String[] swords = stopwords.stream().toArray(String[]::new);

		URL url = new URL(urlString);
		URLConnection uc = url.openConnection();

		//read URL

		InputStreamReader input = new InputStreamReader(uc.getInputStream(), StandardCharsets.UTF_8);
		BufferedReader in = new BufferedReader(input);
		String inputLine;

		StringBuilder rawOutput = new StringBuilder();
		StringBuilder outputForLinks = new StringBuilder();
		StringBuilder outputForImages = new StringBuilder();
		boolean bodyflag = false;
		boolean pflag = false;
		// can use titleflag if we need title seperatly
		boolean titleflag = false;

		while ((inputLine = in.readLine()) != null) {
			
			outputForLinks.append(inputLine);
			outputForLinks.append("\n");
			
			outputForImages.append(inputLine);
			outputForImages.append("\n");
			
			// setflags if we find a tag
			if (inputLine.contains("<body"))
				bodyflag = true;
			if (inputLine.contains("<p>") || inputLine.contains("<p class"))
				pflag = true;
			if (inputLine.contains("<title"))
				pflag = true;

			if (bodyflag && pflag) {
				rawOutput.append(inputLine);
				rawOutput.append("\n");
			}

			if (inputLine.contains("</body"))
				bodyflag = false;
			if (inputLine.contains("</p>"))
				pflag = false;
			if (inputLine.contains("</title"))
				pflag = false;
			
			
		}

		in.close();

		//removing HTML tags
		String noHTMLOutput = removeHTML(rawOutput);
		
	
		
		//to lower case and remove punctuation
		String cleanedOutput = cleanOutput(noHTMLOutput);
		
		if (verbose)System.out.println("No HTML:\n" + noHTMLOutput);
		if (verbose)System.out.println("Cleaned Text:\n" + cleanedOutput);

		double wordCountBefore = countWords(cleanedOutput);
		
		String noStopwords =cleanedOutput;
		
		// remove stopwords
		for (String s : swords) {
			noStopwords = noStopwords.replaceAll(" " + s + " ", " ");
		}
		if (verbose)
			System.out.println("Text without stopwords:\n" + cleanedOutput);

		double wordCountAfter = countWords(noStopwords);

		String language;
		String stemmedOutput;
		
		if (verbose) System.out.println((wordCountAfter/wordCountBefore));
		if((wordCountAfter/wordCountBefore) < 0.8) {
			language="english";
			stemmedOutput =stemOutput(noStopwords);
		}
		else {
			language="german";
			stemmedOutput =cleanedOutput;
		}


		if (verbose)
			System.out.println(stemmedOutput);

		// calculate term frequencies
		Map<String, Integer> tf = calculateTermFrequencies(stemmedOutput);

	
		// show terms with tf > 5
		if (verbose) {
			System.out.println(tf);
			Map<String, Integer> largetf = new HashMap<>();
			for (Map.Entry<String, Integer> e : tf.entrySet()) {
				if (e.getValue() > 5)
					largetf.put(e.getKey(), e.getValue());
			}
			System.out.println(largetf);
		}
		
		//get images
		 Map<String, Map<String,Double>> images =getImages(outputForImages);
		
		//contains(<img class
		
		// get links
		Set<String> outgoingLinks = getLinks(outputForLinks);


		
		if (verbose) {
			System.out.println("number of valid links: " + outgoingLinks.size());
			System.out.println("number of valid images: " + images.size());
			
			
			System.out.println("Link URLs: ");
			for(String s : outgoingLinks) {
				//System.out.println(s);
			} 
			
			System.out.println("Image URLs:");
			for(Map.Entry<String, Map<String, Double>> entry : images.entrySet()) {
				System.out.println(entry.getKey());
				System.out.println(entry.getValue());
			}
		}
		
		IndexerResults results = new IndexerResults(tf, outgoingLinks,language,images,noHTMLOutput);
		return results;

	}
	


	public Map<String, Map<String,Double>> getImages (StringBuilder sb){
		Map<String, Map<String,Double>> images = new HashMap();
		String imageText = sb.toString();
		
		 List<String> imageUrls= new ArrayList<String>();
		 List<Map<String,Double>> imageTerms= new ArrayList<Map<String,Double>>();
		 Matcher tagMatch = Pattern.compile("<img .*?src=\"(.*?)\".*?>(.{512})",Pattern.DOTALL)
		     .matcher(imageText);
		 /*Matcher tagMatch = Pattern.compile("(.{512})<img .*?src=\"(.*?)\".*?>(.{512})",Pattern.DOTALL)
			     .matcher(imageText);*/
		 while (tagMatch.find()) {
		   imageUrls.add(tagMatch.group(1));
		   String terms = tagMatch.group(2);
		   terms = removeHTML(terms);
		   terms = cleanOutput(terms);
		   terms = stemOutput(terms);
		   Map<String,Double> tf = exponetialDistribution(terms,1.0);
		   
		   imageTerms.add(tf);
		 }
		 
		 
		 if (imageUrls.size() != imageTerms.size()) {
			System.out.println("Error: noOfURls "+imageUrls.size() +" not equal noOfTermLists "+ imageTerms.size()); 
		 }
		 for(int i =0; i< imageUrls.size(); i++ ){
			 Map<String,Integer> terms = new HashMap();
			 images.put(imageUrls.get(i), imageTerms.get(i));
		 }
		 
		 return images;
		
	}
	
	public Set<String> getLinks(StringBuilder sb) throws MalformedURLException {
		Set<String> links = new HashSet();
		String linkText = sb.toString();

		List<String> linkTags = new ArrayList<String>();
		Matcher tagMatch = Pattern.compile("<a .*?>").matcher(linkText);
		while (tagMatch.find()) {
			linkTags.add(tagMatch.group());
		}

		List<String> linkUrls = new ArrayList<String>();
		Matcher urlMatch = Pattern.compile("href=\".*?\"").matcher(String.join(" ", linkTags));
		while (urlMatch.find()) {
			String linkUrl = urlMatch.group();
			linkUrl = linkUrl.substring(6, linkUrl.length() - 1);
			linkUrls.add(linkUrl);
		}

		for (String url : linkUrls) {
			if (url.contains("https:")) {
				String host = Crawler.getHost(url);
				String path = Crawler.getPath(url);
				if(host != null) {
					links.add("https://" + host + path);
				}
				
			}

		}

		return links;

	}
	public String removeHTML(StringBuilder raw) {

		String cleanedOutput = raw.toString().replaceAll("<.*?>", " ");
		return cleanedOutput;
	}
	
	public String removeHTML(String raw) {
		String cleanedOutput = raw.replaceAll("<.*?>", " ");
		return cleanedOutput;
	}
	public static String cleanOutputterm(String raw) {
		// remove and-sign and quotation marks
		String cleanedOutput = raw.replaceAll("&amp;", " ");
		cleanedOutput = cleanedOutput.replaceAll("&#34;", " ");
		cleanedOutput = cleanedOutput.replaceAll("&#x27;", " ");

		// remove punctuation
		//cleanedOutput = cleanedOutput.replaceAll("\\p{Punct}", " ");
		//cleanedOutput = cleanedOutput.replaceAll("[�����]", "");
		//cleanedOutput = cleanedOutput.replaceAll("[^A-Za-z]", " ");

		// remove case
		cleanedOutput = cleanedOutput.toLowerCase();

		return cleanedOutput;
	}
	public static String cleanOutput(String raw) {
		// remove and-sign and quotation marks
		String cleanedOutput = raw.replaceAll("&amp;", " ");
		cleanedOutput = cleanedOutput.replaceAll("&#34;", " ");
		cleanedOutput = cleanedOutput.replaceAll("&#x27;", " ");

		// remove punctuation
		cleanedOutput = cleanedOutput.replaceAll("\\p{Punct}", " ");
		cleanedOutput = cleanedOutput.replaceAll("[�����]", "");
		//cleanedOutput = cleanedOutput.replaceAll("[^A-Za-z]", " ");

		// remove case
		cleanedOutput = cleanedOutput.toLowerCase();

		return cleanedOutput;
	}

	public Map<String, Integer> calculateTermFrequencies(String input) {
		Map<String, Integer> tf = new HashMap<>();
		String[] stemmedWords = input.split("\\s+");
		for (String s : stemmedWords) {
			//remove terms that are too long and terms that are blank
			if (s.length() < 127 && !s.equals(" ") && !s.equals("")) {
				if (tf.containsKey(s)) {
					int freq = tf.get(s);
					tf.put(s, freq + 1);
				} else {
					tf.put(s, 1);
				}
			}

		}
		return tf;
	}
	
	public Map<String, Double> exponetialDistribution(String input, double lamda) {
		Map<String, Double> tf = new HashMap<>();
		String[] stemmedWords = input.split("\\s+");
		int numberOfWords = 32;
		if(stemmedWords.length < 32) {
			numberOfWords = stemmedWords.length;
		}

		for (int i =0; i<numberOfWords-1; i++) {
			//remove terms that are too long and terms that are blank
			String s = stemmedWords[i+1];
			if (s.length() < 127 && !s.equals(" ") && !s.equals("")) {
				double value = lamda*Math.exp(-lamda*i);
				if (tf.containsKey(s)) {
					double current_val = tf.get(s);
					tf.put(s, current_val + value);
				} else {
					tf.put(s, value);
				}
			}

		}
		return tf;
	}
	
	public String stemOutput(String input) {
		
		// stemming
		StringBuilder stemBuilder = new StringBuilder();

		String[] words = input.split("\\s+");
		for (String s : words) {
			Stemmer stemmer = new Stemmer();
			char[] chars = s.toCharArray();
			stemmer.add(chars, s.length());
			stemmer.stem();
			stemBuilder.append(stemmer.toString());
			stemBuilder.append(" ");
			// System.out.println(stemmer.toString() + " original: "+s);
		}

		String stemmedOutput = stemBuilder.toString();
		return stemmedOutput;
		
	}
	
	 public static int countWords(String input) {
		    String[] words = input.split("\\s+");
		    return words.length;
		  }

}
