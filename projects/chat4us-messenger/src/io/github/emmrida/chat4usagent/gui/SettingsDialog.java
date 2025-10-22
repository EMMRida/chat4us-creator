/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import io.github.emmrida.chat4usagent.util.Helper;
import io.github.emmrida.chat4usagent.util.Messages;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Settings dialog
 *
 * @author El Mhadder Mohamed Rida
 */
public class SettingsDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private boolean cancelled = false;
	private boolean openModels = false;
	private Settings settings = null;
	private List<String> models = new ArrayList<>();

	private final JPanel contentPanel = new JPanel();
	private JCheckBox chkNotifSound;
	private JCheckBox chkNotifSys;
	private JList<String> lstWhiteList;
	private JList<String> lstModels;
	private JTextField tfHost;
	private JTextField tfPort;
	private JCheckBox chkAutoConnect;
	private JButton btnModelAdd;
	private JTabbedPane tabbedPane;
	private JCheckBox chkBringToFront;
	private JCheckBox chkIconMinimize;

	public boolean isCancelled() { return cancelled; }
	public Settings getSettings() { return settings; }

	/**
	 * Create the dialog.
	 * @param openModels Open models dialog if true
	 */
	public SettingsDialog(Frame parent, boolean openModels) {
		this(parent);
		this.openModels = openModels;
	}

	/**
	 * Create the dialog.
	 * @wbp.parser.constructor
	 */
	public SettingsDialog(Frame parent) {
		super(parent, Messages.getString("SettingsDialog.SETTINGSDLG_TITLE"), true); //$NON-NLS-1$
		SwingUtilities.invokeLater(() -> {
			if(!parent.isVisible() || parent.getState() == Frame.ICONIFIED) {
				setLocationRelativeTo(null);
			} else setLocationRelativeTo(parent);
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				settings = SettingsDialog.Settings.load();
				if(settings != null) {
					tfHost.setText(settings.host);
					tfPort.setText(String.valueOf(settings.port));
					chkNotifSound.setSelected(settings.notifSound);
					chkNotifSys.setSelected(settings.notifSys);
					chkAutoConnect.setSelected(settings.autoConnect);
					chkBringToFront.setSelected(settings.bringToFront);
					chkIconMinimize.setSelected(settings.iconMinimize);
					DefaultListModel<String> model = new DefaultListModel<String>();
					lstWhiteList.setModel(model);
					for(Object ip : settings.ipWhiteList.toArray())
						model.addElement((String)ip);
					model = new DefaultListModel<String>();
					lstModels.setModel(model);
					for(int i = 0; i < settings.modelsNames.size(); i++) {
						model.addElement(settings.modelsNames.get(i));
						models.add(settings.modelsContents.get(i));
					}
				}
				if(openModels) {
					tabbedPane.setSelectedIndex(2);
					btnModelAdd.doClick();
				}
			}
		});
		setResizable(false);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setFocusable(false);
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
		);

		JPanel panelSettings = new JPanel();
		tabbedPane.addTab(Messages.getString("SettingsDialog.SETTINGS_TAB_TITLE"), null, panelSettings, null); //$NON-NLS-1$

		chkNotifSound = new JCheckBox(Messages.getString("SettingsDialog.CHK_AUDIO_NOTIF")); //$NON-NLS-1$

		chkNotifSys = new JCheckBox(Messages.getString("SettingsDialog.CHK_SYS_NOTIF")); //$NON-NLS-1$

		chkAutoConnect = new JCheckBox(Messages.getString("SettingsDialog.CHK_AUTO_CONNECT")); //$NON-NLS-1$

		chkBringToFront = new JCheckBox(Messages.getString("SettingsDialog.CHK_BRING2FRONT")); //$NON-NLS-1$ //$NON-NLS-1$

		chkIconMinimize = new JCheckBox(Messages.getString("SettingsDialog.CHK_MINIMIZE2ICON")); //$NON-NLS-1$
		GroupLayout gl_panelSettings = new GroupLayout(panelSettings);
		gl_panelSettings.setHorizontalGroup(
			gl_panelSettings.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSettings.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelSettings.createParallelGroup(Alignment.LEADING)
						.addComponent(chkNotifSound)
						.addComponent(chkNotifSys)
						.addComponent(chkAutoConnect)
						.addComponent(chkBringToFront)
						.addComponent(chkIconMinimize))
					.addContainerGap(162, Short.MAX_VALUE))
		);
		gl_panelSettings.setVerticalGroup(
			gl_panelSettings.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSettings.createSequentialGroup()
					.addContainerGap()
					.addComponent(chkNotifSound)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chkNotifSys)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chkAutoConnect)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chkBringToFront)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chkIconMinimize)
					.addContainerGap(68, Short.MAX_VALUE))
		);
		panelSettings.setLayout(gl_panelSettings);

		JPanel panelServer = new JPanel();
		tabbedPane.addTab(Messages.getString("SettingsDialog.SERVER_TAB_TITLE"), null, panelServer, null); //$NON-NLS-1$

		tfHost = new JTextField();
		tfHost.setColumns(10);

		JLabel lblNewLabel = new JLabel(Messages.getString("SettingsDialog.LBL_HOST")); //$NON-NLS-1$

		tfPort = new JTextField();
		tfPort.setColumns(10);

		JLabel lblNewLabel_1 = new JLabel(Messages.getString("SettingsDialog.LBL_PORT")); //$NON-NLS-1$

		JScrollPane scrollPane = new JScrollPane();

		JLabel lblNewLabel_2 = new JLabel(Messages.getString("SettingsDialog.LBL_WHITELIST")); //$NON-NLS-1$

		JButton btnWBRem = new JButton("-"); //$NON-NLS-1$
		btnWBRem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = lstWhiteList.getSelectedIndex();
				if(index != -1) {
					if(JOptionPane.YES_OPTION != Helper.showConfirmDialog(SettingsDialog.this, Messages.getString("SettingsDialog.MB_REMOVE_HOST"), Messages.getString("SettingsDialog.MB_REMOVE_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 1)) //$NON-NLS-1$ //$NON-NLS-2$
						return;
					DefaultListModel<String> model = (DefaultListModel<String>)lstWhiteList.getModel();
					model.remove(index);
				} else JOptionPane.showMessageDialog(SettingsDialog.this, Messages.getString("SettingsDialog.MSGBOX_NO_SEL_IP"), Messages.getString("SettingsDialog.MSGBOX_NOIP_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		JButton btnWBAdd = new JButton("+"); //$NON-NLS-1$
		btnWBAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String ip = JOptionPane.showInputDialog(SettingsDialog.this, Messages.getString("SettingsDialog.INPUTBOX_WHITELIST_MSG"), Messages.getString("SettingsDialog.INPUTBOX_WHITELIST_TITLE"), JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
				if(Helper.isValidIP(ip)) {
					DefaultListModel<String> model = (DefaultListModel<String>)lstWhiteList.getModel();
					if(!model.contains(ip)) {
						model.addElement(ip);
					} else Toolkit.getDefaultToolkit().beep();
				} else JOptionPane.showMessageDialog(SettingsDialog.this, Messages.getString("SettingsDialog.MSGBOX_WRONG_IP_MSG"), Messages.getString("SettingsDialog.MSGBOX_WRONG_IP_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
		GroupLayout gl_panelServer = new GroupLayout(panelServer);
		gl_panelServer.setHorizontalGroup(
			gl_panelServer.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panelServer.createSequentialGroup()
					.addGap(10)
					.addGroup(gl_panelServer.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_panelServer.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblNewLabel_2))
						.addComponent(lblNewLabel_1)
						.addComponent(lblNewLabel)
						.addComponent(btnWBRem, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnWBAdd, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE))
					.addGap(3)
					.addGroup(gl_panelServer.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
						.addComponent(tfPort, GroupLayout.PREFERRED_SIZE, 146, GroupLayout.PREFERRED_SIZE)
						.addComponent(tfHost, GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panelServer.setVerticalGroup(
			gl_panelServer.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelServer.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelServer.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfHost, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelServer.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_1))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelServer.createParallelGroup(Alignment.BASELINE)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_panelServer.createSequentialGroup()
							.addComponent(lblNewLabel_2)
							.addPreferredGap(ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
							.addComponent(btnWBAdd)
							.addGap(1)
							.addComponent(btnWBRem)))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);

		lstWhiteList = new JList<>();
		scrollPane.setViewportView(lstWhiteList);
		panelServer.setLayout(gl_panelServer);

		JPanel panelModels = new JPanel();
		tabbedPane.addTab(Messages.getString("SettingsDialog.TAB_MODELS_TITLE"), null, panelModels, null); //$NON-NLS-1$

		JLabel lblNewLabel_2_1 = new JLabel(Messages.getString("SettingsDialog.LBL_RESPONSE_MODELS")); //$NON-NLS-1$

		JButton btnModelRem = new JButton("-"); //$NON-NLS-1$
		btnModelRem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = lstModels.getSelectedIndex();
				if(index != -1) {
					int ret = JOptionPane.showConfirmDialog(SettingsDialog.this, Messages.getString("SettingsDialog.MSGBOX_MODEL_DEL_MSG"), Messages.getString("SettingsDialog.MSGBOX_CONFIRM"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					if(ret == JOptionPane.YES_OPTION) {
						DefaultListModel<String> model = (DefaultListModel<String>)lstModels.getModel();
						model.remove(index);
					}
				} else Toolkit.getDefaultToolkit().beep();
			}
		});
		btnModelRem.setFocusable(false);

		btnModelAdd = new JButton("+"); //$NON-NLS-1$
		btnModelAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ModelsDialog dlg = new ModelsDialog(MainWindow.getFrame(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$
				dlg.setLocationRelativeTo(SettingsDialog.this);
				dlg.setVisible(true);
				if(!dlg.isCancelled()) {
					DefaultListModel<String> model = (DefaultListModel<String>)lstModels.getModel();
					model.addElement(dlg.getTitle());
					models.add(dlg.getContent());
				}
			}
		});
		btnModelAdd.setFocusable(false);

		JScrollPane scrollPane_1 = new JScrollPane();
		GroupLayout gl_panelModels = new GroupLayout(panelModels);
		gl_panelModels.setHorizontalGroup(
			gl_panelModels.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelModels.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelModels.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblNewLabel_2_1)
						.addComponent(btnModelRem, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnModelAdd, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE))
					.addGap(3)
					.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 284, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		gl_panelModels.setVerticalGroup(
			gl_panelModels.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panelModels.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelModels.createParallelGroup(Alignment.TRAILING)
						.addComponent(scrollPane_1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
						.addGroup(gl_panelModels.createSequentialGroup()
							.addComponent(lblNewLabel_2_1)
							.addPreferredGap(ComponentPlacement.RELATED, 102, Short.MAX_VALUE)
							.addComponent(btnModelAdd)
							.addGap(1)
							.addComponent(btnModelRem)))
					.addContainerGap())
		);

		lstModels = new JList<>();
		lstModels.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					ModelsDialog dlg = new ModelsDialog(MainWindow.getFrame(), lstModels.getSelectedValue().toString(), models.get(lstModels.getSelectedIndex()));
					dlg.setLocationRelativeTo(SettingsDialog.this);
					dlg.setVisible(true);
					if(!dlg.isCancelled()) {
						DefaultListModel<String> model = (DefaultListModel<String>)lstModels.getModel();
						model.setElementAt(dlg.getTitle(), lstModels.getSelectedIndex());
						models.set(lstModels.getSelectedIndex(), dlg.getContent());
					}
				}
			}
		});
		scrollPane_1.setViewportView(lstModels);
		panelModels.setLayout(gl_panelModels);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.TRAILING));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(Messages.getString("SettingsDialog.BTN_SAVE")); //$NON-NLS-1$
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int port;
						try {
							port = Integer.parseUnsignedInt(tfPort.getText());
						} catch(NumberFormatException ex) {
							Helper.logError(ex, Messages.getString("SettingsDialog.LOG_WRONG_PORT"), true); //$NON-NLS-1$
							tfPort.requestFocus();
							return;
						}
						boolean needsRestart = false;
						settings = MainWindow.getSettings();
						settings.notifSound = chkNotifSound.isSelected();
						settings.notifSys = chkNotifSys.isSelected();
						settings.autoConnect = chkAutoConnect.isSelected();
						settings.bringToFront = chkBringToFront.isSelected();
						settings.iconMinimize = chkIconMinimize.isSelected();
						needsRestart |= !tfHost.getText().equals(settings.host);
						settings.host = tfHost.getText();
						needsRestart |= port != settings.port;
						settings.port = port;
						settings.ipWhiteList = new ArrayList<String>();
						for(int i = 0; i < lstWhiteList.getModel().getSize(); i++)
							settings.ipWhiteList.add(lstWhiteList.getModel().getElementAt(i).toString().trim());
						settings.modelsNames = new ArrayList<String>();
						settings.modelsContents = new ArrayList<String>();
						for(int i = 0; i < lstModels.getModel().getSize(); i++) {
							settings.modelsNames.add(lstModels.getModel().getElementAt(i).toString().trim());
							settings.modelsContents.add(models.get(i).trim());
						}
						if(settings.save()) {
							if(needsRestart)
								Helper.logWarning(Messages.getString("SettingsDialog.LOG_APP_NEEDS_RESTART"), true); //$NON-NLS-1$
							cancelled = false;
							setVisible(false);
						} else Helper.logError(Messages.getString("SettingsDialog.MSGBOX_SAVE_ERROR"), true); //$NON-NLS-1$
					}
				});
				okButton.setActionCommand("OK"); //$NON-NLS-1$
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton(Messages.getString("SettingsDialog.BTN_CANCEL")); //$NON-NLS-1$
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelled = true;
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel"); //$NON-NLS-1$
				buttonPane.add(cancelButton);
				Helper.registerCancelByEsc(this, cancelButton);
			}
		}
		Helper.enableRtlWhenNeeded(this);
		pack();
	}

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	/**
	 * The Settings class
	 */
	public static class Settings {
		public static final String SETTINGS_FILE = "./settings.cfg"; //$NON-NLS-1$
		private boolean notifSound;
		private boolean notifSys;
		private boolean autoConnect;
		private boolean bringToFront;
		private boolean iconMinimize;
		private String host;
		private int port;
		private List<String> ipWhiteList;
		private List<String> modelsNames;
		private List<String> modelsContents;

		private String css;

		/**
		 * Init settings instance
		 */
		public Settings() {
			notifSound = false;
			notifSys = false;
			host = ""; //$NON-NLS-1$
			port = 0;
			autoConnect = true;
			css = ""; //$NON-NLS-1$
			ipWhiteList = new ArrayList<>();
			modelsNames = new ArrayList<>();
			modelsContents = new ArrayList<>();
		}

		/**
		 * Load settings from the settings.cfg file
		 * @return Settings instance.
		 */
		public static Settings load() {
			Settings settings = null;
			try(BufferedReader br = new BufferedReader(new FileReader(Settings.SETTINGS_FILE))) {
				String line;
				settings = new Settings();
				while ((line = br.readLine()) != null) {
					if (line.startsWith("notifSound=")) settings.setNotifSound(Boolean.parseBoolean(line.substring(11).trim())); //$NON-NLS-1$
					else if (line.startsWith("notifSys=")) settings.setNotifSys(Boolean.parseBoolean(line.substring(9).trim())); //$NON-NLS-1$
					else if (line.startsWith("host=")) settings.setHost(line.substring(5).trim()); //$NON-NLS-1$
					else if (line.startsWith("port=")) settings.setPort(Integer.parseInt(line.substring(5).trim())); //$NON-NLS-1$
					else if (line.startsWith("autoConnect=")) settings.setAutoConnect(Boolean.parseBoolean(line.substring(12).trim())); //$NON-NLS-1$
					else if (line.startsWith("bringToFront=")) settings.setBringToFront(Boolean.parseBoolean(line.substring(13).trim())); //$NON-NLS-1$
					else if (line.startsWith("iconMinimize=")) settings.setIconMinimize(Boolean.parseBoolean(line.substring(13).trim())); //$NON-NLS-1$
					else if (line.startsWith("css=")) settings.css = new String(Base64.getDecoder().decode(line.substring(4).trim())); //$NON-NLS-1$
					else if (line.startsWith("ipWhiteList=")) { //$NON-NLS-1$
						String[] items = line.substring(12).trim().split(";"); //$NON-NLS-1$
						for (int i = 0; i < items.length; i++)
							if(Helper.isValidIP(items[i]))
								settings.addIpWhiteList(items[i]);
					}
					else if (line.startsWith("models=")) { //$NON-NLS-1$
						String[] items = line.substring(7).trim().split(";"); //$NON-NLS-1$
						for (int i = 0; i < items.length; i++) {
							String[] item = items[i].split(":", 2); //$NON-NLS-1$
							if(item.length == 2)
								settings.addModel(item[0], new String(Base64.getDecoder().decode(item[1])));
						}
					} else throw new Exception(Messages.getString("SettingsDialog.EX_INVALID_LINE") + line); //$NON-NLS-1$
				}
				return settings;
			} catch (Exception ex) {
				Helper.logError(ex, Messages.getString("SettingsDialog.LOG_ERROR_LOAD_PARAMS"), true); //$NON-NLS-1$
				return null;
			}
		}

		/**
		 * Save settings into the settings.cfg file
		 * @return true if success
		 */
		public boolean save() {
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(Settings.SETTINGS_FILE))) {
				bw.write("notifSound=" + notifSound + System.lineSeparator()); //$NON-NLS-1$
				bw.write("notifSys=" + notifSys + System.lineSeparator()); //$NON-NLS-1$
				bw.write("host=" + host + System.lineSeparator()); //$NON-NLS-1$
				bw.write("port=" + port + System.lineSeparator()); //$NON-NLS-1$
				bw.write("autoConnect=" + autoConnect + System.lineSeparator()); //$NON-NLS-1$
				bw.write("bringToFront=" + bringToFront + System.lineSeparator()); //$NON-NLS-1$
				bw.write("iconMinimize=" + iconMinimize + System.lineSeparator()); //$NON-NLS-1$
				bw.write("css=" + new String(Base64.getEncoder().encode(css.getBytes())) + System.lineSeparator()); //$NON-NLS-1$
				bw.write("ipWhiteList="); //$NON-NLS-1$
				for(String ip : ipWhiteList)
					bw.write(ip);
				bw.write(System.lineSeparator() + "models="); //$NON-NLS-1$
				String models = ""; //$NON-NLS-1$
				for(int i = 0; i < modelsNames.size(); i++)
					models += modelsNames.get(i) + ":" + new String(Base64.getEncoder().encode(modelsContents.get(i).getBytes())) + ";"; //$NON-NLS-1$ //$NON-NLS-2$
				bw.write(models + System.lineSeparator());
				bw.flush();
				return true;
			} catch (Exception ex) {
				Helper.logError(ex, Messages.getString("SettingsDialog.LOG_ERROR_SAVE_PARAMS"), true); //$NON-NLS-1$
				return false;
			}
		}

		/**
		 * Sets the notif sound state
		 * @param notifSound App plays notifications sound if true
		 */
		public void setNotifSound(boolean notifSound) { this.notifSound = notifSound; }

		/**
		 * Sets the notif system state
		 * @param notifSys App plays system notifications if true
		 */
		public void setNotifSys(boolean notifSys) { this.notifSys = notifSys; }

		/**
		 * Set server host
		 * @param host Server host
		 */
		public void setHost(String host) { this.host = host; }

		/**
		 * Set server port
		 * @param port Server port
		 */
		public void setPort(int port) { this.port = port; }

		/**
		 * Set auto connect state at app start.
		 * @param value True to auto connect at app start
		 */
		public void setAutoConnect(boolean value) { autoConnect = value; }

		/**
		 * Add ip to white list. App will refuse connections from IPs not in the white list
		 * @param ip IP to add to white list
		 */
		public void addIpWhiteList(String ip) { ipWhiteList.add(ip); }

		/**
		 * Set white list
		 * @param ipWhiteList List of IPs to add to white list
		 */
		public void setIpWhiteList(List<String> ipWhiteList) { this.ipWhiteList = ipWhiteList; }

		/**
		 * Add model to list
		 * @param name Model name
		 * @param content Model content
		 */
		public void addModel(String name, String content) { modelsNames.add(name); modelsContents.add(content); }

		/**
		 * Set models names
		 * @param modelsNames List of models names
		 */
		public void setModelsNames(List<String> modelsNames) { this.modelsNames = modelsNames; }

		/**
		 * Set models contents
		 * @param modelsContents List of models contents
		 */
		public void setModelsContents(List<String> modelsContents) { this.modelsContents = modelsContents; }

		/**
		 * Get notif sound state
		 * @return True if app plays notifications sound
		 */
		public boolean isNotifSound() { return notifSound; }

		/**
		 * Get notif system state
		 * @return True if app plays system notifications
		 */
		public boolean isNotifSys() { return notifSys; }

		/**
		 * Get server host
		 * @return Server host
		 */
		public String getHost() { return host; }

		/**
		 * Get server port
		 * @return Server port
		 */
		public int getPort() { return port; }

		/**
		 * Get auto connect state
		 * @return True if auto connect at app start
		 */
		public boolean isAutoConnect() { return autoConnect; }

		/**
		 * Get white list
		 * @return White list of IPs allowed to connect to the messenger app
		 */
		public List<String> getIpWhiteList() { return ipWhiteList; }

		/**
		 * Get models names
		 * @return List of models names
		 */
		public List<String> getModelsNmes() { return modelsNames; }

		/**
		 * Get models contents
		 * @return List of models contents
		 */
		public List<String> getModelsContents() { return modelsContents; }

		/**
		 * Get css
		 * @return Css
		 */
		public String getCss() { return css; }

		/**
		 * Set css
		 * @param css Css
		 */
		public void setCss(String css) { this.css = css; }

		/**
		 * Get Bring to front on incoming messages.
		 * @return True if the app will bring to front on incoming messages
		 */
		public boolean getBringToFront() { return bringToFront; }

		/**
		 * Set Bring to front on incoming messages.
		 * @param value True if the app will bring to front on incoming messages
		 */
		public void setBringToFront(boolean value) { bringToFront = value; }

		/**
		 * Get minimize to tray on minimize
		 * @return True if the app will minimize to tray on minimize
		 */
		public boolean getIconMinimize() { return iconMinimize; }

		/**
		 * Set minimize to tray on minimize
		 * @param value True if the app will minimize to tray on minimize
		 */
		public void setIconMinimize(boolean value) { iconMinimize = value; }
	}
}













