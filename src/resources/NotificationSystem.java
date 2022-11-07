package resources;

import java.awt.*;
import java.awt.TrayIcon.MessageType;

/* notification system */
public class NotificationSystem {

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