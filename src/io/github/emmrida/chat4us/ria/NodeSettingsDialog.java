/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.ria;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import io.github.emmrida.chat4us.gui.MainWindow;
import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;
import io.github.emmrida.chat4us.util.Settings;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.InputVerifier;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dimension;

/**
 * The Class NodeSettingsDialog.
 *
 * @author El Mhadder Mohamed Rida
 */
public class NodeSettingsDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private boolean modified = false;
	private boolean scriptEdited = false;
	private List<Integer> ids = null;
	private NodePanel.Data data;

	private final JPanel contentPanel = new JPanel();
	private JTextArea taMessage;
	private JComboBox<String> cmbType;
	private JTextArea taSuccessMsg;
	private JComboBox<String> cmbSuccessAction;
	private JComboBox<Integer> cmbSuccessMove;
	private JTextArea taErrorMsg;
	private JComboBox<String> cmbErrorAction;
	private JComboBox<Integer> cmbErrorMove;
	private JTextField tfCondition;
	private JTextField tfSuccessValue;
	private JTextField tfErrorValue;
	private RSyntaxTextArea taScript;
	private JButton cancelButton;

	/**
	 * Checks if is modified.
	 *
	 * @return true, if is modified
	 */
	public boolean isModified() { return modified; }

	/**
	 * Gets the Data instance filled with user edits.
	 *
	 * @return the Data instance.
	 */
	public NodePanel.Data getData() { return data; }

	/**
	 * Fill dialog fields with the given Data instance content.
	 */
	private void setupData() {
		NodePanel.Data.fillValidationComboBox(cmbType);
		NodePanel.Data.fillSuccessActionComboBox(cmbSuccessAction);
		NodePanel.Data.fillErrorActionComboBox(cmbErrorAction);
		cmbSuccessMove.addItem(0);
		cmbErrorMove.addItem(0);
		for(int i = 0; i < ids.size(); i++) {
			int id = ids.get(i);
			if(id != data.getId()) {
				cmbSuccessMove.addItem(id);
				cmbErrorMove.addItem(id);
			}
		}
		taMessage.setText(data.getMessage().trim());
		cmbType.setSelectedItem(data.getValType());
		tfCondition.setText(data.getValCondition().trim());
		taSuccessMsg.setText(data.getSucMessage().trim());
		cmbSuccessAction.setSelectedItem(data.getSucAction());
		tfSuccessValue.setText(data.getSucValue().trim());
		cmbSuccessMove.setSelectedItem(data.getSucMoveTo());
		taErrorMsg.setText(data.getErrMessage().trim());
		cmbErrorAction.setSelectedItem(data.getErrAction());
		tfErrorValue.setText(data.getErrValue().trim());
		cmbErrorMove.setSelectedItem(data.getErrMoveTo());
		taScript.setText(data.getScript().trim());
		scriptEdited = false;
	}

	/**
	 * Create the dialog.
	 *
	 * @param parent the parent of this dialog
	 * @param data the Data instance to edit properties.
	 * @param ids the ids
	 */
	public NodeSettingsDialog(JFrame parent, NodePanel.Data data, List<Integer> ids) {
		super(parent, String.format(Messages.getString("NodeSettingsDialog.DLG_TITLE"), data.getId()), true); //$NON-NLS-1$
		//setTitle(Messages.getString("NodeSettingsDialog.DLG_TITLE")); //$NON-NLS-1$
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(scriptEdited) {
					int ret = Helper.showConfirmDialog(NodeSettingsDialog.this, Messages.getString("NodeSettingsDialog.MB_IGNORE_CHANGES_MSG"), Messages.getString("NodeSettingsDialog.MB_IGNORE_CHANGES_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
					if(ret == JOptionPane.NO_OPTION) {
						modified = false;
						return;
					}
				}
				Settings settings = MainWindow.getSettings();
				settings.setDlgRouteItemSize(getSize());
				settings.setDlgRouteItemPos(getLocation());
				dispose();
			}
			@Override
			public void windowOpened(WindowEvent e) {
				Helper.addPopupMenuToTextBoxes(NodeSettingsDialog.this);
			}
		});
		this.data = data;
		this.ids = ids;
		//setLocationRelativeTo(parent);
		Settings settings = MainWindow.getSettings();
		setSize(new Dimension(468, 364));
		setLocation(settings.getDlgRouteItemPos());
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane();

		JLabel lblNewLabel = new JLabel(Messages.getString("NodeSettingsDialog.LBL_MESSAGE")); //$NON-NLS-1$

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

		JLabel lblNewLabel_1 = new JLabel(Messages.getString("NodeSettingsDialog.LBL_RESPONSE")); //$NON-NLS-1$
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblNewLabel_1)
						.addComponent(lblNewLabel))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
						.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)))
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
						.addComponent(lblNewLabel))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 174, Short.MAX_VALUE)
						.addComponent(lblNewLabel_1)))
		);

		JPanel pnlValidation = new JPanel();
		tabbedPane.addTab(Messages.getString("NodeSettingsDialog.TAB_TITLE_VALIDATION"), null, pnlValidation, null); //$NON-NLS-1$

		cmbType = new JComboBox<>();

		JLabel lblNewLabel_2 = new JLabel(Messages.getString("NodeSettingsDialog.LBL_TITLE")); //$NON-NLS-1$

		JLabel lblNewLabel_3 = new JLabel(Messages.getString("NodeSettingsDialog.LBL_CONDITION")); //$NON-NLS-1$

		tfCondition = new JTextField();
		tfCondition.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				if(tfCondition.getText().isEmpty()) return true;

				String errMsg = ""; //$NON-NLS-1$
				String cnd = cmbType.getSelectedItem().toString();
				if("matching_list".equals(cnd)) { //$NON-NLS-1$
					if(!Helper.regexValidate("^\\['[^']*'\\s*,\\s*-?\\d+\\](?:\\['[^']*'\\s*,\\s*-?\\d+\\])*$", tfCondition.getText())) //$NON-NLS-1$
						errMsg = Messages.getString("NodeSettingsDialog.ERR_INVALID_MATCHING_LIST"); //$NON-NLS-1$
				} else if("matching_values".equals(cnd)) { //$NON-NLS-1$
					if(!Helper.regexValidate("^\\['[^']*'\\s*,\\s*'[^']*'\\](?:\\['[^']*'\\s*,\\s*'[^']*'\\])*$", tfCondition.getText())) //$NON-NLS-1$
						errMsg = Messages.getString("NodeSettingsDialog.ERR_INVALID_MATCHING_VALUES"); //$NON-NLS-1$
				} else if("number:interval".equals(cnd)) { //$NON-NLS-1$
					String[] parts = tfCondition.getText().split("\\.\\.\\."); //$NON-NLS-1$
					try {
						double min = Double.parseDouble(parts[0]);
						double max = Double.parseDouble(parts[1]);
						if(min > max)
							errMsg = Messages.getString("NodeSettingsDialog.ERR_INVALID_INTERVAL"); //$NON-NLS-1$
					} catch(Exception ex) {
						errMsg = Messages.getString("NodeSettingsDialog.ERR_INVALID_INTERVAL"); //$NON-NLS-1$
					}
				} else if("text:in_list".equals(cnd)) { //$NON-NLS-1$
					if(!tfCondition.getText().contains(";")) //$NON-NLS-1$
						errMsg = Messages.getString("NodeSettingsDialog.ERR_INVALID_INLIST"); //$NON-NLS-1$
				} else if(cnd.startsWith("number:")) { //$NON-NLS-1$
					if(!Helper.isNumeric(tfCondition.getText()))
						errMsg = Messages.getString("NodeSettingsDialog.ERR_INVALID_NUMBER"); //$NON-NLS-1$
				}
				if(!errMsg.isBlank()) {
					JOptionPane.showMessageDialog(NodeSettingsDialog.this, errMsg, Messages.getString("NodeSettingsDialog.MB_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					return false;
				}
				return true;
			}
		});
		tfCondition.setColumns(10);
		GroupLayout gl_pnlValidation = new GroupLayout(pnlValidation);
		gl_pnlValidation.setHorizontalGroup(
			gl_pnlValidation.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlValidation.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_pnlValidation.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblNewLabel_2)
						.addComponent(lblNewLabel_3))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlValidation.createParallelGroup(Alignment.TRAILING)
						.addComponent(tfCondition, GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
						.addComponent(cmbType, 0, 297, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_pnlValidation.setVerticalGroup(
			gl_pnlValidation.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlValidation.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_pnlValidation.createParallelGroup(Alignment.BASELINE)
						.addComponent(cmbType, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_2))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlValidation.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfCondition, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_3))
					.addContainerGap(78, Short.MAX_VALUE))
		);
		pnlValidation.setLayout(gl_pnlValidation);

		JPanel pnlOnSuccess = new JPanel();
		tabbedPane.addTab(Messages.getString("NodeSettingsDialog.TAB_TITLE_SUCCESS"), null, pnlOnSuccess, null); //$NON-NLS-1$

		JScrollPane scrollPane_1 = new JScrollPane();

		JLabel lblNewLabel_4 = new JLabel(Messages.getString("NodeSettingsDialog.LBL_SUC_MESSAGE")); //$NON-NLS-1$

		cmbSuccessAction = new JComboBox<>();

		JLabel lblNewLabel_5 = new JLabel(Messages.getString("NodeSettingsDialog.LBL_SUC_ACTION")); //$NON-NLS-1$

		tfSuccessValue = new JTextField();
		tfSuccessValue.setColumns(10);

		JLabel lblNewLabel_6 = new JLabel(Messages.getString("NodeSettingsDialog.LBL_SUC_VALUE")); //$NON-NLS-1$

		cmbSuccessMove = new JComboBox<>();

		JLabel lblNewLabel_7 = new JLabel(Messages.getString("NodeSettingsDialog.LBL_SUC_MOVETO")); //$NON-NLS-1$

		JLabel lblNewLabel_8 = new JLabel(Messages.getString("NodeSettingsDialog.LBL_SUC_NODE")); //$NON-NLS-1$
		GroupLayout gl_pnlOnSuccess = new GroupLayout(pnlOnSuccess);
		gl_pnlOnSuccess.setHorizontalGroup(
			gl_pnlOnSuccess.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlOnSuccess.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_pnlOnSuccess.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblNewLabel_4)
						.addComponent(lblNewLabel_7))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlOnSuccess.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_pnlOnSuccess.createSequentialGroup()
							.addComponent(cmbSuccessMove, GroupLayout.PREFERRED_SIZE, 74, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblNewLabel_8)
							.addGap(58))
						.addGroup(gl_pnlOnSuccess.createSequentialGroup()
							.addComponent(scrollPane_1)
							.addGap(6))))
				.addGroup(Alignment.TRAILING, gl_pnlOnSuccess.createSequentialGroup()
					.addGroup(gl_pnlOnSuccess.createParallelGroup(Alignment.TRAILING)
						.addGroup(Alignment.LEADING, gl_pnlOnSuccess.createSequentialGroup()
							.addGap(63)
							.addComponent(lblNewLabel_6)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(tfSuccessValue, GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE))
						.addGroup(gl_pnlOnSuccess.createSequentialGroup()
							.addGap(18)
							.addComponent(lblNewLabel_5)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(cmbSuccessAction, 0, 263, Short.MAX_VALUE)))
					.addGap(6))
		);
		gl_pnlOnSuccess.setVerticalGroup(
			gl_pnlOnSuccess.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlOnSuccess.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_pnlOnSuccess.createParallelGroup(Alignment.BASELINE)
						.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
						.addComponent(lblNewLabel_4))
					.addGap(6)
					.addGroup(gl_pnlOnSuccess.createParallelGroup(Alignment.BASELINE)
						.addComponent(cmbSuccessAction, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_5))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlOnSuccess.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfSuccessValue, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_6))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlOnSuccess.createParallelGroup(Alignment.BASELINE)
						.addComponent(cmbSuccessMove, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_7)
						.addComponent(lblNewLabel_8))
					.addGap(6))
		);

		taSuccessMsg = new JTextArea();
		taSuccessMsg.setLineWrap(true);
		taSuccessMsg.setWrapStyleWord(true);
		scrollPane_1.setViewportView(taSuccessMsg);
		pnlOnSuccess.setLayout(gl_pnlOnSuccess);

		JPanel pnlOnError = new JPanel();
		tabbedPane.addTab(Messages.getString("NodeSettingsDialog.TAB_TITLE_ERROR"), null, pnlOnError, null); //$NON-NLS-1$

		JLabel lblNewLabel_7_1 = new JLabel(Messages.getString("NodeSettingsDialog.LBL_ERR_MOVETO")); //$NON-NLS-1$

		JLabel lblNewLabel_6_1 = new JLabel(Messages.getString("NodeSettingsDialog.LBL_ERR_VALUE")); //$NON-NLS-1$

		JLabel lblNewLabel_5_1 = new JLabel(Messages.getString("NodeSettingsDialog.LBL_ERR_ACTION")); //$NON-NLS-1$

		JLabel lblNewLabel_4_1 = new JLabel(Messages.getString("NodeSettingsDialog.LBL_ERR_MESSAGE")); //$NON-NLS-1$

		tfErrorValue = new JTextField();
		tfErrorValue.setColumns(10);

		cmbErrorAction = new JComboBox<>();

		JScrollPane scrollPane_1_1 = new JScrollPane();

		cmbErrorMove = new JComboBox<>();

		JLabel lblNewLabel_8_1 = new JLabel(Messages.getString("NodeSettingsDialog.LBL_ERR_NODE")); //$NON-NLS-1$
		GroupLayout gl_pnlOnError = new GroupLayout(pnlOnError);
		gl_pnlOnError.setHorizontalGroup(
			gl_pnlOnError.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_pnlOnError.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_pnlOnError.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblNewLabel_7_1)
						.addComponent(lblNewLabel_5_1)
						.addComponent(lblNewLabel_4_1)
						.addComponent(lblNewLabel_6_1))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlOnError.createParallelGroup(Alignment.LEADING)
						.addComponent(cmbErrorAction, 0, 265, Short.MAX_VALUE)
						.addComponent(tfErrorValue, GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
						.addGroup(gl_pnlOnError.createSequentialGroup()
							.addComponent(cmbErrorMove, GroupLayout.PREFERRED_SIZE, 74, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblNewLabel_8_1, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE))
						.addComponent(scrollPane_1_1, GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE))
					.addGap(6))
		);
		gl_pnlOnError.setVerticalGroup(
			gl_pnlOnError.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_pnlOnError.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_pnlOnError.createParallelGroup(Alignment.BASELINE)
						.addComponent(scrollPane_1_1)
						.addComponent(lblNewLabel_4_1))
					.addGap(6)
					.addGroup(gl_pnlOnError.createParallelGroup(Alignment.BASELINE)
						.addComponent(cmbErrorAction, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_5_1))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlOnError.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfErrorValue, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_6_1))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlOnError.createParallelGroup(Alignment.BASELINE)
						.addComponent(cmbErrorMove, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_7_1)
						.addComponent(lblNewLabel_8_1))
					.addGap(6))
		);

		taErrorMsg = new JTextArea();
		taErrorMsg.setLineWrap(true);
		taErrorMsg.setWrapStyleWord(true);
		scrollPane_1_1.setViewportView(taErrorMsg);
		pnlOnError.setLayout(gl_pnlOnError);

		JPanel panelScript = new JPanel();
		tabbedPane.addTab(Messages.getString("NodeSettingsDialog.TAB_TITLE_SCRIPT"), null, panelScript, null); //$NON-NLS-1$
		panelScript.setLayout(new BorderLayout(0, 0));

		taScript = new RSyntaxTextArea();
		taScript.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
		taScript.setCodeFoldingEnabled(true);
		taScript.setAntiAliasingEnabled(true);
		org.fife.ui.autocomplete.CompletionProvider provider = new io.github.emmrida.chat4us.util.JavetCompletionProvider();
        AutoCompletion ac = new AutoCompletion(provider);
        ac.install(taScript);
		taScript.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) { scriptEdited = true; }
			@Override
			public void removeUpdate(DocumentEvent e) { scriptEdited = true; }
			@Override
			public void changedUpdate(DocumentEvent e) { scriptEdited = true; }
		});
		RTextScrollPane scrollPane_2 = new RTextScrollPane(taScript);
		scrollPane_2.setBorder(null);
		scrollPane_2.setLineNumbersEnabled(true);
		panelScript.add(scrollPane_2, BorderLayout.CENTER);

		taMessage = new JTextArea();
		taMessage.setLineWrap(true);
		taMessage.setWrapStyleWord(true);
		scrollPane.setViewportView(taMessage);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(Messages.getString("NodeSettingsDialog.BTN_SAVE")); //$NON-NLS-1$
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						/*
						if(BuildConfig.IS_VIEWER_APP) {
							BuildConfig.showUnsupportedFeatureDialog(NodeSettingsDialog.this);
							return;
						}
						*/
						modified = true;
						NodePanel.Data data = new NodePanel.Data(NodeSettingsDialog.this.data);
						// Copy data from dialog to NodePanel.Data
						data.setMessage(taMessage.getText().trim());
						data.setValidation(cmbType.getSelectedItem().toString(), tfCondition.getText().trim(), taScript.getText().trim());
						data.setSuccess(taSuccessMsg.getText().trim(), cmbSuccessAction.getSelectedItem().toString(), tfSuccessValue.getText().trim(), Integer.parseInt(cmbSuccessMove.getSelectedIndex()>=0 ? cmbSuccessMove.getSelectedItem().toString() : "0")); //$NON-NLS-1$
						data.setError(taErrorMsg.getText().trim(), cmbErrorAction.getSelectedItem().toString(), tfErrorValue.getText().trim(), Integer.parseInt(cmbErrorMove.getSelectedIndex()>=0 ? cmbErrorMove.getSelectedItem().toString() : "0")); //$NON-NLS-1$
						NodeSettingsDialog.this.data = data;
						Settings settings = MainWindow.getSettings();
						settings.setDlgRouteItemSize(getSize());
						settings.setDlgRouteItemPos(getLocation());
						dispose();
					}
				});
				okButton.setActionCommand("OK"); //$NON-NLS-1$
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton(Messages.getString("NodeSettingsDialog.BTN_CANCEL")); //$NON-NLS-1$
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(scriptEdited) {
							int ret = Helper.showConfirmDialog(NodeSettingsDialog.this, Messages.getString("NodeSettingsDialog.MB_IGNORE_SCRIPT_CHANGES_MSG"), Messages.getString("NodeSettingsDialog.MB_IGNORE_SCRIPT_CHANGES_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 1); //$NON-NLS-1$ //$NON-NLS-2$
							if(ret == JOptionPane.NO_OPTION)
								return;
						}
						Settings settings = MainWindow.getSettings();
						settings.setDlgRouteItemSize(getSize());
						settings.setDlgRouteItemPos(getLocation());
						modified = false;
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel"); //$NON-NLS-1$
				buttonPane.add(cancelButton);
			}
		}
		Helper.registerCancelByEsc(this, cancelButton);
		if(settings.getDlgRouteItemSize().height == 0) {
			pack();
			setLocationRelativeTo(getParent());
		}

		Helper.enableRtlWhenNeeded(this);
		setupData();
	}
}
