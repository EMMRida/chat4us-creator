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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import io.github.emmrida.chat4usagent.util.Helper;
import io.github.emmrida.chat4usagent.util.Messages;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.awt.event.ActionEvent;

/**
 * Models dialog. This dialog is used to create a new model for messages that are used frequently
 * by the agent when responding to users.
 *
 * @author El Mhadder Mohamed Rida
 */
public class ModelsDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private boolean cancelled = true;

	public boolean isCancelled() { return cancelled; }
	public String getTitle() { return tfTitle.getText(); }
	public String getContent() { return taContent.getText(); }

	private final JPanel contentPanel = new JPanel();
	private JTextField tfTitle;
	private JButton okButton;
	private JTextArea taContent;

	/**
	 * Create the dialog.
	 * @param title The title of the model
	 * @param content The content of the model. Agent can use variables to let them fill it with misc information
	 *        at the moment of sending the message. Ex: @S:Text or @I:Number
	 */
	public ModelsDialog(Frame parent, String title, String content) {
		this(parent);
		Objects.requireNonNull(title);
		Objects.requireNonNull(content);
		tfTitle.setText(title);
		taContent.setText(content);
		okButton.setText(Messages.getString("ModelsDialog.BTN_SAVE")); //$NON-NLS-1$
	}

	/**
	 * Create the dialog.
	 * @wbp.parser.constructor
	 */
	public ModelsDialog(Frame parent) {
		super(parent, Messages.getString("ModelsDialog.MODELSDLG_TITLE"), true); //$NON-NLS-1$
		setResizable(false);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		tfTitle = new JTextField();
		tfTitle.setColumns(10);
		tfTitle.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) { checkContent(); }
			@Override
			public void removeUpdate(DocumentEvent e) { checkContent(); }
			@Override
			public void changedUpdate(DocumentEvent e) { checkContent(); }
			private void checkContent() {
				okButton.setEnabled(tfTitle.getText().trim().length() > 2 && taContent.getText().trim().length() > 2);
				setTitle(tfTitle.getText() + Messages.getString("ModelsDialog.MODELSDLG_TITLE_SUFFIX")); //$NON-NLS-1$
			}
		});

		JLabel lblNewLabel = new JLabel(Messages.getString("ModelsDialog.LBL_MODEL_TITLE")); //$NON-NLS-1$

		JScrollPane scrollPane = new JScrollPane();

		JLabel lblNewLabel_1 = new JLabel(Messages.getString("ModelsDialog.LBL_MODEL_CONTENT")); //$NON-NLS-1$
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGap(16)
					.addComponent(lblNewLabel_1)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE))
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGap(35)
					.addComponent(lblNewLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(tfTitle, GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE))
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(tfTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
						.addComponent(lblNewLabel_1)))
		);

		taContent = new JTextArea();
		taContent.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) { checkContent(); }
			@Override
			public void removeUpdate(DocumentEvent e) { checkContent(); }
			@Override
			public void changedUpdate(DocumentEvent e) { checkContent(); }
			private void checkContent() {
				okButton.setEnabled(taContent.getText().trim().length() > 2 && tfTitle.getText().trim().length() > 2);
				setTitle(tfTitle.getText() + Messages.getString("ModelsDialog.MODELSDLG_TITLE_SUFFIX")); //$NON-NLS-1$
			}
		});
		scrollPane.setViewportView(taContent);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.TRAILING));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton(Messages.getString("ModelsDialog.BTN_ADD")); //$NON-NLS-1$
				okButton.setEnabled(false);
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
				JButton cancelButton = new JButton(Messages.getString("ModelsDialog.BTN_CANCEL")); //$NON-NLS-1$
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
