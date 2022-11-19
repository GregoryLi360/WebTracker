package systems;

import java.awt.*;
import java.awt.Taskbar.Feature;
import java.awt.TrayIcon.MessageType;

/* notification system */
public class NotificationSystem {

	public int badgeNumber = 0;
	
	/* displays a tray notification */
    public void displayTray(String message) {
    	if (!SystemTray.isSupported()) return;
    	
    	SystemTray tray = SystemTray.getSystemTray();
        
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");

        TrayIcon trayIcon = new TrayIcon(image, "Notification");
        trayIcon.setImageAutoSize(true);
        
        try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			e.printStackTrace();
		}

        trayIcon.displayMessage("WebTracker", message, MessageType.INFO);
    }
    
    /* adds 1 to badge number */
    public void addToBadge() {
    	if (Taskbar.getTaskbar().isSupported(Feature.ICON_BADGE_NUMBER))
        	Taskbar.getTaskbar().setIconBadge(Integer.toString(++badgeNumber));
    }
    
    /* clears badge number */
    public void clearBadge() {
    	if (Taskbar.getTaskbar().isSupported(Feature.ICON_BADGE_NUMBER))
        	Taskbar.getTaskbar().setIconBadge(Integer.toString(badgeNumber = 0));
    }
}