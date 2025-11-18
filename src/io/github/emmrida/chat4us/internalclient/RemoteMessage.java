/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.internalclient;

import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import javax.swing.JLabel;
import javax.swing.JEditorPane;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import io.github.emmrida.chat4us.util.Helper;

import java.awt.Color;
import java.awt.event.MouseAdapter;

/**
 * Panel for agent messages that appears inside the chat window
 */
public class RemoteMessage extends JPanel implements IMessagePanel {
	private static final long serialVersionUID = 3317521967808457428L;
	private JEditorPane epUserMsg;
	private JLabel lblMsgTime;
	private JLabel lblUserIcon;

	/** @see IMessagePanel#setMessage(String, long) **/
	@Override
	public void setMessage(String msg, long time) {
		Helper.setupEditorPane(epUserMsg);
		String css = "body { font-family: Arial, sans-serif; } a.btn_snd { text-decoration: none; background-color: #e1e1e1; font-weight:bold; word-wrap:none; }";
		String html = "<html><head><style type=\"text/css\">" + css + "</style></head><body>" + msg + "</body></html>";
		epUserMsg.setText(html);
		lblMsgTime.setText(Helper.getLocalTime(time));
	}

	/** @see IMessagePanel#setIcon(String) */
	@Override
	public void setIcon(String icon) {
		lblUserIcon.setIcon(Helper.loadIconFromResources(icon, 48, 48));
	}

	/** @see IMessagePanel#onShown() **/
	@Override
	public void onShown() {}

	/**
	 * Init the panel
	 */
	public RemoteMessage() {
		setBackground(new Color(255, 255, 255));
		JPanel panel = new JPanel();
		panel.setBackground(new Color(245, 245, 245));
		lblUserIcon = new JLabel("");
		lblUserIcon.setIcon(Helper.loadIconFromResources("/chatbot.png", 48, 48));
		epUserMsg = new JEditorPane();
		epUserMsg.setContentType("text/html");
		epUserMsg.setBackground(new Color(255, 255, 255));
		epUserMsg.setEditable(false);
		lblMsgTime = new JLabel("00:00");
		lblMsgTime.setHorizontalAlignment(SwingConstants.TRAILING);
		epUserMsg.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				int pos = epUserMsg.viewToModel2D(evt.getPoint());
				if(pos >= 0) {
					try {
						HTMLDocument doc = (HTMLDocument)epUserMsg.getDocument();
						Element el = doc.getCharacterElement(pos);
						AttributeSet ats = el.getAttributes();
						SimpleAttributeSet obj = (SimpleAttributeSet) ats.getAttribute(HTML.Tag.A);
						String id = obj.getAttribute(HTML.Attribute.ID).toString();
						InternalClientFrame.onRemoteMsgLinkClicked(id);
					} catch (Exception e) {}
				}
			}
		});
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
