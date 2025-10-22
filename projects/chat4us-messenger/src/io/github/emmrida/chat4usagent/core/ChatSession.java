/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.emmrida.chat4usagent.util.Helper;
import io.undertow.server.HttpServerExchange;

/**
 * Holds all data used during a chat session with a remote user.
 *
 * @author El Mhadder Mohamed Rida
 */
public class ChatSession {

	private String userId;
	private boolean ended;
	private long lastUserMsgTime;
	private long lastAgentMsgTime;
	private int timeout;
	private HttpServerExchange exchange;
	private List<String> chatMessages;


	/**
	 * Init this ChatSession instance.
	 * @param userId Remote user id that is sent by the remote IFrame.
	 * @param chatMsgList List of all chat history messages.
	 */
	public ChatSession(String userId, List<String> chatMsgList) {
		this.userId = userId;
		this.ended = false;
		this.lastUserMsgTime = 0L;
		this.lastAgentMsgTime = 0L;
		this.exchange = null;
		this.chatMessages = new ArrayList<>(chatMsgList);
	}

	/**
	 * Called when an agent/operator sent a message.
	 */
	public void agentMsgSent() {
		lastAgentMsgTime = System.currentTimeMillis();
		exchange = null;
	}

	/**
	 * Called when a incoming message arrived
	 * @param xchng Undertow Http server exchange.
	 */
	public void userMsgArrived(HttpServerExchange xchng) {
		Objects.requireNonNull(xchng);
		lastUserMsgTime = System.currentTimeMillis();
		exchange = xchng;
	}

	/**
	 * Called when an incoming message arrived
	 * @param xchng Undertow Http server exchange
	 * @param msg The incoming message.
	 */
	public void userMsgArrived(HttpServerExchange xchng, String msg) {
		Objects.requireNonNull(xchng);
		Helper.requiresNotEmpty(msg);
		lastUserMsgTime = System.currentTimeMillis();
		exchange = xchng;
		chatMessages.add(msg);
	}

	/**
	 * @return The remote user id
	 */
	public String getUserId() { return userId; }

	/**
	 * @return True if the chat session is ended.
	 */
	public boolean isEnded() { return ended; }

	/**
	 * Sets the chat session as ended.
	 * @param ended True if the chat session is ended.
	 */
	public void setEnded(boolean ended) { this.ended = ended; }

	/**
	 * @return List of all chat history messages.
	 */
	public List<String> getChatMessages() { return chatMessages; }

	/**
	 * Add a message to the chat history.
	 * @param msg The message to add.
	 */
	public void addMessage(String msg) { chatMessages.add(msg); }

	/**
	 * Add a message to the chat history.
	 * @param msg The message to add.
	 * @param isAgent True if the message is from the agent.
	 */
	public void addMessage(String msg, boolean isAgent) { chatMessages.add((isAgent ? "Agent : " : "User : ") + msg); } //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * @return True if the remote user is waiting for a response.
	 */
	public boolean isWaitingForResponse() { return lastUserMsgTime >= lastAgentMsgTime; }

	/**
	 * @return The last time a message arrived from the remote user.
	 */
	public long getLastUserMsgTime() { return lastUserMsgTime; }

	/**
	 * @return The last time an agent/operator sent a message.
	 */
	public long getLastAgentMsgTime() { return lastAgentMsgTime; }

	/**
	 * @return The Undertow Http server exchange if the remote user is waiting for a response, null otherwise.
	 */
	public HttpServerExchange getExchange() { return exchange; }

	/**
	 * @return The timeout in seconds that the agent/operator must respond to the remote user.
	 */
	public int getTimeout() { return timeout; }

	/**
	 * Sets the timeout in seconds that the agent/operator must respond to the remote user.
	 * @param timeout The timeout in seconds.
	 */
	public void setTimeout(int timeout) { this.timeout = timeout; }
}











