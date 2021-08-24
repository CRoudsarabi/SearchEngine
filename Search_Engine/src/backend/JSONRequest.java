package backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONRequest implements Runnable{

	public String urlString;
	public JSONObject json;
	
	public JSONRequest(String url) {
		this.urlString=url;
	}
	
	@Override
	public void run() {
		URL url;
		try {
			url = new URL(urlString);
			URLConnection uc = url.openConnection();

			InputStreamReader input = new InputStreamReader(uc.getInputStream(), StandardCharsets.UTF_8);
			BufferedReader in = new BufferedReader(input);
			String inputLine;
			StringBuilder rawOutput = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				rawOutput.append(inputLine);
			}

			String stringToParse = rawOutput.toString();
			JSONParser parser = new JSONParser();
			json = (JSONObject) parser.parse(stringToParse);
		} catch (IOException | ParseException e) {

			e.printStackTrace();
		}
	
		
	}

}
