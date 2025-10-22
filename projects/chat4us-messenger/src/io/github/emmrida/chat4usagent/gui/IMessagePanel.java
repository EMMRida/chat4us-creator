/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.gui;

/**
 * Interface for message panels
 *
 * @author El Mhadder Mohamed Rida
 */
public interface IMessagePanel {
	/**
	 * Sets the message and time.
	 * @param msg the incoming message
	 * @param time the time the message arrived.
	 */
	void setMessage(String msg, long time);
	/** Called when the message panel is shown. */
	void onShown();
}
