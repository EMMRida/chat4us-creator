/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.util;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * Manages hotkeys in a Swing application using KeyBindings.
 *
 * @author El Mhadder Mohamed Rida
 */
public class HotkeyManager {
    private final JComponent component;
    private final Map<String, HotkeyListener> hotkeyListeners = new HashMap<>();

    /**
     * Init this instance.
     * @param component Component where the hotkey is active.
     */
    public HotkeyManager(JComponent component) {
        this.component = component;
    }

    /**
     * Registers a hotkey and associates it with a listener.
     *
     * @param keyStroke The keystroke string (e.g., "control H").
     * @param actionName The action name identifier.
     * @param listener The listener to notify when the hotkey is triggered.
     */
    public void registerHotkey(String keyStroke, String actionName, HotkeyListener listener) {
        KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
        if (ks == null) {
            throw new IllegalArgumentException(Messages.getString("HotkeyManager.EX_INVALID_KEY_STROKE") + keyStroke); //$NON-NLS-1$
        }

        // Add to InputMap & ActionMap
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();

        inputMap.put(ks, actionName);
        actionMap.put(actionName, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
                HotkeyListener hotkeyListener = hotkeyListeners.get(actionName);
                if (hotkeyListener != null) {
                    hotkeyListener.onHotkeyPressed(actionName);
                }
            }
        });

        // Store the listener
        hotkeyListeners.put(actionName, listener);
    }

    /**************************************************************************
     **************************************************************************
     **************************************************************************/

    /**
     * Hotkey listener interface.
     */
    public interface HotkeyListener {
        void onHotkeyPressed(String actionName);
    }
}
