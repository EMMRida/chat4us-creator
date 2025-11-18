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
import java.util.Map;
import java.util.WeakHashMap;

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

	/** Current UI state */
	private ChatServerUI curUI;
	/** UI state for each ChatServer */
	private Map<ChatServer, ChatServerUI> uiMap;

	/**
	 * Init ChatServerListCellRenderer instance
	 */
	public ChatServerListCellRenderer() {
		uiMap = new WeakHashMap<ChatServer, ChatServerUI>();
		if(imgSnooze == null) {
			try {
				imgSnooze = ImageIO.read(getClass().getResource("/snooze.png")); //$NON-NLS-1$
				imgChatBot = ImageIO.read(getClass().getResource("/chatbot.png")); //$NON-NLS-1$
				imgAi = ImageIO.read(getClass().getResource("/ai_assistant.png")); //$NON-NLS-1$
				imgAgent = ImageIO.read(getClass().getResource("/agent.png")); //$NON-NLS-1$
				imgDisabled = ImageIO.read(getClass().getResource("/link_off.png")); //$NON-NLS-1$
				imgWebsite = ImageIO.read(getClass().getResource("/web.png")); //$NON-NLS-1$
			} catch (IOException ex) {
				Helper.logError(ex, Messages.getString("ChatServerListCellRenderer.ERROR_LOADING_ICONS"), true); //$NON-NLS-1$
			}
		}
		setPreferredSize(new Dimension(getWidth(), imgSnooze.getHeight()));
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends ChatServer> list, ChatServer value, int index, boolean isSelected, boolean cellHasFocus) {
		ChatServerUI ui = uiMap.computeIfAbsent(value, k -> new ChatServerUI(value));
        ui.index = index;
        ui.selected = isSelected;
        ui.hasFocus = cellHasFocus;
        ui.parentList = (JList<ChatServer>)list;
        curUI = ui;

        return this;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(curUI == null || curUI.chatServer == null)
			return;
		int w = getWidth();
		int h = getHeight();
		Font font = curUI.parentList.getFont();
		if(curUI.selected) {
			g.setColor(curUI.parentList.getSelectionBackground());
			g.fillRect(0, 0, w, h);
		}
        // Draw image based on currentUI state
        BufferedImage image = curUI.getCurrentImage();
        if (image != null) {
            g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        }

        // Draw text
        g.setColor(curUI.selected ?
            curUI.parentList.getSelectionForeground() :
            curUI.parentList.getForeground());
		g.setFont(font);
        int textX = image != null ? image.getWidth() + 5 : 5;
        g.drawString(curUI.chatServer.getName(), textX, font.getSize() + 10);
        g.drawString(curUI.chatServer.getChatClient().getChatBotClient().getRiaFileName(),
                    textX, h - (font.getSize() + 5));

        if (curUI.hasFocus) {
            g.setColor(curUI.parentList.getSelectionForeground());
            g.drawRect(0, 0, w-1, h-1);
        }
	}

	///////////////////////////////////////////////////////////////////////////////
	/// ChatServer JList UI state
	///////////////////////////////////////////////////////////////////////////////
	private static class ChatServerUI {
        int index = -1;
        boolean selected = false;
        boolean hasFocus = false;
        JList<ChatServer> parentList = null;
        long lastTime = 0;
        final ChatServer chatServer; // Make final
        BufferedImage curImage = imgSnooze;

        // Listener for this specific UI instance
        private final ChatServerListener listener;

        /**
         * Constructor
         * @param server ChatServer instance
         */
        public ChatServerUI(ChatServer server) {
            this.chatServer = server;
            this.listener = createListener();
            this.chatServer.addChatServerListener(this.listener);
        }

        /**
         * Create listener instance
         * @return ChatServerListener instance
         */
        private ChatServerListener createListener() {
            return new ChatServerListener() {
                @Override
                public void onActivityStateChanged(ChatServer server, ChatSessionState state) {
                    if (server.equals(chatServer)) {
                        switch(state) {
                            case CHATBOT: curImage = imgChatBot; break;
                            case AIMODEL: curImage = imgAi;      break;
                            case AGENT:   curImage = imgAgent;   break;
                            case WEBSITE: curImage = imgWebsite; break;
                        }
                        lastTime = System.currentTimeMillis();
                        // Trigger repaint through parent list
                        if (parentList != null) {
                            parentList.repaint();
                        }
                    }
                }

                @Override
                public void onStatsChanged(ChatServer server) { }
            };
        }

        /**
         * Get current image depending on current state
         * @return BufferedImage instance
         */
        public BufferedImage getCurrentImage() {
            if (!chatServer.isEnabled()) {
                return imgDisabled;
            }

            // Auto-reset to snooze
            if (!curImage.equals(imgSnooze) &&
                System.currentTimeMillis() - lastTime >= 1000) {
                curImage = imgSnooze;
            }

            return curImage;
        }
    }
}
