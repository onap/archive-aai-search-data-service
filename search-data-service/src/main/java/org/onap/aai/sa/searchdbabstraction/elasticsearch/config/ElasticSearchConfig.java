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
package org.onap.aai.sa.searchdbabstraction.elasticsearch.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.security.Password;
import org.onap.aai.sa.searchdbabstraction.util.SearchDbConstants;

public class ElasticSearchConfig {

    private String uriScheme;
    private String trustStore;
    private String trustStorePassword;
    private String keyStore;
    private String keyStorePassword;
    private String authUser;
    private String authPassword;
    private String ipAddress;
    private String httpPort;
    private String javaApiPort;
    private String clusterName;
    private String authorizationEnabled;

    public static final String ES_CLUSTER_NAME = "es.cluster-name";
    public static final String ES_IP_ADDRESS = "es.ip-address";
    public static final String ES_HTTP_PORT = "es.http-port";
    public static final String ES_URI_SCHEME = "es.uri-scheme";
    public static final String ES_TRUST_STORE = "es.trust-store";
    public static final String ES_TRUST_STORE_ENC = "es.trust-store-password";
    public static final String ES_KEY_STORE = "es.key-store";
    public static final String ES_KEY_STORE_ENC = "es.key-store-password";
    public static final String ES_AUTH_USER = "es.auth-user";
    public static final String ES_AUTH_ENC = "es.auth-password";
    public static final String ES_AUTH_ENABLED = "es.auth.authorization.enabled";

    private static final String DEFAULT_URI_SCHEME = "http";
    private static final String JAVA_API_PORT_DEFAULT = "9300";
    private String authValue;

    public ElasticSearchConfig(Properties props) {
        setUriScheme(props.getProperty(ES_URI_SCHEME));
        if (getUriScheme().equals("https")) {
            initializeHttpsProperties(props);
        }
        setClusterName(props.getProperty(ES_CLUSTER_NAME));
        setIpAddress(props.getProperty(ES_IP_ADDRESS));
        setHttpPort(props.getProperty(ES_HTTP_PORT));
        setJavaApiPort(JAVA_API_PORT_DEFAULT);
        initializeAuthValues(props);
        setAuthorizationEnabled(props.getProperty(ES_AUTH_ENABLED));
    }


    public String getUriScheme() {
        return this.uriScheme;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }

    public String getJavaApiPort() {
        return javaApiPort;
    }

    public void setJavaApiPort(String javaApiPort) {
        this.javaApiPort = javaApiPort;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyStorePath() {
        return keyStore;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getTrustStorePath() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setAuthUser(String authUser) {
        this.authUser = authUser;
    }

    public String getAuthUser() {
        return authUser;
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    public String getAuthPassword() {
        return authPassword;
    }

    public boolean useAuth() {
        return getAuthUser() != null || getAuthPassword() != null;
    }

    public String getAuthValue() {
        return authValue;
    }

    public String getAuthorizationEnabled() {
        return authorizationEnabled;
    }

    public void setAuthorizationEnabled(String authorizationEnabled) {
        this.authorizationEnabled = authorizationEnabled;
    }

    public boolean useAuthorizationUser() {
        return getAuthorizationEnabled()== null? true : Boolean.parseBoolean(getAuthorizationEnabled());
    }

    @Override
    public String toString() {
        return String.format(
                "%s://%s:%s (cluster=%s) (API port=%s)%nauth=%s%ntrustStore=%s (passwd %s)%nkeyStore=%s (passwd %s)%nauthorizationUser=%s",
                uriScheme, ipAddress, httpPort, clusterName, javaApiPort, useAuth(), trustStore,
                trustStorePassword != null, keyStore, keyStorePassword != null, useAuthorizationUser());
    }

    private void initializeAuthValues(Properties props) {
        setAuthUser(props.getProperty(ES_AUTH_USER));
        Optional<String> passwordValue = Optional.ofNullable(props.getProperty(ES_AUTH_ENC));
        if (passwordValue.isPresent()) {
            setAuthPassword(Password.deobfuscate(passwordValue.get()));
        }
        if (useAuth()) {
            authValue = "Basic " + Base64.getEncoder()
                    .encodeToString((getAuthUser() + ":" + getAuthPassword()).getBytes(StandardCharsets.UTF_8));
        }
    }

    private void initializeHttpsProperties(Properties props) {
        Optional<String> trustStoreFile = Optional.ofNullable(props.getProperty(ES_TRUST_STORE));
        if (trustStoreFile.isPresent()) {
            setTrustStore(SearchDbConstants.SDB_SPECIFIC_CONFIG + trustStoreFile.get());
        }

        Optional<String> passwordValue = Optional.ofNullable(props.getProperty(ES_TRUST_STORE_ENC));
        if (passwordValue.isPresent()) {
          if(passwordValue.get().startsWith("OBF:")){
            setTrustStorePassword(Password.deobfuscate(passwordValue.get()));
          }else if(passwordValue.get().startsWith("ENV:")){
              setTrustStorePassword(System.getenv(StringUtils.removeStart(passwordValue.get(), "ENV:")));
          }
          else{
            setTrustStorePassword(passwordValue.get());
          }
        }

        Optional<String> keyStoreFile = Optional.ofNullable(props.getProperty(ES_KEY_STORE));
        if (keyStoreFile.isPresent()) {
            setKeyStore(SearchDbConstants.SDB_SPECIFIC_CONFIG + keyStoreFile.get());
        }

        passwordValue = Optional.ofNullable(props.getProperty(ES_KEY_STORE_ENC));
        if (passwordValue.isPresent()) {
          if(passwordValue.get().startsWith("OBF:")){
            setKeyStorePassword(Password.deobfuscate(passwordValue.get()));
          }else if(passwordValue.get().startsWith("ENV:")){
            setKeyStorePassword(System.getenv(StringUtils.removeStart(passwordValue.get(), "ENV:")));
           }
          else{
            setKeyStorePassword(passwordValue.get());
          }
        }
    }

    private void setUriScheme(String uriScheme) {
        this.uriScheme = Optional.ofNullable(uriScheme).orElse(DEFAULT_URI_SCHEME);
    }
}
