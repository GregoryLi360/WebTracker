package systems;

import java.awt.*;
import java.awt.Taskbar.Feature;
import java.awt.TrayIcon.MessageType;
import java.util.Collection;

import main.Gui;
import types.URLState;

/* notification system */
public class NotificationSystem {

	public int badgeNumber = 0;
	private SystemTray tray;
    private TrayIcon trayIcon;
    private Taskbar taskbar;

    /* initiates system requirements */
    public NotificationSystem() {
        if (Taskbar.getTaskbar().isSupported(Feature.ICON_BADGE_NUMBER)) taskbar = Taskbar.getTaskbar();
        if (!SystemTray.isSupported()) return;
        tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
        trayIcon = new TrayIcon(image, "Notification");
        trayIcon.setImageAutoSize(true);
        try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			e.printStackTrace();
		}
    }

	/* displays a banner notification */
    public void displayTray(String message) {
    	if (trayIcon == null) return;
        trayIcon.displayMessage(Gui.APPNAME, message, MessageType.INFO);
    }
    
    /* adds one to badge number */
    public void addBadge() {
		if (taskbar == null) return;
        taskbar.setIconBadge(Integer.toString(++badgeNumber));
    }
    
    /* removes one from badge number */
    public void removeBadge() {
		if (taskbar == null) return;
        taskbar.setIconBadge(Integer.toString(--badgeNumber));
        if (badgeNumber == 0)
            taskbar.setIconBadge(null);
    }
    
    /* updates badge number */
    public void updateBadge(Collection<URLState> states) {
    	if (taskbar == null) return;
        taskbar.setIconBadge(Integer.toString(badgeNumber = (int) states.stream().filter(s -> s == URLState.UPDATED).count()));
        if (badgeNumber == 0)
            taskbar.setIconBadge(null);
    }
    
    /* clears badge number */
    public void clearBadge() {
    	if (taskbar == null) return;
        badgeNumber = 0;
        taskbar.setIconBadge(null);
    }
}