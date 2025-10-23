/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.controls;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import io.github.emmrida.chat4us.core.ChatServer;
import io.github.emmrida.chat4us.core.ChatServer.ChatServerListener;
import io.github.emmrida.chat4us.core.ChatSession.ChatSessionState;
import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;

/**
 * Chat server cells renderer class
 *
 * @author El Mhadder Mohamed Rida
 */
public class ChatServerListCellRenderer extends JPanel implements ListCellRenderer<ChatServer> {
	private static final long serialVersionUID = -923056119820651352L;

	private static BufferedImage imgSnooze = null;
	private static BufferedImage imgChatBot = null;
	private static BufferedImage imgAi = null;
	private static BufferedImage imgAgent = null;
	private static BufferedImage imgWebsite = null;
	private static BufferedImage imgDisabled = null;

	private BufferedImage curImage = null;

	private int index;
	private boolean selected;
	private boolean hasFocus;
	private ChatServer chatServer; // item value
	private JList<ChatServer> parentList;

	private long lastTime = 0;

	/**
	 * Init ChatServerListCellRenderer instance
	 */
	public ChatServerListCellRenderer() {
		if(imgSnooze == null) {
			try {
				imgSnooze = ImageIO.read(getClass().getResource("/snooze.png")); //$NON-NLS-1$
				imgChatBot = ImageIO.read(getClass().getResource("/chatbot.png")); //$NON-NLS-1$
				imgAi = ImageIO.read(getClass().getResource("/ai_assistant.png")); //$NON-NLS-1$
				imgAgent = ImageIO.read(getClass().getResource("/agent.png")); //$NON-NLS-1$
				imgDisabled = ImageIO.read(getClass().getResource("/link_off.png")); //$NON-NLS-1$
				imgWebsite = ImageIO.read(getClass().getResource("/web.png")); //$NON-NLS-1$
				this.curImage = imgSnooze;
			} catch (IOException ex) {
				Helper.logError(ex, Messages.getString("ChatServerListCellRenderer.ERROR_LOADING_ICONS"), true); //$NON-NLS-1$
			}
		}
		setPreferredSize(new Dimension(getWidth(), imgSnooze.getHeight()));
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends ChatServer> list, ChatServer value, int index, boolean isSelected, boolean cellHasFocus) {
		this.index = index;
		this.selected = isSelected;
		this.hasFocus = cellHasFocus;
		this.chatServer = value;
		this.parentList = (JList<ChatServer>)list;
		if(value != null && !value.equals(this.chatServer)) {
			this.chatServer.addChatServerListener(new ChatServerListener() {
				@Override
				public void onActivityStateChanged(ChatServer server, ChatSessionState state) {
					switch(state) {
						case CHATBOT: curImage = imgChatBot; break;
						case AIMODEL: curImage = imgAi;      break;
						case AGENT:   curImage = imgAgent;   break;
						case WEBSITE: curImage = imgWebsite; break;
					}
					lastTime = System.currentTimeMillis();
					list.repaint();
				}

				@Override
				public void onStatsChanged(ChatServer server) { }
			});
		}
		return this;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(this.chatServer == null)
			return;
		int w = getWidth();
		int h = getHeight();
		Font font = this.parentList.getFont();
		if(this.selected) {
			g.setColor(this.parentList.getSelectionBackground());
			g.fillRect(0, 0, w, h);
		}
		if(!chatServer.isEnabled()) {
			g.drawImage(imgDisabled, 0, 0, imgDisabled.getWidth(), imgDisabled.getHeight(), null);
		} else {
			if(!curImage.equals(imgSnooze))
				if(System.currentTimeMillis() - lastTime >= 1000)
					curImage = imgSnooze;
			g.drawImage(curImage, 0, 0, curImage.getWidth(), curImage.getHeight(), null);
		}
		g.setColor(this.parentList.getForeground());
		g.setFont(font);
		g.drawString(this.chatServer.getName(), imgSnooze.getWidth()+5, font.getSize()+10);
		g.drawString(this.chatServer.getChatClient().getChatBotClient().getRiaFileName(), imgSnooze.getWidth()+5, h - (font.getSize()+5));
		if(this.hasFocus) {
			g.setColor(this.parentList.getSelectionForeground());
			g.drawRect(0, 0, w-1, h-1);
		}
	}
}
