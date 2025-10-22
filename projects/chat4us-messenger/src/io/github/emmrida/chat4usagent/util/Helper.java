/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.util;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
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
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import io.github.emmrida.chat4usagent.gui.CertGenDialog;
import io.github.emmrida.chat4usagent.gui.MainWindow;

/**
 * A collection of static utility methods
 *
 * @author El Mhadder Mohamed Rida
 */
public class Helper {
	private static final String SECURE_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!#$%&()*+-./:<=>?@[\\]^_{|}~"; //$NON-NLS-1$

	private static final JPopupMenu tbPopupMenu = new JPopupMenu();

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
			JOptionPane.showMessageDialog(MainWindow.getFrame(), Messages.getString("Helper.TRAY_SETUP_ERROR"), Messages.getString("Helper.TRAY_SETUP_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
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
	 * Enables RTL when needed. Depends on current locale.
	 * @param container Parent component to enable RTL and its children
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
	 * Checks if current locale is RTL
	 * @param locale The locale to check
	 * @return True if RTL
	 */
	public static boolean isRTL(Locale locale) {
		char firstChar = locale.getDisplayName(locale).charAt(0);
		byte directionality = Character.getDirectionality(firstChar);
		return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
	}

	/**
	 * Adds popup menu to text boxes and text areas in a window.
	 * @param wnd The window
	 */
	public static void addPopupMenuToTextBoxes(Window wnd) {
		Objects.requireNonNull(wnd);
		if(tbPopupMenu.getComponentCount() == 0) {
			JMenuItem mnuCut = new JMenuItem(Messages.getString("Helper.PMNU_CUT")); //$NON-NLS-1$
			mnuCut.addActionListener(ev -> {
				((JTextComponent)tbPopupMenu.getInvoker()).cut();
			});
			JMenuItem mnuCopy = new JMenuItem(Messages.getString("Helper.PMNU_COPY")); //$NON-NLS-1$
			mnuCopy.addActionListener(ev -> {
				((JTextComponent)tbPopupMenu.getInvoker()).copy();
			});
			JMenuItem mnuPaste = new JMenuItem(Messages.getString("Helper.PMNU_PASTE")); //$NON-NLS-1$
			mnuPaste.addActionListener(ev -> {
				((JTextComponent)tbPopupMenu.getInvoker()).paste();
			});
			JMenuItem mnuSelectAll = new JMenuItem(Messages.getString("Helper.PMNU_SEL_ALL")); //$NON-NLS-1$
			mnuSelectAll.addActionListener(ev -> {
				((JTextComponent)tbPopupMenu.getInvoker()).selectAll();
			});
			tbPopupMenu.add(mnuSelectAll);
			tbPopupMenu.addSeparator();
			tbPopupMenu.add(mnuCopy);
			tbPopupMenu.add(mnuCut);
			tbPopupMenu.add(mnuPaste);
			tbPopupMenu.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					mnuCopy.setEnabled(((JTextComponent)tbPopupMenu.getInvoker()).getSelectedText() != null);
					mnuCut.setEnabled(mnuCopy.isEnabled());
					mnuPaste.setEnabled(Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor));
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
	 * Recursively gets all components in a container
	 * @param container The container
	 * @return The list of components
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
	 * Registers a key stroke to cancel a dialog
	 * @param dlg The dialog
	 * @param btn The button to click
	 */
	public static void registerCancelByEsc(JDialog dlg, JButton btn) {
		Objects.requireNonNull(dlg);
		Objects.requireNonNull(btn);
		KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		dlg.getRootPane().registerKeyboardAction(e -> btn.doClick(), keyStroke, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	/**
	 * Sets up the editor pane
	 * @param editorPane The editor pane
	 */
	public static void setupEditorPane(JEditorPane editorPane) {
		editorPane.setContentType("text/html"); //$NON-NLS-1$
		HTMLEditorKit kit = new HTMLEditorKit();
		editorPane.setEditorKit(kit);
		StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet.addRule("body {word-wrap: break-word; font-family: sans-serif; margin: 3px;}"); //$NON-NLS-1$
	}

	/**
	 * Gets the local time
	 * @return The local time
	 */
	public static String getLocalTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); //$NON-NLS-1$
		return sdf.format(new Date());
	}

	/**
	 * Gets the local time
	 * @param time The time
	 * @return The local time
	 */
	public static String getLocalTime(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); //$NON-NLS-1$
		return sdf.format(new Date(time));
	}

	/**
	 * Gets the duration between two times.
	 * @param start The start time
	 * @param end The end time
	 * @return The duration in mm:ss
	 */
	public static String getDuration(long start, long end) {
		long diff = end - start;
		long seconds = diff / 1000;
		long minutes = seconds / 60;
		seconds = seconds % 60;
		SimpleDateFormat sdf = new SimpleDateFormat("mm:ss"); //$NON-NLS-1$
		return sdf.format(new Date(minutes * 60 * 1000 + seconds * 1000));
	}

	/**
	* Checks the keystore password
	* @param pswd The password
	* @return True if the password is correct
	*/
	public static boolean checkKeystorePassword(char[] pswd) {
			try (InputStream fos = new java.io.FileInputStream(CertGenDialog.KEYSTORE_PFX)) {
				KeyStore keyStore = KeyStore.getInstance("PKCS12"); //$NON-NLS-1$
				keyStore.load(fos, pswd);
				return true;
			} catch(Exception ex) {
				Helper.logError(ex, Messages.getString("Helper.WRONG_AUTH_ERROR"), false); //$NON-NLS-1$
			}
			return false;
	}

	/**
	 * Generates a salt for password hashing
	 * @param length The length of the salt
	 * @return The salt
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
	 * Hashes a key using PBKDF2 with SHA-256
	 * @param key The key to hash
	 * @param salt The salt to use
	 * @param its Hash iterations
	 * @param klen Hash key length
	 * @return The hashed key as a base64 string
	 * @throws Exception
	 */
	public static String hashKey(String key, String saltB64, int its, int klen) throws Exception {
		PBEKeySpec spec = new PBEKeySpec(key.toCharArray(), saltB64.getBytes(), its, klen);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); //$NON-NLS-1$
		byte[] hash = factory.generateSecret(spec).getEncoded();
		return Base64.getEncoder().encodeToString(hash);
	}

	/**
	 * Hashes a key using PBKDF2 with SHA-256
	 * @param key The key to hash
	 * @param saltB64 The salt to use
	 * @return The hashed key as a base64 string
	 * @throws Exception
	 */
	public static String hashKey(String key, String saltB64) throws Exception {
		return hashKey(key, saltB64, 65536, 256);
	}

	/**
	 * SHA-256 hash of a content
	 * @param content The content to hash
	 * @return The hashed content as a base64 string
	 * @throws Exception
	 */
	public static String hashString(String content) throws Exception {
		Helper.requiresNotEmpty(content);
		MessageDigest sha = MessageDigest.getInstance("SHA-256"); //$NON-NLS-1$
		return Base64.getEncoder().encodeToString(sha.digest(content.getBytes()));
	}

	/**
	 * Generates a secure key
	 * @param length The length
	 * @return The key
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
	 * Returns the relative path of a file to a base directory
	 * @param file The file
	 * @param baseDir The base directory
	 * @return The relative path
	 */
	public static String getRelativePath(File file, File baseDir) {
		try {
			Path filePath = file.toPath().toAbsolutePath();
			Path basePath = baseDir.toPath().toAbsolutePath();
			return basePath.relativize(filePath).toString(); // Compute the relative path
		} catch (Exception ex) {
			ex.printStackTrace();
			return file.getAbsolutePath();
		}
	}

	/**
	 * Creates a file chooser
	 * @param title The title
	 * @param dir Default directory
	 * @param filter The filter to apply to the file chooser
	 * @return
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
	 * Checks if an IP is valid
	 * @param ip The IP
	 * @return True if the IP is valid
	 */
	public static boolean isValidIP(String ip) {
		if(ip == null)
			return false;
		try {
			InetAddress.getByName(ip);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Checks if a URL is valid
	 * @param url The URL to check
	 * @return True if the URL is valid
	 */
	public static boolean isValidURL(String url) {
		if(url == null)
			return false;
		try {
			new URI(url).parseServerAuthority();
			return true; // Valid URL
		} catch (URISyntaxException ex) {
			return false; // Invalid URL
		}
	}

	/**
	 * Checks if a string is numeric
	 * @param str The string
	 * @return True if the string is numeric
	 */
	public static boolean isNumeric(String str) {
		if(str == null)
			return false;
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	/**
	 * Copies text to the clipboard
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
	 * Retrieves text from the clipboard
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
	* Checks if an email is valid
	* @param email The email to check
	* @return True if the email is valid
	*/
	public static boolean isValidEmail(String email) {
		// Regex for email validation
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*" + //$NON-NLS-1$
							"@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"; //$NON-NLS-1$
		Pattern pattern = Pattern.compile(emailRegex);
		return email != null && pattern.matcher(email).matches();
	}

	/**
	 * Load an image from resources
	 * @param fileName The file name
	 * @return The image or null
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
	 * Load a remote content
	 * @param url Url to load the content
	 * @return The content or null
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
			logWarning(ex, Messages.getString("Helper.LOAD_CONTENT_ERROR") + url); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Open a content in the default browser/app
	 * @param content The content to open
	 */
	public static void openContent(String content) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				desktop.browse(new URI(content));
			} else logInfo(Messages.getString("Helper.UNSUPPORTED_DESKTOP_ERROR"), true); //$NON-NLS-1$
		} catch (Exception ex) {
			logWarning(ex, Messages.getString("Helper.SYS_OPEN_ERROR"), true); //$NON-NLS-1$
		}
	}

	/**
	 * Checks if a string is not null or empty
	 * @param str The string to check
	 * @return The string
	 */
	public static String requiresNotEmpty(String str) {
		if(str == null)
			throw new NullPointerException();
		if(str.length() == 0)
			throw new IllegalArgumentException();
		return str;
	}

	/**
	 * Checks if a string is not null or empty
	 * @param str The string to check
	 * @param errMsg The error message
	 * @return The string or throws an exception
	 */
	public static String requiresNotEmpty(String str, String errMsg) {
		if(str == null)
			throw new NullPointerException(errMsg);
		if(str.length() == 0)
			throw new IllegalArgumentException(errMsg);
		return str;
	}

	/**
	 * Checks if a string is null or empty
	 * @param str The string to check
	 * @return True if the string is null or empty
	 */
	public static boolean isNullOrEmpty(String str) {
		if(str == null)
			return true;
		return str.length() == 0;
	}

	/**
	 * Converts a number of bytes to a human readable format
	 * @param bytes The number of bytes
	 * @return The human readable format
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
	 * List the files in a directory with a specific extension
	 * @param dir The directory
	 * @param filterExt The extension
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
	 * Sleeps the thread for a certain amount of time
	 * @param millis The amount of time to sleep in milliseconds
	 */
	public static void threadSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ignored) { }
	}

	/**
	 * Reads a file and returns its content
	 * @param filePath The file path
	 * @return The file content
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
	 * Logs an info
	 * @param msg The message to log
	 */
	public static void logInfo(String msg) { logInfo(msg, false); }

	/**
	 * Logs an info and shows a message dialog if showMsg is true
	 * @param msg The message to log
	 * @param showMsg True to show a message dialog
	 */
	public static void logInfo(String msg, boolean showMsg) {
		Helper.requiresNotEmpty(msg);
		String logMsg = String.format(Messages.getString("Helper.LOG_INFO"), Helper.toDate(Instant.now()), msg); //$NON-NLS-1$
		System.out.printf(logMsg);
		MainWindow.log(logMsg);
		if(showMsg)
			JOptionPane.showMessageDialog(MainWindow.getFrame(), msg, Messages.getString("Helper.DLG_TITLE_INFO"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
	}

	/**
	 * Logs a warning
	 * @param msg The message to log
	 */
	public static void logWarning(String msg) { logWarning(msg, false); }

	/**
	 * Logs a warning and shows a message dialog if showMsg is true
	 * @param msg The message to log
	 * @param showMsg True to show a message dialog
	 */
	public static void logWarning(String msg, boolean showMsg) {
		Helper.requiresNotEmpty(msg);
		String logMsg = String.format(Messages.getString("Helper.LOG_WARNING"), Helper.toDate(Instant.now()), msg); //$NON-NLS-1$
		System.err.printf(logMsg);
		MainWindow.log(logMsg);
		if(showMsg)
			JOptionPane.showMessageDialog(MainWindow.getFrame(), msg, Messages.getString("Helper.DLG_TITLE_WARNING"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
	}

	/**
	 * Logs a warning with an exception
	 * @param ex The exception to log
	 * @param msg The message to log
	 */
	public static void logWarning(Throwable ex, String msg) {
		if(ex == null)
			logWarning(msg);
		Helper.requiresNotEmpty(msg);
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		String logMsg = String.format(Messages.getString("Helper.LOG_WARNING_EX"), Helper.toDate(Instant.now()), ste.getModuleName(), ste.getClassName(), ste.getMethodName(), ste.getFileName(), ste.getLineNumber(), msg, ex.getClass().getSimpleName(), ex.getMessage()); //$NON-NLS-1$
		System.err.printf(logMsg);
		MainWindow.log(logMsg);
	}

	/**
	 * Logs a warning with an exception and shows a message dialog if showMsg is true
	 * @param ex The exception to log
	 * @param msg The message to log
	 * @param showMsg True to show a message dialog
	 */
	public static void logWarning(Throwable ex, String msg, boolean showMsg) {
		if(ex == null)
			logWarning(msg, showMsg);
		Helper.requiresNotEmpty(msg);
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		String logMsg = String.format(Messages.getString("Helper.LOG_WARNING_EX"), Helper.toDate(Instant.now()), ste.getModuleName(), ste.getClassName(), ste.getMethodName(), ste.getFileName(), ste.getLineNumber(), msg, ex.getClass().getSimpleName(), ex.getMessage()); //$NON-NLS-1$
		System.err.printf(logMsg);
		MainWindow.log(logMsg);
		if(showMsg)
			JOptionPane.showMessageDialog(MainWindow.getFrame(), msg + "\n" + ex.getMessage(), Messages.getString("Helper.DLG_TITLE_WARNING"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Logs an error
	 * @param msg The message to log
	 */
	public static void logError(String msg) { logError(msg, false); }

	/**
	 * Logs an error and shows a message dialog if showMsg is true
	 * @param msg The message to log
	 * @param showMsg True to show a message dialog
	 */
	public static void logError(String msg, boolean showMsg) {
		Helper.requiresNotEmpty(msg);
		String logMsg = String.format(Messages.getString("Helper.LOG_ERROR"), Helper.toDate(Instant.now()), msg); //$NON-NLS-1$
		System.err.printf(logMsg);
		MainWindow.log(logMsg);
		if(showMsg)
			JOptionPane.showMessageDialog(MainWindow.getFrame(), msg, Messages.getString("Helper.DLG_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
	}

	/**
	 * Logs an error with an exception
	 * @param ex The exception to log
	 * @param msg The message to log
	 */
	public static void logError(Throwable ex, String msg) {
		if(ex == null)
			logError(msg);
		Helper.requiresNotEmpty(msg);
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		String logMsg = String.format(Messages.getString("Helper.LOG_ERROR_EX"), Helper.toDate(Instant.now()), ste.getModuleName(), ste.getClassName(), ste.getMethodName(), ste.getFileName(), ste.getLineNumber(), msg, ex.getClass().getSimpleName(), ex.getMessage()); //$NON-NLS-1$
		System.err.printf(logMsg);
		MainWindow.log(logMsg);
	}

	/**
	 * Logs an error with an exception and shows a message dialog if showMsg is true
	 * @param ex The exception to log
	 * @param msg The message to log
	 * @param showMsg True to show a message dialog
	 */
	public static void logError(Throwable ex, String msg, boolean showMsg) {
		if(ex == null)
			logError(msg, showMsg);
		Helper.requiresNotEmpty(msg);
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		String logMsg = String.format(Messages.getString("Helper.LOG_ERROR_EX"), Helper.toDate(Instant.now()), ste.getModuleName(), ste.getClassName(), ste.getMethodName(), ste.getFileName(), ste.getLineNumber(), msg, ex.getClass().getSimpleName(), ex.getMessage()); //$NON-NLS-1$
		System.err.printf(logMsg);
		MainWindow.log(logMsg);
		if(showMsg)
			JOptionPane.showMessageDialog(MainWindow.getFrame(), msg + "\n" + ex.getMessage(), Messages.getString("Helper.DLG_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Loads an icon from resources and resizes it
	 * @param resourcePath The path to the icon in resources folder
	 * @param width The width of the icon to return
	 * @param height The height of the icon to return
	 * @return The icon
	 */
	public static Icon loadIconFromResources(String resourcePath, int width, int height) {
		URL resourceURL = Helper.class.getResource(resourcePath);
		if (resourceURL == null)
			throw new IllegalArgumentException(Messages.getString("Helper.RES_NOT_FOUND") + resourcePath); //$NON-NLS-1$
		ImageIcon imageIcon = new ImageIcon(resourceURL);
		Image originalImage = imageIcon.getImage();
		Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		return new ImageIcon(resizedImage);
	}
}
