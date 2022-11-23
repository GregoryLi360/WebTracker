package pages;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import systems.GuiHelperSystem;

public class Home extends JComponent {

	private static final long serialVersionUID = -3786802108268922698L;

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
	public Image settingsImg;
	public JButton settings;
	public Image refreshImg;
	public JButton refresh; 

	public Home() {
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
		
		var settingsAction = GuiHelperSystem.getSettingsAction(this);
		try {
			settingsImg = ImageIO.read(new File("settings.png"));
		} catch (IOException e1) {
			e1.printStackTrace();
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
		
		var refreshAction = GuiHelperSystem.getRefreshAction(this);
		try {
			refreshImg = ImageIO.read(new File("refresh.png"));
		} catch (IOException e1) {
			e1.printStackTrace();
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
		
		/* adds k]\ listeners */
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
        add(settings);
        add(refresh);
	}
}
