package systems;

import java.awt.*;
import java.awt.Taskbar.Feature;
import java.awt.TrayIcon.MessageType;
import java.util.Collection;

import types.URLState;

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
    
    /* adds one to badge number */
    public void addBadge() {
		if (Taskbar.getTaskbar().isSupported(Feature.ICON_BADGE_NUMBER))
        	Taskbar.getTaskbar().setIconBadge(Integer.toString(++badgeNumber));
    }
    
    /* removes one from badge number */
    public void removeBadge() {
		if (Taskbar.getTaskbar().isSupported(Feature.ICON_BADGE_NUMBER))
        	Taskbar.getTaskbar().setIconBadge(Integer.toString(--badgeNumber));
    }
    
    /* updates badge number */
    public void updateBadge(Collection<URLState> states) {
    	if (Taskbar.getTaskbar().isSupported(Feature.ICON_BADGE_NUMBER))
        	Taskbar.getTaskbar().setIconBadge(Integer.toString(badgeNumber = (int) states.stream().filter(s -> s == URLState.UPDATED).count()));
    }
    
    /* clears badge number */
    public void clearBadge() {
    	if (Taskbar.getTaskbar().isSupported(Feature.ICON_BADGE_NUMBER))
        	Taskbar.getTaskbar().setIconBadge(Integer.toString(badgeNumber = 0));
    }
}