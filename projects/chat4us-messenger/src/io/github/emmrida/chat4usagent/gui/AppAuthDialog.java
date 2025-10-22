/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import io.github.emmrida.chat4usagent.util.Helper;
import io.github.emmrida.chat4usagent.util.Messages;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * The application authentication dialog. This dialog is used to authenticate the user to access the application.
 *
 * @author El Mhadder Mohamed Rida
 */
public class AppAuthDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private boolean cancelled  = true;

	private final JPanel contentPanel = new JPanel();
	private JPasswordField tfPassword;

	public boolean isCancelled() { return cancelled; }
	public char[] getPassword() { return tfPassword.getPassword(); }

	/**
	 * Create the dialog.
	 */
	public AppAuthDialog(Frame parent) {
		super(parent, Messages.getString("AppAuthDialog.DLG_TITLE"), true); //$NON-NLS-1$
		setResizable(false);
		//setBounds(100, 100, 360, 140);
		SwingUtilities.invokeLater(() -> {
			if(!parent.isVisible() || parent.getState() == Frame.ICONIFIED)
				setLocationRelativeTo(null);
		});
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		JLabel lblNewLabel = new JLabel(Messages.getString("AppAuthDialog.LBL_PASSWORD")); //$NON-NLS-1$
		tfPassword = new JPasswordField();
		tfPassword.setColumns(10);
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(tfPassword, GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
						.addComponent(lblNewLabel))
					.addContainerGap())
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblNewLabel)
					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(tfPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.TRAILING));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(Messages.getString("AppAuthDialog.BTN_OK")); //$NON-NLS-1$
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(getPassword().length > 8) {
							cancelled = false;
							setVisible(false);
							return;
						}
						JOptionPane.showMessageDialog(AppAuthDialog.this, Messages.getString("AppAuthDialog.APP_AUTH_DLG_MESSAGE"), Messages.getString("AppAuthDialog.DLG_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					}
				});
				okButton.setActionCommand("OK"); //$NON-NLS-1$
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton(Messages.getString("AppAuthDialog.BTN_CANCEL")); //$NON-NLS-1$
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
}
