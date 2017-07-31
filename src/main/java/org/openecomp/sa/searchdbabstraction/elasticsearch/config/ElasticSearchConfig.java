/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017 Amdocs
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.openecomp.sa.searchdbabstraction.elasticsearch.config;

import java.util.Properties;

public class ElasticSearchConfig {
  private String ipAddress;
  private String httpPort;
  private String javaApiPort;
  private String clusterName;

  public static final String ES_CLUSTER_NAME = "es.cluster-name";
  public static final String ES_IP_ADDRESS = "es.ip-address";
  public static final String ES_HTTP_PORT = "es.http-port";

  private static final String JAVA_API_PORT_DEFAULT = "9300";

  public ElasticSearchConfig(Properties props) {

    setClusterName(props.getProperty(ES_CLUSTER_NAME));
    setIpAddress(props.getProperty(ES_IP_ADDRESS));
    setHttpPort(props.getProperty(ES_HTTP_PORT));
    setJavaApiPort(JAVA_API_PORT_DEFAULT);
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

  @Override
  public String toString() {
    return "ElasticSearchConfig [ipAddress=" + ipAddress + ", httpPort=" + httpPort
        + ", javaApiPort=" + javaApiPort + ", clusterName=" + clusterName + "]";
  }

}