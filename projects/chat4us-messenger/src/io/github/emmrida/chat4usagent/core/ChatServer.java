/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.core;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.xnio.Options;
import com.google.gson.Gson;

import io.github.emmrida.chat4usagent.gui.MainWindow;
import io.github.emmrida.chat4usagent.util.Helper;
import io.github.emmrida.chat4usagent.util.Messages;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.util.Headers;

/**
 * The ChatServer class that manages chats with remote users via a remote chat bot server.
 *
 * @author El Mhadder Mohamed Rida
 */
public class ChatServer {
	public static final String KEYSTORE_PFX = "./cert/keystore.pfx"; //$NON-NLS-1$
	public static final String CHAT_MSG_SEPARATOR = "<<br/>>"; //$NON-NLS-1$

	private Undertow webServer;
	private boolean started;
	private boolean terminated;
	private String host;
	private int port;

	private Map<String, ChatSession> chatSessions;
	private List<ChatListener> chatListeners;

	/**
	 * Init this ChatServer instance.
	 */
	public ChatServer() {
		this.started = false;
		this.terminated = false;
		this.host = null;
		this.port = 0;
		this.webServer = null;
		this.chatSessions = new HashMap<>();
		this.chatListeners = new ArrayList<>();
	}

	/**
	 * Init this ChatServer instance using a host address and port number.
	 * @param host Host address
	 * @param port Port number
	 */
	public ChatServer(String host, int port) {
		this();
		this.host = host;
		this.port = port;
	}

	/**
	 * Start an Http/unsecure server, this was first coded for testing purposes.
	 */
	public void startServer() {
		this.startServer(this.host, this.port);
	}

	/**
	 * Start an Http/unsecure server, this was first coded for testing purposes.
	 * @param host Host address
	 * @param port Port number
	 */
	public void startServer(String host, int port) {
		Helper.requiresNotEmpty(host);
		this.host = host;
		this.port = port;
		try {
	        HttpHandler prefPathHandler = new PathHandler()
                    .addPrefixPath("/letschat", exchange -> { //$NON-NLS-1$
                        processUserLetsChat(exchange);
                    })
                    .addPrefixPath("/message", exchange -> { //$NON-NLS-1$
                        processUserMessage(exchange);
                    });
	        HttpHandler fallbackHandler = new PathHandler() {
	        	@Override
	        	public void handleRequest(HttpServerExchange exchange) throws Exception {
		        	processError(exchange, Messages.getString("ChatServer.RES404"), 404); //$NON-NLS-1$
	        	}
	        };

	        HttpHandler rootHandler = new PathHandler()
	                .addPrefixPath("/", exchange -> { //$NON-NLS-1$
		                prefPathHandler.handleRequest(exchange);
		                if(!exchange.isResponseComplete())
		                	fallbackHandler.handleRequest(exchange);
		            });
	        webServer = Undertow.builder()
	                .addHttpListener(port, host)
	                .setHandler(rootHandler)
	                .build();
	        webServer.start();
	        this.started = true;
		} catch(Exception ex) {
			this.host = null;
			this.port = 0;
			Helper.logError(ex, Messages.getString("ChatServer.SERVER_RUN_ERROR") + host + ":" + port, true); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Start a secure chat server. Remote chat bot server will redirect remote users messages into here
	 * and wait for agent responses.
	 * @param pfxPswd Key store password.
	 * @return True if the server started successfully
	 */
	public boolean startSecureServer(char[] pfxPswd) {
		return startSecureServer(this.host, this.port, pfxPswd);
	}

	/**
	 * Start a secure chat server. Remote chat bot server will redirect remote users messages into here
	 * and wait for agent responses.
	 * @param host Host address
	 * @param port Port number
	 * @param pfxPswd Key store password.
	 * @return True if the server started successfully
	 */
	public boolean startSecureServer(String host, int port, char[] pfxPswd) {
		this.host = host;
		this.port = port;
		try {
	        KeyStore keyStore = KeyStore.getInstance("PKCS12"); //$NON-NLS-1$
	        try (FileInputStream fis = new FileInputStream(KEYSTORE_PFX)) {
	            keyStore.load(fis, pfxPswd);
	        }

	        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	        keyManagerFactory.init(keyStore, pfxPswd);

	        SSLContext sslContext = SSLContext.getInstance("TLS"); //$NON-NLS-1$
	        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

	        HttpHandler prefPathHandler = new PathHandler()
                   .addPrefixPath("/letschat", exchange -> { //$NON-NLS-1$
                       processUserLetsChat(exchange);
                   })
                   .addPrefixPath("/message", exchange -> { //$NON-NLS-1$
                       processUserMessage(exchange);
                   });
	        HttpHandler fallbackHandler = exchange -> {
	       		if(!exchange.isResponseComplete())
	       			processError(exchange, Messages.getString("ChatServer.RES404"), 404); //$NON-NLS-1$
	        };

	        HttpHandler rootHandler = exchange -> {
			    prefPathHandler.handleRequest(exchange);
			    if(!exchange.isResponseComplete()) {
			        exchange.addExchangeCompleteListener((exchng, next) -> {
			            if(!exchng.isResponseComplete()) {
			            	try {
			 					fallbackHandler.handleRequest(exchng);
		 					} catch (Exception ex) {
	 							Helper.logError(ex, Messages.getString("ChatServer.FBACK_HANDLER_FAILED"), true); //$NON-NLS-1$
 							}
			            }
			            next.proceed();
			       });
			   }
           };

	        // Configure Undertow server
	        webServer = Undertow.builder()
	                .addHttpsListener(port, host, sslContext)
	                .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
	                .setWorkerOption(Options.WORKER_IO_THREADS, 4)
	                .setWorkerOption(Options.TCP_NODELAY, true)
	                .setHandler(rootHandler)
	                .build();
	        webServer.start();
	        this.started = true;
	        return true;
		} catch(Exception ex) {
			this.host = null;
			this.port = 0;
			Helper.logError(ex, Messages.getString("ChatServer.SERVER_RUN_ERROR") + host + ":" + port, true); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
  }


	/**
	 * Stop and release the server.
	 */
	public void stopServer() {
		if(webServer != null) {
			webServer.stop();
			webServer = null;
			terminated = true;
		}
	}

	/**
	 * Starts a new user chat session.
	 * @param exchange Undertow Http server exchange.
	 */
	public void processUserLetsChat(HttpServerExchange exchange) {
		if("POST".equalsIgnoreCase(exchange.getRequestMethod().toString())) { //$NON-NLS-1$
			String ct = exchange.getRequestHeaders().getFirst(Headers.CONTENT_TYPE);
			if(ct.equalsIgnoreCase("application/x-www-form-urlencoded")) { //$NON-NLS-1$
				exchange.getRequestReceiver().receiveFullString((xchng, body) -> {
					String srvIp = Helper.ipv6Compress(xchng.getSourceAddress().getAddress().getHostAddress());
					String srvDomain = Helper.ipv6Compress(xchng.getSourceAddress().getAddress().getHostName());
					List<String> ipWhiteList = MainWindow.getSettings().getIpWhiteList();
					if(!ipWhiteList.contains(srvIp) && !ipWhiteList.contains(srvDomain)) {
						Helper.logWarning(Messages.getString("ChatServer.IP_DOMAIN_NOT_WHITELISTED") + srvIp + "/" + srvDomain); //$NON-NLS-1$ //$NON-NLS-2$
						processError(xchng, "403 Forbidden", 403); //$NON-NLS-1$
						return;
					}
					Map<String, String> params = Helper.parsePostData(body);
					String chat = params.get("chat"); //$NON-NLS-1$
					String usrId = params.get("usr_id"); //$NON-NLS-1$
					int timeout = Integer.valueOf(params.get("timeout")); //$NON-NLS-1$
					if(Helper.isNullOrEmpty(chat) || Helper.isNullOrEmpty(usrId)) {
						processError(xchng, Messages.getString("ChatServer.MISSING_PARAMS_ERROR"), 400); //$NON-NLS-1$
						return;
					}
					if(!chatSessions.containsKey(usrId)) {
						List<String> chatMsgList = new ArrayList<>();
						String[] msgs = chat.split(CHAT_MSG_SEPARATOR);
						String[] parts;
						for(String msg : msgs) {
							parts = msg.split(":", 2); //$NON-NLS-1$
							chatMsgList.add(parts[0].trim() + ":" + parts[1].trim()); //$NON-NLS-1$
						}
						xchng.dispatch(() -> {
							ChatSession ses = new ChatSession(usrId, chatMsgList);
							ses.setTimeout(timeout);
							chatSessions.put(usrId, ses);
							for(ChatListener l : chatListeners) {
								l.onLetsChat(xchng, ses, chat);
								if(xchng.isResponseComplete())
									return;
							}
							if((chatListeners.size() > 0) && !xchng.isResponseComplete()) {
								xchng.addExchangeCompleteListener((exchng, next) -> {
									if(!exchng.isResponseComplete()) {
										for(ChatListener l : chatListeners)
											l.onMessageTimeout(ses);
									}
									next.proceed();
								});
							}
						});
					} else {
						Helper.logWarning(Messages.getString("ChatServer.OPEN_SESSION_ERROR")); //$NON-NLS-1$
						processError(xchng, Messages.getString("ChatServer.SESSION_EXISTANT_ERROR"), 400); //$NON-NLS-1$
						for(ChatListener l : chatListeners)
							l.onChatError(xchng, chatSessions.get(usrId));
					}
				});
			} else processError(exchange, Messages.getString("ChatServer.MISSING_PARAMS_ERROR"), 400); //$NON-NLS-1$
		} else processError(exchange, Messages.getString("ChatServer.INVALID_PARAMS_ERROR"), 400); //$NON-NLS-1$
	}

	/**
	 * Processes a user message. This will show a message to the agent/operator and wait for his/her response
	 * @param exchange  Undertow Http server exchange.
	 */
	public void processUserMessage(HttpServerExchange exchange) {
		if("POST".equalsIgnoreCase(exchange.getRequestMethod().toString())) { //$NON-NLS-1$
			String ct = exchange.getRequestHeaders().getFirst(Headers.CONTENT_TYPE);
			if(ct.equalsIgnoreCase("application/x-www-form-urlencoded")) { //$NON-NLS-1$
				exchange.getRequestReceiver().receiveFullString((xchng, body) -> {
					String srvIp = Helper.ipv6Compress(xchng.getSourceAddress().getAddress().getHostAddress());
					String srvDomain = Helper.ipv6Compress(xchng.getSourceAddress().getAddress().getHostName());
					List<String> ipWhiteList = MainWindow.getSettings().getIpWhiteList();
					if(!ipWhiteList.contains(srvIp) && !ipWhiteList.contains(srvDomain)) {
						Helper.logWarning(Messages.getString("ChatServer.IP_DOMAIN_NOT_WHITELISTED") + srvIp + "/" + srvDomain); //$NON-NLS-1$ //$NON-NLS-2$
						processError(xchng, "403 Forbidden", 403); //$NON-NLS-1$
						return;
					}
					Map<String, String> params = Helper.parsePostData(body);
					String usrId = params.get("usr_id"); //$NON-NLS-1$
					String usrMsg = params.get("usr_msg"); //$NON-NLS-1$
					int timeout = Integer.valueOf(params.get("timeout")); //$NON-NLS-1$
					if(Helper.isNullOrEmpty(usrMsg) || Helper.isNullOrEmpty(usrId)) {
						processError(xchng, Messages.getString("ChatServer.MISSING_PARAMS_ERROR"), 400); //$NON-NLS-1$
						return;
					}
					xchng.dispatch(() -> {
						ChatSession ses = chatSessions.get(usrId);
						if(ses != null) {
							ses.setTimeout(timeout);
							for(ChatListener l : chatListeners) {
								l.onIncomingMessage(xchng, ses, usrMsg);
								if(xchng.isResponseComplete())
									return;
							}
							if((chatListeners.size() > 0) && !xchng.isResponseComplete()) {
								xchng.addExchangeCompleteListener((exchng, next) -> {
									if(!exchng.isResponseComplete()) {
										for(ChatListener l : chatListeners)
											l.onMessageTimeout(ses);
									}
									next.proceed();
								});
							}
						} else processError(xchng, Messages.getString("ChatServer.MISSING_SESSION_ERROR"), 400); //$NON-NLS-1$
					});
				});
			} else processError(exchange, Messages.getString("ChatServer.MISSING_PARAMS_ERROR"), 400); //$NON-NLS-1$
		} else processError(exchange, Messages.getString("ChatServer.INVALID_PARAMS_ERROR"), 400); //$NON-NLS-1$
	}

	/**
	 * 400 Bad request ex: missing/bad parameters
	 * 401 Unathorized : wrong credentials/tokens or session expired or needs to be re/connected
	 * 403 Forbidden : Not authorized to access this resource
	 * 404 Not found : resource not found
	 * 503 Service unavailable : When !enabled for example.
	 *
	 * @param exchange Undertow Http server exchange.
	 * @param errorMsg Error message to return to the chat bot IFrame.
	 * @param errorCode Error code to return to the chat bot IFrame.
	 */
	public void processError(HttpServerExchange exchange, String errorMsg, int errorCode) {
		String ip = exchange.getSourceAddress().getAddress().getHostAddress();
		String domain = exchange.getSourceAddress().getAddress().getHostName();
		Helper.logWarning(String.format(Messages.getString("ChatServer.CHATSERVER_API_ERROR"), ip, domain, exchange.getRequestURI().toString(), errorMsg, errorCode), false); //$NON-NLS-1$
		exchange.setStatusCode(errorCode);
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json"); //$NON-NLS-1$
		Map<String, Object> rslt = new HashMap<>();
		rslt.put("ERROR_MESSAGE", errorMsg); //$NON-NLS-1$
		rslt.put("ERROR_CODE", errorCode); //$NON-NLS-1$
		rslt.put("STATUS", "ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
		exchange.getResponseSender().send(new Gson().toJson(rslt));
	}

	/**
	 * @return true if the server is terminated
	 */
	public boolean isTerminated() { return this.terminated; }

	/**
	 * @return true if the server is started
	 */
	public boolean isStarted() { return this.started; }

	/**
	 * @return true if the server is enabled
	 */
	public String getHost() { return this.host; }

	/**
	 * @return true if the server is enabled
	 */
	public int getPort() { return this.port; }

	/**
	 * Get a chat session identified by user id
	 * @param userId User id to identify the chat session
	 * @return Chat session or null
	 */
	public ChatSession getChatSession(String userId) { return this.chatSessions.get(userId); }

	/**
	 * Remove a chat session identified by user id
	 * @param userId User id to identify the chat session
	 */
	public void removeChatSession(String userId) { this.chatSessions.remove(userId); }

	/**
	 * Add a listener to monitor the ChatServer activities.
	 * @param listener A listener
	 */
	public void addChatListener(ChatListener listener) { this.chatListeners.add(listener); }

	/**
	 * Remove a listener
	 * @param listener A listener
	 */
	public void removeChatListener(ChatListener listener) { this.chatListeners.remove(listener); }

	///////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	/**
	 * Chat listener
	 */
	public static interface ChatListener {
		/**
		 * Fired when a chat session is established
		 * @param exchange Undertow Http server exchange
		 * @param ses Chat session
		 * @param chat Should contain all chat history.
		 */
		void onLetsChat(HttpServerExchange exchange, ChatSession ses, String chat);

		/**
		 * Fired when a message arrives.
		 * @param exchange Undertow Http server exchange
		 * @param ses Chat session
		 * @param message Incoming remote user message.
		 */
		void onIncomingMessage(HttpServerExchange exchange, ChatSession ses, String message);

		/**
		 * Fired when an agent/operator took too much time to respond to remote user message.
		 * @param ses Chat session
		 */
		void onMessageTimeout(ChatSession ses);

		/**
		 * Fired when an exception raises during a chat session
		 * @param exchange Undertow Http server exchange
		 * @param ses Chat session
		 */
		void onChatError(HttpServerExchange exchange, ChatSession ses);
	}
}













