/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import io.github.emmrida.chat4us.controls.IdLabelComboElement;
import io.github.emmrida.chat4us.controls.IdLabelComboModel;
import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JSeparator;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;

/**
 * The Class AgentDialog. Dialog to add or edit an agent and his/her messenger app in the database.
 *
 * @author El Mhadder Mohamed Rida
 */
public class AgentDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private int dbId;
	private boolean cancelled = true;
	private boolean edited = false;

	private final JPanel contentPanel = new JPanel();
	private JTextField tfName;
	private JTextField tfHost;
	private JTextField tfPort;
	private JButton btnAdd;
	private JButton btnCancel;
	private JComboBox<IdLabelComboElement> cmbPoste;
	private JTextArea taDescription;
	private JComboBox<IdLabelComboElement> cmbAiGroup;

	public boolean isCancelled()            { return cancelled; }
	public boolean isEdited()               { return edited; }
	public int getDbId()                    { return cancelled ? -1 : dbId; }
	public String getName()                 { return cancelled ? null : tfName.getText().trim(); }
	public String getHost()                 { return cancelled ? null : tfHost.getText().trim(); }
	public int getPort()                    { return cancelled ? -1 : Integer.parseInt(tfPort.getText()); }
	public String getDescription()          { return cancelled ? null : taDescription.getText().trim(); }
	public IdLabelComboElement getPoste()   { return cancelled ? null : (IdLabelComboElement)cmbPoste.getSelectedItem(); }
	public IdLabelComboElement getAiGroup() { return cancelled ? null : (IdLabelComboElement)cmbAiGroup.getSelectedItem(); }

	/**
	 * Instantiates a new agent dialog.
	 * @param parent The parent frame
	 * @param dbId The agent id on database
	 * @param edit True to edit the agent, false to add a new one.
	 */
	public AgentDialog(Frame parent, int dbId, boolean edit) {
		this(parent);
		this.dbId = dbId;
		this.edited = edit;
		if(edit) setTitle(Messages.getString("AgentDialog.DLG_TITLE_EDIT")); //$NON-NLS-1$

		IdLabelComboModel agpModel = loadAgentPositions();
		IdLabelComboModel aigModel = loadAgentAiGroups();

		if(edit) {
			if(this.dbId <= 0)
				throw new IllegalArgumentException();
			btnAdd.setText(Messages.getString("AgentDialog.BTN_SAVE")); //$NON-NLS-1$
			try(PreparedStatement ps = MainWindow.getDBConnection().prepareStatement("SELECT * FROM agents WHERE id = ?;")) { //$NON-NLS-1$
				ps.setInt(1, this.dbId);
				try(ResultSet rs = ps.executeQuery()) {
					if(rs.next()) {
						tfName.setText(rs.getString("name")); //$NON-NLS-1$
						tfHost.setText(rs.getString("host")); //$NON-NLS-1$
						tfPort.setText(String.valueOf(rs.getInt("port"))); //$NON-NLS-1$
						taDescription.setText(rs.getString("description")); //$NON-NLS-1$
						Object[] agp = MainWindow.getAgentPosteById(rs.getInt("poste_id")); //$NON-NLS-1$
						if(agp != null) {
							for(int i = 0; i < agpModel.getSize(); i++) {
								if((int)agp[0] == agpModel.getElementAt(i).getId()) {
									cmbPoste.setSelectedIndex(i);
									break;
								}
							}
						}
						Object[] aig = MainWindow.getAIGroupById(rs.getInt("ai_group_id")); //$NON-NLS-1$
						if(aig != null) {
							for(int i = 0; i < aigModel.getSize(); i++) {
								if((int)aig[0] == aigModel.getElementAt(i).getId()) {
									cmbAiGroup.setSelectedIndex(i);
									break;
								}
							}
						}
					} else {
						Helper.logError(String.format(Messages.getString("AgentDialog.AGENT_NOT_FOUND"), this.dbId), true); //$NON-NLS-1$
						dispose();
						return;
					}
				} catch (SQLException ex) {
					Helper.logError(ex, String.format(Messages.getString("AgentDialog.ERROR_LOADING_AGENT"), this.dbId), true); //$NON-NLS-1$
					dispose();
					return;
				}
			} catch (SQLException ex) {
				Helper.logError(ex, String.format(Messages.getString("AgentDialog.ERROR_LOADING_AGENT"), this.dbId), true); //$NON-NLS-1$
				dispose();
				return;
			}
		}
	}

	/**
	 *
	 * @return
	 */
	private IdLabelComboModel loadAgentAiGroups() {
		IdLabelComboModel aigModel = new IdLabelComboModel();
		cmbAiGroup.setModel(aigModel);
		List<Object[]> aiGroups = MainWindow.getAIGroups();
		for(Object[] aig : aiGroups)
			if((int)aig[2] == 0) // Not removed
				cmbAiGroup.addItem(new IdLabelComboElement((int)aig[0], (String)aig[1], true));
		return aigModel;
	}

	/**
	 *
	 * @return
	 */
	private IdLabelComboModel loadAgentPositions() {
		IdLabelComboModel agpModel = new IdLabelComboModel();
		cmbPoste.setModel(agpModel);
		List<Object[]> postes = MainWindow.getAgentsPostes();
		for(Object[] agp : postes)
			if((int)agp[2] == 0) // Not removed
				cmbPoste.addItem(new IdLabelComboElement((int)agp[0], (String)agp[1], true));
		return agpModel;
	}

	/**
	 * Create the dialog.
	 * @param parent The parent frame
	 * @wbp.parser.constructor
	 */
	public AgentDialog(Frame parent) {
		super(parent, Messages.getString("AgentDialog.DLG_TITLE"), true); //$NON-NLS-1$
		//setTitle(Messages.getString("AgentDialog.DLG_TITLE")); //$NON-NLS-1$
		setResizable(false);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				Helper.addPopupMenuToTextBoxes(AgentDialog.this);
			}
		});
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		tfName = new JTextField();
		tfName.setColumns(10);
		JLabel lblNewLabel = new JLabel(Messages.getString("AgentDialog.LBL_NAME")); //$NON-NLS-1$
		JLabel lblNewLabel_1 = new JLabel(Messages.getString("AgentDialog.LBL_POSTE")); //$NON-NLS-1$
		JSeparator separator = new JSeparator();
		tfHost = new JTextField();
		tfHost.setColumns(10);
		JLabel lblNewLabel_2 = new JLabel(Messages.getString("AgentDialog.LBL_HOST")); //$NON-NLS-1$
		tfPort = new JTextField();
		tfPort.setColumns(10);
		JLabel lblNewLabel_3 = new JLabel(Messages.getString("AgentDialog.LBL_PORT")); //$NON-NLS-1$
		cmbPoste = new JComboBox<>();
		cmbPoste.setModel(loadAgentPositions());

		JSeparator separator_1 = new JSeparator();

		JScrollPane scrollPane = new JScrollPane();

		JLabel lblNewLabel_5 = new JLabel(Messages.getString("AgentDialog.LBL_DESCRIPTION")); //$NON-NLS-1$

		cmbAiGroup = new JComboBox<>();
		cmbAiGroup.setModel(loadAgentAiGroups());

		JLabel lblNewLabel_4 = new JLabel(Messages.getString("AgentDialog.LBL_AI_GROUP")); //$NON-NLS-1$
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblNewLabel_2)
						.addComponent(lblNewLabel_3)
						.addComponent(lblNewLabel_5)
						.addComponent(lblNewLabel)
						.addComponent(lblNewLabel_1)
						.addComponent(lblNewLabel_4))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(tfName, GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
						.addComponent(cmbPoste, 0, 375, Short.MAX_VALUE)
						.addComponent(cmbAiGroup, Alignment.TRAILING, 0, 375, Short.MAX_VALUE)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
						.addComponent(separator, GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
						.addComponent(separator_1, GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
						.addComponent(tfPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(tfHost, GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE))
					.addGap(6))
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfName, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(cmbPoste, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_1))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(cmbAiGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_4))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(separator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfHost, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_2))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfPort, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_3))
					.addGap(6)
					.addComponent(separator_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
						.addComponent(lblNewLabel_5))
					.addGap(6))
		);

		taDescription = new JTextArea();
		scrollPane.setViewportView(taDescription);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				btnAdd = new JButton(Messages.getString("AgentDialog.BTN_ADD")); //$NON-NLS-1$
				btnAdd.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(Helper.isValidIP(tfHost.getText())) {
							if(Helper.isNumeric(tfPort.getText())) {
								if(tfName.getText().trim().length() > 2) {
									cancelled = false;
									setVisible(false);
									return;
								}
							}
						}
						Helper.logWarning(Messages.getString("AgentDialog.MB_ERROR_MSG"), true); //$NON-NLS-1$
					}
				});
				btnAdd.setActionCommand("OK"); //$NON-NLS-1$
				getRootPane().setDefaultButton(btnAdd);
			}
			{
				btnCancel = new JButton(Messages.getString("AgentDialog.BTN_CANCEL")); //$NON-NLS-1$
				btnCancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelled = true;
						setVisible(false);
					}
				});
				btnCancel.setActionCommand("Cancel"); //$NON-NLS-1$
			}
			GroupLayout gl_buttonPane = new GroupLayout(buttonPane);
			gl_buttonPane.setHorizontalGroup(
				gl_buttonPane.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_buttonPane.createSequentialGroup()
						.addGap(334)
						.addComponent(btnAdd)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(btnCancel)
						.addGap(6))
			);
			gl_buttonPane.setVerticalGroup(
				gl_buttonPane.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_buttonPane.createSequentialGroup()
						.addGap(3)
						.addGroup(gl_buttonPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(btnAdd)
							.addComponent(btnCancel))
						.addGap(3))
			);
			buttonPane.setLayout(gl_buttonPane);
			Helper.registerCancelByEsc(this, btnCancel);
			Helper.enableRtlWhenNeeded(this);
			pack();
		}
	}
}
