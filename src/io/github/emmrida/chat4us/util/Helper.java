/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.StringCharacterIterator;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.FocusManager;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.github.emmrida.chat4us.gui.CertGenDialog;
import io.github.emmrida.chat4us.gui.MainWindow;

/**
 * Contains public static functions used by different classes of this project.
 *
 * @author El Mhadder Mohamed Rida
 */
public class Helper {
    public static final String SECURE_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!#$%&()*+-./:<=>?@[\\]^_{|}~"; //$NON-NLS-1$
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win"); //$NON-NLS-1$ //$NON-NLS-2$

    private static final JPopupMenu tbPopupMenu = new JPopupMenu();

    private static final Gson GSON = new Gson();

    /**
     * Compares a text IP address (IPv4 or IPv6) with an InetAddress instance
     *
     * @param textIP the text representation of IP (e.g., "192.168.1.1", "::1")
     * @param inetAddress the InetAddress to compare against
     * @return true if the IP addresses are equivalent, false otherwise
     */
    public static boolean ipCompare(String textIP, InetAddress inetAddress) {
        if (textIP == null || inetAddress == null)
            return false;
        try {
            // Normalize the text IP and resolve it to InetAddress
            InetAddress textIPAddress = InetAddress.getByName(textIP.trim());
            // Compare the two InetAddress objects
            return textIPAddress.equals(inetAddress);
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * Simple IPv6 compression using Java's built-in functionality
     * @param ipv6Address the IPv6 address to compress
     */
    public static String ipv6Compress(String ipv6Address) {
        if (ipv6Address == null)
        	return null;
        try {
            // Parse the address to ensure it's valid
            java.net.Inet6Address addr = (java.net.Inet6Address) java.net.InetAddress.getByName(ipv6Address.trim());

            // Get the full expanded form
            String fullForm = addr.getHostAddress();

            // Split into groups and compress manually
            String[] groups = fullForm.split(":"); //$NON-NLS-1$
            StringBuilder compressed = new StringBuilder();

            int zeroStart = -1, zeroLength = 0, maxStart = -1, maxLength = 0;

            // Find longest zero sequence
            for (int i = 0; i < groups.length; i++) {
                if (Integer.parseInt(groups[i], 16) == 0) {
                    if (zeroStart == -1) zeroStart = i;
                    zeroLength++;
                    if (zeroLength > maxLength) {
                        maxLength = zeroLength;
                        maxStart = zeroStart;
                    }
                } else {
                    zeroStart = -1;
                    zeroLength = 0;
                }
            }

            // Build compressed string
            for (int i = 0; i < groups.length; i++) {
                if (i == maxStart && maxLength > 1) {
                    compressed.append("::"); //$NON-NLS-1$
                    i += maxLength - 1;
                } else {
                    // Remove leading zeros
                    String group = groups[i].replaceFirst("^0+(?!$)", ""); //$NON-NLS-1$ //$NON-NLS-2$
                    compressed.append(group);
                    if (i < groups.length - 1 && !(i == maxStart - 1 && maxLength > 1)) {
                        compressed.append(":"); //$NON-NLS-1$
                    }
                }
            }

            return compressed.toString();
        } catch (Exception e) {
            return ipv6Address; // Return original if invalid
        }
    }

    /**
     * Converts a path to the current OS-specific format. For example, on Windows, converts a Linux path to a Windows path and vice versa.
     * @param path the path to convert
     * @return the converted path
     */
    public static String osSpecificPath(String path) {
        if (path == null)
        	return null;

        String convertedPath;
        if (IS_WINDOWS) {
            convertedPath = path.replace("/", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            // Linux/Unix - convert Windows paths
            convertedPath = path.replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
            // Remove Windows drive letters if present
            if (path.matches("^[A-Za-z]:\\\\.*")) //$NON-NLS-1$
                convertedPath = convertedPath.substring(2); // Remove "C:"
        }

        return convertedPath;
    }

	/**
	 * Set up the tray icon.
	 */
	public static TrayIcon setupTrayIcon() {
		try {
			if(!SystemTray.isSupported())
				return null;
			Image img = Toolkit.getDefaultToolkit().createImage(MainWindow.class.getResource("/icons/icon-16.png")); //$NON-NLS-1$
			TrayIcon trayIcon = new TrayIcon(img);
			trayIcon.setImageAutoSize(true);
			trayIcon.setToolTip(Messages.getString("Helper.TRAY_TOOLTIP_STARTING")); //$NON-NLS-1$
			SystemTray.getSystemTray().add(trayIcon);
			return trayIcon;
		} catch(Exception ex) {
			logError(ex, Messages.getString("Helper.TRAY_SETUP_ERROR")); //$NON-NLS-1$
			JOptionPane.showMessageDialog(MainWindow.getFrame(), Messages.getString("Helper.TRAY_SETUP_ERROR"), Messages.getString("Helper.MB_TRAY_SETUP_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}

    /**
     * Validate a string against a regex pattern.
     * @param regex The regex pattern
     * @param input The string to validate
     * @return True if the string matches the regex pattern.
     */
    public static boolean regexValidate(String regex, String input) {
    	try {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(input);
			return matcher.matches();
    	} catch (Exception ex) {
    		logError(ex, Messages.getString("Helper.ERROR_VALIDATING_REGEX") + regex); //$NON-NLS-1$
			return false;
		}
	}

    /**
     *
     * @param jsonString
     * @param path
     * @return
     */
    public static Object getValueFromJsonPath(String jsonString, String path) {
        try {
            // Parse the JSON strin
            JsonElement rootElement = GSON.fromJson(jsonString, JsonElement.class);

            // Split the path into individual elements
            List<String> pathElements = Arrays.asList(path.split("/")); //$NON-NLS-1$

            // Traverse the JSON structure
            JsonElement currentElement = rootElement;
            for (String pathElement : pathElements) {
                if (currentElement == null || currentElement.isJsonNull()) {
                    return null;
                }

                if (currentElement.isJsonObject()) {
                    JsonObject jsonObject = currentElement.getAsJsonObject();
                    currentElement = jsonObject.get(pathElement);
                } else if (currentElement.isJsonArray()) {
                    JsonArray jsonArray = currentElement.getAsJsonArray();
                    try {
                        int index = Integer.parseInt(pathElement);
                        if (index >= 0 && index < jsonArray.size()) {
                            currentElement = jsonArray.get(index);
                        } else {
                            return null; // Index out of bounds
                        }
                    } catch (NumberFormatException e) {
                        return null; // Invalid array index
                    }
                } else {
                    return null; // Cannot traverse further - not an object or array
                }
            }

            // Convert the final element to the appropriate Java type
            return convertJsonElementToJavaObject(currentElement);

        } catch (Exception ex) {
        	logError(ex, Messages.getString("Helper.INVALID_JSON_PATH") + path); //$NON-NLS-1$
            throw new IllegalArgumentException(Messages.getString("Helper.INVALID_JSON_PATH") + ex.getMessage(), ex); //$NON-NLS-1$
        }
    }

    /**
     *
     * @param element
     * @return
     */
    private static Object convertJsonElementToJavaObject(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }

        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();

            if (primitive.isString()) {
                return primitive.getAsString();
            } else if (primitive.isNumber()) {
                Number number = primitive.getAsNumber();

                // Try to return the most appropriate numeric type
                if (number.longValue() == number.doubleValue()) {
                    // It's an integer value
                    return number.longValue();
                } else {
                    // It's a floating-point value
                    return number.doubleValue();
                }
            } else if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
        } else if (element.isJsonObject() || element.isJsonArray()) {
            // Return complex objects as JSON strings
            return GSON.toJson(element);
        }

        return null;
    }

    /**
     * Gets non system tables from a SQLite database.
     * @param connection Database connection
     * @return List of all non system tables.
     * @throws SQLException
     */
    public static List<String> getNonSystemTables(Connection connection) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"}); //$NON-NLS-1$ //$NON-NLS-2$
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME"); //$NON-NLS-1$
            // Exclude SQLite system tables (usually start with "sqlite_")
            if (!tableName.startsWith("sqlite_")) //$NON-NLS-1$
                tableNames.add(tableName);
        }
        tables.close();
        return tableNames;
    }

	/**
	 * Remove all records marked for remove.
	 * @param connection Database connection.
	 * @param tableName Table name.
	 * @return Number of removed records.
	 * @throws SQLException
	 */
    public static int removeRecords(Connection connection, String tableName) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE removed != 0"; //$NON-NLS-1$ //$NON-NLS-2$
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return preparedStatement.executeUpdate();
        }
    }

    /**
     * Enable RTL when needed.
     * @param container Container of the components to enable RTL.
     */
    public static void enableRtlWhenNeeded(Container container) {
    	if(!isRTL(Locale.getDefault()))
    		return;
        container.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        for (Component component : container.getComponents())
            if (component instanceof Container)
            	enableRtlWhenNeeded((Container) component);
        container.revalidate();
        container.repaint();
    }

    /**
     * Checks whether a locale is RTL or not.
     * @param locale Locale to check.
     * @return true if RTL.
     */
    public static boolean isRTL(Locale locale) {
    	String name = locale.getDisplayName();
    	if(name.length() == 0)
    		return false;
        char firstChar = name.charAt(0);
        byte directionality = Character.getDirectionality(firstChar);
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
               directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    /**
     * Add a tab with a close button.
     * @param tabbedPane Tabbed pane to add the tab to.
     * @param scrollPane Scroll pane to add to the tab.
     * @param title Tab title.
     * @param closeListener Listener for the close button.
     */
    public static void addTabWithCloseButton(JTabbedPane tabbedPane, JScrollPane scrollPane, String title, ActionListener closeListener) {
        JPanel tabHeader = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				JButton closeButton = (JButton)getComponent(1);
				if(closeButton.isOpaque() || (tabbedPane.indexOfComponent(scrollPane) == tabbedPane.getSelectedIndex())) {
					closeButton.setText("x"); //$NON-NLS-1$
				} else closeButton.setText(""); //$NON-NLS-1$
				super.paintComponent(g);
			}
        };
        tabHeader.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        tabHeader.add(titleLabel, BorderLayout.LINE_START);

        JButton closeButton = new JButton(""); //$NON-NLS-1$
        closeButton.setFocusable(false);
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.setPreferredSize(new Dimension(16, 16));
        closeButton.setOpaque(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setForeground((Color.GRAY));
        tabHeader.add(closeButton, BorderLayout.LINE_END);

        closeButton.addActionListener(closeListener);

        tabbedPane.setTabComponentAt(tabbedPane.indexOfComponent(scrollPane), tabHeader);

        MouseAdapter mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { forwardMouseEvent(e, tabbedPane); }
            @Override
            public void mousePressed(MouseEvent e) { forwardMouseEvent(e, tabbedPane); }
            @Override
            public void mouseReleased(MouseEvent e) { forwardMouseEvent(e, tabbedPane); }
            @Override
            public void mouseDragged(MouseEvent e) { forwardMouseEvent(e, tabbedPane); }
            @Override
            public void mouseMoved(MouseEvent e) { forwardMouseEvent(e, tabbedPane); }
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setOpaque(true);
                closeButton.setContentAreaFilled(true);
                closeButton.setBorderPainted(true);
                closeButton.setForeground(closeButton.equals(e.getSource()) ? Color.RED : Color.GRAY);
                closeButton.setText("x"); //$NON-NLS-1$
				forwardMouseEvent(e, tabbedPane);
			}
            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setOpaque(false);
                closeButton.setContentAreaFilled(false);
                closeButton.setBorderPainted(false);
                closeButton.setForeground((Color.GRAY));
                closeButton.setText(tabbedPane.indexOfComponent(scrollPane) == tabbedPane.getSelectedIndex() ? "x" : ""); //$NON-NLS-1$ //$NON-NLS-2$
				forwardMouseEvent(e, tabbedPane);
			}
        };
        tabHeader.addMouseListener(mouseListener);
        closeButton.addMouseListener(mouseListener);
    }

    /**
     * Forward mouse events to the tabbed pane.
     * @param e Mouse event
     * @param tabbedPane Tabbed pane to forward the event to.
     */
    private static void forwardMouseEvent(MouseEvent e, JTabbedPane tabbedPane) {
        Point tabPoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), tabbedPane);
        MouseEvent forwardedEvent = new MouseEvent(
                tabbedPane,
                e.getID(),
                e.getWhen(),
                e.getModifiersEx(),
                tabPoint.x,
                tabPoint.y,
                e.getClickCount(),
                e.isPopupTrigger(),
                e.getButton()
        );
        tabbedPane.dispatchEvent(forwardedEvent);
    }

    /**
     * Show a confirm dialog with the possibility to define a default button.
     * @param parent Parent window
     * @param msg Message to show in the dialog.
     * @param title Title of the dialog.
     * @param buttons Buttons definition.
     * @param optionType Option type.
     * @param defButton Default button.
     * @return Index of the button pressed by the user.
     */
    public static int showConfirmDialog(Window parent, String msg, String title, int buttons, int optionType, int defButton) {
    	boolean cancellable = false;
        Object[] options;
        switch (buttons) {
            case JOptionPane.YES_NO_OPTION:
                options = new Object[]{Messages.getString("Helper.JOP_YES"), Messages.getString("Helper.JOP_NO")}; //$NON-NLS-1$ //$NON-NLS-2$
                break;
            case JOptionPane.YES_NO_CANCEL_OPTION:
                options = new Object[]{Messages.getString("Helper.JOP_YES"), Messages.getString("Helper.JOP_NO"), Messages.getString("Helper.JOP_CANCEL")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                cancellable = true;
                break;
            case JOptionPane.OK_CANCEL_OPTION:
                options = new Object[]{Messages.getString("Helper.JOP_OK"), Messages.getString("Helper.JOP_CANCEL")}; //$NON-NLS-1$ //$NON-NLS-2$
                cancellable = true;
                break;
            default:
                throw new IllegalArgumentException(String.format(Messages.getString("Helper.EX_INVALID_BUTTONS_VALUE"), buttons)); //$NON-NLS-1$
        }

        // Validate default button index
        if (defButton < 0 || defButton >= options.length) {
            throw new IllegalArgumentException(String.format(Messages.getString("Helper.EX_INVALID_DEF_BUTTON_INDEX"), defButton)); //$NON-NLS-1$
        }

        // Create JOptionPane
        JOptionPane optionPane = new JOptionPane(
                msg,
                JOptionPane.QUESTION_MESSAGE, // Use a valid message type
                JOptionPane.DEFAULT_OPTION,
                null,
                options,
                options[defButton]);

        // Create and show dialog
        JDialog dialog = optionPane.createDialog(parent, title);
        if(parent != null && parent instanceof JFrame) {
        	if(!parent.isVisible() || ((JFrame)parent).getState() == JFrame.ICONIFIED) {
        		dialog.setLocationRelativeTo(null);
        	} else dialog.setLocationRelativeTo(parent);
        } else dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        // Retrieve user selection
        Object selectedValue = optionPane.getValue();
        if (selectedValue instanceof String) {
            String selectedText = (String) selectedValue;
            if(selectedText.equals(Messages.getString("Helper.JOP_YES"))) //$NON-NLS-1$
            	return JOptionPane.YES_OPTION;
            if(selectedText.equals(Messages.getString("Helper.JOP_NO"))) //$NON-NLS-1$
                return JOptionPane.NO_OPTION;
            if(selectedText.equals(Messages.getString("Helper.JOP_CANCEL"))) //$NON-NLS-1$
                return JOptionPane.CANCEL_OPTION;
            if(selectedText.equals(Messages.getString("Helper.JOP_OK"))) //$NON-NLS-1$
                return JOptionPane.OK_OPTION;
        }
        if(cancellable) // When the user closes the confirm dialog either by pressing ESC or Alt-F4 or presses close button
        	return JOptionPane.CANCEL_OPTION;
        return JOptionPane.CLOSED_OPTION;
    }

    /**
	 * Add popup menu to text boxes for copy, cut, paste, select all.
	 * @param wnd Window to add the popup menu.
	 */
    public static void addPopupMenuToTextBoxes(Window wnd) {
	    Objects.requireNonNull(wnd);
	    if(tbPopupMenu.getComponentCount() == 0) {
		    JMenuItem mnuSelectAll = new JMenuItem(Messages.getString("Helper.MNU_SELALL")); //$NON-NLS-1$
		    mnuSelectAll.addActionListener(ev -> {
		    	((JTextComponent)tbPopupMenu.getInvoker()).selectAll();
		    });
		    tbPopupMenu.add(mnuSelectAll);
		    tbPopupMenu.addSeparator();
		    JMenuItem mnuCopy = new JMenuItem(Messages.getString("Helper.MNU_COPY")); //$NON-NLS-1$
		    mnuCopy.addActionListener(ev -> {
		    	((JTextComponent)tbPopupMenu.getInvoker()).copy();
		    });
		    tbPopupMenu.add(mnuCopy);
		    JMenuItem mnuCut = new JMenuItem(Messages.getString("Helper.MNU_CUT")); //$NON-NLS-1$
		    mnuCut.addActionListener(ev -> {
		    	((JTextComponent)tbPopupMenu.getInvoker()).cut();
		    });
		    tbPopupMenu.add(mnuCut);
		    JMenuItem mnuPaste = new JMenuItem(Messages.getString("Helper.MNU_PASTE")); //$NON-NLS-1$
		    mnuPaste.addActionListener(ev -> {
		    	((JTextComponent)tbPopupMenu.getInvoker()).paste();
		    });
		    tbPopupMenu.add(mnuPaste);
		    tbPopupMenu.addSeparator();
		    JMenuItem mnuTextDir = new JMenuItem(Messages.getString("Helper.MNU_TEXT_DIR")); //$NON-NLS-1$
		    mnuTextDir.addActionListener(ev -> {
		    	JTextComponent tc = (JTextComponent)tbPopupMenu.getInvoker();
		    	tc.setComponentOrientation(tc.getComponentOrientation().isLeftToRight() ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
		    	tc.invalidate();
		    	tc.repaint();
		    });
		    tbPopupMenu.add(mnuTextDir);

		    tbPopupMenu.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					JTextComponent tc = (JTextComponent)tbPopupMenu.getInvoker();
					mnuCopy.setEnabled(tc.getSelectedText() != null);
					mnuCut.setEnabled(mnuCopy.isEnabled());
					mnuPaste.setEnabled(Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor));
					mnuTextDir.setText(tc.getComponentOrientation().isLeftToRight() ? Messages.getString("Helper.MNU_RTL") : Messages.getString("Helper.MNU_LTR")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }
				@Override
				public void popupMenuCanceled(PopupMenuEvent e) { }
		    });
	    }
	    List<Component> components = getAllComponents(wnd);
	    for(Component component : components) {
	    	if(component instanceof JTextField || component instanceof JTextArea)
	    	((JComponent)component).setComponentPopupMenu(tbPopupMenu);
	    }
	}

    /**
     * Get all components in a container.
     * @param container Container of the components.
     * @return List of components.
     */
    public static List<Component> getAllComponents(Container container) {
        List<Component> components = new ArrayList<>();
        for (Component component : container.getComponents()) {
            components.add(component);
            if (component instanceof Container) {
                components.addAll(getAllComponents((Container) component));
            }
        }
        return components;
    }

    /**
     * Register a button to cancel a dialog by pressing ESC.
     * @param dlg Dialog to register the button to.
     * @param btn Button to register.
     */
    public static void registerCancelByEsc(JDialog dlg, JButton btn) {
    	Objects.requireNonNull(dlg);
		Objects.requireNonNull(btn);
	    KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	    dlg.getRootPane().registerKeyboardAction(e -> btn.doClick(), keyStroke, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

	/**
	 * Load an icon from resources and resize it.
	 * @param resourcePath Path to the icon in resources.
	 * @param width Width of the icon.
	 * @param height Height of the icon.
	 * @return Icon loaded from resources and resized.
	 */
   public static Icon loadIconFromResources(String resourcePath, int width, int height) {
       URL resourceURL = Helper.class.getResource(resourcePath);
       if (resourceURL == null)
           throw new IllegalArgumentException(String.format(Messages.getString("Helper.MNU_REZ_NOT_FOUND"), resourcePath)); //$NON-NLS-1$
       ImageIcon imageIcon = new ImageIcon(resourceURL);
       Image originalImage = imageIcon.getImage();
       Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
       return new ImageIcon(resizedImage);
   }

    /**
     * Escape HTML special characters.
     * @param input String to escape.
     * @return Escaped string.
     */
    public static String escapeHtml(String input) {
        if (input == null)
        	return null;
        return input.replace("&", "&amp;") //$NON-NLS-1$ //$NON-NLS-2$
                    .replace("<", "&lt;") //$NON-NLS-1$ //$NON-NLS-2$
                    .replace(">", "&gt;") //$NON-NLS-1$ //$NON-NLS-2$
                    .replace("\"", "&quot;") //$NON-NLS-1$ //$NON-NLS-2$
                    .replace("'", "&#39;"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Unescape HTML special characters.
     * @param input String to unescape.
     * @return Unescaped string.
     */
    public static String unescapeHtml(String input) {
        if (input == null)
        	return null;
        return input.replace("&lt;", "<") //$NON-NLS-1$ //$NON-NLS-2$
                    .replace("&gt;", ">") //$NON-NLS-1$ //$NON-NLS-2$
                    .replace("&quot;", "\"") //$NON-NLS-1$ //$NON-NLS-2$
                    .replace("&#39;", "'") //$NON-NLS-1$ //$NON-NLS-2$
                    .replace("&amp;", "&"); // "&" must be replaced last //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Check if the keystore password is correct.
     * @param pswd Password to check.
     * @return True if the password is correct.
     */
    public static boolean checkKeystorePassword(char[] pswd) {
		try (InputStream fos = new java.io.FileInputStream(CertGenDialog.KEYSTORE_PFX)) {
	        KeyStore keyStore = KeyStore.getInstance("PKCS12"); //$NON-NLS-1$
	        keyStore.load(fos, pswd);
			return true;
		} catch(Exception ex) {
			Helper.logError(ex, Messages.getString("Helper.LOG_PASSWORD_INCORRECT"), true); //$NON-NLS-1$
		}
		return false;
    }

    /**
     * Generate a random salt.
     * @param length Length of the salt.
     * @return Generated salt.
     */
    public static String generateSalt(int length) {
		if(length < 8)
			length = 8;
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[length];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hash a key using PBKDF2 with SHA-256.
     * @param key Key to hash
     * @param salt Salt to use for the hash
     * @param its Hash iterations
     * @param klen Hash key length
     * @return Hashed key as a base64 string
     * @throws Exception
     */
    public static String hashKey(String key, String saltB64, int its, int klen) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(key.toCharArray(), saltB64.getBytes(), its, klen);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); //$NON-NLS-1$
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Hash a key using PBKDF2 with SHA-256.
     * @param key Key to hash
     * @param saltB64 Salt to use for the hash in base64
     * @return Hashed key as a base64 string
     * @throws Exception
     */
	public static String hashKey(String key, String saltB64) throws Exception {
		return hashKey(key, saltB64, 65536, 256);
	}

	/**
	 * SHA-256 hash of a content
	 * @param content Content to hash
	 * @return Hashed content
	 * @throws Exception
	 */
    public static String hashString(String content) throws Exception {
	    Helper.requiresNotEmpty(content);
	    MessageDigest sha = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
	    return Base64.getEncoder().encodeToString(sha.digest(content.getBytes()));
    }

    /**
     * Generate a random secure key of a given length.
     * @param length Length of the key
     * @return Generated key
     */
    public static String generateSecureKey(int length) {
    	if(length < 8)
			length = 8;
        SecureRandom random = new SecureRandom();
        StringBuilder key = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            key.append(SECURE_CHARS.charAt(random.nextInt(SECURE_CHARS.length())));
        }
        return key.toString();
    }

    /**
     * Parses POST data encoded in application/x-www-form-urlencoded format into a map.
     *
     * @param body the raw body of the POST request
     * @return a map containing key-value pairs
     */
    public static Map<String, String> parsePostData(String body) {
        Map<String, String> params = new HashMap<>();
        if (!body.isEmpty()) {
            String[] pairs = body.split("&"); //$NON-NLS-1$
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2); // Split into key and value //$NON-NLS-1$
                String key = urlDecode(keyValue[0]);
                String value = keyValue.length > 1 ? urlDecode(keyValue[1]) : ""; // Value may be empty //$NON-NLS-1$
                params.put(key, value);
            }
        }
        return params;
    }

    /**
     * Decodes URL-encoded strings.
     *
     * @param value the string to decode
     * @return the decoded string
     */
    public static String urlDecode(String value) {
        try {
            return java.net.URLDecoder.decode(value, "UTF-8"); //$NON-NLS-1$
        } catch (Exception e) {
            return value;
        }
    }

	/**
	 * Load a table from the database and return the result as a list of rows.
	 * @param con Database connection
	 * @param tblName Table name
	 * @return List of rows
	 */
	public static List<Object[]> loadTable(Connection con, String tblName) {
		List<Object[]> list = null;
		try {
			PreparedStatement stmt = con.prepareStatement("SELECT * FROM " + tblName); //$NON-NLS-1$
			ResultSet rs = stmt.executeQuery();
			int rows = rs.getMetaData().getColumnCount();
			list = new ArrayList<>();
			while(rs.next()) {
				Object[] row = new Object[rows];
				for(int i = 0; i < row.length; i++)
					row[i] = rs.getObject(i + 1);
				list.add(row);
			}
		} catch (Exception ex) {
			logError(ex, Messages.getString("Helper.DB_TABLE_LOAD_ERROR") + tblName, true); //$NON-NLS-1$
			list = null;
		}
		return list;
	}

	/**
	 * Update the database with the given SQL statement.
	 * @param con Database connection
	 * @param sql SQL statement to execute
	 * @return Number of rows affected
	 */
	public static int dbUpdate(Connection con, String sql) {
		try(Statement st = con.createStatement()) {
			return st.executeUpdate(sql);
		} catch (SQLException ex) {
			logError(ex, String.format(Messages.getString("Helper.DB_UPDATE_ERROR"), sql), true); //$NON-NLS-1$
		}
		return -1;
	}

	/**
	 * Execute a query and return the result as a list of rows
	 * @param con Database connection
	 * @param sql SQL statement to execute
	 * @return List of rows
	 */
	public static List<Object[]> dbQuery(Connection con, String sql) {
		List<Object[]> rows = new ArrayList<>();
		try(Statement st = con.createStatement()) {
			ResultSet rs = st.executeQuery(sql);
			while(rs.next()) {
				if(rows == null)
					rows = new ArrayList<>();
				int nRows = rs.getMetaData().getColumnCount();
				Object[] row = new Object[nRows];
				for(int i = 0; i < row.length; i++)
					row[i] = rs.getObject(i + 1);
				rows.add(row);
			}
			return rows;
		} catch (SQLException ex) {
			logError(ex, String.format(Messages.getString("Helper.DB_QUERY_ERROR"), sql), true); //$NON-NLS-1$
		}
		return rows;
	}

	/**
	 * Compute the relative path between a file and a base directory.
	 * @param file The file to compute the relative path for.
	 * @param baseDir The base directory.
	 * @return The relative path
	 */
    public static String getRelativePath(File file, File baseDir) {
        try {
        	Objects.requireNonNull(file);
    		Objects.requireNonNull(baseDir);
            Path filePath = file.toPath().toAbsolutePath();
            Path basePath = baseDir.toPath().toAbsolutePath();
            return basePath.relativize(filePath).toString(); // Compute the relative path
        } catch (Exception ex) {
            logError(ex, Messages.getString("Helper.ERROR_RELATIVE_PATH") + file.getPath() + "\n" + baseDir.getPath(), false); //$NON-NLS-1$ //$NON-NLS-2$
            return file.getAbsolutePath();
        }
    }

	/**
	 * Create a file chooser with the given parameters.
	 * @param title The title of the file chooser
	 * @param dir The default directory
	 * @param filter The filter text for the file chooser
	 * @return The file chooser
	 */
	public static JFileChooser createFileChooser(String title, String dir, String filterText, String filterExt) {
		JFileChooser openFileChooser = new JFileChooser();
		openFileChooser.setCurrentDirectory(new java.io.File(dir));
		openFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		openFileChooser.setMultiSelectionEnabled(false);
		openFileChooser.setAcceptAllFileFilterUsed(false);
		openFileChooser.setFileFilter(new FileNameExtensionFilter(filterText, filterExt));
		openFileChooser.setDialogTitle(title);
		return openFileChooser;
	}

	/**
	 * Check if the given string is a valid IP address
	 * @param ip The string to check
	 * @return True if the string is a valid IP address
	 */
    public static boolean isValidIP(String ip) {
    	Objects.requireNonNull(ip);
    	if(ip.isEmpty())
    		return false;
        try {
            InetAddress.getByName(ip);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the given string is a valid URL
     * @param url The string to check
     * @return True if the string is a valid URL
     */
    public static boolean isValidURL(String url) {
		Objects.requireNonNull(url);
		if(url.isEmpty())
			return false;
        try {
            new URI(url).parseServerAuthority();
            return true; // Valid URL
        } catch (URISyntaxException ex) {
            return false; // Invalid URL
        }
    }

	/**
	 * Check if the given string is a number or not by trying to parse it as a double.
	 * @param str The string to check
	 * @return True if the string is a number
	 */
	public static boolean isNumeric(String str) {
    	Objects.requireNonNull(str);
    	if(str.isEmpty())
    		return false;
	    try {
	        Double.parseDouble(str);
	        return true;
	    } catch (NumberFormatException ex) {
	        return false;
	    }
	}

	/**
	 * Copy the given text to the clipboard
	 * @param text The text to copy
	 */
    public static boolean copyToClipboard(String text) {
    	try {
	        StringSelection stringSelection = new StringSelection(text);
	        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	        clipboard.setContents(stringSelection, null);
    	} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
    }

    /**
     * Get the text from the clipboard
     * @return The text from the clipboard
     */
    public static String getTextFromClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        	if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
        		return (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
    * Check if the given string is a valid email
    * @param email The string to check
    * @return True if the string is a valid email
    */
	public static boolean isValidEmail(String email) {
       // Regex for email validation
       String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*" + //$NON-NLS-1$
                           "@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"; //$NON-NLS-1$
       Pattern pattern = Pattern.compile(emailRegex);
       return email != null && pattern.matcher(email).matches();
   }

	/**
	 * Load an image from the resources folder
	 * @param fileName The name of the image
	 * @return The image
	 */
	public static BufferedImage resLoadImage(String fileName) {
		try {
			return javax.imageio.ImageIO.read(Helper.class.getResource(fileName));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Load a remote content.
	 * @param url Url to load the content from
	 * @return The content loaded, null on error.
	 */
	public static String loadRemoteContentGET(String url) {
		requiresNotEmpty(url);
		try {
			URL u = URI.create(url).toURL();
			HttpsURLConnection con = (HttpsURLConnection) u.openConnection();
			con.setRequestMethod("GET"); //$NON-NLS-1$
			con.setRequestProperty("User-Agent", "Mozilla/5.0"); //$NON-NLS-1$ //$NON-NLS-2$
			BufferedReader in = new BufferedReader(new java.io.InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null)
				content.append(inputLine);
			in.close();
			return content.toString();
		} catch (Exception ex) {
			logWarning(ex, String.format(Messages.getString("Helper.REMOTE_CONTENT_LOADING_ERROR"), url)); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Open the content in the default browser
	 * @param content The content to open
	 */
    public static void openContent(String content) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(new URI(content));
            } else logInfo(Messages.getString("Helper.DESKTOP_NOT_SUPPORTED"), true); //$NON-NLS-1$
        } catch (Exception ex) {
        	logWarning(ex, Messages.getString("Helper.AUTO_RUN_ERROR"), true); //$NON-NLS-1$
        }
    }

	/**
	 * Open the mail in the default mail client. Example: "mailto:2Hs0p@example.com&subject=Subject&body=Body"
	 * @param mailto The mail to open.
	 */
    public static void openMail(String mailto) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.mail(new URI(mailto));
            } else logInfo(Messages.getString("Helper.DESKTOP_NOT_SUPPORTED"), true); //$NON-NLS-1$
        } catch (Exception ex) {
        	logWarning(ex, Messages.getString("Helper.AUTO_RUN_ERROR"), true); //$NON-NLS-1$
        }
    }

	/**
	 * Throws an exception if the given string is null or empty
	 * @param str The string to check
	 * @return The string if it is not null or empty
	 */
	public static String requiresNotEmpty(String str) {
		if(str == null)
			throw new NullPointerException();
		if(str.length() == 0)
			throw new IllegalArgumentException();
		return str;
	}

	/**
	 * Throws an exception if the given string is null or empty with the given error message.
	 * @param str The string to check
	 * @param errMsg The error message
	 * @return The string if it is not null or empty
	 */
	public static String requiresNotEmpty(String str, String errMsg) {
		if(str == null)
			throw new NullPointerException(errMsg);
		if(str.length() == 0)
			throw new IllegalArgumentException(errMsg);
		return str;
	}

	/**
	 * Check if the given string is null or empty
	 * @param str The string to check
	 * @return True if the string is null or empty
	 */
	public static boolean isNullOrEmpty(String str) {
		if(str == null)
			return true;
		return str.length() == 0;
	}

	/**
	 * Get the date string from the given instant using dd/MM/yyyy HH:mm:ss format.
	 * @param instant The instant
	 * @return The date string
	 */
	public static String toDate(Instant instant) {
		return toDate(instant, "dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$
	}

	/**
	 * Get the date string from the given instant using the given format
	 * @param instant The instant
	 * @param format The format to use
	 * @return The date string
	 */
	public static String toDate(Instant instant, String format) {
		Objects.requireNonNull(instant);
        // Define the desired date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format)
                .withZone(ZoneId.systemDefault());

        // Format the Instant to the desired format
        return formatter.format(instant);
    }

	/**
	 * Get the size of the given bytes in a human readable format.
	 * @param bytes The bytes to get the size of
	 * @return The size of the bytes in a human readable format.
	 */
	public static String dataSize(long bytes) {
	    long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
	    if (absB < 1024) {
	        return bytes + " B"; //$NON-NLS-1$
	    }
	    long value = absB;
	    StringCharacterIterator ci = new StringCharacterIterator("KMGTPE"); //$NON-NLS-1$
	    for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
	        value >>= 10;
	        ci.next();
	    }
	    value *= Long.signum(bytes);
	    return String.format("%.1f %co", value / 1024.0, ci.current()); //$NON-NLS-1$
	}

	/**
	 * Get the list of files in the given directory with the given extension
	 * @param dir The directory to get the files from
	 * @return The list of files
	 */
	public static Set<String> listFiles(String dir, String filterExt) {
	    return Stream.of(new File(dir).listFiles())
	            .filter(file -> !file.isDirectory())
	            .filter(file -> file.getName().endsWith(filterExt))
	            .map(File::getName)
	            .collect(Collectors.toSet());
	}

	/**
	 * Sleep the current thread for the given number of milliseconds
	 * @param millis The number of milliseconds to sleep
	 */
	public static void threadSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ignored) { }
	}

	/**
	 * Check if the given file name is valid.
	 * @param fileName The file name
	 * @return True if the file name is valid
	 */
    public static boolean isValidDBName(String fileName) {
        // Define a regular expression pattern for valid file names without extension
        String regex = "^[^\\\\/:*?\"<>|.]{3,}$"; //$NON-NLS-1$

        // Compile the regular expression pattern
        Pattern pattern = Pattern.compile(regex);

        // Match the file name against the pattern
        Matcher matcher = pattern.matcher(fileName);

        // Return true if the file name matches the pattern, false otherwise
        return matcher.matches();
    }

    /**
     * Read the content of the given file and return it as a string.
     * @param filePath The file path
     * @return The file content of the given file.
     * @throws IOException
     */
    public static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
        }

        return content.toString();
    }

	/**
	 * Get the node by name
	 * @param nodeList The node list
	 * @param nodeName The node name
	 * @return The node with the given name, null on error.
	 */
    public static Node getNodeByName(NodeList nodeList, String nodeName) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(nodeName)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Get the node value
     * @param nodes The node list
     * @param nodeName The node name
     * @return The node value, empty string on error.
     */
    public static String getNodeValue(NodeList nodes, String nodeName) {
        Node node = getNodeByName(nodes, nodeName);
        if(node != null) {
	        node = node.getChildNodes().item(0);
	        if(node != null) {
	        	return node.getNodeValue();
	        }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Log the given message as info.
     * @param msg The message to log
     */
    public static void logInfo(String msg) { logInfo(msg, false); }

    /**
     * Log the given message as info
     * @param msg The message to log
     * @param showMsg Show the message in a message box.
     */
	public static void logInfo(String msg, boolean showMsg) {
		Helper.requiresNotEmpty(msg);
		String logMsg = String.format(Messages.getString("Helper.LOG_INFO_FORMAT"), Helper.toDate(Instant.now()), msg); //$NON-NLS-1$
		System.out.println(logMsg + System.lineSeparator());
		MainWindow.log("<p>" + logMsg + "</p>"); //$NON-NLS-1$ //$NON-NLS-2$
		if(showMsg)
			JOptionPane.showMessageDialog(getActiveWindow(), msg, Messages.getString("Helper.MB_LOG_INFO_TITLE"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
	}

	/**
	 * Log the given message as warning
	 * @param msg The message to log
	 */
	public static void logWarning(String msg) { logWarning(msg, false); }

	/**
	 * Log the given message as warning
	 * @param msg The message to log
	 * @param showMsg Show the message in a message box
	 */
	public static void logWarning(String msg, boolean showMsg) {
		Helper.requiresNotEmpty(msg);
		String logMsg = String.format(Messages.getString("Helper.LOG_WARNING_FORMAT"), Helper.toDate(Instant.now()), msg); //$NON-NLS-1$
		System.err.println(logMsg + System.lineSeparator());
		MainWindow.log("<p style=\"color:orange;\">" + logMsg + "</p>"); //$NON-NLS-1$ //$NON-NLS-2$
		if(showMsg)
			JOptionPane.showMessageDialog(getActiveWindow(), msg, Messages.getString("Helper.MB_LOG_WARN_TITLE"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
	}

	/**
	 * Log the given message as warning
	 * @param ex Exception to log
	 * @param msg The message to log
	 */
	public static void logWarning(Throwable ex, String msg) {
		if(ex == null)
			logWarning(msg);
		Helper.requiresNotEmpty(msg);
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		String logMsg = String.format(Messages.getString("Helper.LOG_WARNING_FORMAT_EX"), Helper.toDate(Instant.now()), ste.getModuleName(), ste.getClassName(), ste.getMethodName(), ste.getFileName(), ste.getLineNumber(), msg, ex.getClass().getSimpleName(), ex.getMessage()); //$NON-NLS-1$
		System.err.println(logMsg + System.lineSeparator());
		MainWindow.log("<p style=\"color:orange;\">" + logMsg + "</p>"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Log the given message as warning
	 * @param ex Exception to log
	 * @param msg The message to log
	 * @param showMsg Show the message in a message box
	 */
	public static void logWarning(Throwable ex, String msg, boolean showMsg) {
		if(ex == null)
			logWarning(msg, showMsg);
		Helper.requiresNotEmpty(msg);
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		String logMsg = String.format(Messages.getString("Helper.LOG_WARNING_FORMAT_EXX"), Helper.toDate(Instant.now()), ste.getModuleName(), ste.getClassName(), ste.getMethodName(), ste.getFileName(), ste.getLineNumber(), msg, ex.getClass().getSimpleName(), ex.getMessage()); //$NON-NLS-1$
		System.err.println(logMsg + System.lineSeparator());
		MainWindow.log("<p style=\"color:orange;\">" + logMsg + "</p>"); //$NON-NLS-1$ //$NON-NLS-2$
		if(showMsg)
			JOptionPane.showMessageDialog(getActiveWindow(), msg + "\n" + ex.getMessage(), Messages.getString("Helper.MB_LOG_WARN_TITLE"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Log the given message as error
	 * @param msg The message to log
	 */
	public static void logError(String msg) { logError(msg, false); }

	/**
	 * Log the given message as error
	 * @param msg The message to log
	 * @param showMsg Show the message in a message box
	 */
	public static void logError(String msg, boolean showMsg) {
		Helper.requiresNotEmpty(msg);
		String logMsg = String.format(Messages.getString("Helper.LOG_ERROR_FORMAT"), Helper.toDate(Instant.now()), msg); //$NON-NLS-1$
		System.err.println(logMsg + System.lineSeparator());
		MainWindow.log("<p style=\"color:red;\">" + logMsg + "</p>", true); //$NON-NLS-1$ //$NON-NLS-2$
		if(showMsg)
			JOptionPane.showMessageDialog(getActiveWindow(), msg, Messages.getString("Helper.MB_LOG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
	}

	/**
	 * Log the given message as error
	 * @param ex Exception to log
	 * @param msg The message to log
	 */
	public static void logError(Throwable ex, String msg) {
		if(ex == null)
			logError(msg);
		Helper.requiresNotEmpty(msg);
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		String logMsg = String.format(Messages.getString("Helper.LOG_ERROR_FORMAT_EX"), Helper.toDate(Instant.now()), ste.getModuleName(), ste.getClassName(), ste.getMethodName(), ste.getFileName(), ste.getLineNumber(), msg, ex.getClass().getSimpleName(), ex.getMessage()); //$NON-NLS-1$
		System.err.println(logMsg + System.lineSeparator());
		MainWindow.log("<p style=\"color:red;\">" + logMsg + "</p>", true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Log the given message as error
	 * @param ex Exception to log
	 * @param msg The message to log
	 * @param showMsg Show the message in a message box
	 */
	public static void logError(Throwable ex, String msg, boolean showMsg) {
		if(ex == null)
			logError(msg, showMsg);
		Helper.requiresNotEmpty(msg);
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		String logMsg = String.format(Messages.getString("Helper.LOG_ERROR_FORMAT_EXX"), Helper.toDate(Instant.now()), ste.getModuleName(), ste.getClassName(), ste.getMethodName(), ste.getFileName(), ste.getLineNumber(), msg, ex.getClass().getSimpleName(), ex.getMessage()); //$NON-NLS-1$
		System.err.println(logMsg + System.lineSeparator());
		MainWindow.log("<p style=\"color:red;\">" + logMsg + "</p>"); //$NON-NLS-1$ //$NON-NLS-2$
		ex.printStackTrace();
		if(showMsg)
			JOptionPane.showMessageDialog(getActiveWindow(), msg + "\n" + ex.getMessage(), Messages.getString("Helper.MB_LOG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Get the active frame
	 * @return The active frame
	 */
    public static Window getActiveWindow() {
        Window window = FocusManager.getCurrentManager().getActiveWindow();
        if (window != null)
            return window;
        return MainWindow.getFrame();
    }

    /**
     * Get the application version
     * @return The application version
     */
    public static String getAppVersion() {
        Properties properties = new Properties();

        try (InputStream input = Helper.class.getClassLoader().getResourceAsStream("build.properties")) { //$NON-NLS-1$
            properties.load(input);
            String version = properties.getProperty("app.version", "X.X"); //$NON-NLS-1$ //$NON-NLS-2$
            String buildNumber = properties.getProperty("build.number", "X"); //$NON-NLS-1$ //$NON-NLS-2$
            return version + "." + buildNumber; //$NON-NLS-1$
        } catch (IOException ex) {
        	logError(ex, Messages.getString("Helper.LOG_ERROR_LOADING_BUILD_PROPERTIES")); //$NON-NLS-1$
            return "X.X.X"; //$NON-NLS-1$
        }
    }
}


















