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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class ElasticSearchConfigTest {

    ElasticSearchConfig elasticSearchConfig;

    @Before
    public void setUp(){
        Properties prop = new Properties();
        prop.put("es.cluster-name", "cluster-1");
        prop.put("es.ip-address", "127.0.0.1");
        prop.put("es.http-port", "9001");
        elasticSearchConfig = new ElasticSearchConfig(prop);
    }

    @Test
    public void testAllGetMethods(){
        Assert.assertEquals(elasticSearchConfig.getClusterName(), "cluster-1");
        Assert.assertEquals(elasticSearchConfig.getIpAddress(), "127.0.0.1");
        Assert.assertEquals(elasticSearchConfig.getHttpPort(), "9001");
        Assert.assertEquals(elasticSearchConfig.getJavaApiPort(), "9300");
        Assert.assertNotNull(elasticSearchConfig.toString());
    }
}
