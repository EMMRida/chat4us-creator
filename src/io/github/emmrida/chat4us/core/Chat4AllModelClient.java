/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.core;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import io.github.emmrida.chat4us.gui.MainWindow;
import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;

/**
 * Manages the chat flows with local/offline AI models via GPT4All API server.
 *
 * @author El Mhadder Mohamed Rida
 */
public class Chat4AllModelClient implements IChatModelClient {

	public static final String AIQ_PREFIX = "gpt4all_"; //$NON-NLS-1$

	private HttpClient httpClient = null;

	private boolean busy;
	private boolean enabled;
	private String aiServerUrl; // http://localhost:4891/v1/chat/completions
	private int dbId;

	/**
	 * Init a chat model client object.
	 * @param dbId Id of the the chat model data in the database.
	 * @param aiServerUrl Url of the AI model server
	 * @param enabled State of the the instance.
	 */
	public Chat4AllModelClient(int dbId, String aiServerUrl, boolean enabled) {
		this.dbId = dbId;
		this.aiServerUrl = aiServerUrl;
		this.enabled = enabled;
		this.busy = false;
	}

	/**
	 * Compose an AI model web server query.
	 * @param ses Chat session object.
	 * @param msg User message.
	 * @return AI model web server query.
	 */
	private String makeQuery(ChatSession ses, String msg) {
		String key;
		String value;
		int contentLength = msg.length();
		int maxQueryLength = MainWindow.getSettings().getAiQueryMaxLength();
		Map<String, Object> gson = new HashMap<>();
		for(Map.Entry<String, String> entry : ses.getAiModelParamsEntrySet()) {
			if(!entry.getKey().isBlank()) {
				key = entry.getKey().trim();
				if(key.startsWith(AIQ_PREFIX)) {
					key = key.substring(AIQ_PREFIX.length());
					contentLength += key.length();
					value = entry.getValue().trim();
					contentLength += value.length();
					if(Helper.isNumeric(value)) {
						if(value.contains(".")) { //$NON-NLS-1$
							gson.put(key, Double.valueOf(value));
						} else gson.put(key, Long.valueOf(value));
					} else gson.put(key, value);
				}
			}
		}

		Map<String, Object> data;
		List<Map<String, Object>> messages = new ArrayList<>();
		Map<String, Object> gLines = new HashMap<>();
		gLines.put("role", "user"); //$NON-NLS-1$ //$NON-NLS-2$
		gLines.put("content", ses.getAiModelGuidelines()); //$NON-NLS-1$
		contentLength += 8 + 7 + ses.getAiModelGuidelines().length();
		StringBuilder content = new StringBuilder();
		for(int i = ses.getHistoryChatMessagesCount()-2; i >= Math.max(0, ses.getHistoryChatMessagesCount()-MainWindow.getSettings().getAiContextLines()-1); i--) {
			data = new HashMap<>();
			content.setLength(0);
			String[] hline = ses.getHistoryChatMessage(i).split(" : ", 2); //$NON-NLS-1$
			if(hline.length == 2) {
				data.put("role", "User".equals(hline[0]) ? "user" : "assistant"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				hline[1] = hline[1].replaceAll("<[^>]+>", "").replaceAll("<br/>", " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				contentLength += hline[1].length() + ("User".equals(hline[0]) ? 4 : 9); //$NON-NLS-1$
				if(contentLength <= maxQueryLength) {
					content.append(hline[1]);
					data.put("content", content.toString()); //$NON-NLS-1$
					messages.add(data);
				} else break;
			} else Helper.logWarning(String.format(Messages.getString("Chat4AllModelClient.INVALID_HISTORY_LINE"), i)); //$NON-NLS-1$
		}
		Collections.reverse(messages);
		messages.add(0, gLines);
		data = new HashMap<>();
		data.put("role", "user"); //$NON-NLS-1$ //$NON-NLS-2$
		data.put("content", msg); //$NON-NLS-1$
		messages.add(data);
		gson.put("messages", messages); //$NON-NLS-1$

		String json = new Gson().toJson(gson);
		System.out.println(json);
		return json;
	}

	/**
	 * Sends a query to the AI model server.
	 * @param ses Chat session object.
	 * @param msg User message.
	 * @return AI model generated response.
	 */
    private String sendMsgToModelServer(ChatSession ses, String msg) {
        if(httpClient == null)
        	httpClient = HttpClient.newHttpClient();
        String jsonInputString = makeQuery(ses, msg);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(aiServerUrl))
                .header("Content-Type", "application/json") //$NON-NLS-1$ //$NON-NLS-2$
                .POST(HttpRequest.BodyPublishers.ofString(jsonInputString, StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException ex) {
        	Helper.logError(ex, Messages.getString("Chat4AllModelClient.RESPONSE_ERROR")); //$NON-NLS-1$
        	ses.setEnded(true);
            return Messages.getString("Chat4AllModelClient.OFFLINE"); //$NON-NLS-1$
        }
    }

    /**
     * Starts a chat with the AI model.
     * @param ses Chat session object.
     * @return AI model generated response.
     */
    @Override
    public String[] letsChat(ChatSession ses) {
		return userMessage(ses, ""); //$NON-NLS-1$
	}

    /**
     * Sends a remote user message to the AI model server.
     * @param ses Chat session object.
     * @param msg User message.
     * @return AI model generated response.
     */
    @Override
    public String[] userMessage(ChatSession ses, String msg) {
    	busy = true;
		long l = System.currentTimeMillis();
		ses.addHistoryChatMessage(false, msg.trim());
		String response = sendMsgToModelServer(ses, msg);
		System.out.println(response);
		response = (String)Helper.getValueFromJsonPath(response, "choices/0/message/content"); //$NON-NLS-1$
		if(response == null) {
			response = ses.getDefaultErrorMessage();
			ses.setEnded(true);
		}

		response = response.replaceAll("\\n", "<br/>").replaceAll("\\\"", "&quot;"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ses.addHistoryChatMessage(true, response.trim());
		if((System.currentTimeMillis() - l) / 1000L > io.github.emmrida.chat4us.gui.MainWindow.getSettings().getAiLogOnLongResponse()*60)
			Helper.logWarning(String.format(Messages.getString("Chat4AllModelClient.AI_RESPONSE_TIME"), (System.currentTimeMillis() - l) / 1000L, msg)); //$NON-NLS-1$
		busy = false;
		return new String[] { response };
	}

    /**
     * Retrieves the root domain of the current AI model server
     * @return AI model server domain
     */
    @Override
    public String getAiServerDomain() {
    	try {
    		URI uri = new URI(aiServerUrl);
    		return uri.getHost() + ":" + uri.getPort(); //$NON-NLS-1$
		} catch (URISyntaxException ex) {
			Helper.logWarning(ex, Messages.getString("Chat4AllModelClient.AI_SERVER_DOMAIN_ERROR"), true); //$NON-NLS-1$
		}
    	return null;
    }

    /**
     * Returns the id of this instance in database.
     * @return Record id of this instance.
     */
    @Override
	public int getDbId() { return dbId; }

	/**
	 * Change the state of this instance.
	 * @param enabled The new state of this instance.
	 */
    @Override
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * Gets the state of this instance.
     * @return The state of this instance.
     */
    @Override
	public boolean isEnabled() { return this.enabled; }

	/**
	 * Returns the state of this instance. True if this instance is waiting for an AI model server instance.
	 * @return The state of this instance.
	 */
    @Override
	public boolean isBusy() { return busy; }

	/**
	 * Returns the url of the AI model server.
	 * @return The url of the AI model server.
	 */
    @Override
	public String getAiServerUrl() { return aiServerUrl; }
}











