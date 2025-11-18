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

import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.JPasswordField;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.SystemColor;
import javax.swing.UIManager;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The application authentication dialog. This dialog is used to authenticate the user to access the application.
 * User must enter the password of the app key store to access the app or add/edit a chat server.
 *
 * @author El Mhadder Mohamed Rida
 */
public class AppAuthDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private boolean cancelled  = true;
    private Timer timer;

	private final JPanel contentPanel = new JPanel();
	private JPasswordField tfPassword;
	private JButton cancelButton;
	private JLabel lblXXXLock;

	public boolean isCancelled() { return cancelled; }
	public char[] getPassword() { return tfPassword.getPassword(); }

    public static boolean isNumLockOn() {
        return Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);
    }

    public static boolean isCapsLockOn() {
        return Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
    }

	/**
	 * Create the dialog.
	 * @param parent the parent frame
	 */
	public AppAuthDialog(Frame parent) {
		super(parent, Messages.getString("AppAuthDialog.DLG_TITLE"), true); //$NON-NLS-1$
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				timer.stop();
			}
		});
		setResizable(false);
		SwingUtilities.invokeLater(() -> {
			if(!parent.isVisible() || parent.getState() == Frame.ICONIFIED)
				setLocationRelativeTo(null);
		});
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		JLabel lblNewLabel = new JLabel(Messages.getString("AppAuthDialog.LBL_PSWD")); //$NON-NLS-1$
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
					.addComponent(tfPassword, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.TRAILING));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(Messages.getString("AppAuthDialog.LBL_OK")); //$NON-NLS-1$
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(getPassword().length > 8) {
							cancelled = false;
							setVisible(false);
							return;
						}
						JOptionPane.showMessageDialog(AppAuthDialog.this, Messages.getString("AppAuthDialog.MB_WRONG_PSWD_MSG"), Messages.getString("AppAuthDialog.MB_WRONG_PSWD_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					}
				});
				{
					lblXXXLock = new JLabel((String) null); //$NON-NLS-1$
					lblXXXLock.setVisible(false);
					lblXXXLock.setBorder(new EmptyBorder(2, 5, 2, 5));
					lblXXXLock.setFont(new Font("Tahoma", Font.BOLD, 13));
					lblXXXLock.setForeground(UIManager.getColor("ToolBar.dockingForeground"));
					lblXXXLock.setFocusable(false);
					lblXXXLock.setOpaque(true);
					lblXXXLock.setBackground(SystemColor.info);
					buttonPane.add(lblXXXLock);
				}
				okButton.setActionCommand("OK"); //$NON-NLS-1$
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton(Messages.getString("AppAuthDialog.BTN_CANCEL")); //$NON-NLS-1$
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelled = true;
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel"); //$NON-NLS-1$
				buttonPane.add(cancelButton);
			}
			Helper.registerCancelByEsc(this, cancelButton);
			Helper.enableRtlWhenNeeded(this);

			timer = new Timer(500, e -> {
    			String warn = isCapsLockOn()||!isNumLockOn() ? Messages.getString("AppAuthDialog.KEYBOARD_WARNING") : ""; //$NON-NLS-1$
    			lblXXXLock.setText(warn);
    			lblXXXLock.setVisible(!warn.isEmpty());
			});
			timer.start();
			pack();
		}
	}
}
