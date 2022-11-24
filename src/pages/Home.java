package pages;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;

import main.Gui;
import systems.*;

public class Home extends JPanel {
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
	public JButton refresh; 
	
	/* actions */
	public ActionListener submitAction;
	public ActionListener viewAction;
	public ActionListener removeAction;
	
	/* image icons */
	public Image settingsImg;
	public Image refreshImg;

	/* constructor to initialize all variables and constructs the page */
    public Home(Gui gui) {
        /* creates a submit button */
		submitAction = GuiHelperSystem.getSubmitAction(gui, this);
		submit = new JButton("Submit");
		submit.addActionListener(submitAction);

		/* creates a view button to view the selected link as a local HTML file */
		viewAction = GuiHelperSystem.getViewAction(gui, this);
		view = new JButton("View");
		view.addActionListener(viewAction);

		/* creates a remove button */
		removeAction = GuiHelperSystem.getRemoveAction(gui, this);
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
		dropdown = new JComboBox<String>(Stream.of(new String[] {"ALL"}, gui.links.toArray(new String[0])).flatMap(Stream::of).toArray(String[]::new));
		
		/* creates a scrollable text display component */
		textArea = new JTextArea();
		textArea.setEditable(false);
		scroll = new JScrollPane(textArea);

		/* creates a wifi warning label */
		warning = new JLabel("Not connected to WIFI");
		warning.setForeground(Color.RED);
		
		var settingsAction = GuiHelperSystem.getSettingsAction(gui);
		try {
			settingsImg = ImageIO.read(new File("images/settings.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		settings = new JButton() {
			private static final long serialVersionUID = -8201887293511234096L;
			
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				final int sideLen = Math.max(Math.min(Math.min(getWidth() - 10, getHeight() - 10), 30), 10);
				g.drawImage(settingsImg, getWidth() / 2 - sideLen / 2, getHeight() / 2 - sideLen / 2, sideLen, sideLen, null);
			}
		};
		
		settings.addActionListener(settingsAction);
		
		var refreshAction = GuiHelperSystem.getRefreshAction(gui, this);
		try {
			refreshImg = ImageIO.read(new File("images/refresh.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		refresh = new JButton() {
			private static final long serialVersionUID = 2502053912393206291L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				final int sideLen = Math.max(Math.min(Math.min(getWidth() - 25, getHeight() - 25), 30), 10);
				g.drawImage(refreshImg, getWidth() / 2 - sideLen / 2, getHeight() / 2 - sideLen / 2, sideLen, sideLen, null);
			}
		};

		refresh.addActionListener(refreshAction);
	    
		/* default options */
		setVisible(true);
		setSize(gui.dim);
		setMinimumSize(new Dimension(200, 100));
		setLayout(null);

		/* adds components to frame */
		add(entry);
		add(submit);
		add(dropdown);
		add(view);
		add(remove);
		add(scroll);
        add(settings);
        add(refresh);
    }
}
