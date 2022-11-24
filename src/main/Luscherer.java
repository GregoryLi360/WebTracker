package main;
import javax.swing.SwingUtilities;

import systems.*;

public class Luscherer {
	/* creates instance of notification system and feeds into gui */
	public static void main(String[] args) {
		/* runs on the AWT thread so other threads can change gui without unexpected errors */
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				NotificationSystem notif = new NotificationSystem();
				try {
					new Gui(notif).setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}