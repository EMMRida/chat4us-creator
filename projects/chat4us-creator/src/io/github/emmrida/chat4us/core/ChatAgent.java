/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.core;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.google.gson.Gson;

import io.github.emmrida.chat4us.gui.MainWindow;
import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;
import io.github.emmrida.chat4us.util.NoopTrustManager;

/**
 * This class takes control of the exchanges with the remote user. It starts by
 * sending all the messages of the chat session to the remote agent. Then it
 * retrieves the response from the remote agent through the messenger app.
 *
 * @author El Mhadder Mohamed Rida
 */
public class ChatAgent {
	private static final int AG_ID				= 0;
	private static final int AG_NAME			= 1;
	private static final int AG_POST_ID			= 2;
	private static final int AG_HOST			= 3;
	private static final int AG_PORT			= 4;
	private static final int AG_DESCRRIPTION	= 5;
	private static final int AG_LAST_CONNECTED	= 6;
	private static final int AG_ENABLED			= 7;
	private static final int AG_REMOVED			= 8;
	private static final int AG_AI_GROUP		= 9;

	private int nextAgentIndex;
	private List<Object[]> agents;

	private final Gson gson = new Gson();


	/**
	 * Init the chat agent object
	 */
	public ChatAgent() {
		nextAgentIndex = 0;
		loadAgents();
	}

	/**
	 *
	 * @param ses
	 * @param agentStartIndex
	 * @param agentEndIndex
	 * @param params
	 * @return
	 */
	private String[] connectToNextAgent(ChatSession ses, int agentStartIndex, int agentEndIndex, Map<String, String> params) {
		HttpResponse<String> response = null;
		for(int i = agentStartIndex; i < agentEndIndex; i++) {
			Object[] agent = agents.get(i);
			if((int)agent[AG_ENABLED] != 0 && (int)agent[AG_REMOVED] == 0 && (int)agent[AG_AI_GROUP] == ses.getAIGroupId()) {
				Helper.logInfo(Messages.getString("ChatAgent.LOG_CONNECTING_TO") + agent[AG_NAME] + "@" + agent[AG_HOST] + ":" + agent[AG_PORT] + "..."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				response = sendHttpRequest("https://" + (String)agent[AG_HOST] + ":" + (String)agent[AG_PORT] + "/letschat", params); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if(response != null && response.statusCode() == 200) {
					nextAgentIndex = i + 1;
					if(nextAgentIndex == agents.size())
						nextAgentIndex = 0;
					Map<String, Object> agentResponse = gson.fromJson(response.body(), Map.class);
					ses.switchToAgentChatSession((int)agent[AG_ID], (String)agent[AG_HOST], Integer.valueOf((String)agent[AG_PORT]), ses.getUserId());
					Helper.logInfo(String.format(Messages.getString("ChatAgent.AGENT_STARTED_CHAT"), agent[1], ses.getUserId())); //$NON-NLS-1$
					String msg = (String)agentResponse.get("AGENT_MESSAGE"); //$NON-NLS-1$
					ses.addHistoryChatMessage(true, msg);
					nextAgentIndex = i + 1;
					Helper.logInfo(Messages.getString("ChatAgent.LOG_CONTO_SUCCESS") + agent[AG_NAME] + "@" + agent[AG_HOST] + ":" + agent[AG_PORT]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					return new String[] { msg };
				} else Helper.logInfo("Error connecting to agent " + agent[AG_NAME] + "@" + agent[AG_HOST] + ":" + agent[AG_PORT]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		nextAgentIndex = agentEndIndex;
		return null;
	}

	/**
	 * Starts the chat with remote agent. It sends the whole discussion history so as
	 * the agent can understand the context and continues the conversation.
	 * Ends the chat session on timeout.
	 * @param ses The chat session object
	 * @return The response from the remote agent/messenger.
	 */
	public String[] letsChat(ChatSession ses) {
		Objects.requireNonNull(ses);
		try {
			loadAgents();
			StringBuffer chat = new StringBuffer();
			for(int i = 0; i < ses.getHistoryChatMessagesCount(); i++)
				chat.append(ses.getHistoryChatMessage(i)).append("<<br/>>"); //$NON-NLS-1$
			Map<String, String> params = new HashMap<>();
			params.put("chat", chat.toString()); //$NON-NLS-1$
			params.put("usr_id", ses.getUserId()); //$NON-NLS-1$
			params.put("timeout", Integer.toString(io.github.emmrida.chat4us.gui.MainWindow.getSettings().getAgentResponseTimeoutSeconds())); //$NON-NLS-1$
			int startAgentIndex = nextAgentIndex < agents.size() ? nextAgentIndex : 0;
			String[] response = connectToNextAgent(ses, startAgentIndex, agents.size(), params);
			if(response != null)
				return response;
			response = connectToNextAgent(ses, 0, startAgentIndex, params);
			if(response != null)
				return response;
			Helper.logWarning(String.format(Messages.getString("ChatAgent.NO_AGENT_AVAILABLE"), ses.getUserId())); //$NON-NLS-1$
		} catch(Exception ex) {
			Helper.logWarning(ex, Messages.getString("ChatAgent.AGENT_CHAT_INIT_ERROR"), false); //$NON-NLS-1$
		}
		ses.setEnded(true);
		String msg = ses.getNoAgentErrorMessage();
		return new String[] { msg.isBlank() ? Messages.getString("ChatAgent.NO_AGENT_AVAILABLE_TRY_LATER") : msg }; //$NON-NLS-1$
	}

	/**
	 * Sends the remote user message to the remote agent/messenger.
	 * Ends the chat on timeout.
	 * @param ses The chat session object
	 * @param msg User message.
	 * @return Agent response.
	 */
	public String[] userMessage(ChatSession ses, String msg) {
		Objects.requireNonNull(ses);
		Helper.requiresNotEmpty(msg);
		ses.addHistoryChatMessage(false, msg.trim());
		Object[] agent = getAgentById(ses.getAgentId());
		if(agent != null) {
			Map<String, String> params = new HashMap<>();
			params.put("usr_id", ses.getUserId()); //$NON-NLS-1$
			params.put("usr_msg", msg); //$NON-NLS-1$
			params.put("timeout", Integer.toString(io.github.emmrida.chat4us.gui.MainWindow.getSettings().getAgentResponseTimeoutSeconds())); //$NON-NLS-1$
			HttpResponse<String> response = sendHttpRequest("https://" + (String)agent[AG_HOST] + ":" + (String)agent[AG_PORT] + "/message", params); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if(response.statusCode() == 200) {
				Map<String, Object> agentResponse = gson.fromJson(response.body(), Map.class);
				String agResponse = (String)agentResponse.get("AGENT_MESSAGE"); //$NON-NLS-1$
				ses.addHistoryChatMessage(true, agResponse.trim());
				return new String[] { agResponse };
			}
		}

		Helper.logWarning(String.format(Messages.getString("ChatAgent.AGENT_RESPONSE_TIMEOUT"), agent[AG_NAME], ses.getUserId())); //$NON-NLS-1$
		ses.setEnded(true);
		return new String[] { Messages.getString("ChatAgent.UNK_ERROR_TRY_LATER") }; //$NON-NLS-1$
	}

	/**
	 * Sends the HTTP request to the remote messenger app wait for the agent response
	 * to return it.
	 * @param url Url of the messenger app machine:port number.
	 * @param params Request parameters.
	 * @return Agent response or null on error or timeout.
	 */
	public HttpResponse<String> sendHttpRequest(String url, Map<String, String> params) {
		Helper.requiresNotEmpty(url);
		Objects.requireNonNull(params);
        StringBuffer requestBody = new StringBuffer();
        for(Map.Entry<String, String> entry : params.entrySet()) {
			requestBody.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
			requestBody.append("="); //$NON-NLS-1$
			requestBody.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
			requestBody.append("&"); //$NON-NLS-1$
		}
		requestBody.deleteCharAt(requestBody.length() - 1);

        // Create an HttpClient instance
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .sslContext(new NoopTrustManager().getNoopSSLContext()) // Disables SSL certificate verification
                .build();

        // Build the HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded") //$NON-NLS-1$ //$NON-NLS-2$
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .timeout(java.time.Duration.ofSeconds(120))
                .build();
        try {
            // Send the request and get the response
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            Helper.logWarning(ex, String.format(Messages.getString("ChatAgent.ERROR_SENDING_MSG"), url)); //$NON-NLS-1$
        }
        return null;
    }

	/**
	 * Loads the list of the available agents.
	 */
	private void loadAgents() {
		agents = Helper.loadTable(MainWindow.getDBConnection(), "agents"); //$NON-NLS-1$
	}

	/**
	 * Returns the agent by id.
	 * @param id The id of the agent.
	 * @return Agent data or null if not found.
	 */
	public Object[] getAgentById(int id) {
		for(Object[] o : agents)
			if(((int)o[AG_ID]) == id)
				return o;
		return null;
	}

	/**
	 * Reloads the list of the available agents.
	 */
	public void agentsChanged() { loadAgents(); }
}









