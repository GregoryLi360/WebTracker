package main;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import pages.*;
import systems.*;
import types.*;

// TODO: save tracking to text file and load from text file
public class Gui extends JFrame {
	/* Java default serialID */
	private static final long serialVersionUID = 7525862982119764367L;

	/* constants */
	public static final String APPNAME = "Luscherer";
	public final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

	/* notification system */
	public NotificationSystem notif;

	public Home homePage;
	public Settings settingsPage;

	public PageState pageState;

	public ScheduledExecutorService scheduler;
	public ScheduledFuture<?> autoScrape;

	/* link states */
	public List<String> links;
	public HashMap<String, URLState> states;
	public HashMap<String, Document> info;

	/* random booleans */
	public boolean wasConnected = true;
	public boolean inFocus = true;

	/* creates gui for app */
	public Gui(NotificationSystem notif) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		/* use system graphic */
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		/* initialize data structures used to track states */
		var cache = ActionSystem.readCacheFile(APPNAME);
		links = cache.get(1);
		states = new HashMap<>();
		info = new HashMap<>();
		pageState = PageState.HOME;
		links.forEach(link -> {
			states.put(link, URLState.UNCHANGED);
			Document doc = null;
			try {
				doc = Jsoup.parse(ActionSystem.getMostRecent(APPNAME, ActionSystem.prependHTTP(link)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			info.put(link, doc);
		});

		this.notif = notif;
		notif.clearBadge();
		homePage = new Home(this);
		long interval = 300;
		try {
			interval = Long.parseLong(cache.get(0).get(0));
			if (interval < 300) interval = 300;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		settingsPage = new Settings(this, interval);
		ActionSystem.writeCacheFile(APPNAME, interval);
		ActionSystem.writeCacheFile(APPNAME, links, states);
		GuiHelperSystem.updateTextArea(homePage.textArea, links, states);

		init();
	}

	/* initializes components */
	private void init() {
		/* creates a resources folder */
		File dir = new File(System.getProperty("user.home") + "/" + APPNAME + "Resources");
		if (!dir.exists())
			dir.mkdir();
			
		/* adds key listeners */
	    GuiHelperSystem.addActionListeners(this, homePage, settingsPage, new AbstractAction() {
			private static final long serialVersionUID = 3888947883624355853L;
			@Override
			public void actionPerformed(ActionEvent e) {
				homePage.viewAction.actionPerformed(null);
			}
	    }, new AbstractAction() {
			private static final long serialVersionUID = 5482259887847269497L;
			@Override
			public void actionPerformed(ActionEvent e) {
				homePage.removeAction.actionPerformed(null);
			}
	    }, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int curr = homePage.dropdown.getSelectedIndex();
				if (curr > 0)
					homePage.dropdown.setSelectedIndex(curr - 1);
			}
		}, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int curr = homePage.dropdown.getSelectedIndex();
				if (curr < homePage.dropdown.getItemCount() - 1)
					homePage.dropdown.setSelectedIndex(curr + 1);
			}
		});	  
		
	    /* sets default options */
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle(APPNAME);
		setSize(dim.width / 2, dim.height / 2 + 50);
		setMinimumSize(new Dimension(200, 100));
		setLayout(null);
		setLocationRelativeTo(null);

		/* displays home page on launch */
		add(homePage);

		/* scheuldes auto scraping for every 5 minutes */
		scheduler = Executors.newScheduledThreadPool(1);
		changeAutoScrapeInterval(settingsPage.interval.getSeconds(), TimeUnit.SECONDS);
	}

	/* changes interval between auto fetches */
	public void changeAutoScrapeInterval(long time, TimeUnit unit) {
		/* catches invalid time parameters */
		if (time <= 0) return;

		/* cancels previous interval scheduled task */
		if (autoScrape != null)
			autoScrape.cancel(true);

		/* schedules at new interval */	
		autoScrape = scheduler.scheduleAtFixedRate(() -> { scrapeAll(); System.out.println("auto scraped"); }, time, time, unit);  
		ActionSystem.writeCacheFile(APPNAME, Duration.of(time, unit.toChronoUnit()).getSeconds());
		System.out.println(time + " " + unit);
	}

	/* fetches update for all links being tracked */
	public void scrapeAll() {
		/* check for active wifi connection */
		CommunicationSystem.connectedToWiFi().thenAccept(connected -> {
			if (!(wasConnected = ActionSystem.warnWiFi(homePage, homePage.warning, connected, wasConnected)))
				return;

			/* loop through links */
			links.forEach(link -> {

				/* scrape link */
				final String newLink = ActionSystem.prependHTTP(link);
				CommunicationSystem.scrape(newLink).whenComplete((res, ex) -> {
					/* handle invalid url */
					if (ex != null) {
						CommunicationSystem.connectedToWiFi().thenAccept(stillConnected -> {
							if (!(wasConnected = ActionSystem.warnWiFi(homePage, homePage.warning, stillConnected, wasConnected)))
								throw new RuntimeException();

							states.put(link, URLState.INVALID);
						});

						return;
					}

					/* get old info */
					Document doc = info.get(link);

					/* decides url state */
					if (doc == null || doc.html().equals(res.html()))
						return;
					else if (!doc.text().equals(res.text())) { 
						states.put(link, URLState.UPDATED);
						GuiHelperSystem.updateTextArea(homePage.textArea, links, states);
						ActionSystem.writeFile(APPNAME, newLink, res.html());
						notif.updateBadge(states.values());
						if (!inFocus) {
							notif.displayTray(link + " updated");
						}
					}

					/* updates info */
					info.put(link, res);
				});

			});
		});
	}
}