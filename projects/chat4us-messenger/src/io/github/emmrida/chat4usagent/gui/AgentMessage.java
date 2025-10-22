/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.gui;

import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import io.github.emmrida.chat4usagent.util.Helper;

import javax.swing.JLabel;
import javax.swing.JEditorPane;
import javax.swing.SwingConstants;

import java.awt.Color;

/**
 * Panel for agent messages that appears inside the chat window
 *
 * @author El Mhadder Mohamed Rida
 */
public class AgentMessage extends JPanel implements IMessagePanel {
	private static final long serialVersionUID = 3317521967808457428L;
	private JEditorPane epUserMsg;
	private JLabel lblMsgTime;

	/** @see IMessagePanel#setMessage(String, long) **/
	@Override
	public void setMessage(String msg, long time) {
		Helper.setupEditorPane(epUserMsg);
		String css = MainWindow.getSettings().getCss();
		String html = "<html><head><style type=\"text/css\">" + css + "</style></head><body>" + msg + "</body></html>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		epUserMsg.setText(html);
		lblMsgTime.setText(Helper.getLocalTime(time));
	}

	/** @see IMessagePanel#onShown() **/
	@Override
	public void onShown() {}

	/**
	 * Init the panel
	 */
	public AgentMessage() {
		setBackground(new Color(255, 255, 255));
		JPanel panel = new JPanel();
		panel.setBackground(new Color(245, 245, 245));
		JLabel lblUserIcon = new JLabel(""); //$NON-NLS-1$
		lblUserIcon.setIcon(Helper.loadIconFromResources("/agent.png", 48, 48)); //$NON-NLS-1$
		epUserMsg = new JEditorPane();
		epUserMsg.setBackground(new Color(255, 255, 255));
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
						.addComponent(lblMsgTime, GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
						.addComponent(epUserMsg, GroupLayout.PREFERRED_SIZE, 133, Short.MAX_VALUE))
					.addGap(3))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(3)
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(lblUserIcon, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
							.addContainerGap())
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(epUserMsg, GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
							.addGap(3)
							.addComponent(lblMsgTime)
							.addGap(3))))
		);
		panel.setLayout(gl_panel);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(5)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 185, Short.MAX_VALUE)
					.addGap(50))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(5)
					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
					.addGap(5))
		);
		setLayout(groupLayout);
		Helper.enableRtlWhenNeeded(this);
	}
}
