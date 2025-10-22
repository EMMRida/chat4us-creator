/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import io.github.emmrida.chat4usagent.util.Helper;
import io.github.emmrida.chat4usagent.util.Messages;

/**
 * This class represents a dialog that allows the user to input values for placeholders in a template.
 * The placeholders are identified by the pattern "@(S|I|D):[a-zA-Z0-9_]+" where "S" means "string", "I" means "integer", and "D" means "double".
 * The user can enter values for the placeholders in the dialog, and the dialog will return a string with the placeholders replaced by the entered values.
 *
 * @author El Mhadder Mohamed Rida
 */
public class DynamicInputDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private String template;
    private Map<String, JTextField> fields;
    private boolean confirmed;

    /**
     * Init the dialog
     * @param parent Parent window
     * @param template Template
     */
    public DynamicInputDialog(Frame parent, String template) {
        super(parent, Messages.getString("DynamicInputDialog.DIDLG_TITLE"), true); //$NON-NLS-1$
        this.template = template;
        this.fields = new LinkedHashMap<>();
        this.confirmed = false;

        // Parse template and build UI
        parseTemplateAndBuildUI();

        // Set up dialog behavior
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Parse template and build UI.
     */
    private void parseTemplateAndBuildUI() {
        // Extract identifiers from template
        Pattern pattern = Pattern.compile("@(S|I|D):([a-zA-Z0-9_]+)\\?"); //$NON-NLS-1$
        Matcher matcher = pattern.matcher(template);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3, 3, 3, 3);

        while (matcher.find()) {
            String type = matcher.group(1);
            String idLabel = matcher.group(2);
            String identifier = matcher.group();

            // Label for input
            JLabel label = new JLabel(idLabel + " : "); //$NON-NLS-1$
            label.setHorizontalAlignment(JLabel.TRAILING);
            panel.add(label, gbc);
            gbc.gridx++;

            // Text field for input
            JTextField textField = new JTextField(15);
            panel.add(textField, gbc);
            fields.put(identifier, textField);

            gbc.gridx = 0;
            gbc.gridy++;
        }

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);

        // Add buttons
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton(Messages.getString("DynamicInputDialog.BTN_OK")); //$NON-NLS-1$
        JButton cancelButton = new JButton(Messages.getString("DynamicInputDialog.BTN_CANCEL")); //$NON-NLS-1$

        okButton.setDefaultCapable(true);
        okButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        Helper.registerCancelByEsc(this, cancelButton);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        Helper.registerCancelByEsc(this, cancelButton);
        Helper.enableRtlWhenNeeded(this);
    }

    /**
     * Get resolved string.
     * @return Resolved string
     */
    public String getResolvedString() {
        if (!confirmed)
            return null;

        String result = template;
        for (Map.Entry<String, JTextField> entry : fields.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().getText();

            // Extract type from key
            Matcher matcher = Pattern.compile("@(S|I|D):").matcher(key); //$NON-NLS-1$
            if (matcher.find()) {
                String type = matcher.group(1);

                // Validate input type
                switch (type) {
                    case "I": //$NON-NLS-1$
                        if (!value.matches("\\d+")) { //$NON-NLS-1$
                            JOptionPane.showMessageDialog(this, Messages.getString("DynamicInputDialog.MSGBOX_TEXT_FOR") + key + Messages.getString("DynamicInputDialog.MSGBOX_TEXT_FOR_INTEGER"), Messages.getString("DynamicInputDialog.DLG_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            return null;
                        }
                        break;
                    case "D": //$NON-NLS-1$
                        if (!value.matches("\\d+(\\.\\d+)?")) { //$NON-NLS-1$
                            JOptionPane.showMessageDialog(this, Messages.getString("DynamicInputDialog.MSGBOX_TEXT_FOR") + key + Messages.getString("DynamicInputDialog.MSGBOX_TEXT_FOR_DOUBLE"), Messages.getString("DynamicInputDialog.DLG_TITLE_ERROR"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            return null;
                        }
                        break;
                    case "S": //$NON-NLS-1$
                        // No specific validation for strings
                        break;
                }
            }

            // Replace all occurrences of the placeholder with user input
            result = result.replaceAll(Pattern.quote(key), Matcher.quoteReplacement(value));
        }

        return result;
    }
}
