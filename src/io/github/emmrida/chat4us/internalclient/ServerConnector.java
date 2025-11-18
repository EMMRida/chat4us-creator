/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.internalclient;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import io.github.emmrida.chat4us.gui.MainWindow;
import io.github.emmrida.chat4us.util.Messages;

/**
 * Manages the connection to the bot server
 */
public class ServerConnector {

    private final String botUrl; //URL of the bot server
    private final HttpClient httpClient; //HTTP client for making requests

    private String serverToken; // Server token returned after authentication

    /**
     * Constructor for ServerConnector
     * @param botUrl Base URL of the bot server
     */
    public ServerConnector(String botUrl) {
        this.botUrl = normalizeUrl(botUrl);
        this.httpClient = createHttpClient();
        this.serverToken = null;
    }

    /**
     * Normalizes the URL by ensuring it doesn't end with a slash
     * @param url The URL to normalize
     * @return The normalized URL
     */
    private String normalizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.getString("ServerConnector.EX_NULL_URL")); //$NON-NLS-1$
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url; //$NON-NLS-1$
    }

    /**
     * Creates an HTTP client that is used to communicate with the bot server
     * @return The HTTP client
     */
    private HttpClient createHttpClient() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS"); //$NON-NLS-1$
            SSLParameters sslParameters = new SSLParameters();
        	if(MainWindow.isSelfSigned()) {
	            TrustManager[] trustAllCerts = new TrustManager[] {
	                new X509TrustManager() {
	    				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
	    				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
	    				public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
	                }
	            };

				sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
	            SSLContext.setDefault(sslContext);
	            sslParameters.setEndpointIdentificationAlgorithm(null);
	            HostnameVerifier allHostsValid = (hostname, session) -> true;
	            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        	} else {
        		sslContext.init(null, null, null);
        	}

            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .sslParameters(sslParameters)
                    .connectTimeout(Duration.ofMinutes(MainWindow.getSettings().getAiLogOnLongResponse()))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(Messages.getString("ServerConnector.EX_HTTPS_CLIENT_CREATION"), e); //$NON-NLS-1$
        }
    }

    /**
     * Performs login operation and stores the token.
     * @param hdr Auth key included in query header
     * @param key Auth key included in post query
     * @return true if login successful, false otherwise
     */
    public boolean login(String hdr, String key) {
        if (serverToken != null) {
            System.err.println(Messages.getString("ServerConnector.LOG_ALREADY_LOGGED_IN")); //$NON-NLS-1$
            return true;
        }

        try {
        	// Credentials to login
            Map<String, String> formData = Map.of(
                "auth_key", key //$NON-NLS-1$
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(botUrl + "/ilogin")) //$NON-NLS-1$
                    .header("Content-Type", "application/x-www-form-urlencoded") //$NON-NLS-1$ //$NON-NLS-2$
                    .header("auth_key", hdr) //$NON-NLS-1$
                    .POST(BodyPublishers.ofString(buildQueryString(formData)))
                    .timeout(Duration.ofMinutes(MainWindow.getSettings().getAiLogOnLongResponse()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            ApiResponse apiResponse = parseResponseWithAdditionalFields(response.body());

            if (apiResponse != null && apiResponse.isSuccess() && apiResponse.getToken() != null) {
                serverToken = apiResponse.getToken();
                return true;
            } else {
                System.err.println(Messages.getString("ServerConnector.LOG_LOGIN_FAILURE_STATUS") + //$NON-NLS-1$
                    (apiResponse != null ? apiResponse.isSuccess() : "null")); //$NON-NLS-1$
                serverToken = null;
                return false;
            }
        } catch (Exception e) {
            System.err.println(Messages.getString("ServerConnector.EX_LOGIN_ERROR") + e.getMessage()); //$NON-NLS-1$
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Performs logout operation and removes the token
     * @return true if logout successful or no token was present, false on error
     */
    public boolean logout() {
        if (serverToken == null) {
            return true; // No token to logout, consider it successful
        }

        try {
            Map<String, String> formData = Map.of("token", serverToken); //$NON-NLS-1$

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(botUrl + "/logout")) //$NON-NLS-1$
                    .header("Content-Type", "application/x-www-form-urlencoded") //$NON-NLS-1$ //$NON-NLS-2$
                    .POST(BodyPublishers.ofString(buildQueryString(formData)))
                    .timeout(Duration.ofMinutes(MainWindow.getSettings().getAiLogOnLongResponse()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

            serverToken = null;
            return true;
        } catch (Exception e) {
            System.err.println(Messages.getString("ServerConnector.EX_LOGOUT_ERROR") + e.getMessage()); //$NON-NLS-1$
            serverToken = null;
            return false;
        }
    }

    /**
     * Initiates a chat session
     * @param userId The user ID to start chat with
     * @return ApiResponse if successful, null otherwise
     */
    public ApiResponse letsChat(String userId) {
        if (serverToken == null) {
            return null;
        }

        try {
            Map<String, String> formData = Map.of(
                "usr_id", userId, //$NON-NLS-1$
                "token", serverToken //$NON-NLS-1$
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(botUrl + "/letschat")) //$NON-NLS-1$
                    .header("Content-Type", "application/x-www-form-urlencoded") //$NON-NLS-1$ //$NON-NLS-2$
                    .POST(BodyPublishers.ofString(buildQueryString(formData)))
                    .timeout(Duration.ofMinutes(MainWindow.getSettings().getAiLogOnLongResponse()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            ApiResponse apiResponse = parseResponseWithAdditionalFields(response.body());

            return (apiResponse != null && apiResponse.isSuccess()) ? apiResponse : null;
        } catch (Exception e) {
            System.err.println(Messages.getString("ServerConnector.EX_LETSCHAT_ERROR") + e.getMessage()); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * Sends a message to the chat bot server
     * @param userId The ID of the user sending the message
     * @param message The message content
     * @return ApiResponse if successful, null otherwise
     */
    public ApiResponse sendMessage(String userId, String message) {
        if (serverToken == null) {
            return null;
        }

        try {
            Map<String, String> formData = Map.of(
                "usr_id", userId, //$NON-NLS-1$
                "token", serverToken, //$NON-NLS-1$
                "message", message //$NON-NLS-1$
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(botUrl + "/message")) //$NON-NLS-1$
                    .header("Content-Type", "application/x-www-form-urlencoded") //$NON-NLS-1$ //$NON-NLS-2$
                    .POST(BodyPublishers.ofString(buildQueryString(formData)))
                    .timeout(Duration.ofMinutes(MainWindow.getSettings().getAiLogOnLongResponse()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            ApiResponse apiResponse = parseResponseWithAdditionalFields(response.body());

            return (apiResponse != null && apiResponse.isSuccess()) ? apiResponse : null;
        } catch (Exception e) {
            System.err.println(Messages.getString("ServerConnector.EX_SENDMESSAGE_ERROR") + e.getMessage()); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * Helper method to build URL-encoded query string
     * @param parameters Query parameters
     */
    private String buildQueryString(Map<String, String> parameters) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (result.length() > 0) {
                result.append("&"); //$NON-NLS-1$
            }
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                  .append("=") //$NON-NLS-1$
                  .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return result.toString();
    }

    /**
     * Helper method to parse JSON response with additional fields.
     * See ApiResponse class for more details...
     * @param jsonResponse The JSON response string
     * @return ApiResponse if successful, null otherwise
     */
    private ApiResponse parseResponseWithAdditionalFields(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            return null;
        }

        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            String status = jsonObject.has("STATUS") ? jsonObject.get("STATUS").getAsString() : null; //$NON-NLS-1$ //$NON-NLS-2$
			String token = jsonObject.has("TOKEN") ? jsonObject.get("TOKEN").getAsString() : null; //$NON-NLS-1$ //$NON-NLS-2$
			boolean chatEnded = jsonObject.has("CHAT_ENDED") ? jsonObject.get("CHAT_ENDED").getAsBoolean() : false; //$NON-NLS-1$ //$NON-NLS-2$
			String chatState = jsonObject.has("CHAT_STATE") ? jsonObject.get("CHAT_STATE").getAsString() : null; //$NON-NLS-1$ //$NON-NLS-2$
			String locale = jsonObject.has("LOCALE") ? jsonObject.get("LOCALE").getAsString() : null; //$NON-NLS-1$ //$NON-NLS-2$
			boolean chatbotWaiting = jsonObject.has("CHATBOT_WAITING") ? jsonObject.get("CHATBOT_WAITING").getAsBoolean() : false; //$NON-NLS-1$ //$NON-NLS-2$
			String[] messages = null;
			if(jsonObject.has("CHATBOT_MESSAGE")) { //$NON-NLS-1$
				JsonArray ja = jsonObject.get("CHATBOT_MESSAGE").getAsJsonArray(); //$NON-NLS-1$
				messages = new String[ja.size()];
				for(int i = 0; i < ja.size(); i++)
					messages[i] = ja.get(i).getAsString();
			} else messages = new String[0];

            // Create ApiResponse with additional fields if needed
            ApiResponse response = new ApiResponse(status, token, messages, chatEnded, chatState, locale, chatbotWaiting);

            return response;
        } catch (Exception e) {
            System.err.println(Messages.getString("ServerConnector.EX_JSON_PARSE_ERROR") + e.getMessage()); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * Utility method to check if currently logged in
     * @return True if logged in
     */
    public boolean isLoggedIn() {
        return serverToken != null && !serverToken.isEmpty();
    }

    /**
     * Gets the current token
     * @return Current token
     */
    public String getCurrentToken() {
        return serverToken;
    }

    /**
     * Manual token management for advanced scenarios
     * @param token New token value
     */
    public void setToken(String token) {
    	serverToken = token;
    }

    /**
     * Direct JSON parsing utility method
     * @param json JSON string
     * @return ApiResponse if successful, null otherwise
     */
    public ApiResponse parseJsonResponse(String json) {
        return parseResponseWithAdditionalFields(json);
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * ApiResponse class using Gson annotations
     */
    public static class ApiResponse {
        @SerializedName("STATUS")
        private String status; // OK or ERROR

        @SerializedName("TOKEN")
		private String token; // Exists only on login response

        @SerializedName("MESSAGES")
        private String[] messages; // Array of messages from chatbot, usually 1 message

        @SerializedName("CHAT_ENDED")
        private boolean chatEnded; // True if the chat has ended and the client is disconnected

        @SerializedName("CHAT_STATE")
        private String chatState; // Current chat state. Could be either CHATBOT, AIMODEL or AGENT

        @SerializedName("LOCALE")
        private String locale; // Current chat session locale

        @SerializedName("CHATBOT_WAITING")
        private boolean chatbotWaiting; // True if the chatbot is waiting for user input, use when switching to AIMODEL (Send '...' back).

        // Default constructor for Gson
        public ApiResponse() {}

        /**
         * ApiResponse constructor
         * @param status Response status
         * @param token Response token
         * @param messages Array of messages from chatbot
         * @param chatEnded True if the chat has ended and the client is disconnected
         * @param chatState Current chat state. Could be either CHATBOT, AIMODEL or AGENT
         * @param locale Current chat session locale
         */
		public ApiResponse(String status, String token, String[] messages, boolean chatEnded, String chatState, String locale, boolean chatbotWaiting) {
            this.status = status;
			this.token = token;
			this.messages = messages;
			this.chatEnded = chatEnded;
			this.chatState = chatState;
			this.locale = locale;
			this.chatbotWaiting = chatbotWaiting;
        }

		public String getToken() { return token; } // Return communication token
        public boolean isSuccess() { return "OK".equals(status); } // Return true if status is OK //$NON-NLS-1$
		public boolean isChatEnded() { return chatEnded; } // Return true if chat has ended
		public boolean isChatbotWaiting() { return chatbotWaiting; } // Return true if chatbot is waiting for user input
		public String getChatState() { return chatState; } // Return current chat state
		public String[] getMessages() { return messages; } // Return array of messages from chatbot
		public String getLocale() { return locale; } // Return current chat session locale

		/**
		 * Get chat state icon name based on current chat state
		 * @return Chat state icon name
		 */
		public String getChatStateIconName() {
			if (chatState != null) {
				if(chatState.equals("AIMODEL")) return "ai_assistant"; //$NON-NLS-1$ //$NON-NLS-2$
				if(chatState.equals("AGENT")) return "agent"; //$NON-NLS-1$ //$NON-NLS-2$
				if(chatState.equals("CHATBOT")) return "chatbot"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			return null;
		}

		@Override
		public String toString() {
			return "ApiResponse{" + //$NON-NLS-1$
					"status='" + status + '\'' + //$NON-NLS-1$
					", token='" + token + '\'' + //$NON-NLS-1$
					", messages=" + Arrays.toString(messages) + //$NON-NLS-1$
					", chatEnded=" + chatEnded + //$NON-NLS-1$
					", chatState='" + chatState + '\'' + //$NON-NLS-1$
					", locale='" + locale + '\'' + //$NON-NLS-1$
					", chatbotWaiting=" + chatbotWaiting + //$NON-NLS-1$
					'}';
		}
    }
}