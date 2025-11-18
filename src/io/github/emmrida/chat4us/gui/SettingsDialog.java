/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import io.github.emmrida.chat4us.controls.IdLabelListElement;
import io.github.emmrida.chat4us.controls.IdLabelListModel;
import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;
import io.github.emmrida.chat4us.util.Settings;

import javax.swing.JTabbedPane;
import javax.swing.ComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JSeparator;
import javax.swing.JComboBox;

/**
 * The settings dialog
 *
 * @author El Mhadder Mohamed Rida
 */
public class SettingsDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private boolean cancelled = true;
	private boolean needRestart = false;

	private final JPanel contentPanel = new JPanel();
	private JTextField tfAgentResponseTimeout;
	private JTextField tfAiServersTasks;
	private JButton cancelButton;
	private JTextField tfRecentFiles;
	private JTextField tfLogsTimeout;
	private JTextField tfChatsTimeout;
	private JTextField tfChatSessionsTimeout;
	private JTextField tfAiContextLines;
	private JList<IdLabelListElement> listGroups;
	private JList<IdLabelListElement> listPostes;
	private JTextField tfLogOnLongResponse;
	private JTextField tfAiMaxQuery;
	private JComboBox<String> cmbLanguages;
	private JCheckBox chkMinToIcon;
	private JCheckBox chkNotifyOnError;

	public boolean isCancelled() { return cancelled; }

	/**
	 * Checks if the value exists in the list model. The search is case insensitive.
	 * @param value The value to check
	 * @param listModel The list model to look in
	 * @return True if the value exists
	 */
	private boolean existsInList(String value, IdLabelListModel listModel) {
		value = value.toLowerCase();
		for(int i = 0; i < listModel.getSize(); i++)
			if(value.equals(listModel.getElementAt(i).getLabel().toLowerCase()))
				return true;
		return false;
	}

	/**
	 * Checks if the value is in use in the database. Useful to check if an ai group or an agent position is in use
	 * by chat bot, client or agent.
	 * @param con The database connection
	 * @param table The table name
	 * @param field The field name
	 * @param value The value to check
	 * @return True if the value is in use
	 */
	private boolean valueIsInUse(Connection con, String table, String field, int value) {
		List<Object[]> rows = Helper.dbQuery(con, "SELECT id FROM " + table + " WHERE " + field + " = " + value); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return rows.size() > 0;
	}

	/**
	 * Load settings and fills the dialog fields.
	 */
	private void loadSettings() {
		Settings settings = MainWindow.getSettings();
		tfRecentFiles.setText(String.valueOf(settings.getMaxRecentFiles()));
		tfLogsTimeout.setText(String.valueOf(settings.getLogsTimeoutDays()));
		tfChatsTimeout.setText(String.valueOf(settings.getChatsTimeoutDays()));
		tfChatSessionsTimeout.setText(String.valueOf(settings.getChatSessionsTimeoutMinutes()));
		tfAiContextLines.setText(String.valueOf(settings.getAiContextLines()));
		tfAiMaxQuery.setText(String.valueOf(settings.getAiQueryMaxLength()));
		tfAiServersTasks.setText(String.valueOf(settings.getAiServersTasks()));
		tfLogOnLongResponse.setText(String.valueOf(settings.getAiLogOnLongResponse()));
		tfAgentResponseTimeout.setText(String.valueOf(settings.getAgentResponseTimeoutSeconds()));
		//chkNsLookup.setSelected(settings.nsLookupOnLogin());
		chkMinToIcon.setSelected(settings.isMinimizeToTray());
		chkNotifyOnError.setSelected(settings.isNotifyOnErrors());

		Connection con = MainWindow.getDBConnection();
		loadAIGroups(con);
		loadAgentPositions(con);

		String[] langs = Messages.getString("SettingsDialog.SUPPORTED_LOCALES").split(";"); //$NON-NLS-1$ //$NON-NLS-2$
		ComboBoxModel<String> model = (ComboBoxModel<String>)cmbLanguages.getModel();
		cmbLanguages.setModel(model);
		for(int i = 0; i < langs.length; i++) {
			cmbLanguages.insertItemAt(langs[i], i);
			if(settings.getDefLocale().equals(langs[i].substring(0, 2)))
				cmbLanguages.setSelectedIndex(i);
		}
	}

	/**
	 *
	 * @param con
	 */
	private void loadAgentPositions(Connection con) {
		try(Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT id, name, removed FROM agents_postes;")) { //$NON-NLS-1$
			IdLabelListModel model = new IdLabelListModel();
			listPostes.setModel(model);
			while(rs.next())
				if(!rs.getBoolean(3)) // Not removed
					model.addElement(new IdLabelListElement(rs.getInt(1), rs.getString(2), true));
		} catch (SQLException ex) {
			Helper.logError(ex, Messages.getString("SettingsDialog.AGENTS_LOADING_ERROR"), true); //$NON-NLS-1$
		}
	}

	/**
	 *
	 * @param con
	 */
	private void loadAIGroups(Connection con) {
		try(Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT id, name, removed FROM ai_groups;")) { //$NON-NLS-1$
			IdLabelListModel model = new IdLabelListModel();
			listGroups.setModel(model);
			while(rs.next())
				if(!rs.getBoolean(3)) // Not removed
					model.addElement(new IdLabelListElement(rs.getInt(1), rs.getString(2), true));
		} catch (SQLException ex) {
			Helper.logError(ex, Messages.getString("SettingsDialog.AI_GROUPS_LOADING_ERROR"), true); //$NON-NLS-1$
		}
	}

	/**
	 * Create the dialog.
	 */
	public SettingsDialog(Frame parent) {
		super(parent, Messages.getString("SettingsDialog.DLG_TITLE"), true); //$NON-NLS-1$
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				loadSettings();
			}
		});
		SwingUtilities.invokeLater(() -> {
			if(!parent.isVisible() || parent.getState() == Frame.ICONIFIED)
				setLocationRelativeTo(null);
		});
		setResizable(false);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.setFocusable(false);
			contentPanel.add(tabbedPane, BorderLayout.CENTER);
			{
				JPanel panelApp = new JPanel();
				tabbedPane.addTab(Messages.getString("SettingsDialog.TAB_TITLE_MISC"), null, panelApp, null); //$NON-NLS-1$

				tfRecentFiles = new JTextField();
				tfRecentFiles.setText("5"); //$NON-NLS-1$
				tfRecentFiles.setHorizontalAlignment(SwingConstants.TRAILING);
				tfRecentFiles.setColumns(10);

				JLabel lblNewLabel_2_2 = new JLabel(Messages.getString("SettingsDialog.LBL_OPENED_RECENTLY")); //$NON-NLS-1$

				JLabel lblNewLabel_4 = new JLabel(Messages.getString("SettingsDialog.LBL_FILES")); //$NON-NLS-1$

				JLabel lblNewLabel_2_1_2 = new JLabel(Messages.getString("SettingsDialog.LBL_LOG_DURATION")); //$NON-NLS-1$

				tfLogsTimeout = new JTextField();
				tfLogsTimeout.setText("30"); //$NON-NLS-1$
				tfLogsTimeout.setHorizontalAlignment(SwingConstants.TRAILING);
				tfLogsTimeout.setColumns(10);

				JLabel lblNewLabel_4_1 = new JLabel(Messages.getString("SettingsDialog.LBL_DAYS")); //$NON-NLS-1$

				JLabel lblNewLabel_2_1_1 = new JLabel(Messages.getString("SettingsDialog.LBL_SAVED_CHATS_DURATION")); //$NON-NLS-1$

				tfChatsTimeout = new JTextField();
				tfChatsTimeout.setText("30"); //$NON-NLS-1$
				tfChatsTimeout.setHorizontalAlignment(SwingConstants.TRAILING);
				tfChatsTimeout.setColumns(10);

				JLabel lblNewLabel_4_1_1 = new JLabel(Messages.getString("SettingsDialog.LBL_DAYS")); //$NON-NLS-1$

				JSeparator separator = new JSeparator();

				cmbLanguages = new JComboBox<String>();
				cmbLanguages.addItemListener(e1 -> {
					needRestart = true;
				});


				JLabel lblNewLabel_12 = new JLabel(Messages.getString("SettingsDialog.LBL_LANGS")); //$NON-NLS-1$

				JLabel lblNewLabel_13 = new JLabel(Messages.getString("SettingsDialog.LBL_REQ_RESTART")); //$NON-NLS-1$

				JSeparator separator_1 = new JSeparator();

				chkMinToIcon = new JCheckBox(Messages.getString("SettingsDialog.CHK_MIN2ICON")); //$NON-NLS-1$

				chkNotifyOnError = new JCheckBox(Messages.getString("SettingsDialog.CHK_NOTIFY_ON_ERROR")); //$NON-NLS-1$
				GroupLayout gl_panelApp = new GroupLayout(panelApp);
				gl_panelApp.setHorizontalGroup(
					gl_panelApp.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelApp.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_panelApp.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblNewLabel_2_1_1)
								.addComponent(lblNewLabel_2_1_2)
								.addComponent(lblNewLabel_2_2)
								.addComponent(lblNewLabel_12))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panelApp.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panelApp.createSequentialGroup()
									.addComponent(chkNotifyOnError)
									.addContainerGap())
								.addGroup(gl_panelApp.createParallelGroup(Alignment.LEADING)
									.addGroup(gl_panelApp.createSequentialGroup()
										.addComponent(chkMinToIcon)
										.addContainerGap())
									.addGroup(gl_panelApp.createParallelGroup(Alignment.LEADING)
										.addGroup(Alignment.TRAILING, gl_panelApp.createSequentialGroup()
											.addComponent(separator_1, GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
											.addContainerGap())
										.addGroup(gl_panelApp.createSequentialGroup()
											.addComponent(tfChatsTimeout, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(lblNewLabel_4_1_1)
											.addContainerGap())
										.addGroup(gl_panelApp.createSequentialGroup()
											.addComponent(tfLogsTimeout, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(lblNewLabel_4_1)
											.addContainerGap())
										.addGroup(gl_panelApp.createSequentialGroup()
											.addComponent(tfRecentFiles, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(lblNewLabel_4)
											.addContainerGap())
										.addGroup(gl_panelApp.createParallelGroup(Alignment.LEADING)
											.addGroup(gl_panelApp.createSequentialGroup()
												.addComponent(cmbLanguages, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(lblNewLabel_13)
												.addGap(98))
											.addGroup(gl_panelApp.createSequentialGroup()
												.addComponent(separator, GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
												.addContainerGap()))))))
				);
				gl_panelApp.setVerticalGroup(
					gl_panelApp.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelApp.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_panelApp.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel_2_2)
								.addComponent(tfRecentFiles, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNewLabel_4))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panelApp.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel_2_1_2)
								.addComponent(tfLogsTimeout, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNewLabel_4_1))
							.addGap(6)
							.addGroup(gl_panelApp.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel_2_1_1)
								.addComponent(tfChatsTimeout, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNewLabel_4_1_1))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(separator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panelApp.createParallelGroup(Alignment.BASELINE)
								.addComponent(cmbLanguages, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNewLabel_12)
								.addComponent(lblNewLabel_13))
							.addGap(8)
							.addComponent(separator_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chkMinToIcon)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chkNotifyOnError)
							.addGap(39))
				);
				panelApp.setLayout(gl_panelApp);
			}
			{
				JPanel panelAI = new JPanel();
				tabbedPane.addTab(Messages.getString("SettingsDialog.TAB_TITLE_AI"), null, panelAI, null); //$NON-NLS-1$

				JLabel lblNewLabel_3 = new JLabel(Messages.getString("SettingsDialog.LBL_GROUPS")); //$NON-NLS-1$

				JScrollPane scrollPane_1 = new JScrollPane();

				JButton btnNewGroup = new JButton("+"); //$NON-NLS-1$
				btnNewGroup.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String group = JOptionPane.showInputDialog(SettingsDialog.this, Messages.getString("SettingsDialog.IDLG_GROUP_NAME_MSG"), Messages.getString("SettingsDialog.IDLG_GROUP_NAME_TITLE"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
						if(group != null) {
							group = group.trim();
							if(group.length() > 2) {
								IdLabelListModel model = (IdLabelListModel)listGroups.getModel();
								if(!existsInList(group, model)) {
									Connection con = MainWindow.getDBConnection();
									int n = Helper.dbUpdate(con, "INSERT INTO ai_groups (name, removed) VALUES('"+ group +"', 0);"); //$NON-NLS-1$ //$NON-NLS-2$
									if(n == 1) {
										loadAIGroups(con);
									} else Helper.logWarning(String.format(Messages.getString("SettingsDialog.GROUP_ADD_FAILURE"), group), true); //$NON-NLS-1$
								} else Helper.logWarning(String.format(Messages.getString("SettingsDialog.GROUP_ALREADY_EXISTS"), group), true); //$NON-NLS-1$
							} else Toolkit.getDefaultToolkit().beep();
						} else Toolkit.getDefaultToolkit().beep();
					}
				});

				JButton btnRemGroup = new JButton("-"); //$NON-NLS-1$
				btnRemGroup.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						IdLabelListModel model = (IdLabelListModel)listGroups.getModel();
						IdLabelListElement group = listGroups.getSelectedValue();
						if(group != null) {
							Connection con = MainWindow.getDBConnection();
							if(valueIsInUse(con, "chatbots", "ai_group_id", group.getId()) //$NON-NLS-1$ //$NON-NLS-2$
								|| valueIsInUse(con, "websites", "ai_group_id", group.getId()) //$NON-NLS-1$ //$NON-NLS-2$
									|| valueIsInUse(con, "agents", "ai_group_id", group.getId())) { //$NON-NLS-1$ //$NON-NLS-2$
								Helper.logWarning(Messages.getString("SettingsDialog.USED_GROUP_WARNING"), true); //$NON-NLS-1$
								return;
							}
							int ret = Helper.showConfirmDialog(SettingsDialog.this, Messages.getString("SettingsDialog.CDLG_REM_SEL_ENTRY_MSG") + group.getLabel(), Messages.getString("SettingsDialog.CDLG_REM_SEL_ENTRY_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
							if(ret != JOptionPane.YES_OPTION)
								return;
							int n = Helper.dbUpdate(con, "UPDATE ai_groups SET removed = 1 WHERE id = " + group.getId() + ";"); //$NON-NLS-1$ //$NON-NLS-2$
							if(n == 1) {
								loadAIGroups(con);
							} else Helper.logWarning(String.format(Messages.getString("SettingsDialog.GROUP_REMOVE_FAILURE"), group), true); //$NON-NLS-1$
						} else Toolkit.getDefaultToolkit().beep();
					}
				});

				tfAiContextLines = new JTextField();
				tfAiContextLines.setHorizontalAlignment(SwingConstants.TRAILING);
				tfAiContextLines.setText("25"); //$NON-NLS-1$
				tfAiContextLines.setColumns(10);

				JLabel lblNewLabel_6 = new JLabel(Messages.getString("SettingsDialog.LBL_CONTEXTE")); //$NON-NLS-1$

				JLabel lblNewLabel_7 = new JLabel(Messages.getString("SettingsDialog.LBL_LAST_LINES")); //$NON-NLS-1$

				tfLogOnLongResponse = new JTextField();
				tfLogOnLongResponse.setHorizontalAlignment(SwingConstants.TRAILING);
				tfLogOnLongResponse.setText("2"); //$NON-NLS-1$
				tfLogOnLongResponse.setColumns(10);

				JLabel lblNewLabel_8 = new JLabel(Messages.getString("SettingsDialog.LBL_DURATION")); //$NON-NLS-1$

				JLabel lblNewLabel_9 = new JLabel(Messages.getString("SettingsDialog.LBL_LOG_LONG_RESPONSES")); //$NON-NLS-1$

				tfAiMaxQuery = new JTextField();
				tfAiMaxQuery.setHorizontalAlignment(SwingConstants.TRAILING);
				tfAiMaxQuery.setText("1536"); //$NON-NLS-1$
				tfAiMaxQuery.setColumns(10);

				JLabel lblNewLabel_10 = new JLabel(Messages.getString("SettingsDialog.AIQ_MAX")); //$NON-NLS-1$

				JLabel lblNewLabel_11 = new JLabel(Messages.getString("SettingsDialog.AIQ_MAX_CHARS")); //$NON-NLS-1$
				GroupLayout gl_panelAI = new GroupLayout(panelAI);
				gl_panelAI.setHorizontalGroup(
					gl_panelAI.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelAI.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_panelAI.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblNewLabel_6)
								.addComponent(lblNewLabel_10)
								.addComponent(lblNewLabel_3)
								.addGroup(gl_panelAI.createSequentialGroup()
									.addGroup(gl_panelAI.createParallelGroup(Alignment.TRAILING)
										.addComponent(btnRemGroup, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
										.addComponent(lblNewLabel_8)
										.addComponent(btnNewGroup, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE))
									.addGap(0)))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panelAI.createParallelGroup(Alignment.TRAILING)
								.addGroup(Alignment.LEADING, gl_panelAI.createSequentialGroup()
									.addComponent(tfAiMaxQuery, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(lblNewLabel_11))
								.addGroup(Alignment.LEADING, gl_panelAI.createSequentialGroup()
									.addComponent(tfAiContextLines, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(lblNewLabel_7))
								.addGroup(Alignment.LEADING, gl_panelAI.createSequentialGroup()
									.addComponent(tfLogOnLongResponse, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(lblNewLabel_9))
								.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE))
							.addContainerGap())
				);
				gl_panelAI.setVerticalGroup(
					gl_panelAI.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelAI.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_panelAI.createParallelGroup(Alignment.BASELINE)
								.addComponent(tfAiContextLines, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNewLabel_6)
								.addComponent(lblNewLabel_7))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panelAI.createParallelGroup(Alignment.BASELINE)
								.addComponent(tfAiMaxQuery, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNewLabel_10)
								.addComponent(lblNewLabel_11))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panelAI.createParallelGroup(Alignment.BASELINE)
								.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_panelAI.createSequentialGroup()
									.addComponent(lblNewLabel_3)
									.addPreferredGap(ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
									.addComponent(btnNewGroup)
									.addGap(2)
									.addComponent(btnRemGroup)))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panelAI.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel_8)
								.addComponent(tfLogOnLongResponse, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNewLabel_9))
							.addContainerGap(6, Short.MAX_VALUE))
				);

				listGroups = new JList<>();
				listGroups.setModel(new IdLabelListModel());
				scrollPane_1.setViewportView(listGroups);
				panelAI.setLayout(gl_panelAI);
			}

			JPanel panelChatBots = new JPanel();
			tabbedPane.addTab(Messages.getString("SettingsDialog.TAB_TITLE_CHATBOTS"), null, panelChatBots, null); //$NON-NLS-1$

			tfAiServersTasks = new JTextField();
			tfAiServersTasks.setText("4"); //$NON-NLS-1$
			tfAiServersTasks.setColumns(10);
			tfAiServersTasks.setHorizontalAlignment(JTextField.TRAILING);
			tfAiServersTasks.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) { needRestart = true; }
				@Override
				public void removeUpdate(DocumentEvent e) { needRestart = true; }
				@Override
				public void changedUpdate(DocumentEvent e) { needRestart = true; }

			});

			JLabel lblNewLabel_2 = new JLabel(Messages.getString("SettingsDialog.LBL_THREADS_PER_SERVER")); //$NON-NLS-1$

			JLabel lblNewLabel_5 = new JLabel(Messages.getString("SettingsDialog.LBL_THREADS")); //$NON-NLS-1$
			lblNewLabel_5.setToolTipText(""); //$NON-NLS-1$

			JLabel lblNewLabel_5_1 = new JLabel(Messages.getString("SettingsDialog.LBL_SES_MINUTES")); //$NON-NLS-1$
			lblNewLabel_5_1.setToolTipText(""); //$NON-NLS-1$

			JLabel lblNewLabel_2_1 = new JLabel(Messages.getString("SettingsDialog.LBL_SES_DURATION")); //$NON-NLS-1$

			tfChatSessionsTimeout = new JTextField();
			tfChatSessionsTimeout.setText("20"); //$NON-NLS-1$
			tfChatSessionsTimeout.setHorizontalAlignment(SwingConstants.TRAILING);
			tfChatSessionsTimeout.setColumns(10);
			GroupLayout gl_panelChatBots = new GroupLayout(panelChatBots);
			gl_panelChatBots.setHorizontalGroup(
				gl_panelChatBots.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panelChatBots.createSequentialGroup()
						.addGap(10)
						.addGroup(gl_panelChatBots.createParallelGroup(Alignment.TRAILING)
							.addGroup(gl_panelChatBots.createSequentialGroup()
								.addComponent(lblNewLabel_2_1)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(tfChatSessionsTimeout, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE))
							.addGroup(gl_panelChatBots.createSequentialGroup()
								.addComponent(lblNewLabel_2)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(tfAiServersTasks, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panelChatBots.createParallelGroup(Alignment.LEADING)
							.addComponent(lblNewLabel_5)
							.addComponent(lblNewLabel_5_1)))
			);
			gl_panelChatBots.setVerticalGroup(
				gl_panelChatBots.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panelChatBots.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panelChatBots.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblNewLabel_5)
							.addComponent(tfAiServersTasks, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblNewLabel_2))
						.addGroup(gl_panelChatBots.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_panelChatBots.createSequentialGroup()
								.addGap(9)
								.addComponent(lblNewLabel_5_1))
							.addGroup(gl_panelChatBots.createSequentialGroup()
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_panelChatBots.createParallelGroup(Alignment.BASELINE)
									.addComponent(tfChatSessionsTimeout, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(lblNewLabel_2_1))))
						.addContainerGap(157, Short.MAX_VALUE))
			);
			panelChatBots.setLayout(gl_panelChatBots);

			JPanel panelAgents = new JPanel();
			tabbedPane.addTab(Messages.getString("SettingsDialog.TAB_TITLE_AGENTS"), null, panelAgents, null); //$NON-NLS-1$

			JButton btnNewPoste = new JButton("+"); //$NON-NLS-1$
			btnNewPoste.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String poste = JOptionPane.showInputDialog(SettingsDialog.this, Messages.getString("SettingsDialog.IDLG_POSTE_NAME_MSG"), Messages.getString("SettingsDialog.IDLG_POSTE_NAME_TITLE"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					if(poste != null) {
						poste = poste.trim();
						if(poste.length() > 2) {
							IdLabelListModel model = (IdLabelListModel)listPostes.getModel();
							if(!existsInList(poste, model)) {
								Connection con = MainWindow.getDBConnection();
								int n = Helper.dbUpdate(con, "INSERT INTO agents_postes (name, removed) VALUES('"+ poste +"', 0);"); //$NON-NLS-1$ //$NON-NLS-2$
								if(n == 1) {
									loadAgentPositions(con);
								} else Helper.logWarning(String.format(Messages.getString("SettingsDialog.POSTE_ADD_FAILURE"), poste), true); //$NON-NLS-1$
							} else Helper.logWarning(String.format(Messages.getString("SettingsDialog.POSTE_ALREADY_EXISTS"), poste), true); //$NON-NLS-1$
						} else Toolkit.getDefaultToolkit().beep();
					} else Toolkit.getDefaultToolkit().beep();
				}
			});

			JButton btnRemPoste = new JButton("-"); //$NON-NLS-1$
			btnRemPoste.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					IdLabelListElement poste = listPostes.getSelectedValue();
					if(poste != null) {
						Connection con = MainWindow.getDBConnection();
						if(valueIsInUse(con, "agents", "poste_id", poste.getId())) { //$NON-NLS-1$ //$NON-NLS-2$
							Helper.logWarning(Messages.getString("SettingsDialog.USED_POSITION_WARNING"), true); //$NON-NLS-1$
							return;
						}
						int ret = Helper.showConfirmDialog(SettingsDialog.this, Messages.getString("SettingsDialog.CDLG_REM_SEL_ENTRY_MSG") + poste.getLabel(), Messages.getString("SettingsDialog.CDLG_REM_SEL_ENTRY_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
						if(ret != JOptionPane.YES_OPTION)
							return;
						IdLabelListModel model = (IdLabelListModel)listPostes.getModel();
						int n = Helper.dbUpdate(con, "UPDATE agents_postes SET removed = 1 WHERE id = "+ poste.getId() +";"); //$NON-NLS-1$ //$NON-NLS-2$
						if(n == 1) {
							loadAgentPositions(con);
						} else Helper.logWarning(String.format(Messages.getString("SettingsDialog.POSTE_REMOVE_FAILURE"), poste), true); //$NON-NLS-1$
					} else Toolkit.getDefaultToolkit().beep();
				}
			});

			JScrollPane scrollPane_1 = new JScrollPane();

			JLabel lblNewLabel_3 = new JLabel(Messages.getString("SettingsDialog.LBL_POSTES")); //$NON-NLS-1$

			tfAgentResponseTimeout = new JTextField();
			tfAgentResponseTimeout.setText("120"); //$NON-NLS-1$
			tfAgentResponseTimeout.setColumns(10);

			JLabel lblNewLabel = new JLabel(Messages.getString("SettingsDialog.LBL_AGENT_MAX_RESPONSE")); //$NON-NLS-1$

			JLabel lblNewLabel_1 = new JLabel(Messages.getString("SettingsDialog.LBL_AGENT_MAX_RESP_SECONDS")); //$NON-NLS-1$
			GroupLayout gl_panelAgents = new GroupLayout(panelAgents);
			gl_panelAgents.setHorizontalGroup(
				gl_panelAgents.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panelAgents.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panelAgents.createParallelGroup(Alignment.TRAILING)
							.addComponent(lblNewLabel)
							.addGroup(gl_panelAgents.createParallelGroup(Alignment.LEADING)
								.addComponent(btnNewPoste, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnRemPoste, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE))
							.addComponent(lblNewLabel_3))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panelAgents.createParallelGroup(Alignment.LEADING)
							.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
							.addGroup(gl_panelAgents.createSequentialGroup()
								.addComponent(tfAgentResponseTimeout, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(lblNewLabel_1)))
						.addContainerGap())
			);
			gl_panelAgents.setVerticalGroup(
				gl_panelAgents.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panelAgents.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panelAgents.createParallelGroup(Alignment.BASELINE)
							.addComponent(tfAgentResponseTimeout, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblNewLabel)
							.addComponent(lblNewLabel_1))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panelAgents.createParallelGroup(Alignment.TRAILING)
							.addGroup(gl_panelAgents.createParallelGroup(Alignment.BASELINE)
								.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNewLabel_3))
							.addGroup(gl_panelAgents.createSequentialGroup()
								.addComponent(btnNewPoste)
								.addGap(1)
								.addComponent(btnRemPoste)))
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
			);

			listPostes = new JList<>();
			listPostes.setModel(new IdLabelListModel());
			scrollPane_1.setViewportView(listPostes);
			panelAgents.setLayout(gl_panelAgents);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.TRAILING));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(Messages.getString("SettingsDialog.BTN_SAVE")); //$NON-NLS-1$
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							int maxRecentFiles = Integer.parseInt(tfRecentFiles.getText().trim());
							if(maxRecentFiles < 5) throw new InvalidParameterException(Messages.getString("SettingsDialog.EX_RECENT_FILES")); //$NON-NLS-1$
							int logsTimeoutDays = Integer.parseInt(tfLogsTimeout.getText().trim());
							if(logsTimeoutDays < 1) throw new InvalidParameterException(Messages.getString("SettingsDialog.EX_LOG_FILES_MIN_DAYS")); //$NON-NLS-1$
							int chatsTimeoutDays = Integer.parseInt(tfChatsTimeout.getText().trim());
							if(chatsTimeoutDays < 1) throw new InvalidParameterException(Messages.getString("SettingsDialog.EX_SAVED_CHATS_MIN_DAYS")); //$NON-NLS-1$
							int aiContextLines = Integer.parseInt(tfAiContextLines.getText().trim());
							if(aiContextLines < 0) throw new InvalidParameterException(Messages.getString("SettingsDialog.EX_CONTEXTE_LINES_COUNT")); //$NON-NLS-1$
							int aiMaxQuery = Integer.parseInt(tfAiMaxQuery.getText().trim());
							if(aiMaxQuery < 1024) throw new InvalidParameterException(Messages.getString("SettingsDialog.EX_AIQ_MAX_LENGTH")); //$NON-NLS-1$
							int aiServersTasks = Integer.parseInt(tfAiServersTasks.getText().trim());
							if(aiServersTasks < 1) throw new InvalidParameterException(Messages.getString("SettingsDialog.EX_MIN_THREADS_PER_SERVER")); //$NON-NLS-1$
							int aiLogWhenTooLong = Integer.parseInt(tfLogOnLongResponse.getText().trim());
							if(aiLogWhenTooLong < 1) throw new InvalidParameterException(Messages.getString("SettingsDialog.EX_MIN_LOG_LONG_RESPONSE")); //$NON-NLS-1$
							int chatSessionsTimeoutMinutes = Integer.parseInt(tfChatSessionsTimeout.getText().trim());
							if(chatSessionsTimeoutMinutes < 1) throw new InvalidParameterException(Messages.getString("SettingsDialog.EX_MIN_CHAT_SES_TIMOUT")); //$NON-NLS-1$
							int agentResponseTimeoutSeconds = Integer.parseInt(tfAgentResponseTimeout.getText().trim());
							if(agentResponseTimeoutSeconds < 60) throw new InvalidParameterException(Messages.getString("SettingsDialog.EX_MAX_AGENT_RESPONSE")); //$NON-NLS-1$
							if(cmbLanguages.getSelectedIndex() < 0) throw new InvalidParameterException(Messages.getString("SettingsDialog.EX_SEL_LANGUAGE")); //$NON-NLS-1$
							//boolean nsLookupOnLogin = chkNsLookup.isSelected();
							boolean notifyOnError = chkNotifyOnError.isSelected();
							boolean minToTray = chkMinToIcon.isSelected();

							Settings settings = MainWindow.getSettings();
							settings.setDefLocale(cmbLanguages.getSelectedItem().toString().substring(0, 2));
							settings.setMaxRecentFiles(maxRecentFiles);
							settings.setLogsTimeoutDays(logsTimeoutDays);
							settings.setChatsTimeoutDays(chatsTimeoutDays);
							settings.setAiContextLines(aiContextLines);
							settings.setAiQueryMaxLength(aiMaxQuery);
							settings.setAiServersTasks(aiServersTasks);
							settings.setAiLogOnLongResponse(aiLogWhenTooLong);
							settings.setChatSessionsTimeoutMinutes(chatSessionsTimeoutMinutes);
							settings.setAgentResponseTimeoutSeconds(agentResponseTimeoutSeconds);
							//settings.setNsLookupOnLogin(nsLookupOnLogin);
							settings.setNotifyOnErrors(notifyOnError);
							settings.setMinimizeToTray(minToTray);
							settings.save();
							if(needRestart)
								JOptionPane.showMessageDialog(SettingsDialog.this, Messages.getString("SettingsDialog.MB_SETTINGS_NEED_RESTART"), "Information", JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
							cancelled = false;
							setVisible(false);
						} catch (Exception ex) {
							Helper.logError(ex, Messages.getString("SettingsDialog.LOG_SETTINGS_SAVE_FAILURE"), true); //$NON-NLS-1$
						}
					}
				});
				okButton.setActionCommand("OK"); //$NON-NLS-1$
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton(Messages.getString("SettingsDialog.BTN_CANCEL")); //$NON-NLS-1$
				cancelButton.setActionCommand("Cancel"); //$NON-NLS-1$
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelled = true;
						setVisible(false);
					}
				});
				buttonPane.add(cancelButton);
			}
		}
		Helper.registerCancelByEsc(this, cancelButton);
		Helper.enableRtlWhenNeeded(this);
		pack();
	}
}
