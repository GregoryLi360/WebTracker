package pages;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.ImageIO;

import main.Gui;
import systems.*;

/* settings page */
public class Settings extends JPanel {
    /* components */
    public JButton home;
    public JButton clear;
    public JSlider slider;
    public JLabel sliderValue;
    public JButton save;
    public JLabel saveWarning;
    public JTextArea shortcuts;
    
    /* actions */
    public ActionListener homeAction;
    public ActionListener clearAction;
    
    /* image icons */
    public Image homeImg;

    /* auto fetch interval */
    public Duration interval;

    /* constructor to initialize all variables and constructs the page */
    public Settings(Gui gui, long seconds) {
        interval = Duration.ofSeconds(seconds);

        homeAction = GuiHelperSystem.getHomeAction(gui, this);
        try {
            homeImg = ImageIO.read(new File("images/home.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        home = new JButton() {
            @Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				final int sideLen = Math.max(Math.min(Math.min(getWidth() - 10, getHeight() - 10), 30), 10);
				g.drawImage(homeImg, getWidth() / 2 - sideLen / 2, getHeight() / 2 - sideLen / 2, sideLen, sideLen, null);
			}
        };
        home.addActionListener(homeAction);

        clearAction = GuiHelperSystem.getClearAction(gui);
        clear = new JButton("Clear Cache");
        clear.addActionListener(clearAction);
        
        long secs = interval.getSeconds();
        long[] hms = { secs / 3600, (secs % 3600) / 60, secs % 60 };
        StringBuilder sb = new StringBuilder();
        if (hms[0] != 0) sb.append(hms[0] + " hour" + (hms[0] == 1 ? ", " : "s, "));
        if (hms[1] != 0) sb.append(hms[1] + " minute" + (hms[1] == 1 ? ", " : "s, "));
        if (hms[2] != 0 || hms[0] == 0 && hms[1] == 0) sb.append(hms[2] + " second" + (hms[2] == 1 ? "": "s"));
        else sb.setLength(sb.length() - 2);
        sliderValue = new JLabel("Auto fetch interval is set to " + sb, JLabel.CENTER);
        saveWarning = new JLabel("Unsaved changes", JLabel.CENTER);
        saveWarning.setForeground(Color.RED);

        slider = new JSlider(JSlider.HORIZONTAL, 30, (int) Duration.ofMinutes(30).getSeconds(), (int) interval.getSeconds());
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                long value = slider.getValue();
                long[] hms = { value / 3600, (value % 3600) / 60, value % 60 };

                /* format hours minutes seconds */
                StringBuilder sb = new StringBuilder();
                if (hms[0] != 0) sb.append(hms[0] + " hour" + (hms[0] == 1 ? ", " : "s, "));
                if (hms[1] != 0) sb.append(hms[1] + " minute" + (hms[1] == 1 ? ", " : "s, "));
                if (hms[2] != 0 || hms[0] == 0 && hms[1] == 0) sb.append(hms[2] + " second" + (hms[2] == 1 ? "": "s"));
                else sb.setLength(sb.length() - 2);

                /* displays new interval */
                sliderValue.setText("Auto fetch interval is set to " + sb);

                /* warn for unsaved changes */
                if (interval.getSeconds() != value) {
                    if (saveWarning.getParent() == null) {
                        add(saveWarning);
                        repaint();
                    }
                } else if (saveWarning.getParent() != null) {
                    remove(saveWarning);
                    repaint();
                }
            }
        });
        slider.setMajorTickSpacing(300);
        slider.setMinorTickSpacing(60);
        slider.setPaintTicks(true);

        save = new JButton("Save");
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                /* changes auto fetch interval */
                long value = slider.getValue();
                interval = Duration.ofSeconds(value);
                long[] hms = { value / 3600, (value % 3600) / 60, value % 60 };
                long temp = Arrays.stream(hms).filter(t -> t == 0).count();
                
                if (temp == 3) {
                    JOptionPane.showMessageDialog(null, "Unable to set 0 second auto scrape interval");
                    return;
                }

                TimeUnit unit = TimeUnit.SECONDS;
                if (temp == 2) {
                    if (hms[0] != 0) {
                        value = hms[0];
                        unit = TimeUnit.HOURS;
                    } else if (hms[1] != 0) {
                        value = hms[1];
                        unit = TimeUnit.MINUTES;
                    }
                }

                gui.changeAutoScrapeInterval(value, unit);

                /* removes unsaved changes warning */
                if (saveWarning.getParent() != null) {
                    remove(saveWarning);
                    repaint();
                }
            }
        });

        shortcuts = new JTextArea("Shortcut Keys:\nEnter -> View\nDelete -> Remove\nUp -> Select link above\nDown -> Select link below");
        shortcuts.setEditable(false);
        shortcuts.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        /* default options */
        setVisible(true);
        setSize(gui.dim);
        setMinimumSize(new Dimension(200, 100));
        setLayout(null);

        /* add components to panel */
        add(home);
        add(clear);
        add(slider);
        add(sliderValue);
        add(save);
        add(shortcuts);
    } 
}
