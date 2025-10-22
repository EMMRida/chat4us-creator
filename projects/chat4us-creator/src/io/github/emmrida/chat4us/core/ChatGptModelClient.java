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
 * Manages the chat flows with OpenAI API service.
 *
 * @author El Mhadder Mohamed Rida
 */
public class ChatGptModelClient implements IChatModelClient {

	public static final String AIQ_PREFIX = "chatgpt_"; //$NON-NLS-1$

	private HttpClient httpClient = null;

	private boolean busy;
	private boolean enabled;
	private String aiServerUrl; // https://api.openai.com/v1/chat/completions
	private int dbId;

	/**
	 *
	 * @param dbId
	 * @param aiServerUrl
	 * @param enabled
	 */
	public ChatGptModelClient(int dbId, String aiServerUrl, boolean enabled) {
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
				if(key.startsWith(AIQ_PREFIX) && !key.equals(AIQ_PREFIX + "api_key")) { //$NON-NLS-1$
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
				data.put("role", "User".equals(hline[0]) ? "user" : "system"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				hline[1] = hline[1].replaceAll("<[^>]+>", "").replaceAll("<br/>", " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				contentLength += hline[1].length() + ("User".equals(hline[0]) ? 4 : 9); //$NON-NLS-1$
				if(contentLength <= maxQueryLength) {
					content.append(hline[1]);
					data.put("content", content.toString()); //$NON-NLS-1$
					messages.add(data);
				} else break;
			} else Helper.logWarning(String.format(Messages.getString("ChatGptModelClient.INVALID_HISTORY_LINE"), i)); //$NON-NLS-1$
		}
		Collections.reverse(messages);
		messages.add(0, gLines);
		data = new HashMap<>();
		data.put("role", "user"); //$NON-NLS-1$ //$NON-NLS-2$
		data.put("content", msg); //$NON-NLS-1$
		messages.add(data);
		gson.put("messages", messages); //$NON-NLS-1$

		String json = new Gson().toJson(gson);
		Helper.logInfo(json);
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
                .header("Authorization", "Bearer " + ses.getAiModelParam(AIQ_PREFIX+"api_key")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                .header("Content-Type", "application/json") //$NON-NLS-1$ //$NON-NLS-2$
                .POST(HttpRequest.BodyPublishers.ofString(jsonInputString, StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException ex) {
        	Helper.logError(ex, Messages.getString("ChatGptModelClient.RESPONSE_ERROR")); //$NON-NLS-1$
        	ses.setEnded(true);
            return Messages.getString("ChatGptModelClient.OFFLINE"); //$NON-NLS-1$
        }
    }

	/**
	 *
	 * @param ses
	 * @return
	 */
	@Override
	public String[] letsChat(ChatSession ses) {
		return userMessage(ses, ""); //$NON-NLS-1$
	}

	/**
	 *
	 * @param ses
	 * @param msg
	 * @return
	 */
	@Override
	public String[] userMessage(ChatSession ses, String msg) {
    	busy = true;
		long l = System.currentTimeMillis();
		ses.addHistoryChatMessage(false, msg.trim());
		String response = sendMsgToModelServer(ses, msg);
		Helper.logInfo(response);
		response = (String)Helper.getValueFromJsonPath(response, "choices/0/message/content"); // TODO : Check content path validity //$NON-NLS-1$
		if(response == null) {
			response = ses.getDefaultErrorMessage();
			ses.setEnded(true);
		}

		response = response.replaceAll("\\n", "<br/>").replaceAll("\\\"", "&quot;"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ses.addHistoryChatMessage(true, response.trim());
		if((System.currentTimeMillis() - l) / 1000L > io.github.emmrida.chat4us.gui.MainWindow.getSettings().getAiLogOnLongResponse()*60)
			Helper.logWarning(String.format(Messages.getString("ChatGptModelClient.AI_RESPONSE_TIME"), (System.currentTimeMillis() - l) / 1000L, msg)); //$NON-NLS-1$
		busy = false;
		return new String[] { response };
	}

	/**
	 *
	 * @return
	 */
	@Override
	public int getDbId() { return this.dbId; }

	/**
	 *
	 * @return
	 */
	@Override
	public String getAiServerUrl() {
    	try {
    		URI uri = new URI(aiServerUrl);
    		return uri.getHost() + ":" + uri.getPort(); //$NON-NLS-1$
		} catch (URISyntaxException ex) {
			Helper.logWarning(ex, Messages.getString("ChatGptModelClient.AI_SERVER_DOMAIN_ERROR"), true); //$NON-NLS-1$
		}
    	return null;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public String getAiServerDomain() { return this.aiServerUrl; }

	/**
	 *
	 * @return
	 */
	@Override
	public boolean isEnabled() { return this.enabled; }

	/**
	 *
	 * @param enabled
	 */
	@Override
	public void setEnabled(boolean enabled) { this.enabled = enabled; }

	/**
	 *
	 * @return
	 */
	@Override
	public boolean isBusy() { return this.busy; }
}














