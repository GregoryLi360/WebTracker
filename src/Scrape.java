import java.io.IOException;
import java.util.*;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

//import org.python.util.PythonInterpreter;

public class Scrape {
	public static void main(String[] args) {
		String url = "https://en.wikipedia.org/";
		crawl(1, url, new HashSet<String>());
	}
	
	private static void crawl(int level, String url, Set<String> visited) {
		if (level > 5) return;
		Document doc = request(url, visited);
		
		if (doc != null) {
			for (Element link: doc.select("a[href]")) {
				String nextLink = link.absUrl("href");
				if (!visited.contains(nextLink))
					crawl(level++, nextLink, visited);
			}
		}
	}
	
	/* scrapes webpage by url */
	private static Document request(String url, Set<String> v) {
		try {
			Connection con = Jsoup.connect(url);
			Document doc = con.get();
			
			if (con.response().statusCode() != 200) return null;
			
			System.out.println(url + ": " + doc.title());
			v.add(url);
			
			return doc;
		} catch (IOException e) {
			return null;
		}
	}
}