package main;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;

import org.jsoup.nodes.Document;

import systems.*;
import types.*;

// TODO: change state when notifications are recognized
public class Gui extends JFrame {

	/* Java default serialID */
	private static final long serialVersionUID = 7525862982119764367L;

	/* constants */
	public static final String APPNAME = "WebTracker";
	private final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

	/* components */
	public JTextField entry;
	public JScrollPane scroll;
	public JTextArea textArea;
	public JButton submit;
	public JButton view;
	public JButton remove;
	public JComboBox<String> dropdown;
	public JLabel warning;
	public JButton settings;

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
	public Gui(NotificationSystem notif) throws Exception {
		/* use system graphic */
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		this.notif = notif;
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

		/* creates a submit button */
		var submitAction = GuiHelperSystem.getSubmitAction(this);
		submit = new JButton("Submit");
		submit.addActionListener(submitAction);

		/* creates a view button to view the selected link as a local HTML file */
		var viewAction = GuiHelperSystem.getViewAction(this);
		view = new JButton("View");
		view.addActionListener(viewAction);

		/* creates a remove button */
		var removeAction = GuiHelperSystem.getRemoveAction(this);
		remove = new JButton("Remove");
		remove.addActionListener(removeAction);

		/* creates a text entry field and submits when enter key is pressed */
		entry = new JTextField();
		entry.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == '\n') {
					e.consume();
					submitAction.actionPerformed(null);
				}
			}

			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		
		/* creates a dropdown menu of all tracking links */
		dropdown = new JComboBox<String>(links.toArray(new String[0]));
		
		/* creates a scrollable text display component */
		textArea = new JTextArea();
		textArea.setEditable(false);
		scroll = new JScrollPane(textArea);

		/* creates a wifi warning label */
		warning = new JLabel("Not connected to WIFI");

		/* adds action listeners */
	    GuiHelperSystem.addActionListeners(this, new AbstractAction() {
			private static final long serialVersionUID = 3888947883624355853L;
			@Override
			public void actionPerformed(ActionEvent e) {
				viewAction.actionPerformed(null);
			}
	    }, new AbstractAction() {
			private static final long serialVersionUID = 5482259887847269497L;
			@Override
			public void actionPerformed(ActionEvent e) {
				removeAction.actionPerformed(null);
			}
	    });
	    
	    /* sets default options */
		GuiHelperSystem.setOptions(this, APPNAME, dim);

		/* adds components to frame */
		add(entry);
		add(submit);
		add(dropdown);
		add(view);
		add(remove);
		add(scroll);
//        add(settings);

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		/* executes every 5 minutes */
		scheduler.scheduleAtFixedRate(() -> {
			CommunicationSystem.connectedToWiFi().thenAccept(connected -> {
				if (!(wasConnected = ActionSystem.warnWiFi(Gui.this, warning, connected, wasConnected)))
					return;

				/* loop through links */
				links.forEach(link -> {
					/* scrape link */
					final String newLink = ActionSystem.prependHTTP(link);
					CommunicationSystem.scrape(newLink).whenComplete((res, ex) -> {
						if (ex != null) {
							CommunicationSystem.connectedToWiFi().thenAccept(stillConnected -> {
								if (!(wasConnected = ActionSystem.warnWiFi(Gui.this, warning, stillConnected,
										wasConnected)))
									throw new RuntimeException();

								states.put(link, URLState.INVALID);
							});

							return;
						}

						Document doc = info.get(link);

						if (doc == null || doc.html().equals(res.html()))
							return;
						else if (!doc.text().equals(res.text())) { // TODO: find difference
							states.put(link, URLState.UPDATED);
							ActionSystem.updateTextArea(textArea, links, states);
							notif.displayTray(link + " updated");
							if (!inFocus)
								notif.addToBadge();
						}

						info.put(link, res);
						ActionSystem.writeFile(APPNAME, newLink, res.html());
					});

				}); /* end of loop */

				System.out.println("auto scraped");
			}); 
		}, 60, 50000, TimeUnit.SECONDS); 

		if (Math.random() == 1) { // TODO: change time interval
			scheduler.shutdown();
		}
	}
}