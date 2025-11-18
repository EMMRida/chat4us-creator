/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.util;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.awt.Color;
import java.awt.Frame;
import java.awt.SystemColor;
import javax.swing.UIManager;
import java.awt.event.ActionListener;
import java.net.URLEncoder;
import java.awt.event.ActionEvent;

/**
 * Show an unhandled exception in a dialog box with its stack trace.
 *
 * @author El Mhadder Mohamed Rida
 */
public class UExceptionDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private StyledDocument styledDoc;

	private UExceptionDialog thisDialog;
	private JPanel contentPane;
	private JTextArea taInfo;
	private JTextPane taStackTrace;

	/**
	 * Create the frame.
	 */
	public UExceptionDialog(Frame parent, Thread t, Throwable e) {
		super(parent, Messages.getString("UExceptionDialog.DLG_TITLE"), true); //$NON-NLS-1$
		thisDialog = this;
		setSize(600, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);

		JButton btnReport = new JButton(Messages.getString("UExceptionDialog.BTN_SEND")); //$NON-NLS-1$
		btnReport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Helper.openMail("mailto:nospam.rida@gmail.com" + //$NON-NLS-1$
 					   			   			"?subject=" + URLEncoder.encode(Messages.getString("UExceptionDialog.EX_REPORT_EMAIL_SUBJECT"), "UTF-8") + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 					   			   				"&body=" + URLEncoder.encode(taInfo.getText() + "\n\n" + taStackTrace.getText(), "UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} catch (Exception ex) {
					Helper.logWarning(ex, Messages.getString("UExceptionDialog.LOG_ERR_EX_REPORTING"), true); //$NON-NLS-1$
				}
			}
		});
		btnReport.setFocusable(false);

		JButton btnClose = new JButton(Messages.getString("UExceptionDialog.BTN_CLOSE")); //$NON-NLS-1$
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		btnClose.setFocusable(false);

		taInfo = new JTextArea();
		taInfo.setFont(UIManager.getFont("Label.font")); //$NON-NLS-1$
		taInfo.setFocusable(false);
		taInfo.setWrapStyleWord(true);
		taInfo.setLineWrap(true);
		taInfo.setBackground(SystemColor.control);
		taInfo.setEditable(false);

		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addComponent(scrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(taInfo, GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
								.addComponent(btnClose, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(btnReport, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
					.addGap(1))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(btnReport)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnClose))
						.addComponent(taInfo, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE))
		);

		taStackTrace = new JTextPane();
		taStackTrace.setFont(UIManager.getFont("Label.font")); //$NON-NLS-1$
		taStackTrace.setEditable(false);
		scrollPane.setViewportView(taStackTrace);
		contentPane.setLayout(gl_contentPane);

		Helper.registerCancelByEsc(thisDialog, btnClose);
		Helper.enableRtlWhenNeeded(this);

		/*
		 *
		 */

		try {
			int start;
			int end;
			String el;
			taInfo.setText(Messages.getString("UExceptionDialog.DLG_MESSAGE")); //$NON-NLS-1$
			styledDoc = taStackTrace.getStyledDocument();
			Style style = styledDoc.addStyle("AppLines", null); //$NON-NLS-1$
			StyleConstants.setForeground(style, Color.RED);
			taStackTrace.setText(String.format(Messages.getString("UExceptionDialog.EX_STACK_TRACE_HEADER"), e.getClass().getSimpleName(), t.getName(), e.getLocalizedMessage())); //$NON-NLS-1$
			for(StackTraceElement ste : e.getStackTrace()) {
				start = taStackTrace.getCaretPosition();
				el = ste.toString();
				styledDoc.insertString(styledDoc.getLength(), el + System.lineSeparator(), null);
				end = taStackTrace.getCaretPosition();
				if(!el.startsWith("java")) //$NON-NLS-1$
					styledDoc.setCharacterAttributes(start, end-start, style, false);
			}
			taStackTrace.setCaretPosition(0);
		} catch (Exception ex) {
			Helper.logError(ex, Messages.getString("UExceptionDialog.ERROR_UEX_REPORT"), true); //$NON-NLS-1$
		}
	}
}
