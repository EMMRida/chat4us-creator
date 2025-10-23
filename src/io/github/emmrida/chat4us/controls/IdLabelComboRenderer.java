/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.controls;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * Combo cell renderer.
 *
 * @author El Mhadder Mohamed Rida
 */
public class IdLabelComboRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = 6983729229450051242L;

	@Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if(c instanceof JLabel && value instanceof IdLabelComboElement) {
            JLabel label = (JLabel) c;
            IdLabelComboElement row = (IdLabelComboElement)value;
            if (!row.isEnabled()) {
                label.setForeground(Color.GRAY);
            } else {
                label.setForeground(isSelected ? Color.WHITE : Color.BLACK);
            }
            label.setEnabled(row.isEnabled());
        }
        return c;
    }
}
