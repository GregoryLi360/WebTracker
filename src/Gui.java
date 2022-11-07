import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

public class Gui extends JFrame {

	/* Java default serialID */
	private static final long serialVersionUID = 7525862982119764367L;
	
	/* components */
	private JTextField entry;
    private JScrollPane scroll;
    private JTextArea textArea;
    private JButton submit;
    private JButton view;
    private JButton remove;
    private JComboBox<String> dropdown;
    private JLabel warning;
    
    /* notification system */
    private NotificationSystem notif;
    
    /* links to track */
    private ArrayList<String> links;
    
    private boolean wasConnected = true;
	
    /* creates gui for app */ 
	public Gui (NotificationSystem notif) {
		
		/* use system graphic */
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/* save instance of notification system */
		this.notif = notif;
		init();
	}
	
	/* initializes components */
	private void init() {
		
		/* initialize list of links to track */
		links = new ArrayList<String>();
        
		/* initializes components */
        entry = new JTextField();
        submit = new JButton("Submit");
        submit.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
            	String txt = entry.getText();
            	if (links.contains(txt)) return;
            	
            	links.add(txt);
                entry.setText("");
                
                StringBuilder sb = new StringBuilder();
                for (int i=0; i<links.size(); i++)
                	sb.append((i + 1) + ". " + links.get(i)+"\n");
                
                textArea.setText(sb.toString());
                dropdown.addItem(txt);
                
                try {
					notif.displayTray(txt + " added to tracker");
				} catch (AWTException e1) {
					e1.printStackTrace();
				}
                
                boolean connected = WebTracker.connectedToWifi();
                if (wasConnected && !connected) warn();
        		else if (!wasConnected && connected) unwarn();
        		wasConnected = connected;
            }
        });
        

        dropdown = new JComboBox<String>(links.toArray(new String[0]));
        view = new JButton("View");
        view.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				String link = (String)dropdown.getSelectedItem();
				if (link == null) return;
				
				boolean connected = WebTracker.connectedToWifi();
				if (wasConnected && !connected) warn();
	    		else if (!wasConnected && connected) unwarn();
	    		wasConnected = connected;
				
				System.out.println(link);
			}
        });
        remove = new JButton("Remove");
        remove.addActionListener(new ActionListener() {
        	public void actionPerformed(final ActionEvent e) {
        		int index = dropdown.getSelectedIndex();
        		if (index < 0) return; 
        		
        		dropdown.removeItemAt(index);
        		links.remove(index);
        		
        		StringBuilder sb = new StringBuilder();
                for (int i=0; i<links.size(); i++)
                	sb.append((i + 1) + ". " + links.get(i)+"\n");
                
        		textArea.setText(sb.toString());
        		
        		boolean connected = WebTracker.connectedToWifi();
        		if (wasConnected && !connected) warn();
        		else if (!wasConnected && connected) unwarn();
        		wasConnected = connected;
        	}
        });
        
        
        textArea = new JTextArea();
        textArea.setColumns(20);
        textArea.setRows(5);
        textArea.setEditable(false);
        scroll = new JScrollPane(textArea);
        
        /* adds a window resize listener to scale components automatically */
        this.addComponentListener(new ComponentAdapter() {  
			public void componentResized(ComponentEvent evt) {
				var bounds = getComponentBounds(getSize());
				entry.setBounds(bounds[0]);
				submit.setBounds(bounds[1]);
				dropdown.setBounds(bounds[2]);
				view.setBounds(bounds[3]);
				remove.setBounds(bounds[4]);
				scroll.setBounds(bounds[5]);
				warning.setBounds(bounds[6]);
			}
        });
        
        warning = new JLabel("Not connected to WIFI");
        
        /* set default options */
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("WebTracker");
		setSize(700, 500);
		setLayout(null);
		setLocationRelativeTo(null);
        
        /* adds components to frame */
        add(entry);
        add(submit);
        add(dropdown);
        add(view);
        add(remove);
        add(scroll);
	}
	
	/* gets bounds of components from window dimensions */
	public Rectangle[] getComponentBounds(Dimension window) {
		int ww = window.width, wh = window.height;
		int minw = 20, maxw = 500, minh = 20, maxh = 100;
		
		Rectangle entry = new Rectangle(5, 5, Math.max(minw, Math.min(maxw, ww/2)), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle submit = new Rectangle(entry.width + 10, 5, Math.max(minw, Math.min(maxw/2, ww/3)), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle dropdown = new Rectangle(5, entry.height + 10, Math.max(minw, Math.min(maxw/3, ww/4)), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle view = new Rectangle(dropdown.width + 10, entry.height + 10, Math.max(minw, Math.min(maxw/3, ww/4)), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle remove = new Rectangle(dropdown.width + view.width + 10, entry.height + 10, Math.max(minw, Math.min(maxw/3, ww/4)), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle textArea = new Rectangle(5, entry.height + remove.height + 10, Math.max(minw, ww - 10), Math.max(minh, wh - entry.height - dropdown.height - 45));
		Rectangle warning = new Rectangle(dropdown.width + view.width + remove.width + 20, entry.height +10, Math.min(ww - (dropdown.width + view.width + remove.width + 20), 300), Math.max(minh, Math.min(maxh, wh/10)));
		
		return new Rectangle[]{entry, submit, dropdown, view, remove, textArea, warning};
	}
	
	public void warn() {
		add(warning);
		repaint();
	}
	
	public void unwarn() {
		remove(warning);
		repaint();
	}
}
