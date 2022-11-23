package main;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.jsoup.nodes.Document;

import pages.*;
import systems.*;
import types.*;

// TODO: change state when notifications are recognized
public class Gui extends JFrame {

	/* Java default serialID */
	private static final long serialVersionUID = 7525862982119764367L;

	/* constants */
	public static final String APPNAME = "WebTracker";
	
	/* pages */
	public Home homePage;
	public Settings settingsPage;
	
	/* notification system */
	public NotificationSystem notif;

	/* link states */
	public ArrayList<String> links;
	public HashMap<String, URLState> states;
	public HashMap<String, Document> info;

	/* random booleans */
	public boolean wasConnected = true;
	public boolean inFocus = true;

	/* creates gui for app */
	public Gui(final NotificationSystem notif, final Home homePage, final Settings settingsPage) throws Exception {
		/* use system graphic */
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		this.notif = notif;
		this.homePage = homePage;
		this.settingsPage = settingsPage;
		init();
	}

	/* initializes components */
	private void init() {
		/* initialize data structures used to track states */
		links = new ArrayList<>();
		states = new HashMap<>();
		info = new HashMap<>();

		/* creates a resources folder */
		File dir = new File(System.getProperty("user.home") + "/" + APPNAME + "Resources");
		if (!dir.exists())
			dir.mkdir();

		

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		/* executes every 5 minutes */
		scheduler.scheduleAtFixedRate(() -> {
			CommunicationSystem.connectedToWiFi().thenAccept(connected -> {
				if (!(wasConnected = ActionSystem.warnWiFi(Gui.this, homePage.warning, connected, wasConnected)))
					return;

				/* loop through links */
				links.forEach(link -> {
					/* scrape link */
					final String newLink = ActionSystem.prependHTTP(link);
					CommunicationSystem.scrape(newLink).whenComplete((res, ex) -> {
						if (ex != null) {
							CommunicationSystem.connectedToWiFi().thenAccept(stillConnected -> {
								if (!(wasConnected = ActionSystem.warnWiFi(Gui.this, homePage.warning, stillConnected,
										wasConnected)))
									throw new RuntimeException();

								states.put(link, URLState.INVALID);
							});

							return;
						}

						Document doc = info.get(link);

						if (doc == null || doc.html().equals(res.html()))
							return;
						else if (!doc.text().equals(res.text())) { 
							states.put(link, URLState.UPDATED);
							ActionSystem.updateTextArea(homePage.textArea, links, states);
							ActionSystem.writeFile(APPNAME, newLink, res.html());
							notif.updateBadge(states.values());
							if (!inFocus) {
								notif.displayTray(link + " updated");
							}
						}
						info.put(link, res);
					});

				}); /* end of loop */

				System.out.println("auto scraped");
			}); 
		}, 10, 5555, TimeUnit.SECONDS);  // TODO: find the difference of texts and show it

		if (Math.random() == 1) { // TODO: change time interval
			scheduler.shutdown();
		}
	}
}