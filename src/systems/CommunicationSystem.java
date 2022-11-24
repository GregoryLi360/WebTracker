package systems;
import java.util.concurrent.*;
import java.io.IOException;
import java.net.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/* communication system */
public class CommunicationSystem {
	/* scrape text from url */
	public static CompletableFuture<Document> scrape (String url) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return Jsoup.connect(url).get();
			} catch (IOException e) {
				throw new CompletionException(e);
			} 
		});
	}
	
	/* check wifi status */
	public static CompletableFuture<Boolean> connectedToWiFi() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				final URLConnection connection = new URL("http://www.google.com").openConnection();
				connection.connect();
				connection.getInputStream().close();
				return true;
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				return false;
			}
		});
	}
}