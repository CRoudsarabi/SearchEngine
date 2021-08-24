package backend;

import net.sf.extjwnl.*;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;
import com.optimaize.langdetect.*;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;

public class Synonyms {

	public static void main(String[] args) throws JWNLException, CloneNotSupportedException, IOException {
		Set<String> synonymSet = getSynonyms("aufbrechen");
		/*List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();

		//build language detector:
		LanguageDetector languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
		        .withProfiles(languageProfiles)
		        .build();

		//create a text object factory
		TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();

		//query:
		TextObject textObject = textObjectFactory.forText("trotzdem Tagung");
		Optional<LdLocale> lang = languageDetector.detect(textObject);
		List<DetectedLanguage>  sss = languageDetector.getProbabilities(textObject);
		System.out.println(sss.get(0).getLocale());**/
		
		for (String ff: synonymSet) {
			System.out.println("the word means "+ ff);

		}
		int limit = 20;
		Set<String> stemmedDisjunctive = new HashSet<>();
		String scoretype = "features_tfidf";
		String term= "opposition parties and injured students blamed sunday night’s violence on a student organisation linked to narendra modi’s bharatiya janata party \r\n" + 
				" floods in indonesia s capital city have left more than 60 people dead and forced tens of thousands to flee their homes \r\n" + 
				" police beat protesters with batons as they storm hong kong shopping centre \r\n" + 
				" indian police clashed with thousands of protesters who demonstrated nbsp  to oppose a new law they say discriminates against muslims \r\n" + 
				" students have condemned as  barbaric  the tactics of delhi police after they stormed a university campus to break up a peaceful protest  injuring dozens \r\n" + 
				" indian police storm main library of new delhi s jamia millia university on sunday  firing teargas at students barricaded inside  footage shot inside the library shows students scrambling over desks and climbing through smashed windows to escape \r\n" + 
				"";
		term = term.replaceAll("&amp;", " ");
		term = term.replaceAll("&#34;", " ");
		term = term.replaceAll("&#x27;", " ");
		term = term.replaceAll("  ", " ");
		// remove punctuation
		ArrayList<String> domainurl = new ArrayList<String>();
		String patternString1 = "((site:)(.*?)) ";
		Matcher sitematcher = Pattern.compile(patternString1).matcher(term);
		while (sitematcher.find()) {
			String ss = sitematcher.group();
			term = term.replaceAll(ss, "");
			ss = ss.replaceAll("site:", "");
			ss = ss.replaceAll(" ", "");
			domainurl.add(ss);

		}

		ArrayList<String[]> conarrayterms = new 	ArrayList<String[]>();
		Matcher matcher = Pattern.compile("“(.*?)”").matcher(term);
		while (matcher.find()) {
			String conterm = matcher.group().replaceAll("“", "");
			conterm = conterm.replaceAll("”", "");
			if(conterm.charAt(0)=='~') {
				conterm = conterm.replaceAll("~", "");
				conterm = conterm.replaceAll(" ", "");
				System.out.println("the word with lemma and qotation " +conterm );
				Set<String> conTermSynonyms = Synonyms.getSynonyms(conterm);
				String[] conSynonymsword= new String[conTermSynonyms.size()];
				conTermSynonyms.toArray(conSynonymsword);
				for (int i= 0; i< conSynonymsword.length;i++) {
					Stemmer stemmer = new Stemmer();
					char[] chars = conSynonymsword[i].toCharArray();
					stemmer.add(chars, conSynonymsword[i].length());
					stemmer.stem();
					conSynonymsword[i]= stemmer.toString();
				}
				conarrayterms.add(conSynonymsword);
			}else if(conterm.charAt(0)!='~')  {				
				String[] conwords= { conterm};
				conarrayterms.add(conwords);
			}
		}
		String conQueryTerm = "";
		if (conarrayterms.size()> 0) {
			conQueryTerm = "and "+scoretype+".docid in (SELECT distinct docid from "+scoretype +" where ( ";
			int allcontterm = conarrayterms.size(); 
			for (int f = 0; f< conarrayterms.size(); f++ ) {
				int termlenth = conarrayterms.get(f).length;
				for (int i= 0; i< termlenth; i++) {
					conQueryTerm = conQueryTerm + "term = '" + conarrayterms.get(f)[i]+"' ";
					if (i < termlenth-1) {
						conQueryTerm = conQueryTerm +" or ";
					}
				}
				if (f < allcontterm-1) {
					conQueryTerm =conQueryTerm+") and docid in \r\n" + 
							" (SELECT  docid  from "+scoretype+" where (" ;
				}else {
					conQueryTerm = conQueryTerm +")".repeat(allcontterm+1);
				}
			}
		}
		System.out.println(conQueryTerm);
		Set<String> disjunctive = new HashSet<>();

		term = term.replaceAll("“", "");
		term = term.replaceAll("”", " ");
		Matcher matcher12 = Pattern.compile("~(.*?) ").matcher(term);
		while (matcher12.find()) {
			String synonym = matcher12.group().replaceAll("~", "");
			synonym = synonym.replaceAll(" ", "");
			disjunctive.addAll(Synonyms.getSynonyms(synonym));

		}
		term = term.replaceAll("~", "");
		term = term.replaceAll("  ", " ");
		String[] disjunctivewords = term.split("\\s+");
		for(String word :disjunctivewords) {
			disjunctive.add(word);
		}
		for (String chr : disjunctive) {
			Stemmer stemmer = new Stemmer();
			char[] chars = chr.toCharArray();
			stemmer.add(chars, chr.length());
			stemmer.stem();
			stemmedDisjunctive.add(stemmer.toString());
		}
		String site = "";
		if (domainurl.size() > 0) {
			site = " and "+scoretype+".url like CONCAT('%',"+"?"+ ",'%') ";

		}
		String whereStatement = "term = ?  ";
		for (String ss: stemmedDisjunctive) {
			whereStatement = whereStatement + " or term = '"+ ss + "' " ;
		}
		String query = "select a.docid, a.totalScore ,rank() over (order by totalScore desc) as docrank,url as url, txt as txt from \n"
				+ "(SELECT  "+scoretype+".docid, sum(score)as totalScore, documents.url as url,documents.txt as txt from "+scoretype+ " left join documents on documents.docid= "+scoretype+".docid \n" + "where (\n" + whereStatement + ") "
				+ conQueryTerm + site+ "group by "+scoretype+".docid,documents.url,documents.txt \n" + ") as a \n" + "order by docrank  limit \n" + limit;

		System.out.println(query);
	}
	public static Set<String> getSynonyms(String word) throws JWNLException {
	/*String language ="German";
		Dictionary dictionary ;
		if(language == "German") {
			dictionary= Dictionary.getFileBackedInstance("openthesaurus.txt");
		}else {
			dictionary = Dictionary.getDefaultResourceInstance();
		}
		*/Dictionary dictionary   = Dictionary.getDefaultResourceInstance();
		ArrayList<IndexWord> searchedword = new ArrayList<IndexWord>();
		searchedword.add(dictionary.getIndexWord(POS.VERB, word));
		searchedword.add(dictionary.getIndexWord(POS.NOUN, word));
		searchedword.add(dictionary.getIndexWord(POS.ADJECTIVE, word));
		searchedword.add(dictionary.getIndexWord(POS.ADVERB, word));
		Set<String> synonymSet = new HashSet<>();
		for(IndexWord searchwordtype:searchedword )  {  
			if  (searchwordtype!= null)  {
				for (Synset synset : searchwordtype.getSenses()) {
					for (Word phrase : synset.getWords()) {
						synonymSet.add(phrase.getLemma());
					} 
					if (synonymSet.size()==0) {
						ArrayList<IndexWord> searchedwordingerman = new ArrayList<IndexWord>();
						Dictionary germanDictionary   = Dictionary.getFileBackedInstance("openthesaurus.txt");
						searchedwordingerman.add(germanDictionary.getIndexWord(POS.VERB, word));
						searchedwordingerman.add(germanDictionary.getIndexWord(POS.NOUN, word));
						searchedwordingerman.add(germanDictionary.getIndexWord(POS.ADJECTIVE, word));
						searchedwordingerman.add(germanDictionary.getIndexWord(POS.ADVERB, word));
						for(IndexWord searchwordtypegerman:searchedword )  {  
							if  (searchwordtypegerman!= null)  {
								for (Synset synsetgerman : searchwordtypegerman.getSenses()) {
									for (Word phrase : synsetgerman.getWords()) {
										synonymSet.add(phrase.getLemma());
									} 
								}
							}
						}
					}
				}
			}		
		}return synonymSet;
	}

}


