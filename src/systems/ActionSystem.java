package systems;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.*;

import types.URLState;

/* helper actions */ 
public class ActionSystem {
	/* handles warning when network connection state changes */
	public static boolean warnWiFi(JPanel page, JLabel warning, Boolean connected, boolean wasConnected) {
		if (wasConnected && !connected) warn(page, warning, true);
		else if (!wasConnected && connected) warn(page, warning, false);
		return wasConnected = connected;
	}
	
	/* adds JLabel to JFrame */
	private static void warn(JPanel page, JLabel warning, boolean b) {
		if (b) page.add(warning);
		else page.remove(warning);
		page.repaint();
	}
	
	/* prepends http when neccessary */
	public static String prependHTTP(String link) {
		return (link.length() < 8 || !link.substring(0, 8).equals("https://") && !link.substring(0, 7).equals("http://")) ? 
				"http://" + link : link;
	}
	
	/* writes html files in subdirectory with current date time */
	public static File writeFile(final String APPNAME, String link, String content) {
		File dir = new File(System.getProperty("user.home") + "/" + APPNAME + "Resources/" + link.substring(link.indexOf('/') + 2).replaceAll("\\.", "_"));
		if (!dir.exists()) dir.mkdirs();
		
		File file = new File(dir.getPath() + "/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd h.m.s a")) + ".html");
		try {
			Files.write(file.toPath(), content.getBytes());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Unable to write HTML file");
		}
		
		return file;
	}

	/* recursively deletes non empty directories */
	public static void deleteDirectory(File dir) throws StackOverflowError {
		if (!dir.isDirectory()) return;
		
		Arrays.stream(dir.listFiles()).forEach(file -> {
			if (file.isDirectory()) deleteDirectory(file);
			file.delete();
		});
	}
	
	/* gets the most recent html file by the dated name */
	public static File getMostRecent(String APPNAME, String dirname) {
		return getMostRecent(APPNAME, dirname, Set.of());
	}
	
	/* gets most recent html file excluding a set of files */
	public static File getMostRecent(String APPNAME, String dirname, Set<String> exclude) {
		File dir = new File(System.getProperty("user.home") + "/" + APPNAME + "Resources/" + dirname.substring(dirname.indexOf('/') + 2).replaceAll("\\.", "_"));
		if (!dir.exists()) return null;
		
		LocalDateTime mostRecent = null;
		final var pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd h.m.s a");
		
		for (File f: dir.listFiles()) {
			if (!f.isFile()) continue;
			if (exclude.contains(f.getName())) continue;
			
			String name = f.getName();
			int index = name.lastIndexOf('.');
			if (!name.substring(index + 1).equals("html")) continue;
			
			LocalDateTime fileTime;
			
			try {
				fileTime = LocalDateTime.parse(name.substring(0, index), pattern);
			} catch (Exception e) {
				continue;
			}
			
			if (mostRecent == null) {
				mostRecent = fileTime;
			} else if (fileTime.isAfter(mostRecent)) {
				mostRecent = fileTime;
			}
		}
		
		return mostRecent == null ? null : new File(dir.getPath() + "/" + mostRecent.format(pattern) + ".html");
	}

	public static File writeCacheFile(final String APPNAME, long interval) {
		File dir = new File(System.getProperty("user.home") + "/" + APPNAME + "Resources");
		if (!dir.exists()) dir.mkdirs();

		File cache = new File(dir.getPath() + "/cache.txt");
		if (!cache.exists())
			try {
				Files.write(cache.toPath(), Long.toString(interval).getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		else {
			try {
				List<String> content = Files.readAllLines(cache.toPath());
				List<String> links = content.subList(1, content.size());
				Files.write(cache.toPath(), (Long.toString(interval) + "\n" + String.join("\n", links)).getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return cache;
	}

	public static File writeCacheFile(final String APPNAME, List<String> links, Map<String, URLState> states) {
		File dir = new File(System.getProperty("user.home") + "/" + APPNAME + "Resources");
		if (!dir.exists()) dir.mkdirs();

		File cache = new File(dir.getPath() + "/cache.txt");
		links = links.stream().filter(link -> states.get(link) != URLState.INVALID).collect(Collectors.toList());
		if (cache.exists()) 
			try {
				long interval = Long.parseLong(Files.readAllLines(cache.toPath()).get(0));
				Files.write(cache.toPath(), (interval + "\n" + String.join("\n", links)).getBytes());
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
		return cache;
	}

	public static List<List<String>> readCacheFile(final String APPNAME) {
		File cache = new File(System.getProperty("user.home") + "/" + APPNAME + "Resources/cache.txt");
		if (!cache.exists()) return List.of(new ArrayList<>(Arrays.asList("300")), new ArrayList<>());

		try {
			List<String> content = Files.readAllLines(cache.toPath());
			return List.of(content.subList(0, 1), content.subList(1, content.size()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return List.of(new ArrayList<>(Arrays.asList("300")), new ArrayList<>());
	}
}