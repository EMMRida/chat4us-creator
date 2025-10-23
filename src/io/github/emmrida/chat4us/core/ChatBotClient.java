/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.caoccao.javet.exceptions.JavetCompilationException;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.converters.JavetProxyConverter;
import com.caoccao.javet.values.reference.V8ValueGlobalObject;

import io.github.emmrida.chat4us.core.ChatSession.ChatSessionState;
import io.github.emmrida.chat4us.ria.NodePanel;
import io.github.emmrida.chat4us.util.Helper;
import io.github.emmrida.chat4us.util.Messages;
import io.github.emmrida.chat4us.util.ScriptEx;

/**
 * This class loads and manages RIA files that contains the static chat bots Q/A ria
 * from the entry to the end node unless there's a switch to an AI or agent.
 *
 * @author El Mhadder Mohamed Rida
 */
public class ChatBotClient {
	private static final Map<String, String[]> BOOLEAN_VALUES = new HashMap<>(); // locale : [non, oui]

    private String mainLocale;
    private Map<String, Integer> entryIds; // locale : entryId
    private Map<String, String> riaFileNames; // locale : fileName
    private Map<String, ChatSession> riaChatSessions; // locale : ChatSession>
	private Map<String, Map<Integer, NodePanel.Data>> riaNodes; // locale : Map<Integer, NodePanel.Data>

    static {
    	BOOLEAN_VALUES.put("FR", new String[] {"non",  "oui"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		BOOLEAN_VALUES.put("EN", new String[] {"no",   "yes"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		BOOLEAN_VALUES.put("DE", new String[] {"nein", "ja"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		BOOLEAN_VALUES.put("ES", new String[] {"no",   "si"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		BOOLEAN_VALUES.put("IT", new String[] {"no",   "si"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		BOOLEAN_VALUES.put("AR", new String[] {"لا",    "نعم"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Init the chat bot client.
     */
    public ChatBotClient() {
    	this.entryIds = new HashMap<>();
    	this.riaFileNames = new HashMap<>();
		this.riaChatSessions = new HashMap<>();
		this.riaNodes = new HashMap<>();
    }

    /**
     * Performs a move to another chat bot node.
     * @param nodeId Node id to move to.
     * @param ses Chat session object.
     * @return The message of the destination node.
     */
    public String moveTo(ChatSession ses, int nodeId) {
        NodePanel.Data nodeData = getNodeById(ses.getCurLocale(), nodeId);
        if(nodeData == null)
            throw new IllegalArgumentException(Messages.getString("ChatBotClient.INVALID_ROUTE_ID")); //$NON-NLS-1$
        ses.setCurrentNode(nodeData);
        return nodeData.getMessage();
    }

    /**
     * Gets the boolean value from a user response depending on the locale param.
     * @param locale Locale/language id.
     * @param response User response to covert.
     * @return O|1 on success, -1 on error.
     */
    private int getBooleanValue(String locale, String response) {
    	String r = response.trim();
    	String[] values = BOOLEAN_VALUES.get(locale);
    	if(values != null) {
    		if(r.equalsIgnoreCase(values[0])) {
	    		return 0;
    		} else if(r.equalsIgnoreCase(values[1])) {
				return 1;
			}
    	}
        return -1;
    }

    /**
     * Starts a chat session with chat bot directed by a RIA file.
     * @param ses Chat session object.
     * @return Chat bot messages.
     */
    public String[] letsChat(ChatSession ses) {
        if(ses.isEnded())
            return new String[0];
        List<String> responses = new ArrayList<>();
        NodePanel.Data curNode = getNodeById(ses.getCurLocale(), this.entryIds.get(ses.getCurLocale()));
        NodePanel.Data data = curNode;
        String msg = curNode.getMessage();
        if(!msg.trim().isEmpty()) {
        	responses.add(msg);
        	ses.addHistoryChatMessage(true, msg);
        }
        while(curNode.getValType().equals("nop")) { //$NON-NLS-1$
            ses.setCurrentNode(getNodeById(ses.getCurLocale(), data.getSucMoveTo()));
            curNode = ses.getCurrentNode();
            if(curNode != null) {
				msg = curNode.getMessage();
				if(!msg.trim().isEmpty()) {
					responses.add(msg);
					ses.addHistoryChatMessage(true, msg);
				}
            } else {
                ses.setEnded(true);
                break;
            }
        }
        return responses.toArray(new String[responses.size()]);
    }

    /**
     * Processes the user message.
     * @param ses Chat session object.
     * @param response User message.
     * @return Chat bot messages depending on current node condition validation.
     */
    public String[] userMessage(ChatSession ses, String response) {
        String[] botMessages = null;
        if(!response.isEmpty())
            ses.addHistoryChatMessage(false, response.trim());
        NodePanel.Data curNode = ses.getCurrentNode();
        NodePanel.Data data = curNode;
        if(data == null) {
            ses.setEnded(true);
            return new String[] { Messages.getString("ChatBotClient.OFFLINE") }; //$NON-NLS-1$
        }
        if("nop".equals(data.getValType())) { //$NON-NLS-1$
            List<String> responses = new ArrayList<>();
            while(curNode.getValType().equals("nop")) { //$NON-NLS-1$
                ses.setCurrentNode(getNodeById(ses.getCurLocale(), data.getSucMoveTo()));
                responses.add(curNode.getMessage());
                curNode = ses.getCurrentNode();
                if(curNode == null) {
                    ses.setEnded(true);
                    responses.add(Messages.getString("ChatBotClient.OFFLINE")); //$NON-NLS-1$
                    break;
                }
            }
            botMessages = responses.toArray(new String[responses.size()]);
        } else if("text:any".equals(data.getValType())) { //$NON-NLS-1$
            if(response.isEmpty()) {
                botMessages =  processError(ses, data, response);
            } else botMessages =  processSuccess(ses, data, response);
        } else if("text:equal".equals(data.getValType())) { //$NON-NLS-1$
            if(response.equalsIgnoreCase(data.getValCondition())) {
                botMessages =  processSuccess(ses, data, response);
            } else botMessages =  processError(ses, data, response);
        } else if("text:in_list".equals(data.getValType())) { //$NON-NLS-1$
            boolean found = false;
            String[] list = data.getValCondition().split(";"); //$NON-NLS-1$
            for(String s : list) {
                if(s.equalsIgnoreCase(response)) {
                    found = true;
                    break;
                }
            }
            if(found) {
                botMessages =  processSuccess(ses, data, response);
            } else botMessages =  processError(ses, data, response);
        } else if("text:email".equals(data.getValType())) { //$NON-NLS-1$
            if(response.isEmpty()) {
                botMessages =  processError(ses, data, response);
            } else {
                if(Helper.isValidEmail(response)) {
                    botMessages =  processSuccess(ses, data, response);
                } else botMessages =  processError(ses, data, response);
            }
        } else if("number:any".equals(data.getValType())) { //$NON-NLS-1$
            try {
                double n = Double.parseDouble(response);
                botMessages =  processSuccess(ses, data, response);
            } catch(NumberFormatException ex) {
                botMessages =  processError(ses, data, response);
            }
        } else if("number:equal".equals(data.getValType())) { //$NON-NLS-1$
            try {
                double n = Double.parseDouble(response);
                if(response.trim().equals(data.getValCondition())) {
                	botMessages =  processSuccess(ses, data, response);
                } else botMessages =  processError(ses, data, response);
            } catch(NumberFormatException ex) {
                botMessages =  processError(ses, data, response);
            }
        } else if("number:interval".equals(data.getValType())) { //$NON-NLS-1$
            if(response.isEmpty()) {
                botMessages =  processError(ses, data, response);
            } else {
                String interval = curNode.getValCondition();
                Pattern p = Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\.\\.\\.(-?\\d+(?:\\.\\d+)?)"); //$NON-NLS-1$
                Matcher m = p.matcher(interval);
                if(m.find()) {
                    try {
                        double min = Double.parseDouble(m.group(1));
                        double max = Double.parseDouble(m.group(2));
                        double val = Double.parseDouble(response);
                        if(val >= min && val <= max) {
                            botMessages =  processSuccess(ses, data, response);
                        } else botMessages = processError(ses, data, response);
                    } catch(NumberFormatException ex) {
                        Helper.logWarning(ex, String.format(Messages.getString("ChatBotClient.INVALID_NUMBER"), response)); //$NON-NLS-1$
                        botMessages =  processError(ses, data, response);
                    }
                }
            }
        } else if("boolean:any".equals(data.getValType())) { //$NON-NLS-1$
            int val = getBooleanValue(ses.getCurLocale(), response);
            if(val == 0 || val == 1) {
                botMessages =  processSuccess(ses, data, response);
            } else botMessages =  processError(ses, data, response);
        } else if("matching_list".equals(data.getValType())) { //$NON-NLS-1$
        	botMessages = processMatchingList(data, ses, response);
        } else if("matching_values".equals(data.getValType())) { //$NON-NLS-1$
	        botMessages = processMatchingValues(data, ses, response);
        } else if("script".equals(data.getValType())) { //$NON-NLS-1$
            String script = data.getScript();
            ses.setVar("message", ""); //$NON-NLS-1$ //$NON-NLS-2$
            Integer nodeId = executeScript(script, ses, response);
            String msg = ses.getVar("message"); // Defined in the script as a return message to show to the user //$NON-NLS-1$
            ses.removeVar("message"); //$NON-NLS-1$
            if(nodeId == null) {
                botMessages = new String[] { curNode.getErrMessage() };
            } else if(nodeId > 0) {
            	curNode = getNodeById(ses.getCurLocale(), nodeId);
                ses.setCurrentNode(curNode);
            	if(curNode != null) {
	                if(msg != null && !msg.trim().isEmpty()) {
	                	botMessages = new String[] { msg, curNode.getMessage() };
	                } else botMessages = new String[] { curNode.getMessage() };
            	} else botMessages = new String[] { msg != null ? msg : Messages.getString("ChatBotClient.OFFLINE") }; //$NON-NLS-1$
            } else if(nodeId == 0) {
                botMessages = new String[] { (msg != null ? msg : ""), curNode.getErrMessage() }; //$NON-NLS-1$
            } else if(nodeId == -1) { // End
                ses.setCurrentNode(null);
                ses.setEnded(true);
                botMessages = new String[] { msg != null ? msg : Messages.getString("ChatBotClient.OFFLINE") }; //$NON-NLS-1$
            } else if(nodeId == -2) { // Restart
                nodeId = entryIds.get(ses.getCurLocale());
                ses.clearVars();
                ses.setCurrentNode(getNodeById(ses.getCurLocale(), nodeId));
                botMessages = new String[] { curNode.getMessage() };
            } else if(nodeId == -3) { // Switch to AI
	            ses.setState(ChatSessionState.AIMODEL);
	            return new String[] { msg != null ? msg : Messages.getString("ChatBotClient.OFFLINE") }; //$NON-NLS-1$
            } else if(nodeId == -4) { // Switch to a human Agent
	            ses.setState(ChatSessionState.AGENT);
	            return new String[] { msg != null ? msg : Messages.getString("ChatBotClient.OFFLINE") }; //$NON-NLS-1$
            }
        }
        if(botMessages == null) {
            IllegalStateException ex = new IllegalStateException(Messages.getString("ChatBotClient.ILLEGAL_STATE_EX")); //$NON-NLS-1$
            Helper.logError(ex, Messages.getString("ChatBotClient.USER_MESSAGE_FAILURE"), true); //$NON-NLS-1$
            throw ex;
        }
        return postProcessChatBotMessages(ses, botMessages);
    }

    /**
    * Executes the node's script on user message. Returns a message defined by the script.
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
    * @param script Script to execute
    * @param ses Chat session object.
    * @param response User response.
    * @return The node id to move to if > 0. 0:repeat. -1:end. -2:restart. -3:switch_to_ai. -4:switch_to_agent. null:Error.
    */
   private Integer executeScript(String script, ChatSession ses, String response) {
       try (V8Runtime v8Runtime = V8Host.getV8Instance().createV8Runtime()) {
           v8Runtime.setConverter(new JavetProxyConverter());
           V8ValueGlobalObject global = v8Runtime.getGlobalObject();
           global.set("ScriptEx", ScriptEx.class); //$NON-NLS-1$
           global.set("response", response); //$NON-NLS-1$
           for(Map.Entry<String, String> entry : ses.getVarsSet())
               global.set(entry.getKey(), entry.getValue());
           Integer ret = v8Runtime.getExecutor(script).executeInteger();
           for(Map.Entry<String, String> entry : ses.getVarsSet()) {
               String varName = entry.getKey();
               if(global.hasOwnProperty(varName)) {
                   Object obj = global.get(entry.getKey());
                   entry.setValue(obj.toString());
                   global.delete(entry.getKey());
               }
           }
           global.delete("response");
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
     * Process a matching list condition based on user response
     * Example: ['user_response_a', 1]['user_repsonse_b', 2]['user_response_c', 3]
     * @param nodeData Node data.
     * @param ses Chat bot session
     * @param response User response.
     * @return Node response based on its condition and validation.
     */
    private String[] processMatchingList(NodePanel.Data nodeData, ChatSession ses, String response) {
    	Objects.requireNonNull(nodeData);
    	Objects.requireNonNull(ses);
    	Objects.requireNonNull(response);
		Map<String, Integer> map = extractMatchingList(nodeData.getValCondition());
		if(map.containsKey(response)) {
			int eid = map.get(response);
			if(eid > 0) {
				NodePanel.Data data = getNodeById(ses.getCurLocale(), eid);
				if(data != null) {
					NodePanel.Data curNode = ses.getCurrentNode();
					ses.setCurrentNode(data);
					return nextSucMessages(ses, curNode.getSucMessage());
				} else return processError(ses, nodeData, response);
			} else {
				String msg = nodeData.getSucMessage();
				switch(eid) {
					case 0: // Repeat
						return new String[] { msg };
					case -1: // End
		                ses.setCurrentNode(null);
		                ses.setEnded(true);
		                return new String[] { msg.isEmpty() ? Messages.getString("ChatBotClient.OFFLINE") : msg }; //$NON-NLS-1$
					case -2: // Restart
		                int nodeId = entryIds.get(ses.getCurLocale());
		                ses.clearVars();
		                ses.setCurrentNode(getNodeById(ses.getCurLocale(), nodeId));
		                return new String[] { ses.getCurrentNode().getMessage() };
					case -3: // Suitch to AI
			            ses.setState(ChatSessionState.AIMODEL);
			            ses.setCurrentNode(getNodeById(ses.getCurLocale(), nodeData.getSucMoveTo()));
			            return nextSucMessages(ses, msg);
					case -4: // Suitch to Agent
				        ses.setState(ChatSessionState.AGENT);
			            ses.setCurrentNode(getNodeById(ses.getCurLocale(), nodeData.getSucMoveTo()));
			            return nextSucMessages(ses, msg);
					default: // Error
						return processError(ses, nodeData, response);
				}
			}
		} else {
			return processError(ses, nodeData, response);
		}
    }

    /**
     * Processes user response by converting it to the corresponding value in the matching values list.
     * Example: ['Français', 'FR']['Arabic', 'AR']['English', 'EN']
     * @param nodeData Node data
     * @param ses Chat bot session
     * @param response User response
     * @return Chat bot response strings.
     */
    private String[] processMatchingValues(NodePanel.Data nodeData, ChatSession ses, String response) {
    	Objects.requireNonNull(nodeData);
    	Objects.requireNonNull(ses);
    	Objects.requireNonNull(response);
		Map<String, String> map = extractMatchingValues(nodeData.getValCondition());
		if(map.containsKey(response)) {
			String value = map.get(response);
			NodePanel.Data curNode = ses.getCurrentNode();
			if(curNode != null && value != null) {
				return processSuccess(ses, nodeData, value);
			} else return processError(ses, nodeData, response);
		} else {
			return processError(ses, nodeData, response);
		}
    }

    /**
     * Helper function that converts a matching list into a hash map object
     * @param list A matching list string
     * @return Map object.
     */
    public static Map<String, Integer> extractMatchingList(String list) {
        Map<String, Integer> map = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile("\\['(.*?)'\\s*,\\s*([-|\\d]+)\\]"); //$NON-NLS-1$
        Matcher matcher = pattern.matcher(list);
        while (matcher.find()) {
            String key = matcher.group(1); // Extract the key
            int value = Integer.parseInt(matcher.group(2)); // Extract and convert value to Integer
            map.put(key, value);
        }
		return map;
    }

    /**
    * Helper function that converts a matching values list into a hash map object.
    * @param list A matching values list string
    * @return Map object
    */
   public static Map<String, String> extractMatchingValues(String list) {
       Map<String, String> map = new LinkedHashMap<>();
       Pattern pattern = Pattern.compile("\\['(.*?)'\\s*,\\s*'(.*?)'\\]"); //$NON-NLS-1$
       Matcher matcher = pattern.matcher(list);
       while (matcher.find()) {
           String key = matcher.group(1); // Extract the key
           String value = matcher.group(2); // Extract value
           map.put(key, value);
       }
		return map;
   }

    /**
     * Post processes cha bot messages for additional format and replacing variables names by their values.
     * @param botMessages List of bot messages.
     * @return List of bot messages.
     */
    private String[] postProcessChatBotMessages(ChatSession ses, String[] botMessages) {
        for(int i = 0; i < botMessages.length; i++) {
            String msg = botMessages[i].replaceAll("\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
            if(!msg.isEmpty()) {
                botMessages[i] = replacePlaceholders(ses, msg);
                ses.addHistoryChatMessage(true, botMessages[i].trim());
            }
        }
        return botMessages;
    }

    /**
     * Chat bots can save variables by choosing "variable:user_value" or "variable:operation" and show them
     * inside Node messages and success/error messages using {:var_name:} format. Chat bots can also read/write variables
     * in a node script.
     * @param ses Chat bot session
     * @param msg Chat bot message.
     * @return Post processed chat bot message.
     */
    private String replacePlaceholders(ChatSession ses, String msg) {
        // Regex to match placeholders like {:key:}
        Pattern pattern = Pattern.compile("\\{:(.*?):\\}"); //$NON-NLS-1$
        Matcher matcher = pattern.matcher(msg);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1); // Extract the key inside {:...:}
            String replacement = ses.getVar(key, ""); // Get the replacement or empty string //$NON-NLS-1$
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result); // Append the remaining part of the template

        return result.toString();
    }

    /**
    * Returns next success messages. If a node condition=nop the chat bot client will move automatically
    * to the next node until the last node or a node with a condition!=nop.
    * @param ses Chat bot session
    * @param firstMsg First message to return.
    * @return Next success messages.
    */
   public String[] nextSucMessages(ChatSession ses, String firstMsg) {
       if(ses.isEnded())
           return new String[0];
       List<String> responses = new ArrayList<>();
       responses.add(firstMsg);
       NodePanel.Data curNode = ses.getCurrentNode();
       if(curNode != null) {
    	   responses.add(curNode.getMessage());
		   while(curNode.getValType().equals("nop")) { //$NON-NLS-1$
				curNode = getNodeById(ses.getCurLocale(), curNode.getSucMoveTo());
				if(curNode != null) {
					responses.add(curNode.getMessage());
					ses.setCurrentNode(curNode);
				} else {
					if(ses.getState() == ChatSessionState.CHATBOT)
						ses.setEnded(true);
					break;
				}
		   }
       }
       return responses.toArray(new String[responses.size()]);
   }

   /**
    * Returns next error messages. If a node condition=nop the chat bot client will move automatically
    * to the next node until the last node or a node with a condition!=nop.
    * @param ses Chat bot session
    * @param firstMsg First message to return.
    * @return Next error messages.
    */
   public String[] nextErrMessages(ChatSession ses, String firstMsg) {
       if(ses.isEnded())
           return new String[0];
       List<String> responses = new ArrayList<>();
       responses.add(firstMsg);
       NodePanel.Data curNode = ses.getCurrentNode();
       if(curNode != null) {
    	   responses.add(curNode.getMessage());
           	while(curNode.getValType().equals("nop")) { //$NON-NLS-1$
               curNode = getNodeById(ses.getCurLocale(), curNode.getErrMoveTo());
               if(curNode != null) {
                   responses.add(curNode.getMessage());
                   ses.setCurrentNode(curNode);
               } else {
	           	   if(ses.getState() == ChatSessionState.CHATBOT)
	           		   ses.setEnded(true);
	               break;
               }
           }
       }
       return responses.toArray(new String[responses.size()]);
   }

    /**
     * Processes current node success action and return success messages then moves to the node id specified
     * int the success action.
     * @param ses Chat bot session.
     * @param nodeData Node data.
     * @param response User response.
     * @return Next success messages.
     */
    private String[] processSuccess(ChatSession ses, NodePanel.Data nodeData, String response) {
        NodePanel.Data curNode = ses.getCurrentNode();
        if("nop".equals(nodeData.getSucAction())) { //$NON-NLS-1$
            String msg = curNode.getSucMessage();
            ses.setCurrentNode(getNodeById(ses.getCurLocale(), nodeData.getSucMoveTo()));
            return nextSucMessages(ses, msg);
        }
        if("repeat".equals(nodeData.getSucAction())) { //$NON-NLS-1$
        	List<String> responses = new ArrayList<>();
            responses.add(nodeData.getSucMessage());
            responses.add(nodeData.getMessage());
			return responses.toArray(new String[responses.size()]);
        }
        if("restart".equals(nodeData.getSucAction())) { //$NON-NLS-1$
            String msg = curNode.getSucMessage();
            ses.setCurrentNode(getNodeById(ses.getCurLocale(), entryIds.get(ses.getCurLocale())));
            return nextSucMessages(ses, msg);
        }
        if("end".equals(nodeData.getSucAction())) { //$NON-NLS-1$
            ses.setEnded(true);
            // TODO : Ensure no more nodes processed
            return new String[] { nodeData.getSucMessage() };
        }
        if("variable:user_value".equals(nodeData.getSucAction())) { //$NON-NLS-1$
            ses.setVar(nodeData.getSucValue(), response);
            String msg = curNode.getSucMessage();
            ses.setCurrentNode(getNodeById(ses.getCurLocale(), nodeData.getSucMoveTo()));
            return nextSucMessages(ses, msg);
        }
        if("user_locale:user_value".equals(nodeData.getSucAction())) { //$NON-NLS-1$
        	return changeUserLocale(ses, nodeData, response);
        }
        if("switch_to_ai".equals(nodeData.getSucAction())) { //$NON-NLS-1$
            ses.setState(ChatSessionState.AIMODEL);
            String msg = curNode.getSucMessage();
            ses.setCurrentNode(getNodeById(ses.getCurLocale(), nodeData.getSucMoveTo()));
            return nextSucMessages(ses, msg);
        }
        if("switch_to_agent".equals(nodeData.getSucAction())) { //$NON-NLS-1$
	        ses.setState(ChatSessionState.AGENT);
            String msg = curNode.getSucMessage();
            ses.setCurrentNode(getNodeById(ses.getCurLocale(), nodeData.getSucMoveTo()));
            return nextSucMessages(ses, msg);
        }
        if("variable:operation".equals(nodeData.getSucAction())) { //$NON-NLS-1$
            String[] parts = nodeData.getSucValue().split("=", 2); //$NON-NLS-1$
            if(parts.length == 2) {
                Character c = parts[1].charAt(0);
                Double val = Double.NaN;
                try {
                    val = Double.parseDouble(parts[1].substring(1));
                } catch(NumberFormatException ex) {
                    ex.printStackTrace();
                    return processError(ses, nodeData, response);
                }
                String key = parts[0].trim();
                switch(c) {
                case '+':
                    Double v = Double.parseDouble(ses.getVar(key, "0")); //$NON-NLS-1$
                    String s = String.valueOf(v + val);
                    if(s.endsWith(".0")) s = s.substring(0, s.length() - 2); //$NON-NLS-1$
                    ses.setVar(key, s);
                    break;
                case '-':
                    v = Double.parseDouble(ses.getVar(key, "0")); //$NON-NLS-1$
                    s = String.valueOf(v - val);
                    if(s.endsWith(".0")) s = s.substring(0, s.length() - 2); //$NON-NLS-1$
                    ses.setVar(key, s);
                    break;
                case '*':
                    v = Double.parseDouble(ses.getVar(key, "0")); //$NON-NLS-1$
                    s = String.valueOf(v * val);
                    if(s.endsWith(".0")) s = s.substring(0, s.length() - 2); //$NON-NLS-1$
                    ses.setVar(key, s);
                    break;
                case '/':
                    v = Double.parseDouble(ses.getVar(key, "0")); //$NON-NLS-1$
                    s = String.valueOf(v + val);
                    if(s.endsWith(".0")) s = s.substring(0, s.length() - 2); //$NON-NLS-1$
                    ses.setVar(key, s);
                    break;
                default:
                    return processError(ses, nodeData, response);
                }
                String msg = curNode.getSucMessage();
                return nextSucMessages(ses, msg);
            }
        }

        return null;
    }

    /**
     * Changes current chat bot locale based on user response and available locals in the RIA parameters table.
     * To add a locolized RIA file, Add in the parameters table a key/entry pair
     * in the form locale_<locale_name> : RIA file relative path. Example: locale_EN : ria_file_EN.ria
     * This will load the localized RIA file, switch to it then returns the entry node message.
     * @param ses Chat bot session
     * @param nodeData Node data
     * @param response User response
     * @return Localized RIA file entry node message.
     */
    private String[] changeUserLocale(ChatSession ses, NodePanel.Data nodeData, String response) {
    	String ria = ses.getAiModelParam("locale_" + response); //$NON-NLS-1$
		if(ria != null && new File(ria).exists()) {
			try {
				loadRIA(ria, response);
				ses.setCurLocale(response, this.riaChatSessions.get(response));
				List<String> responses = new ArrayList<>();
				responses.add(nodeData.getSucMessage());
				int eid = entryIds.get(response);
				NodePanel.Data node = getNodeById(response, eid);
				ses.setCurrentNode(node);
				responses.add(node.getMessage());
				return responses.toArray(new String[responses.size()]);
			} catch (Exception ex) {
				Helper.logError(ex, String.format(Messages.getString("ChatBotClient.ERROR_LOADING_RIA"), response,  ria)); //$NON-NLS-1$
			}
		} else Helper.logWarning(String.format(Messages.getString("ChatBotClient.NO_RIA_FOR_LOCALE"), response, ria)); //$NON-NLS-1$
		return processError(ses, nodeData, response);
	}

	/**
     * Processes current node error action and return error messages then moves to the node id specified
     * int the error action.
     * @param ses Chat session object
     * @param nodeData Node data
     * @param response User response
     * @return Next error messages.
     */
    private String[] processError(ChatSession ses, NodePanel.Data nodeData, String response) {
        NodePanel.Data curNode = ses.getCurrentNode();
        if("nop".equals(nodeData.getErrAction())) { //$NON-NLS-1$
            String msg = curNode.getErrMessage();
            ses.setCurrentNode(getNodeById(ses.getCurLocale(), nodeData.getErrMoveTo()));
            return nextErrMessages(ses, msg);
        }
        if("repeat".equals(nodeData.getErrAction())) { //$NON-NLS-1$
        	List<String> responses = new ArrayList<>();
            responses.add(nodeData.getErrMessage());
            responses.add(nodeData.getMessage());
			return responses.toArray(new String[responses.size()]);
        }
        if("restart".equals(nodeData.getErrAction())) { //$NON-NLS-1$
            String msg = curNode.getErrMessage();
            ses.setCurrentNode(getNodeById(ses.getCurLocale(), entryIds.get(ses.getCurLocale())));
            return nextErrMessages(ses, msg);
        }
        if("end".equals(nodeData.getErrAction())) { //$NON-NLS-1$
            ses.setEnded(true);
            ses.setCurrentNode(null);
            return new String[] { nodeData.getErrMessage() };
        }
        if("variable:user_value".equals(nodeData.getErrAction())) { //$NON-NLS-1$
            ses.setVar(nodeData.getErrValue(), response);
            String msg = curNode.getErrMessage();
            ses.setCurrentNode(getNodeById(ses.getCurLocale(), curNode.getErrMoveTo()));
            return nextErrMessages(ses, msg);
        }
        if("switch_to_ai".equals(nodeData.getErrAction())) { //$NON-NLS-1$
            ses.setState(ChatSessionState.AIMODEL);
            String msg = curNode.getErrMessage();
            ses.setCurrentNode(getNodeById(ses.getCurLocale(), nodeData.getErrMoveTo()));
            return nextErrMessages(ses, msg);
        }
		if("switch_to_agent".equals(nodeData.getErrAction())) { //$NON-NLS-1$
	        ses.setState(ChatSessionState.AGENT);
            String msg = curNode.getErrMessage();
            ses.setCurrentNode(getNodeById(ses.getCurLocale(), nodeData.getErrMoveTo()));
            return nextErrMessages(ses, msg);
		}
        if("variable:operation".equals(nodeData.getErrAction())) { //$NON-NLS-1$
            String[] parts = response.split("=", 2); //$NON-NLS-1$
            if(parts.length == 2) {
                Character c = parts[1].charAt(0);
                Double val = Double.NaN;
                try {
                    val = Double.parseDouble(parts[1].substring(1));
                } catch(NumberFormatException ex) {
                    ex.printStackTrace();
                    return processError(ses, nodeData, response);
                }
                String key = parts[0].trim();
                switch(c) {
                case '+':
                    Double v = Double.parseDouble(ses.getVar(key, "0.0")); //$NON-NLS-1$
                    ses.setVar(key, String.valueOf(v + val));
                    break;
                case '-':
                    v = Double.parseDouble(ses.getVar(key, "0.0")); //$NON-NLS-1$
                    ses.setVar(key, String.valueOf(v - val));
                    break;
                case '*':
                    v = Double.parseDouble(ses.getVar(key, "0.0")); //$NON-NLS-1$
                    ses.setVar(key, String.valueOf(v * val));
                    break;
                case '/':
                    v = Double.parseDouble(ses.getVar(key, "0.0")); //$NON-NLS-1$
                    ses.setVar(key, String.valueOf(v / val));
                    break;
                default:
                    return processError(ses, nodeData, response);
                }
                return nextErrMessages(ses, curNode.getErrMessage());
            }
        }

        return null;
    }

    /**
     * Reloads all RIA files. Its called when a RIA file is changed and the user wants to apply changes
     * to the running chat bot server.
     * @throws Exception
     */
    public void reloadRIAs() throws Exception {
    	Map<String, String> rias = this.riaFileNames;
    	this.entryIds = new HashMap<>();
		this.riaFileNames = new HashMap<>();
		this.riaChatSessions = new HashMap<>();
		this.riaNodes = new HashMap<>();
		String locale = this.mainLocale;
		this.mainLocale = null;
		loadRIA(rias.get(locale), null);
		rias.remove(locale);
		for(String loc : rias.keySet()) {
			loadRIA(rias.get(loc), loc);
		}
    }

    /**
     * Loads a RIA file into memory so as to use it by the chat bot server when needed.
     * @param riaFileName RIA file relative or absolute path.
     * @param locale Locale of the RIA file
     * @throws Exception
     */
    public void loadRIA(String riaFileName, String locale) throws Exception {
    	if(locale == null && this.riaFileNames.size() > 0)
    		throw new IllegalArgumentException(Messages.getString("ChatBotClient.RIA_ALREADY_LOADED")); //$NON-NLS-1$
    	riaFileName = Helper.osSpecificPath(riaFileName);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(riaFileName);
        document.getDocumentElement().normalize();
        NodeList nodes = document.getElementsByTagName("info").item(0).getChildNodes(); //$NON-NLS-1$
        String loc = Helper.getNodeValue(nodes, "locale"); //$NON-NLS-1$
        if(locale == null) {
        	this.mainLocale = loc;
        } else {
            if(!loc.equals(locale))
    			throw new IllegalArgumentException(String.format(Messages.getString("ChatBotClient.INVALID_RIA_LOCALE"), riaFileName)); //$NON-NLS-1$
        }
        this.riaFileNames.put(loc, riaFileName);
        this.entryIds.put(loc, Integer.parseInt(Helper.getNodeValue(nodes, "entry_id"))); //$NON-NLS-1$
        nodes = document.getElementsByTagName("ai_model").item(0).getChildNodes(); //$NON-NLS-1$
        ChatSession riaSes = new ChatSession();
        riaSes.setCurLocale(loc, null);
        riaSes.setBotName(Helper.getNodeValue(nodes, "name")); //$NON-NLS-1$
        riaSes.setAiModelGuidelines(Helper.getNodeValue(nodes, "guidelines")); //$NON-NLS-1$
        riaSes.setBotScript(Helper.unescapeHtml(Helper.getNodeValue(nodes, "script"))); //$NON-NLS-1$
        this.riaChatSessions.put(loc, riaSes);
        nodes = document.getElementsByTagName("params").item(0).getChildNodes(); //$NON-NLS-1$
        for(int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE) {
        		Node item = Helper.getNodeByName(node.getChildNodes(), "key").getChildNodes().item(0); //$NON-NLS-1$
	        	String param = ""; //$NON-NLS-1$
	        	if(item != null)
	        		param = item.getNodeValue();
	        	item = Helper.getNodeByName(node.getChildNodes(), "value").getChildNodes().item(0); //$NON-NLS-1$
	        	String value = ""; //$NON-NLS-1$
	        	if(item != null)
	        		value = item.getNodeValue();
	        	riaSes.addAiModelParam(param, value);
            }
        }

        Map<Integer, NodePanel.Data> nodesMap = new HashMap<>();
        nodes = document.getElementsByTagName("question"); //$NON-NLS-1$
        for(int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            NodePanel.Data data = NodePanel.Data.fromXml(node);
            nodesMap.put(data.getId(), data);
        }
        this.riaNodes.put(loc, nodesMap);
        riaSes.setCurrentNode(getNodeById(loc, entryIds.get(loc)));
    }

    /**
     * Returns a node data by its ID
     * @param locale Locale of the RIA file
     * @param nodeId Node item id
     * @return Node Item data or null on error.
     */
    public NodePanel.Data getNodeById(String locale, int nodeId) {
    	Map<Integer, NodePanel.Data> routeItems = this.riaNodes.get(locale);
    	if(routeItems == null)
			return null;
    	return routeItems.get(nodeId);
    }

    public int getEntryId(String locale) { return entryIds.get(locale); }

   /**
    * Returns the number of nodes in current RIA file.
    * @param locale Locale of the RIA file.
    * @return
    */
    public int getNodesCount(String locale) {
    	Map<Integer, NodePanel.Data> nodes = this.riaNodes.get(locale);
    	if(nodes == null)
			return -1;
    	return nodes.size();
    }

    /**
     * Returns the default RIA chat session
     * @param locale Locale of the RIA file
     * @return Default chat session or null on error.
     */
    public ChatSession riaChatSession(String locale) { return riaChatSessions.get(locale); }

    /**
     * Return the main RIA file name of current chat bot.
     * @return Main RIA file name
     */
    public String getRiaFileName() { return Helper.getRelativePath(new File(Helper.osSpecificPath(riaFileNames.get(mainLocale))), new File(".")); } //$NON-NLS-1$

    /**
     * Returns the RIA file name by its locale
     * @param locale Locale of the RIA file
     * @return RIA file name
     */
    public String getRiaFileName(String locale) { return Helper.getRelativePath(new File(Helper.osSpecificPath(riaFileNames.get(locale))), new File(".")); } //$NON-NLS-1$

    /**
     * Returns the chat bot name.
     * @return Chat bot name
     */
    public String getChatBotName() { return riaChatSessions.get(this.mainLocale).getBotName(); }

	/**
	 * Returns the main chat bot locale.
	 * @return Main chat bot locale
	 */
    public String getMainLocale() { return mainLocale; }
}















