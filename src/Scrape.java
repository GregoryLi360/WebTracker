import java.util.*;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Scrape {
	
	/* scrape text from url */
	public String scrape (String url) throws IOException {
		Document doc = Jsoup.connect(url).get();
		return doc.text();
	}
	
	public HashMap<String, String> crawl(String url) {
		return null;
	}
}