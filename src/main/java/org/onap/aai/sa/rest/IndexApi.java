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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.DocumentStoreInterface;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.exception.DocumentStoreOperationException;
import org.onap.aai.sa.searchdbabstraction.entity.OperationResult;
import org.onap.aai.sa.searchdbabstraction.logging.SearchDbMsgs;
import org.onap.aai.cl.api.LogFields;
import org.onap.aai.cl.api.LogLine;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.sa.rest.DocumentFieldSchema;
import org.onap.aai.sa.rest.DocumentSchema;
import org.slf4j.MDC;

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

// Spring Imports
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
// import org.springframework.http.server.HttpServletRequest;


/**
 * This class encapsulates the REST end points associated with manipulating
 * indexes in the document store.
 */
public class IndexApi {


  private static final String HEADER_VALIDATION_SUCCESS = "SUCCESS";
  protected SearchServiceApi searchService = null;

  /**
   * Configuration for the custom analyzers that will be used for indexing.
   */
  protected AnalysisConfiguration analysisConfig;

  // Set up the loggers.
  private static Logger logger = LoggerFactory.getInstance()
    .getLogger(IndexApi.class.getName());
  private static Logger auditLogger = LoggerFactory.getInstance()
    .getAuditLogger(IndexApi.class.getName());


  public IndexApi(SearchServiceApi searchService) {
    this.searchService = searchService;
    init();
  }


  /**
   * Initializes the end point.
   *
   * @throws FileNotFoundException
   * @throws IOException
   * @throws DocumentStoreOperationException
   */
  public void init() {

    // Instantiate our analysis configuration object.
    analysisConfig = new AnalysisConfiguration();
  }


  /**
   * Processes client requests to create a new index and document type in the
   * document store.
   *
   * @param documentSchema - The contents of the request body which is expected
   *                       to be a JSON structure which corresponds to the
   *                       schema defined in document.schema.json
   * @param index          - The name of the index to create.
   * @return - A Standard REST response
   */
  public ResponseEntity<String> processCreateIndex (String documentSchema,
                                                    HttpServletRequest request,
                                                    HttpHeaders headers,
                                                    String index,
                                                    DocumentStoreInterface documentStore) {

    int resultCode = 500;
    String resultString = "Unexpected error";
   
    // Initialize the MDC Context for logging purposes.
    ApiUtils.initMdcContext(request, headers);

    // Validate that the request is correctly authenticated before going
    // any further.
    try {

      if (!searchService.validateRequest(headers, request,
                                         ApiUtils.Action.POST, ApiUtils.SEARCH_AUTH_POLICY_NAME)) {
        logger.warn(SearchDbMsgs.INDEX_CREATE_FAILURE, index, "Authentication failure.");
        return errorResponse(HttpStatus.FORBIDDEN, "Authentication failure.", request);
      }

    } catch (Exception e) {

      logger.warn(SearchDbMsgs.INDEX_CREATE_FAILURE, index,
                  "Unexpected authentication failure - cause: " + e.getMessage());
      return errorResponse(HttpStatus.FORBIDDEN, "Authentication failure.", request);
    }


    // We expect a payload containing the document schema.  Make sure
    // it is present.
    if (documentSchema == null) {
      logger.warn(SearchDbMsgs.INDEX_CREATE_FAILURE, index, "Missing document schema payload");
      return errorResponse(HttpStatus.valueOf(resultCode), "Missing payload", request);
    }

    try {

      // Marshal the supplied json string into a document schema object.
      ObjectMapper mapper = new ObjectMapper();
      DocumentSchema schema = mapper.readValue(documentSchema, DocumentSchema.class);

      // Now, ask the DAO to create the index.
      OperationResult result = documentStore.createIndex(index, schema);

      // Extract the result code and string from the OperationResult
      // object so that we can use them to generate a standard REST
      // response.
      // Note that we want to return a 201 result code on a successful
      // create, so if we get back a 200 from the document store,
      // translate that int a 201.
      resultCode = (result.getResultCode() == 200) ? 201 : result.getResultCode();
      resultString = (result.getFailureCause() == null)
        ? result.getResult() : result.getFailureCause();

    } catch (com.fasterxml.jackson.core.JsonParseException
             | com.fasterxml.jackson.databind.JsonMappingException e) {

      // We were unable to marshal the supplied json string into a valid
      // document schema, so return an appropriate error response.
      resultCode = HttpStatus.BAD_REQUEST.value();
      resultString = "Malformed schema: " + e.getMessage();

    } catch (IOException e) {

      // We'll treat this is a general internal error.
      resultCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
      resultString = "IO Failure: " + e.getMessage();
    }

    ResponseEntity<String> response = ResponseEntity.status(resultCode).contentType ( MediaType.APPLICATION_JSON ).body(resultString);


    // Log the result.
    if ((response.getStatusCodeValue() >= 200) && (response.getStatusCodeValue() < 300)) {
      logger.info(SearchDbMsgs.CREATED_INDEX, index);
    } else {
      logger.warn(SearchDbMsgs.INDEX_CREATE_FAILURE, index, resultString);
    }

    // Generate our audit log.
    auditLogger.info(SearchDbMsgs.PROCESS_REST_REQUEST,
                     new LogFields()
                     .setField(LogLine.DefinedFields.RESPONSE_CODE, resultCode)
                     .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION,
                               HttpStatus.valueOf(resultCode).toString()),
                     (request != null) ? request.getMethod().toString () : "Unknown",
                     (request != null) ? request.getRequestURL ().toString () : "Unknown",
                     (request != null) ? request.getRemoteHost () : "Unknown",
                     Integer.toString(response.getStatusCodeValue ()));



    // Clear the MDC context so that no other transaction inadvertently
    // uses our transaction id.
    ApiUtils.clearMdcContext();

    // Finally, return the response.
    return response;
  }

  /**
   * This function accepts any JSON and will "blindly" write it to the
   * document store.
   *
   * Note, eventually this "dynamic" flow should follow the same JSON-Schema
   * validation procedure as the normal create index flow.
   *
   * @param dynamicSchema - The JSON string that will be sent to the document store.
   * @param index - The name of the index to be created.
   * @param documentStore - The document store specific interface.
   * @return The result of the document store interface's operation.
   */
  public ResponseEntity<String> processCreateDynamicIndex(String dynamicSchema, HttpServletRequest request,
                                            HttpHeaders headers, String index, DocumentStoreInterface documentStore) {

    ResponseEntity<String> response = null;

    ResponseEntity<String> validationResponse = validateRequest(request, headers, index, SearchDbMsgs.INDEX_CREATE_FAILURE);


    if (validationResponse.getStatusCodeValue () != HttpStatus.OK.value ()) {
      response = validationResponse;
    } else {
      OperationResult result = documentStore.createDynamicIndex(index, dynamicSchema);

      int resultCode = (result.getResultCode() == 200) ? 201 : result.getResultCode();
      String resultString = (result.getFailureCause() == null) ? result.getResult() : result.getFailureCause();

      response = ResponseEntity.status(resultCode).body(resultString);
    }

    return response;
  }

  /**
   * Processes a client request to remove an index from the document store.
   * Note that this implicitly deletes all documents contained within that index.
   *
   * @param index - The index to be deleted.
   * @return - A standard REST response.
   */
  public ResponseEntity<String> processDelete(String index,
                                              HttpServletRequest request,
                                              HttpHeaders headers,
                                              DocumentStoreInterface documentStore) {

    // Initialize the MDC Context for logging purposes.
    ApiUtils.initMdcContext(request, headers);

    // Set a default response in case something unexpected goes wrong.
    ResponseEntity<String> response = ResponseEntity.status ( HttpStatus.INTERNAL_SERVER_ERROR ).body ( "Unknown" );

    // Validate that the request is correctly authenticated before going
    // any further.
    try {

      if (!searchService.validateRequest(headers, request, ApiUtils.Action.POST,
                                         ApiUtils.SEARCH_AUTH_POLICY_NAME)) {
        logger.warn(SearchDbMsgs.INDEX_CREATE_FAILURE, index, "Authentication failure.");
        return errorResponse(HttpStatus.FORBIDDEN, "Authentication failure.", request);
      }

    } catch (Exception e) {

      logger.warn(SearchDbMsgs.INDEX_CREATE_FAILURE, index,
                  "Unexpected authentication failure - cause: " + e.getMessage());
      return errorResponse(HttpStatus.FORBIDDEN, "Authentication failure.", request);
    }


    try {
      // Send the request to the document store.
      response = responseFromOperationResult(documentStore.deleteIndex(index));

    } catch (DocumentStoreOperationException e) {
      response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType ( MediaType.APPLICATION_JSON ).body(e.getMessage());
    }

    // Log the result.
    if ((response.getStatusCodeValue() >= 200) && (response.getStatusCodeValue() < 300)) {
      logger.info(SearchDbMsgs.DELETED_INDEX, index);
    } else {
      logger.warn(SearchDbMsgs.INDEX_DELETE_FAILURE, index, (String) response.getBody ());
    }

    // Generate our audit log.
    auditLogger.info(SearchDbMsgs.PROCESS_REST_REQUEST,
                     new LogFields()
                     .setField(LogLine.DefinedFields.RESPONSE_CODE, response.getStatusCodeValue())
                     .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION,
                               response.getStatusCode ().getReasonPhrase()),
                     (request != null) ? request.getMethod().toString () : "Unknown",
                     (request != null) ? request.getRequestURL ().toString () : "Unknown",
                     (request != null) ? request.getRemoteHost () : "Unknown",
                     Integer.toString(response.getStatusCodeValue()));

    // Clear the MDC context so that no other transaction inadvertently
    // uses our transaction id.
    ApiUtils.clearMdcContext();

    return response;
  }


  /**
   * This method takes a JSON format document schema and produces a set of
   * field mappings in the form that Elastic Search expects.
   *
   * @param documentSchema - A document schema expressed as a JSON string.
   * @return - A JSON string expressing an Elastic Search mapping configuration.
   * @throws com.fasterxml.jackson.core.JsonParseException
   * @throws com.fasterxml.jackson.databind.JsonMappingException
   * @throws IOException
   */
  public String generateDocumentMappings(String documentSchema)
    throws com.fasterxml.jackson.core.JsonParseException,
           com.fasterxml.jackson.databind.JsonMappingException, IOException {

    // Unmarshal the json content into a document schema object.
    ObjectMapper mapper = new ObjectMapper();
    DocumentSchema schema = mapper.readValue(documentSchema, DocumentSchema.class);

    // Now, generate the Elastic Search mapping json and return it.
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\"properties\": {");

    boolean first = true;
    for (DocumentFieldSchema field : schema.getFields()) {

      if (!first) {
        sb.append(",");
      } else {
        first = false;
      }

      sb.append("\"").append(field.getName()).append("\": {");

      // The field type is mandatory.
      sb.append("\"type\": \"").append(field.getDataType()).append("\"");

      // If the index field was specified, then append it.
      if (field.getSearchable() != null) {
        sb.append(", \"index\": \"").append(field.getSearchable()
                                            ? "analyzed" : "not_analyzed").append("\"");
      }

      // If a search analyzer was specified, then append it.
      if (field.getSearchAnalyzer() != null) {
        sb.append(", \"search_analyzer\": \"").append(field.getSearchAnalyzer()).append("\"");
      }

      // If an indexing analyzer was specified, then append it.
      if (field.getIndexAnalyzer() != null) {
        sb.append(", \"analyzer\": \"").append(field.getIndexAnalyzer()).append("\"");
      } else {
        sb.append(", \"analyzer\": \"").append("whitespace").append("\"");
      }

      sb.append("}");
    }

    sb.append("}");
    sb.append("}");

    logger.debug("Generated document mappings: " + sb.toString());

    return sb.toString();
  }


  /**
   * Converts an {@link OperationResult} to a standard REST {@link ResponseEntity}
   * object.
   *
   * @param result - The {@link OperationResult} to be converted.
   * @return - The equivalent {@link ResponseEntity} object.
   */
  public ResponseEntity<String> responseFromOperationResult(OperationResult result) {

    if ((result.getResultCode() >= 200) && (result.getResultCode() < 300)) {
      return ResponseEntity.status(result.getResultCode()).contentType ( MediaType.APPLICATION_JSON ).body(result.getResult());
    } else {
      if (result.getFailureCause() != null) {
        return ResponseEntity.status(result.getResultCode()).contentType ( MediaType.APPLICATION_JSON ).body(result.getFailureCause());
      } else {
        return ResponseEntity.status(result.getResultCode()).contentType ( MediaType.APPLICATION_JSON ).body(result.getResult());
      }
    }
  }

  public ResponseEntity<String> errorResponse(HttpStatus status, String msg, HttpServletRequest request) {

    // Generate our audit log.
    auditLogger.info(SearchDbMsgs.PROCESS_REST_REQUEST,
                     new LogFields()
                     .setField(LogLine.DefinedFields.RESPONSE_CODE, status.value ())
                     .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, status.getReasonPhrase()),
                     (request != null) ? request.getMethod().toString () : "Unknown",
                     (request != null) ? request.getRequestURL ().toString () : "Unknown",
                     (request != null) ? request.getRemoteHost () : "Unknown",
                     Integer.toString(status.value ()));

    // Clear the MDC context so that no other transaction inadvertently
    // uses our transaction id.
    ApiUtils.clearMdcContext();

    return ResponseEntity.status(status).contentType ( MediaType.APPLICATION_JSON ).body(msg);
  }


  /**
   * A helper method used for validating/authenticating an incoming request.
   *
   * @param request - The http request that will be validated.
   * @param headers - The http headers that will be validated.
   * @param index - The name of the index that the document store request is being made against.
   * @param failureMsgEnum - The logging message to be used upon validation failure.
   * @return A success or failure response
   */
  private ResponseEntity<String> validateRequest(HttpServletRequest request, HttpHeaders headers, String index, SearchDbMsgs failureMsgEnum) {
    try {
      if (!searchService.validateRequest(headers, request, ApiUtils.Action.POST, ApiUtils.SEARCH_AUTH_POLICY_NAME)) {
        logger.warn(failureMsgEnum, index, "Authentication failure.");
        return errorResponse(HttpStatus.FORBIDDEN, "Authentication failure.", request);
      }
    } catch (Exception e) {
      logger.warn(failureMsgEnum, index, "Unexpected authentication failure - cause: " + e.getMessage());
      return errorResponse(HttpStatus.FORBIDDEN, "Authentication failure.", request);
    }
    return ResponseEntity.status(HttpStatus.OK).body(HEADER_VALIDATION_SUCCESS);
  }
}
