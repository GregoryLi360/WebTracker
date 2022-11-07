import java.awt.SystemTray;
import javax.swing.SwingUtilities;

import resources.*;


public class WebTracker {
	
	/* creates instance of notification system and feeds into gui */
	public static void main (String[] args) {
		
		/* runs on the AWT thread so other threads can change gui without unexpected errors */
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
            	if (SystemTray.isSupported()) {
                    NotificationSystem notif = new NotificationSystem();
                    CommunicationSystem scraper = new CommunicationSystem();
                    new Gui(notif, scraper).setVisible(true);
            	}
            }
        });	
	}
}