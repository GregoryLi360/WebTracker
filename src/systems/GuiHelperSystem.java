package systems;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.swing.*;

import org.jsoup.nodes.Document;

import main.Gui;
import types.URLState;

public class GuiHelperSystem {

	/* gets bounds of components from window dimensions */
	public static Rectangle[] getComponentBounds(Dimension window) {
		int ww = window.width, wh = window.height;
		final int minw = 20, maxw = 500, minh = 20, maxh = 100, gap = 5;
		
		Rectangle entry = new Rectangle(gap, gap, Math.max(minw, Math.min(maxw, ww/2)), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle submit = new Rectangle(entry.x + entry.width + gap, gap, Math.max(minw, Math.min(maxw/2, ww/3)), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle dropdown = new Rectangle(gap, entry.y + entry.height + gap, Math.max(minw, Math.min(maxw/3, ww/4)), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle view = new Rectangle(dropdown.x + dropdown.width + gap, entry.y + entry.height + gap, Math.max(minw, Math.min(maxw/3, ww/4)), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle remove = new Rectangle(view.x + view.width + gap, entry.y + entry.height + gap, Math.max(minw, Math.min(maxw/3, ww/4)), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle textArea = new Rectangle(gap, remove.y + remove.height + gap, Math.max(minw, ww - 10), Math.max(minh, wh - entry.height - dropdown.height - 50));
		Rectangle warning = new Rectangle(remove.x + remove.width + 2 * gap, entry.y + entry.height + gap, Math.min(ww - (dropdown.width + view.width + remove.width + 20), 300), Math.max(minh, Math.min(maxh, wh/10)));
		Rectangle setting = new Rectangle(window.width - Math.max(minw, Math.min(maxw/5, ww/7)), gap, Math.max(minw, Math.min(maxw/5, ww/7)) - 5, Math.max(minh, Math.min(maxh, wh/10)));
		
		return new Rectangle[]{entry, submit, dropdown, view, remove, textArea, warning, setting};
	}
	
	public static void addActionListeners(Gui gui, AbstractAction viewAction, AbstractAction removeAction) {
		/* map enter to view selected link */
        gui.textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "viewAction");
        gui.textArea.getActionMap().put("viewAction", viewAction);
        gui.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "viewAction");
        gui.getRootPane().getActionMap().put("viewAction", viewAction);
        
        /* map delete to remove selected link */
        gui.textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "removeAction");
        gui.textArea.getActionMap().put("removeAction", removeAction);
        gui.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "removeAction");
        gui.getRootPane().getActionMap().put("removeAction", removeAction);
        
        /* in focus listener */
        gui.addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				gui.inFocus = false;
			}

			@Override
			public void windowGainedFocus(WindowEvent e) {
				gui.inFocus = true;
				gui.notif.clearBadge();
			}
		});
        
        /* adds a window resize listener to scale components automatically */
		gui.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent evt) {
				var bounds = getComponentBounds(gui.getSize());
				gui.entry.setBounds(bounds[0]);
				gui.submit.setBounds(bounds[1]);
				gui.dropdown.setBounds(bounds[2]);
				gui.view.setBounds(bounds[3]);
				gui.remove.setBounds(bounds[4]);
				gui.scroll.setBounds(bounds[5]);
				gui.warning.setBounds(bounds[6]);
//				settings.setBounds(bounds[7]);
			}
		});
	}
	
	/* set default options */
	public static void setOptions(Gui gui, final String APPNAME, final Dimension dim) {
		gui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		gui.setTitle(APPNAME);
		gui.setSize(dim.width / 2, dim.height / 2 + 50);
		gui.setLayout(null);
		gui.setLocationRelativeTo(null);
	}
	
	public static ActionListener getSubmitAction(Gui gui) {
		return new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				String txt = gui.entry.getText().replaceAll("\\s+", ""); // replace all whitespace
				if (!gui.submit.isEnabled() || gui.links.contains(txt))
					return;

				int len = gui.links.size() + 1;
				if (len > 100) {
					JOptionPane.showMessageDialog(null, "Tracking limit exceeded, try removing entries");
					return;
				}

				gui.submit.setEnabled(false);
				gui.links.add(txt);
				gui.dropdown.addItem(txt);
				gui.entry.setText("");
				gui.states.put(txt, URLState.UNCHANGED);
				ActionSystem.updateTextArea(gui.textArea, gui.links, gui.states);

				CommunicationSystem.connectedToWiFi().thenAccept(connected -> {
					if (!(gui.wasConnected = ActionSystem.warnWiFi(gui, gui.warning, connected, gui.wasConnected))) {
						gui.submit.setEnabled(true);
						return;
					}

					final String newLink = ActionSystem.prependHTTP(txt);

					CommunicationSystem.scrape(newLink).whenComplete((res, ex) -> {
						gui.submit.setEnabled(true);

						if (ex != null) {
							gui.states.put(txt, URLState.INVALID);
							ActionSystem.updateTextArea(gui.textArea, gui.links, gui.states);
							return;
						}

						gui.info.put(txt, res);

						ActionSystem.writeFile(Gui.APPNAME, newLink, res.html());
					});
				});

				/* notif testing things */
//				notif.displayTray(txt + " added to tracker");
//				notif.addToBadge();
			}
		};
	}
	
	public static ActionListener getViewAction(Gui gui) {
		return new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				String link = (String) gui.dropdown.getSelectedItem();
				if (link == null)
					return;

				gui.view.setEnabled(false);

				CommunicationSystem.connectedToWiFi().thenAccept((Boolean connected) -> {
					if (!(gui.wasConnected = ActionSystem.warnWiFi(gui, gui.warning, connected, gui.wasConnected))) {
						gui.view.setEnabled(true);
						return;
					}

					final String newLink = ActionSystem.prependHTTP(link);

					CommunicationSystem.scrape(newLink).whenComplete((res, ex) -> {
						gui.view.setEnabled(true);

						/* handle invalid url */
						if (ex != null) {
							JOptionPane.showMessageDialog(null, "URL " + newLink + " is invalid");
							gui.states.put(link, URLState.INVALID);
							ActionSystem.updateTextArea(gui.textArea, gui.links, gui.states);
							return;
						}

						/* compare with previous info */
						Document doc = gui.info.get(link);
						gui.info.put(link, res);
						
						/* finds latest file to open */
						File file;
						if (doc == null) {
							file = ActionSystem.writeFile(Gui.APPNAME, newLink, res.html());
							gui.states.put(link, URLState.UNCHANGED);
						} else if (doc.text().equals(res.text())) {
							file = ActionSystem.getMostRecent(Gui.APPNAME, newLink);
							gui.states.put(link, URLState.UNCHANGED);
						} else {
							file = ActionSystem.writeFile(Gui.APPNAME, newLink, res.html());
							gui.states.put(link, URLState.UPDATED);
							gui.notif.displayTray(link + " updated");
							gui.notif.addToBadge();
						}
						
						/* opens designated file */
						try {
							Desktop.getDesktop().browse(file.toURI());
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(null, "Unable to open HTML file");
						}

					});
				});
			}
		};
	}
	
	public static ActionListener getRemoveAction(Gui gui) {
		return new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				int index = gui.dropdown.getSelectedIndex();
				if (index < 0)
					return;

				String link = gui.links.get(index);
				gui.dropdown.removeItemAt(index);
				gui.links.remove(index);
				gui.states.remove(link);
				gui.info.remove(link);

				ActionSystem.updateTextArea(gui.textArea, gui.links, gui.states);

				CommunicationSystem.connectedToWiFi().thenAccept(
						connected -> gui.wasConnected = ActionSystem.warnWiFi(gui, gui.warning, connected, gui.wasConnected));
			}
		};
	}
}
