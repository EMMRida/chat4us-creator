/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.gui;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;

import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import io.github.emmrida.chat4usagent.controls.ChatSessionListCellRenderer;
import io.github.emmrida.chat4usagent.controls.ChatSessionListModel;
import io.github.emmrida.chat4usagent.core.ChatServer;
import io.github.emmrida.chat4usagent.core.ChatSession;
import io.github.emmrida.chat4usagent.gui.SettingsDialog.Settings;
import io.github.emmrida.chat4usagent.util.AudioClip;
import io.github.emmrida.chat4usagent.util.Helper;
import io.github.emmrida.chat4usagent.util.Messages;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.awt.BorderLayout;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JToggleButton;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.Timer;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.google.gson.Gson;

import javax.swing.event.ListSelectionEvent;
import javax.swing.JSeparator;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BoxLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Toolkit;

/**
 * This class represents the main window of the application.
 *
 * @author El Mhadder Mohamed Rida
 */
public class MainWindow {
	private static final String appVersion = " 0.1.25"; //$NON-NLS-1$

    private static final String PREF_X = "frame_x"; //$NON-NLS-1$
    private static final String PREF_Y = "frame_y"; //$NON-NLS-1$
    private static final String PREF_WIDTH = "frame_width"; //$NON-NLS-1$
    private static final String PREF_HEIGHT = "frame_height"; //$NON-NLS-1$
    private static final String PREF_STATE = "frame_state"; //$NON-NLS-1$

	private static Icon connectedIcon;
	private static Icon disconnectedIcon;
	private static ChatServer chatServer;
	private static MainWindow mainWindow;

	private TrayIcon trayIcon = null;
	private Settings settings;
	private AudioClip newChat;
	private AudioClip newMsg;
	private AudioClip missedChat;
	private JPopupMenu pMenuChats;

	private static JFrame thisFrame;
	private JMenuItem mnuQuit;
	private JToggleButton tglConnected;
	private JPanel pnlChat;
	private JScrollPane spChat;
	private JList<ChatSession> lstChats;
	private JTextArea taAgentMsg;
	private JButton btnSettings;
	private JPanel pnlAgentMsg;
	private JCheckBoxMenuItem mnuLocaleEN;
	private JCheckBoxMenuItem mnuLocaleAR;
	private JCheckBoxMenuItem mnuLocaleFR;
	private JSplitPane splitPane;
	private JButton btnCss;

	/** Returns the main frame of the application. */
	public static JFrame getFrame() { return thisFrame; }
	/** Returns the main window of the application. */
	public static MainWindow getInstance() { return mainWindow; }
	/** Logs a message. */
	public static void log(String msg) { }
	/** Get settings instance. */
	public static Settings getSettings() { return getInstance().settings; }
	/** Returns true if the application is accepting connections from remote chat bots servers. */
	public boolean isConnected() { return tglConnected.isSelected(); }
	/** Returns the tray icon. */
	public TrayIcon getTrayIcon() { return trayIcon; }

	/**
	 * Saves the frame state
	 */
    private void saveFrameState() {
        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        int state = thisFrame.getExtendedState();
        if ((state & thisFrame.MAXIMIZED_BOTH) == 0) { // Only save size if not maximized
            prefs.putInt(PREF_X, thisFrame.getX());
            prefs.putInt(PREF_Y, thisFrame.getY());
            prefs.putInt(PREF_WIDTH, thisFrame.getWidth());
            prefs.putInt(PREF_HEIGHT, thisFrame.getHeight());
        }
        prefs.putInt(PREF_STATE, state);
    }

    /**
     * Restores the frame state
     */
    private void restoreFrameState() {
        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        int x = prefs.getInt(PREF_X, 100);
        int y = prefs.getInt(PREF_Y, 100);
        int width = prefs.getInt(PREF_WIDTH, 800);
        int height = prefs.getInt(PREF_HEIGHT, 600);
        int state = prefs.getInt(PREF_STATE, Frame.NORMAL);

        thisFrame.setBounds(x, y, width, height);
        thisFrame.setExtendedState(state!=Frame.ICONIFIED ? state : Frame.NORMAL);
    }

	/**
	 * Create the application.
	 */
	public MainWindow() {
		connectedIcon = Helper.loadIconFromResources("/connected.png", 32, 32); //$NON-NLS-1$
		disconnectedIcon = Helper.loadIconFromResources("/disconnected.png", 32, 32); //$NON-NLS-1$
		newChat = new AudioClip("./snd/new-chat.wav"); //$NON-NLS-1$
		newMsg = new AudioClip("./snd/new-msg.wav"); //$NON-NLS-1$
		missedChat = new AudioClip("./snd/missed-chat.wav"); //$NON-NLS-1$
		initialize();

		restoreFrameState();
	}

	/**
	 * Scrolls the chat window to the bottom
	 */
	private void scrollToBottom() {
		Timer timer = new Timer(10, null);
		ActionListener al = new ActionListener() {
			float steps = -1.0f;
			@Override
			public void actionPerformed(ActionEvent e) {
				JScrollBar bar = spChat.getVerticalScrollBar();
				int max = bar.getMaximum() - bar.getVisibleAmount();
				if(steps < 0.0f)
					steps = (max - bar.getValue()) / 10;
				bar.setValue(bar.getValue() + Math.round(steps*=0.915f));
				if(steps <= 1.0f)
					steps = 1.0f;
				if(bar.getValue() >= max)
					timer.stop();
			}
		};
		timer.addActionListener(al);
		timer.start();
	}

	/**
	 * Fills the chat window with ChatSession messages. This is called when a ChatSession is redirected by
	 * a chat bot when a remote user ask for human support.
	 * @param ses ChatSession object
	 */
	private void fillChat(ChatSession ses) {
		if(ses != null) {
			long now = System.currentTimeMillis();
			pnlChat.removeAll();
			for(String msg : ses.getChatMessages()) {
				IMessagePanel panel = null;
				String[] parts = msg.split(":", 2); //$NON-NLS-1$
				if("User".equalsIgnoreCase(parts[0])) { //$NON-NLS-1$
					panel = new UserMessage();
				} else if("Sys".equalsIgnoreCase(parts[0])) { //$NON-NLS-1$
					panel = new ErrorMessage();
				} else panel = new AgentMessage();
				pnlChat.add((JPanel)panel);
				panel.setMessage(parts[1], now);
			}
			pnlChat.repaint();
			scrollToBottom();
		} else {
			pnlChat.removeAll();
			pnlChat.repaint();
			pnlChat.revalidate();
		}
	}

	/**
	 * Send a response back to the remote user using the Undertow Http server exchange.
	 * @param exchange Undertow Http server exchange
	 * @param agentMsg Agent message
	 * @param statusMsg Status message
	 * @param statusCode Status code
	 */
	private void exchangeResult(HttpServerExchange exchange, String agentMsg, String statusMsg, int statusCode) {
		exchange.setStatusCode(statusCode);
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json"); //$NON-NLS-1$
		Map<String, Object> rslt = new HashMap<>();
		if(agentMsg != null)
			rslt.put("AGENT_MESSAGE", agentMsg); //$NON-NLS-1$
		rslt.put("STATUS_MESSAGE", statusMsg); //$NON-NLS-1$
		rslt.put("STATUS_CODE", statusCode); //$NON-NLS-1$
		rslt.put("STATUS", statusCode == 200 ? "OK" : "ERROR"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		exchange.getResponseSender().send(new Gson().toJson(rslt));
	}

	/**
	 * Load the application settings after a successful login.
	 * Creates the chat server and starts listening for chat requests to let the agent respond
	 * back to the remote users.
	 */
	private void loadData() {
		settings = Settings.load();
		if(settings == null) {
			settings = new Settings();
			settings.setHost("localhost"); //$NON-NLS-1$
			settings.setPort(7443);
			settings.setNotifSound(true);
			settings.setNotifSys(true);
			settings.setAutoConnect(true);
			settings.setBringToFront(true);
			settings.setIconMinimize(false);
			if(!settings.save()) {
				cleanAndQuit();
				return;
			}
		}

		AppAuthDialog dlg = new AppAuthDialog(thisFrame);
		dlg.setLocationRelativeTo(thisFrame);
		dlg.setVisible(true);
		if(dlg.isCancelled()) {
			cleanAndQuit();
			return;
		}
		if(Helper.checkKeystorePassword(dlg.getPassword())) {
			chatServer = new ChatServer(settings.getHost(), settings.getPort());
			if(chatServer.startSecureServer(dlg.getPassword())) {
				chatServer.addChatListener(new ChatServer.ChatListener() {
					/** @see ChatServer.ChatListener#onLetsChat(HttpServerExchange, ChatSession, String) */
					@Override
					public void onLetsChat(HttpServerExchange exchange, ChatSession ses, String chat) {
						if(tglConnected.isSelected()) {
							ChatSessionListModel model = (ChatSessionListModel)lstChats.getModel();
							if(!model.contains(ses)) {
								SwingUtilities.invokeLater(() -> {
									model.addElement(ses);
									ses.userMsgArrived(exchange);
									if(model.getSize() == 1) {
										lstChats.setSelectedIndex(0);
										fillChat(ses);
										updateScrollUnit();
									}
									if(settings.isNotifSound())
										newChat.play();
									pnlAgentMsg.setEnabled(true);
									if(settings.getBringToFront()) {
										thisFrame.setState(JFrame.NORMAL);
										thisFrame.setVisible(true);
										thisFrame.toFront();
									}
									if(trayIcon != null && settings.isNotifSys() && !thisFrame.isFocused()) {
										trayIcon.displayMessage(Messages.getString("MainWindow.TRAY_TITLE_INCOMING_CHAT"), Messages.getString("MainWindow.TRAY_MSG_INCOMING_CHAT") + ses.getUserId(), TrayIcon.MessageType.INFO); //$NON-NLS-1$ //$NON-NLS-2$
									}
									thisFrame.revalidate();
									thisFrame.repaint();
								});
							} else exchangeResult(exchange, null, Messages.getString("MainWindow.SESSION_EXISTANT_400"), 400); //$NON-NLS-1$
						} else { // Agent is not accepting new chats
							if(settings.isNotifSound())
								missedChat.play();
							chatServer.removeChatSession(ses.getUserId());
							exchangeResult(exchange, null, Messages.getString("MainWindow.UNAVAILABLE_SERVICE_503"), 503); //$NON-NLS-1$
						}
					}

					/** @see ChatServer.ChatListener#onIncomingMessage(HttpServerExchange, ChatSession, String) */
					@Override
					public void onIncomingMessage(HttpServerExchange exchange, ChatSession ses, String message) {
						ChatSessionListModel model = (ChatSessionListModel)lstChats.getModel();
						if(model.contains(ses)) {
							long now = System.currentTimeMillis();
							ses.userMsgArrived(exchange, message);
							ses.addMessage(message, false);
							if(lstChats.getSelectedValue() == ses) {
								SwingUtilities.invokeLater(() -> {
									IMessagePanel panel = new UserMessage();
									panel.setMessage(message, now);
									pnlChat.add((JPanel)panel);
									pnlChat.revalidate();
									pnlChat.repaint();
									updateScrollUnit();
									scrollToBottom();
									pnlAgentMsg.setEnabled(true);
									if(settings.getBringToFront()) {
										thisFrame.setState(JFrame.NORMAL);
										thisFrame.setVisible(true);
										thisFrame.toFront();
									}
									if(trayIcon != null && settings.isNotifSys() && !(thisFrame.isActive())) {
										trayIcon.displayMessage(Messages.getString("MainWindow.TTRAY_TITLE_INCOMING_MSG") + ses.getUserId(), message, TrayIcon.MessageType.INFO); //$NON-NLS-1$
									}
								});
							}
							if(settings.isNotifSound())
								newMsg.play();
							// TODO : Hilight the session item.
						} else { // TODO : Handle the case where the session does not exist
							exchangeResult(exchange, null, Messages.getString("MainWindow.INEXISTANT_SESSION_400"), 400); //$NON-NLS-1$
						}
					}

					/** @see ChatServer.ChatListener#onMessageTimeout(ChatSession) */
					@Override
					public void onMessageTimeout(ChatSession ses) {
						Helper.logWarning(Messages.getString("MainWindow.SESSION_TIMEOUT_ERROR") + ses.getUserId()); //$NON-NLS-1$
					}

					/** @see ChatServer.ChatListener#onChatError(HttpServerExchange, ChatSession) */
					@Override
					public void onChatError(HttpServerExchange exchange, ChatSession ses) {
						ChatSessionListModel model = (ChatSessionListModel)lstChats.getModel();
						if(model.contains(ses)) {
							String errMsg = Messages.getString("MainWindow.TERMINATED_CHAT_ERROR"); //$NON-NLS-1$
							if(ses.equals(lstChats.getSelectedValue())) {
								SwingUtilities.invokeLater(() -> {
									IMessagePanel panel = new ErrorMessage();
									panel.setMessage(errMsg, 0);
									pnlChat.add((JPanel)panel);
									pnlChat.revalidate();
									pnlChat.repaint();
									updateScrollUnit();
									scrollToBottom();
									// TODO : Play error sound.
								});
							} else ses.addMessage("Sys:" + errMsg); //$NON-NLS-1$
						}
					}
				});
				Timer timer = new Timer(1000, e -> {
					if(lstChats.getModel().getSize() > 0) {
						lstChats.repaint();
						lstChats.revalidate();
					}
				});
				timer.start();
			} else {
				Helper.logError(Messages.getString("MainWindow.LOG_SERVER_START_FAILURE"), true); //$NON-NLS-1$
				cleanAndQuit();
			}
		} else {
			Helper.logError(Messages.getString("MainWindow.LOG_AUTH_FAILURE"), true); //$NON-NLS-1$
			cleanAndQuit();
		}
		tglConnected.setSelected(settings.isAutoConnect());
	}

	/**
	 * Update the chat window scroll unit.
	 */
	private void updateScrollUnit() {
		spChat.getVerticalScrollBar().setUnitIncrement((int)Math.round((double)pnlChat.getHeight() / lstChats.getSelectedValue().getChatMessages().size()));
	}

	/**
	 * Clean and quit the app.
	 */
	private void cleanAndQuit() {
		if(chatServer != null)
			chatServer.stopServer();
		newChat.close();
		newMsg.close();
		missedChat.close();
		if(trayIcon != null)
			SystemTray.getSystemTray().remove(trayIcon);
		// TODO : Free app and system resources.
		thisFrame.dispose();
		System.exit(0);
	}

	/**
	 * Change the locale of the app
	 * @param locale The new locale
	 */
	private void localeChange(String locale) {
		int ret = Helper.showConfirmDialog(thisFrame, Messages.getString("MainWindow.MSGBOX_LOCALE_CHANGE"), Messages.getString("MainWindow.MSGBOX_LOCALE_CHANGE_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
		if(ret == JOptionPane.YES_OPTION) {
			Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
			prefs.put("locale", locale); //$NON-NLS-1$
			cleanAndQuit();
		}
	}

	/**
	 *
	 */
	private void setupTrayIcon() {
		trayIcon = Helper.setupTrayIcon();
		if(trayIcon == null)
			return;
		final PopupMenu pmnuTrayIcon = new PopupMenu();
		MenuItem mnuDisConnect = new MenuItem(Messages.getString("MainWindow.PMNU_DISABLED_DISCONNECT")); //$NON-NLS-1$
		mnuDisConnect.addActionListener(e -> {
			if(tglConnected.isSelected()) {
				tglConnected.doClick();
				mnuDisConnect.setLabel(Messages.getString("MainWindow.LBL_DISCONNECT")); //$NON-NLS-1$
			} else {
				tglConnected.doClick();
				mnuDisConnect.setLabel(Messages.getString("MainWindow.LBL_CONNECT")); //$NON-NLS-1$
			}
		});
		mnuDisConnect.setEnabled(false);
		pmnuTrayIcon.add(mnuDisConnect);
		MenuItem mnuSettings = new MenuItem(Messages.getString("MainWindow.MNU_SETTINGS")); //$NON-NLS-1$
		mnuSettings.addActionListener(e -> {
			btnSettings.doClick();
		});
		pmnuTrayIcon.add(mnuSettings);
		pmnuTrayIcon.addSeparator();
		MenuItem mnuExit = new MenuItem(Messages.getString("MainWindow.MNU_QUIT")); //$NON-NLS-1$
		mnuExit.addActionListener(e -> {
			WindowEvent we = new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING);
			thisFrame.dispatchEvent(we);
		});
		pmnuTrayIcon.add(mnuExit);
		trayIcon.setPopupMenu(pmnuTrayIcon);
		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					thisFrame.setExtendedState(JFrame.NORMAL);
					thisFrame.setVisible(true);
					thisFrame.toFront();
				} else if(e.getClickCount() == 1) {
					if(e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
						if(chatServer != null) {
							mnuDisConnect.setEnabled(true);
							mnuDisConnect.setLabel(tglConnected.isSelected() ? Messages.getString("MainWindow.LBL_DISCONNECT") : Messages.getString("MainWindow.LBL_CONNECT")); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
				super.mouseClicked(e);
			}
		});
	}

	/**
	 * Initialize the contents of the thisFrame.
	 */
	private void initialize() {
		mainWindow = this;
		thisFrame = new JFrame();
		thisFrame.setTitle(Messages.getString("MainWindow.MAINWND_TITLE") + appVersion); //$NON-NLS-1$
		thisFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/icons/icon-48.png"))); //$NON-NLS-1$
		thisFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				switch(Locale.getDefault().getLanguage()) {
					case "ar": mnuLocaleAR.setSelected(true); break; //$NON-NLS-1$
					case "en": mnuLocaleEN.setSelected(true); break; //$NON-NLS-1$
					default: mnuLocaleFR.setSelected(true);
				}
				loadData();
				splitPane.setDividerLocation(0.75);
			}

			@Override
			public void windowClosing(WindowEvent e) {
				if(isConnected()) {
					int ret = Helper.showConfirmDialog(thisFrame, Messages.getString("MainWindow.CONFDLG_MSG"), Messages.getString("MainWindow.CONFDLG_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
					if(ret != JOptionPane.YES_OPTION)
						return;
				}
				saveFrameState();
				cleanAndQuit();
			}

			@Override
			public void windowIconified(WindowEvent e) {
				if(settings.getIconMinimize()) {
					new Thread(() -> {
						Helper.threadSleep(500);
						thisFrame.setVisible(false);
					}).start();
				}
			}
		});
		thisFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		thisFrame.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		thisFrame.getContentPane().add(panel, BorderLayout.CENTER);

		splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

		spChat = new JScrollPane();
		spChat.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		spChat.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		splitPane.setLeftComponent(spChat);

		pnlChat = new JPanel();
		pnlChat.setBackground(new Color(255, 255, 255));
		spChat.setViewportView(pnlChat);
		pnlChat.setLayout(new BoxLayout(pnlChat, BoxLayout.Y_AXIS));

		pnlAgentMsg = new JPanel();
		pnlAgentMsg.setEnabled(false);
		splitPane.setRightComponent(pnlAgentMsg);

		JScrollPane spAgentMsg = new JScrollPane();

		JButton btnSend = new JButton(""); //$NON-NLS-1$
		btnSend.setFocusable(false);
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChatSession ses = lstChats.getSelectedValue();
				if(ses != null) {
					HttpServerExchange xchng = ses.getExchange();
					if(xchng != null) {
						if(!xchng.isResponseComplete()) {
							String msg = taAgentMsg.getText();
							exchangeResult(xchng, msg, "OK", 200); //$NON-NLS-1$
							ses.addMessage(msg, true);
							ses.agentMsgSent();
							taAgentMsg.setText(""); //$NON-NLS-1$
							IMessagePanel panel = new AgentMessage();
							panel.setMessage(msg, System.currentTimeMillis());
							pnlChat.add((JPanel)panel);
							pnlChat.repaint();
							pnlChat.revalidate();
							scrollToBottom();
							pnlAgentMsg.setEnabled(false);
							return;
						}
					}
				}
				Helper.logWarning(Messages.getString("MainWindow.TIMEOUT_ERROR"), true); //$NON-NLS-1$
			}
		});
		btnSend.setIcon(Helper.loadIconFromResources("/send.png", 32, 32)); //$NON-NLS-1$

		JButton btnMsgTemplates = new JButton("..."); //$NON-NLS-1$
		btnMsgTemplates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(settings.getModelsNmes().size() > 0) {
					JPopupMenu menu = new JPopupMenu();
					for(int i = 0; i < settings.getModelsNmes().size(); i++) {
						String name = settings.getModelsNmes().get(i);
						JMenuItem mnu = new JMenuItem(name);
						mnu.putClientProperty(name, settings.getModelsContents().get(i));
						mnu.addActionListener(ev -> {
							DynamicInputDialog dlg = new DynamicInputDialog(thisFrame, mnu.getClientProperty(name).toString());
							dlg.setLocationRelativeTo(thisFrame);
							dlg.setVisible(true);
							String content = dlg.getResolvedString();
							if(content != null)
								taAgentMsg.append(content);
						});
						menu.add(mnu);
					}
					menu.show(btnMsgTemplates, btnMsgTemplates.getWidth()/2, btnMsgTemplates.getHeight());
				} else JOptionPane.showMessageDialog(thisFrame, Messages.getString("MainWindow.MB_NO_TEMPLATES"), Messages.getString("MainWindow.MB_NO_TEMPLATES_TITLE"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
		btnMsgTemplates.setFocusable(false);
		GroupLayout gl_pnlAgentMsg = new GroupLayout(pnlAgentMsg);
		gl_pnlAgentMsg.setHorizontalGroup(
			gl_pnlAgentMsg.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_pnlAgentMsg.createSequentialGroup()
					.addGap(3)
					.addComponent(spAgentMsg, GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
					.addGap(3)
					.addGroup(gl_pnlAgentMsg.createParallelGroup(Alignment.TRAILING, false)
						.addComponent(btnMsgTemplates, 0, 0, Short.MAX_VALUE)
						.addComponent(btnSend, GroupLayout.PREFERRED_SIZE, 52, Short.MAX_VALUE))
					.addGap(3))
		);
		gl_pnlAgentMsg.setVerticalGroup(
			gl_pnlAgentMsg.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlAgentMsg.createSequentialGroup()
					.addGap(3)
					.addGroup(gl_pnlAgentMsg.createParallelGroup(Alignment.LEADING)
						.addComponent(spAgentMsg, GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
						.addGroup(gl_pnlAgentMsg.createSequentialGroup()
							.addComponent(btnSend)
							.addGap(1)
							.addComponent(btnMsgTemplates)))
					.addGap(3))
		);

		taAgentMsg = new JTextArea();
		taAgentMsg.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
					btnSend.doClick();
				}
			}
		});
		spAgentMsg.setViewportView(taAgentMsg);
		pnlAgentMsg.setLayout(gl_pnlAgentMsg);

		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(6)
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(splitPane, GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
					.addGap(6))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
						.addComponent(scrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
						.addComponent(splitPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE))
					.addGap(6))
		);
		splitPane.setDividerLocation(340);

				lstChats = new JList<ChatSession>();
				lstChats.addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						if(e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
							final JMenuItem mnuRemove = new JMenuItem(Messages.getString("MainWindow.MNU_DEL_CHAT"));; //$NON-NLS-1$
							if(pMenuChats == null) {
								pMenuChats = new JPopupMenu();
								mnuRemove.addActionListener(ev -> {
									ChatSession ses = lstChats.getSelectedValue();
									if(ses != null) {
										int ret = Helper.showConfirmDialog(thisFrame, Messages.getString("MainWindow.MSGBOX_DEL_CHAT"), Messages.getString("MainWindow.DLG_TITLE_DELETE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
										if(ret == JOptionPane.YES_OPTION) {
											ChatSessionListModel model = (ChatSessionListModel)lstChats.getModel();
											model.removeElement(ses);
											chatServer.removeChatSession(ses.getUserId());
										}
									}
								});
								pMenuChats.add(mnuRemove);
							}
							pMenuChats.addPopupMenuListener(new PopupMenuListener() {
								@Override
								public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
									if(mnuRemove != null)
										mnuRemove.setEnabled(lstChats.getSelectedValue() != null);
								}
								@Override
								public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }
								@Override
								public void popupMenuCanceled(PopupMenuEvent e) { }

							});
							pMenuChats.show(lstChats, e.getX(), e.getY());
						}
					}
				});
				lstChats.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if(!e.getValueIsAdjusting()) {
							fillChat(lstChats.getSelectedValue());
						}
					}
				});
				scrollPane.setViewportView(lstChats);
				lstChats.setCellRenderer(new ChatSessionListCellRenderer());
				lstChats.setModel(new ChatSessionListModel(new ArrayList<ChatSession>()));
		panel.setLayout(gl_panel);

		JToolBar toolBar = new JToolBar();
		thisFrame.getContentPane().add(toolBar, BorderLayout.NORTH);

		tglConnected = new JToggleButton(""); //$NON-NLS-1$
		tglConnected.setFocusable(false);
		tglConnected.setIcon(Helper.loadIconFromResources("/disconnected.png", 32, 32)); //$NON-NLS-1$
		tglConnected.setSelectedIcon(Helper.loadIconFromResources("/connected.png", 32, 32)); //$NON-NLS-1$
		tglConnected.addChangeListener(e -> {
			if(trayIcon != null)
				trayIcon.setToolTip(tglConnected.isSelected() ? Messages.getString("MainWindow.TRAY_STATUS_CONNECTED") : Messages.getString("MainWindow.TRAY_STATUS_DISCONNECTED")); //$NON-NLS-1$ //$NON-NLS-2$
		});
		toolBar.add(tglConnected);
		toolBar.addSeparator();

		JButton btnAdd = new JButton(""); //$NON-NLS-1$
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SettingsDialog dlg = new SettingsDialog(thisFrame, true);
				dlg.setLocationRelativeTo(thisFrame);
				dlg.setVisible(true);
				if(dlg.isCancelled()) return;
				settings = dlg.getSettings();
				settings.save();
			}
		});
		btnAdd.setFocusable(false);
		btnAdd.setIcon(Helper.loadIconFromResources("/add.png", 32, 32)); //$NON-NLS-1$
		toolBar.add(btnAdd);

		btnCss = new JButton((String) null); //$NON-NLS-1$
		btnCss.setFocusable(false);
		btnCss.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MsgStylesDialog dlg = new MsgStylesDialog(thisFrame);
				dlg.setLocationRelativeTo(thisFrame);
				dlg.setCss(settings.getCss());
				dlg.setVisible(true);
				if(dlg.isCancelled()) return;
				String css = dlg.getCss();
				settings.setCss(css);
				settings.save();
				dlg.dispose();
			}
		});
		btnCss.setIcon(Helper.loadIconFromResources("/css.png", 32, 32)); //$NON-NLS-1$
		toolBar.add(btnCss);

		toolBar.addSeparator();

		btnSettings = new JButton(""); //$NON-NLS-1$
		btnSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SettingsDialog dlg = new SettingsDialog(thisFrame);
				dlg.setLocationRelativeTo(thisFrame);
				dlg.setVisible(true);
				if(dlg.isCancelled()) return;
				settings = dlg.getSettings();
				settings.save();
			}
		});
		btnSettings.setFocusable(false);
		btnSettings.setIcon(Helper.loadIconFromResources("/settings.png", 32, 32)); //$NON-NLS-1$
		toolBar.add(btnSettings);

		JMenuBar menuBar = new JMenuBar();
		thisFrame.setJMenuBar(menuBar);

		JMenu mnuFile = new JMenu(Messages.getString("MainWindow.MNU_FILE")); //$NON-NLS-1$
		menuBar.add(mnuFile);

		mnuQuit = new JMenuItem(Messages.getString("MainWindow.MNU_QUIT")); //$NON-NLS-1$
		mnuQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				WindowEvent we = new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING);
				thisFrame.dispatchEvent(we);
			}
		});
		mnuFile.add(mnuQuit);

		JMenu mnuTools = new JMenu(Messages.getString("MainWindow.MNU_TOOLS")); //$NON-NLS-1$
		menuBar.add(mnuTools);

		JMenu mnNewMenu_3 = new JMenu(Messages.getString("MainWindow.MNU_SECURITY")); //$NON-NLS-1$
		mnuTools.add(mnNewMenu_3);

				JMenuItem mnuCertImport = new JMenuItem(Messages.getString("MainWindow.MNU_CERT_IMPORT")); //$NON-NLS-1$
				mnNewMenu_3.add(mnuCertImport);

						JMenuItem mnuCertGen = new JMenuItem(Messages.getString("MainWindow.MNU_CERT_GENT")); //$NON-NLS-1$
						mnNewMenu_3.add(mnuCertGen);
						mnuCertGen.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								CertGenDialog dlg = new CertGenDialog(thisFrame);
								dlg.setLocationRelativeTo(thisFrame);
								dlg.setVisible(true);
								if(dlg.isCancelled()) return;
								mnuQuit.doClick();
							}
						});
				mnuCertImport.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						CertImportDialog dlg = new CertImportDialog(thisFrame);
						dlg.setLocationRelativeTo(thisFrame);
						dlg.setVisible(true);
						if(dlg.isCancelled()) return;
						mnuQuit.doClick();
					}
				});

		JMenu mnuLocales = new JMenu(Messages.getString("MainWindow.MNU_LOCALES")); //$NON-NLS-1$
		mnuTools.add(mnuLocales);

		mnuLocaleEN = new JCheckBoxMenuItem(Messages.getString("MainWindow.MNU_LOCALE_EN")); //$NON-NLS-1$
		mnuLocaleEN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				localeChange("en"); //$NON-NLS-1$
			}
		});
		mnuLocales.add(mnuLocaleEN);

		mnuLocaleAR = new JCheckBoxMenuItem(Messages.getString("MainWindow.MNU_LOCALE_AR")); //$NON-NLS-1$
		mnuLocaleAR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				localeChange("ar"); //$NON-NLS-1$
			}
		});
		mnuLocales.add(mnuLocaleAR);

		mnuLocaleFR = new JCheckBoxMenuItem(Messages.getString("MainWindow.MNU_LOCALE_FR")); //$NON-NLS-1$
		mnuLocaleFR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				localeChange("fr"); //$NON-NLS-1$
			}
		});
		mnuLocales.add(mnuLocaleFR);

		JSeparator separator = new JSeparator();
		mnuTools.add(separator);

		JMenuItem mnuSettings = new JMenuItem(Messages.getString("MainWindow.MNU_SETTINGS")); //$NON-NLS-1$
		mnuSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnSettings.doClick();
			}
		});
		mnuTools.add(mnuSettings);

		JMenu mnuHelp = new JMenu(Messages.getString("MainWindow.MNU_HELP")); //$NON-NLS-1$
		menuBar.add(mnuHelp);

		JMenuItem mnuAbout = new JMenuItem(Messages.getString("MainWindow.MNU_ABOUT")); //$NON-NLS-1$
		mnuAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AboutDialog dlg = new AboutDialog(thisFrame);
				dlg.setLocationRelativeTo(thisFrame);
				dlg.setVisible(true);
				dlg.dispose();
			}
		});

		JMenuItem mnuUserGuide = new JMenuItem(Messages.getString("MainWindow.MNU_USER_GUIDE")); //$NON-NLS-1$
		mnuUserGuide.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.openContent("http://chat4usai.com/chat4us-agent-user-guide/"); //$NON-NLS-1$
			}
		});
		mnuHelp.add(mnuUserGuide);
		mnuHelp.addSeparator();

		JMenuItem mnuCheck4Update = new JMenuItem(Messages.getString("MainWindow.MNU_CHECK_UPDATES")); //$NON-NLS-1$
		mnuCheck4Update.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.openContent("http://chat4usai.com/downloads/?app=chat4us-agent&version=" + appVersion.trim()); //$NON-NLS-1$
			}
		});
		mnuHelp.add(mnuCheck4Update);
		mnuHelp.addSeparator();
		mnuHelp.add(mnuAbout);

		setupTrayIcon();

		Helper.addPopupMenuToTextBoxes(thisFrame);
		Helper.enableRtlWhenNeeded(thisFrame);
		thisFrame.pack();
	}

	/**
	 * Launch the application.
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				//System.getProperties().entrySet().forEach((e) -> System.out.printf("%s +> %s\n", e.getKey(), e.getValue()));
				//System.setProperty("sun.awt.Application.name", "Chat4Us-Agent");
		        try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				try {
					Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
					Locale.setDefault(Locale.forLanguageTag(prefs.get("locale", "en"))); //$NON-NLS-1$ //$NON-NLS-2$
					JComponent.setDefaultLocale(Locale.getDefault());
					MainWindow window = new MainWindow();
					window.thisFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
