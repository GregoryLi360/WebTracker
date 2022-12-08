package systems;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import org.jsoup.nodes.Document;

import main.Gui;
import pages.*;
import types.*;

/* helper methods for gui */
public class GuiHelperSystem {
	/* gets bounds of components from window dimensions */
	public static Rectangle[] getComponentBounds(Dimension window) {
		int ww = window.width, wh = window.height;
		final int minw = 20, maxw = 500, minh = 20, maxh = 100, gap = 5;
		
		Rectangle entry = new Rectangle(gap, gap, Math.max(minw, Math.min(maxw, ww/2)), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle submit = new Rectangle(entry.x + entry.width + gap, gap, Math.max(minw, Math.min(maxw/2, ww/3)), Math.max(minh, Math.min(maxh, wh/10)));
		final int settingsSideLen = Math.min(Math.max(minw, Math.min(maxw/5, ww/7)), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle setting = new Rectangle(window.width - settingsSideLen - gap, gap, settingsSideLen, settingsSideLen);
		Rectangle dropdown = new Rectangle(gap, entry.y + entry.height + gap, Math.max(minw, Math.min(maxw/3, ww/4)), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle view = new Rectangle(dropdown.x + dropdown.width + gap, entry.y + entry.height + gap, Math.max(minw, Math.min(maxw/4, ww/6)), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle remove = new Rectangle(view.x + view.width + gap, entry.y + entry.height + gap, Math.max(minw, Math.min(maxw/4, ww/6)), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle refresh = new Rectangle(remove.x + remove.width + gap, entry.y + entry.height + gap, Math.max(minw, Math.min(maxw/4, ww/6)), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle warning = new Rectangle(refresh.x + refresh.width + gap, entry.y + entry.height + gap, Math.min(ww - (dropdown.width + view.width + remove.width + 20), 300), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle textArea = new Rectangle(gap, remove.y + remove.height + gap, Math.max(minw, ww - 10), Math.max(minh, wh - entry.height - dropdown.height - 50));
		Rectangle slider = new Rectangle(dropdown.x, dropdown.y, dropdown.width + view.width + remove.width, dropdown.height);
		Rectangle sliderValue = new Rectangle(dropdown.x, textArea.y, dropdown.width + view.width + remove.width, dropdown.height);

		return new Rectangle[]{entry, submit, dropdown, view, remove, textArea, warning, setting, refresh, slider, sliderValue};
	}
	
	public static void addActionListeners(Gui gui, Home homePage, Settings settingsPage, AbstractAction viewAction, AbstractAction removeAction, AbstractAction upAction, AbstractAction downAction) {
		/* map enter key to view selected link */
        homePage.textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "viewAction");
        homePage.textArea.getActionMap().put("viewAction", viewAction);
        gui.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "viewAction");
        gui.getRootPane().getActionMap().put("viewAction", viewAction);
        
        /* map delete key to remove selected link */
        homePage.textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "removeAction");
        homePage.textArea.getActionMap().put("removeAction", removeAction);
        gui.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "removeAction");
        gui.getRootPane().getActionMap().put("removeAction", removeAction);

		/* map up arrow key select above link */
		homePage.textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "upAction");
		homePage.textArea.getActionMap().put("upAction", upAction);
		gui.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "upAction");
		gui.getRootPane().getActionMap().put("upAction", upAction);	

		/* map down arrow key select below link */
		homePage.textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "downAction");
		homePage.textArea.getActionMap().put("downAction", downAction);
		gui.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "downAction");
		gui.getRootPane().getActionMap().put("downAction", downAction);
        
        /* detects when window is in focus */
        gui.addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				gui.inFocus = false;
			}

			@Override
			public void windowGainedFocus(WindowEvent e) {
				gui.inFocus = true;
			}
		});
        
        /* adds a window resize listener to scale components automatically */
		gui.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent evt) {
				var bounds = getComponentBounds(gui.getSize());
				homePage.setSize(gui.getSize());
				homePage.entry.setBounds(bounds[0]);
				homePage.submit.setBounds(bounds[1]);
				homePage.dropdown.setBounds(bounds[2]);
				homePage.view.setBounds(bounds[3]);
				homePage.remove.setBounds(bounds[4]);
				homePage.scroll.setBounds(bounds[5]);
				homePage.warning.setBounds(bounds[6]);
				homePage.settings.setBounds(bounds[7]);
				homePage.refresh.setBounds(bounds[8]);
				
				settingsPage.setSize(gui.getSize());
				settingsPage.home.setBounds(bounds[7]);
				settingsPage.clear.setBounds(bounds[0]);
				settingsPage.slider.setBounds(bounds[9]);
				settingsPage.sliderValue.setBounds(bounds[10]);
				bounds[4].x = bounds[4].x + bounds[4].width;
				settingsPage.save.setBounds(bounds[4]);
				bounds[4].y = bounds[5].y;
				settingsPage.saveWarning.setBounds(bounds[4]);
				settingsPage.shortcuts.setBounds(bounds[5].x, bounds[5].y + bounds[4].height + 5, bounds[5].width, bounds[5].height - bounds[4].height - 5);
			}
		});
	}

	/* displays a different page */
	public static void switchPage(Gui gui, JPanel page) {
		gui.getContentPane().removeAll();
		gui.add(page);
		gui.repaint();
	}

	/* updates viewable text area */
	public static void updateTextArea(JTextArea textArea, List<String> list, HashMap<String, URLState> states) {
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
	
	/* adds link to keep track of */
	public static ActionListener getSubmitAction(Gui gui, Home homePage) {
		return new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				/* remove whitespace */
				String txt = homePage.entry.getText().replaceAll("\\s+", "");
				
				/* do nothing if the button is disabled or link is already being tracked */
				if (!homePage.submit.isEnabled() || gui.links.contains(txt))
					return;

				/* do not add new link and warn the user if there are over 100 links */
				int len = gui.links.size() + 1;
				if (len > 100) { 
					JOptionPane.showMessageDialog(null, "Tracking limit exceeded, try removing entries");
					return;
				}

				homePage.submit.setEnabled(false);
				gui.links.add(txt);
				homePage.dropdown.addItem(txt);
				homePage.entry.setText("");
				gui.states.put(txt, URLState.UNCHANGED);
				GuiHelperSystem.updateTextArea(homePage.textArea, gui.links, gui.states);
				homePage.dropdown.setSelectedIndex(homePage.dropdown.getItemCount() - 1);

				/* scrape the webpage on submit */
				CommunicationSystem.connectedToWiFi().thenAccept(connected -> {
					if (!(gui.wasConnected = ActionSystem.warnWiFi(homePage, homePage.warning, connected, gui.wasConnected))) {
						homePage.submit.setEnabled(true);
						ActionSystem.writeCacheFile(Gui.APPNAME, gui.links, gui.states, gui.unviewed);
						return;
					}

					final String newLink = ActionSystem.prependHTTP(txt);

					CommunicationSystem.scrape(newLink).whenComplete((res, ex) -> {
						homePage.submit.setEnabled(true);

						if (ex != null) {
							gui.states.put(txt, URLState.INVALID);
							GuiHelperSystem.updateTextArea(homePage.textArea, gui.links, gui.states);
							return;
						}

						gui.info.put(txt, res);

						ActionSystem.writeFile(Gui.APPNAME, newLink, res.html());
						ActionSystem.writeCacheFile(Gui.APPNAME, gui.links, gui.states, gui.unviewed);
					});
				});
			}
		};
	}
	
	/* show settings page */
	public static ActionListener getSettingsAction(Gui gui) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gui.pageState = PageState.SETTINGS;
				switchPage(gui, gui.settingsPage);
			}
		};
	}
	
	/* views the selected link through both local html file(s) and url */
	public static ActionListener getViewAction(Gui gui, Home homePage) {
		return new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				CommunicationSystem.connectedToWiFi().thenAccept(connected -> gui.wasConnected = ActionSystem.warnWiFi(homePage, homePage.warning, connected, gui.wasConnected));

				String link = (String) homePage.dropdown.getSelectedItem();
				if (link == null || gui.states.get(link) == URLState.INVALID)
					return;

				if (link.equals("ALL") && homePage.dropdown.getSelectedIndex() == 0) {
					int len = gui.links.size();
					if (len > 2) {
						int v = JOptionPane.showConfirmDialog(gui, "You are about to view " + len + " links. Are you sure you want to continue?");
						if (v != 0) return;
					}
					
					for (int i = 0; i < len; i++) {
						link = gui.links.get(i);
						if (gui.states.get(link) == URLState.INVALID) continue;
						viewAction(gui, homePage, link);
					}
				} else {
					viewAction(gui, homePage, link);
				}
				
			}
		};
	}

	/* view action helper method */
	private static void viewAction(Gui gui, Home homePage, String link) {
		homePage.view.setEnabled(false);

		final String newLink = ActionSystem.prependHTTP(link);
		
		/* opens the most recent file and the actual website url; if it has an updated status, opens the old local html version */
		File file;
		
		/* only used if submitted when not connected to wifi */
		if ((file = ActionSystem.getMostRecent(Gui.APPNAME, newLink)) == null) {
			CommunicationSystem.connectedToWiFi().thenAccept((Boolean connected) -> {
				if (!(gui.wasConnected = ActionSystem.warnWiFi(homePage, homePage.warning, connected, gui.wasConnected))) {
					homePage.view.setEnabled(true);
					return;
				}

				CommunicationSystem.scrape(newLink).whenComplete((res, ex) -> {
					homePage.view.setEnabled(true);

					/* handle invalid url */
					if (ex != null) {
						JOptionPane.showMessageDialog(null, "URL " + newLink + " is invalid");
						gui.states.put(link, URLState.INVALID);
						ActionSystem.writeCacheFile(Gui.APPNAME,gui.links, gui.states, gui.unviewed);
						GuiHelperSystem.updateTextArea(homePage.textArea, gui.links, gui.states);
						return;
					}

					/* put into data structures */
					gui.info.put(link, res);
					gui.states.put(link, URLState.UNCHANGED);
					gui.unviewed.put(link, null);
					ActionSystem.writeCacheFile(Gui.APPNAME, gui.links, gui.states, gui.unviewed);
						
					
					/* opens designated file */
					ActionSystem.writeFile(Gui.APPNAME, newLink, res.html());
					
					try {
						Desktop.getDesktop().browse(new URI(newLink));
					} catch (IOException | URISyntaxException e1) {
						JOptionPane.showMessageDialog(null, "Unable to open URL");
					}
					
				});
			});
			return;
		}
		
		homePage.view.setEnabled(true);
		Set<String> exclude = new HashSet<>(Arrays.asList(file.getName()));
		// try {
		// 	Desktop.getDesktop().browse(file.toURI());
		// } catch (IOException e1) {
		// 	JOptionPane.showMessageDialog(null, "Unable to open HTML file");
		// }

		var unviewed = gui.unviewed.get(link);
		if (gui.states.get(link) == URLState.UPDATED && !file.equals(unviewed)) {
			gui.unviewed.put(link, file);
			System.out.println("unviewed: " + gui.unviewed.get(link));
			System.out.println("curr file: " + file);
			for (int i=0; i<10; i++) {
				file = ActionSystem.getMostRecent(Gui.APPNAME, newLink, exclude);
				if (file == null) break;

				System.out.println("file: " + file);
				if (file.equals(unviewed)) {
					try {
						Desktop.getDesktop().browse(file.toURI());
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "Unable to open HTML file");
					}
					break;
				}

				exclude.add(file.getName());
				
				try {
					Desktop.getDesktop().browse(file.toURI());
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Unable to open HTML file");
					break;
				}
			}
		}
		
		try {
			Desktop.getDesktop().browse(new URI(newLink));
		} catch (IOException | URISyntaxException e1) {
			JOptionPane.showMessageDialog(null, "Unable to open URL");
		}
		
		homePage.view.setEnabled(true);
		gui.states.put(link, URLState.UNCHANGED);
		gui.notif.updateBadge(gui.states.values());
		GuiHelperSystem.updateTextArea(homePage.textArea, gui.links, gui.states);
		ActionSystem.writeCacheFile(Gui.APPNAME,gui.links, gui.states, gui.unviewed);
	}
	
	/* stop tracking selected link */
	public static ActionListener getRemoveAction(Gui gui, Home homePage) {
		return new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				CommunicationSystem.connectedToWiFi().thenAccept(connected -> gui.wasConnected = ActionSystem.warnWiFi(homePage, homePage.warning, connected, gui.wasConnected));

				int index = homePage.dropdown.getSelectedIndex() - 1;
				if (index < 0) {
					if (homePage.dropdown.getSelectedIndex() != 0) return;

					int len = gui.links.size();
					if (len == 0) return; 
					if (len == 1) {
						removeAction(gui, homePage, 0);
						ActionSystem.writeCacheFile(Gui.APPNAME, gui.links, gui.states, gui.unviewed);
						return;
					}

					int v = JOptionPane.showConfirmDialog(gui, "You are about to remove " + len + " links. Are you sure you want to continue?");
					if (v != 0) return;

					for (int i = 0; i < len; i++) 
						removeAction(gui, homePage, 0);

				} else removeAction(gui, homePage, index);

				ActionSystem.writeCacheFile(Gui.APPNAME, gui.links, gui.states, gui.unviewed);
			}
		};
	}

	/* remove action helper method */
	private static void removeAction(Gui gui, Home homePage, int index) {
		String link = gui.links.get(index);
		homePage.dropdown.removeItemAt(index + 1);
		gui.links.remove(index);
		gui.states.remove(link);
		gui.info.remove(link);
		gui.unviewed.remove(link);

		GuiHelperSystem.updateTextArea(homePage.textArea, gui.links, gui.states);
	}
	
	/* manually fetches the link and removes updated status and badge notifications */
	public static ActionListener getRefreshAction(Gui gui, Home homePage) {
		return new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				CommunicationSystem.connectedToWiFi().thenAccept(connected -> gui.wasConnected = ActionSystem.warnWiFi(homePage, homePage.warning, connected, gui.wasConnected));
				int index = homePage.dropdown.getSelectedIndex() - 1;
				homePage.refresh.setEnabled(false);
				if (index < 0) {
					if (!homePage.dropdown.getSelectedItem().equals("ALL")) return;
					checkAll(gui, gui.links, homePage);
				} else {
					check(gui, homePage, gui.links.get(index));
					homePage.refresh.setEnabled(true);
				}
			}
		};
	}

	public static void check(Gui gui, Home homePage, String link) {
		/* scrape link */
		final String newLink = ActionSystem.prependHTTP(link);
		CommunicationSystem.scrape(newLink).whenComplete((res, ex) -> {
			/* handle invalid url */
			if (ex != null) {
				CommunicationSystem.connectedToWiFi().thenAccept(stillConnected -> {
					if (!(gui.wasConnected = ActionSystem.warnWiFi(homePage, homePage.warning, stillConnected, gui.wasConnected)))
						throw new RuntimeException();

					gui.states.put(link, URLState.INVALID);

				});

				return;
			}

			/* get old info */
			Document doc = gui.info.get(link);

			/* decides url state */
			if (doc == null) {
				gui.states.put(link, URLState.UNCHANGED);
				GuiHelperSystem.updateTextArea(homePage.textArea, gui.links, gui.states);
				File f = ActionSystem.writeFile(Gui.APPNAME, newLink, res.html());
				if (gui.unviewed.get(link) == null) gui.unviewed.put(link, f);
			} else if (doc.html().equals(res.html())) {
				return;
			}
			else if (!doc.text().equals(res.text())) { 
				gui.states.put(link, URLState.UPDATED);
				GuiHelperSystem.updateTextArea(homePage.textArea, gui.links, gui.states);
				gui.notif.updateBadge(gui.states.values());
				if (!gui.inFocus) {
					gui.notif.displayTray(link + " updated");
				}
				File f = ActionSystem.writeFile(Gui.APPNAME, newLink, res.html());
				if (gui.unviewed.get(link) == null) gui.unviewed.put(link, f);
			}
			/* updates info */
			gui.info.put(link, res);
			ActionSystem.writeCacheFile(Gui.APPNAME, gui.links, gui.states, gui.unviewed);
		});
	}

	public static void checkAll(Gui gui, List<String> links, Home homePage) {
		/* check for active wifi connection */
		CommunicationSystem.connectedToWiFi().thenAccept(connected -> {
			homePage.refresh.setEnabled(true);
			if (!(gui.wasConnected = ActionSystem.warnWiFi(homePage, homePage.warning, connected, gui.wasConnected)))
				return;

			/* loop through links */
			links.forEach(link -> {
				
				GuiHelperSystem.check(gui, homePage, link);

			});
		});
	}

	public static ActionListener getHomeAction(Gui gui, Settings settingsPage) {
		return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				if (settingsPage.saveWarning.getParent() != null) {
					int v = JOptionPane.showConfirmDialog(gui, "Warning: Unsaved changes! Changes you have made will be lost.");
					if (v != 0)
						return;
				}
                gui.pageState = PageState.HOME;
				settingsPage.slider.setValue((int) settingsPage.interval.getSeconds());
                GuiHelperSystem.switchPage(gui, gui.homePage);
            }
        };
	}

	public static ActionListener getClearAction(Gui gui) {
		return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(gui, "Warning: All local html files will be deleted except the most recent versions of links you are currently tracking.") != 0)
                    return;
                
                HashMap<Path, byte[]> filesToKeep = new HashMap<>();
				File cache = new File(System.getProperty("user.home") + "/" + Gui.APPNAME + "Resources/cache.txt");
				if (cache.exists()) 
					try {
						filesToKeep.put(cache.toPath(), Files.readAllBytes(cache.toPath()));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					           
                gui.links.forEach(link -> {
                    final String newLink = ActionSystem.prependHTTP(link);
                    Path path = ActionSystem.getMostRecent(Gui.APPNAME, newLink).toPath();
                    try {
                        byte[] bytes = Files.readAllBytes(path);
                        filesToKeep.put(path, bytes);
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "Unable to clear resources folder. Unexpected resources folder structure at " + path + ".");
                        return;
                    }
                });

                var resourcesDir = new File(System.getProperty("user.home") + "/" + Gui.APPNAME + "Resources/");
                try {
                    ActionSystem.deleteDirectory(resourcesDir);
                } catch (StackOverflowError e1) {
                    JOptionPane.showMessageDialog(null, "Unable to clear resources folder. Try manually clearing it at " + resourcesDir.getPath() + ".");
                    return;
                }
                
                gui.states.replaceAll((k, v) -> v == URLState.UPDATED ? URLState.UNCHANGED : v);
                GuiHelperSystem.updateTextArea(gui.homePage.textArea, gui.links, gui.states);

                filesToKeep.forEach((path, bytes) -> {
                    path.toFile().getParentFile().mkdirs();
                    try {
                        Files.write(path, bytes);
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "Unable to write HTML file");
                    }
                });

                JOptionPane.showMessageDialog(null, "Clearing cache complete");
            }
        };
	}
}
