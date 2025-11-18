/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.internalclient;

/**
 * Interface for message panels
 */
public interface IMessagePanel {
	/**
	 * Sets the message and time.
	 * @param msg the incoming message
	 * @param time the time the message arrived.
	 */
	void setMessage(String msg, long time);

	/**
	 * Sets the icon
	 * @param icon The icon resources path
	 */
	void setIcon(String icon);

	/** Called when the message panel is shown. */
	void onShown();
}
