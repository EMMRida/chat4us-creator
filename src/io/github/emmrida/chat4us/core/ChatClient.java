/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.github.emmrida.chat4us.core.ChatSession.ChatSessionState;
import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;

/**
 * Manages chat sessions between remote users and chat bots either RIA, AI Model or Agent/Messenger.
 *
 * @author El Mhadder Mohamed Rida
 */
public class ChatClient {

	private ChatBotClient chatBotClient;
	private ChatAgent chatAgent;
	private List<IChatModelClient> chatModelClients;
	private Map<Integer, ChatSession> chatSessions;

	/**
	 * Init the chat client
	 */
	public ChatClient() {
		this.chatSessions = new HashMap<Integer, ChatSession>();
		this.chatAgent = new ChatAgent();
		this.chatBotClient = new ChatBotClient();
		this.chatModelClients = new ArrayList<IChatModelClient>();
	}

	/**
	 * Starts the a chat session for a remote user with either a RIA, AI model or an agent/messenger.
	 * @param ses Chat session object.
	 * @return Returns responses list.
	 */
	public String[] letsChat(ChatSession ses) {
		Objects.requireNonNull(ses);
		if(ses.getState() == ChatSessionState.CHATBOT) {
			return this.chatBotClient.letsChat(ses);
		} else if(ses.getState() == ChatSessionState.AIMODEL) {

			boolean done = false;
			for(IChatModelClient cmc : this.chatModelClients) {
				if(!cmc.isBusy()) {
					done = true;
					return cmc.letsChat(ses);
				}
			}
			if(!done) {
				Helper.logWarning(Messages.getString("ChatClient.AI_SERVERS_ALL_BUSY")); //$NON-NLS-1$
				return new String[] { Messages.getString("ChatClient.UNK_ERROR_TRY_LATER") }; //$NON-NLS-1$
			}
		} else if(ses.getState() == ChatSessionState.AGENT) {
			return this.chatAgent.letsChat(ses);
		}
		IllegalStateException ex = new IllegalStateException(Messages.getString("ChatClient.ILLEGAL_STATE_EX")); //$NON-NLS-1$
		Helper.logError(ex, Messages.getString("ChatClient.LOG_ILLEGAL_STATE_EX")); //$NON-NLS-1$
		throw ex;
	}

	/**
	 * Processes a message in a chat session scope.
	 * @param ses Chat session.
	 * @param msg message content.
	 * @return Responses list.
	 */
	public String[] userMessage(ChatSession ses, String msg) {
		Objects.requireNonNull(ses);
		Helper.requiresNotEmpty(msg);
		if(ses.getState() == ChatSessionState.CHATBOT) {
			return this.chatBotClient.userMessage(ses, msg);
		} else if(ses.getState() == ChatSessionState.AIMODEL) {
			// Let the server switch to an agent so the last ai model message is sent immediatly.
			boolean done = false;
			for(IChatModelClient cmc : this.chatModelClients) {
				if(!cmc.isBusy()) {
					done = true;
					return cmc.userMessage(ses, msg);
				}
			}
			if(!done) {
				Helper.logWarning(Messages.getString("ChatClient.AI_SERVERS_ALL_BUSY")); //$NON-NLS-1$
				return new String[] { Messages.getString("ChatClient.UNK_ERROR_TRY_LATER") }; //$NON-NLS-1$
			}
		} else if(ses.getState() == ChatSessionState.AGENT) {
			return this.chatAgent.userMessage(ses, msg);
		}
		IllegalStateException ex = new IllegalStateException(Messages.getString("ChatClient.ILLEGAL_STATE_EX")); //$NON-NLS-1$
		Helper.logError(ex, Messages.getString("ChatClient.LOG_ILLEGAL_STATE_EX")); //$NON-NLS-1$
		throw ex;
	}

	/**
	 * Loads a RIA file.
	 * @param riaFileName RIA file name relative/absolute path.
	 * @throws Exception
	 */
	public void loadChatBotRIA(String riaFileName) throws Exception {
		Helper.requiresNotEmpty(riaFileName);
		this.chatBotClient.loadRIA(riaFileName, null);
	}

	/**
	 * Adds an AI model server to the list. As AI models are heavy on OS resources and may require long time to generate
	 * a response, we can add one or more AIM servers in order to use the ones that are not busy.
	 * @param chatModelClient Chat model client object.
	 */
	public void addChatModelClient(IChatModelClient chatModelClient) {
		Objects.requireNonNull(chatModelClient);
		this.chatModelClients.add(chatModelClient);
	}

	/**
	 * Returns the ids of the connected agents.
	 * @return The ids of the connected agents.
	 */
	public Set<Integer> getConnectedAgents() {
		Set<Integer> aids = new HashSet<Integer>();
		chatSessions.forEach((id, ses) -> {
			if(!ses.isEnded() && ses.getAgentId() > 0) {
				aids.add(ses.getAgentId());
			}
		});
		return aids;
	}

	/**
	 * Returns the main locale.
	 * @return The main locale.
	 */
	public String getMainLocale() { return this.chatBotClient.getMainLocale(); }

	/**
	 * Returns the default RIA chat session.
	 * @param locale Locale of the RIA to return.
	 * @return Default RIA chat session.
	 */
	public ChatSession riaChatSession(String locale) { return new ChatSession(this.chatBotClient.riaChatSession(locale)); }

	/**
	 * Returns a chat session by id.
	 * @param id Id of the chat session to return.
	 * @return Chat session, null on error.
	 */
	public ChatSession getChatSessionById(int id) { return this.chatSessions.get(id); }

	/**
	 * Returns the chat bot client object
	 * @return Chat bot client object.
	 */
	public ChatBotClient getChatBotClient() { return this.chatBotClient; }

	/**
	 * Returns the chat model client at the index param.
	 * @param index Index of the chat model client to return.
	 * @return Chat model client object, null on error.
	 */
	public IChatModelClient getChatModelClient(int index) { return this.chatModelClients.get(index); }

	/**
	 * Number of the chat model clients managed by the server.
	 * @return Number of chat model clients.
	 */
	public int getChatModelClientsCount() { return this.chatModelClients.size(); }
}











