/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.ria;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import io.github.emmrida.chat4us.gui.MainWindow;
import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;
import io.github.emmrida.chat4us.util.Settings;

import javax.swing.ScrollPaneConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dimension;
import javax.swing.JTabbedPane;

/**
 * The Class ChatBotSettingsDialog. Manages the chat bot settings dialog for every RIA file.
 *
 * @author El Mhadder Mohamed Rida
 */
public class ChatBotSettingsDialog extends JDialog {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The content panel. */
	private final JPanel contentPanel = new JPanel();

	/** The guidelines text area. */
	private JTextArea taGuidelines;

	/** The text field name. */
	private JTextField tfName;

	/** The table params. */
	private JTable tblParams;

	/** Set to true if the data has been modified. */
	public boolean isModified = false;

	/** The bot name. */
	public String botName;

	/** The bot locale. */
	public String botLocale;

	/** The bot guidelines. */
	public String botGuidelines;

	/** The bot script. */
	public String botScript;

	/** The bot params. */
	public Map<String, String> botParams;

	/** The text field locale. */
	private JTextField tfLocale;

	/** The button cancel. */
	private JButton btnCancel;
	private RSyntaxTextArea taScript;

	/**
	 * Setup data. Fills the data into the dialog fields.
	 *
	 * @param botName the bot name
	 * @param botLocale the bot locale
	 * @param botGuidelines the bot guidelines
	 * @param botParams the bot params
	 */
	private void setupData(String botName, String botLocale, String botGuidelines, String botScript, Map<String, String> botParams) {
		tfName.setText(botName);
		tfLocale.setText(botLocale);
		taGuidelines.setText(botGuidelines);
		taScript.setText(botScript);
		DefaultTableModel model = (DefaultTableModel)tblParams.getModel();
		model.setRowCount(0);
		for (Map.Entry<String, String> entry : botParams.entrySet()) {
			model.addRow(new Object[] {entry.getKey(), entry.getValue()});
		}
		if(model.getRowCount() == 0) {
			model.addRow(new Object[] {"locale_XX", ""}); //$NON-NLS-1$ //$NON-NLS-2$
			model.addRow(new Object[] {"app_agent_req_prefix", ""}); //$NON-NLS-1$ //$NON-NLS-2$
			model.addRow(new Object[] {"default_error_msg", ""}); //$NON-NLS-1$ //$NON-NLS-2$
			model.addRow(new Object[] {"no_agent_error_msg", ""}); //$NON-NLS-1$ //$NON-NLS-2$
			model.addRow(new Object[] {"XXX_max_tokens", "512"}); //$NON-NLS-1$ //$NON-NLS-2$
			model.addRow(new Object[] {"XXX_temperature", "0.5"}); //$NON-NLS-1$ //$NON-NLS-2$
			model.addRow(new Object[] {"XXX_model", "Llama 3.2 3B Instruct"}); //$NON-NLS-1$ //$NON-NLS-2$
			model.addRow(new Object[] {"XXX_api_key", ""}); //$NON-NLS-1$ //$NON-NLS-2$
		}
		tblParams.setAutoCreateRowSorter(true);
		isModified = false;
	}

	/**
	 * Fills data from the dialog fields into the caller data.
	 *
	 * @param okClicked User clicked on Ok button.
	 */
	private void setupCallerData(boolean okClicked) {
		if(!okClicked) {
			botName = null;
			botLocale = null;
			botGuidelines = null;
			botParams = null;
			return;
		}

		isModified = okClicked;
		botName = tfName.getText();
		botLocale = tfLocale.getText();
		botScript = taScript.getText();
		botGuidelines = taGuidelines.getText();
		DefaultTableModel model = (DefaultTableModel)tblParams.getModel();
		botParams = new java.util.HashMap<String, String>();
		for(int i = 0; i < model.getRowCount(); i++) {
			botParams.put((String)model.getValueAt(i, 0), (String)model.getValueAt(i, 1));
		}
	}

	/**
	 * Instantiates a new chat bot settings dialog.
	 *
	 * @param parent the parent frame
	 * @param botName the bot name
	 * @param botLocale the bot locale
	 * @param botGuidelines the bot guidelines
	 * @param botScript the bot script
	 * @param botParams the bot params
	 */
	public ChatBotSettingsDialog(Frame parent, String botName, String botLocale, String botGuidelines, String botScript, Map<String, String> botParams) {
		super(parent, Messages.getString("ChatBotSettingsDialog.DLG_TITLE"), true); //$NON-NLS-1$
		setTitle(Messages.getString("ChatBotSettingsDialog.DLG_TITLE")); //$NON-NLS-1$
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Settings settings = MainWindow.getSettings();
				settings.setDlgChatBotSize(getSize());
				settings.setDlgChatBotPos(getLocation());
			}
			@Override
			public void windowOpened(WindowEvent e) {
				Helper.addPopupMenuToTextBoxes(ChatBotSettingsDialog.this);
			}
		});
		Settings settings = MainWindow.getSettings();
		setSize(new Dimension(466, 389));
		setLocation(settings.getDlgChatBotPos());
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setFocusable(false);
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 130, Short.MAX_VALUE)
					.addGap(1))
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGap(1)
					.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
					.addGap(1))
		);

		JPanel pnlOptions = new JPanel();
		tabbedPane.addTab(Messages.getString("ChatBotSettingsDialog.TAB_OPTIONS"), null, pnlOptions, null); //$NON-NLS-1$

				JLabel lblNewLabel = new JLabel(Messages.getString("ChatBotSettingsDialog.NAME"));

				tfName = new JTextField();
				tfName.setColumns(10);

				JLabel lblNewLabel_3 = new JLabel(Messages.getString("ChatBotSettingsDialog.LOCALE"));

				tfLocale = new JTextField();
				tfLocale.setColumns(10);

				JLabel lblNewLabel_1 = new JLabel(Messages.getString("ChatBotSettingsDialog.GUIDELINES"));

				JScrollPane scrollPane_1 = new JScrollPane();
				scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

						taGuidelines = new JTextArea();
						taGuidelines.setLineWrap(true);
						taGuidelines.setWrapStyleWord(true);
						scrollPane_1.setViewportView(taGuidelines);
		GroupLayout gl_pnlOptions = new GroupLayout(pnlOptions);
		gl_pnlOptions.setHorizontalGroup(
			gl_pnlOptions.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlOptions.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_pnlOptions.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblNewLabel)
						.addComponent(lblNewLabel_3)
						.addComponent(lblNewLabel_1))
					.addGap(6)
					.addGroup(gl_pnlOptions.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_pnlOptions.createSequentialGroup()
							.addComponent(tfLocale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addContainerGap())
						.addGroup(Alignment.TRAILING, gl_pnlOptions.createSequentialGroup()
							.addGroup(gl_pnlOptions.createParallelGroup(Alignment.TRAILING)
								.addComponent(tfName, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
								.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE))
							.addGap(6))))
		);
		gl_pnlOptions.setVerticalGroup(
			gl_pnlOptions.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlOptions.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_pnlOptions.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel)
						.addComponent(tfName, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlOptions.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_3)
						.addComponent(tfLocale, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
					.addGap(6)
					.addGroup(gl_pnlOptions.createParallelGroup(Alignment.BASELINE)
						.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
						.addComponent(lblNewLabel_1))
					.addGap(6))
		);
		pnlOptions.setLayout(gl_pnlOptions);

		JPanel pnlParams = new JPanel();
		tabbedPane.addTab(Messages.getString("ChatBotSettingsDialog.TAB_PARAMS"), null, pnlParams, null); //$NON-NLS-1$

				JLabel lblNewLabel_2 = new JLabel(Messages.getString("ChatBotSettingsDialog.PARAMS"));

				JButton btnRemoveRow = new JButton("-"); //$NON-NLS-1$
				btnRemoveRow.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						DefaultTableModel model = (DefaultTableModel)tblParams.getModel();
						int rowIndex = tblParams.getSelectedRow();
						if(rowIndex >= 0)
							model.removeRow(rowIndex);
					}
				});
				btnRemoveRow.setMargin(new Insets(2, 2, 2, 2));

				JButton btnNewRow = new JButton("+"); //$NON-NLS-1$
				btnNewRow.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						DefaultTableModel model = (DefaultTableModel)tblParams.getModel();
						model.addRow( new Object[] { "", "" } ); //$NON-NLS-1$ //$NON-NLS-2$
						int rowIndex = model.getRowCount() - 1;
						tblParams.setRowSelectionInterval(rowIndex, rowIndex);
						tblParams.scrollRectToVisible(tblParams.getCellRect(rowIndex, 0, true));
						tblParams.requestFocus();
						tblParams.editCellAt(rowIndex, 0);
					}
				});
				btnNewRow.setMargin(new Insets(2, 2, 2, 2));

				JScrollPane scrollPane = new JScrollPane();

						tblParams = new JTable();
						tblParams.setFillsViewportHeight(true);
						tblParams.setModel(new DefaultTableModel(
							new Object[][] {
								{null, null},
							},
							new String[] {
								Messages.getString("ChatBotSettingsDialog.TH_KEY"), Messages.getString("ChatBotSettingsDialog.TH_VALUE") //$NON-NLS-1$ //$NON-NLS-2$
							}
						) {
							Class[] columnTypes = new Class[] {
								String.class, String.class
							};
							public Class getColumnClass(int columnIndex) {
								return columnTypes[columnIndex];
							}
						});
						scrollPane.setViewportView(tblParams);
		GroupLayout gl_pnlParams = new GroupLayout(pnlParams);
		gl_pnlParams.setHorizontalGroup(
			gl_pnlParams.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_pnlParams.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_pnlParams.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblNewLabel_2)
						.addComponent(btnNewRow, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnRemoveRow, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE))
					.addGap(6)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
					.addGap(6))
		);
		gl_pnlParams.setVerticalGroup(
			gl_pnlParams.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlParams.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_pnlParams.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
						.addGroup(gl_pnlParams.createSequentialGroup()
							.addComponent(lblNewLabel_2)
							.addPreferredGap(ComponentPlacement.RELATED, 192, Short.MAX_VALUE)
							.addComponent(btnRemoveRow)
							.addGap(3)
							.addComponent(btnNewRow)))
					.addGap(6))
		);
		pnlParams.setLayout(gl_pnlParams);

		JPanel pnlScript = new JPanel();
		tabbedPane.addTab(Messages.getString("ChatBotSettingsDialog.TAB_SCRIPT"), null, pnlScript, null); //$NON-NLS-1$

		RTextScrollPane spScript = new RTextScrollPane();
		GroupLayout gl_pnlScript = new GroupLayout(pnlScript);
		gl_pnlScript.setHorizontalGroup(
			gl_pnlScript.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlScript.createSequentialGroup()
					.addGap(3)
					.addComponent(spScript, GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
					.addGap(3))
		);
		gl_pnlScript.setVerticalGroup(
			gl_pnlScript.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlScript.createSequentialGroup()
					.addGap(3)
					.addComponent(spScript, GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
					.addGap(3))
		);

		taScript = new RSyntaxTextArea();
		taScript.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
		taScript.setCodeFoldingEnabled(true);
		taScript.setAntiAliasingEnabled(true);
		org.fife.ui.autocomplete.CompletionProvider provider = new io.github.emmrida.chat4us.util.JavetCompletionProvider();
        AutoCompletion ac = new AutoCompletion(provider);
        ac.install(taScript);
        taScript.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) { isModified = true;  }
			@Override
			public void removeUpdate(DocumentEvent e) { isModified = true; }
			@Override
			public void changedUpdate(DocumentEvent e) { isModified = true; }
		});
		spScript.setViewportView(taScript);
		spScript.setBorder(null);
		spScript.setLineNumbersEnabled(true);
		pnlScript.setLayout(gl_pnlScript);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton btnSave = new JButton(Messages.getString("ChatBotSettingsDialog.BTN_SAVE")); //$NON-NLS-1$
				btnSave.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Settings settings = MainWindow.getSettings();
						settings.setDlgChatBotSize(getSize());
						settings.setDlgChatBotPos(getLocation());
						setupCallerData(true);
						setVisible(false);
					}
				});
				btnSave.setActionCommand("OK"); //$NON-NLS-1$
				buttonPane.add(btnSave);
				getRootPane().setDefaultButton(btnSave);
			}
			{
				btnCancel = new JButton(Messages.getString("ChatBotSettingsDialog.BTN_CANCEL")); //$NON-NLS-1$
				btnCancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Settings settings = MainWindow.getSettings();
						settings.setDlgChatBotSize(getSize());
						settings.setDlgChatBotPos(getLocation());
						setupCallerData(false);
						setVisible(false);
					}
				});
				btnCancel.setActionCommand("Cancel"); //$NON-NLS-1$
				buttonPane.add(btnCancel);
			}
		}
		Helper.registerCancelByEsc(this, btnCancel);
		if(settings.getDlgChatBotSize().height == 0) {
			pack();
			setLocationRelativeTo(getParent());
		}

		/*
		 *
		 */
		Helper.enableRtlWhenNeeded(this);
		setupData(botName, botLocale, botGuidelines, botScript, botParams);
	}
}
