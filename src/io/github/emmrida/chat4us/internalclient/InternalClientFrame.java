/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.internalclient;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Toolkit;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import javax.swing.JToolBar;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import io.github.emmrida.chat4us.internalclient.ServerConnector.ApiResponse;
import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;

import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.JToggleButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.Component;
import javax.swing.Box;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The main frame of the internal chat client
 *
 * @author El Mhadder Mohamed Rida
 */
public class InternalClientFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private static enum UISTATE { LOGOUT, DISCONNECTED, CONNECTED, WAITING, IDLE };

	private static InternalClientFrame INSTANCE;

	private ServerConnector serverConnector = null;

	private boolean isChatbotWaiting = false;

	private JPanel contentPane;
	private JTextField tfUserMsg;
	private JPanel pnlChat;
	private JButton btnSend;
	private JToggleButton tglConnect;
	private JScrollPane spChat;
	private JProgressBar pbWait;
	private Component horizontalStrut;
	private Component horizontalStrut_1;
	private JTextField tfUserName;

	/**
	 * Called when a remote message link is clicked by the user
	 * @param id ID of the message
	 */
	public static void onRemoteMsgLinkClicked(String id) {
		if(id != null && !id.isBlank()) {
			int us = id.indexOf('_'); // TODO : Document this feature.
			if(us >= 0) {
				String response = id.substring(us).replaceAll("_", " "); //$NON-NLS-1$ //$NON-NLS-2$
				INSTANCE.tfUserMsg.setText(response);
				INSTANCE.btnSend.doClick();
			}
		}
	}

	/**
	 * Sets the UI mode
	 * @param mode The UI mode
	 */
	private void setUIMode(UISTATE mode) {
		pbWait.setEnabled(false);
		tfUserName.setEditable(false);
		tfUserMsg.setEnabled(false);
		btnSend.setEnabled(false);
		pbWait.setVisible(false);
		switch(mode) {
		case LOGOUT:
			tglConnect.setSelected(false);
			tfUserName.requestFocus();
			break;
		case DISCONNECTED:
			tglConnect.setSelected(false);
			tfUserName.setEditable(true);
			tfUserName.requestFocus();
			break;
		case CONNECTED:
			pnlChat.removeAll();
			btnSend.setEnabled(true);
			tfUserMsg.setEnabled(true);
			break;
		case WAITING:
			pbWait.setVisible(true);
			tfUserMsg.setText(""); //$NON-NLS-1$
			break;
		case IDLE:
			btnSend.setEnabled(true);
			tfUserMsg.setEnabled(true);
			tfUserMsg.requestFocus();
			break;
		}
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
	 * Create the frame and initialize
	 * @param botName Chatbot name
	 * @param botUrl Chatbot server URL
	 * @param aAuthKey Authentication key to use as a POST parameter
	 * @param hAuthKey Authentication key to use as a header parameter
	 */
	public InternalClientFrame(String botName, String botUrl, String aAuthKey, String hAuthKey) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				pbWait.setVisible(true);
                serverConnector = new ServerConnector(botUrl);
				new Thread(() -> {
                    if(serverConnector.login(aAuthKey, hAuthKey)) {
                    	SwingUtilities.invokeLater(() -> setUIMode(UISTATE.DISCONNECTED));
                    } else {
                    	JOptionPane.showMessageDialog(InternalClientFrame.this, Messages.getString("InternalClientFrame.MB_MSG_LOGIN_FAILED"), Messages.getString("InternalClientFrame.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
                    	dispatchEvent(new WindowEvent(InternalClientFrame.this, WindowEvent.WINDOW_CLOSING));
                    }
				}).start();
			}
			@Override
			public void windowClosing(WindowEvent e) {
				if(!serverConnector.logout())
					JOptionPane.showMessageDialog(InternalClientFrame.this, Messages.getString("InternalClientFrame.MB_MSG_LOGOUT_FAILED"), Messages.getString("InternalClientFrame.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
		setIconImage(Toolkit.getDefaultToolkit().getImage(InternalClientFrame.class.getResource("/icons/icon-48.png"))); //$NON-NLS-1$
		INSTANCE = this;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle(Messages.getString("InternalClientFrame.WND_TITLE_PREFIX") + botName); //$NON-NLS-1$
		setBounds(100, 100, 420, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar = new JToolBar();
		toolBar.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		contentPane.add(toolBar, BorderLayout.NORTH);

		tglConnect = new JToggleButton(""); //$NON-NLS-1$
		tglConnect.addItemListener(e -> {
			if(serverConnector.isLoggedIn()) {
				if(tglConnect.isSelected()) {
					if(tfUserName.getText().isEmpty()) {
						tglConnect.setSelected(false);
						tfUserName.requestFocus();
						JOptionPane.showMessageDialog(InternalClientFrame.this, Messages.getString("InternalClientFrame.MB_MSG_ENTER_USER_NAME"), Messages.getString("InternalClientFrame.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
						return;
					}
					pnlChat.removeAll();
					new Thread(() -> {
						ApiResponse response = serverConnector.letsChat(tfUserName.getText());
						if(response != null && response.isSuccess()) {
							String[] messages = response.getMessages();
							SwingUtilities.invokeLater(() -> {
								setUIMode(UISTATE.CONNECTED);
								IMessagePanel panel;
								for(String message : messages) {
									panel = new RemoteMessage();
									panel.setMessage(message, System.currentTimeMillis());
									pnlChat.add((JPanel)panel);
									pnlChat.revalidate();
									pnlChat.repaint();
								}
								if(!response.isChatEnded()) {
									setUIMode(UISTATE.IDLE);
								}
							});
						} else {
							SwingUtilities.invokeLater(() -> {
								setUIMode(UISTATE.LOGOUT);
		                        JOptionPane.showMessageDialog(InternalClientFrame.this, Messages.getString("InternalClientFrame.MB_MSG_CHAT_START_FAILURE"), Messages.getString("InternalClientFrame.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
							});
						}
					}).start();
				} else {
					SwingUtilities.invokeLater(() -> setUIMode(UISTATE.DISCONNECTED));
				}
			} else JOptionPane.showMessageDialog(InternalClientFrame.this, Messages.getString("InternalClientFrame.MB_MSG_USER_NOT_LOGGED_IN"), Messages.getString("InternalClientFrame.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
		});
		tglConnect.setIcon(Helper.loadIconFromResources("/toolbar/disconnected.png", 20, 20)); //$NON-NLS-1$
		tglConnect.setRolloverIcon(Helper.loadIconFromResources("/toolbar/selected/disconnected.png", 20, 20)); //$NON-NLS-1$
		tglConnect.setSelectedIcon(Helper.loadIconFromResources("/toolbar/selected/connected.png", 20, 20)); //$NON-NLS-1$
		tglConnect.setRolloverSelectedIcon(Helper.loadIconFromResources("/toolbar/selected/connected.png", 20, 20)); //$NON-NLS-1$
		tglConnect.setFocusable(false);
		toolBar.add(tglConnect);

		tfUserName = new JTextField();
		tfUserName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER)
					tglConnect.doClick();
			}
		});
		tfUserName.setEditable(false);
		tfUserName.setColumns(10);
		tfUserName.setMaximumSize(new Dimension(128, 20));
		tfUserName.setBorder(new EmptyBorder(0, 3, 0, 3));
		toolBar.add(tfUserName);

		pbWait = new JProgressBar();
		pbWait.setVisible(false);
		pbWait.setBorder(null);
		pbWait.setMaximumSize(new Dimension(32767, 20));

		horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setPreferredSize(new Dimension(5, 0));
		horizontalStrut.setMinimumSize(new Dimension(5, 0));
		toolBar.add(horizontalStrut);
		pbWait.setIndeterminate(true);
		toolBar.add(pbWait);

		horizontalStrut_1 = Box.createHorizontalStrut(20);
		horizontalStrut_1.setPreferredSize(new Dimension(5, 0));
		horizontalStrut_1.setMinimumSize(new Dimension(5, 0));
		toolBar.add(horizontalStrut_1);

		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		contentPane.add(panel, BorderLayout.SOUTH);

		tfUserMsg = new JTextField();
		tfUserMsg.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					btnSend.doClick();
				}
			}
		});
		tfUserMsg.setEnabled(false);
		tfUserMsg.setColumns(10);

		btnSend = new JButton(); //$NON-NLS-1$
		btnSend.setIcon(Helper.loadIconFromResources("/toolbar/send.png", 20, 20)); //$NON-NLS-1$
        btnSend.setRolloverIcon(Helper.loadIconFromResources("/toolbar/selected/send.png", 20, 20)); //$NON-NLS-1$
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(serverConnector != null && serverConnector.isLoggedIn()) {
					String msg = tfUserMsg.getText();
					if(!msg.isBlank()) {
						setUIMode(UISTATE.WAITING);
						if(!msg.equals("...") && !isChatbotWaiting) { //$NON-NLS-1$
							IMessagePanel panel = new UserMessage();
							panel.setMessage(Helper.escapeHtml(msg), System.currentTimeMillis());
							pnlChat.add((JPanel)panel);
							scrollToBottom();
							pnlChat.revalidate();
							pnlChat.repaint();
						}
						Thread th = new Thread(() -> {
							ApiResponse response = serverConnector.sendMessage(tfUserName.getText(), msg);
							if(response != null) {
								handleServerAnswer(response);
							} else { // Fatal error or disconnected
								SwingUtilities.invokeLater(() -> {
									setUIMode(UISTATE.DISCONNECTED);
									appendErrorMessage(Messages.getString("InternalClientFrame.CHAT_ERR_MSG_FATAL_ERROR")); //$NON-NLS-1$
								});
							}
						});
						th.start();
					} else Toolkit.getDefaultToolkit().beep();
				} else {
					setUIMode(UISTATE.DISCONNECTED);
					JOptionPane.showMessageDialog(InternalClientFrame.this, "Your are not logged in. Please close this window and try again.", Messages.getString("InternalClientFrame.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			/**
			 *
			 * @param response
			 */
			private void handleServerAnswer(ApiResponse response) {
				if(response != null && response.isSuccess()) {
					String[] messages = response.getMessages();
					for(String message : messages) {
						if(!message.isBlank()) {
							SwingUtilities.invokeLater(() -> {
								IMessagePanel pnl;
								pnl = new RemoteMessage();
								pnl.setMessage(message, System.currentTimeMillis());
								pnl.setIcon("/" + response.getChatStateIconName() + ".png"); //$NON-NLS-1$ //$NON-NLS-2$
								pnlChat.add((JPanel)pnl);
							});
						}
					}
					if(!response.isChatEnded()) {
						SwingUtilities.invokeLater(() -> {
							setUIMode(UISTATE.IDLE);
							scrollToBottom();
						});
					} else {
						SwingUtilities.invokeLater(() -> {
							setUIMode(UISTATE.DISCONNECTED);
                            appendErrorMessage(Messages.getString("InternalClientFrame.CHAT_ERR_MSG_CHAT_ENDED")); //$NON-NLS-1$
							scrollToBottom();
                        });
					}
					isChatbotWaiting = response.isChatbotWaiting();
					if(isChatbotWaiting) {
						SwingUtilities.invokeLater(() -> {
							// Should send "..." back to server in order to connect to an agent/messenger.
							tfUserMsg.setText("..."); //$NON-NLS-1$
							btnSend.doClick();
						});
					}
				} else if(response != null) {
					SwingUtilities.invokeLater(() -> {
						appendErrorMessage(Messages.getString("InternalClientFrame.ERRPNL_ERROR_SENDING_MSG")); //$NON-NLS-1$
					});
				}
				SwingUtilities.invokeLater(() -> {
					pnlChat.revalidate();
					pnlChat.repaint();
				});
			}

			/**
			 *
			 * @param msg
			 */
			public void appendErrorMessage(String msg) {
				IMessagePanel pnl = new ErrorMessage();
				pnl.setMessage(msg, System.currentTimeMillis());
				pnlChat.add((JPanel)pnl);
			}
		});
		btnSend.setEnabled(false);
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(1)
					.addComponent(tfUserMsg, GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
					.addGap(3)
					.addComponent(btnSend)
					.addGap(1))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfUserMsg, GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
						.addComponent(btnSend, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addGap(1))
		);
		panel.setLayout(gl_panel);

		spChat = new JScrollPane();
		spChat.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		spChat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		contentPane.add(spChat, BorderLayout.CENTER);

		pnlChat = new JPanel();
		pnlChat.setBackground(UIManager.getColor("window")); //$NON-NLS-1$
		spChat.setViewportView(pnlChat);
		pnlChat.setLayout(new BoxLayout(pnlChat, BoxLayout.Y_AXIS));

	}
}
