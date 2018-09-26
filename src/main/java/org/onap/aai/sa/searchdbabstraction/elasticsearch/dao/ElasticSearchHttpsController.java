/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017-2018 Amdocs
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.sa.searchdbabstraction.elasticsearch.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.config.ElasticSearchConfig;

/**
 * HTTPS (TLS) specific configuration.
 */
public class ElasticSearchHttpsController {

    private static final Logger logger =
            LoggerFactory.getInstance().getLogger(ElasticSearchHttpsController.class.getName());

    private static final String SSL_PROTOCOL = "TLS";
    private static final String KEYSTORE_ALGORITHM = "SunX509";
    private static final String KEYSTORE_TYPE = "PKCS12";

    public ElasticSearchHttpsController(ElasticSearchConfig config) throws NoSuchAlgorithmException, KeyStoreException,
            CertificateException, IOException, KeyManagementException, UnrecoverableKeyException {
        logger.debug("Initialising HTTPS configuration");

        SSLContext ctx = SSLContext.getInstance(SSL_PROTOCOL);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KEYSTORE_ALGORITHM);
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);

        String clientCertPassword = config.getKeyStorePassword();

        char[] pwd = null;
        if (clientCertPassword != null) {
            pwd = clientCertPassword.toCharArray();
        } else {
            logger.debug("No key store password is defined");
        }

        TrustManager[] trustManagers = getTrustManagers(config);
        KeyManager[] keyManagers = null;

        String clientCertFileName = config.getKeyStorePath();
        if (clientCertFileName != null) {
            InputStream fin = Files.newInputStream(Paths.get(clientCertFileName));
            keyStore.load(fin, pwd);
            kmf.init(keyStore, pwd);
            keyManagers = kmf.getKeyManagers();
        }

        ctx.init(keyManagers, trustManagers, null);
        logger.debug("Initialised SSL context");

        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((host, session) -> host.equalsIgnoreCase(session.getPeerHost()));
    }

    private TrustManager[] getTrustManagers(ElasticSearchConfig config)
            throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        // Using null here initializes the TMF with the default trust store.
        tmf.init((KeyStore) null);

        // Find the default trust manager.
        final X509TrustManager defaultTrustManager = findX509TrustManager(tmf);

        String trustStoreFile = config.getTrustStorePath();
        if (trustStoreFile == null) {
            logger.debug("No trust store defined");
            return new TrustManager[] {defaultTrustManager};
        }

        // Create a new Trust Manager from the local trust store.
        try (InputStream myKeys = Files.newInputStream(Paths.get(trustStoreFile))) {
            KeyStore myTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] pwdArray = null;
            if (config.getTrustStorePassword() != null) {
                pwdArray = config.getTrustStorePassword().toCharArray();
            }
            myTrustStore.load(myKeys, pwdArray);
            tmf.init(myTrustStore);
        }

        // Create a custom trust manager that wraps both our trust store and the default.
        final X509TrustManager finalLocalTm = findX509TrustManager(tmf);

        return new TrustManager[] {new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return defaultTrustManager.getAcceptedIssuers();
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                try {
                    finalLocalTm.checkServerTrusted(chain, authType);
                } catch (CertificateException e) {
                    defaultTrustManager.checkServerTrusted(chain, authType);
                }
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                defaultTrustManager.checkClientTrusted(chain, authType);
            }
        }};
    }

    private X509TrustManager findX509TrustManager(TrustManagerFactory tmf) {
        return (X509TrustManager) Arrays.asList(tmf.getTrustManagers()).stream()
                .filter(tm -> tm instanceof X509TrustManager).findFirst().orElse(null);
    }
}
