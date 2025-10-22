/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4usagent.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The Messages class that manages loading localized messages from properties files;
 *
 * @author El Mhadder Mohamed Rida
 */
public class Messages {
	private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages";

	private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * Init the class
	 */
	private Messages() { }

	/**
	 * Gets the message from the properties file
	 * @param key The message key
	 * @return The message from the properties file
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Changes the locale
	 * @param locale The new locale
	 */
	public static void changeLocale(Locale locale) {
		RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, locale);
	}
}
