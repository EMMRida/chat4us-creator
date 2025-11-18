/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.emmrida.chat4us.gui.MainWindow;

/**
 * Helper functions for JavetScript scripts. The public static functions here are available to use by the
 * chat bots scripts in RIA files.
 *
 * @author El Mhadder Mohamed Rida
 */
public class ScriptEx {
	private static final int CONNECT_TIMEOUT = 5000;
	private static final int READ_TIMEOUT = 10000;

	//private static final String CHATBOTS_DIR = "./routes";

	/**
	 * Write content to a file
	 * @param filePath The file path
	 * @param content The content
	 * @param append True to append the content, false to overwrite the file
	 * @return True if the content was appended successfully, false otherwise
	 */
	public static boolean writeContentToFile(String filePath, String content, boolean append) {
		File file = new File(filePath);
		File base = new File(MainWindow.CHATBOTS_ROOT_FOLDER);
		if(Helper.getRelativePath(file, base).startsWith("..")) { //$NON-NLS-1$
			Helper.logWarning(Messages.getString("ScriptEx.SE_ACTF_WRONG_PATH") + filePath); //$NON-NLS-1$
			return false;
		}
		try(BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(filePath, append))) {
			writer.append(content);
		} catch (Exception ex) {
			Helper.logWarning(ex, Messages.getString("ScriptEx.SE_ACTF_EXCEPTION") + ex.getMessage()); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	/**
	 * Read content from a file
	 * @param filePath The file path
	 * @return The content
	 */
	public static String readContentFromFile(String filePath) {
		File file = new File(filePath);
		File base = new File(MainWindow.CHATBOTS_ROOT_FOLDER);
		if(Helper.getRelativePath(file, base).startsWith("..")) { //$NON-NLS-1$
			Helper.logWarning(Messages.getString("ScriptEx.SE_RCFF_WRONG_PATH") + filePath); //$NON-NLS-1$
			return null;
		}
		StringBuilder content = new StringBuilder();
		try(BufferedReader reader = new BufferedReader(new java.io.FileReader(filePath))) {
			String line;
			while((line = reader.readLine()) != null) {
				content.append(line);
			}
		} catch (Exception ex) {
			Helper.logWarning(ex, Messages.getString("ScriptEx.SE_RCFF_EXCEPTION") + ex.getMessage()); //$NON-NLS-1$
			return null;
		}
		return content.toString();
	}

	/**
	 * Load content from a CSV file by keyword.
	 * @param filePath The file path of the CSV file
	 * @param keyword The keyword to search for.
	 * @param column The column index to search in
	 * @param separator The separator character
	 * @param limit The limit of the number of lines to read
	 * @return The content of the CSV file by keyword, empty string if not found or null if an error occurs.
	 */
	public static String loadCSVContent(String filePath, String keyword, int column, String separator, int limit) {
		File file = new File(filePath);
		File base = new File(MainWindow.CHATBOTS_ROOT_FOLDER);
		if(Helper.getRelativePath(file, base).startsWith("..")) { //$NON-NLS-1$
			Helper.logWarning(Messages.getString("ScriptEx.SE_LCSVC_WRONG_PATH") + filePath); //$NON-NLS-1$
			return null;
		}
		StringBuilder content = new StringBuilder();
		try(BufferedReader reader = new BufferedReader(new java.io.FileReader(filePath))) {
			String line;
			String[] values;
			int count = 0;
			while((line = reader.readLine()) != null && count < limit) {
				values = line.split(separator);
				if (values[column].equals(keyword)) {
					content.append(line);
					count++;
				}
			}
		} catch (Exception ex) {
			Helper.logWarning(ex, Messages.getString("ScriptEx.SE_LCSVC_EXCEPTION") + ex.getMessage()); //$NON-NLS-1$
			return null;
		}
		return content.toString();
	}

	/**
	 * Load content from a remote URL using a GET request
	 * @param url The URL
	 * @return The content
	 */
	public static String loadRemoteContentGET(String url) {
        StringBuilder response = new StringBuilder();
        try {
            URL requestUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("GET"); //$NON-NLS-1$
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            } else {
                return "HTTPError: " + responseCode; //$NON-NLS-1$
            }
        } catch (Exception ex) {
        	Helper.logWarning(ex, Messages.getString("ScriptEx.SE_LRCGET_EXCEPTION") + ex.getMessage()); //$NON-NLS-1$
            return "Exception: " + ex.getMessage(); //$NON-NLS-1$
        }
        return response.toString();
	}

	/**
	 * Load content from a remote URL using a POST request
	 * @param url The URL
	 * @param params The parameters
	 * @return The content
	 */
    public static String loadRemoteContentPOST(String url, Map<String, String> params) {
        StringBuilder response = new StringBuilder();
        try {
            URL requestUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("POST"); //$NON-NLS-1$
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); //$NON-NLS-1$ //$NON-NLS-2$
            connection.setDoOutput(true); // Enable sending data
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            // Convert parameters map to URL-encoded string
            String postData = getParamsString(params);

            // Send request body
            try (OutputStream os = connection.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) { //$NON-NLS-1$
                writer.write(postData);
                writer.flush();
            }

            // Read response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
            } else {
            	return "HTTPError: " + responseCode; //$NON-NLS-1$
            }
        } catch (Exception ex) {
        	Helper.logWarning(ex, Messages.getString("ScriptEx.SE_LRCPOST_EXCEPTION") + ex.getMessage()); //$NON-NLS-1$
            return "Exception: " + ex.getMessage(); //$NON-NLS-1$
        }
        return response.toString();
    }

    /**
     *  Helper method to convert Map to URL-encoded form data
     * @param params The parameters
     * @return The URL-encoded form data
     * @throws UnsupportedEncodingException
     */
    private static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (result.length() > 0) {
                result.append("&"); //$NON-NLS-1$
            }
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8")); //$NON-NLS-1$
            result.append("="); //$NON-NLS-1$
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8")); //$NON-NLS-1$
        }
        return result.toString();
    }

    /**
     * Execute a SQL query
     * @param dbFile The database file
     * @param query The SQL query
     * @param args The query parameters
     * @return The query results
     */
    public static Object[][] sqlQuery(String dbFile, String query, Object args) {
        List<Object[]> results = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile); //$NON-NLS-1$
             PreparedStatement stmt = conn.prepareStatement(query)) {
            setParameters(stmt, ((List<Object>)args).toArray(new Object[((List<Object>)args).size()]));
            try (ResultSet rs = stmt.executeQuery()) {
                int columnCount = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        row[i] = getTypedValue(rs, i + 1);
                    }
                    results.add(row);
                }
            }
        } catch (SQLException ex) {
            Helper.logWarning(ex, Messages.getString("ScriptEx.SE_SQUERY_EXCEPTION") + ex.getMessage()); //$NON-NLS-1$
            return new Object[][]{{"Error", ex.getMessage()}}; //$NON-NLS-1$
        }
        return results.toArray(new Object[0][]);
    }

    /**
     * Execute a SQL update
     * @param dbFile The database file
     * @param query The SQL query
     * @param args The query parameters
     * @return The number of affected rows
     */
    public static int sqlUpdate(String dbFile, String query, Object args) {
        int affectedRows = 0;
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile); //$NON-NLS-1$
             PreparedStatement stmt = conn.prepareStatement(query)) {
        	setParameters(stmt, ((List<Object>)args).toArray(new Object[((List<Object>)args).size()]));
            affectedRows = stmt.executeUpdate();
        } catch (SQLException ex) {
			Helper.logWarning(ex, Messages.getString("ScriptEx.SE_SUPDATE_EXCEPTION") + ex.getMessage()); //$NON-NLS-1$
        }
        return affectedRows;
    }

    /**
     * Execute a SQL insert
     * @param dbFile The database file
     * @param query The SQL query
     * @param args The query parameters
     * @return The generated key, 0 on error.
     */
    public static int sqlInsert(String dbFile, String query, Object args) {
        int gKey = 0;
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile); //$NON-NLS-1$
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
        	setParameters(stmt, ((List<Object>)args).toArray(new Object[((List<Object>)args).size()]));
            if(stmt.executeUpdate() > 0) {
	            ResultSet gKeys = stmt.getGeneratedKeys();
	            if(gKeys.next())
	                gKey = gKeys.getInt(1);
            }
        } catch (SQLException ex) {
			Helper.logWarning(ex, Messages.getString("ScriptEx.SE_SINSERT_EXCEPTION") + ex.getMessage()); //$NON-NLS-1$
        }
        return gKey;
    }

    /**
     * Set query parameters
     * @param stmt The statement
     * @param args The parameters
     * @throws SQLException
     */
    private static  void setParameters(PreparedStatement stmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
        	if(args[i] instanceof String) {
        		stmt.setString(i + 1, (String) args[i]);
        	} else if (args[i] instanceof Integer) {
                stmt.setInt(i + 1, (Integer) args[i]);
            } else if (args[i] instanceof Long) {
                stmt.setLong(i + 1, (Long) args[i]);
            } else if (args[i] instanceof Double) {
                stmt.setDouble(i + 1, (Double) args[i]);
            } else if (args[i] instanceof Float) {
                stmt.setFloat(i + 1, (Float) args[i]);
            } else if (args[i] instanceof Boolean) {
                stmt.setBoolean(i + 1, (Boolean) args[i]);
            } else if (args[i] instanceof byte[]) {
                stmt.setBytes(i + 1, (byte[]) args[i]);
            } else if (args[i] instanceof java.sql.Date) {
                stmt.setDate(i + 1, (java.sql.Date) args[i]);
            } else if (args[i] instanceof java.sql.Timestamp) {
                stmt.setTimestamp(i + 1, (java.sql.Timestamp) args[i]);
            } else {
                stmt.setObject(i + 1, args[i]); // Default case
            }
        }
    }

    /**
     * Get a typed value from a ResultSet
     * @param rs The ResultSet
     * @param columnIndex The column index
     * @return The value
     * @throws SQLException
     */
    private static Object getTypedValue(ResultSet rs, int columnIndex) throws SQLException {
        Object value = rs.getObject(columnIndex);
        if(value instanceof String) {
	        return rs.getString(columnIndex);
        } else if (value instanceof Integer) {
            return rs.getInt(columnIndex);
        } else if (value instanceof Long) {
            return rs.getLong(columnIndex);
        } else if (value instanceof Double) {
            return rs.getDouble(columnIndex);
        } else if (value instanceof Float) {
            return rs.getFloat(columnIndex);
        } else if (value instanceof Boolean) {
            return rs.getBoolean(columnIndex);
        } else if (value instanceof byte[]) {
            return rs.getBytes(columnIndex);
        } else if (value instanceof java.sql.Date) {
            return rs.getDate(columnIndex);
        } else if (value instanceof java.sql.Timestamp) {
            return rs.getTimestamp(columnIndex);
        } else {
            return value; // Default case
        }
    }
}
