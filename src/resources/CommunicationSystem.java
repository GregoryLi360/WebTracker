package resources;
import java.util.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/* communication system */
public class CommunicationSystem {
	
	/* scrape text from url */
	public String scrape (String url) throws IOException {
		Document doc = Jsoup.connect(url).get();
		return doc.text();
	}
	
	public HashMap<String, String> crawl(String url) {
		return null;
	}
	
	/* check wifi status */
	public static boolean connectedToWifi() {
	    try {
	        final URL url = new URL("http://www.google.com");
	        final URLConnection conn = url.openConnection();
	        conn.connect();
	        conn.getInputStream().close();
	        return true;
	    } catch (MalformedURLException e) {
	        throw new RuntimeException(e);
	    } catch (IOException e) {
	        return false;
	    }
	}
}