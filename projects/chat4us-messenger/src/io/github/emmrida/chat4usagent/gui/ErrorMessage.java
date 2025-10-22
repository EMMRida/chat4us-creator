/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.gui;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import io.github.emmrida.chat4usagent.util.Helper;

import javax.swing.JLabel;
import javax.swing.JEditorPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

/**
 * Panel for error messages. This panel is used to display error messages in the chat window.
 *
 * @author El Mhadder Mohamed Rida
 */
public class ErrorMessage extends JPanel implements IMessagePanel {
	private static final long serialVersionUID = 3317521967808457428L;
	private JEditorPane editorPane;

	/** @see IMessagePanel#setMessage(String, long) */
	@Override
	public void setMessage(String msg, long time) {
		Helper.setupEditorPane(editorPane);
		editorPane.setText(msg);
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
		JLabel lblNewLabel = new JLabel(""); //$NON-NLS-1$
		lblNewLabel.setIcon(UIManager.getIcon("OptionPane.warningIcon")); //$NON-NLS-1$
		editorPane = new JEditorPane();
		editorPane.setText("1\r\n2\r\n3"); //$NON-NLS-1$
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

/*
Table of contents
1. Introduction
2. Requirements and installation
3. How to use
4- Settings Dialog
4.1 Misc tab
4.2 Server tab
4.3 Templates tab
5. Messages CSS styles
6. Miscellaneous features

1- Introduction
Chat4Us-Agent is a messenger app that connects an agent/human operator to a remote user through a chat boot server hosted on Chat4Us creator.
It servers to handle complex cases that a chat flow or an AI-driven discussion cannot handle like payments, bookings, etc.
Chat4Us-Agent UI is very simple to use. On the left, there is a list of chats active or ended. On the right, there is a chat window where
the user and the agent can exchange messages. There are also a few other features that will be covered later in this guide.

2- Requirements and installation
Chat4Us-Agent requires Java 21 JRE or higher being installed on the agent computer. You can download it from https://adoptium.net/temurin/releases/.
The installation process is easy, you just need to extract the archive and grant permissions when needed then start chat4us-agent.jar.

When started for the first time, you need to open the Settings Dialog and configure how the messenger app will handle connections from the remote chat bot servers.
Refer to the Settings Dialog section for more details.

3- How to use
As an agent/human operator, start chat4us-agent.jar and then click on the "Connect" toggle button on the toolbar to start accepting connections from the chat bot server.
You can continue your work as usual and you will be notified when a chat flow is redirecting a remote user to you. On incoming connection, a new chat with the remote user
name will be added to the left chats list. Click on it to show the chat discussion between the remote user and the chat bot.
As an agent/human operator, you need to continue the discussion with the remote user in order to handle complexe cases that the chat flow failed to handle.
Pay attention to the timeout of the messages that appears on each chat list entry. The connection with the remote user will be disconnected when the timeout is reached.

4- Settings
To setup the messenger app, you need to open the Settings Dialog by clicking on the Settings button on the toolbar. There are 3 tabs holding the following settings:

4.1 Misc tab
•	Sound notifications: Enable or disable sound notifications.
•	System notifications: Enable or disable system notifications.
•	Connect automatically: Enable or disable auto connect at app start.
4.2 Server tab
•	Host: The host accepting connections from the remote chat bot servers.
•	Port: The port accepting connections from the remote chat bot servers.
•	Whitelist: The list of IP addresses that are allowed to connect to the messenger app.
4.3 Templates tab
Here you can define the templates that will let you customize the look and content of most recurrent responses to the remote user. Those templates will be then
available on the button under the "Send" button besides the response text editor which shows a menu of available templates for quick access.
Click on the "+" button to open the template editor dialog. There You can define the template title and Content.
•	Title: The title of the template that will be shown on the list when clicking on the "..." button under the "Send" button.
•	Content: The content that will be copied to the response text editor for quick editing then sent to the remote user. You can define variables that will be replaced
    using the Response Template dialog. Consider the following example: <a href="@S:Url?">@S:Texte?</a>
    This is a template for an HTML Url tag that holds two variables @S:Url and @S:Texte. The 3 first characters @S: are used to define the variables type here as Text.
    the first variable @S:Url will define of the URL that will be opened when clicking on the tag. The second variable @S:Texte will define the text that will be shown.
    When choosing that template, the Response Template dialog will show two fields:
    •	Url: Where you can define the URL that will be opened when clicking on the tag.
	•	Text: Where you can define the text that will be shown.
	When you fll in the fields and click on the "OK" button, the template will be copied to the response text editor ready to be sent to the remote user.
Variables are defined using the following syntax: @X:var_name? where X is the type of the variable (S for string, I for integer, D for real number)
and var_name is the name of the variable. Ex: $S:Text? for a string variable named "Text", $D:Price? for a real number variable named "Price" and $I:Quantity? for an integer variable named "Quantity".

5 - Messages CSS styles
As mentioned in the Chat4Us guide documentation, chat bots designer shoud use CSS styles to format their messages for web clients and add similar CSS styles to the
messenger app so the message look and feel is similar to the one used by the chat bot clients to help agents/human operators easily read the chat discussion messages.
chat4us-agent supports basic CSS styles to format messages.
Click on the CSS+ button on the toolbar to open Messages CSS styles dialog. Here you can define the CSS styles that will be applied on the incoming messages only.

6- Miscellaneous features
•	Chat switching back to the chat flow: In some cases the remote user can switch back to the chat flow and the agent/human operator will no more receive messages
from the remote user without notification.
•	Spam avoidance: If at some point the remote user starts spamming the agent/human operator, the operator can right click on the specified chat entry and delete it.
When deleted, no more messages will be received from the remote user.
•	Agent/human operator not available: When the agent/human operator is not available, a click on the Disconnected button will let the chat bot server ignore the messenger app.
Pay attention that if no agent/human operator is available, the chat bot server will disconnect and show an error message to the remote user.
•	Whitelist: You need to add the IP address of the remote chat bot server to the whitelist to let the messenger app accept connections from it.
*/
















