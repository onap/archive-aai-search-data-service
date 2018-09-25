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
package org.onap.aai.sa.rest;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.http.HttpServletRequest;
import org.onap.aai.cl.api.LogFields;
import org.onap.aai.cl.api.LogLine;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.ElasticSearchHttpController;
import org.onap.aai.sa.searchdbabstraction.logging.SearchDbMsgs;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Component
@EnableWebSecurity
@RestController
@RequestMapping("/services/search-data-service/v1/analyzers/search")
public class AnalyzerApi {

    private SearchServiceApi searchService = null;

    // Set up the loggers.
    private static Logger logger = LoggerFactory.getInstance().getLogger(IndexApi.class.getName());
    private static Logger auditLogger = LoggerFactory.getInstance().getAuditLogger(IndexApi.class.getName());

    private static final String MSG_AUTHENTICATION_FAILURE = "Authentication failure.";

    public AnalyzerApi(@Qualifier("searchServiceApi") SearchServiceApi searchService) {
        this.searchService = searchService;
    }

    @RequestMapping(method = RequestMethod.GET, consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<String> processGet(HttpServletRequest request, @RequestHeader HttpHeaders headers,
            ApiUtils apiUtils) {

        HttpStatus responseCode = HttpStatus.INTERNAL_SERVER_ERROR;
        String responseString;

        // Initialize the MDC Context for logging purposes.
        ApiUtils.initMdcContext(request, headers);

        // Validate that the request is correctly authenticated before going
        // any further.
        try {

            if (!searchService.validateRequest(headers, request, ApiUtils.Action.GET,
                    ApiUtils.SEARCH_AUTH_POLICY_NAME)) {
                logger.warn(SearchDbMsgs.GET_ANALYZERS_FAILURE, MSG_AUTHENTICATION_FAILURE);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                        .body(MSG_AUTHENTICATION_FAILURE);
            }

        } catch (Exception e) {

            logger.warn(SearchDbMsgs.GET_ANALYZERS_FAILURE,
                    "Unexpected authentication failure - cause: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                    .body(MSG_AUTHENTICATION_FAILURE);
        }


        // Now, build the list of analyzers.
        try {
            responseString = buildAnalyzerList(ElasticSearchHttpController.getInstance().getAnalysisConfig());
            responseCode = HttpStatus.OK;
        } catch (Exception e) {
            logger.warn(SearchDbMsgs.GET_ANALYZERS_FAILURE,
                    "Unexpected failure retrieving analysis configuration - cause: " + e.getMessage());
            responseString = "Failed to retrieve analysis configuration.  Cause: " + e.getMessage();
        }

        // Build the HTTP response.
        ResponseEntity<String> response =
                ResponseEntity.status(responseCode).contentType(MediaType.APPLICATION_JSON).body(responseString);

        // Generate our audit log.
        String unknownLogField = "Unknown";
        auditLogger.info(SearchDbMsgs.PROCESS_REST_REQUEST,
                new LogFields().setField(LogLine.DefinedFields.RESPONSE_CODE, responseCode.value())
                        .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, responseCode.value()),
                (request != null) ? request.getMethod() : unknownLogField,
                (request != null) ? request.getRequestURL().toString() : unknownLogField,
                (request != null) ? request.getRemoteHost() : unknownLogField,
                Integer.toString(response.getStatusCodeValue()));

        // Clear the MDC context so that no other transaction inadvertently
        // uses our transaction id.
        ApiUtils.clearMdcContext();

        return response;
    }

    /**
     * This method takes a list of analyzer objects and generates a simple json structure to enumerate them.
     *
     * <p>
     * Note, this includes only the aspects of the analyzer object that we want to make public to an external client.
     *
     * @param analysisConfig - The analysis configuration object to extract the analyzers from.
     * @return - A json string enumerating the defined analyzers.
     */
    private String buildAnalyzerList(AnalysisConfiguration analysisConfig) {

        StringBuilder sb = new StringBuilder();

        sb.append("{");
        AtomicBoolean firstAnalyzer = new AtomicBoolean(true);
        for (AnalyzerSchema analyzer : analysisConfig.getAnalyzers()) {

            if (!firstAnalyzer.compareAndSet(true, false)) {
                sb.append(", ");
            }

            sb.append("{");
            sb.append("\"name\": \"").append(analyzer.getName()).append("\", ");
            sb.append("\"description\": \"").append(analyzer.getDescription()).append("\", ");
            sb.append("\"behaviours\": [");
            AtomicBoolean firstBehaviour = new AtomicBoolean(true);
            for (String behaviour : analyzer.getBehaviours()) {
                if (!firstBehaviour.compareAndSet(true, false)) {
                    sb.append(", ");
                }
                sb.append("\"").append(behaviour).append("\"");
            }
            sb.append("]");
            sb.append("}");
        }
        sb.append("}");

        return sb.toString();
    }
}
