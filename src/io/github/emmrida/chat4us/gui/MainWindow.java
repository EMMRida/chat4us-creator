/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.gui;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.MenuItem;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import io.github.emmrida.chat4us.controls.ChatServerListCellRenderer;
import io.github.emmrida.chat4us.controls.ChatServerListModel;
import io.github.emmrida.chat4us.controls.IdLabelComboElement;
import io.github.emmrida.chat4us.controls.IdLabelComboModel;
import io.github.emmrida.chat4us.controls.IdLabelComboRenderer;
import io.github.emmrida.chat4us.core.ChatClient;
import io.github.emmrida.chat4us.core.ChatServer;
import io.github.emmrida.chat4us.core.ChatServer.AiServer;
import io.github.emmrida.chat4us.core.ChatServer.ChatServerListener;
import io.github.emmrida.chat4us.core.ChatSession.ChatSessionState;
import io.github.emmrida.chat4us.core.IChatModelClient;
import io.github.emmrida.chat4us.internalclient.InternalClientFrame;
import io.github.emmrida.chat4us.ria.NodePanel;
import io.github.emmrida.chat4us.ria.RiaEditorPanel;
import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.HotkeyManager;
import io.github.emmrida.chat4us.util.Messages;
import io.github.emmrida.chat4us.util.Settings;
import io.github.emmrida.chat4us.util.TeePrintStream;
import io.github.emmrida.chat4us.util.UExceptionDialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JLabel;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;
import javax.swing.JMenu;
import javax.swing.JScrollPane;
import javax.swing.JList;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.event.ChangeEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import javax.swing.JEditorPane;
import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListModel;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

/**
 * This class represents the main window of the application.
 *
 * @author El Mhadder Mohamed Rida
 */
public class MainWindow {
	public static final String CHATBOTS_ROOT_FOLDER = "./chatbots"; //$NON-NLS-1$

	private static JFrame mainFrame;
	private static MainWindow mainWindow;
	private static Settings settings;
	private static BufferedImage imgBkg = null;
	private static boolean selfSigned = false;

	private boolean dbOptimize = false;
	private Connection conChat4Us;
	private List<Object[]> aiGroups;
	private List<Object[]> aiServers;
	private List<Object[]> agPostes;
	private Timer timer;
	private HotkeyManager hotKeyManager;
	private TrayIcon trayIcon;

	private JFileChooser riaFileChooser = null;

	JPopupMenu lstSelBotPopup = null;
	JPopupMenu lstNoSelBotPopup = null;
	JPopupMenu lstNoSelWebsitePopup = null;
	JPopupMenu lstSelWebsitePopup = null;
	JPopupMenu lstNoSelAgentPopup = null;
	JPopupMenu lstSelAgentPopup = null;

	private JEditorPane epLog;
	private JTabbedPane tabbedPaneRight;
	private JMenu mnuChatBotAI;
	private JList<ChatServer> lstChatBots;
	private JList<IdLabelComboElement> lstWebsites;
	private JList<IdLabelComboElement> lstAgents;
	private JTabbedPane tabbedPaneLeft;
	private JCheckBoxMenuItem mnuWebsiteDeActivate;
	private JMenuItem mnuWebsiteEdit;
	private JMenuItem mnuWebsiteRemove;
	private JMenu mnuServerAssistants;
	private JMenuItem mnuServerRemove;
	private JMenuItem mnuServerEdit;
	private JCheckBoxMenuItem mnuServerDeActivate;
	private JCheckBoxMenuItem mnuAgentDeActivate;
	private JMenuItem mnuAgentEdit;
	private JMenuItem mnuAgentRemove;
	private JMenu mnuOpenedList;
	private JMenuItem mnuNewChatBot;
	private JSplitPane splitPaneHorizontal;
	private JSplitPane splitPaneVertical;
	private JLabel lblChatBotsCount;
	private JLabel lblAgentsCount;
	private JLabel lblWebsitesCount;
	private JButton btnNew;
	private JButton btnOpen;
	private JButton btnSave;
	private JButton btnSaveAs;
	private JButton btnUndo;
	private JButton btnRedo;
	private JButton btnCopy;
	private JButton btnCut;
	private JButton btnPaste;
	private JButton btnSettings;
	private JMenuItem mnuNewDoc;
	private JMenuItem mnuOpenDoc;
	private JMenuItem mnuSaveDoc;
	private JMenuItem mnuSaveAsDoc;
	private JMenuItem mnuCloseDoc;
	private JMenu mnuRecentDoc;
	private JMenu mnNewMenu_1;
	private JMenuItem mnuCleanLogFiles;
	private JMenuItem mnuCleanChatFiles;
	private JLabel lblChatsIcon;
	private JLabel lblChatsCount;
	private JLabel lblStatusText;
	private JMenuItem mnuCleanDatabase;
	private JMenu mnNewMenu_6;
	private JMenuItem mnuCertImport;
	private JMenuItem mnuOpenGuide;
	private JMenuItem mnuSettings;
	private JMenuItem mnuTutorials;
	private JMenuItem mnuExamples;
	private JMenuItem mnuSampleProjects;
	private JMenuItem mnuGetStarted;
	private JMenuItem mnuCheck4Updates;
	private JMenuItem mnuContribute;
	private JMenuItem mnuInternalChatClient;
	private JMenuItem mnuOpenInEditor;
	private JMenuItem mnuServerRestart;

	/**
	 * Gets the main frame.
	 *
	 * @return the main frame
	 */
	public static JFrame getFrame() { return mainFrame; }

	/**
	 * Gets the single instance of MainWindow.
	 *
	 * @return single instance of MainWindow
	 */
	public static MainWindow getInstance() { return mainWindow; }

	/**
	 * Gets the chat bot menu item.
	 *
	 * @return the chat bot menu item
	 */
	public static JMenu getChatBotMenuItem() { return mainWindow.mnuChatBotAI; }

	/**
	 * Gets the DB connection.
	 *
	 * @return the DB connection
	 */
	public static Connection getDBConnection() { return mainWindow.conChat4Us; }

	/**
	 * Gets the settings.
	 *
	 * @return the settings
	 */
	public static Settings getSettings() { return MainWindow.settings; }

	/**
	 * Sets the status text.
	 *
	 * @param text the new status text
	 */
	public static void setStatusText(String text) { if(text != null) mainWindow.lblStatusText.setText(text); }

	public static TrayIcon getTrayIcon() { return mainWindow.trayIcon; }

	public static boolean isSelfSigned() { return selfSigned; }

	/**
	 * Show log message in the log window and append it to the log file.
	 *
	 * @param msg the msg to log
	 */
	public static void log(String msg) {
		Helper.requiresNotEmpty(msg);
		SwingUtilities.invokeLater(() -> {
			if(mainWindow != null) {
				final String html = msg.replaceAll("\n", "<br>").replaceAll("\r", "").replaceAll("\t", "&ensp;&ensp;&ensp;"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				try {
					HTMLEditorKit kit = (HTMLEditorKit)mainWindow.epLog.getEditorKit();
					HTMLDocument doc = (HTMLDocument)mainWindow.epLog.getDocument();
					kit.insertHTML(doc, doc.getLength(), html, 0, 0, null);
					if(!mainWindow.epLog.hasFocus())
						mainWindow.epLog.setCaretPosition(doc.getLength());
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	/**
	 * Show log message in the log window then display a system tray notification if enabled.
	 * @param msg The message to log and display
	 * @param notify Whether to display a system tray notification or not.
	 */
	public static void log(String msg, boolean notify) {
		log(msg);
		if(mainWindow.trayIcon != null && settings.isNotifyOnErrors())
			mainWindow.trayIcon.displayMessage(Messages.getString("MainWindow.TI_ERROR_NOTIFICATION"), msg, TrayIcon.MessageType.ERROR); //$NON-NLS-1$
	}

	/**
	 * Gets the AI groups list.
	 *
	 * @return the AI groups list.
	 */
	public static List<Object[]> getAIGroups() { return mainWindow.aiGroups; }

	/**
	 * Gets the AI group by id.
	 *
	 * @param id the id
	 * @return the AI group record or null.
	 */
	public static Object[] getAIGroupById(int id) {
		for(Object[] o : mainWindow.aiGroups) {
			if(((int)o[0]) == id) return o;
		}
		return null;
	}

	/**
	 * Gets the AI servers list.
	 *
	 * @return the AI servers
	 */
	public static List<Object[]> getAIServers() { return mainWindow.aiServers; }

	/**
	 * Gets the AI server by id.
	 *
	 * @param id the id
	 * @return the AI server record or null
	 */
	public static Object[] getAIServerById(int id) {
		for(Object[] o : mainWindow.aiServers) {
			if(((int)o[0]) == id) return o;
		}
		return null;
	}

	/**
	 * Gets the agents postes.
	 *
	 * @return the agents postes
	 */
	public static List<Object[]> getAgentsPostes() { return mainWindow.agPostes; }

	/**
	 * Gets the agent poste by id.
	 *
	 * @param id the id
	 * @return the agent poste record or null
	 */
	public static Object[] getAgentPosteById(int id) {
		for(Object[] o : mainWindow.agPostes) {
			if(((int)o[0]) == id) return o;
		}
		return null;
	}

	/**
	 * Close route editor tab.
	 *
	 * @param index the index of the tab to close
	 */
	public void closeRouteEditorTab(int index) {
		JScrollPane sp;
        RiaEditorPanel editor;
		tabbedPaneRight.removeTabAt(index);
		for(int i = 0; i < tabbedPaneRight.getTabCount(); i++) {
			sp = (JScrollPane)tabbedPaneRight.getComponentAt(i);
			editor = (RiaEditorPanel)sp.getViewport().getView();
			editor.setTabIndex(i);
		}
		SwingUtilities.invokeLater(() -> {
			int i = tabbedPaneRight.getSelectedIndex();
			if(i >= 0)
				((JScrollPane)tabbedPaneRight.getComponentAt(i)).getViewport().getView().requestFocus();
		});
	}

	/**
	 * Connect to database.
	 *
	 * @param dbName the db name
	 * @return the connection to the database.
	 */
	private Connection connectToDatabase(String dbName) {
		Connection con = null;
		if(new File("./" + dbName).exists()) { //$NON-NLS-1$
			try {
				con = DriverManager.getConnection("jdbc:sqlite:./" + dbName); //$NON-NLS-1$
				return con;
			} catch (SQLException ex) {
				Helper.logError(ex, Messages.getString("MainWindow.DB_CONNECTION_ERROR"), true); //$NON-NLS-1$
			}
		}
		return null;
	}

	/**
	 *
	 */
	private void setupTrayIcon() {
		trayIcon = Helper.setupTrayIcon();
		if(trayIcon == null)
			return;
		PopupMenu pmnuTrayIcon = new PopupMenu();
		MenuItem mnuSettings = new MenuItem(Messages.getString("MainWindow.MNU_OPTIONS")); //$NON-NLS-1$
		mnuSettings.addActionListener(ev -> {
			this.mnuSettings.doClick();
		});
		pmnuTrayIcon.add(mnuSettings);
		MenuItem mnuExit = new MenuItem(Messages.getString("MainWindow.MNU_QUIT")); //$NON-NLS-1$
		mnuExit.addActionListener(ev -> {
			WindowEvent we = new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING);
			mainFrame.dispatchEvent(we);
		});
		pmnuTrayIcon.add(mnuExit);
		trayIcon.setPopupMenu(pmnuTrayIcon);
		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					mainFrame.setExtendedState(JFrame.NORMAL);
					mainFrame.setVisible(true);
					mainFrame.toFront();
				}
				super.mouseClicked(e);
			}
		});
	}

	/**
	 * Adds the new website to the list of websites that can integrate a chat bot IFrame
	 * to communicate to the chat bot servers.
	 *
	 * @param dlg the dialog that holds the new website data
	 */
	private void addNewWebsite(WebsiteDialog dlg) {
		String qws = "INSERT INTO websites (domain, ip, ai_group_id, key1_hash, key2_hash, salt, description, enabled, removed, sha_id) VALUES(?, ?, ?, ?, ?, ?, ?, 1, 0, ?);"; //$NON-NLS-1$
		int aig = dlg.getAIGroupId();
		String domain = dlg.getDomain().trim();
		String ip = dlg.getIP().trim();
		String key1 = dlg.getKey1().trim();
		String key2 = dlg.getKey2().trim();
		String desc = dlg.getDescription().trim();
		String salt = Helper.generateSalt(key1.length() + key2.length());
		String shaId;
		try {
			shaId = Helper.hashString(domain + ip + key1 + key2);
			key1 = Helper.hashKey(key1, salt);
			key2 = Helper.hashKey(key2, salt);
		} catch (Exception ex) {
			Helper.logError(ex, Messages.getString("MainWindow.AUTH_HASH_ERROR"), true); //$NON-NLS-1$
			return;
		}
		try(PreparedStatement ps = conChat4Us.prepareStatement(qws, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, domain);
			ps.setString(2, ip);
			ps.setInt(3, aig);
			ps.setString(4, key1);
			ps.setString(5, key2);
			ps.setString(6, salt);
			ps.setString(7, desc);
			ps.setString(8, shaId);
			int n = ps.executeUpdate();
			if(n == 1) {
				try(ResultSet rs = ps.getGeneratedKeys()) {
					if(rs.next()) {
						int id = rs.getInt(1);
						IdLabelComboModel model = (IdLabelComboModel)lstWebsites.getModel();
						model.addElement(new IdLabelComboElement(id, domain, true));
					}
				} catch (SQLException ex) {
					Helper.logError(ex, String.format(Messages.getString("MainWindow.NEW_WEBSITE_ID_ERROR"), domain), true); //$NON-NLS-1$
				}
			} else Helper.logError(String.format(Messages.getString("MainWindow.NEW_WEBSITE_NOT_ADDED"), domain)); //$NON-NLS-1$
		} catch (SQLException ex) {
			Helper.logError(ex, String.format(Messages.getString("MainWindow.ADD_NEW_WEBSITE_ERROR"), domain), true); //$NON-NLS-1$
		}
	}

	/**
	 * Edits the website.
	 *
	 * @param dlg the dialog that holds the website data to edit.
	 */
	private void editWebsite(WebsiteDialog dlg) {
		String kh1 = null, kh2 = null;
		boolean k1e = dlg.isKey1Edited() && dlg.getKey1() != null;
		boolean k2e = dlg.isKey2Edited() && dlg.getKey2() != null;
		System.out.println(k1e + " " + k2e); //$NON-NLS-1$
		String qws = "UPDATE websites SET domain = ?, ip = ?, ai_group_id = ?"; //$NON-NLS-1$
		if(k1e) qws += ", key1_hash = ?"; //$NON-NLS-1$
		if(k2e) qws += ", key2_hash = ?"; //$NON-NLS-1$
		if(k1e || k2e) qws += ", salt = ?, sha_id = ?"; //$NON-NLS-1$
		qws += ", description = ? WHERE id = ?;"; //$NON-NLS-1$
		int aig = dlg.getAIGroupId();
		String domain = dlg.getDomain().trim();
		String ip = dlg.getIP().trim();
		String key1 = k1e ? dlg.getKey1().trim() : null;
		String key2 = k2e ? dlg.getKey2().trim() : null;
		String salt = k1e || k2e ? Helper.generateSalt(key1.length() + key2.length()) : null;
		String desc = dlg.getDescription().trim();
		String shaId = null;
		if(k1e || k2e) {
			try {
				//long now = Instant.now().toEpochMilli();
				shaId = Helper.hashString(domain + ip + key1 + key2);
				kh1 = Helper.hashKey(key1, salt);
				kh2 = Helper.hashKey(key2, salt);
				//System.out.printf("PBKDF2: %d seconds\n", (Instant.now().toEpochMilli() - now) / 1000);
			} catch (Exception ex) {
				Helper.logError(ex, Messages.getString("MainWindow.AUTH_HASH_ERROR"), true); //$NON-NLS-1$
				return;
			}
		}
		try(PreparedStatement ps = conChat4Us.prepareStatement(qws)) {
			int n = 1;
			ps.setString(n++, domain);
			ps.setString(n++, ip);
			ps.setInt(n++, aig);
			if(k1e) ps.setString(n++, kh1);
			if(k2e) ps.setString(n++, kh2);
			if(k1e || k2e) {
				ps.setString(n++, salt);
				ps.setString(n++, shaId);
			}
			ps.setString(n++, desc);
			ps.setInt(n++, dlg.getDbId());
			n = ps.executeUpdate();
			if(n == 1) {
				IdLabelComboElement el;
				IdLabelComboElement old;
				IdLabelComboModel model = (IdLabelComboModel)lstWebsites.getModel();
				for(int i = 0; i < model.getSize(); i++) {
					el = (IdLabelComboElement)model.getElementAt(i);
					if(el.getId() == dlg.getDbId()) {
						old = model.getElementAt(i);
						model.replaceElement(old, new IdLabelComboElement(el.getId(), domain, true));
						break;
					}
				}
			} else Helper.logError(String.format(Messages.getString("MainWindow.WEBSITE_NOT_MODIFIED"), domain)); //$NON-NLS-1$
		} catch (SQLException ex) {
			Helper.logError(ex, String.format(Messages.getString("MainWindow.WEBSITE_EDIT_ERROR"), domain), true); //$NON-NLS-1$
		}
	}

	/**
	 * Adds the new chat server.
	 *
	 * @param dlg the dialog that holds the chat server data to add.
	 */
	private void addNewChatServer(ChatServerDialog dlg) {
		int cid = 0;
		String host = dlg.getHostIp();
		int port = dlg.getHostPort();
		int aics = dlg.getAiContextSize();
		ChatServer cs;
		ChatServerListModel model = (ChatServerListModel)lstChatBots.getModel();
		for(int i = 0; i < model.getSize(); i++) {
			cs = model.getElementAt(i);
			if(cs.getHost().equals(host) && cs.getPort() == port) {
				JOptionPane.showMessageDialog(mainFrame, Messages.getString("MainWindow.SERVER_ALREADY_EXISTS")); //$NON-NLS-1$
				return;
			}
		}
		int gid = dlg.getAIGroup();
		String ria = dlg.getRiaFile();
		String desc = dlg.getDescription();
		String qcs = "INSERT INTO chatbots (ai_group_id, server_ip, server_port, ria_file, description, enabled, removed, ai_context_size) VALUES(?, ?, ?, ?, ?, ?, ?, ?);"; //$NON-NLS-1$
		String qai = "INSERT INTO ai_servers (chatbot_id, url, enabled, removed) VALUES(?, ?, ?, ?);"; //$NON-NLS-1$
		try {
			conChat4Us.setAutoCommit(false);
			try(PreparedStatement pst = conChat4Us.prepareStatement(qcs, Statement.RETURN_GENERATED_KEYS)) {
				pst.setInt(1, gid);
				pst.setString(2, host);
				pst.setInt(3, port);
				pst.setString(4, ria);
				pst.setString(5, desc);
				pst.setInt(6, 1);
				pst.setInt(7, 0);
                pst.setInt(8, aics);
				if(pst.executeUpdate() == 1) {
					ResultSet rs = pst.getGeneratedKeys();
					if(rs.next()) {
						cid = rs.getInt(1);
						try(PreparedStatement pst1 = conChat4Us.prepareStatement(qai, Statement.RETURN_GENERATED_KEYS)) {
							for(int i = 0; i < dlg.getAiServersCount(); i++) {
								AiServer ais = dlg.getAiServer(i);
								if(ais != null) {
									pst1.setInt(1, cid);
									pst1.setString(2, ais.getUrl());
									pst1.setInt(3, ais.isEnabled() ? 1 : 0);
									pst1.setInt(4, 0);
									if(pst1.executeUpdate() == 1) {
										rs = pst1.getGeneratedKeys();
										if(rs.next()) {
											int aid = rs.getInt(1);
											rs.close();
											aiServers.add(new Object[] { aid, cid, ais.getUrl(), ais.isEnabled()?1:0, 0 });
										}
									} else Helper.logWarning(String.format(Messages.getString("MainWindow.AI_URL_NOT_ADDED"), ais.getUrl()), true); //$NON-NLS-1$
								} else Helper.logWarning(Messages.getString("MainWindow.CSD_AI_SERVER_NULL")); //$NON-NLS-1$
							}
						} catch (SQLException ex) {
							Helper.logError(ex, Messages.getString("MainWindow.URL_ADD_ERROR"), true); //$NON-NLS-1$
							conChat4Us.rollback();
							return;
						}
					}
				} else Helper.logWarning(String.format(Messages.getString("MainWindow.SERVER_NOT_ADDED"), host, port), true); //$NON-NLS-1$
				conChat4Us.commit();
			} catch (SQLException ex) {
				Helper.logError(ex, String.format(Messages.getString("MainWindow.CHATBOT_SERVER_ADD_ERROR"), host, port), true); //$NON-NLS-1$
				conChat4Us.rollback();
				return;
			} finally {
				conChat4Us.setAutoCommit(true);
			}
		} catch (SQLException ex) {
			Helper.logError(ex, String.format(Messages.getString("MainWindow.CHATBOT_SERVER_ADD_ERROR"), host, port), true); //$NON-NLS-1$
			return;
		}
		if(cid > 0) {
			try {
				cs = new ChatServer(cid, host, port, gid, aics, aiServers);
				cs.setDescription(desc);
				ChatClient cc = cs.getChatClient();
				cc.loadChatBotRIA(ria);
				cs.loadChatModelClients(aiServers);
				model.addElement(cs);
				cs.addChatServerListener(new ChatServerListener() {
					@Override
					public void onActivityStateChanged(ChatServer server, ChatSessionState state) { }
					@Override
					public void onStatsChanged(ChatServer server) { updateServerState(server); }
				});
				// Let the user starts the server whenever it's suitable.
			} catch (Exception ex) {
				Helper.logError(ex, String.format(Messages.getString("MainWindow.CHATBOT_SERVER_CREATION_ERROR"), host, port), true); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Edits the chat server.
	 *
	 * @param dlg the dialog that holds the chat server data to edit.
	 */
	private void editChatServer(ChatServerDialog dlg) {
		AppAuthDialog aadlg = new AppAuthDialog(mainFrame);
		aadlg.setLocationRelativeTo(mainFrame);
		aadlg.setVisible(true);
		if(aadlg.isCancelled()) {
			aadlg.dispose();
			return;
		}
		char[] pswd = aadlg.getPassword();
		if(!Helper.checkKeystorePassword(pswd)) {
            aadlg.dispose();
			return;
		}
		aadlg.dispose();

		String uqcs = "UPDATE chatbots SET ai_group_id=?, server_ip=?, server_port=?, ria_file=?, description=?, enabled=?, ai_context_size=? WHERE id=?;"; //$NON-NLS-1$
		String uqai = "UPDATE ai_servers SET url=?, enabled=? WHERE id=?;"; //$NON-NLS-1$
		String sqai = "UPDATE ai_servers SET removed=? WHERE id=?;"; //$NON-NLS-1$
		String iqai = "INSERT INTO ai_servers (chatbot_id, url, enabled, removed) VALUES(?, ?, ?, ?);"; //$NON-NLS-1$
		ChatServer cs = dlg.getChatServer();
		if(cs != null) {
			int cid = cs.getDbId();
			String host = dlg.getHostIp();
			int port = dlg.getHostPort();
			int gid = dlg.getAIGroup();
			int aics = dlg.getAiContextSize();
			String ria = dlg.getRiaFile();
			String desc = dlg.getDescription();
			try {
				conChat4Us.setAutoCommit(false);
				try(PreparedStatement pst = conChat4Us.prepareStatement(uqcs)) {
					pst.setInt(1, gid);
					pst.setString(2, host);
					pst.setInt(3, port);
					pst.setString(4, ria);
					pst.setString(5, desc);
					pst.setInt(6, cs.isEnabled() ? 1 : 0);
                    pst.setInt(7, aics);
					pst.setInt(8, cid);
					if(pst.executeUpdate() == 1) {
						try {
							Object[] aid;
							Object[] ais;
							ResultSet rs;
							for(int i = 0; i < dlg.getAiServersCount(); i++) {
								ais = dlg.getAiServerRow(i);
								if(ais != null) {
									int id = (Integer)ais[0];
									if(id > 0) {
										try(PreparedStatement pst1 = conChat4Us.prepareStatement(uqai)) {
											pst1.setString(1, (String)ais[1]);
											pst1.setInt(2,   (Integer)ais[2]);
											pst1.setInt(3,   (Integer)ais[0]);
											if(pst1.executeUpdate() != 1) {
												Helper.logWarning(String.format(Messages.getString("MainWindow.AI_URL_EDIT_FAILURE"), (String)ais[1]), true); //$NON-NLS-1$
											} else {
												aid = getAIServerById(id);
												if(aid != null) {
													aid[1] = cid;
													aid[2] = (String)ais[1];
													aid[3] = (Integer)ais[2];
												}
											}
										} catch (SQLException ex) {
											Helper.logError(ex, String.format(Messages.getString("MainWindow.AI_URL_EDIT_ERROR"), (String)ais[1]), true); //$NON-NLS-1$
											conChat4Us.rollback();
											return;
										}
									} else {
										try(PreparedStatement pst1 = conChat4Us.prepareStatement(iqai, Statement.RETURN_GENERATED_KEYS)) {
											pst1.setInt(1, cid);
											pst1.setString(2, (String)ais[1]);
											pst1.setInt(3, (Integer)ais[2]);
											pst1.setInt(4, 0);
											if(pst1.executeUpdate() != 1) {
												Helper.logWarning(String.format(Messages.getString("MainWindow.AI_URL_ADD_FAILURE"), (String)ais[1]), true); //$NON-NLS-1$
											} else {
												rs = pst1.getGeneratedKeys();
												if(rs.next()) {
													int nid = rs.getInt(1);
													aiServers.add(new Object[] { nid, cid, (String)ais[1], (Integer)ais[2], 0 });
												}
												rs.close();
											}
										} catch (SQLException ex) {
											Helper.logError(ex, String.format(Messages.getString("MainWindow.AI_URL_ADD_ERROR"), (String)ais[1]), true); //$NON-NLS-1$
											conChat4Us.rollback();
											return;
										}
									}
								}
							}
						} catch (SQLException ex) {
							Helper.logError(ex, Messages.getString("MainWindow.AI_URLS_EDIT_ERROR"), true); //$NON-NLS-1$
							conChat4Us.rollback();
							return;
						}
						// Mark ai servers for deletion
						List<Integer> remIds = dlg.getRemovedIds();
						if(remIds.size() > 0) {
							try(PreparedStatement pst1 = conChat4Us.prepareStatement(sqai)) {
								int id;
								Object[] ais;
								for(int i = 0; i < remIds.size(); i++) {
									id = remIds.get(i);
									if(id > 0) {
										pst1.setInt(1, 1);
										pst1.setInt(2, id);
										if(pst1.executeUpdate() != 1) {
											Helper.logWarning(String.format(Messages.getString("MainWindow.AI_URL_NOT_REMOVED"), id), true); //$NON-NLS-1$
										} else {
											ais = getAIServerById(id);
											if(ais != null)
												ais[4] = 1;
										}
									} else Helper.logWarning(String.format(Messages.getString("MainWindow.GETREMOVEDIDS_INVALID_ID"), id), true); //$NON-NLS-1$
								}
							} catch (SQLException ex) {
								Helper.logError(ex, Messages.getString("MainWindow.AI_URLS_DEL_ERROR"), true); //$NON-NLS-1$
								conChat4Us.rollback();
								return;
							}
						}
						conChat4Us.commit();
					}
				} catch(SQLException ex) {
					Helper.logError(ex, String.format(Messages.getString("MainWindow.SERVER_EDIT_ERROR"), host, port), true); //$NON-NLS-1$
					conChat4Us.rollback();
					return;
				} finally {
					conChat4Us.setAutoCommit(true);
				}
			} catch(SQLException ex) {
				Helper.logError(ex, String.format(Messages.getString("MainWindow.SERVER_EDIT_ERROR"), host, port), true); //$NON-NLS-1$
				return;
			}
			// Update/Replace edited ChatServer
			try {
				ChatServer old = cs;
				cs = new ChatServer(cid, host, port, gid, aics, aiServers);
				cs.setDescription(desc);
				ChatClient cc = cs.getChatClient();
				cc.loadChatBotRIA(ria);
				old.setEnabled(false);
				old.stopServer();
				cs.loadChatModelClients(aiServers);
				cs.startSecureServer(pswd);
				ChatServerListModel model = (ChatServerListModel)lstChatBots.getModel();
				model.replaceElement(old, cs);
				cs.addChatServerListener(new ChatServerListener() {
					@Override
					public void onActivityStateChanged(ChatServer server, ChatSessionState state) { }
					@Override
					public void onStatsChanged(ChatServer server) { updateServerState(server); }
				});
				if(!cs.isStarted())
					cs.setEnabled(false);
			} catch (Exception ex) {
				Helper.logError(ex, String.format(Messages.getString("MainWindow.SERVER_RECREATION_ERROR"), host, port), true); //$NON-NLS-1$
			}
			Arrays.fill(pswd, (char)0);
		} else {
			Helper.logError(Messages.getString("MainWindow.NULL_SERVER_EDIT_ERROR")); //$NON-NLS-1$
			return;
		}
	}

	/**
	 * Update server state.
	 *
	 * @param server the server
	 */
	private void updateServerState(ChatServer server) {
		int nChats = 0;
		ChatServer s;
		Set<Integer> cids = new HashSet<Integer>();
		Set<Integer> aids = new HashSet<Integer>();
		ListModel<ChatServer> model = this.lstChatBots.getModel();
		for(int i = 0; i < model.getSize(); i++) {
			s = model.getElementAt(i);
			cids.addAll(s.getConnectedClients());
			aids.addAll(s.getConnectedAgents());
			nChats += s.getActiveChatsCount();
		}
		lblChatsCount.setText(nChats + ""); //$NON-NLS-1$
		lblWebsitesCount.setText(cids.size() + ""); //$NON-NLS-1$
		lblAgentsCount.setText(aids.size() + ""); //$NON-NLS-1$
		lblChatBotsCount.setText(model.getSize() + ""); //$NON-NLS-1$
		if(trayIcon != null)
			trayIcon.setToolTip(String.format(Messages.getString("MainWindow.TI_TOOLTIP"), nChats, model.getSize(), aids.size(), cids.size())); //$NON-NLS-1$
	}

	/**
	 * Adds the new agent to the list of operators that chats can be redirected to for human support.
	 *
	 * @param dlg the dialog that holds data for the new agent
	 */
	private void addNewAgent(AgentDialog dlg) {
		String qag = "INSERT INTO agents (name, poste_id, host, port, description, last_connected, enabled, removed, ai_group_id) VALUES(?, ?, ?, ?, ?, 0, 0, 0, ?);"; //$NON-NLS-1$
		String name = dlg.getName();
		int pid = dlg.getPoste().getId();
		int aid = dlg.getAiGroup().getId();
		String host = dlg.getHost();
		int port = dlg.getPort();
		String desc = dlg.getDescription();
		try(PreparedStatement ps = conChat4Us.prepareStatement(qag, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, name);
			ps.setInt(2, pid);
			ps.setString(3, host);
			ps.setInt(4, port);
			ps.setString(5, desc);
			ps.setInt(6, aid);
			int n = ps.executeUpdate();
			if(n == 1) {
				try(ResultSet rs = ps.getGeneratedKeys()) {
					if(rs.next()) {
						int id = rs.getInt(1);
						IdLabelComboModel model = (IdLabelComboModel)lstAgents.getModel();
						model.addElement(new IdLabelComboElement(id, name, true));
					}
				} catch(SQLException ex) {
					Helper.logError(Messages.getString("MainWindow.NEW_AGENT_NOT_ADDED"), true); //$NON-NLS-1$
				}
			} else Helper.logError(Messages.getString("MainWindow.LOG_NEW_AGENT_NOT_ADDED"), true); //$NON-NLS-1$
		} catch (SQLException ex) {
			Helper.logError(ex, Messages.getString("MainWindow.NEW_AGENT_ADD_ERROR"), true); //$NON-NLS-1$
		}
	}

	/**
	 * Edits the agent.
	 *
	 * @param dlg the dialog that holds data for the edited agent
	 */
	private void editAgent(AgentDialog dlg) {
		String qag = "UPDATE agents SET name = ?, poste_id = ?, host = ?, port = ?, description = ?, ai_group_id = ? WHERE id = ?;"; //$NON-NLS-1$
		int id = dlg.getDbId();
		String name = dlg.getName();
		int pid = dlg.getPoste().getId();
		int aid = dlg.getAiGroup().getId();
		String host = dlg.getHost();
		int port = dlg.getPort();
		String desc = dlg.getDescription();
		try(PreparedStatement ps = conChat4Us.prepareStatement(qag)) {
			ps.setString(1, name);
			ps.setInt(2, pid);
			ps.setString(3, host);
			ps.setInt(4, port);
			ps.setString(5, desc);
			ps.setInt(6, aid);
			ps.setInt(7, id);
			int n = ps.executeUpdate();
			if(n == 1) {
				IdLabelComboElement agent;
				IdLabelComboModel model = (IdLabelComboModel)lstAgents.getModel();
				for(int i = 0; i < model.getSize(); i++) {
					agent = model.getElementAt(i);
					if(agent.getId() == id) {
						model.replaceElement(agent, new IdLabelComboElement(id, name, true));
						break;
					}
				}
			} else Helper.logError(Messages.getString("MainWindow.AGENT_NOT_EDITED"), true); //$NON-NLS-1$
		} catch (SQLException ex) {
			Helper.logError(ex, Messages.getString("MainWindow.AGENT_EDIT_ERROR"), true); //$NON-NLS-1$
		}
	}

	/**
	 * Handles the mouse pressed event on the list of agents.
	 *
	 * @param e the mouse event
	 */
	private void lstAgentsMousePressed(MouseEvent e) {
		if(e.isPopupTrigger()  || e.getButton() == MouseEvent.BUTTON3) {
			JList<IdLabelComboElement> lst = (JList<IdLabelComboElement>)e.getSource();
			int index = lst.locationToIndex(e.getPoint());
			if(index != -1) {
				Rectangle r = lst.getCellBounds(index, index);
				if(r != null)
					index = r.contains(e.getPoint()) ? index : -1;
			}
			if(index != -1) {
				lst.setSelectedIndex(index);
			} else if(lstNoSelAgentPopup != null)
				lstNoSelAgentPopup.show(e.getComponent(), e.getX(), e.getY());

			if(lstSelAgentPopup == null) {
				lstSelAgentPopup = new JPopupMenu();
				JCheckBoxMenuItem mnuEnabled = new JCheckBoxMenuItem(Messages.getString("MainWindow.CBMI_ACTIVATE")); //$NON-NLS-1$
				mnuEnabled.addActionListener(ev -> {
					IdLabelComboElement agent = lst.getSelectedValue();
					if(agent != null)
						menuAgentDeActivate(agent);
				});
				lstSelAgentPopup.add(mnuEnabled);
				lstSelAgentPopup.addSeparator();
				JMenuItem mnuEdit = new JMenuItem(Messages.getString("MainWindow.MNU_EDIT")); //$NON-NLS-1$
				mnuEdit.addActionListener(ev -> {
					IdLabelComboElement agent = lst.getSelectedValue();
					if(agent != null)
						menuAgentEdit(agent);
				});
				lstSelAgentPopup.add(mnuEdit);
				JMenuItem mnuDelete = new JMenuItem(Messages.getString("MainWindow.MNU_DELETE")); //$NON-NLS-1$
				mnuDelete.addActionListener(ev -> {
					IdLabelComboElement agent = lst.getSelectedValue();
					if(agent != null)
						menuAgentRemove(agent);
				});
				lstSelAgentPopup.add(mnuDelete);
				lstSelAgentPopup.addPopupMenuListener(new PopupMenuListener() {
					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
						IdLabelComboElement agent = lst.getSelectedValue();
						if(agent != null) {
							mnuEnabled.setText(agent.isEnabled() ? Messages.getString("MainWindow.MNU_DEACTIVATE") : Messages.getString("MainWindow.MNU_ACTIVATE")); //$NON-NLS-1$ //$NON-NLS-2$
							mnuEnabled.setSelected(agent.isEnabled());
						}
					}
					@Override
					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }
					@Override
					public void popupMenuCanceled(PopupMenuEvent e) { }
				});
			}
			if(index != -1)
				lstSelAgentPopup.show(e.getComponent(), e.getX(), e.getY());

			if(lstNoSelAgentPopup == null) {
				lstNoSelAgentPopup = new JPopupMenu();
				JMenuItem mnuAdd = new JMenuItem(Messages.getString("MainWindow.MNU_ADD_DOTS")); //$NON-NLS-1$
				mnuAdd.addActionListener(ev -> {
					menuAgentNew();
				});
				lstNoSelAgentPopup.add(mnuAdd);
			}
		}
	}

	/**
	 * Menu agent new click even handler.
	 */
	private void menuAgentNew() {
		AgentDialog dlg = new AgentDialog(mainFrame);
		dlg.setLocationRelativeTo(MainWindow.getFrame());
		dlg.setVisible(true);
		if(!dlg.isCancelled())
			addNewAgent(dlg);
		dlg.dispose();
	}

	/**
	 * Menu agent remove event handler
	 *
	 * @param agent the agent record
	 */
	private void menuAgentRemove(IdLabelComboElement agent) {
		int ret = Helper.showConfirmDialog(MainWindow.getFrame(), String.format(Messages.getString("MainWindow.MB_AGENT_DELETE_MSG"), agent.getLabel()), Messages.getString("MainWindow.MB_TITLE_CONFIRMATION"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
		if(ret == JOptionPane.YES_OPTION) {
			int n = Helper.dbUpdate(conChat4Us, "UPDATE agents SET removed = 1 WHERE id = " + agent.getId() + ";"); //$NON-NLS-1$ //$NON-NLS-2$
			if(n == 1) {
				IdLabelComboModel model = (IdLabelComboModel)lstAgents.getModel();
				model.removeElement(agent);
			} else Helper.logWarning(String.format(Messages.getString("MainWindow.AGENT_NOT_REMOVED"), agent.getLabel()), true); //$NON-NLS-1$
		}
	}

	/**
	 * Menu agent edit click event handler.
	 *
	 * @param agent the agent record
	 */
	private void menuAgentEdit(IdLabelComboElement agent) {
		AgentDialog dlg = new AgentDialog(mainFrame, agent.getId(), true);
		dlg.setLocationRelativeTo(mainFrame);
		dlg.setVisible(true);
		if(!dlg.isCancelled())
			editAgent(dlg);
		dlg.dispose();
	}

	/**
	 * Menu agent de/activate click event handler.
	 *
	 * @param agent the agent record
	 */
	private void menuAgentDeActivate(IdLabelComboElement agent) {
		int ret = Helper.showConfirmDialog(MainWindow.getFrame(), String.format(Messages.getString("MainWindow.MB_AGENT_REMOVE_MSG"), (agent.isEnabled() ? Messages.getString("MainWindow.BOOLEAN_DEACTIVATE") : Messages.getString("MainWindow.BOOLEAN_ACTIVATE")), agent.getLabel()), Messages.getString("MainWindow.MB_TITLE_CONFIRMATION"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if(ret == JOptionPane.YES_OPTION) {
			int n = Helper.dbUpdate(conChat4Us, "UPDATE agents SET enabled = " + (agent.isEnabled() ? 0 : 1) + " WHERE id = " + agent.getId() + ";"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if(n == 1) {
				agent.setEnabled(!agent.isEnabled());
				lstAgents.repaint();
				Helper.logInfo(String.format(Messages.getString("MainWindow.LOG_AGENT_STATE"), agent.getLabel(), (agent.isEnabled() ? Messages.getString("MainWindow.BOOLEAN_DEACTIVATED") : Messages.getString("MainWindow.BOOLEAN_ACTIVATED"))), false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else Helper.logWarning(String.format(Messages.getString("MainWindow.LOG_AGENT_STATE_ERROR"), agent.getLabel()), true); //$NON-NLS-1$
		}
	}

	/**
	 * Websites list changed event handler
	 */
	private void websitesListChanged() {
		ChatServerListModel model = (ChatServerListModel)lstChatBots.getModel();
		for(int i = 0; i < model.getSize(); i++)
			model.getElementAt(i).websitesTableChanged();
	}

	/**
	 * Lst websites mouse pressed event handler
	 *
	 * @param e the mouse event
	 */
	private void lstWebsitesMousePressed(MouseEvent e) {
		if(e.isPopupTrigger()  || e.getButton() == MouseEvent.BUTTON3) {
			JList<IdLabelComboElement> lst = (JList<IdLabelComboElement>)e.getSource();
			int index = lst.locationToIndex(e.getPoint());
			if(index != -1) {
				Rectangle r = lst.getCellBounds(index, index);
				if(r != null)
					index = r.contains(e.getPoint()) ? index : -1;
			}
			if(index != -1) {
				lst.setSelectedIndex(index);
			} else if(lstNoSelWebsitePopup != null)
				lstNoSelWebsitePopup.show(e.getComponent(), e.getX(), e.getY());

			if(lstSelWebsitePopup == null) {
				lstSelWebsitePopup = new JPopupMenu();
				JCheckBoxMenuItem mnuEnabled = new JCheckBoxMenuItem(Messages.getString("MainWindow.CBMI_ACTIVATE")); //$NON-NLS-1$
				mnuEnabled.addActionListener(ev -> {
					IdLabelComboElement webSite = lst.getSelectedValue();
					if(webSite != null)
						menuWebsiteDeactivate(webSite);
				});
				lstSelWebsitePopup.add(mnuEnabled);
				lstSelWebsitePopup.addSeparator();
				JMenuItem mnuEdit = new JMenuItem(Messages.getString("MainWindow.MNU_EDIT")); //$NON-NLS-1$
				mnuEdit.addActionListener(ev -> {
					IdLabelComboElement webSite = lst.getSelectedValue();
					if(webSite != null)
						menuWebsiteEdit(webSite);
				});
				lstSelWebsitePopup.add(mnuEdit);
				JMenuItem mnuRemove = new JMenuItem(Messages.getString("MainWindow.MNU_DELETE")); //$NON-NLS-1$
				mnuRemove.addActionListener(ev -> {
					IdLabelComboElement webSite = lst.getSelectedValue();
					if(webSite != null)
						menuWebsiteRemove(webSite);
				});
				lstSelWebsitePopup.add(mnuRemove);
				lstSelWebsitePopup.addPopupMenuListener(new PopupMenuListener() {
					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
						IdLabelComboElement webSite = lst.getSelectedValue();
						if(webSite != null) {
							mnuEnabled.setText(webSite.isEnabled() ? Messages.getString("MainWindow.MNU_DEACTIVATE") : Messages.getString("MainWindow.MNU_ACTIVATE")); //$NON-NLS-1$ //$NON-NLS-2$
							mnuEnabled.setSelected(webSite.isEnabled());
						}
					}
					@Override
					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }
					@Override
					public void popupMenuCanceled(PopupMenuEvent e) { }

				});
			}

			if(index != -1)
				lstSelWebsitePopup.show(e.getComponent(), e.getX(), e.getY());

			if(lstNoSelWebsitePopup == null) {
				lstNoSelWebsitePopup = new JPopupMenu();
				JMenuItem mnuAdd = new JMenuItem(Messages.getString("MainWindow.MNU_ADD_DOTS")); //$NON-NLS-1$
				mnuAdd.addActionListener(ev -> {
					menuWebsiteNew();
				});
				lstNoSelWebsitePopup.add(mnuAdd);
			}
		}
	}

	/**
	 * Menu website new  click event handler.
	 */
	private void menuWebsiteNew() {
		WebsiteDialog dlg = new WebsiteDialog(mainFrame, 0, false);
		dlg.setLocationRelativeTo(MainWindow.getFrame());
		dlg.setVisible(true);
		if(!dlg.isCanceled())
			addNewWebsite(dlg);
		dlg.dispose();
		websitesListChanged();
	}

	/**
	 * Menu website remove click event handler.
	 *
	 * @param webs the website record
	 */
	private void menuWebsiteRemove(IdLabelComboElement webs) {
		int ret = Helper.showConfirmDialog(MainWindow.getFrame(), String.format(Messages.getString("MainWindow.MB_WEBSITE_REMOVE_MSG"), webs.getLabel()), Messages.getString("MainWindow.MB_TITLE_CONFIRMATION"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
		if(ret == JOptionPane.YES_OPTION) {
			int n = Helper.dbUpdate(conChat4Us, "UPDATE websites SET removed = 1 WHERE id = " + webs.getId() + ";"); //$NON-NLS-1$ //$NON-NLS-2$
			if(n == 1) {
				IdLabelComboModel model = (IdLabelComboModel)lstWebsites.getModel();
				model.removeElement(webs);
				websitesListChanged();
			} else Helper.logWarning(String.format(Messages.getString("MainWindow.LOG_WEBSITE_DELETE_ERROR"),webs.getLabel()), true); //$NON-NLS-1$
		}
	}

	/**
	 * Menu website edit click event handler.
	 *
	 * @param webs the website record
	 */
	private void menuWebsiteEdit(IdLabelComboElement webs) {
		WebsiteDialog dlg = new WebsiteDialog(mainFrame, webs.getId(), true);
		dlg.setLocationRelativeTo(MainWindow.getFrame());
		dlg.setVisible(true);
		if(!dlg.isCanceled() && dlg.isEdited())
			editWebsite(dlg);
		dlg.dispose();
		websitesListChanged();
	}

	/**
	 * Menu website deactivate click event handler.
	 *
	 * @param webs the website record.
	 */
	private void menuWebsiteDeactivate(IdLabelComboElement webs) {
		int ret = Helper.showConfirmDialog(MainWindow.getFrame(), String.format(Messages.getString("MainWindow.MB_DE_ACTIVATE_WEBSITE_MSG"), (webs.isEnabled() ? Messages.getString("MainWindow.BOOLEAN_DEACTIVATE") : Messages.getString("MainWindow.BOOLEAN_ACTIVATE")), webs.getLabel()), Messages.getString("MainWindow.MB_TITLE_CONFIRMATION"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if(ret == JOptionPane.YES_OPTION) {
			int n = Helper.dbUpdate(conChat4Us, "UPDATE websites SET enabled = " + (webs.isEnabled() ? 0 : 1) + " WHERE id = " + webs.getId() + ";"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if(n == 1) {
				websitesListChanged();
				webs.setEnabled(!webs.isEnabled());
				lstWebsites.repaint();
				Helper.logInfo(String.format(Messages.getString("MainWindow.WEBSITE_STATE_CHANGED"), webs.getLabel(), (!webs.isEnabled() ? Messages.getString("MainWindow.BOOLEAN_DEACTIVATED") : Messages.getString("MainWindow.BOOLEAN_ACTIVATED"))), false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else Helper.logWarning(String.format(Messages.getString("MainWindow.WEBSITE_NOT_REMOVED"), webs.getLabel()), true); //$NON-NLS-1$
		}
	}

	/**
	 * Lst chat bots mouse pressed event handler.
	 *
	 * @param e the e
	 */
	private void lstChatBotsMousePressed(MouseEvent e) {
		if(e.isPopupTrigger()  || e.getButton() == MouseEvent.BUTTON3) {
			final JList<ChatServer> lst = (JList<ChatServer>)e.getSource();
			int index = lst.locationToIndex(e.getPoint());
			if(index != -1) {
				Rectangle r = lst.getCellBounds(index, index);
				if(r != null)
					index = r.contains(e.getPoint()) ? index : -1;
			}
			if(index != -1) {
				lst.setSelectedIndex(index);
			} else if(lstNoSelBotPopup != null)
				lstNoSelBotPopup.show(e.getComponent(), e.getX(), e.getY());

			if(lstSelBotPopup == null) {
				lstSelBotPopup = new JPopupMenu();
				JMenuItem mnuRestart = new JMenuItem(Messages.getString("MainWindow.MNU_RESTART")); //$NON-NLS-1$
                mnuRestart.addActionListener(ev -> {
                	ChatServer chatServer = lst.getSelectedValue();
                	if(chatServer != null)
                		menuServerRestart(chatServer);
				});
                lstSelBotPopup.add(mnuRestart);
				JCheckBoxMenuItem mnuDeActivate = new JCheckBoxMenuItem(Messages.getString("MainWindow.CBMI_DE_ACTIVATE")); //$NON-NLS-1$
				mnuDeActivate.addActionListener(ev -> {
                	ChatServer chatServer = lst.getSelectedValue();
                	if(chatServer != null)
                		menuServerDeActivate(chatServer);
				});
				lstSelBotPopup.add(mnuDeActivate);
				lstSelBotPopup.addSeparator();
				JMenuItem mnuEdit = new JMenuItem(Messages.getString("MainWindow.MNU_EDIT")); //$NON-NLS-1$
				mnuEdit.addActionListener(ev -> {
					ChatServer chatServer = lst.getSelectedValue();
                	if(chatServer != null)
                		menuServerEdit(chatServer);
				});
				lstSelBotPopup.add(mnuEdit);
				JMenuItem mnuRemove = new JMenuItem(Messages.getString("MainWindow.MNU_DELETE")); //$NON-NLS-1$
				mnuRemove.addActionListener(ev -> {
					ChatServer chatServer = lst.getSelectedValue();
                	if(chatServer != null)
                		menuServerRemove(chatServer);
				});
				lstSelBotPopup.add(mnuRemove);
				JMenu mnuAssistants = new JMenu(Messages.getString("MainWindow.MNU_ASSISTANTS")); //$NON-NLS-1$
				lstSelBotPopup.add(mnuAssistants);
				lstSelBotPopup.addSeparator();
				JMenuItem mnuOpenRia = new JMenuItem(Messages.getString("MainWindow.MNU_OPEN_IN_EDITOR")); //$NON-NLS-1$
				mnuOpenRia.addActionListener(ev -> {
					ChatServer chatServer = lst.getSelectedValue();
                	if(chatServer != null)
                		openRiaDocument(chatServer.getChatClient().getChatBotClient().getRiaFileName());
				});
				lstSelBotPopup.add(mnuOpenRia);
				JMenuItem mnuShowInternalClient = new JMenuItem(Messages.getString("MainWindow.MNU_INTERNAL_CHAT_CLIENT")); //$NON-NLS-1$
                mnuShowInternalClient.addActionListener(ev -> {
                	ChatServer chatServer = lst.getSelectedValue();
                	if(chatServer != null)
                		showInternalChatClient(chatServer);
                });
                lstSelBotPopup.add(mnuShowInternalClient);
				lstSelBotPopup.addPopupMenuListener(new PopupMenuListener() {
					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
						ChatServer chatServer = lst.getSelectedValue();
	                	if(chatServer != null) {
	                		mnuRestart.setEnabled(chatServer.isEnabled());
							mnuDeActivate.setText(chatServer.isEnabled() ? Messages.getString("MainWindow.MNU_DEACTIVATE") : Messages.getString("MainWindow.MNU_ACTIVATE")); //$NON-NLS-1$ //$NON-NLS-2$
							mnuDeActivate.setSelected(chatServer.isEnabled());
							mnuShowInternalClient.setEnabled(chatServer.isEnabled());
	                	}
					}
					@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }
					@Override public void popupMenuCanceled(PopupMenuEvent e) { }
				});
			}
			ChatServer chatServer = lst.getSelectedValue();
        	if(chatServer != null) {
        		JMenuItem mnuAssistants = (JMenuItem)lstSelBotPopup.getComponent(5);
        		menuServerAddAssistants(chatServer, mnuAssistants);
        	}
        	if(index != -1)
        		lstSelBotPopup.show(e.getComponent(), e.getX(), e.getY());

			if(lstNoSelBotPopup == null) {
				lstNoSelBotPopup = new JPopupMenu();
				JMenuItem mnuAdd = new JMenuItem(Messages.getString("MainWindow.MNU_ADD_DOTS")); //$NON-NLS-1$
				mnuAdd.addActionListener(ev -> {
					menuServerNew();
				});
				lstNoSelBotPopup.add(mnuAdd);
			}
		}
	}

	/**
	 * Show internal chat client for the given chat server.
	 * @param cs Chat server object
	 */
	public void showInternalChatClient(ChatServer cs) {
		String[] keys = cs.internalAuthRequest();
		if(keys != null) {
		    InternalClientFrame client = new InternalClientFrame(cs.getName(), "https://localhost:" + cs.getPort(), keys[0], keys[1]); //$NON-NLS-1$
		    client.setLocationRelativeTo(mainFrame);
		    client.setVisible(true);
		}
	}

	/**
	 * Menu server new click event handler.
	 */
	private void menuServerNew() {
		ChatServerDialog csd = new ChatServerDialog(mainFrame);
		csd.setLocationRelativeTo(mainFrame);
		csd.setVisible(true);
		if(!csd.isCancelled()) {
			if(csd.isEdited()) {
				editChatServer(csd);
			} else addNewChatServer(csd);
		}
		csd.dispose();
	}

	/**
	 * Menu server add assistants click event handler.
	 *
	 * @param chatServer the chat server
	 * @param mnuAssistants the menu assistants component
	 */
	private void menuServerAddAssistants(ChatServer chatServer, JMenuItem mnuAssistants) {
		mnuAssistants.removeAll();
		ChatClient cc = chatServer.getChatClient();
		for(int i = 0; i < cc.getChatModelClientsCount(); i++) {
			IChatModelClient cmc = cc.getChatModelClient(i);
			JCheckBoxMenuItem mnuAssistant = new JCheckBoxMenuItem(cmc.getAiServerDomain());
			mnuAssistant.setSelected(cmc.isEnabled());
			mnuAssistant.addActionListener(ev -> {
				int dbId = cmc.getDbId();
				boolean b = !cmc.isEnabled();
				if(1 == Helper.dbUpdate(conChat4Us, "UPDATE ai_servers SET enabled=" + (b?1:0) + " WHERE id=" + dbId + ";")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					cmc.setEnabled(b);
					mnuAssistant.setSelected(b);
					Object[] aid = getAIServerById(dbId);
					if(aid != null)
						aid[3] = b ? 1 : 0;
				} else Helper.logError(Messages.getString("MainWindow.AIMODEL_STATE_CHANGE_ERROR"), true); //$NON-NLS-1$
			});
			mnuAssistants.add(mnuAssistant);
		}
	}

	/**
	 * Menu server remove click event handler.
	 *
	 * @param chatServer the chat server
	 */
	private void menuServerRemove(ChatServer chatServer) {
		int ret = Helper.showConfirmDialog(mainFrame, Messages.getString("MainWindow.MB_SERVER_REMOVE_MSG"), Messages.getString("MainWindow.MB_TITLE_CONFIRMATION"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
		if(ret == JOptionPane.YES_OPTION) {
			if(1 == Helper.dbUpdate(conChat4Us, "UPDATE chatbots SET removed=1 WHERE id=" + chatServer.getDbId() + ";")) { //$NON-NLS-1$ //$NON-NLS-2$
				ChatServerListModel model = (ChatServerListModel)lstChatBots.getModel();
				model.removeElement(chatServer);
				chatServer.stopServer();
			} else Helper.logError(Messages.getString("MainWindow.LOG_SERVER_REMOVE_ERROR"), true); //$NON-NLS-1$
		}
	}

	/**
	 * Menu server edit click event handler.
	 *
	 * @param chatServer the chat server
	 */
	private void menuServerEdit(ChatServer chatServer) {
		ChatServerDialog csd = new ChatServerDialog(mainFrame, chatServer);
		csd.setLocationRelativeTo(mainFrame);
		csd.setVisible(true);
		if(!csd.isCancelled())
			editChatServer(csd);
		csd.dispose();
	}

	/**
	 * Menu server de/activate click event handler.
	 *
	 * @param chatServer the chat server
	 */
	private void menuServerDeActivate(ChatServer chatServer) {
		int ret = Helper.showConfirmDialog(mainFrame, String.format(Messages.getString("MainWindow.MB_SERVER_DE_ACTIVATE_MSG"), (chatServer.isEnabled() ? Messages.getString("MainWindow.BOOLEAN_DEACTIVATE") : Messages.getString("MainWindow.BOOLEAN_ACTIVATE")), chatServer.getName()), Messages.getString("MainWindow.MB_TITLE_CONFIRMATION"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if(ret == JOptionPane.YES_OPTION) {
			if(!chatServer.isStarted() && !chatServer.isEnabled()) {
				AppAuthDialog dlg = new AppAuthDialog(MainWindow.getFrame());
				dlg.setLocationRelativeTo(MainWindow.getFrame());
				dlg.setVisible(true);
				if(dlg.isCancelled()) return;
				char[] pswd = dlg.getPassword();
				dlg.dispose();
				if(Helper.checkKeystorePassword(pswd)) {
					chatServer.startSecureServer(pswd);
					Arrays.fill(pswd, (char)0);
				} else return;
			}
			boolean b = !chatServer.isEnabled();
			if(1 == Helper.dbUpdate(conChat4Us, "UPDATE chatbots SET enabled=" + (b ? 1 : 0) + " WHERE id=" + chatServer.getDbId() + ";")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				chatServer.setEnabled(b);
				lstChatBots.repaint();
				Helper.logInfo(String.format(Messages.getString("MainWindow.LOG_SERVER_DE_ACTIVATE"), chatServer.getName(), (!b ? Messages.getString("MainWindow.BOOLEAN_DEACTIVATED") : Messages.getString("MainWindow.BOOLEAN_ACTIVATED"))), false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else Helper.logError(Messages.getString("MainWindow.SERVER_STATE_CHANGE_FAILURE"), true); //$NON-NLS-1$
		}
	}

	/**
	 * Menu server restart click event handler.
	 * @param chatServer the chat server to restart
	 */
	private void menuServerRestart(ChatServer chatServer) {
        int ret = Helper.showConfirmDialog(mainFrame, Messages.getString("MainWindow.MB_SERVER_RESTART_MSG"), Messages.getString("MainWindow.MB_TITLE_CONFIRMATION"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
        if(ret == JOptionPane.YES_OPTION) {
			if(!chatServer.isStarted() && !chatServer.isEnabled()) {
				AppAuthDialog dlg = new AppAuthDialog(MainWindow.getFrame());
				dlg.setLocationRelativeTo(MainWindow.getFrame());
				dlg.setVisible(true);
				if(dlg.isCancelled()) return;
				char[] pswd = dlg.getPassword();
				dlg.dispose();
				if(Helper.checkKeystorePassword(pswd)) {
					chatServer.startSecureServer(pswd);
					Arrays.fill(pswd, (char)0);
				} else return;
			}
			if(chatServer.isEnabled()) {
    			chatServer.setEnabled(false);
				lstChatBots.repaint();
				Helper.logInfo(String.format(Messages.getString("MainWindow.LOG_SERVER_DE_ACTIVATE"), chatServer.getName(), (!chatServer.isEnabled() ? Messages.getString("MainWindow.BOOLEAN_DEACTIVATED") : Messages.getString("MainWindow.BOOLEAN_ACTIVATED"))), false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    			chatServer.setEnabled(true);
				lstChatBots.repaint();
				Helper.logInfo(String.format(Messages.getString("MainWindow.LOG_SERVER_DE_ACTIVATE"), chatServer.getName(), (!chatServer.isEnabled() ? Messages.getString("MainWindow.BOOLEAN_DEACTIVATED") : Messages.getString("MainWindow.BOOLEAN_ACTIVATED"))), false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
        }
	}

	/**
	 * Load chat bots from database then run them.
	 */
	private void loadChatBots() {
		SwingUtilities.invokeLater(() -> {
			AppAuthDialog dlg = new AppAuthDialog(mainFrame);
			dlg.setLocationRelativeTo(mainFrame);
			dlg.setVisible(true);
			if(dlg.isCancelled()) {
				dlg.dispose();
				getFrame().dispose();
				System.exit(1);
				return;
			}
			char[] pswd = dlg.getPassword();
			dlg.dispose();
			try {
				selfSigned = Helper.isSelfSigned(pswd);
			} catch(Exception ex) {
				Helper.logError(Messages.getString("MainWindow.LOG_FATAL_AUTH_ERROR"), true); //$NON-NLS-1$
				getFrame().dispose();
				System.exit(1);
				return;
			}

			ChatServerListModel model = (ChatServerListModel)lstChatBots.getModel();
			try(Statement st = conChat4Us.createStatement();
					ResultSet rs = st.executeQuery("SELECT * FROM chatbots WHERE removed=0;")) { //$NON-NLS-1$
				ChatServer cs;
				while(rs.next()) {
					cs = ChatServer.fromDatabase(rs, aiServers);
					model.addElement(cs);
					cs.addChatServerListener(new ChatServerListener() {
						@Override
						public void onActivityStateChanged(ChatServer server, ChatSessionState state) { }
						@Override
						public void onStatsChanged(ChatServer server) { updateServerState(server); }

					});
					Helper.logInfo(String.format(Messages.getString("MainWindow.CHAT_SERVER_STARTING"), cs.toString()), false); //$NON-NLS-1$
					cs.startSecureServer(pswd);
					if(!cs.isStarted())
						cs.setEnabled(false);
				}
				return;
			} catch (Exception ex) {
				Helper.logError(ex, Messages.getString("MainWindow.CHATBOTS_LOADING_ERROR"), true); //$NON-NLS-1$
			}
			Arrays.fill(pswd, '\0');
			getFrame().dispose();
		});
	}

	/**
	 * Load websites from database.
	 */
	private void loadWebsites() {
		IdLabelComboModel model = (IdLabelComboModel)lstWebsites.getModel();
		try(Statement st = conChat4Us.createStatement();
			ResultSet rs = st.executeQuery("SELECT id, domain FROM websites WHERE removed=0;")) { //$NON-NLS-1$
			while(rs.next())
				model.addElement(new IdLabelComboElement(rs.getInt("id"), rs.getString("domain"), true)); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (SQLException ex) {
			Helper.logError(ex, Messages.getString("MainWindow.WEBSITE_LOADING_ERROR"), true); //$NON-NLS-1$
		}
	}

	/**
	 * Load agents from database.
	 */
	private void loadAgents() {
		IdLabelComboModel model = (IdLabelComboModel)lstAgents.getModel();
		try(Statement st = conChat4Us.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT id, name FROM agents WHERE removed=0;"); //$NON-NLS-1$
			while(rs.next())
				model.addElement(new IdLabelComboElement(rs.getInt("id"), rs.getString("name"), true)); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (SQLException ex) {
			Helper.logError(ex, Messages.getString("MainWindow.AGENTS_LOADING_ERROR"), true); //$NON-NLS-1$
		}
	}

	/**
	 * Load data.
	 *
	 * @param e the window event
	 */
	private void loadData(WindowEvent e) {
		// TODO : Make it unblocking...
		Helper.addPopupMenuToTextBoxes(mainFrame);
		aiGroups = Helper.loadTable(conChat4Us, "ai_groups"); //$NON-NLS-1$
		aiServers = Helper.loadTable(conChat4Us, "ai_servers"); //$NON-NLS-1$
		agPostes = Helper.loadTable(conChat4Us, "agents_postes"); //$NON-NLS-1$
		loadChatBots();
		loadAgents();
		loadWebsites();
	}

	/**
	 * Checks if is RIA file already opened.
	 *
	 * @param riaFile the RIA file
	 * @return index of the tab, -1 if not opened.
	 */
	public int isRiaFileAlreadyOpened(String riaFile) {
		JScrollPane sp;
        RiaEditorPanel editor;
		for(int i = 0; i < tabbedPaneRight.getTabCount(); i++) {
			sp = (JScrollPane)tabbedPaneRight.getComponentAt(i);
			editor = (RiaEditorPanel)sp.getViewport().getView();
			if(riaFile.equals(editor.getRiaFile()))
				return i;
		}
		return -1;
	}

	/**
	 * Append recent RIA files into the ChatBot menu.
	 */
	private void appendRecentRiaFiles() {
		File baseDir = new File("./"); //$NON-NLS-1$
		mnuOpenedList.removeAll();
		int nMax = settings.getMaxRecentFiles();
		File f;
		String s;
		List<String> openedRecently = settings.getOpenedRecently();
		for(int i = 0; i < Math.min(nMax, openedRecently.size()); i++) {
			final int index = i;
			final String riaFile = openedRecently.get(index);
			if(riaFile.isEmpty()) {
				continue;
			} else if(Files.notExists(Path.of(riaFile))) {
				Helper.logWarning(String.format(Messages.getString("MainWindow.INVALID_RIA_FILE"), riaFile), false); //$NON-NLS-1$
				continue;
			}
			f = new File(riaFile);
			s = Helper.getRelativePath(new File(riaFile), baseDir);
			JMenuItem mnu = new JMenuItem(s.contains("..") ? f.getAbsolutePath() : s); //$NON-NLS-1$
			mnu.addActionListener(ev -> {
				int ri = isRiaFileAlreadyOpened(riaFile);
				if(ri >= 0) {
					tabbedPaneRight.setSelectedIndex(ri);
					return;
				}
				mnuNewChatBot.doClick();
				int tabIndex = tabbedPaneRight.getTabCount() - 1;
				JScrollPane sp = (JScrollPane)tabbedPaneRight.getComponentAt(tabIndex);
				RiaEditorPanel editor = (RiaEditorPanel)sp.getViewport().getView();
				editor.openRiaFile(riaFile);
				settings.removeOpenedRecently(riaFile);
				settings.addOpenedRecently(riaFile);
				appendRecentRiaFiles();
			});
			mnuOpenedList.add(mnu);
		}
	}

	/**
	 * Sets the tab title.
	 *
	 * @param tabIndex the tab index
	 * @param title the title
	 */
	public void setTabTitle(int tabIndex, String title) {
		JPanel tabHeader = (JPanel)tabbedPaneRight.getTabComponentAt(tabIndex);
		JLabel titleLabel = (JLabel)tabHeader.getComponent(0);
		titleLabel.setText(title);
		JScrollPane sp = (JScrollPane)tabbedPaneRight.getComponentAt(tabIndex);
		RiaEditorPanel editor = (RiaEditorPanel)sp.getViewport().getView();
		tabHeader.setToolTipText(editor.getRiaFile());
	}

	/**
	 * Gets the tab title.
	 *
	 * @param tabIndex the tab index
	 * @return the tab title
	 */
	public String getTabTitle(int tabIndex) {
		JPanel tabHeader = (JPanel)tabbedPaneRight.getTabComponentAt(tabIndex);
		JLabel titleLabel = (JLabel)tabHeader.getComponent(0);
		return titleLabel.getText();
	}

	/**
	 * Adds a new RIA document into the tabbed pane.
	 */
	private void newRiaDocument() {
		int tabIndex = tabbedPaneRight.getTabCount();
		RiaEditorPanel editor = new RiaEditorPanel(tabbedPaneRight, tabIndex);
		JScrollPane js = new JScrollPane(editor);
		JViewport vp = js.getViewport();
		vp.setBackground(Color.WHITE);
		vp.setScrollMode(JViewport.BLIT_SCROLL_MODE);
		tabbedPaneRight.addTab("", js); //$NON-NLS-1$
		Helper.addTabWithCloseButton(tabbedPaneRight, js, RiaEditorPanel.EDITOR_TITLE + Messages.getString("MainWindow.NO_NAME_DOC"), new ActionListener() { //$NON-NLS-1$
			RiaEditorPanel selEditor = editor;
			@Override
			public void actionPerformed(ActionEvent e) {
				tabbedPaneRight.setSelectedIndex(selEditor.getTabIndex());
				selEditor.onClose();
			}
		});
		tabbedPaneRight.setSelectedIndex(tabIndex);
		editor.requestFocus();
	}

	/**
	 * Open RIA document into an editor tab.
	 *
	 * @param fileName the file name
	 */
	private void openRiaDocument(String fileName) {
		String riaFile;
		if(fileName == null) {
			if(riaFileChooser == null)
				riaFileChooser = Helper.createFileChooser(Messages.getString("MainWindow.FCO_RIA_TITLE"), CHATBOTS_ROOT_FOLDER, Messages.getString("MainWindow.FCO_RIA_FILTER"), "ria"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			if(riaFileChooser.showOpenDialog(mainFrame) != JFileChooser.APPROVE_OPTION)
				return;
			riaFile = riaFileChooser.getSelectedFile().getAbsolutePath();
		} else riaFile = new File(fileName).getAbsolutePath();
		int ri = isRiaFileAlreadyOpened(riaFile);
		if(ri >= 0) {
			tabbedPaneRight.setSelectedIndex(ri);
			return;
		}
		mnuNewChatBot.doClick();
		int tabIndex = tabbedPaneRight.getTabCount() - 1;
		JScrollPane sp = (JScrollPane)tabbedPaneRight.getComponentAt(tabIndex);
		RiaEditorPanel editor = (RiaEditorPanel)sp.getViewport().getView();
		editor.openRiaFile(riaFile);
		settings.addOpenedRecently(riaFile);
		appendRecentRiaFiles();
	}

	/**
	 * Gets the active RIA editor.
	 *
	 * @return the active RIA editor
	 */
	public RiaEditorPanel getActiveRiaEditor() {
		Component c = tabbedPaneRight.getSelectedComponent();
		if(c != null && c instanceof JScrollPane) {
			JScrollPane sp = (JScrollPane)c;
			return (RiaEditorPanel)sp.getViewport().getView();
		}
		return null;
	}

	/**
	 * Register hot keys for main features so the user can use them by shortcuts.
	 */
	private void registerHotKeys() {
		hotKeyManager = new HotkeyManager(splitPaneVertical);
		hotKeyManager.registerHotkey("control O", Messages.getString("MainWindow.OPEN_FILE"), e -> btnOpen.doClick()); //$NON-NLS-1$ //$NON-NLS-2$
		hotKeyManager.registerHotkey("control N", Messages.getString("MainWindow.NEW_FILE"), e -> btnNew.doClick()); //$NON-NLS-1$ //$NON-NLS-2$
		hotKeyManager.registerHotkey("control W", Messages.getString("MainWindow.CLOSE_FILE"), e -> mnuCloseDoc.doClick()); //$NON-NLS-1$ //$NON-NLS-2$
		hotKeyManager.registerHotkey("control S", Messages.getString("MainWindow.SAVE_FILE"), e -> { //$NON-NLS-1$ //$NON-NLS-2$
			RiaEditorPanel editor = getActiveRiaEditor();
			if(editor != null) {
				mnuSaveDoc.doClick();
			} else Toolkit.getDefaultToolkit().beep();
		});
		hotKeyManager.registerHotkey("control V", Messages.getString("MainWindow.PASTE_RIP"), e -> { //$NON-NLS-1$ //$NON-NLS-2$
			RiaEditorPanel editor = getActiveRiaEditor();
			if(editor != null) {
				editor.paste();
			} else Toolkit.getDefaultToolkit().beep();
		});
		hotKeyManager.registerHotkey("control C", Messages.getString("MainWindow.COPY_RIP"), e -> { //$NON-NLS-1$ //$NON-NLS-2$
			RiaEditorPanel editor = getActiveRiaEditor();
			if(editor != null) {
				NodePanel rip = editor.getSelectedRouteItem();
				if(rip != null) {
					rip.copy();
				} else Toolkit.getDefaultToolkit().beep();
			} else Toolkit.getDefaultToolkit().beep();
		});
		hotKeyManager.registerHotkey("control X", Messages.getString("MainWindow.CUT_RIP"), e -> { //$NON-NLS-1$ //$NON-NLS-2$
			RiaEditorPanel editor = getActiveRiaEditor();
			if(editor != null) {
				NodePanel rip = editor.getSelectedRouteItem();
				if(rip != null) {
					rip.cut();
				} else Toolkit.getDefaultToolkit().beep();
			} else Toolkit.getDefaultToolkit().beep();
		});
		hotKeyManager.registerHotkey("F2", Messages.getString("MainWindow.EDIT_RIP"), e -> { //$NON-NLS-1$ //$NON-NLS-2$
			RiaEditorPanel editor = getActiveRiaEditor();
			if(editor != null) {
				NodePanel rip = editor.getSelectedRouteItem();
				if(rip != null) {
					rip.edit();
				} else Toolkit.getDefaultToolkit().beep();
			} else Toolkit.getDefaultToolkit().beep();
		});
		hotKeyManager.registerHotkey("DELETE", Messages.getString("MainWindow.DELETE_RIP"), e -> { //$NON-NLS-1$ //$NON-NLS-2$
			RiaEditorPanel editor = getActiveRiaEditor();
			if(editor != null) {
				NodePanel rip = editor.getSelectedRouteItem();
				if(rip != null) {
					rip.delete();
				} else Toolkit.getDefaultToolkit().beep();
			} else Toolkit.getDefaultToolkit().beep();
		});
		hotKeyManager.registerHotkey("INSERT", Messages.getString("MainWindow.ADD_RIP"), e -> { //$NON-NLS-1$ //$NON-NLS-2$
			RiaEditorPanel editor = getActiveRiaEditor();
			if(editor != null) {
				editor.addNew();
			} else Toolkit.getDefaultToolkit().beep();
		});
		hotKeyManager.registerHotkey("control Z", Messages.getString("MainWindow.UNDO_RIP"), e -> { //$NON-NLS-1$ //$NON-NLS-2$
			RiaEditorPanel editor = getActiveRiaEditor();
			if(editor != null) {
				btnUndo.doClick();
			} else Toolkit.getDefaultToolkit().beep();
		});
		hotKeyManager.registerHotkey("control Y", Messages.getString("MainWindow.REDO_RIP"), e -> { //$NON-NLS-1$ //$NON-NLS-2$
			RiaEditorPanel editor = getActiveRiaEditor();
			if(editor != null) {
				btnRedo.doClick();
			} else Toolkit.getDefaultToolkit().beep();
		});
	}

	/**
	 * Create the application window.
	 */
	public MainWindow() {
		conChat4Us = connectToDatabase("chat4us.db"); //$NON-NLS-1$
		if(conChat4Us == null) {
			JOptionPane.showMessageDialog(null, Messages.getString("MainWindow.MB_DB_CON_FAILURE_MSG"), Messages.getString("MainWindow.MB_DB_CON_FAILURE_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			System.exit(0);
			return;
		}
		if(!new File(Settings.SETTINGS_FILE).exists()) {
			Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
			settings = new Settings();
			settings.setMainWndSize(new Dimension(800, 600));
			settings.setMainWndPos(new Point(scr.width/2 - 400, scr.height/2 - 300));
			settings.save();
		} else settings = Settings.load();
		if(settings == null) {
			JOptionPane.showMessageDialog(null, Messages.getString("MainWindow.MB_SETTINGS_LOADING_ERROR_MSG"), Messages.getString("MainWindow.MB_SETTINGS_LOADING_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			System.exit(0);
			return;
		}
		try {
			Locale.setDefault(Locale.forLanguageTag(settings.getDefLocale()));
			JComponent.setDefaultLocale(Locale.getDefault());
		} catch(IllegalArgumentException e) {
			JOptionPane.showMessageDialog(null, Messages.getString("MainWindow.MB_DEF_LOCALE_ERROR_MSG"), Messages.getString("MainWindow.MB_DEF_LOCALE_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			System.exit(0);
			return;
		}
		setupTrayIcon();

		initialize();

		Point splitPos = settings.getMainWndSplitPos();
		if(splitPos.x > 0 && splitPos.y > 0) {
			splitPaneHorizontal.setDividerLocation(splitPos.x);
			splitPaneVertical.setDividerLocation(splitPos.y);
		}
		mainFrame.setLocation(settings.getMainWndPos());
		mainFrame.setSize(settings.getMainWndSize());

		epLog.setContentType("text/html"); //$NON-NLS-1$
		epLog.setText("<html><head><style>body { font-family:sans-serif; font-size:11pt; margin:0px; padding:0px; } p { margin:0px; padding:0px; }</style></head><body></body></html>"); //$NON-NLS-1$

		lstChatBots.setCellRenderer(new ChatServerListCellRenderer());
		ChatServerListModel cblm = new ChatServerListModel(new ArrayList<ChatServer>());
		lstChatBots.setModel(cblm);
		lstChatBots.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) { lstChatBotsMousePressed(e); }
			@Override
			public void mouseClicked(MouseEvent e) {
				if((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
					int index = lstChatBots.locationToIndex(e.getPoint());
					if(index != -1) {
						Rectangle r = lstChatBots.getCellBounds(index, index);
						if((r != null) && r.contains(e.getPoint())) {
							ChatServer cs = lstChatBots.getSelectedValue();
							if(cs != null) {
								menuServerEdit(cs);
								return;
							}
						}
					}
				} else return;
				Toolkit.getDefaultToolkit().beep();
			}
		});
		lstWebsites.setModel(new IdLabelComboModel());
		lstWebsites.setCellRenderer(new IdLabelComboRenderer());
		lstWebsites.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) { lstWebsitesMousePressed(e); }
			@Override
			public void mouseClicked(MouseEvent e) {
				if((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
					int index = lstWebsites.locationToIndex(e.getPoint());
					if(index != -1) {
						Rectangle r = lstWebsites.getCellBounds(index, index);
						if((r != null) && r.contains(e.getPoint())) {
							IdLabelComboElement we = lstWebsites.getSelectedValue();
							if(we != null) {
								menuWebsiteEdit(we);
								return;
							}
						}
					}
				} else return;
				Toolkit.getDefaultToolkit().beep();
			}
		});
		lstAgents.setModel(new IdLabelComboModel());
		lstAgents.setCellRenderer(new IdLabelComboRenderer());
		lstAgents.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) { lstAgentsMousePressed(e); }
			@Override
			public void mouseClicked(MouseEvent e) {
				if((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
					int index = lstAgents.locationToIndex(e.getPoint());
					if(index != -1) {
						Rectangle r = lstWebsites.getCellBounds(index, index);
						if((r != null) && r.contains(e.getPoint())) {
							IdLabelComboElement ag = lstAgents.getSelectedValue();
							if(ag != null) {
								menuAgentEdit(ag);
								return;
							}
						}
					}
				} else return;
				Toolkit.getDefaultToolkit().beep();
			}
		});

		this.timer = new Timer(1000, null);
		this.timer.addActionListener(e -> {
			lblStatusText.setText(Messages.getString("MainWindow.STATUS_BAR_DEF_TEXT")); //$NON-NLS-1$
			lstChatBots.repaint();
		});
		this.timer.start();

		Helper.addPopupMenuToTextBoxes(mainFrame);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		mainWindow = this;
		mainFrame = new JFrame();
		mainFrame.setTitle("Chat4Us-Creator " + Helper.getAppVersion()); //$NON-NLS-1$
		mainFrame.setSize(new Dimension(800, 600));
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/icons/icon-48.png"))); //$NON-NLS-1$
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				tabbedPaneRight.repaint();
				int state = settings.getMainWndState();
				if(state != JFrame.ICONIFIED)
					mainFrame.setExtendedState(state);
				registerHotKeys();
				loadData(e);
				appendRecentRiaFiles();
			}

			@Override
			public void windowIconified(WindowEvent e) {
				if(settings.isMinimizeToTray()) {
					new Thread(() -> {
						Helper.threadSleep(500);
						mainFrame.setVisible(false);
					}).start();
				}
			}

			@Override
			public void windowClosing(WindowEvent e) {
				boolean cancelled = false;
				int rslt = Helper.showConfirmDialog(mainFrame, Messages.getString("MainWindow.MB_DISCONNECT_ALL_MSG"), Messages.getString("MainWindow.MB_TITLE_CONFIRMATION"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
				if(rslt == JOptionPane.YES_OPTION) {
					JScrollPane sp;
					RiaEditorPanel editor;
					while(tabbedPaneRight.getTabCount() > 0) {
						tabbedPaneRight.setSelectedIndex(0);
						sp = (JScrollPane)tabbedPaneRight.getComponentAt(0);
						editor = (RiaEditorPanel)sp.getViewport().getView();
						cancelled |= editor.onClose() == JOptionPane.CANCEL_OPTION;
						if(cancelled)
							break;
					}
					if(!cancelled) {
						timer.stop();
						ChatServer cs;
						ChatServerListModel model = (ChatServerListModel)lstChatBots.getModel();
						for(int i = 0; i < model.getSize(); i++) {
							cs = model.getElementAt(i);
							cs.stopServer();
						}
						settings.setMainWndState(mainFrame.getExtendedState());
						mainFrame.setExtendedState(JFrame.NORMAL);
						settings.setMainWndSize(mainFrame.getSize());
						settings.setMainWndPos(mainFrame.getLocation());
						settings.setMainWndSplitPos(new Point(splitPaneHorizontal.getDividerLocation(), splitPaneVertical.getDividerLocation()));
						settings.save();
						try {
							conChat4Us.close();
							conChat4Us = null;
							if(dbOptimize) {
								Helper.logInfo(Messages.getString("MainWindow.DB_VACUUM_STARTED")); //$NON-NLS-1$
						        conChat4Us = connectToDatabase("chat4us.db"); //$NON-NLS-1$
						        try (Statement statement = conChat4Us.createStatement()) {
						            statement.executeUpdate("VACUUM;"); //$NON-NLS-1$
						            Helper.logInfo(Messages.getString("MainWindow.DB_VACUUM_COMPLETED")); //$NON-NLS-1$
						        } catch (SQLException ex) {
									Helper.logWarning(ex, Messages.getString("MainWindow.DB_VACUUM_ERROR"), false); //$NON-NLS-1$
								}
							}
						} catch (SQLException ex) {
							Helper.logWarning(ex, Messages.getString("MainWindow.DB_CLOSING_ERROR"), false); //$NON-NLS-1$
						}
						mainFrame.dispose();
					}
				}
			}
			@Override
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});

		JMenuBar menuBar = new JMenuBar();
		menuBar.setFocusable(false);
		mainFrame.setJMenuBar(menuBar);

		JMenu mnuFile = new JMenu(Messages.getString("MainWindow.MNU_FILE")); //$NON-NLS-1$
		mnuFile.addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent e) { }
			public void menuDeselected(MenuEvent e) { }
			public void menuSelected(MenuEvent e) {
				boolean onRiaDoc = tabbedPaneRight.getTabCount() > 0;
				mnuSaveDoc.setEnabled(onRiaDoc);
				mnuSaveAsDoc.setEnabled(onRiaDoc);
				mnuCloseDoc.setEnabled(onRiaDoc);
			}
		});
		menuBar.add(mnuFile);

		JMenuItem mnuQuit = new JMenuItem(Messages.getString("MainWindow.MNU_QUIT")); //$NON-NLS-1$
		mnuQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
		mnuQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
			}
		});

		mnuNewDoc = new JMenuItem(Messages.getString("MainWindow.MNU_NEW_FILE")); //$NON-NLS-1$
		mnuNewDoc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		mnuNewDoc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnNew.doClick();
			}
		});
		mnuFile.add(mnuNewDoc);

		mnuOpenDoc = new JMenuItem(Messages.getString("MainWindow.MNU_OPEN_FILE")); //$NON-NLS-1$
		mnuOpenDoc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		mnuOpenDoc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnOpen.doClick();
			}
		});
		mnuFile.add(mnuOpenDoc);

				mnuRecentDoc = new JMenu(Messages.getString("MainWindow.MNU_OPENED_RECENTLY")); //$NON-NLS-1$
				mnuFile.add(mnuRecentDoc);
		mnuFile.addSeparator();

		mnuSaveDoc = new JMenuItem(Messages.getString("MainWindow.MNU_SAVE_FILE")); //$NON-NLS-1$
		mnuSaveDoc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		mnuSaveDoc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnSave.doClick();
			}
		});
		mnuFile.add(mnuSaveDoc);

		mnuSaveAsDoc = new JMenuItem(Messages.getString("MainWindow.MNU_SAVE_FILE_AS")); //$NON-NLS-1$
		mnuSaveAsDoc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnSaveAs.doClick();
			}
		});
		mnuFile.add(mnuSaveAsDoc);
		mnuFile.addSeparator();

		mnuCloseDoc = new JMenuItem(Messages.getString("MainWindow.MNU_CLOSE")); //$NON-NLS-1$
		mnuCloseDoc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
		mnuCloseDoc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tabbedPaneRight.getTabCount() > 0) {
					JScrollPane sp = (JScrollPane)tabbedPaneRight.getSelectedComponent();
					RiaEditorPanel editor = (RiaEditorPanel)sp.getViewport().getView();
					editor.onClose();
				} else Toolkit.getDefaultToolkit().beep();
			}
		});
		mnuFile.add(mnuCloseDoc);
		mnuFile.addSeparator();
		mnuFile.add(mnuQuit);

		mnuChatBotAI = new JMenu(Messages.getString("MainWindow.MNU_CHATBOTS")); //$NON-NLS-1$
		menuBar.add(mnuChatBotAI);

		mnuNewChatBot = new JMenuItem(Messages.getString("MainWindow.MNU_NEW")); //$NON-NLS-1$
		mnuNewChatBot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		mnuNewChatBot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newRiaDocument();
			}
		});
		mnuChatBotAI.add(mnuNewChatBot);

		JMenuItem mnuOpenChatBot = new JMenuItem(Messages.getString("MainWindow.MNU_OPEN")); //$NON-NLS-1$
		mnuOpenChatBot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		mnuOpenChatBot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openRiaDocument(null);
			}
		});
		mnuChatBotAI.add(mnuOpenChatBot);

		mnuOpenedList = new JMenu(Messages.getString("MainWindow.MNU_OPENED_RECENTLY")); //$NON-NLS-1$
		mnuChatBotAI.add(mnuOpenedList);
		mnuChatBotAI.addSeparator();

		JMenu mnuChatServers = new JMenu(Messages.getString("MainWindow.MNU_SERVERS")); //$NON-NLS-1$
		mnuChatServers.addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent e) { }
			public void menuDeselected(MenuEvent e) { }
			public void menuSelected(MenuEvent e) {
				ChatServer cs = lstChatBots.getSelectedValue();
				boolean activated = false;
				boolean selected = cs != null;
				if(selected) {
					activated = cs.isEnabled();
					menuServerAddAssistants(cs, mnuServerAssistants);
				}
				if(selected) {
					mnuServerDeActivate.setSelected(cs.isEnabled());
					mnuServerDeActivate.setText(cs.isEnabled() ? Messages.getString("MainWindow.MNUU_DEACTIVATE") : Messages.getString("MainWindow.MNU_ACTIVATE")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				mnuServerRestart.setEnabled(activated);
				mnuServerDeActivate.setEnabled(selected);
				mnuServerAssistants.setEnabled(selected);
				mnuServerRemove.setEnabled(selected);
				mnuServerEdit.setEnabled(selected);
				mnuOpenInEditor.setEnabled(selected);
				mnuInternalChatClient.setEnabled(selected && activated);
			}
		});
		menuBar.add(mnuChatServers);

		JMenuItem mnuNewServer = new JMenuItem(Messages.getString("MainWindow.MNU_NEW_SERVER")); //$NON-NLS-1$ //$NON-NLS-1$
		mnuNewServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPaneLeft.setSelectedIndex(0);
				lstChatBots.requestFocus();
				menuServerNew();
			}
		});
		mnuChatServers.add(mnuNewServer);
		mnuChatServers.addSeparator();
		mnuServerDeActivate = new JCheckBoxMenuItem(Messages.getString("MainWindow.MNU_DEACTIVATE")); //$NON-NLS-1$
		mnuServerDeActivate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPaneLeft.setSelectedIndex(0);
				lstChatBots.requestFocus();
				menuServerDeActivate(lstChatBots.getSelectedValue());
			}
		});
		mnuServerRestart = new JMenuItem(Messages.getString("MainWindow.MNU_RESTART")); //$NON-NLS-1$
		mnuChatServers.add(mnuServerRestart);
		mnuServerDeActivate.setSelected(true);
		mnuChatServers.add(mnuServerDeActivate);
		mnuChatServers.addSeparator();
		mnuServerEdit = new JMenuItem(Messages.getString("MainWindow.MNU_EDIT")); //$NON-NLS-1$
		mnuServerEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPaneLeft.setSelectedIndex(0);
				lstChatBots.requestFocus();
				menuServerEdit(lstChatBots.getSelectedValue());
			}
		});
		mnuChatServers.add(mnuServerEdit);

		mnuServerRemove = new JMenuItem(Messages.getString("MainWindow.MNU_DELETE")); //$NON-NLS-1$
		mnuServerRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPaneLeft.setSelectedIndex(0);
				lstChatBots.requestFocus();
				menuServerRemove(lstChatBots.getSelectedValue());
			}
		});
		mnuChatServers.add(mnuServerRemove);

		mnuServerAssistants = new JMenu(Messages.getString("MainWindow.MNU_ASSISTANTS")); //$NON-NLS-1$
		mnuChatServers.add(mnuServerAssistants);
		mnuChatServers.addSeparator();

		mnuOpenInEditor = new JMenuItem(Messages.getString("MainWindow.MNU_OPEN_IN_EDITOR")); //$NON-NLS-1$ //$NON-NLS-1$
		mnuChatServers.add(mnuOpenInEditor);

		mnuInternalChatClient = new JMenuItem(Messages.getString("MainWindow.MNU_INTERNAL_CHAT_CLIENT")); //$NON-NLS-1$ //$NON-NLS-1$
		mnuChatServers.add(mnuInternalChatClient);

		JMenu mnuAgents = new JMenu(Messages.getString("MainWindow.MNU_AGENTS")); //$NON-NLS-1$
		mnuAgents.addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent e) { }
			public void menuDeselected(MenuEvent e) { }
			public void menuSelected(MenuEvent e) {
				IdLabelComboElement el = lstAgents.getSelectedValue();
				boolean enabled = el != null;
				if(enabled) {
					mnuAgentDeActivate.setSelected(el.isEnabled());
					mnuAgentDeActivate.setText(el.isEnabled() ? Messages.getString("MainWindow.MNU_DEACTIVATE") : Messages.getString("MainWindow.MNU_ACTIVATE")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				mnuAgentDeActivate.setEnabled(enabled);
				mnuAgentEdit.setEnabled(enabled);
				mnuAgentRemove.setEnabled(enabled);
			}
		});
		menuBar.add(mnuAgents);

		JMenuItem mnuNewAgent = new JMenuItem(Messages.getString("MainWindow.MNU_NEW_AGENT")); //$NON-NLS-1$ //$NON-NLS-1$
		mnuNewAgent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPaneLeft.setSelectedIndex(1);
				lstAgents.requestFocus();
				menuAgentNew();
			}
		});
		mnuAgents.add(mnuNewAgent);
		mnuAgents.addSeparator();

		mnuAgentDeActivate = new JCheckBoxMenuItem(Messages.getString("MainWindow.CBMI_DEACTIVATE")); //$NON-NLS-1$
		mnuAgentDeActivate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPaneLeft.setSelectedIndex(1);
				lstAgents.requestFocus();
				menuAgentDeActivate(lstAgents.getSelectedValue());
			}
		});
		mnuAgentDeActivate.setSelected(true);
		mnuAgents.add(mnuAgentDeActivate);

		mnuAgentEdit = new JMenuItem(Messages.getString("MainWindow.MNU_EDIT")); //$NON-NLS-1$
		mnuAgentEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPaneLeft.setSelectedIndex(1);
				lstAgents.requestFocus();
				menuAgentEdit(lstAgents.getSelectedValue());
			}
		});
		mnuAgents.add(mnuAgentEdit);

		mnuAgentRemove = new JMenuItem(Messages.getString("MainWindow.MNU_DELETE")); //$NON-NLS-1$
		mnuAgentRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPaneLeft.setSelectedIndex(1);
				lstAgents.requestFocus();
				menuAgentRemove(lstAgents.getSelectedValue());
			}
		});
		mnuAgents.add(mnuAgentRemove);

		JMenu mnuClients = new JMenu(Messages.getString("MainWindow.MNU_WEBSITES")); //$NON-NLS-1$
		mnuClients.addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent e) { }
			public void menuDeselected(MenuEvent e) { }
			public void menuSelected(MenuEvent e) {
				IdLabelComboElement el = lstWebsites.getSelectedValue();
				boolean enabled = el != null;
				if(enabled) {
					mnuWebsiteDeActivate.setSelected(el.isEnabled());
					mnuWebsiteDeActivate.setText(el.isEnabled() ? Messages.getString("MainWindow.MNU_DEACTIVATE") : Messages.getString("MainWindow.MNU_ACTIVATE")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				mnuWebsiteDeActivate.setEnabled(enabled);
				mnuWebsiteEdit.setEnabled(enabled);
				mnuWebsiteRemove.setEnabled(enabled);
			}
		});
		menuBar.add(mnuClients);

		JMenuItem mnuNewWebsite = new JMenuItem(Messages.getString("MainWindow.MNU_NEW_DOTS")); //$NON-NLS-1$
		mnuNewWebsite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPaneLeft.setSelectedIndex(2);
				lstWebsites.requestFocus();
				menuWebsiteNew();
			}
		});
		mnuClients.add(mnuNewWebsite);
		mnuClients.addSeparator();

		mnuWebsiteDeActivate = new JCheckBoxMenuItem(Messages.getString("MainWindow.CBMI_DEACTIVATE")); //$NON-NLS-1$
		mnuWebsiteDeActivate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPaneLeft.setSelectedIndex(2);
				lstWebsites.requestFocus();
				menuWebsiteDeactivate(lstWebsites.getSelectedValue());
			}
		});
		mnuWebsiteDeActivate.setSelected(true);
		mnuClients.add(mnuWebsiteDeActivate);

		mnuWebsiteEdit = new JMenuItem(Messages.getString("MainWindow.MNU_EDIT")); //$NON-NLS-1$
		mnuWebsiteEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPaneLeft.setSelectedIndex(2);
				lstWebsites.requestFocus();
				menuWebsiteEdit(lstWebsites.getSelectedValue());
			}
		});
		mnuClients.add(mnuWebsiteEdit);

		mnuWebsiteRemove = new JMenuItem(Messages.getString("MainWindow.MNU_DELETE")); //$NON-NLS-1$
		mnuWebsiteRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPaneLeft.setSelectedIndex(2);
				lstWebsites.requestFocus();
				menuWebsiteRemove(lstWebsites.getSelectedValue());
			}
		});
		mnuClients.add(mnuWebsiteRemove);

		JMenu mnuTools = new JMenu(Messages.getString("MainWindow.MNU_TOOLS")); //$NON-NLS-1$
		menuBar.add(mnuTools);

				mnNewMenu_1 = new JMenu(Messages.getString("MainWindow.MNU_CLEAN")); //$NON-NLS-1$
				mnuTools.add(mnNewMenu_1);
				mnuTools.addSeparator();

						mnuCleanLogFiles = new JMenuItem(Messages.getString("MainWindow.MNU_CLEAN_LOG_FILES")); //$NON-NLS-1$
						mnuCleanLogFiles.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								int ret = Helper.showConfirmDialog(mainFrame, Messages.getString("MainWindow.MB_LOG_FILES_CLEAN_MSG"), Messages.getString("MainWindow.MB_TITLE_CONFIRMATION"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
								if(ret == JOptionPane.YES_OPTION) {
									int n = 0;
									int nMax = settings.getLogsTimeoutDays()*24*60*60*1000;
									File[] files = new File("./log").listFiles(); //$NON-NLS-1$
									if(files != null) {
										for(File f : files) {
											if(f.getName().endsWith(".log") && f.lastModified() < System.currentTimeMillis() - nMax) { //$NON-NLS-1$
												f.delete();
												n++;
											}
										}
									}
									Helper.logInfo(String.format(Messages.getString("MainWindow.N_LOG_FILES_REMOVED"), n)); //$NON-NLS-1$
									}
								}
							});
						mnNewMenu_1.add(mnuCleanLogFiles);

								mnuCleanChatFiles = new JMenuItem(Messages.getString("MainWindow.MNU_SAVED_CHATS_CLEAN")); //$NON-NLS-1$
								mnuCleanChatFiles.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										int ret = Helper.showConfirmDialog(mainFrame, Messages.getString("MainWindow.MB_SAVED_CHATS_CLEAN_MSG"), Messages.getString("MainWindow.MB_TITLE_CONFIRMATION"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
										if(ret == JOptionPane.YES_OPTION) {
											int n = 0;
											int nMax = settings.getChatsTimeoutDays()*24*60*60*1000;
											File[] files = new File("./chat4us").listFiles(); //$NON-NLS-1$
											if(files != null) {
												for(File f : files) {
													if(f.getName().endsWith(".txt") && f.lastModified() < System.currentTimeMillis() - nMax) { //$NON-NLS-1$
														f.delete();
														n++;
													}
												}
											}
											Helper.logInfo(String.format(Messages.getString("MainWindow.N_SAVED_CHATS_REMOVED"), n)); //$NON-NLS-1$
										}
									}
								});
								mnNewMenu_1.add(mnuCleanChatFiles);

								mnuCleanDatabase = new JMenuItem(Messages.getString("MainWindow.CLEAN_DB_COMPONENTS")); //$NON-NLS-1$
								mnuCleanDatabase.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										int ret = Helper.showConfirmDialog(mainFrame, Messages.getString("MainWindow.MSGBOX_DB_CLEAN_MESSAGE"), Messages.getString("MainWindow.MB_TITLE_CONFIRMATION"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
										if(ret == JOptionPane.NO_OPTION)
											return;
										try {
											int rowsDeleted;
											List<String> tableNames = Helper.getNonSystemTables(conChat4Us);
											for (String tableName : tableNames) {
												rowsDeleted = Helper.removeRecords(conChat4Us, tableName);
												if (rowsDeleted > 0)
													Helper.logInfo(String.format(Messages.getString("MainWindow.LOG_DB_N_RECS_REMOVED"), rowsDeleted, tableName), false); //$NON-NLS-1$
											}
											Helper.logInfo(Messages.getString("MainWindow.DB_CLEANING_COMPLETED")); //$NON-NLS-1$
											aiGroups = Helper.loadTable(conChat4Us, "ai_groups"); //$NON-NLS-1$
											agPostes = Helper.loadTable(conChat4Us, "agents_postes"); //$NON-NLS-1$
											aiServers = Helper.loadTable(conChat4Us, "ai_servers"); //$NON-NLS-1$
											dbOptimize = true;
										} catch (SQLException ex) {
											Helper.logError(ex, Messages.getString("MainWindow.DB_CLEANING_ERROR"), true); //$NON-NLS-1$
										}
									}
								});
								mnNewMenu_1.add(mnuCleanDatabase);

								mnuSettings = new JMenuItem(Messages.getString("MainWindow.MNU_OPTIONS")); //$NON-NLS-1$
								mnuSettings.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										SettingsDialog dlg = new SettingsDialog(mainFrame);
										dlg.setLocationRelativeTo(mainFrame);
										dlg.setVisible(true);
										if(!dlg.isCancelled()) {
											aiGroups = Helper.loadTable(conChat4Us, "ai_groups"); //$NON-NLS-1$
											agPostes = Helper.loadTable(conChat4Us, "agents_postes"); //$NON-NLS-1$
											aiServers = Helper.loadTable(conChat4Us, "ai_servers"); //$NON-NLS-1$
										}
										dlg.dispose();
									}
								});

								mnNewMenu_6 = new JMenu(Messages.getString("MainWindow.MNU_SECURITY")); //$NON-NLS-1$
								mnuTools.add(mnNewMenu_6);

														JMenuItem mnuCertGen = new JMenuItem(Messages.getString("MainWindow.MNU_CERT_REGENERATE")); //$NON-NLS-1$
														mnNewMenu_6.add(mnuCertGen);

														mnuCertImport = new JMenuItem(Messages.getString("MainWindow.MNU_CERT_IMPORT")); //$NON-NLS-1$
														mnuCertImport.addActionListener(new ActionListener() {
															public void actionPerformed(ActionEvent e) {
																CertImportDialog dlg = new CertImportDialog(mainFrame);
																dlg.setLocationRelativeTo(mainFrame);
																dlg.setVisible(true);
																dlg.dispose();
															}
														});
														mnNewMenu_6.add(mnuCertImport);
														mnuCertGen.addActionListener(new ActionListener() {
															public void actionPerformed(ActionEvent e) {
																CertGenDialog dlg = new CertGenDialog(mainFrame);
																dlg.setLocationRelativeTo(mainFrame);
																dlg.setVisible(true);
																dlg.dispose();
															}
														});
								mnuTools.addSeparator();
								mnuTools.add(mnuSettings);

		JMenu mnuHelp = new JMenu(Messages.getString("MainWindow.MNU_HELP")); //$NON-NLS-1$
		menuBar.add(mnuHelp);

		JMenuItem mnuAbout = new JMenuItem(Messages.getString("MainWindow.MNU_ABOUT")); //$NON-NLS-1$
		mnuAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AboutDialog dlg = new AboutDialog(mainFrame);
				dlg.setLocationRelativeTo(mainFrame);
				dlg.setVisible(true);
				dlg.dispose();
			}
		});

		mnuOpenGuide = new JMenuItem(Messages.getString("MainWindow.MNU_GUIDE_OPEN")); //$NON-NLS-1$
		mnuOpenGuide.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		mnuOpenGuide.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.openContent("https://chat4usai.com/chat4us-creator-user-guide/"); //$NON-NLS-1$
			}
		});
		mnuHelp.add(mnuOpenGuide);

		mnuGetStarted = new JMenuItem(Messages.getString("MainWindow.MNU_GET_STARTED")); //$NON-NLS-1$
		mnuGetStarted.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.openContent("https://chat4usai.com/get-started/"); //$NON-NLS-1$
			}
		});
		mnuHelp.add(mnuGetStarted);

		mnuTutorials = new JMenuItem(Messages.getString("MainWindow.MNU_TUTORIALS")); //$NON-NLS-1$ //$NON-NLS-1$
		mnuTutorials.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.openContent("https://chat4usai.com/tutorials/"); //$NON-NLS-1$
			}
		});
		mnuHelp.add(mnuTutorials);

		mnuExamples = new JMenuItem(Messages.getString("MainWindow.MNU_EXAMPLES")); //$NON-NLS-1$
		mnuExamples.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.openContent("https://chat4usai.com/chat-bots-examples/"); //$NON-NLS-1$
			}
		});
		mnuHelp.add(mnuExamples);

		mnuSampleProjects = new JMenuItem(Messages.getString("MainWindow.MNU_SAMPLE_PROJECTS")); //$NON-NLS-1$
		mnuSampleProjects.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.openContent("https://chat4usai.com/downloads#sample-projects/"); //$NON-NLS-1$
			}
		});
		mnuHelp.add(mnuSampleProjects);
		mnuHelp.addSeparator();
		mnuCheck4Updates = new JMenuItem(Messages.getString("MainWindow.MNU_CHECK4UPDATES")); //$NON-NLS-1$ //$NON-NLS-1$
		mnuCheck4Updates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.openContent("https://chat4usai.com/downloads/?app=chat4us-creator&version=" + Helper.getAppVersion()); //$NON-NLS-1$
			}
		});
		mnuHelp.add(mnuCheck4Updates);
		mnuHelp.addSeparator();
		mnuHelp.add(mnuAbout);

		mnuContribute = new JMenuItem(Messages.getString("MainWindow.MNU_CONTRIBUTE")); //$NON-NLS-1$
		mnuHelp.add(mnuContribute);

		JToolBar toolBar = new JToolBar();
		toolBar.setFocusable(false);
		mainFrame.getContentPane().add(toolBar, BorderLayout.NORTH);

		btnNew = new JButton(""); //$NON-NLS-1$
		btnNew.setToolTipText(Messages.getString("MainWindow.BTN_NEW_DOC_TOOLTIP")); //$NON-NLS-1$
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newRiaDocument();
			}
		});
		btnNew.setFocusable(false);
		btnNew.setIcon(Helper.loadIconFromResources("/toolbar/new.png", 32, 32)); //$NON-NLS-1$
		btnNew.setRolloverIcon(Helper.loadIconFromResources("/toolbar/selected/new.png", 32, 32)); //$NON-NLS-1$
		toolBar.add(btnNew);

		btnOpen = new JButton(""); //$NON-NLS-1$
		btnOpen.setToolTipText(Messages.getString("MainWindow.BTN_OPEN_TOOLTIP")); //$NON-NLS-1$
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openRiaDocument(null);
			}
		});
		btnOpen.setFocusable(false);
		btnOpen.setIcon(Helper.loadIconFromResources("/toolbar/open.png", 32, 32)); //$NON-NLS-1$
		btnOpen.setRolloverIcon(Helper.loadIconFromResources("/toolbar/selected/open.png", 32, 32)); //$NON-NLS-1$
		toolBar.add(btnOpen);

		btnSave = new JButton(""); //$NON-NLS-1$
		btnSave.setToolTipText(Messages.getString("MainWindow.BTN_SAVE_TOOLTIP")); //$NON-NLS-1$
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tabbedPaneRight.getTabCount() > 0) {
					JScrollPane sp = (JScrollPane)tabbedPaneRight.getSelectedComponent();
					RiaEditorPanel editor = (RiaEditorPanel)sp.getViewport().getView();
					if(editor.saveRiaDocument())
						setTabTitle(tabbedPaneRight.getSelectedIndex(), RiaEditorPanel.EDITOR_TITLE + new File(editor.getRiaFile()).getName());
				} else Toolkit.getDefaultToolkit().beep();
			}
		});
		btnSave.setFocusable(false);
		btnSave.setIcon(Helper.loadIconFromResources("/toolbar/save.png", 32, 32)); //$NON-NLS-1$
		btnSave.setRolloverIcon(Helper.loadIconFromResources("/toolbar/selected/save.png", 32, 32)); //$NON-NLS-1$
		toolBar.add(btnSave);

		btnSaveAs = new JButton(""); //$NON-NLS-1$
		btnSaveAs.setToolTipText(Messages.getString("MainWindow.BTN_SAVEAS_TOOLTIP")); //$NON-NLS-1$
		btnSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tabbedPaneRight.getTabCount() > 0) {
					JScrollPane sp = (JScrollPane)tabbedPaneRight.getSelectedComponent();
					RiaEditorPanel editor = (RiaEditorPanel)sp.getViewport().getView();
					String xmlFile = editor.getRiaFile();
					if(xmlFile != null) {
						editor.saveRiaDocumentAs();
						if(!xmlFile.equals(editor.getRiaFile())) {
							setTabTitle(tabbedPaneRight.getSelectedIndex(), RiaEditorPanel.EDITOR_TITLE + new File(editor.getRiaFile()).getName());
						}
					} else editor.saveRiaDocument();
				} else Toolkit.getDefaultToolkit().beep();
			}
		});
		btnSaveAs.setFocusable(false);
		btnSaveAs.setIcon(Helper.loadIconFromResources("/toolbar/saveas.png", 32, 32)); //$NON-NLS-1$
		btnSaveAs.setRolloverIcon(Helper.loadIconFromResources("/toolbar/selected/saveas.png", 32, 32)); //$NON-NLS-1$
		toolBar.add(btnSaveAs);

		toolBar.addSeparator();

		btnUndo = new JButton(""); //$NON-NLS-1$
		btnUndo.setToolTipText(Messages.getString("MainWindow.BTN_UNDO_TOOLTIP")); //$NON-NLS-1$
		btnUndo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tabbedPaneRight.getTabCount() > 0) {
					JScrollPane sp = (JScrollPane)tabbedPaneRight.getSelectedComponent();
					RiaEditorPanel editor = (RiaEditorPanel)sp.getViewport().getView();
					editor.undo();
				} else Toolkit.getDefaultToolkit().beep();
			}
		});
		btnUndo.setFocusable(false);
		btnUndo.setIcon(Helper.loadIconFromResources("/toolbar/undo.png", 32, 32)); //$NON-NLS-1$
		btnUndo.setRolloverIcon(Helper.loadIconFromResources("/toolbar/selected/undo.png", 32, 32)); //$NON-NLS-1$
		toolBar.add(btnUndo);

		btnRedo = new JButton(""); //$NON-NLS-1$
		btnRedo.setToolTipText(Messages.getString("MainWindow.BTN_REDO_TOOLTIP")); //$NON-NLS-1$
		btnRedo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tabbedPaneRight.getTabCount() > 0) {
					JScrollPane sp = (JScrollPane)tabbedPaneRight.getSelectedComponent();
					RiaEditorPanel editor = (RiaEditorPanel)sp.getViewport().getView();
					editor.redo();
				} else Toolkit.getDefaultToolkit().beep();
			}
		});
		btnRedo.setFocusable(false);
		btnRedo.setIcon(Helper.loadIconFromResources("/toolbar/redo.png", 32, 32)); //$NON-NLS-1$
		btnRedo.setRolloverIcon(Helper.loadIconFromResources("/toolbar/selected/redo.png", 32, 32)); //$NON-NLS-1$
		toolBar.add(btnRedo);
		toolBar.addSeparator();

		btnCopy = new JButton(""); //$NON-NLS-1$
		btnCopy.setToolTipText(Messages.getString("MainWindow.BTN_COPY_TOOLTIP")); //$NON-NLS-1$
		btnCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tabbedPaneRight.getTabCount() == 0) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				JScrollPane sp = (JScrollPane)tabbedPaneRight.getSelectedComponent();
				RiaEditorPanel editor = (RiaEditorPanel)sp.getViewport().getView();
				NodePanel rip = editor.getSelectedRouteItem();
				if(rip != null) {
					rip.copy();
				} else Toolkit.getDefaultToolkit().beep();
			}
		});
		btnCopy.setFocusable(false);
		btnCopy.setIcon(Helper.loadIconFromResources("/toolbar/copy.png", 32, 32)); //$NON-NLS-1$
		btnCopy.setRolloverIcon(Helper.loadIconFromResources("/toolbar/selected/copy.png", 32, 32)); //$NON-NLS-1$
		toolBar.add(btnCopy);

		btnCut = new JButton(""); //$NON-NLS-1$
		btnCut.setToolTipText(Messages.getString("MainWindow.BTN_CUT_TOOLTIP")); //$NON-NLS-1$
		btnCut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tabbedPaneRight.getTabCount() == 0) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				JScrollPane sp = (JScrollPane)tabbedPaneRight.getSelectedComponent();
				RiaEditorPanel editor = (RiaEditorPanel)sp.getViewport().getView();
				NodePanel rip = editor.getSelectedRouteItem();
				if(rip != null) {
					rip.cut();
				} else Toolkit.getDefaultToolkit().beep();
			}
		});
		btnCut.setFocusable(false);
		btnCut.setIcon(Helper.loadIconFromResources("/toolbar/cut.png", 32, 32)); //$NON-NLS-1$
		btnCut.setRolloverIcon(Helper.loadIconFromResources("/toolbar/selected/cut.png", 32, 32)); //$NON-NLS-1$
		toolBar.add(btnCut);

		btnPaste = new JButton(""); //$NON-NLS-1$
		btnPaste.setToolTipText(Messages.getString("MainWindow.BTN_PASTE_TOOLTIP")); //$NON-NLS-1$
		btnPaste.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tabbedPaneRight.getTabCount() == 0) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				JScrollPane sp = (JScrollPane)tabbedPaneRight.getSelectedComponent();
				RiaEditorPanel editor = (RiaEditorPanel)sp.getViewport().getView();
				editor.paste();
			}
		});
		btnPaste.setFocusable(false);
		btnPaste.setIcon(Helper.loadIconFromResources("/toolbar/paste.png", 32, 32)); //$NON-NLS-1$
		btnPaste.setRolloverIcon(Helper.loadIconFromResources("/toolbar/selected/paste.png", 32, 32)); //$NON-NLS-1$
		toolBar.add(btnPaste);
		toolBar.addSeparator();

		btnSettings = new JButton(""); //$NON-NLS-1$
		btnSettings.setToolTipText(Messages.getString("MainWindow.BTN_SETTINGS_TOOLTIP")); //$NON-NLS-1$
		btnSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mnuSettings.doClick();
			}
		});
		btnSettings.setFocusable(false);
		btnSettings.setIcon(Helper.loadIconFromResources("/toolbar/settings.png", 32, 32)); //$NON-NLS-1$
		btnSettings.setRolloverIcon(Helper.loadIconFromResources("/toolbar/selected/settings.png", 32, 32)); //$NON-NLS-1$
		toolBar.add(btnSettings);

		JPanel panel = new JPanel();
		panel.setFocusable(false);
		mainFrame.getContentPane().add(panel, BorderLayout.SOUTH);

		lblStatusText = new JLabel(Messages.getString("MainWindow.STATUS_BAR_DEF_TEXT")); //$NON-NLS-1$

		JPanel pnlStatus = new JPanel();

		JLabel lblChatBotsIcon = new JLabel(""); //$NON-NLS-1$
		lblChatBotsIcon.setIcon(Helper.loadIconFromResources("/chatbot.png", 16, 16)); //$NON-NLS-1$
		pnlStatus.add(lblChatBotsIcon);

		lblChatBotsCount = new JLabel("0"); //$NON-NLS-1$
		pnlStatus.add(lblChatBotsCount);

		JLabel lblAgentsIcon = new JLabel(""); //$NON-NLS-1$
		lblAgentsIcon.setIcon(Helper.loadIconFromResources("/agent.png", 16, 16)); //$NON-NLS-1$
		pnlStatus.add(lblAgentsIcon);

		lblAgentsCount = new JLabel("0"); //$NON-NLS-1$
		pnlStatus.add(lblAgentsCount);

		JLabel lblWebsitesIcon = new JLabel(""); //$NON-NLS-1$
		lblWebsitesIcon.setIcon(Helper.loadIconFromResources("/web.png", 16, 16)); //$NON-NLS-1$
		pnlStatus.add(lblWebsitesIcon);

		lblWebsitesCount = new JLabel("0"); //$NON-NLS-1$
		pnlStatus.add(lblWebsitesCount);
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(5)
					.addComponent(lblStatusText)
					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(pnlStatus, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(1)
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false)
						.addComponent(lblStatusText, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(pnlStatus, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addGap(1))
		);

		lblChatsIcon = new JLabel(""); //$NON-NLS-1$
		lblChatsIcon.setIcon(Helper.loadIconFromResources("/chat.png", 16, 16)); //$NON-NLS-1$
		pnlStatus.add(lblChatsIcon);

		lblChatsCount = new JLabel("0"); //$NON-NLS-1$
		pnlStatus.add(lblChatsCount);

		panel.setLayout(gl_panel);

		splitPaneVertical = new JSplitPane();
		splitPaneVertical.setResizeWeight(1.0);
		splitPaneVertical.setFocusable(false);
		splitPaneVertical.setOrientation(JSplitPane.VERTICAL_SPLIT);
		mainFrame.getContentPane().add(splitPaneVertical, BorderLayout.CENTER);

		splitPaneHorizontal = new JSplitPane();
		splitPaneHorizontal.setFocusable(false);
		splitPaneVertical.setLeftComponent(splitPaneHorizontal);

		tabbedPaneLeft = new JTabbedPane(JTabbedPane.TOP);
		tabbedPaneLeft.setFocusable(false);
		splitPaneHorizontal.setLeftComponent(tabbedPaneLeft);

		JPanel pnlChatBots = new JPanel();
		pnlChatBots.setFocusable(false);
		tabbedPaneLeft.addTab(Messages.getString("MainWindow.TAB_TITLE_CHATBOTS"), null, pnlChatBots, null); //$NON-NLS-1$
		pnlChatBots.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_1 = new JScrollPane();
		pnlChatBots.add(scrollPane_1, BorderLayout.CENTER);

		lstChatBots = new JList<>();
		scrollPane_1.setViewportView(lstChatBots);

		JPanel pnlAgents = new JPanel();
		tabbedPaneLeft.addTab(Messages.getString("MainWindow.TAB_TITLE_AGENTS"), null, pnlAgents, null); //$NON-NLS-1$
		pnlAgents.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_2 = new JScrollPane();
		pnlAgents.add(scrollPane_2, BorderLayout.CENTER);

		lstAgents = new JList<>();
		scrollPane_2.setViewportView(lstAgents);

		JPanel pnlWebsites = new JPanel();
		tabbedPaneLeft.addTab(Messages.getString("MainWindow.TAB_TITLE_WEBSITES"), null, pnlWebsites, null); //$NON-NLS-1$
		pnlWebsites.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_4 = new JScrollPane();
		pnlWebsites.add(scrollPane_4, BorderLayout.CENTER);

		lstWebsites = new JList<>();
		scrollPane_4.setViewportView(lstWebsites);

		tabbedPaneRight = new JTabbedPane(JTabbedPane.TOP) {
			@Override
			public void paint(Graphics g) {
				super.paint(g);

				if(tabbedPaneRight.getTabCount() != 0)
					return;
				try {
					if(imgBkg == null) {
						imgBkg = ImageIO.read(getClass().getResource("/about-logo-grayed.png")); //$NON-NLS-1$
					} else {
						int x = (getWidth() - imgBkg.getWidth()) / 2;
						int y = (getHeight() - imgBkg.getHeight()) / 2;
						g.drawImage(imgBkg, x, y, null);
					}
				} catch (IOException ex) {
					Helper.logError(ex, Messages.getString("MainWindow.ABOUT_LOGO_ERROR"), false); //$NON-NLS-1$
				}
			}
		};
		tabbedPaneRight.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
				int tabIndex = sourceTabbedPane.getSelectedIndex();
				if(tabIndex != -1)
					sourceTabbedPane.getComponentAt(tabIndex).requestFocusInWindow();
			}
		});
		tabbedPaneRight.setFocusable(false);
		splitPaneHorizontal.setRightComponent(tabbedPaneRight);
		splitPaneHorizontal.setDividerLocation(200);

		JTabbedPane tabbedPaneBottom = new JTabbedPane(JTabbedPane.TOP);
		tabbedPaneBottom.setFocusable(false);
		splitPaneVertical.setRightComponent(tabbedPaneBottom);

		JPanel panel_2 = new JPanel();
		panel_2.setFocusable(false);
		tabbedPaneBottom.addTab(Messages.getString("MainWindow.TAB_TITLE_LOG"), null, panel_2, null); //$NON-NLS-1$
		panel_2.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel_2.add(scrollPane, BorderLayout.CENTER);

		epLog = new JEditorPane();
		epLog.setContentType("text/html"); //$NON-NLS-1$
		epLog.setEditable(false);
		scrollPane.setViewportView(epLog);
		splitPaneVertical.setDividerLocation(350);

		Helper.enableRtlWhenNeeded(mainFrame);
	}

	/**
	 * Launch the application.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		String logFileName = "./log/" + Helper.toDate(Instant.now(), "dd-MM-yyyy_HH-mm-ss") + ".log"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		try {
			PrintStream originalOut = System.out;
			PrintStream originalErr = System.err;
			FileOutputStream fileOut = new FileOutputStream(logFileName, true);
			PrintStream fileStream = new PrintStream(fileOut, true);
			System.setOut(new TeePrintStream(originalOut, fileStream));
			System.setErr(new TeePrintStream(originalErr, fileStream));
		} catch(IOException ex) {
			ex.printStackTrace();
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			Helper.logError(ex, "Error setting L&F", false); //$NON-NLS-1$
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
					Helper.logError(e, String.format(Messages.getString("MainWindow.UNCAUGHT_EXCEPTION"), t.getName()), false); //$NON-NLS-1$
					if(Helper.getActiveWindow() instanceof UExceptionDialog) {
						Helper.logError(e, String.format(Messages.getString("MainWindow.LOG_UNCAUGHT_MULTIPLE_EXCEPTIONS"), e.getClass().getName(), e.getMessage(), t.getName()), false); //$NON-NLS-1$
						return;
					}
					JFrame frame = MainWindow.getFrame();
					UExceptionDialog dlg = new UExceptionDialog(mainFrame, t, e);
					dlg.setLocationRelativeTo(frame != null ? frame : null);
					dlg.setVisible(true);
					dlg.dispose();
				});

				new MainWindow();
				MainWindow.mainFrame.setVisible(true);
			}
		});
	}
}
