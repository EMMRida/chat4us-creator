/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.util;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

/**
 * The NoopTrustManager. Used to disable SSL certificate check.
 *
 * @author El Mhadder Mohamed Rida
 */
public class NoopTrustManager {
	/**
	 * Gets a SSL context that ignores SSL certificate check.
	 * @return
	 */
    public SSLContext getNoopSSLContext() {
        try {
            TrustManager[] trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS"); //$NON-NLS-1$
            sslContext.init(null, trustManagers, new java.security.SecureRandom());
            return sslContext;
        } catch (Exception ex) {
            throw new RuntimeException(Messages.getString("NoopTrustManager.SSL_CONTEXT_FAILURE"), ex); //$NON-NLS-1$
        }
    }
}