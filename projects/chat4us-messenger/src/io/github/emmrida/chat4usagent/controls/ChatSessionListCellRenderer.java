/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.controls;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import io.github.emmrida.chat4usagent.core.ChatSession;

/**
 * Chat session list cell renderer.
 *
 * @author El Mhadder Mohamed Rida
 */
public class ChatSessionListCellRenderer extends JPanel implements ListCellRenderer<ChatSession> {
	private static final long serialVersionUID = 8729588634438436418L;
	private static BasicStroke dashedStroke = null;

	private int index;
	private boolean selected;
	private boolean hasFocus;
	private ChatSession chatSession;
	private JList<ChatSession> parentList;

	/**
	 * Init chat session list cell renderer.
	 */
	public ChatSessionListCellRenderer() {
		chatSession = null;
		selected = hasFocus = false;
		parentList = null;
		FontMetrics fm = getFontMetrics(this.getFont());
		setPreferredSize(new Dimension(getWidth(), fm.getHeight() * 2));
		if(dashedStroke == null) {
            float[] dashPattern = {1, 1}; // Dash length 10px, gap 5px
            dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0);
		}
	}

	/** @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean) */
	@Override
	public Component getListCellRendererComponent(JList<? extends ChatSession> list, ChatSession value, int index, boolean isSelected, boolean cellHasFocus) {
		this.index = index;
		this.selected = isSelected;
		this.hasFocus = cellHasFocus;
		this.chatSession = value;
		this.parentList = (JList<ChatSession>)list;
		return this;
	}

	/** @see javax.swing.JComponent#paint(java.awt.Graphics) */
	@Override
	protected void paintComponent(java.awt.Graphics g) {
		int w = getWidth();
		int h = getHeight();
		long amsg = chatSession.getLastAgentMsgTime();
		long umsg = chatSession.getLastUserMsgTime();
		long timeout = (long)chatSession.getTimeout() * 1000L;
		Color clr = this.parentList.getBackground();
		long ms = timeout - (System.currentTimeMillis() - umsg);
		if(umsg > amsg) {
			if(ms <= 0) {
				clr = Color.RED;
			} else if(ms > timeout/3*2) {
				clr = Color.GREEN;
			} else if(ms < (timeout/3)*2 && ms > timeout/3) {
				clr = Color.YELLOW;
			} else clr = Color.ORANGE;
		}
		g.setColor(clr);
		g.fillRect(0, 0, w, h);
		g.setColor(this.parentList.getForeground());

		// Create a graphics context with antialiasing
		Graphics2D g2d = (Graphics2D) g;
        // Enable antialiasing for better text quality
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Strings to be drawn
        String rightText = "--:--"; //$NON-NLS-1$
        if((ms > 0) && chatSession.isWaitingForResponse()) {
        	long seconds = ms / 1000L;
        	rightText = "[" + (seconds / 60) + ":" + (seconds % 60) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        String leftText = chatSession.getUserId();

        // Font metrics for text dimensions
        if(selected) {
        	Font font = this.parentList.getFont();
        	font = font.deriveFont(Font.BOLD);
			g2d.setFont(font);
        }
        FontMetrics metrics = g2d.getFontMetrics();
        int leftTextWidth = metrics.stringWidth(leftText);
        int rightTextWidth = metrics.stringWidth(rightText);

        // Padding to ensure some space between the two strings
        int padding = 10;

        // Determine positions
        int leftX = 3; // Fixed padding from left edge
        int leftY = getHeight() / 2 + metrics.getAscent() / 2;

        int rightX = getWidth() - rightTextWidth - 3; // Fixed padding from right edge
        int rightY = leftY;

        // Check and adjust if texts overlap
        if (leftX + leftTextWidth + padding > rightX)
            rightX = leftX + leftTextWidth + padding; // Adjust to avoid overlap

        // Draw texts
        g2d.drawString(leftText, leftX, leftY);
        g2d.drawString(rightText, rightX, rightY);
		if(this.selected) {
			g2d.setColor(this.parentList.getForeground());
            g2d.setStroke(dashedStroke);
			g.drawRect(0, 0, w-1, h-1);
		}
    }
}
