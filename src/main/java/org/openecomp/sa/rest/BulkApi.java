/**
 * ============LICENSE_START=======================================================
 * Search Data Service
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property.
 * Copyright © 2017 Amdocs
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License ati
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 * ECOMP and OpenECOMP are trademarks
 * and service marks of AT&T Intellectual Property.
 */
package org.openecomp.sa.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.openecomp.cl.api.LogFields;
import org.openecomp.cl.api.LogLine;
import org.openecomp.cl.api.Logger;
import org.openecomp.cl.eelf.LoggerFactory;
import org.openecomp.sa.searchdbabstraction.elasticsearch.dao.DocumentStoreInterface;
import org.openecomp.sa.searchdbabstraction.elasticsearch.exception.DocumentStoreOperationException;
import org.openecomp.sa.searchdbabstraction.entity.OperationResult;
import org.openecomp.sa.searchdbabstraction.logging.SearchDbMsgs;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;


/**
 * This class encapsulates the REST end points associated with performing
 * bulk operations against the document store.
 */
@Path("/bulk")
public class BulkApi {

  /**
   * Indicates whether or not we have performed the one-time static
   * initialization required for performing schema validation.
   */
  protected static AtomicBoolean validationInitialized = new AtomicBoolean(false);

  /**
   * Factory used for importing our payload schema for validation purposes.
   */
  protected static JsonSchemaFactory schemaFactory = null;

  /**
   * Imported payload schema that will be used by our validation methods.
   */
  protected static JsonSchema schema = null;

  protected SearchServiceApi searchService = null;

  // Instantiate the loggers.
  private static Logger logger = LoggerFactory.getInstance().getLogger(BulkApi.class.getName());
  private static Logger auditLogger = LoggerFactory.getInstance()
      .getAuditLogger(BulkApi.class.getName());


  /**
   * Create a new instance of the BulkApi end point.
   */
  public BulkApi(SearchServiceApi searchService) {
    this.searchService = searchService;
  }


  /**
   * Processes client requests containing a set of operations to be
   * performed in bulk.
   *
   * <p>Method: POST
   *
   * @param operations - JSON structure enumerating the operations to be
   *                   performed.
   * @param request    - Raw HTTP request.
   * @param headers    - HTTP headers.
   * @return - A standard REST response structure.
   */
  public Response processPost(String operations,
                              HttpServletRequest request,
                              HttpHeaders headers,
                              DocumentStoreInterface documentStore,
                              ApiUtils apiUtils) {


    // Initialize the MDC Context for logging purposes.
    ApiUtils.initMdcContext(request, headers);

    // Set a default result code and entity string for the request.
    int resultCode = 500;
    String resultString = "Unexpected error";

    if (logger.isDebugEnabled()) {
      logger.debug("SEARCH: Process Bulk Request - operations = ["
          + operations.replaceAll("\n", "") + " ]");
    }

    try {

      // Validate that the request is correctly authenticated before going
      // any further.
      if (!searchService.validateRequest(headers, request,
          ApiUtils.Action.POST, ApiUtils.SEARCH_AUTH_POLICY_NAME)) {
        logger.warn(SearchDbMsgs.BULK_OPERATION_FAILURE, "Authentication failure.");

        return buildResponse(Response.Status.FORBIDDEN.getStatusCode(),
            "Authentication failure.", request, apiUtils);
      }

    } catch (Exception e) {

      // This is a catch all for any unexpected failure trying to perform
      // the authentication.
      logger.warn(SearchDbMsgs.BULK_OPERATION_FAILURE,
          "Unexpected authentication failure - cause: " + e.getMessage());
      if (logger.isDebugEnabled()) {
        logger.debug("Stack Trace:\n" + e.getStackTrace());
      }

      return buildResponse(Response.Status.FORBIDDEN.getStatusCode(),
          "Authentication failure - cause " + e.getMessage(),
          request,
          apiUtils);
    }

    // We expect a payload containing a JSON structure enumerating the
    // operations to be performed.
    if (operations == null) {
      logger.warn(SearchDbMsgs.BULK_OPERATION_FAILURE, "Missing operations list payload");

      return buildResponse(resultCode, "Missing payload", request, apiUtils);
    }


    // Marshal the supplied json string into a Java object.
    ObjectMapper mapper = new ObjectMapper();
    BulkRequest[] requests = null;
    try {
      requests = mapper.readValue(operations, BulkRequest[].class);

    } catch (IOException e) {

      logger.warn(SearchDbMsgs.BULK_OPERATION_FAILURE,
          "Failed to marshal operations list: " + e.getMessage());
      if (logger.isDebugEnabled()) {
        logger.debug("Stack Trace:\n" + e.getStackTrace());
      }

      // Populate the result code and entity string for our HTTP response
      // and return the response to the client..
      return buildResponse(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
          "Unable to marshal operations: " + e.getMessage(),
          request,
          apiUtils);
    }

    // Verify that our parsed operations list actually contains some valid
    // operations.
    if (requests.length == 0) {
      logger.warn(SearchDbMsgs.BULK_OPERATION_FAILURE, "Empty operations list in bulk request");


      // Populate the result code and entity string for our HTTP response
      // and return the response to the client..
      return buildResponse(javax.ws.rs.core.Response.Status.BAD_REQUEST.getStatusCode(),
          "Empty operations list in bulk request",
          request,
          apiUtils);
    }
    try {

      // Now, forward the set of bulk operations to the DAO for processing.
      OperationResult result = documentStore.performBulkOperations(requests);

      // Populate the result code and entity string for our HTTP response.
      resultCode = result.getResultCode();
      resultString = (result.getFailureCause() == null)
          ? result.getResult() : result.getFailureCause();

    } catch (DocumentStoreOperationException e) {

      logger.warn(SearchDbMsgs.BULK_OPERATION_FAILURE,
          "Unexpected failure communicating with document store: " + e.getMessage());
      if (logger.isDebugEnabled()) {
        logger.debug("Stack Trace:\n" + e.getStackTrace());
      }

      // Populate the result code and entity string for our HTTP response.
      resultCode = javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
      resultString = "Unexpected failure processing bulk operations: " + e.getMessage();
    }

    // Build our HTTP response.
    Response response = Response.status(resultCode).entity(resultString).build();

    // Log the result.
    if ((response.getStatus() >= 200) && (response.getStatus() < 300)) {
      logger.info(SearchDbMsgs.PROCESSED_BULK_OPERATIONS);
    } else {
      logger.warn(SearchDbMsgs.BULK_OPERATION_FAILURE, (String) response.getEntity());
    }

    // Finally, return the HTTP response to the client.
    return buildResponse(resultCode, resultString, request, apiUtils);
  }


  /**
   * This method generates an audit log and returns an HTTP response object.
   *
   * @param resultCode   - The result code to report.
   * @param resultString - The result string to report.
   * @param request       - The HTTP request to extract data from for the audit log.
   * @return - An HTTP response object.
   */
  private Response buildResponse(int resultCode, String resultString,
                                 HttpServletRequest request, ApiUtils apiUtils) {

    Response response = Response.status(resultCode).entity(resultString).build();

    // Generate our audit log.
    auditLogger.info(SearchDbMsgs.PROCESS_REST_REQUEST,
        new LogFields()
            .setField(LogLine.DefinedFields.RESPONSE_CODE, resultCode)
            .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION,
                ApiUtils.getHttpStatusString(resultCode)),
        (request != null) ? request.getMethod() : "Unknown",
        (request != null) ? request.getRequestURL().toString() : "Unknown",
        (request != null) ? request.getRemoteHost() : "Unknown",
        Integer.toString(response.getStatus()));

    // Clear the MDC context so that no other transaction inadvertently
    // uses our transaction id.
    ApiUtils.clearMdcContext();

    return response;
  }
}
