/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import io.github.emmrida.chat4usagent.util.Helper;
import io.github.emmrida.chat4usagent.util.Messages;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Set chat messages CSS styles for a better look and feel.
 *
 * @author El Mhadder Mohamed Rida
 */
public class MsgStylesDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private boolean cancelled = false;

	private final JPanel contentPanel = new JPanel();
	private JTextArea textArea;

	public boolean isCancelled() { return cancelled; }
	public String getCss() { return textArea.getText(); }
	public void setCss(String styles) { textArea.setText(styles); }

	/**
	 * Create the dialog.
	 */
	public MsgStylesDialog(JFrame thisFrame) {
		super(thisFrame);
		setModal(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle(Messages.getString("MsgStylesDialog.DLG_TITLE")); //$NON-NLS-1$
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			{
				textArea = new JTextArea();
				scrollPane.setViewportView(textArea);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(Messages.getString("MsgStylesDialog.BTN_SAVE")); //$NON-NLS-1$
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelled = false;
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK"); //$NON-NLS-1$
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton(Messages.getString("MsgStylesDialog.BTN_CANCEL")); //$NON-NLS-1$
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
		setBounds(0, 0, 320, 240);
	}
}
