/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.gui;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import io.github.emmrida.chat4usagent.util.Helper;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import java.awt.Color;
import java.awt.Dimension;

/**
 * Panel for user messages that appears inside the chat window.
 *
 * @author El Mhadder Mohamed Rida
 */
public class UserMessage extends JPanel implements IMessagePanel {
	private static final long serialVersionUID = 3317521967808457428L;
	private JEditorPane epUserMsg;
	private JLabel lblMsgTime;

	/** @see IMessagePanel#setMessage(String, long) */
	@Override
	public void setMessage(String msg, long time) {
		Helper.setupEditorPane(epUserMsg);
		epUserMsg.setText(msg);
		lblMsgTime.setText(Helper.getLocalTime(time));
	}

	/** @see IMessagePanel#onShown() */
	@Override
	public void onShown() { }

	/**
	 * Constructs the user message panel
	 */
	public UserMessage() {
		setBackground(new Color(255, 255, 255));
		setMaximumSize(new Dimension(32767, 120));
		JPanel panel = new JPanel();
		panel.setBackground(new Color(232, 243, 255));

				JLabel lblUserIcon = new JLabel(""); //$NON-NLS-1$
				lblUserIcon.setIcon(Helper.loadIconFromResources("/person.png", 48, 48)); //$NON-NLS-1$
				epUserMsg = new JEditorPane();
				epUserMsg.setBackground(new Color(244, 250, 255));
				epUserMsg.setEditable(false);

						lblMsgTime = new JLabel("00:00"); //$NON-NLS-1$
						lblMsgTime.setHorizontalAlignment(SwingConstants.TRAILING);
						GroupLayout gl_panel = new GroupLayout(panel);
						gl_panel.setHorizontalGroup(
							gl_panel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel.createSequentialGroup()
									.addGap(3)
									.addComponent(lblUserIcon)
									.addGap(3)
									.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_panel.createSequentialGroup()
											.addComponent(lblMsgTime, GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
											.addGap(3))
										.addGroup(gl_panel.createSequentialGroup()
											.addComponent(epUserMsg, GroupLayout.PREFERRED_SIZE, 122, Short.MAX_VALUE)
											.addGap(3))))
						);
						gl_panel.setVerticalGroup(
							gl_panel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel.createSequentialGroup()
									.addGap(3)
									.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_panel.createSequentialGroup()
											.addComponent(lblUserIcon, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
											.addContainerGap())
										.addGroup(Alignment.TRAILING, gl_panel.createSequentialGroup()
											.addComponent(epUserMsg, GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
											.addGap(3)
											.addComponent(lblMsgTime)
											.addGap(3))))
						);
						panel.setLayout(gl_panel);
						GroupLayout groupLayout = new GroupLayout(this);
						groupLayout.setHorizontalGroup(
							groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(50)
									.addComponent(panel, GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
									.addGap(5))
						);
						groupLayout.setVerticalGroup(
							groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(5)
									.addComponent(panel, GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
									.addGap(5))
						);
						setLayout(groupLayout);
	}
}
