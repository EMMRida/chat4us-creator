/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.internalclient;

import javax.swing.JPanel;
import javax.swing.UIManager;

import io.github.emmrida.chat4us.util.Helper;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import javax.swing.JLabel;
import javax.swing.JEditorPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

/**
 * Panel for error messages. This panel is used to display error messages in the chat window.
 */
public class ErrorMessage extends JPanel implements IMessagePanel {
	private static final long serialVersionUID = 3317521967808457428L;
	private JEditorPane editorPane;
	private JLabel lblUserIcon;

	/** @see IMessagePanel#setMessage(String, long) */
	@Override
	public void setMessage(String msg, long time) {
		Helper.setupEditorPane(editorPane);
		editorPane.setText(msg);
	}

	/** @see IMessagePanel#setIcon(String) */
	@Override
	public void setIcon(String icon) {
		lblUserIcon.setIcon(Helper.loadIconFromResources(icon, 48, 48));
	}

	/** @see IMessagePanel#onShown() */
	@Override
	public void onShown() { }

	/**
	 * Creates the panel.
	 */
	public ErrorMessage() {
		setMaximumSize(new Dimension(32767, 120));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{28, 0};
		gridBagLayout.rowHeights = new int[]{17, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		JPanel panel = new JPanel();
		panel.setBackground(Color.RED);
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
		editorPane = new JEditorPane();
		editorPane.setText("1\r\n2\r\n3");
		editorPane.setBackground(Color.PINK);
		editorPane.setFocusable(false);
		editorPane.setEditable(false);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(5, 5, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(3)
					.addComponent(lblNewLabel)
					.addGap(3)
					.addComponent(editorPane, GroupLayout.PREFERRED_SIZE, 28, Short.MAX_VALUE)
					.addGap(3))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(3)
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(editorPane, GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
						.addComponent(lblNewLabel))
					.addGap(3))
		);
		panel.setLayout(gl_panel);
		Helper.enableRtlWhenNeeded(this);
	}
}
