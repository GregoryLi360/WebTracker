import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.net.*;
import javax.swing.SwingUtilities;


public class WebTracker {
	
	/* creates instance of notification system and feeds into gui */
	public static void main (String[] args) {
		
		/* runs on the AWT thread so other threads can change gui without unexpected errors */
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
            	if (SystemTray.isSupported()) {
                    NotificationSystem notif = new NotificationSystem();
                    Scrape scraper = new Scrape();
                    new Gui(notif, scraper).setVisible(true);
            	}
            }
        });	
	}
	
	public static boolean connectedToWifi() {
	    try {
	        final URL url = new URL("http://www.google.com");
	        final URLConnection conn = url.openConnection();
	        conn.connect();
	        conn.getInputStream().close();
	        return true;
	    } catch (MalformedURLException e) {
	        throw new RuntimeException(e);
	    } catch (IOException e) {
	        return false;
	    }
	}
}

/* notification system */
class NotificationSystem {

	/* displays a tray notification */
    public void displayTray(String message) throws AWTException {
    	SystemTray tray = SystemTray.getSystemTray();
        
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");

        TrayIcon trayIcon = new TrayIcon(image, "Notification");
        trayIcon.setImageAutoSize(true);
        tray.add(trayIcon);

        trayIcon.displayMessage("WebTracker", message, MessageType.INFO);
    }
}