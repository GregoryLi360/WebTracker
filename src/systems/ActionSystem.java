package systems;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.swing.*;

import types.*;

/* helper actions */ 
public class ActionSystem {

	/* handles warning when network connection state changes */
	public static boolean warnWiFi(JFrame frame, JLabel warning, Boolean connected, boolean wasConnected) {
		if (wasConnected && !connected) warn(frame, warning, true);
		else if (!wasConnected && connected) warn(frame, warning, false);
		return wasConnected = connected;
	}
	
	/* adds JLabel to JFrame */
	private static void warn(JFrame frame, JLabel warning, boolean b) {
		if (b) frame.add(warning);
		else frame.remove(warning);
		frame.repaint();
	}
	
	/* updates viewable text area */
	public static void updateTextArea(JTextArea textArea, ArrayList<String> list, HashMap<String, URLState> states) {
		int len = list.size();
		StringBuilder sb = new StringBuilder(len*100);
        for (int i=0; i<len; i++) {
        	String str = list.get(i);
        	var state = states.get(str);
        	if (state != URLState.UNCHANGED) sb.append((i + 1) + ". " + str + "    " + state + "\n");
        	else sb.append((i + 1) + ". " + str + "\n");
        }
        
		textArea.setText(sb.toString());
	}	
	
	/* prepends http when neccessary */
	public static String prependHTTP(String link) {
		return (link.length() < 8 || !link.substring(0, 8).equals("https://") && !link.substring(0, 7).equals("http://")) ? 
				"http://" + link : link;
	}
	
	public static File writeFile(final String APPNAME, String link, String content) {
		File dir = new File(System.getProperty("user.home") + "/" + APPNAME +"Resources/" + link.substring(link.indexOf('/') + 2));
		if (!dir.exists()) dir.mkdirs();
		
		File file = new File(dir.getPath() + "/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd h.m.s a")) + ".html");
		try {
			Files.write(file.toPath(), content.getBytes());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Unable to write HTML file");
		}
		
		return file;
	}
	
	public static File getMostRecent(String APPNAME, String dirname) {
		File dir = new File(System.getProperty("user.home") + "/" + APPNAME +"Resources/" + dirname.substring(dirname.indexOf('/') + 2));
		if (!dir.exists()) return null;
		
		LocalDateTime mostRecent = null;
		final var pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd h.m.s a");
		
		for (File f: dir.listFiles()) {
			if (!f.isFile()) continue;
			
			String name = f.getName();
			int index = name.lastIndexOf('.');
			if (!name.substring(index + 1).equals("html")) continue;
			
			LocalDateTime fileTime = LocalDateTime.parse(name.substring(0, index), pattern);
			if (mostRecent == null) {
				mostRecent = fileTime;
			} else if (fileTime.isAfter(mostRecent)) {
				mostRecent = fileTime;
			}
		}
		
		return new File(dir.getPath() + "/" + mostRecent.format(pattern) + ".html");
	}
}