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
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import io.github.emmrida.chat4us.controls.IdLabelComboElement;
import io.github.emmrida.chat4us.controls.IdLabelComboModel;
import io.github.emmrida.chat4us.controls.IdLabelComboRenderer;
import io.github.emmrida.chat4us.core.ChatServer;
import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.wb.swing.FocusTraversalOnArray;
import java.awt.Component;

/**
 * The ChatServer Dialog. This dialog is used to add/edit a chat server.
 *
 * @author El Mhadder Mohamed Rida
 */
public class ChatServerDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private boolean canceled = true;
	private boolean edited = false;
	private ChatServer chatServer = null;
	private List<Integer> removedIds = new ArrayList<>();

	private final JPanel contentPanel = new JPanel();
	private JTextField tfHostIp;
	private JTextField tfServerPort;
	private JTextField tfRiaFila;
	private JTable tblAiServer;
	private JComboBox<IdLabelComboElement> cmbAIGroup;
	private JTextArea taDescription;
	private JButton btnAdd;
	private JButton btnCancel;

	/**
	 * Check if the dialog is cancelled
	 * @return true if the dialog is cancelled
	 */
	public boolean isCancelled()         { return canceled; }

	/**
	 * Check if the dialog is edited or a new chat server is added
	 * @return true if the dialog is edited, false if a new chat server is added
	 */
	public boolean isEdited()            { return edited; }

	/**
	 * Get the chat server
	 * @return the chat server
	 */
	public ChatServer getChatServer()    { return chatServer; }

	/**
	 * Get the removed ids
	 * @return the removed ids
	 */
	public List<Integer> getRemovedIds() { return removedIds; }

	/**
	 * Get the host ip
	 * @return the host ip
	 */
	public String  getHostIp()           { return canceled ? null : tfHostIp.getText().trim(); }

	/**
	 * Get the host port
	 * @return the host port
	 */
	public int     getHostPort()         { return canceled ? 0 : Integer.parseInt(tfServerPort.getText().trim()); }

	/**
	 * Get the main RIA file of the chat bot
	 * @return the ria file
	 */
	public String  getRiaFile()          { return canceled ? null : tfRiaFila.getText().trim(); }

	/**
	 * Get the ai group of the chat bot
	 * @return the ai group
	 */
	public int     getAIGroup()          { return canceled ? 0 : ((IdLabelComboElement)cmbAIGroup.getSelectedItem()).getId(); }

	/**
	 * Get the description of the chat bot
	 * @return the description
	 */
	public String  getDescription()      { return canceled ? null : taDescription.getText().trim(); }

	/**
	 * Get the ai servers count
	 * @return the ai servers count
	 */
	public int     getAiServersCount()   { return canceled ? 0 : tblAiServer.getRowCount(); }

	/**
	 * Get the ai server
	 * @param index the index of the ai server
	 * @return the ai server
	 */
	public ChatServer.AiServer getAiServer(int index) {
		if(canceled) return null;
		DefaultTableModel model = (DefaultTableModel)tblAiServer.getModel();
		return new ChatServer.AiServer((Integer)model.getValueAt(index, 0), (String)model.getValueAt(index, 1), (Boolean)model.getValueAt(index, 2));
	}

	/**
	 * Get the ai server row
	 * @param index the index of the ai server
	 * @return the ai server row
	 */
	public Object[] getAiServerRow(int index) {
		if(canceled) return null;
		DefaultTableModel model = (DefaultTableModel)tblAiServer.getModel();
		return new Object[] { (Integer)model.getValueAt(index, 0), (String)model.getValueAt(index, 1), (Integer)((Boolean)model.getValueAt(index, 2) ? 1 : 0) };
	}

	/**
	 * Fill the ai group combobox
	 */
	private void fillAiGroupCombo() {
		List<Object[]> aiGroups = MainWindow.getAIGroups();
		for(Object[] aiGroup : aiGroups)
			if((int)aiGroup[2] == 0)
				cmbAIGroup.addItem(new IdLabelComboElement((int)aiGroup[0], (String)aiGroup[1], true));
	}

	/**
	 * Validate the ai servers list
	 * @return true if the list is valid
	 */
	private boolean validateAiServersList() {
		DefaultTableModel model = (DefaultTableModel)tblAiServer.getModel();
		for(int i = 0; i < model.getRowCount(); i++) {
			String url = model.getValueAt(i, 1).toString().trim();
			if(url.length() <= 7)
				return false;
			if(!Helper.isValidURL(url))
				return false;
		}
		return true;
	}

	/**
	 * Create the dialog
	 * @param parent the parent frame
	 * @param cs the chat server to edit
	 */
	public ChatServerDialog(Frame parent, ChatServer cs) {
		this(parent);
		Objects.requireNonNull(cs);
		edited = true;
		chatServer = cs;
		setTitle(Messages.getString("ChatServerDialog.DLG_TITLE_EDIT")); //$NON-NLS-1$
		tfHostIp.setText(cs.getHost());
		tfServerPort.setText(String.valueOf(cs.getPort()));
		tfRiaFila.setText(cs.getChatClient().getChatBotClient().getRiaFileName());
		taDescription.setText(cs.getDescription());
		DefaultTableModel tblModel = (DefaultTableModel)tblAiServer.getModel();
		List<Object[]> aiServers = MainWindow.getAIServers();
		for(Object[] ais : aiServers) {
			if(((int)ais[1] == cs.getDbId()) && ((int)ais[4] == 0))
				tblModel.addRow(new Object[] { (Integer)ais[0], (String)ais[2], (int)ais[3]==0?Boolean.FALSE:Boolean.TRUE });
		}
		fillAiGroupCombo();
		Object[] aig = MainWindow.getAIGroupById(cs.getGroupId());
		if(aig != null) {
			int i = -1;
			List<Object[]> aiGroups = MainWindow.getAIGroups();
			for(Object[] aiGroup : aiGroups) {
				if(((int)aiGroup[2] == 0)) {
					i++;
					if((int)aiGroup[0] == (int)aig[0]) {
						cmbAIGroup.setSelectedIndex(i);
						break;
					}
				}
			}
		}
		btnAdd.setText(Messages.getString("ChatServerDialog.BTN_SAVE")); //$NON-NLS-1$
	}

	/**
	 * Create the dialog.
	 * @param parent the parent frame
	 * @wbp.parser.constructor
	 */
	public ChatServerDialog(Frame parent) {
		super(parent, Messages.getString("ChatServerDialog.DLG_TITLE"), true); //$NON-NLS-1$
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				if(cmbAIGroup.getItemCount() == 0)
					fillAiGroupCombo();
				Helper.addPopupMenuToTextBoxes(ChatServerDialog.this);
				TableColumn tc = tblAiServer.getColumnModel().getColumn(0);
				tc.setMinWidth(0); tc.setMaxWidth(0); tc.setPreferredWidth(0);
			}
		});
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		JLabel lblNewLabel_1 = new JLabel(Messages.getString("ChatServerDialog.LBL_HOST")); //$NON-NLS-1$

		tfHostIp = new JTextField();
		tfHostIp.setColumns(10);

		tfServerPort = new JTextField();
		tfServerPort.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel(Messages.getString("ChatServerDialog.LBL_PORT")); //$NON-NLS-1$

		JLabel lblNewLabel_5 = new JLabel(Messages.getString("ChatServerDialog.LBL_DESCRIPTION")); //$NON-NLS-1$

		JScrollPane scrollPane = new JScrollPane();

		cmbAIGroup = new JComboBox<>();
		cmbAIGroup.setModel(new IdLabelComboModel());
		cmbAIGroup.setRenderer(new IdLabelComboRenderer());

		JLabel lblNewLabel_6 = new JLabel(Messages.getString("ChatServerDialog.LBL_AIGROUP")); //$NON-NLS-1$

		JButton btnRiaBrowse = new JButton("..."); //$NON-NLS-1$
		btnRiaBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser openFileChooser = Helper.createFileChooser(Messages.getString("ChatServerDialog.OFC_TITLE"), MainWindow.CHATBOTS_ROOT_FOLDER, Messages.getString("ChatServerDialog.OFC_FILE_DESC"), "ria"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				int returnVal = openFileChooser.showOpenDialog(ChatServerDialog.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					tfRiaFila.setText(Helper.getRelativePath(openFileChooser.getSelectedFile(), new File("."))); //$NON-NLS-1$
				}
			}
		});

		tfRiaFila = new JTextField();
		tfRiaFila.setColumns(10);

		JLabel lblNewLabel_9 = new JLabel(Messages.getString("ChatServerDialog.LBL_RIA_FILE")); //$NON-NLS-1$

		JScrollPane scrollPane_1 = new JScrollPane();

		JSeparator separator = new JSeparator();

		JLabel lblNewLabel_3 = new JLabel(Messages.getString("ChatServerDialog.LBL_AISRV_TABLE")); //$NON-NLS-1$

		JButton btnNewAiServer = new JButton("+"); //$NON-NLS-1$
		btnNewAiServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel)tblAiServer.getModel();
				model.addRow(new Object[] { 0, "", Boolean.TRUE }); //$NON-NLS-1$
				tblAiServer.editCellAt(tblAiServer.getRowCount() - 1, 0);
				tblAiServer.requestFocus();
			}
		});

		JButton btnRemoveAiServer = new JButton("-"); //$NON-NLS-1$
		btnRemoveAiServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel)tblAiServer.getModel();
				int index = tblAiServer.getSelectedRow();
				if(tblAiServer.getRowCount() > 1 && index >= 0) {
					int id = (Integer)model.getValueAt(index, 0);
					if(id > 0)
						removedIds.add(id);
					model.removeRow(index);
				} else Toolkit.getDefaultToolkit().beep();
			}
		});
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(btnRemoveAiServer, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_9)
						.addComponent(lblNewLabel_5)
						.addComponent(lblNewLabel_6)
						.addComponent(lblNewLabel_2)
						.addComponent(lblNewLabel_1)
						.addComponent(lblNewLabel_3)
						.addComponent(btnNewAiServer, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))
					.addGap(6)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addComponent(separator, GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
							.addGap(6))
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addComponent(tfHostIp, GroupLayout.PREFERRED_SIZE, 164, GroupLayout.PREFERRED_SIZE)
							.addContainerGap())
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
								.addComponent(cmbAIGroup, 0, 335, Short.MAX_VALUE)
								.addGroup(gl_contentPanel.createSequentialGroup()
									.addComponent(tfRiaFila, GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnRiaBrowse))
								.addComponent(scrollPane, Alignment.LEADING)
								.addComponent(scrollPane_1, 0, 0, Short.MAX_VALUE))
							.addGap(6))
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addComponent(tfServerPort, GroupLayout.PREFERRED_SIZE, 74, GroupLayout.PREFERRED_SIZE)
							.addContainerGap())))
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_1)
						.addComponent(tfHostIp, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_2)
						.addComponent(tfServerPort, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
					.addGap(5)
					.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_6)
						.addComponent(cmbAIGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(9)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_9)
						.addComponent(btnRiaBrowse, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
						.addComponent(tfRiaFila, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING, false)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addGap(9)
							.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addGap(18)
							.addComponent(lblNewLabel_3)
							.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(btnRemoveAiServer)
							.addGap(1)
							.addComponent(btnNewAiServer)))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_5)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE))
					.addGap(6))
		);

		tblAiServer = new JTable();
		tblAiServer.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				Messages.getString("ChatServerDialog.TH_ID"), Messages.getString("ChatServerDialog.TH_URL"), Messages.getString("ChatServerDialog.TH_ACTIVE") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		) {
			private static final long serialVersionUID = 1L;
			Class[] columnTypes = new Class[] {
				Integer.class, String.class, Boolean.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		tblAiServer.getColumnModel().getColumn(0).setResizable(false);
		tblAiServer.getColumnModel().getColumn(0).setPreferredWidth(32);
		tblAiServer.getColumnModel().getColumn(1).setPreferredWidth(192);
		tblAiServer.getColumnModel().getColumn(2).setPreferredWidth(32);
		scrollPane_1.setViewportView(tblAiServer);

		taDescription = new JTextArea();
		taDescription.setLineWrap(true);
		taDescription.setWrapStyleWord(true);
		scrollPane.setViewportView(taDescription);
		contentPanel.setLayout(gl_contentPanel);
		contentPanel.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{lblNewLabel_1, tfHostIp, lblNewLabel_2, tfServerPort, separator, lblNewLabel_6, cmbAIGroup, lblNewLabel_9, tfRiaFila, btnRiaBrowse, lblNewLabel_3, scrollPane_1, tblAiServer, btnNewAiServer, btnRemoveAiServer, lblNewLabel_5, scrollPane, taDescription}));
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.TRAILING));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				btnAdd = new JButton(Messages.getString("ChatServerDialog.BTN_ADD")); //$NON-NLS-1$
				btnAdd.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(Helper.isValidIP(tfHostIp.getText().trim())) {
							if(Helper.isNumeric(tfServerPort.getText().trim())) {
								if(new File(tfRiaFila.getText().trim()).exists()) {
									if(validateAiServersList()) {
										canceled = false;
										setVisible(false);
										return;
									}
								}
							}
						}
						JOptionPane.showMessageDialog(ChatServerDialog.this, Messages.getString("ChatServerDialog.MB_ERROR_MSG"), Messages.getString("ChatServerDialog.MB_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					}
				});
				btnAdd.setActionCommand("OK"); //$NON-NLS-1$
				buttonPane.add(btnAdd);
				getRootPane().setDefaultButton(btnAdd);
			}
			{
				btnCancel = new JButton(Messages.getString("ChatServerDialog.BTN_CANCEL")); //$NON-NLS-1$
				btnCancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						canceled = true;
						setVisible(false);
					}
				});
				btnCancel.setActionCommand("Cancel"); //$NON-NLS-1$
				buttonPane.add(btnCancel);
			}
		}
		Helper.registerCancelByEsc(this, btnCancel);
		Helper.enableRtlWhenNeeded(this);
		pack();
	}
}
