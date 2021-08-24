package backend;

import java.util.Arrays;

public class Levenshtein {

	public static int calculateLevenshteinDistance(String text1, String text2) {
		
		if(text1.length()== 0 || text2.length()== 0 ) {
			return Math.max(text1.length(),text2.length());
		}
		int editCost;
		if(text1.charAt(0) == text2.charAt(0)) {
			editCost=0;
		}
		else {
			editCost=1;
		}
		int[] recursionValues = new int[3];
		recursionValues[0]=calculateLevenshteinDistance(text1.substring(1),text2.substring(1))+editCost;
		recursionValues[1]=calculateLevenshteinDistance(text1.substring(1),text2)+1;
		recursionValues[2]=calculateLevenshteinDistance(text1,text2.substring(1))+1;
		return Arrays.stream(recursionValues).min().orElse(Integer.MAX_VALUE);
		

	}
}
