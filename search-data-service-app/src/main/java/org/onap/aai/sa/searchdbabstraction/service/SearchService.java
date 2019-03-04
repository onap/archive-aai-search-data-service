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
package org.onap.aai.sa.searchdbabstraction.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.config.ElasticSearchConfig;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.ElasticSearchHttpController;
import org.onap.aai.sa.searchdbabstraction.logging.SearchDbMsgs;
import org.onap.aai.sa.searchdbabstraction.util.SearchDbConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class SearchService {
    static Logger logger = LoggerFactory.getInstance().getLogger(SearchService.class.getName());

    @Autowired
    private ElasticSearchConfig esConfig;

    public SearchService() {
        try {
            start();
        } catch (Exception e) {
            logger.error(SearchDbMsgs.STARTUP_EXCEPTION, e.getLocalizedMessage());
        }
    }

    protected void start() throws IOException {
        Properties configProperties = new Properties();
        configProperties.load(new FileInputStream(SearchDbConstants.ES_CONFIG_FILE));
        new ElasticSearchHttpController(esConfig);
        logger.info(SearchDbMsgs.SERVICE_STARTED);
    }
}
