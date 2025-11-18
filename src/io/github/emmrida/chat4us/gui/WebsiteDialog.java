/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.gui;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import io.github.emmrida.chat4us.controls.IdLabelComboElement;
import io.github.emmrida.chat4us.controls.IdLabelComboModel;
import io.github.emmrida.chat4us.controls.IdLabelComboRenderer;
import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.UIManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JPasswordField;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.SwingConstants;
import java.awt.Component;

/**
 * The website dialog. This dialog is used to add/edit a website so the chat bot IFrame
 * can gain access to the chat bot server for user/ai chat.
 *
 * @author El Mhadder Mohamed Rida
 */
public class WebsiteDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final String NOT_MODIFIED = Messages.getString("WebsiteDialog.NOT_MODIFIED"); //$NON-NLS-1$

	private int dbId;
	private boolean canceled = true;
	private boolean edited = false;
	private boolean key1Edited = false;
	private boolean key2Edited = false;

	private final JPanel contentPanel = new JPanel();
	private JTextField tfDomain;
	private JTextField tfIP;
	private JPasswordField tfKey1;
	private JPasswordField tfKey2;
	private JButton btnGenerate;
	private JButton btnAdd;
	private JButton btnCancel;
	private JComboBox<IdLabelComboElement> cmbAIGroup;
	private JTextArea taDesc;
	private JToggleButton tglShow;
	private JButton btnCopyKey1;
	private JButton btnCopyKey2;

	/**
	 * Check if the dialog is canceled
	 * @return true if the dialog is canceled
	 */
	public boolean isCanceled()    { return canceled; }

	/**
	 * Check if the dialog is edited or a new website is added
	 * @return true if the dialog is edited, otherwize a new website is added.
	 */
	public boolean isEdited()      { return edited;   }

	/**
	 * Get the domain of the website
	 * @return the domain of the website
	 */
	public String getDomain()      { return canceled ? null : tfDomain.getText(); }

	/**
	 * Get the ip of the website
	 * @return the ip of the website
	 */
	public String getIP()          { return canceled ? null : tfIP.getText();     }

	/**
	 * Get the key1 of the website
	 * @return the key1 of the website
	 */
	public String getKey1()        { return canceled ? null : NOT_MODIFIED.equals(tfKey1.getText()) ? null : tfKey1.getText();   }

	/**
	 * Get the key2 of the website
	 * @return the key2 of the website
	 */
	public String getKey2()        { return canceled ? null : NOT_MODIFIED.equals(tfKey2.getText()) ? null : tfKey2.getText();   }

	/**
	 * Get the description of the website
	 * @return the description of the website
	 */
	public String getDescription() { return canceled ? null : taDesc.getText(); }

	/**
	 * Get the ai group id of the website
	 * @return the ai group id of the website
	 */
	public int getAIGroupId()      { return canceled ?    0 : ((IdLabelComboElement)cmbAIGroup.getSelectedItem()).getId(); }

	/**
	 * Get the database id of the website
	 * @return the database id of the website
	 */
	public int getDbId()           { return dbId; }

	/**
	 * Check if the key1 is edited
	 * @return true if the key1 is edited
	 */
	public boolean isKey1Edited()  { return key1Edited; }

	/**
	 * Check if the key2 is edited
	 * @return true if the key2 is edited
	 */
	public boolean isKey2Edited()  { return key2Edited; }

	/**
	 * Update the buttons state based on the edited state
	 */
	private void updateButtons() {
		btnCopyKey1.setEnabled(key1Edited);
		btnCopyKey2.setEnabled(key2Edited);
		tglShow.setEnabled(key1Edited || key2Edited);
		tfKey1.setEchoChar(tglShow.isSelected() && key1Edited ? '•' : '\0');
		tfKey2.setEchoChar(tglShow.isSelected() && key2Edited ? '•' : '\0');
	}

	/**
	 * Create the dialog
	 * @param dbId The id of the website to edit
	 * @param edit true if the website is edited, false if a new website is added
	 */
	public WebsiteDialog(Frame parent, int dbId, boolean edit) {
		this(parent);
		this.dbId = dbId;
		this.edited = edit;
		if(edit) setTitle(Messages.getString("WebsiteDialog.DLG_TITLE_EDIT")); //$NON-NLS-1$
		List<Object[]> aiGroups = MainWindow.getAIGroups();
		for(Object[] aig : aiGroups)
			if((Integer)aig[2] == 0)
				cmbAIGroup.addItem(new IdLabelComboElement((Integer)aig[0], (String)aig[1], true));
		if(edit) {
			if(dbId <= 0)
				throw new IllegalArgumentException();
			btnAdd.setText(Messages.getString("WebsiteDialog.BTN_SAVE")); //$NON-NLS-1$
			try(PreparedStatement ps = MainWindow.getDBConnection().prepareStatement("SELECT * FROM websites WHERE id=?;")) { //$NON-NLS-1$
				ps.setInt(1, dbId);
				try(ResultSet rs = ps.executeQuery()) {
					if(rs.next()) {
						tfDomain.setText(rs.getString("domain")); //$NON-NLS-1$
						tfIP.setText(rs.getString("ip")); //$NON-NLS-1$
						tfKey1.setText(NOT_MODIFIED);
						tfKey1.setEchoChar('\0');
						tfKey2.setText(NOT_MODIFIED);
						tfKey2.setEchoChar('\0');
						tglShow.setSelected(true);
						Object[] aig = MainWindow.getAIGroupById(rs.getInt("ai_group_id")); //$NON-NLS-1$
						IdLabelComboModel model = (IdLabelComboModel)cmbAIGroup.getModel();
						for(int i = 0; i < model.getSize(); i++) {
							if((int)aig[0] == model.getElementAt(i).getId()) {
								cmbAIGroup.setSelectedIndex(i);
								break;
							}
						}
						taDesc.setText(rs.getString("description")); //$NON-NLS-1$
					} else {
						Helper.logError(Messages.getString("WebsiteDialog.MB_SITE_LOAD_ERROR"), true); //$NON-NLS-1$
						dispose();
						return;
					}
				} catch (SQLException ex) {
					Helper.logError(ex, String.format(Messages.getString("WebsiteDialog.MB_DB_LOAD_ERROR"), dbId), true); //$NON-NLS-1$
					dispose();
					return;
				}
			} catch (SQLException ex) {
				Helper.logError(ex, String.format(Messages.getString("WebsiteDialog.MB_DB_LOAD_ERROR"), dbId), true); //$NON-NLS-1$
				dispose();
				return;
			}
			updateButtons();
			/*
			btnCopyKey1.setEnabled(false);
			btnCopyKey2.setEnabled(false);
			tglShow.setSelected(false);
			tglShow.setEnabled(false);
			*/
		}
		key1Edited = false;
		key2Edited = false;
		tfKey1.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) { if(!tfKey1.getText().isEmpty() && !tfKey1.getText().equals(NOT_MODIFIED)) key1Edited = true; updateButtons(); }
			@Override
			public void removeUpdate(DocumentEvent e) { if(!tfKey1.getText().isEmpty() && !tfKey1.getText().equals(NOT_MODIFIED)) key1Edited = true; updateButtons(); }
			@Override
			public void changedUpdate(DocumentEvent e) { if(!tfKey1.getText().isEmpty() && !tfKey1.getText().equals(NOT_MODIFIED)) key1Edited = true; updateButtons(); }

		});
		tfKey2.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) { if(!tfKey2.getText().isEmpty() && !tfKey2.getText().equals(NOT_MODIFIED)) key2Edited = true; updateButtons(); }
			@Override
			public void removeUpdate(DocumentEvent e) { if(!tfKey2.getText().isEmpty() && !tfKey2.getText().equals(NOT_MODIFIED)) key2Edited = true; updateButtons(); }
			@Override
			public void changedUpdate(DocumentEvent e) { if(!tfKey2.getText().isEmpty() && !tfKey2.getText().equals(NOT_MODIFIED)) key2Edited = true; updateButtons(); }
		});
	}

	/**
	 * Create the dialog.
	 * @param parent The parent frame
	 * @wbp.parser.constructor
	 */
	private WebsiteDialog(Frame parent) {
		super(parent, Messages.getString("WebsiteDialog.DLG_TITLE"), true); //$NON-NLS-1$
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				Helper.addPopupMenuToTextBoxes(WebsiteDialog.this);
			}
		});
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		tfDomain = new JTextField();
		tfDomain.setColumns(10);
		JLabel lblNewLabel = new JLabel(Messages.getString("WebsiteDialog.LBL_DOMAIN")); //$NON-NLS-1$
		tfIP = new JTextField();
		tfIP.setColumns(10);
		JLabel lblNewLabel_1 = new JLabel(Messages.getString("WebsiteDialog.LBL_IP")); //$NON-NLS-1$
		JSeparator separator = new JSeparator();
		tfKey1 = new JPasswordField();
		tfKey1.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if(edited && tfKey1.getText().equals(NOT_MODIFIED))
					tfKey1.setText(""); //$NON-NLS-1$
			}
			@Override
			public void focusLost(FocusEvent e) {
				if(edited && tfKey1.getText().equals("")) //$NON-NLS-1$
					tfKey1.setText(NOT_MODIFIED);
			}
		});
		tfKey1.setFont(UIManager.getFont("TextArea.font")); //$NON-NLS-1$
		tfKey1.setColumns(10);
		JLabel lblNewLabel_2 = new JLabel(Messages.getString("WebsiteDialog.LBL_KEY1")); //$NON-NLS-1$
		tfKey2 = new JPasswordField();
		tfKey2.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if(edited && tfKey2.getText().equals(NOT_MODIFIED))
					tfKey2.setText(""); //$NON-NLS-1$
			}
			@Override
			public void focusLost(FocusEvent e) {
				if(edited && tfKey2.getText().equals("")) //$NON-NLS-1$
					tfKey2.setText(NOT_MODIFIED);
			}
		});
		tfKey2.setFont(UIManager.getFont("TextArea.font")); //$NON-NLS-1$
		tfKey2.setColumns(10);
		JLabel lblNewLabel_3 = new JLabel(Messages.getString("WebsiteDialog.LBL_KEY2")); //$NON-NLS-1$
		JSeparator separator_1 = new JSeparator();

		cmbAIGroup = new JComboBox<IdLabelComboElement>();
		cmbAIGroup.setModel(new IdLabelComboModel());
		cmbAIGroup.setRenderer(new IdLabelComboRenderer());

		JLabel lblNewLabel_4 = new JLabel(Messages.getString("WebsiteDialog.LBL_AIGROUP")); //$NON-NLS-1$

		JLabel lblNewLabel_5 = new JLabel(Messages.getString("WebsiteDialog.LBL_DESCRIPTION")); //$NON-NLS-1$

		JScrollPane scrollPane = new JScrollPane();

		btnCopyKey2 = new JButton(Messages.getString("WebsiteDialog.BTN_COPY")); //$NON-NLS-1$
		btnCopyKey2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.copyToClipboard(tfKey2.getText());
			}
		});
		btnCopyKey2.setPreferredSize(new Dimension(57, 24));
		btnCopyKey2.setMinimumSize(new Dimension(57, 24));

		btnCopyKey1 = new JButton(Messages.getString("WebsiteDialog.BTN_COPY")); //$NON-NLS-1$
		btnCopyKey1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Helper.copyToClipboard(tfKey1.getText());
			}
		});
		btnCopyKey1.setPreferredSize(new Dimension(57, 24));
		btnCopyKey1.setMinimumSize(new Dimension(57, 24));
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(separator, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
						.addComponent(separator_1, GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addGap(6)
							.addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblNewLabel_1)
								.addComponent(lblNewLabel_2)
								.addComponent(lblNewLabel_4)
								.addComponent(lblNewLabel_3)
								.addComponent(lblNewLabel)
								.addComponent(lblNewLabel_5))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(scrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
								.addComponent(cmbAIGroup, Alignment.TRAILING, 0, 449, Short.MAX_VALUE)
								.addGroup(Alignment.TRAILING, gl_contentPanel.createSequentialGroup()
									.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
										.addComponent(tfKey2, GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
										.addComponent(tfKey1, GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE))
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
										.addComponent(btnCopyKey2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(btnCopyKey1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
								.addComponent(tfIP, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(tfDomain, GroupLayout.PREFERRED_SIZE, 215, GroupLayout.PREFERRED_SIZE))))
					.addContainerGap())
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addGap(6)
						.addComponent(lblNewLabel)
						.addComponent(tfDomain, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_1)
						.addComponent(tfIP, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(separator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE, false)
								.addComponent(lblNewLabel_2)
								.addComponent(btnCopyKey1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(tfKey1, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)))
					.addGap(7)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfKey2, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_3)
						.addComponent(btnCopyKey2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(separator_1, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_4)
						.addComponent(cmbAIGroup, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_5))
					.addContainerGap())
		);
		gl_contentPanel.linkSize(SwingConstants.VERTICAL, new Component[] {tfKey1, btnCopyKey1});

		taDesc = new JTextArea();
		scrollPane.setViewportView(taDesc);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				btnGenerate = new JButton(Messages.getString("WebsiteDialog.BTN_GENERATE")); //$NON-NLS-1$
				btnGenerate.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						tglShow.setSelected(true);
						tfKey1.setText(Helper.generateSecureKey(32));
						tfKey2.setText(Helper.generateSecureKey(32));
						tfKey1.requestFocus();
					}
				});
			}
			{
				btnAdd = new JButton(Messages.getString("WebsiteDialog.BTN_ADD")); //$NON-NLS-1$
				btnAdd.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(Helper.isValidIP(tfDomain.getText()) || Helper.isValidURL(tfDomain.getText())) {
							if(Helper.isValidIP(tfIP.getText())) {
								if(tfKey1.getText().length() >= 8 && tfKey2.getText().length() >= 8) {
									if(cmbAIGroup.getSelectedIndex() >= 0) {
										int ret = JOptionPane.showConfirmDialog(WebsiteDialog.this, String.format(Messages.getString("WebsiteDialog.MB_ALTER_SITE_MSG"), (edited? Messages.getString("WebsiteDialog.MB_ALTER_SITE_EDIT") : Messages.getString("WebsiteDialog.MB_ALTER_SITE_ADD"))), Messages.getString("WebsiteDialog.MB_ALTER_SITE_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
										if(ret == JOptionPane.YES_OPTION) {
											canceled = false;
											WebsiteDialog.this.setVisible(false);
										}
										return;
									}
								}
							}
						}
						Helper.logWarning(Messages.getString("WebsiteDialog.MB_ERROR_MSG"), true); //$NON-NLS-1$
					}
				});
				btnAdd.setActionCommand("OK"); //$NON-NLS-1$
				getRootPane().setDefaultButton(btnAdd);
			}
			{
				btnCancel = new JButton(Messages.getString("WebsiteDialog.BTN_CANCEL")); //$NON-NLS-1$
				btnCancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						canceled = true;
						WebsiteDialog.this.setVisible(false);
					}
				});
				btnCancel.setActionCommand("Cancel"); //$NON-NLS-1$
			}

			tglShow = new JToggleButton(Messages.getString("WebsiteDialog.BTN_UN_HIDE_KEY")); //$NON-NLS-1$
			tglShow.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					tfKey1.setEchoChar(tglShow.isSelected() && key1Edited ? '•' : '\0');
					tfKey2.setEchoChar(tglShow.isSelected() && key2Edited ? '•' : '\0');
				}
			});
			GroupLayout gl_buttonPane = new GroupLayout(buttonPane);
			gl_buttonPane.setHorizontalGroup(
				gl_buttonPane.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_buttonPane.createSequentialGroup()
						.addGap(6)
						.addComponent(btnGenerate)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(tglShow)
						.addPreferredGap(ComponentPlacement.RELATED, 258, Short.MAX_VALUE)
						.addComponent(btnAdd)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(btnCancel)
						.addGap(6))
			);
			gl_buttonPane.setVerticalGroup(
				gl_buttonPane.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_buttonPane.createSequentialGroup()
						.addGap(3)
						.addGroup(gl_buttonPane.createParallelGroup(Alignment.BASELINE, false)
							.addComponent(btnGenerate, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
							.addComponent(btnCancel, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
							.addComponent(btnAdd, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
							.addComponent(tglShow, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
						.addGap(3))
			);
			buttonPane.setLayout(gl_buttonPane);
			Helper.registerCancelByEsc(this, btnCancel);
			Helper.enableRtlWhenNeeded(this);
			pack();
		}
	}
}
