/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyStore;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.swing.SwingUtilities;
import org.xnio.Options;

import com.caoccao.javet.exceptions.JavetCompilationException;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.converters.JavetProxyConverter;
import com.caoccao.javet.values.reference.V8ValueGlobalObject;
import com.google.gson.Gson;

import io.github.emmrida.chat4us.core.ChatSession.ChatSessionState;
import io.github.emmrida.chat4us.gui.CertGenDialog;
import io.github.emmrida.chat4us.gui.MainWindow;
import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;
import io.github.emmrida.chat4us.util.ScriptEx;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.util.Headers;

/**
 * Manages chats between remote users, AI model, agent/messenger apps and chat flows.
 *
 * @author El Mhadder Mohamed Rida
 */
public class ChatServer {

	private boolean started;
	private boolean terminated;

	private int dbId;
	private String host;
	private int port;
	private int groupId;
	private boolean enabled;
	private String description;
	private int aiContextSize;

	private char[] internalPostAuthKey;		// Internal chat client auth key sent in post data
	private char[] internalHeaderAuthKey;	// Internal chat client auth key sent in header

	private ChatClient chatClient;
	private Map<String, WebsiteSession> webSessions;	// Key: "token"
	private Map<String, UserSession> userSessions;		// Key: "userId"
	private Map<Integer, WebsiteRecord> websiteRecords;	// Key: db id record

	private List<ChatServerListener> listeners;

	private Undertow server;

	private Thread serverThread;
	private ExecutorService executor;

	/**
	 * Load a website record from memory, if not found then from database.
	 * @param id Id of the website
	 * @return WebsiteRecord object or null on error.
	 */
	public WebsiteRecord getWebsiteRecord(int id) {
		WebsiteRecord wr = this.websiteRecords.get(id);
		if(wr != null)
			return wr;
		String q = "SELECT * FROM websites WHERE id = " + id + ";"; //$NON-NLS-1$ //$NON-NLS-2$
		Connection con = MainWindow.getDBConnection();
		try(ResultSet rs = con.createStatement().executeQuery(q)) {
			if(rs.next()) {
				wr = new WebsiteRecord(rs);
				this.websiteRecords.put(id, wr);
				return wr;
			}
		} catch (SQLException ex) {
			Helper.logError(ex, String.format(Messages.getString("ChatServer.WEBSITE_LOADING_ERROR_ID"), id), false); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Load a website record from memory, if not found then from the database.
	 * @param ip IP of the website
	 * @param domain Domain of the website
	 * @param key1 Hash of the key1
	 * @param key2 Hash of the key2
	 * @return WebsiteRecord object or null on error.
	 */
	public WebsiteRecord getWebsiteRecord(String ip, String domain, String key1, String key2) {
		String shaId;
		try {
			shaId = Helper.hashString(domain + ip + key1 + key2);
		} catch (Exception ex) {
			Helper.logError(ex, Messages.getString("ChatServer.AUTH_KEY_HASH_ERROR"), false); //$NON-NLS-1$
			return null;
		}
		for(WebsiteRecord wr : this.websiteRecords.values())
			if(wr.getShaId().equals(shaId))
				return wr;

		WebsiteRecord wr = null;
		String q = "SELECT * FROM websites WHERE sha_id = ?;"; //$NON-NLS-1$
		Connection con = MainWindow.getDBConnection();
		try(PreparedStatement ps = con.prepareStatement(q)) {
			ps.setString(1, shaId);
			try(ResultSet rs = ps.executeQuery()) {
				if(rs.next()) {
					wr = new WebsiteRecord(rs);
					this.websiteRecords.put(wr.getId(), wr);
				}
			}
		} catch (SQLException ex) {
			Helper.logError(ex, String.format(Messages.getString("ChatServer.WEBSITE_LOADING_ERROR_IPD"), ip, domain), true); //$NON-NLS-1$
		}
		return wr;
	}
	public void websitesTableChanged() { this.websiteRecords.clear(); }

	/**
	 * Store ended chat sessions into chat files.
	 * @param force If true then store all chat sessions.
	 */
	private void storeEndedChats(boolean force) {
		int n = 0;
		long timeout = io.github.emmrida.chat4us.gui.MainWindow.getSettings().getChatSessionsTimeoutMinutes()*60*1000;
		long now = System.currentTimeMillis();
		boolean tout;
		File file;
		UserSession us;
		ChatSession ses;
		Iterator<Map.Entry<String, UserSession>> it = this.userSessions.entrySet().iterator();
		while(it.hasNext()) {
			us = it.next().getValue();
			ses = us.getChatSession();
			tout = now - ses.getLastMsgTime() > timeout;
			if(tout || force || ses.isEnded()) {
				if(tout && !ses.isEnded()) {
					ses.setEnded(true);
					Helper.logInfo(String.format(Messages.getString("ChatServer.CHAT_MARKED_ENDED"), ses.getUserId())); //$NON-NLS-1$
				}
				if(ses.isEnded() || force) {
					file = new File("./chat/" + ses.getUserId() + "-" + Helper.toDate(Instant.ofEpochMilli(ses.getCreationTime()), "dd-MM-yyyy_HH-mm-ss") + ".txt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
						bw.write("--Chat " + ses.getUserId() + System.lineSeparator()); //$NON-NLS-1$
						bw.write("--Date +> Start : " + Helper.toDate(Instant.ofEpochMilli(ses.getCreationTime()), "dd-MM-yyyy HH:mm:ss") + "\tEnd : " + Helper.toDate(Instant.ofEpochMilli(ses.getLastMsgTime()), "dd-MM-yyyy HH:mm:ss") + System.lineSeparator()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						bw.write("--Session Variables :" + System.lineSeparator()); //$NON-NLS-1$
						for(Map.Entry<String, String> var : ses.getVarsSet())
							bw.write("\t" + var.getKey() + " = " + var.getValue() + System.lineSeparator()); //$NON-NLS-1$ //$NON-NLS-2$
						bw.write("--Messages :" + System.lineSeparator()); //$NON-NLS-1$
						for(int i = 0; i < ses.getHistoryChatMessagesCount(); i++)
							bw.write(ses.getHistoryChatMessage(i).replaceAll("<[^>]+>", "") + System.lineSeparator()); //$NON-NLS-1$ //$NON-NLS-2$
						if(!ses.isEnded()) {
							bw.write("-- NOT ENDED --" + System.lineSeparator()); //$NON-NLS-1$
							Helper.logInfo(String.format(Messages.getString("ChatServer.CHAT_FORCED_ENDED"), ses.getUserId())); //$NON-NLS-1$
						} else bw.write("-- ENDED --" + System.lineSeparator()); //$NON-NLS-1$
						// TODO : Clean user sessions
						it.remove();
						n++;
					} catch (IOException ex) {
						Helper.logWarning(ex, String.format(Messages.getString("ChatServer.CHAT_SAVE_ERROR"), file.getName()), false); //$NON-NLS-1$
					}
				}
			}
		}
		if(n > 0) {
			Helper.logInfo(String.format(Messages.getString("ChatServer.CHAT_SAVE_SUCCESS"), n)); //$NON-NLS-1$
			fireStatsChanged(this);
		}
	}

	/**
	 * Init a ChatServer instance.
	 * @param dbId Record id of this instance in database.
	 */
	public ChatServer(int dbId) {
		this.started = false;
		this.terminated = false;
		this.dbId = dbId;
		this.host = null;
		this.port = 0;
		this.aiContextSize = MainWindow.getSettings().getAiQueryMaxLength();
		this.enabled = false;
		this.server = null;
		this.chatClient = new ChatClient();
		this.webSessions = new HashMap<>();
		this.userSessions = new HashMap<>();
		this.websiteRecords = new HashMap<>();
		this.listeners = new ArrayList<>();

		this.serverThread = new Thread() {
			@Override
			public void run() {
				while(!terminated) {
					try { Thread.sleep(60000); } catch(Exception ignored) { }
					storeEndedChats(false);
				}
			}
		};
		this.serverThread.setDaemon(true);
		this.serverThread.start();
	}

	/**
	 * Init a ChatServer instance.
	 * @param dbId Record id of this instance in database.
	 * @param host Host name
	 * @param port Port number
	 * @param groupId Chat server group id
	 * @param aiContextSize AI context size
	 * @param aiServers List of all AI models servers.
	 */
	public ChatServer(int dbId, String host, int port, int groupId, int aiContextSize, List<Object[]> aiServers) {
		this(dbId);
		Helper.requiresNotEmpty(host);
		Objects.requireNonNull(aiServers);
		if(dbId < 1 || port < 1 || groupId < 1 || aiContextSize < 1)
			throw new IllegalArgumentException();
		//try {
			this.host = host;
			this.port = port;
			this.groupId = groupId;
            this.aiContextSize = aiContextSize;
			this.description = ""; //$NON-NLS-1$
		//} catch(Exception ex) {
		//	Helper.logError(ex, String.format(Messages.getString("ChatServer.SERVER_CREATION_ERROR"), host, port), true); //$NON-NLS-1$
		//}
	}

	/**
	 * Start the server
	 */
	public void startServer() {
		startServer(this.host, this.port);
	}

	/**
	 * Start the server.
	 * @param host Host name
	 * @param port Port number
	 */
	public void startServer(String host, int port) {
		this.host = host;
		this.port = port;
		try {
	        HttpHandler prefPathHandler = new PathHandler()
                    .addPrefixPath("/login", exchange -> { //$NON-NLS-1$
                        processWebsiteLogin(exchange);
                    })
                    .addPrefixPath("/logout", exchange -> { //$NON-NLS-1$
                        processWebsiteLogout(exchange);
                    })
                    .addPrefixPath("/letschat", exchange -> { //$NON-NLS-1$
                        processUserLetsChat(exchange);
                    })
                    .addPrefixPath("/message", exchange -> { //$NON-NLS-1$
                        processUserMessage(exchange);
                    });
	        HttpHandler fallbackHandler = new PathHandler() {
	        	@Override
	        	public void handleRequest(HttpServerExchange exchange) throws Exception {
		        	processError(exchange, Messages.getString("ChatServer.REZ_404"), 404); //$NON-NLS-1$
	        	}
	        };

	        HttpHandler rootHandler = new PathHandler()
	                .addPrefixPath("/", exchange -> { //$NON-NLS-1$
		                prefPathHandler.handleRequest(exchange);
		                if(!exchange.isResponseComplete())
		                	fallbackHandler.handleRequest(exchange);
		            });
	        this.server = Undertow.builder()
	                .addHttpListener(port, host)
	                .setHandler(rootHandler)
	                .build();
	        this.server.start();
	        this.started = true;
	        fireStatsChanged(this);
		} catch(Exception ex) {
			this.host = null;
			this.port = 0;
			Helper.logError(ex, String.format(Messages.getString("ChatServer.SERVER_START_ERROR"), host, port), true); //$NON-NLS-1$
		}
	}

	/**
	 * Start the secure server
	 * @param pfxPswd Password of the Key Store file.
	 */
	public void startSecureServer(char[] pfxPswd) {
		startSecureServer(this.host, this.port, pfxPswd);
	}

	/**
	 * Start the secure server.
	 * @param host Host name
	 * @param port Port number
	 * @param pfxPswd Password of the Key Store file.
	 */
	public void startSecureServer(String host, int port, char[] pfxPswd) {
		this.host = host;
		this.port = port;
		try {
	        KeyStore keyStore = KeyStore.getInstance("PKCS12"); //$NON-NLS-1$
	        try (FileInputStream fis = new FileInputStream(CertGenDialog.KEYSTORE_PFX)) {
	            keyStore.load(fis, pfxPswd);
	        }

	        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	        keyManagerFactory.init(keyStore, pfxPswd);

	        SSLContext sslContext = SSLContext.getInstance("TLS"); //$NON-NLS-1$
	        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

	        executor = Executors.newFixedThreadPool(MainWindow.getSettings().getAiServersTasks());
	        HttpHandler prefPathHandler = new PathHandler()
                    .addPrefixPath("/login", exchange -> { //$NON-NLS-1$
                    	exchange.dispatch(() -> {
                    		executor.submit(() -> {
                    			processWebsiteLogin(exchange);
                    		});
                    	});
                    })
                    .addPrefixPath("/ilogin", exchange -> { // Localhost internal client login //$NON-NLS-1$
                    	exchange.dispatch(() -> {
                    		executor.submit(() -> {
                    			processWebsiteInternalLogin(exchange);
                    		});
                    	});
                    })
                    .addPrefixPath("/logout", exchange -> { //$NON-NLS-1$
                    	exchange.dispatch(() -> {
                    		executor.submit(() -> {
                    			processWebsiteLogout(exchange);
                    		});
                    	});
                    })
                    .addPrefixPath("/letschat", exchange -> { //$NON-NLS-1$
                    	exchange.dispatch(() -> {
                    		executor.submit(() -> {
                    			processUserLetsChat(exchange);
                    		});
                    	});
                    })
                    .addPrefixPath("/message", exchange -> { //$NON-NLS-1$
                    	exchange.dispatch(() -> {
                    		executor.submit(() -> {
                    			processUserMessage(exchange);
                    		});
                    	});
                    });
	        HttpHandler fallbackHandler = exchange -> {
        		if(!exchange.isResponseComplete())
        			processError(exchange, Messages.getString("ChatServer.REZ_404"), 404); //$NON-NLS-1$
	        };

	        HttpHandler rootHandler = exchange -> {
                prefPathHandler.handleRequest(exchange);
                exchange.addExchangeCompleteListener((exchng, next) -> {
	                if(!exchng.isResponseComplete()) {
	                	try {
							fallbackHandler.handleRequest(exchng);
						} catch (Exception ex) {
							Helper.logError(ex, Messages.getString("ChatServer.FALLBACK_HANDLER_FAILURE"), true); //$NON-NLS-1$
						}
	                }
	                next.proceed();
                });
            };

	        // Configure Undertow server
            int timeout = MainWindow.getSettings().getAiLogOnLongResponse()*60000;
	        this.server = Undertow.builder()
	                .addHttpsListener(port, host, sslContext)
	                .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
	                .setWorkerOption(Options.WORKER_IO_THREADS, MainWindow.getSettings().getAiServersTasks())
	                .setWorkerOption(Options.TCP_NODELAY, true)
	                .setHandler(rootHandler)
	                .setServerOption(UndertowOptions.IDLE_TIMEOUT, timeout) // Settings defined
	                .setServerOption(UndertowOptions.NO_REQUEST_TIMEOUT, timeout) // Settings defined
	                .setServerOption(UndertowOptions.REQUEST_PARSE_TIMEOUT, timeout*2) // Settings defined
	                .build();
	        this.server.start();
	        this.started = true;
	        fireStatsChanged(this);
	        Helper.logInfo(String.format(Messages.getString("ChatServer.SERVER_STARTED_SUCCESS"), getName(), host, port)); //$NON-NLS-1$
		} catch(Exception ex) {
			this.host = null;
			this.port = 0;
			Helper.logError(ex, String.format(Messages.getString("ChatServer.SERVER_START_ERROR_IPP"), host, port), true); //$NON-NLS-1$
		}
   }

	/**
	 * Stop the server
	 */
	public void stopServer() {
		if(this.server != null)
			this.server.stop();
		terminated = true;
		storeEndedChats(true);
		Helper.logInfo(String.format(Messages.getString("ChatServer.SERVER_NOW_SHUTDOWN"), host, port)); //$NON-NLS-1$
	}

	/**
	 * Validate authentication keys
	 * @param hakey Authentication key from header
	 * @param pakey Authentication key from post
	 * @return True if keys are valid
	 */
	private boolean validateAuthKeys(String hakey, String pakey) {
    	char[] key = hakey.toCharArray();
    	if(key.length != internalHeaderAuthKey.length)
    		return false;
    	for(int i = 0; i < key.length; i++)
        	if(key[i] != internalHeaderAuthKey[i])
        		return false;
    	key = pakey.toCharArray();
    	if(key.length != internalPostAuthKey.length)
    		return false;
    	for(int i = 0; i < key.length; i++)
        	if(key[i] != internalPostAuthKey[i])
        		return false;
    	return true;
	}

	/**
	 * Called when an internal authentication request is made in order to let an InternalClientFrame instance
	 * to authenticate without normal credentials.
	 * @return An array of strings containing the header[auth_key=key] and post[auth_key=key]
	 */
	public String[] internalAuthRequest() {
		if(internalHeaderAuthKey  != null) {
			if(internalHeaderAuthKey[0] != (char)0) {
				Helper.logWarning(Messages.getString("ChatServer.INTERNAL_HAKEY_NOT_EMPTY")); //$NON-NLS-1$
                return null;
			}
		}
		if(internalPostAuthKey != null) {
    		if(internalPostAuthKey[0] != (char)0) {
    			Helper.logWarning(Messages.getString("ChatServer.INTERNEL_PAKEY_NOT_EMPTY")); //$NON-NLS-1$
                return null;
    		}
		}
		String[] ret = new String[] {
				Helper.generateSecureKey(128),
                Helper.generateSecureKey(128)
		};
		internalHeaderAuthKey = ret[0].toCharArray();
        internalPostAuthKey = ret[1].toCharArray();
        Helper.logWarning(Messages.getString("ChatServer.INTERNAL_AUTH_CLIENT_REQUEST")); //$NON-NLS-1$
        return ret;
	}

    /**
     * Internal client login from localhost only for testing purposes.
     * Caller should provide header[auth_key=key] and post[auth_key=key]
     * @param exchange HttpServerExchange object from Undertow.
     */
	private void processWebsiteInternalLogin(HttpServerExchange exchange) {
		if(!this.enabled || this.terminated) {
			Arrays.fill(internalHeaderAuthKey, (char)0);
            Arrays.fill(internalPostAuthKey, (char)0);
			processError(exchange, Messages.getString("ChatServer.SERVER_UNAVAILABLE_TEMP"), 503); //$NON-NLS-1$
			return;
		}
		if("POST".equalsIgnoreCase(exchange.getRequestMethod().toString())) { //$NON-NLS-1$
			String ct = exchange.getRequestHeaders().getFirst(Headers.CONTENT_TYPE);
			if(ct.equalsIgnoreCase("application/x-www-form-urlencoded")) { //$NON-NLS-1$
				exchange.getRequestReceiver().receiveFullString((xchng, body) -> {
					InetAddress ia = xchng.getSourceAddress().getAddress();
					String ip = Helper.ipv6Compress(ia.getHostAddress());
					String domain = Helper.ipv6Compress(ia.getHostName());
					Helper.logInfo(Messages.getString("ChatServer.INTERNAL_LOGIN_ATTEMPT_FROM") + domain + " (" + ip + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					String hAuthKey = xchng.getRequestHeaders().getFirst("auth_key"); // Check for auth_key=key header //$NON-NLS-1$
					Map<String, String> params = Helper.parsePostData(body);
					String pAuthKey = params.get("auth_key"); //$NON-NLS-1$
					if(hAuthKey != null && pAuthKey != null) {
						try {
							if(validateAuthKeys(hAuthKey, pAuthKey)) {
								WebsiteSession ws = new WebsiteSession(null);
								this.webSessions.put(ws.getAccessToken(), ws);
								Map<String, Object> rslt = new HashMap<>();
								rslt.put("TOKEN", ws.getAccessToken()); //$NON-NLS-1$
								rslt.put("STATUS", "OK"); //$NON-NLS-1$ //$NON-NLS-2$
								exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json"); //$NON-NLS-1$
								exchange.getResponseSender().send(new Gson().toJson(rslt));
								exchange.endExchange();
								fireStatsChanged(this);
								Helper.logInfo(String.format(Messages.getString("ChatServer.INTERNAL_CLIENT_LOGGED_FROM"), domain, ip), false); //$NON-NLS-1$
							} else processError(exchange, Messages.getString("ChatServer.AUTH_ERROR"), 401); //$NON-NLS-1$
						} catch(Exception ex) {
							Helper.logWarning(ex, String.format("Internal client login failed from %s", domain), false); //$NON-NLS-1$
							processError(exchange, Messages.getString("ChatServer.INTERNAL_ERROR"), 500); //$NON-NLS-1$
						}
					} else processError(exchange, Messages.getString("ChatServer.AUTH_ERROR"), 400); //$NON-NLS-1$
					Arrays.fill(internalHeaderAuthKey, (char)0);
                    Arrays.fill(internalPostAuthKey, (char)0);
				});
			} else processError(exchange, Messages.getString("ChatServer.BAD_QUERY"), 400); //$NON-NLS-1$
		} else processError(exchange, Messages.getString("ChatServer.BAD_QUERY"), 400); //$NON-NLS-1$
	}

	/**
	 * For a website to access this ChatServer, should provide [key1=value1, key2=value2].
	 * We should check the ip/domain when key1 & key2 are checked then create a new website
	 * session.
	 * @param exchange HttpServerExchange object from Undertow.
	 */
	private void processWebsiteLogin(HttpServerExchange exchange) {
		if(!this.enabled || this.terminated) {
			processError(exchange, Messages.getString("ChatServer.SERVER_UNAVAILABLE_TEMP"), 503); //$NON-NLS-1$
			return;
		}
		if("POST".equalsIgnoreCase(exchange.getRequestMethod().toString())) { //$NON-NLS-1$
			String ct = exchange.getRequestHeaders().getFirst(Headers.CONTENT_TYPE);
			if(ct.equalsIgnoreCase("application/x-www-form-urlencoded")) { //$NON-NLS-1$
				exchange.getRequestReceiver().receiveFullString((xchng, body) -> {
					InetAddress ia = xchng.getSourceAddress().getAddress();
					String ip = Helper.ipv6Compress(ia.getHostAddress());
					String domain = Helper.ipv6Compress(ia.getHostName());
					Helper.logInfo(Messages.getString("ChatServer.LOG_LOGIN_ATTEMPT_FROM") + domain + " (" + ip + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					Map<String, String> params = Helper.parsePostData(body);
					String key1 = params.get("key1"); //$NON-NLS-1$
					String key2 = params.get("key2"); //$NON-NLS-1$
					if(key1 != null && key2 != null) {
						try {
							WebsiteRecord wr = getWebsiteRecord(ip, domain, key1, key2);
							if((wr != null) && wr.isEnabled() && !wr.isRemoved() && (wr.getAiGroupId() == this.getGroupId())) {
								String k1h = Helper.hashKey(key1, wr.getSalt());
								String k2h = Helper.hashKey(key2, wr.getSalt());
								if(wr.getKey1Hash().equals(k1h) && wr.getKey2Hash().equals(k2h)) {
									WebsiteSession ws = new WebsiteSession(wr);
									this.webSessions.put(ws.getAccessToken(), ws);
									Map<String, Object> rslt = new HashMap<>();
									rslt.put("TOKEN", ws.getAccessToken()); //$NON-NLS-1$
									rslt.put("STATUS", "OK"); //$NON-NLS-1$ //$NON-NLS-2$
									exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json"); //$NON-NLS-1$
									exchange.getResponseSender().send(new Gson().toJson(rslt));
									exchange.endExchange();
									fireStatsChanged(this);
									Helper.logInfo(String.format(Messages.getString("ChatServer.WEBSITE_LOGGED_IN"), domain, ip), false); //$NON-NLS-1$
								} else processError(exchange, Messages.getString("ChatServer.AUTH_ERROR"), 401); //$NON-NLS-1$
							} else processError(exchange, Messages.getString("ChatServer.AUTH_ERROR"), 401); //$NON-NLS-1$
						} catch(Exception ex) {
							Helper.logWarning(ex, String.format(Messages.getString("ChatServer.WEBSITE_SESSION_ERROR"), domain), false); //$NON-NLS-1$
							processError(exchange, Messages.getString("ChatServer.INTERNAL_ERROR"), 500); //$NON-NLS-1$
						}
					} else processError(exchange, Messages.getString("ChatServer.AUTH_ERROR"), 400); //$NON-NLS-1$
				});
			} else processError(exchange, Messages.getString("ChatServer.BAD_QUERY"), 400); //$NON-NLS-1$
		} else processError(exchange, Messages.getString("ChatServer.BAD_QUERY"), 400); //$NON-NLS-1$
	}

	/**
	 * For a website to close its session. All further requests should be denied with 403 Forbidden or 401 Unauthorized.
	 * @param exchange HttpServerExchange object from Undertow
	 */
	private void processWebsiteLogout(HttpServerExchange exchange) {
		if(!this.enabled || this.terminated) {
			processError(exchange, Messages.getString("ChatServer.SERVICE_UNAVAILABLE_TEMP"), 503); //$NON-NLS-1$
			return;
		}
		if("POST".equalsIgnoreCase(exchange.getRequestMethod().toString())) { //$NON-NLS-1$
			String ct = exchange.getRequestHeaders().getFirst(Headers.CONTENT_TYPE);
			if(ct.equalsIgnoreCase("application/x-www-form-urlencoded")) { //$NON-NLS-1$
				exchange.getRequestReceiver().receiveFullString((xchng, body) -> {
					InetAddress ia = xchng.getSourceAddress().getAddress();
					String ip = Helper.ipv6Compress(ia.getHostAddress());
					String domain = Helper.ipv6Compress(ia.getHostName());
					Map<String, String> params = Helper.parsePostData(body);
					String token = params.get("token"); //$NON-NLS-1$
					WebsiteSession ws = this.webSessions.get(token);
					if(ws != null && ws.getAccessToken().equals(token)) {
						this.webSessions.remove(token);
						Map<String, Object> rslt = new HashMap<>();
						rslt.put("STATUS", "OK"); //$NON-NLS-1$ //$NON-NLS-2$
						exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json"); //$NON-NLS-1$
						exchange.getResponseSender().send(new Gson().toJson(rslt));
						exchange.endExchange();
						fireStatsChanged(this);
						Helper.logInfo(String.format(Messages.getString("ChatServer.WEBSITE_LOGGED_OUT"), domain, ip), false); //$NON-NLS-1$
					} else processError(exchange, Messages.getString("ChatServer.SESSION_NOT_FOUND"), 403); //$NON-NLS-1$
				});
			} else processError(exchange, Messages.getString("ChatServer.DISCONNECTION_ERROR"), 400); //$NON-NLS-1$
		} else processError(exchange, Messages.getString("ChatServer.DISCONNECTION_ERROR"), 400); //$NON-NLS-1$
	}

	/**
	 * Starts a new user chat session.
	 * @param exchange HttpServerExchange object from Undertow
	 */
	public void processUserLetsChat(HttpServerExchange exchange) {
		if(!this.enabled || this.terminated) {
			processError(exchange, Messages.getString("ChatServer.SERVICE_UNAVAILABLE_TEMP"), 503); //$NON-NLS-1$
			return;
		}
		if("POST".equalsIgnoreCase(exchange.getRequestMethod().toString())) { //$NON-NLS-1$
			String ct = exchange.getRequestHeaders().getFirst(Headers.CONTENT_TYPE);
			if(ct.equalsIgnoreCase("application/x-www-form-urlencoded")) { //$NON-NLS-1$
				exchange.getRequestReceiver().receiveFullString((xchng, body) -> {
					InetAddress ia = xchng.getSourceAddress().getAddress();
					String ip = Helper.ipv6Compress(ia.getHostAddress());
					String domain = Helper.ipv6Compress(ia.getHostName());
					Map<String, String> params = Helper.parsePostData(body);
					String token = params.get("token"); //$NON-NLS-1$
					String usrId = params.get("usr_id"); //$NON-NLS-1$
					if(Helper.isNullOrEmpty(token) || Helper.isNullOrEmpty(usrId)) {
						processError(exchange, Messages.getString("ChatServer.MISSING_PARAMS"), 400); //$NON-NLS-1$
						return;
					}
					WebsiteSession ws = this.webSessions.get(token);
					if(ws != null && ws.getAccessToken().equals(token)) {
						UserSession us = new UserSession(chatClient.riaChatSession(chatClient.getMainLocale()), usrId);
						this.userSessions.put(usrId, us);
						ChatSession ses = us.getChatSession();
						ses.setAIGroupId(this.getGroupId());
						SwingUtilities.invokeLater(() -> fireActivityStateChanged(ChatSessionState.WEBSITE));
						Map<String, Object> rslt = new HashMap<>();
						rslt.put("CHATBOT_MESSAGE", chatClient.letsChat(ses)); //$NON-NLS-1$
						rslt.put("CHAT_ENDED", ses.isEnded()); //$NON-NLS-1$
						rslt.put("CHAT_STATE", ses.getState().toString()); //$NON-NLS-1$
						rslt.put("LOCALE", ses.getCurLocale()); //$NON-NLS-1$
						rslt.put("STATUS", "OK"); //$NON-NLS-1$ //$NON-NLS-2$
						exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json"); //$NON-NLS-1$
						exchange.getResponseSender().send(new Gson().toJson(rslt));
						exchange.endExchange();
						SwingUtilities.invokeLater(() -> fireActivityStateChanged(ses.getState()));
						Helper.logInfo(String.format(Messages.getString("ChatServer.NEW_CHAT_STARTED"), ses.getBotName(), usrId), false); //$NON-NLS-1$
						fireStatsChanged(this);
					} else processError(exchange, Messages.getString("ChatServer.SESSION_NOT_FOUND"), 401); //$NON-NLS-1$
				});
			} else processError(exchange, Messages.getString("ChatServer.MISSING_PARAMS"), 400); //$NON-NLS-1$
		} else processError(exchange, Messages.getString("ChatServer.INVALID_PARAMS"), 400); //$NON-NLS-1$
	}

	/**
	 * Processes a user message.
	 * @param exchange HttpServerExchange object from Undertow
	 */
	public void processUserMessage(HttpServerExchange exchange) {
		if(!this.enabled || this.terminated) {
			processError(exchange, Messages.getString("ChatServer.SERVICE_UNAVAILABLE_TEMP"), 503); //$NON-NLS-1$
			return;
		}
		if("POST".equalsIgnoreCase(exchange.getRequestMethod().toString())) { //$NON-NLS-1$
			String ct = exchange.getRequestHeaders().getFirst(Headers.CONTENT_TYPE);
			if(ct.equalsIgnoreCase("application/x-www-form-urlencoded")) { //$NON-NLS-1$
				exchange.getRequestReceiver().receiveFullString((xchng, body) -> {
					InetAddress ia = xchng.getSourceAddress().getAddress();
					String ip = Helper.ipv6Compress(ia.getHostAddress());
					String domain = Helper.ipv6Compress(ia.getHostName());
					Map<String, String> params = Helper.parsePostData(body);
					String token = params.get("token"); //$NON-NLS-1$
					String usrId = params.get("usr_id"); //$NON-NLS-1$
					String usrMsg = params.get("message").trim(); //$NON-NLS-1$
					if(Helper.isNullOrEmpty(usrMsg) || Helper.isNullOrEmpty(token) || Helper.isNullOrEmpty(usrId)) {
						processError(exchange, Messages.getString("ChatServer.MISSING_PARAMS"), 400); //$NON-NLS-1$
						return;
					}
					WebsiteSession ws = this.webSessions.get(token);
					if(ws != null && ws.getAccessToken().equals(token)) {
						UserSession us = this.userSessions.get(usrId);
						if(us != null) {
							ChatSession ses = us.getChatSession();
							ChatSessionState curState = ses.getState();
							SwingUtilities.invokeLater(() -> fireActivityStateChanged(ChatSessionState.WEBSITE));
							Map<String, Object> rslt = new HashMap<>();
							if((ses.getAgentId() == -1) &&
									(ses.getState() == ChatSessionState.AGENT)) { // Needs to switch to agent cchat
								ses.switchToAgentChatSession(-1, null, -1, usrId); // ChatAgent.letsChat will set the missing params
								rslt.put("CHATBOT_MESSAGE", chatClient.letsChat(ses)); //$NON-NLS-1$
							} else {
								/*
								 * Start execute bot script
								 */
								// Bot script onUserMessage is called for every user message during every chatbot mode.

								Integer retScript = null;
								String botScript = ses.getBotScript();
								if(botScript != null && !botScript.isBlank()) {
									retScript = executeScript(botScript, ses, true, usrMsg);
									if(retScript != null) {
										usrMsg = ses.getVar("message"); //$NON-NLS-1$
										ses.removeVar("message"); //$NON-NLS-1$
										if(retScript > 0) { // Go back to chatbot mode then execute the node with (ret) id.
											curState = ChatSessionState.CHATBOT; // Important
											ses.setState(curState);
											ses.setCurrentNode(chatClient.getChatBotClient().getNodeById(ses.getCurLocale(), retScript));
										} else if(retScript ==  0) { // Restart the current node.
											Helper.logWarning(Messages.getString("ChatServer.EX_NODE_RESTART")); //$NON-NLS-1$
										} else if(retScript == -1) { // End the chat.
											ses.setEnded(true);
										} else if(retScript == -2) { // Restart the chat bot.
											ses.setState(ChatSessionState.CHATBOT);
											ses.setCurrentNode(chatClient.getChatBotClient().getNodeById(ses.getCurLocale(), chatClient.getChatBotClient().getEntryId(ses.getCurLocale())));
										} else if(retScript == -3) { // Switch to AI.
											ses.setState(ChatSessionState.AIMODEL);
										} else if(retScript == -4) { // Switch to an agent.
											curState = ChatSessionState.AGENT; // Important
											ses.setState(curState);
										}
									}
								}
								String[] botMsg = chatClient.userMessage(ses, usrMsg);
								// Bot script onAIMessage is called only during AI mode.
								if(botScript != null && !botScript.isBlank() && ChatSessionState.AIMODEL.equals(ses.getState())) {
									retScript = executeScript(botScript, ses, false, String.join(System.lineSeparator(), botMsg));
									if(retScript != null) {
										botMsg = ses.getVar("message").split(System.lineSeparator()); //$NON-NLS-1$
										ses.removeVar("message"); //$NON-NLS-1$
										if(retScript > 0) { // Go back to chatbot mode then execute the node with (ret) id.
											curState = ChatSessionState.CHATBOT; // Important
											ses.setState(curState);
											ses.setCurrentNode(chatClient.getChatBotClient().getNodeById(ses.getCurLocale(), retScript));
											System.out.println(Messages.getString("ChatServer.EX_CHATBOT_SWITCH") + retScript); //$NON-NLS-1$
										} else if(retScript ==  0) { // Restart the current node.
											Helper.logWarning(Messages.getString("ChatServer.EX_NODE_RESTART")); //$NON-NLS-1$
										} else if(retScript == -1) { // End the chat.
											ses.setEnded(true);
										} else if(retScript == -2) { // Restart the chat bot.
											ses.setState(ChatSessionState.CHATBOT);
											ses.setCurrentNode(chatClient.getChatBotClient().getNodeById(ses.getCurLocale(), chatClient.getChatBotClient().getEntryId(ses.getCurLocale())));
										} else if(retScript == -3) { // Switch to AI.
											ses.setState(ChatSessionState.AIMODEL);
										} else if(retScript == -4) { // Switch to an agent.
											curState = ChatSessionState.AGENT; // Important
											ses.setState(curState);
										}
									}
								}
								rslt.put("CHATBOT_MESSAGE", botMsg); //$NON-NLS-1$
								/*
								 * End execute bot script
								 */
							}
							rslt.put("CHAT_ENDED", ses.isEnded()); //$NON-NLS-1$
							rslt.put("CHAT_STATE", ses.getState().toString()); //$NON-NLS-1$
							rslt.put("LOCALE", ses.getCurLocale()); //$NON-NLS-1$
							rslt.put("STATUS", "OK"); //$NON-NLS-1$ //$NON-NLS-2$
							if(curState != ses.getState()) {
								if(!ses.isEnded())
									rslt.put("CHATBOT_WAITING", true); //$NON-NLS-1$
							} else {
								if(ses.getState() == ChatSessionState.AIMODEL) {
									String agentReq = ses.getAiModelParam("app_agent_req_prefix"); //$NON-NLS-1$
									if(agentReq != null && !agentReq.isBlank()) {
										String[] aiMsgs = (String[])rslt.get("CHATBOT_MESSAGE"); //$NON-NLS-1$
										for(String aiMsg : aiMsgs) {
											if(!aiMsg.isEmpty() && aiMsg.startsWith(agentReq)) {
												ses.setState(ChatSessionState.AGENT);
												rslt.put("CHATBOT_WAITING", true); //$NON-NLS-1$
												Helper.logInfo(String.format(Messages.getString("ChatServer.AIMODEL_SWITCH_TO_AGENT"), usrId)); //$NON-NLS-1$
											}
										}
									}
								}
							}
							exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json"); //$NON-NLS-1$
							exchange.getResponseSender().send(new Gson().toJson(rslt));
							exchange.endExchange();
							fireActivityStateChanged(ses.getState()); // TODO : Check out this
							fireStatsChanged(this); // TODO : Check out this
							if(ses.isEnded())
								Helper.logInfo(String.format(Messages.getString("ChatServer.LOG_SESSION_ENDED"), ses.getBotName(), ses.getUserId()));
						} else processError(exchange, Messages.getString("ChatServer.USER_SESSION_NOT_FOUND"), 401); //$NON-NLS-1$
					} else processError(exchange, Messages.getString("ChatServer.WEBSITE_SESSION_NOT_FOUND"), 401); //$NON-NLS-1$
				});
			} else processError(exchange, Messages.getString("ChatServer.MISSING_PARAMS"), 400); //$NON-NLS-1$
		} else processError(exchange, Messages.getString("ChatServer.INVALID_PARAMS"), 400); //$NON-NLS-1$
	}

	/**
	 * 400 Bad request ex: missing/bad parameters
	 * 401 Unathorized : wrong credentials/tokens or session expired or needs to be re/connected
	 * 403 Forbidden : Not authorized to access this resource
	 * 404 Not found : resource not found
	 * 503 Service unavailable : When !enabled for example.
	 *
	 * @param exchange HttpServerExchange object from Undertow
	 * @param errorMsg Error message
	 * @param errorCode Error code
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
		exchange.endExchange();
	}

	/**
	 * Load chat server from database giving a ResultSet object.
	 * @param rs ResultSet object.
	 * @param aiServers List of all AI servers
	 * @return ChatServer object or null on error.
	 */
	public static ChatServer fromDatabase(ResultSet rs, List<Object[]> aiServers) {
		Objects.requireNonNull(rs);
		try {
			int dbId = rs.getInt("id"); //$NON-NLS-1$
			ChatServer cs = new ChatServer(dbId);
			cs.host = rs.getString("server_ip"); //$NON-NLS-1$
			cs.port = rs.getInt("server_port"); //$NON-NLS-1$
			cs.groupId = rs.getInt("ai_group_id"); //$NON-NLS-1$
			cs.description = rs.getString("description"); //$NON-NLS-1$
			cs.enabled = rs.getInt("enabled") == 0 ? false : true; //$NON-NLS-1$
			cs.aiContextSize = rs.getInt("ai_context_size"); //$NON-NLS-1$
			ChatClient cc = cs.getChatClient();
			cc.loadChatBotRIA(rs.getString("ria_file")); //$NON-NLS-1$
			cs.loadChatModelClients(aiServers);
			return cs;
		} catch(Exception ex) {
			Helper.logError(ex, Messages.getString("ChatServer.SERVER_LOAD_ERROR"), true); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Load chat model clients based on AI model params.
	 * @param aiServers List of all AI servers records
	 * @param dbId Chat server database id
	 * @param cc Chat client object to add chat model clients into.
	 */
	public void loadChatModelClients(List<Object[]> aiServers) {

		ChatSession ses = chatClient.riaChatSession(chatClient.getMainLocale());
		String modelPrefix = null;
		for(Entry<String, String> entry : ses.getAiModelParamsEntrySet()) {
			modelPrefix = entry.getKey().split("_")[0] + "_"; //$NON-NLS-1$ //$NON-NLS-2$
			if("ollama_;gpt4all_;chatgpt_;groq_;deepseek_".contains(modelPrefix)) { //$NON-NLS-1$
				break;
			} else modelPrefix = null;
		}
		if(modelPrefix != null) {
			IChatModelClient cmc;
			for(Object[] aiServer : aiServers) {
				if(((int)aiServer[1] == dbId) && ((int)aiServer[4]==0)) { // Enabled and related to this chat server
					cmc = null;
					if(modelPrefix.equals(ChatGptModelClient.AIQ_PREFIX)) {
						cmc = new ChatGptModelClient((int)aiServer[0], (String)aiServer[2], (Boolean)((Integer)aiServer[3]==1?true:false), aiContextSize);
					} else if(modelPrefix.equals(GroqModelClient.AIQ_PREFIX)) {
						cmc = new GroqModelClient((int)aiServer[0], (String)aiServer[2], (Boolean)((Integer)aiServer[3]==1?true:false), aiContextSize);
					} else if(modelPrefix.equals(DeepSeekModelClient.AIQ_PREFIX)) {
						cmc = new DeepSeekModelClient((int)aiServer[0], (String)aiServer[2], (Boolean)((Integer)aiServer[3]==1?true:false), aiContextSize);
					} else if(modelPrefix.equals(Chat4AllModelClient.AIQ_PREFIX)) {
						cmc = new Chat4AllModelClient((int)aiServer[0], (String)aiServer[2], (Boolean)((Integer)aiServer[3]==1?true:false), aiContextSize);
					} else if(modelPrefix.equals(OllamaModelClient.AIQ_PREFIX)) {
						cmc = new OllamaModelClient((int)aiServer[0], (String)aiServer[2], (Boolean)((Integer)aiServer[3]==1?true:false), aiContextSize);
					}
					if(cmc != null) {
						chatClient.addChatModelClient(cmc);
					} else Helper.logWarning(Messages.getString("ChatServer.PREFIX_AI_MODEL_NOTFOUND") + modelPrefix, false); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Change the state of this instance.
	 * @param enabled New enabled state.
	 */
	public void setEnabled(boolean enabled) {
		if(!started && enabled) {
			Helper.logError(Messages.getString("ChatServer.LOG_ERR_SERVER_NOT_STARTED"), false); //$NON-NLS-1$
			enabled = false;
		}
		this.enabled = enabled;
		if(this.enabled) {
			try {
				Helper.logInfo(String.format(Messages.getString("ChatServer.SERVER_RELOADING_START"), this.getName())); //$NON-NLS-1$
				ChatBotClient cbc = this.chatClient.getChatBotClient();
				cbc.reloadRIAs();
				for(UserSession us : this.userSessions.values())
					us.getChatSession().updateRootSession(cbc.riaChatSession(cbc.getMainLocale()));
				Helper.logInfo(String.format(Messages.getString("ChatServer.SERVER_RELOADING_FINISHED"), this.getName())); //$NON-NLS-1$
			} catch(Exception ex) {
				enabled = false;
				Helper.logError(ex, String.format(Messages.getString("ChatServer.SERVER_ACTIVATION_ERROR"), this.getName()), true); //$NON-NLS-1$
			}
		} else {
			storeEndedChats(true);
			this.userSessions.clear();
			this.webSessions.clear();
			this.websiteRecords.clear();
		}
	}

    /**
    * Executes the RIA's script on user/AI messages. Returns a message defined by the script
    * Session variables defined [Success/Error action=variable:user_value & Value=var_name]
    * during static chat flows are defined in the global scope.
    * Inside the script, declare 'message' variable and affect the content you want to
    * send back to remote user.
    * Possible return values:
    * >0 : Next node id.
    *  0 : Repeat current node action.
    * -1 : End the chat.
    * -2 : Restart the chat bot.
    * -3 : Switch to AI.
    * -4 : Switch to an agent.
    * Calls onUserMessage(var msg) or onAIMessage(var msg) depending on the message source.
    * @param script Script to execute
    * @param ses Chat session object.
    * @param isUser True if the message is from the user, False if from the AI.
    * @param response Original user/AI response.
    * @return The node id to move to if > 0. 0:repeat. -1:end. -2:restart. -3:switch_to_ai. -4:switch_to_agent. null:Error.
    */
   private Integer executeScript(String script, ChatSession ses, boolean isUser, String response) {
       try (V8Runtime v8Runtime = V8Host.getV8Instance().createV8Runtime()) {
           v8Runtime.setConverter(new JavetProxyConverter());
           V8ValueGlobalObject global = v8Runtime.getGlobalObject();
           global.set("ScriptEx", ScriptEx.class); //$NON-NLS-1$
           global.set("response", response); //$NON-NLS-1$
           global.set("message", ""); //$NON-NLS-1$ //$NON-NLS-2$
           ses.setVar("message", ""); //$NON-NLS-1$ //$NON-NLS-2$
           for(Map.Entry<String, String> entry : ses.getVarsSet())
               global.set(entry.getKey(), entry.getValue());
           if(isUser) {
        	   script += "function main() { return onUserMessage(response); }" + System.lineSeparator() + "main();"; //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-2$
           } else script += "function main() { return onAIMessage(response); }" + System.lineSeparator() + "main();"; //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-2$
           String varName;
           Object obj;
           Integer ret = v8Runtime.getExecutor(script).executeInteger();
           for(Map.Entry<String, String> entry : ses.getVarsSet()) {
               varName = entry.getKey();
               if(global.hasOwnProperty(varName)) {
                   obj = global.get(entry.getKey());
                   entry.setValue(obj.toString());
               }
           }
           global.delete("message"); //$NON-NLS-1$
           global.delete("response"); //$NON-NLS-1$
           global.delete("ScriptEx"); //$NON-NLS-1$
           global.close();
           v8Runtime.lowMemoryNotification();
           v8Runtime.close();
           return ret;
       } catch(JavetCompilationException ex) {
    	   ses.setEnded(true);
		   Helper.logError(ex, String.format(Messages.getString("ChatBotClient.SCRIPT_COMPILATION_ERROR"), ses.getCurrentNode().getId(), ex.getScriptingError().toString()), true); //$NON-NLS-1$
       } catch (JavetException ex) {
    	   ses.setEnded(true);
           Helper.logWarning(ex, String.format(Messages.getString("ChatBotClient.SCRIPT_RUNTIME_ERROR"), ses.getCurrentNode().getId()), false); //$NON-NLS-1$
       }
       return null;
   }

	/**
	 * @return ChatClient object.
	 */
	public ChatClient getChatClient() { return this.chatClient; }

	/**
	 * @return Server port.
	 */
	public int getPort() { return this.port; }

	/**
	 * @return Server host.
	 */
	public String getHost() { return this.host; }

	/**
	 * @return Server name.
	 */
	public String getName() { return this.chatClient.getChatBotClient().getChatBotName(); }

	/**
	 * @return This server id in database.
	 */
	public int getDbId() { return this.dbId; }

	/**
	 * @return Group id.
	 */
	public int getGroupId() { return this.groupId; }

	/**
	 * Set group id.
	 * @param groupId New group id.
	 */
	public void setGroupId(int groupId) { if(groupId <= 0) throw new IllegalArgumentException(); this.groupId = groupId; }

    /**
     * @return AI context size.
     */
	public int getAiContextSize() { return this.aiContextSize; }

    /**
     * Set AI context size.
     * @param value New AI context size.
     */
    public void setAiContextSize(int value) { if(aiContextSize <= 0) throw new IllegalArgumentException(); this.aiContextSize = value; }

	/**
	 * @return Server description.
	 */
	public String getDescription() { return this.description; }

	/**
	 * Set server description.
	 * @param description New description.
	 */
	public void setDescription(String description) { Objects.requireNonNull(description); this.description = description; }

	/**
	 * @return Server enabled state.
	 */
	public boolean isEnabled() { return this.enabled; }

	/**
	 * @return Server started state.
	 */
	public boolean isStarted() { return this.started; }

	/**
	 * @return Server terminated state.
	 */
	public boolean isTerminated() { return this.terminated; }

	/**
	 * @return Number of active chats.
	 */
	public int getChatsCount() { return this.userSessions.size(); }

	/**
	 * Returns the ids of the connected agents.
	 * @return The ids of the connected agents.
	 */
	public Set<Integer> getConnectedAgents() { return this.chatClient.getConnectedAgents(); }

	/**
	 * Returns the IPs of the connected soft/web clients.
	 * @return The IPs of the connected soft/web clients.
	 */
	public Set<Integer> getConnectedClients() {
		Set<Integer> cids = new HashSet<Integer>();
		this.webSessions.forEach((id, ws) -> {
			WebsiteRecord wr = ws.getWebsite();
			if(wr != null) {
				cids.add(wr.getId());
			}// else Helper.logWarning("WebsiteSession with null WebsiteRecord! " + ws.toString(), false);
		});
		return cids;
	}

	/**
	 * Returns the number of active chat sessions.
	 * @return Number of active chat sessions
	 */
	public int getActiveChatsCount() {
		int count = 0;
		for(UserSession us : this.userSessions.values())
			if(!us.getChatSession().isEnded())
				count++;
		return count;
	}


	/**
	 * Add a listener.
	 * @param listener Listener to add.
	 */
	public void addChatServerListener(ChatServerListener listener)    { synchronized(this.listeners) { this.listeners.add(listener); } }

	/**
	 * Remove a listener.
	 * @param listener Listener to remove.
	 */
	public void removeChatServerListener(ChatServerListener listener) { synchronized(this.listeners) { this.listeners.remove(listener); } }

	/**
	 * Fire activity state changed event.
	 * @param state New activity state.
	 */
	public void fireActivityStateChanged(ChatSessionState state)      {
		synchronized(this.listeners) {
			for(ChatServerListener listener : this.listeners)
				listener.onActivityStateChanged(this, state);
		}
	}

	/**
	 * Fire stats changed event.
	 */
	private void fireStatsChanged(ChatServer chatServer) {
		synchronized(this.listeners) {
			for(ChatServerListener listener : this.listeners)
				listener.onStatsChanged(chatServer);
		}
	}

	@Override
	public String toString() { return String.format(Messages.getString("ChatServer.CHATSERVER_TOSTRING"), this.getName(), this.dbId, this.host, this.port); } //$NON-NLS-1$

	///////////////////////////////////////////////////////////////////////////

	/**
	 * Chat server listener
	 */
	public static interface ChatServerListener {
		/**
		 * Fired when the server activity state is changed.
		 * @param server ChatServer instance.
		 * @param state New activity state.
		 */
		void onActivityStateChanged(ChatServer server, ChatSessionState state);

		/**
		 * Fired when the server stats are changed.
		 * @param server ChatServer instance.
		 */
		void onStatsChanged(ChatServer server);
	}

	/**
	 * Holds user session information
	 */
	private static class UserSession {
		private static int nextId = 0;
		private int sessionId;
		private String userId;
		private ChatSession chatSession;
		private long started;
		private long lastAccess;

		/**
		 * Holds user session information
		 * @param ses Chat session object
		 * @param userId Remote user id
		 */
		public UserSession(ChatSession ses, String userId) {
			Objects.requireNonNull(ses);
			Objects.requireNonNull(userId);
			this.sessionId = ++nextId;
			this.userId = userId;
			this.chatSession = ses;
			this.chatSession.setUserId(userId);
			this.started = System.currentTimeMillis();
		}

		/**
		 * @return Session id.
		 */
		public int getSessionId() { return this.sessionId; }

		/**
		 * Set session id
		 * @param id New session id
		 */
		public void setSessionId(int id) { this.sessionId = id; }

		/**
		 * @return Remote user id
		 */
		public String getUserId() { return this.userId; }

		/**
		 * @return Chat session
		 */
		public ChatSession getChatSession() { return this.chatSession; }

		/**
		 * @return Session start time
		 */
		public long getStartedMillis() { return this.started; }

		/**
		 * @return Last access time
		 */
		public long getLastAccessMillis() { return this.lastAccess; }

		/**
		 * Set last access time
		 * @param lastAccess New last access time
		 */
		public void setLastAccess(long lastAccess) { this.lastAccess = lastAccess; }

		@Override
		public String toString() {
    		return String.format("UserSession{sessionId=%d, userId=%s, started=%s, lastAccess=%s}", this.sessionId, this.userId, Helper.toDate(Instant.ofEpochMilli(this.started)), Helper.toDate(Instant.ofEpochMilli(this.lastAccess))); //$NON-NLS-1$
		}
	}

	///////////////////////////////////////////////////////////////////////////

	/**
	 *
	 */
	private static class  WebsiteSession {
		private static int nextId = 0;
		private int sessionId;
		private WebsiteRecord websiteRecord;
		private long started;
		private String accessToken;
		private long lastAccess;

		/**
		 * Init a WebsiteSession object
		 * @param wr Website record
		 */
		public WebsiteSession(WebsiteRecord wr) {
			//Objects.requireNonNull(wr);
			this.sessionId = ++nextId;
			this.websiteRecord = wr;
			this.started = System.currentTimeMillis();
			try {
				this.accessToken = Helper.hashString(String.format("%d/%d", this.started, this.hashCode())).replace("+", "_").replace("=", "_"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			} catch (Exception ex) {
				Helper.logWarning(ex, String.format(Messages.getString("ChatServer.TOKEN_CREATION_ERROR"), this.websiteRecord), false); //$NON-NLS-1$
				this.accessToken = String.format("%d-%d", this.started, this.hashCode()); //$NON-NLS-1$
			}
		}

		/**
		 * @return Session id
		 */
		public int    getSessionId()                     { return this.sessionId;        }

		/**
		 * @return Session start time
		 */
		public long   getStartedMillis()                 { return this.started;          }

		/**
		 * @return Last access time
		 */
		public long   getLastAccessMillis()              { return this.lastAccess;       }

		/**
		 * Set last access time
		 * @param lastAccess New last access time
		 */
		public void setLastAccessMillis(long lastAccess) { this.lastAccess = lastAccess; }

		/**
		 * @return Website record
		 */
		public WebsiteRecord getWebsite()                { return this.websiteRecord;    }

		/**
		 * @return Website access token
		 */
		public String getAccessToken()                   { return this.accessToken;      }

		@Override
		public String toString() {
    		return String.format("WebsiteSession{sessionId=%d, websiteRecord=%s, started=%s, lastAccess=%s}", this.sessionId, this.websiteRecord, Helper.toDate(Instant.ofEpochMilli(this.started)), Helper.toDate(Instant.ofEpochMilli(this.lastAccess))); //$NON-NLS-1$
		}
	}

	///////////////////////////////////////////////////////////////////////////

	/**
	 * Holds an AI model server information
	 */
	public static class AiServer {
		private int id;
		private String url;
		private boolean enabled;

		/**
		 * Init an AiServer object
		 * @param id Id of the server
		 * @param url Url of the server
		 * @param enabled State of the server
		 */
		public AiServer(int id, String url, boolean enabled) {
			Helper.requiresNotEmpty(url);
			this.id = id;
			this.url = url;
			this.enabled = enabled;
		}

		/**
		 * @return Server id
		 */
		public int getId() { return this.id; }

		/**
		 * @return Server url
		 */
		public String getUrl() { return this.url; }

		/**
		 * @return Server enabled state
		 */
		public boolean isEnabled() { return this.enabled; }
	}

	/**
	 * Holds a website record
	 */
	public static class WebsiteRecord {
		private int id;
		private String shaId;
		private String  domain;
		private String ip;
		private int aiGroupId;
		private String key1Hash;
		private String key2Hash;
		private String salt;
		private String description;
		private boolean enabled;
		private boolean removed;

		/**
		 * Init a WebsiteRecord object using a ResultSet object.
		 * @param rs ResultSet object
		 */
		public WebsiteRecord(ResultSet rs) {
			try {
				this.id = rs.getInt("id"); //$NON-NLS-1$
				this.shaId = rs.getString("sha_id"); //$NON-NLS-1$
				this.domain = rs.getString("domain"); //$NON-NLS-1$
				this.ip = rs.getString("ip"); //$NON-NLS-1$
				this.aiGroupId = rs.getInt("ai_group_id"); //$NON-NLS-1$
				this.key1Hash = rs.getString("key1_hash"); //$NON-NLS-1$
				this.key2Hash = rs.getString("key2_hash"); //$NON-NLS-1$
				this.salt = rs.getString("salt"); //$NON-NLS-1$
				this.description = rs.getString("description"); //$NON-NLS-1$
				this.enabled = rs.getBoolean("enabled"); //$NON-NLS-1$
				this.removed = rs.getBoolean("removed"); //$NON-NLS-1$
			} catch (SQLException ex) {
				Helper.logError(ex, Messages.getString("ChatServer.WEBSITE_LOADING_ERROR"), true); //$NON-NLS-1$
			}
		}

		/**
		 * @return Website id
		 */
		public int     getId()          { return this.id;          }

		/**
		 * @return Website sha id
		 */
		public String  getShaId()       { return this.shaId;       }

		/**
		 * @return Website domain
		 */
		public String  getDomain()      { return this.domain;      }

		/**
		 * @return Website ip
		 */
		public String  getIp()          { return this.ip;          }

		/**
		 * @return AI group id
		 */
		public int     getAiGroupId()   { return this.aiGroupId;   }

		/**
		 * @return Key1 hash
		 */
		public String  getKey1Hash()    { return this.key1Hash;    }

		/**
		 * @return Key2 hash
		 */
		public String  getKey2Hash()    { return this.key2Hash;    }

		/**
		 * @return Salt
		 */
		public String  getSalt()        { return this.salt;        }

		/**
		 * @return Description
		 */
		public String  getDescription() { return this.description; }

		/**
		 * @return Enabled state
		 */
		public boolean isEnabled()      { return this.enabled;     }

		/**
		 * @return Removed state
		 */
		public boolean isRemoved()      { return this.removed;     }
	}
}













