/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.util;

import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;

/**
 * Manages the application settings.
 *
 * @author El Mhadder Mohamed Rida
 */
public class Settings {
	public static final String SETTINGS_FILE = "./settings.cfg"; //$NON-NLS-1$
	public static final String SPLIT_CHARS_R = "\\<\\*\\>"; //$NON-NLS-1$
	public static final String SPLIT_CHARS_W = "<*>"; //$NON-NLS-1$
	private List<String> openedRecently;
	private Point mainWndPos;
	private int mainWndState;
	private Dimension mainWndSize;
	private Point mainWndSplitPos;

	// ChatBotSettingsDialog
	private Point dlgChatBotPos;
	private Dimension dlgChatBotSize;

	// NodeSettingsDialog
	private Point dlgRouteItemPos;
	private Dimension dlgRouteItemSize;

	// SettingsDialog
	private int maxRecentFiles;
	private int logsTimeoutDays;
	private int chatsTimeoutDays;
	private int aiContextLines;
	private int aiQueryMaxLength;
	private int aiServersTasks;
	private int aiLogOnLongResponse;
	private int chatSessionsTimeoutMinutes;
	private int agentResponseTimeoutSeconds;
	private boolean nsLookupOnLogin; // Igored
	private boolean minimizeToTray;
	private boolean notifyOnErrors;

	private String defLocale;

	/**
	 * Init settings with default values.
	 */
	public Settings() {
		openedRecently = new ArrayList<>();
		mainWndPos = new Point();
		mainWndSize = new Dimension();
		mainWndState = JFrame.NORMAL;
		mainWndSplitPos = new Point();

		dlgChatBotPos = new Point();
		dlgChatBotSize = new Dimension();

		dlgRouteItemPos = new Point();
		dlgRouteItemSize = new Dimension();

		maxRecentFiles = 10;
		logsTimeoutDays = 7;
		chatsTimeoutDays = 7;
		aiContextLines = 25;
		aiQueryMaxLength = 1024;
		aiServersTasks = 4;
		aiLogOnLongResponse = 2;
		chatSessionsTimeoutMinutes = 20;
		agentResponseTimeoutSeconds = 120;
		nsLookupOnLogin = false;
		minimizeToTray = false;
		notifyOnErrors = false;

		String loc = Locale.getDefault().getLanguage();
		defLocale = loc.length() > 0 ? loc : "en"; //$NON-NLS-1$
	}

	/**
	 * Saves the settings to the settings file.
	 */
	public void save() {
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(SETTINGS_FILE, false))) {
			bw.write("defLocale=" + defLocale + System.lineSeparator()); //$NON-NLS-1$
			bw.write("openedRecently="); //$NON-NLS-1$
			for(int i = 0; i < openedRecently.size(); i++) {
				bw.write(openedRecently.get(i));
				if(i >= maxRecentFiles-1)
					break;
				if(i < openedRecently.size()-1)
					bw.write(SPLIT_CHARS_W);
			}
			bw.write(System.lineSeparator() + "mainWndPos="); //$NON-NLS-1$
			bw.write(mainWndPos.x + "," + mainWndPos.y + System.lineSeparator()); //$NON-NLS-1$
			bw.write("mainWndSize="); //$NON-NLS-1$
			bw.write(mainWndSize.width + "," + mainWndSize.height + System.lineSeparator()); //$NON-NLS-1$
			bw.write("mainWndState="); //$NON-NLS-1$
			bw.write(mainWndState + System.lineSeparator());
			bw.write("mainWndSplitPos="); //$NON-NLS-1$
			bw.write(mainWndSplitPos.x + "," + mainWndSplitPos.y + System.lineSeparator()); //$NON-NLS-1$
			bw.write("dlgChatBotPos="); //$NON-NLS-1$
			bw.write(dlgChatBotPos.x + "," + dlgChatBotPos.y + System.lineSeparator()); //$NON-NLS-1$
			bw.write("dlgChatBotSize="); //$NON-NLS-1$
			bw.write(dlgChatBotSize.width + "," + dlgChatBotSize.height + System.lineSeparator()); //$NON-NLS-1$
			bw.write("dlgRouteItemPos="); //$NON-NLS-1$
			bw.write(dlgRouteItemPos.x + "," + dlgRouteItemPos.y + System.lineSeparator()); //$NON-NLS-1$
			bw.write("dlgRouteItemSize="); //$NON-NLS-1$
			bw.write(dlgRouteItemSize.width + "," + dlgRouteItemSize.height + System.lineSeparator()); //$NON-NLS-1$
			bw.write("maxRecentFiles="); //$NON-NLS-1$
			bw.write(maxRecentFiles + System.lineSeparator());
			bw.write("logsTimeoutDays="); //$NON-NLS-1$
			bw.write(logsTimeoutDays + System.lineSeparator());
			bw.write("chatsTimeoutDays="); //$NON-NLS-1$
			bw.write(chatsTimeoutDays + System.lineSeparator());
			bw.write("aiContextLines="); //$NON-NLS-1$
			bw.write(aiContextLines + System.lineSeparator());
			bw.write("aiQueryMaxLength="); //$NON-NLS-1$
			bw.write(aiQueryMaxLength + System.lineSeparator());
			bw.write("aiServersTasks="); //$NON-NLS-1$
			bw.write(aiServersTasks + System.lineSeparator());
			bw.write("aiLogOnLongResponse="); //$NON-NLS-1$
			bw.write(aiLogOnLongResponse + System.lineSeparator());
			bw.write("chatSessionsTimeoutMinutes="); //$NON-NLS-1$
			bw.write(chatSessionsTimeoutMinutes + System.lineSeparator());
			bw.write("agentResponseTimeoutSeconds="); //$NON-NLS-1$
			bw.write(agentResponseTimeoutSeconds + System.lineSeparator());
			bw.write("nsLookupOnLogin="); //$NON-NLS-1$
			bw.write(nsLookupOnLogin + System.lineSeparator());
			bw.write("minimizeToTray="); //$NON-NLS-1$
			bw.write(minimizeToTray + System.lineSeparator());
			bw.write("notifyOnErrors="); //$NON-NLS-1$
			bw.write(notifyOnErrors + System.lineSeparator());
			bw.flush();
		} catch (Exception ex) {
			Helper.logError(ex, Messages.getString("Settings.SETTINGS_SAVE_ERROR"), true); //$NON-NLS-1$
		}
	}

	/**
	 * Loads the settings from the settings file.
	 *
	 * @return the settings object, null on error.
	 */
	public static Settings load() {
		try(BufferedReader br = new BufferedReader(new FileReader(SETTINGS_FILE))) {
			Settings st = new Settings();
			String line;
			String[] items;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("openedRecently=")) { //$NON-NLS-1$
					items = line.substring(15).trim().split(SPLIT_CHARS_R);
					for (int i = 0; i < items.length; i++)
						st.openedRecently.add(items[i]);
				} else if (line.startsWith("mainWndPos=")) { //$NON-NLS-1$
					items = line.substring(11).trim().split(","); //$NON-NLS-1$
					st.setMainWndPos(new Point(Integer.parseInt(items[0]), Integer.parseInt(items[1])));
				} else if (line.startsWith("mainWndSize=")) { //$NON-NLS-1$
					items = line.substring(12).trim().split(","); //$NON-NLS-1$
					st.setMainWndSize(new Dimension(Integer.parseInt(items[0]), Integer.parseInt(items[1])));
				} else if (line.startsWith("mainWndState=")) { //$NON-NLS-1$
					st.setMainWndState(Integer.parseInt(line.substring(13).trim()));
				} else if (line.startsWith("dlgChatBotPos=")) { //$NON-NLS-1$
					items = line.substring(14).trim().split(","); //$NON-NLS-1$
					st.setDlgChatBotPos(new Point(Integer.parseInt(items[0]), Integer.parseInt(items[1])));
				} else if (line.startsWith("dlgChatBotSize=")) { //$NON-NLS-1$
					items = line.substring(15).trim().split(","); //$NON-NLS-1$
					st.setDlgChatBotSize(new Dimension(Integer.parseInt(items[0]), Integer.parseInt(items[1])));
				} else if (line.startsWith("mainWndSplitPos=")) { //$NON-NLS-1$
					items = line.substring(16).trim().split(","); //$NON-NLS-1$
					st.setMainWndSplitPos(new Point(Integer.parseInt(items[0]), Integer.parseInt(items[1])));
				} else if (line.startsWith("dlgRouteItemPos=")) { //$NON-NLS-1$
					items = line.substring(16).trim().split(","); //$NON-NLS-1$
					st.setDlgRouteItemPos(new Point(Integer.parseInt(items[0]), Integer.parseInt(items[1])));
				} else if (line.startsWith("dlgRouteItemSize=")) { //$NON-NLS-1$
					items = line.substring(17).trim().split(","); //$NON-NLS-1$
					st.setDlgRouteItemSize(new Dimension(Integer.parseInt(items[0]), Integer.parseInt(items[1])));
				} else if (line.startsWith("maxRecentFiles=")) { //$NON-NLS-1$
					st.setMaxRecentFiles(Integer.parseInt(line.substring(15).trim()));
				} else if (line.startsWith("logsTimeoutDays=")) { //$NON-NLS-1$
					st.setLogsTimeoutDays(Integer.parseInt(line.substring(16).trim()));
				} else if (line.startsWith("chatsTimeoutDays=")) { //$NON-NLS-1$
					st.setChatsTimeoutDays(Integer.parseInt(line.substring(17).trim()));
				} else if (line.startsWith("aiContextLines=")) { //$NON-NLS-1$
					st.setAiContextLines(Integer.parseInt(line.substring(15).trim()));
				} else if (line.startsWith("aiQueryMaxLength=")) { //$NON-NLS-1$
					st.setAiQueryMaxLength(Integer.parseInt(line.substring(17).trim()));
				} else if (line.startsWith("aiServersTasks=")) { //$NON-NLS-1$
					st.setAiServersTasks(Integer.parseInt(line.substring(15).trim()));
				} else if (line.startsWith("aiLogOnLongResponse=")) { //$NON-NLS-1$
					st.setAiLogOnLongResponse(Integer.parseInt(line.substring(20).trim()));
				} else if (line.startsWith("chatSessionsTimeoutMinutes=")) { //$NON-NLS-1$
					st.setChatSessionsTimeoutMinutes(Integer.parseInt(line.substring(27).trim()));
				} else if (line.startsWith("agentResponseTimeoutSeconds=")) { //$NON-NLS-1$
					st.setAgentResponseTimeoutSeconds(Integer.parseInt(line.substring(28).trim()));
				} else if (line.startsWith("nsLookupOnLogin=")) { //$NON-NLS-1$
					st.setNsLookupOnLogin(Boolean.parseBoolean(line.substring(16).trim()));
				} else if (line.startsWith("defLocale=")) { //$NON-NLS-1$
					st.setDefLocale(line.substring(10).trim());
				} else if (line.startsWith("minimizeToTray=")) { //$NON-NLS-1$
					st.setMinimizeToTray(Boolean.parseBoolean(line.substring(15).trim()));
				} else if (line.startsWith("notifyOnErrors=")) { //$NON-NLS-1$
					st.setNotifyOnErrors(Boolean.parseBoolean(line.substring(15).trim()));
				} else Helper.logError(Messages.getString("Settings.SETTINGS_INVALID_LINE") + line); //$NON-NLS-1$
			}
			return st;
		} catch (Exception ex) {
			Helper.logError(ex, Messages.getString("Settings.SETTINGS_LOAD_ERROR"), true); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Gets the default locale.
	 *
	 * @return the default locale
	 */
	public String getDefLocale() { return defLocale; }

	/**
	 * Sets the default locale.
	 *
	 * @param value the new default locale
	 */
	public void setDefLocale(String value) { this.defLocale = value; }

	/**
	 * Ns lookup on login.
	 *
	 * @return true, if successful
	 */
	public boolean nsLookupOnLogin() { return nsLookupOnLogin; }

	/**
	 * Gets the agent response timeout seconds.
	 *
	 * @return the agent response timeout seconds
	 */
	public int getAgentResponseTimeoutSeconds() { return agentResponseTimeoutSeconds; }

	/**
	 * Gets the chat sessions timeout minutes.
	 *
	 * @return the chat sessions timeout minutes
	 */
	public int getChatSessionsTimeoutMinutes() { return chatSessionsTimeoutMinutes; }

	/**
	 * Gets the ai servers tasks.
	 *
	 * @return the ai servers tasks
	 */
	public int getAiServersTasks() { return aiServersTasks; }

	/**
	 * Gets the ai context lines.
	 *
	 * @return the ai context lines
	 */
	public int getAiContextLines() { return aiContextLines; }

	/**
	 * Gets the ai query max length.
	 *
	 * @return the ai query max length
	 */
	public int getAiQueryMaxLength() { return aiQueryMaxLength; }

	/**
	 * Gets the ai log on long response.
	 *
	 * @return the ai log on long response
	 */
	public int getAiLogOnLongResponse() { return aiLogOnLongResponse; }

	/**
	 * Gets the chats timeout days.
	 *
	 * @return the chats timeout days
	 */
	public int getChatsTimeoutDays() { return chatsTimeoutDays; }

	/**
	 * Gets the logs timeout days.
	 *
	 * @return the logs timeout days
	 */
	public int getLogsTimeoutDays() { return logsTimeoutDays; }

	/**
	 * Gets the max recent files.
	 *
	 * @return the max recent files
	 */
	public int getMaxRecentFiles() { return maxRecentFiles; }

	/**
	 * Sets the ns lookup on login.
	 *
	 * @param value the new ns lookup on login
	 */
	public void setNsLookupOnLogin(boolean value) { this.nsLookupOnLogin = value; }

	/**
	 * Sets the agent response timeout seconds.
	 *
	 * @param seconds the new agent response timeout seconds
	 */
	public void setAgentResponseTimeoutSeconds(int seconds) { this.agentResponseTimeoutSeconds = seconds; }

	/**
	 * Sets the chat sessions timeout minutes.
	 *
	 * @param minutes the new chat sessions timeout minutes
	 */
	public void setChatSessionsTimeoutMinutes(int minutes) { this.chatSessionsTimeoutMinutes = minutes; }

	/**
	 * Sets the ai servers tasks.
	 *
	 * @param n the new ai servers tasks
	 */
	public void setAiServersTasks(int n) { this.aiServersTasks = n; }

	/**
	 * Sets the ai context lines.
	 *
	 * @param n the new ai context lines
	 */
	public void setAiContextLines(int n) { this.aiContextLines = n; }

	/**
	 * Sets the ai query max length.
	 *
	 * @param value the new ai query max length
	 */
	public void setAiQueryMaxLength(int value) { aiQueryMaxLength = value; }

	/**
	 * Sets the ai log on long response.
	 *
	 * @param value the new ai log on long response
	 */
	public void setAiLogOnLongResponse(int value) { aiLogOnLongResponse = value; }

	/**
	 * Sets the chats timeout days.
	 *
	 * @param days the new chats timeout days
	 */
	public void setChatsTimeoutDays(int days) { this.chatsTimeoutDays = days; }

	/**
	 * Sets the logs timeout days.
	 *
	 * @param days the new logs timeout days
	 */
	public void setLogsTimeoutDays(int days) { this.logsTimeoutDays = days; }

	/**
	 * Sets the max recent files.
	 *
	 * @param n the new max recent files
	 */
	public void setMaxRecentFiles(int n) { this.maxRecentFiles = n; }

	/**
	 * Gets the dlg route item pos.
	 *
	 * @return the dlg route item pos
	 */
	public Point getDlgRouteItemPos() { return dlgRouteItemPos; }

	/**
	 * Sets the dlg route item pos.
	 *
	 * @param point the new dlg route item pos
	 */
	public void setDlgRouteItemPos(Point point) { this.dlgRouteItemPos = point; }

	/**
	 * Gets the dlg route item size.
	 *
	 * @return the dlg route item size
	 */
	public Dimension getDlgRouteItemSize() { return dlgRouteItemSize; }

	/**
	 * Sets the dlg route item size.
	 *
	 * @param dimension the new dlg route item size
	 */
	public void setDlgRouteItemSize(Dimension dimension) { this.dlgRouteItemSize = dimension; }

	/**
	 * Gets the dlg chat bot pos.
	 *
	 * @return the dlg chat bot pos
	 */
	public Point getDlgChatBotPos() { return dlgChatBotPos; }

	/**
	 * Sets the dlg chat bot pos.
	 *
	 * @param point the new dlg chat bot pos
	 */
	public void setDlgChatBotPos(Point point) { this.dlgChatBotPos = point; }

	/**
	 * Gets the dlg chat bot size.
	 *
	 * @return the dlg chat bot size
	 */
	public Dimension getDlgChatBotSize() { return dlgChatBotSize; }

	/**
	 * Sets the dlg chat bot size.
	 *
	 * @param dimension the new dlg chat bot size
	 */
	public void setDlgChatBotSize(Dimension dimension) { this.dlgChatBotSize = dimension; }

	/**
	 * Gets the opened recently.
	 *
	 * @return the opened recently
	 */
	public List<String> getOpenedRecently() { return openedRecently; }

	/**
	 * Sets the opened recently.
	 *
	 * @param openedRecently the new opened recently
	 */
	public void setOpenedRecently(List<String> openedRecently) { this.openedRecently = openedRecently; }

	/**
	 * Adds the opened recently.
	 *
	 * @param path the path
	 */
	public void addOpenedRecently(String path) { if(openedRecently.contains(path)) openedRecently.remove(path); openedRecently.add(0, path); }

	/**
	 * Removes the opened recently.
	 *
	 * @param path the path
	 */
	public void removeOpenedRecently(String path) { openedRecently.remove(path); }

	/**
	 * Gets the main wnd pos.
	 *
	 * @return the main wnd pos
	 */
	public Point getMainWndPos() { return mainWndPos; }

	/**
	 * Sets the main wnd pos.
	 *
	 * @param mainWndPos the new main wnd pos
	 */
	public void setMainWndPos(Point mainWndPos) { this.mainWndPos = mainWndPos; }

	/**
	 * Gets the main wnd size.
	 *
	 * @return the main wnd size
	 */
	public Dimension getMainWndSize() { return mainWndSize; }

	/**
	 * Sets the main wnd size.
	 *
	 * @param mainWndSize the new main wnd size
	 */
	public void setMainWndSize(Dimension mainWndSize) { this.mainWndSize = mainWndSize; }

	/**
	 * Gets the main wnd state.
	 *
	 * @return the main wnd state
	 */
	public int getMainWndState() { return mainWndState; }

	/**
	 * Sets the main wnd state.
	 *
	 * @param state the new main wnd state
	 */
	public void setMainWndState(int state) { this.mainWndState = state; }

	/**
	 * Sets the main wnd split pos.
	 *
	 * @param pos the new main wnd split pos
	 */
	public void setMainWndSplitPos(Point pos) { this.mainWndSplitPos = pos; }

	/**
	 * Gets the main wnd split pos.
	 *
	 * @return the main wnd split pos
	 */
	public Point getMainWndSplitPos() { return mainWndSplitPos; }

	/**
	 * Gets the notify on errors.
	 *
	 * @return the notify on errors
	 */
	public boolean isNotifyOnErrors() { return notifyOnErrors; }

	/**
	 * Sets the notify on errors.
	 *
	 * @param value the new notify on errors
	 */
	public void setNotifyOnErrors(boolean value) { notifyOnErrors = value; }
	/**
	 * Gets the minimize to tray.
	 *
	 * @return the minimize to tray
	 */
	public boolean isMinimizeToTray() { return minimizeToTray; }

	/**
	 * Sets the minimize to tray.
	 *
	 * @param value the new minimize to tray
	 */
	public void setMinimizeToTray(boolean value) { minimizeToTray = value; }
}
