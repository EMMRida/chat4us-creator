/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.core;

/**
 * Interface for an AI model web server client.
 *
 * @author El Mhadder Mohamed Rida
 */
public interface IChatModelClient {
	String[] letsChat(ChatSession ses);
	String[] userMessage(ChatSession ses, String msg);
	int getDbId();
	String getAiServerUrl();
	String getAiServerDomain();
	boolean isEnabled();
	void setEnabled(boolean enabled);
	boolean isBusy();
}
