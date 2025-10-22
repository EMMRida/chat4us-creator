/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.util;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * The Class CustomSaveFileChooser.	Used to create a custom save file chooser
 * with a confirmation message when a file already exists.
 *
 * @author El Mhadder Mohamed Rida
 */
public class CustomSaveFileChooser extends JFileChooser {

	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new custom save file chooser.
	 */
	public CustomSaveFileChooser() {
        super();
        Helper.enableRtlWhenNeeded(this);
    }

    /**
     * Approve selection if the file exists for overwriting.
     */
    @Override
    public void approveSelection() {
        File selectedFile = getSelectedFile();
        if (selectedFile.exists()) {
            int result = JOptionPane.showConfirmDialog(this, Messages.getString("CustomSaveFileChooser.MB_FILE_EXISTS_MSG"), Messages.getString("CustomSaveFileChooser.MB_FILE_EXISTS_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
            if (result == JOptionPane.YES_OPTION) {
                super.approveSelection();
            }
        } else {
            super.approveSelection();
        }
    }
}
