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
package org.onap.aai.sa.rest;

import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.ElasticSearchHttpController;
import org.onap.aai.sa.searchdbabstraction.logging.SearchDbMsgs;
import org.openecomp.cl.api.LogFields;
import org.openecomp.cl.api.LogLine;
import org.openecomp.cl.api.Logger;
import org.openecomp.cl.eelf.LoggerFactory;
import org.onap.aai.sa.rest.AnalyzerSchema;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

@Path("/analyzers")
public class AnalyzerApi {

  private SearchServiceApi searchService = null;

  // Set up the loggers.
  private static Logger logger = LoggerFactory.getInstance().getLogger(IndexApi.class.getName());
  private static Logger auditLogger = LoggerFactory.getInstance()
      .getAuditLogger(IndexApi.class.getName());

  public AnalyzerApi(SearchServiceApi searchService) {
    this.searchService = searchService;
  }

  @GET
  public Response processGet(@Context HttpServletRequest request,
                             @Context HttpHeaders headers,
                             ApiUtils apiUtils) {

    Response.Status responseCode = Response.Status.INTERNAL_SERVER_ERROR;
    String responseString = "Undefined error";

    // Initialize the MDC Context for logging purposes.
    ApiUtils.initMdcContext(request, headers);

    // Validate that the request is correctly authenticated before going
    // any further.
    try {

      if (!searchService.validateRequest(headers, request,
          ApiUtils.Action.GET, ApiUtils.SEARCH_AUTH_POLICY_NAME)) {
        logger.warn(SearchDbMsgs.GET_ANALYZERS_FAILURE, "Authentication failure.");
        return Response.status(Response.Status.FORBIDDEN).entity("Authentication failure.").build();
      }

    } catch (Exception e) {

      logger.warn(SearchDbMsgs.GET_ANALYZERS_FAILURE,
          "Unexpected authentication failure - cause: " + e.getMessage());
      return Response.status(Response.Status.FORBIDDEN).entity("Authentication failure.").build();
    }


    // Now, build the list of analyzers.
    try {
      responseString = buildAnalyzerList(ElasticSearchHttpController.getInstance()
          .getAnalysisConfig());
      responseCode = Response.Status.OK;

    } catch (Exception e) {

      logger.warn(SearchDbMsgs.GET_ANALYZERS_FAILURE,
          "Unexpected failure retrieving analysis configuration - cause: " + e.getMessage());
      responseString = "Failed to retrieve analysis configuration.  Cause: " + e.getMessage();
    }

    // Build the HTTP response.
    Response response = Response.status(responseCode).entity(responseString).build();

    // Generate our audit log.
    auditLogger.info(SearchDbMsgs.PROCESS_REST_REQUEST,
        new LogFields()
            .setField(LogLine.DefinedFields.RESPONSE_CODE, responseCode.getStatusCode())
            .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, responseCode.getStatusCode()),
        (request != null) ? request.getMethod() : "Unknown",
        (request != null) ? request.getRequestURL().toString() : "Unknown",
        (request != null) ? request.getRemoteHost() : "Unknown",
        Integer.toString(response.getStatus()));

    // Clear the MDC context so that no other transaction inadvertently
    // uses our transaction id.
    ApiUtils.clearMdcContext();

    return response;
  }


  /**
   * This method takes a list of analyzer objects and generates a simple json
   * structure to enumerate them.
   *
   * <p>Note, this includes only the aspects of the analyzer object that we want
   * to make public to an external client.
   *
   * @param analysisConfig - The analysis configuration object to extract the
   *                       analyzers from.
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
