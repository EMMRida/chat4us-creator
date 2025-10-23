/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;

/**
 * The Class AboutDialog.
 *
 * @author El Mhadder Mohamed Rida
 */
public class AboutDialog extends JDialog {
	private static final long serialVersionUID = -4804053574383412456L;

	/**
	 * Instantiates a new about dialog.
	 * @param parent the parent frame
	 */
    public AboutDialog(Frame parent) {
        super(parent, Messages.getString("AboutDialog.DLG_TITLE"), true); //$NON-NLS-1$
        setTitle(Messages.getString("AboutDialog.DLG_TITLE")); //$NON-NLS-1$
        setLocationRelativeTo(parent);
        setResizable(false);

        // Main panel
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Logo or icon
        JLabel iconLabel = new JLabel(Helper.loadIconFromResources("/about-logo.png", 160, 160)); // Change to your logo path //$NON-NLS-1$
        panel.add(iconLabel, BorderLayout.WEST);

        // Text area for information
        JPanel textPanel = new JPanel(new GridLayout(5, 1));
        JLabel lblChatusChatbots = new JLabel(Messages.getString("AboutDialog.LBL_ABOUT")); //$NON-NLS-1$
        textPanel.add(lblChatusChatbots);
        JLabel label_1 = new JLabel(Messages.getString("AboutDialog.LBL_VERSION") + Helper.getAppVersion()); //$NON-NLS-1$
        textPanel.add(label_1);
        JLabel label_2 = new JLabel(Messages.getString("AboutDialog.LBL_DEVELOPPER")); //$NON-NLS-1$
        textPanel.add(label_2);
        JLabel label_3 = new JLabel(Messages.getString("AboutDialog.LBL_COPYRIGHT")); //$NON-NLS-1$
        textPanel.add(label_3);
        panel.add(textPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton websiteButton = new JButton(Messages.getString("AboutDialog.LBL_WEBSITE")); //$NON-NLS-1$
        JButton closeButton = new JButton(Messages.getString("AboutDialog.BTN_CLOSE")); //$NON-NLS-1$

        websiteButton.addActionListener(e -> {
        	Helper.openContent("https://chat4usai.com");
        }); //$NON-NLS-1$
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(websiteButton);
        buttonPanel.add(closeButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Add main panel to dialog
        getContentPane().add(panel);

        Helper.registerCancelByEsc(this, closeButton);
        Helper.enableRtlWhenNeeded(this);
        pack();
    }
}
