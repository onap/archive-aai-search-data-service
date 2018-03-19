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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.DocumentStoreDataEntityImpl;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.DocumentStoreInterface;
import org.onap.aai.sa.searchdbabstraction.entity.AggregationResults;
import org.onap.aai.sa.searchdbabstraction.entity.DocumentOperationResult;
import org.onap.aai.sa.searchdbabstraction.entity.SearchOperationResult;
import org.onap.aai.sa.searchdbabstraction.logging.SearchDbMsgs;
import org.onap.aai.sa.searchdbabstraction.searchapi.SearchStatement;
import org.onap.aai.sa.searchdbabstraction.searchapi.SuggestionStatement;
import org.onap.aai.cl.api.LogFields;
import org.onap.aai.cl.api.LogLine;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class DocumentApi {
  private static final String REQUEST_HEADER_RESOURCE_VERSION = "If-Match";
  private static final String RESPONSE_HEADER_RESOURCE_VERSION = "ETag";
  private static final String REQUEST_HEADER_ALLOW_IMPLICIT_INDEX_CREATION = "X-CreateIndex";
  
  protected SearchServiceApi searchService = null;

  private Logger logger = LoggerFactory.getInstance().getLogger(DocumentApi.class.getName());
  private Logger auditLogger = LoggerFactory.getInstance()
      .getAuditLogger(DocumentApi.class.getName());

  public DocumentApi(SearchServiceApi searchService) {
    this.searchService = searchService;
  }

  public Response processPost(String content, HttpServletRequest request, HttpHeaders headers,
                              HttpServletResponse httpResponse, String index,
                              DocumentStoreInterface documentStore) {

    // Initialize the MDC Context for logging purposes.
    ApiUtils.initMdcContext(request, headers);

    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.setSerializationInclusion(Include.NON_EMPTY);
      if (content == null) {
        return handleError(request, content, Status.BAD_REQUEST);
      }

      boolean isValid;
      try {
        isValid = searchService.validateRequest(headers, request, ApiUtils.Action.POST,
            ApiUtils.SEARCH_AUTH_POLICY_NAME);
      } catch (Exception e) {
        logger.info(SearchDbMsgs.EXCEPTION_DURING_METHOD_CALL,
            "DocumentApi.processPost",
            e.getMessage());
        return handleError(request, content, Status.FORBIDDEN);
      }

      if (!isValid) {
        return handleError(request, content, Status.FORBIDDEN);
      }

      DocumentStoreDataEntityImpl document = new DocumentStoreDataEntityImpl();
      document.setContent(content);

      DocumentOperationResult result = documentStore.createDocument(index, document, implicitlyCreateIndex(headers));
      String output = null;
      if (result.getResultCode() >= 200 && result.getResultCode() <= 299) {
        output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.getDocument());
      } else {
        output = result.getError() != null
            ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.getError())
            : result.getFailureCause();
      }

      if (httpResponse != null) {
        httpResponse.setHeader(RESPONSE_HEADER_RESOURCE_VERSION, result.getResultVersion());
      }
      Response response = Response.status(result.getResultCode()).entity(output).build();
      logResult(request, Response.Status.fromStatusCode(response.getStatus()));

      // Clear the MDC context so that no other transaction inadvertently
      // uses our transaction id.
      ApiUtils.clearMdcContext();

      return response;
    } catch (Exception e) {
      return handleError(request, e.getMessage(), Status.INTERNAL_SERVER_ERROR);
    }
  }

  public Response processPut(String content, HttpServletRequest request, HttpHeaders headers,
                             HttpServletResponse httpResponse, String index,
                             String id, DocumentStoreInterface documentStore) {

    // Initialize the MDC Context for logging purposes.
    ApiUtils.initMdcContext(request, headers);

    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.setSerializationInclusion(Include.NON_EMPTY);
      if (content == null) {
        return handleError(request, content, Status.BAD_REQUEST);
      }

      boolean isValid;
      try {
        isValid = searchService.validateRequest(headers, request, ApiUtils.Action.PUT,
            ApiUtils.SEARCH_AUTH_POLICY_NAME);
      } catch (Exception e) {
        logger.info(SearchDbMsgs.EXCEPTION_DURING_METHOD_CALL,
            "DocumentApi.processPut",
            e.getMessage());
        return handleError(request, content, Status.FORBIDDEN);
      }

      if (!isValid) {
        return handleError(request, content, Status.FORBIDDEN);
      }

      String resourceVersion = headers.getRequestHeaders()
          .getFirst(REQUEST_HEADER_RESOURCE_VERSION);

      DocumentStoreDataEntityImpl document = new DocumentStoreDataEntityImpl();
      document.setId(id);
      document.setContent(content);
      document.setVersion(resourceVersion);

      DocumentOperationResult result = null;
      if (resourceVersion == null) {
        result = documentStore.createDocument(index, document, implicitlyCreateIndex(headers));
      } else {
        result = documentStore.updateDocument(index, document, implicitlyCreateIndex(headers));
      }

      String output = null;
      if (result.getResultCode() >= 200 && result.getResultCode() <= 299) {
        output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.getDocument());
      } else {
        output = result.getError() != null
            ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.getError())
            : result.getFailureCause();
      }
      if (httpResponse != null) {
        httpResponse.setHeader(RESPONSE_HEADER_RESOURCE_VERSION, result.getResultVersion());
      }
      Response response = Response.status(result.getResultCode()).entity(output).build();
      logResult(request, Response.Status.fromStatusCode(response.getStatus()));

      // Clear the MDC context so that no other transaction inadvertently
      // uses our transaction id.
      ApiUtils.clearMdcContext();

      return response;
    } catch (Exception e) {
      return handleError(request, e.getMessage(), Status.INTERNAL_SERVER_ERROR);
    }
  }

  public Response processDelete(String content, HttpServletRequest request, HttpHeaders headers,
                                HttpServletResponse httpResponse, String index, String id,
                                DocumentStoreInterface documentStore) {

    // Initialize the MDC Context for logging purposes.
    ApiUtils.initMdcContext(request, headers);

    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.setSerializationInclusion(Include.NON_EMPTY);
      boolean isValid;
      try {
        isValid = searchService.validateRequest(headers, request, ApiUtils.Action.DELETE,
            ApiUtils.SEARCH_AUTH_POLICY_NAME);
      } catch (Exception e) {
        logger.info(SearchDbMsgs.EXCEPTION_DURING_METHOD_CALL,
            "DocumentApi.processDelete",
            e.getMessage());
        return handleError(request, content, Status.FORBIDDEN);
      }

      if (!isValid) {
        return handleError(request, content, Status.FORBIDDEN);
      }

      String resourceVersion = headers.getRequestHeaders()
          .getFirst(REQUEST_HEADER_RESOURCE_VERSION);
      if (resourceVersion == null || resourceVersion.isEmpty()) {
        return handleError(request, "Request header 'If-Match' missing",
            javax.ws.rs.core.Response.Status.BAD_REQUEST);
      }

      DocumentStoreDataEntityImpl document = new DocumentStoreDataEntityImpl();
      document.setId(id);
      document.setVersion(resourceVersion);

      DocumentOperationResult result = documentStore.deleteDocument(index, document);
      String output = null;
      if (!(result.getResultCode() >= 200 && result.getResultCode() <= 299)) { //
        output = result.getError() != null
            ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.getError())
            : result.getFailureCause();
      }

      if (httpResponse != null) {
        httpResponse.setHeader(RESPONSE_HEADER_RESOURCE_VERSION, result.getResultVersion());
      }
      Response response;
      if (output == null) {
        response = Response.status(result.getResultCode()).build();
      } else {
        response = Response.status(result.getResultCode()).entity(output).build();
      }

      logResult(request, Response.Status.fromStatusCode(response.getStatus()));

      // Clear the MDC context so that no other transaction inadvertently
      // uses our transaction id.
      ApiUtils.clearMdcContext();

      return response;
    } catch (Exception e) {
      return handleError(request, e.getMessage(), Status.INTERNAL_SERVER_ERROR);
    }
  }

  public Response processGet(String content, HttpServletRequest request, HttpHeaders headers,
                             HttpServletResponse httpResponse, String index, String id,
                             DocumentStoreInterface documentStore) {

    // Initialize the MDC Context for logging purposes.
    ApiUtils.initMdcContext(request, headers);

    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.setSerializationInclusion(Include.NON_EMPTY);
      boolean isValid;
      try {
        isValid = searchService.validateRequest(headers, request, ApiUtils.Action.GET,
            ApiUtils.SEARCH_AUTH_POLICY_NAME);
      } catch (Exception e) {
        logger.info(SearchDbMsgs.EXCEPTION_DURING_METHOD_CALL,
            "DocumentApi.processGet",
            e.getMessage());
        return handleError(request, content, Status.FORBIDDEN);
      }

      if (!isValid) {
        return handleError(request, content, Status.FORBIDDEN);
      }

      String resourceVersion = headers.getRequestHeaders()
          .getFirst(REQUEST_HEADER_RESOURCE_VERSION);

      DocumentStoreDataEntityImpl document = new DocumentStoreDataEntityImpl();
      document.setId(id);
      document.setVersion(resourceVersion);

      DocumentOperationResult result = documentStore.getDocument(index, document);
      String output = null;
      if (result.getResultCode() >= 200 && result.getResultCode() <= 299) {
        output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.getDocument());
      } else {
        output = result.getError() != null
            ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.getError())
            : result.getFailureCause();
      }
      if (httpResponse != null) {
        httpResponse.setHeader(RESPONSE_HEADER_RESOURCE_VERSION, result.getResultVersion());
      }
      Response response = Response.status(result.getResultCode()).entity(output).build();
      logResult(request, Response.Status.fromStatusCode(response.getStatus()));

      // Clear the MDC context so that no other transaction inadvertently
      // uses our transaction id.
      ApiUtils.clearMdcContext();

      return response;
    } catch (Exception e) {
      return handleError(request, e.getMessage(), Status.INTERNAL_SERVER_ERROR);
    }
  }

  public Response processSearchWithGet(String content, HttpServletRequest request,
                                       HttpHeaders headers, String index,
                                       String queryText, DocumentStoreInterface documentStore) {

    // Initialize the MDC Context for logging purposes.
    ApiUtils.initMdcContext(request, headers);

    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.setSerializationInclusion(Include.NON_EMPTY);

      boolean isValid;
      try {
        isValid = searchService.validateRequest(headers, request, ApiUtils.Action.GET,
            ApiUtils.SEARCH_AUTH_POLICY_NAME);
      } catch (Exception e) {
        logger.info(SearchDbMsgs.EXCEPTION_DURING_METHOD_CALL,
            "processSearchWithGet",
            e.getMessage());
        return handleError(request, content, Status.FORBIDDEN);
      }

      if (!isValid) {
        return handleError(request, content, Status.FORBIDDEN);
      }

      SearchOperationResult result = documentStore.search(index, queryText);
      String output = null;
      if (result.getResultCode() >= 200 && result.getResultCode() <= 299) {
        output = mapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(result.getSearchResult());
      } else {
        output = result.getError() != null
            ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.getError())
            : result.getFailureCause();
      }
      Response response = Response.status(result.getResultCode()).entity(output).build();

      // Clear the MDC context so that no other transaction inadvertently
      // uses our transaction id.
      ApiUtils.clearMdcContext();

      return response;
    } catch (Exception e) {
      return handleError(request, e.getMessage(), Status.INTERNAL_SERVER_ERROR);
    }
  }

  public Response queryWithGetWithPayload(String content, HttpServletRequest request,
                                          HttpHeaders headers, String index,
                                          DocumentStoreInterface documentStore) {

    // Initialize the MDC Context for logging purposes.
    ApiUtils.initMdcContext(request, headers);

    logger.info(SearchDbMsgs.PROCESS_PAYLOAD_QUERY, "GET", (request != null)
        ? request.getRequestURL().toString() : "");
    if (logger.isDebugEnabled()) {
      logger.debug("Request Body: " + content);
    }
    return processQuery(index, content, request, headers, documentStore);
  }

  public Response processSearchWithPost(String content, HttpServletRequest request,
                                        HttpHeaders headers, String index,
                                        DocumentStoreInterface documentStore) {

    // Initialize the MDC Context for logging purposes.
    ApiUtils.initMdcContext(request, headers);

    logger.info(SearchDbMsgs.PROCESS_PAYLOAD_QUERY, "POST", (request != null)
        ? request.getRequestURL().toString() : "");
    if (logger.isDebugEnabled()) {
      logger.debug("Request Body: " + content);
    }

    return processQuery(index, content, request, headers, documentStore);
  }
  
  public Response processSuggestQueryWithPost(String content, HttpServletRequest request,
          HttpHeaders headers, String index,
          DocumentStoreInterface documentStore) {

	  // Initialize the MDC Context for logging purposes.
	  ApiUtils.initMdcContext(request, headers);

	  logger.info(SearchDbMsgs.PROCESS_PAYLOAD_QUERY, "POST", (request != null)
			  ? request.getRequestURL().toString() : "");
	  if (logger.isDebugEnabled()) {
		  logger.debug("Request Body: " + content);
	  }

	  return processSuggestQuery(index, content, request, headers, documentStore);
  }

  /**
   * Common handler for query requests. This is called by both the GET with
   * payload and POST with payload variants of the query endpoint.
   *
   * @param index   - The index to be queried against.
   * @param content - The payload containing the query structure.
   * @param request - The HTTP request.
   * @param headers - The HTTP headers.
   * @return - A standard HTTP response.
   */
  private Response processQuery(String index, String content, HttpServletRequest request,
                                HttpHeaders headers, DocumentStoreInterface documentStore) {

    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.setSerializationInclusion(Include.NON_EMPTY);

      // Make sure that we were supplied a payload before proceeding.
      if (content == null) {
        return handleError(request, content, Status.BAD_REQUEST);
      }

      // Validate that the request has the appropriate authorization.
      boolean isValid;
      try {
        isValid = searchService.validateRequest(headers, request, ApiUtils.Action.POST,
            ApiUtils.SEARCH_AUTH_POLICY_NAME);

      } catch (Exception e) {
        logger.info(SearchDbMsgs.EXCEPTION_DURING_METHOD_CALL,
            "processQuery",
            e.getMessage());
        return handleError(request, content, Status.FORBIDDEN);
      }

      if (!isValid) {
        return handleError(request, content, Status.FORBIDDEN);
      }

      SearchStatement searchStatement;

      try {
        // Marshall the supplied request payload into a search statement
        // object.
        searchStatement = mapper.readValue(content, SearchStatement.class);

      } catch (Exception e) {
        return handleError(request, e.getMessage(), Status.BAD_REQUEST);
      }

      // Now, submit the search statement, translated into
      // ElasticSearch syntax, to the document store DAO.
      SearchOperationResult result = documentStore.searchWithPayload(index,
          searchStatement.toElasticSearch());
      String output = null;
      if (result.getResultCode() >= 200 && result.getResultCode() <= 299) {
        output = prepareOutput(mapper, result);
      } else {
        output = result.getError() != null
            ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.getError())
            : result.getFailureCause();
      }
      Response response = Response.status(result.getResultCode()).entity(output).build();

      // Clear the MDC context so that no other transaction inadvertently
      // uses our transaction id.
      ApiUtils.clearMdcContext();

      return response;

    } catch (Exception e) {
      return handleError(request, e.getMessage(), Status.INTERNAL_SERVER_ERROR);
    }
  }
  
  /**
   * Common handler for query requests. This is called by both the GET with
   * payload and POST with payload variants of the query endpoint.
   *
   * @param index   - The index to be queried against.
   * @param content - The payload containing the query structure.
   * @param request - The HTTP request.
   * @param headers - The HTTP headers.
   * @return - A standard HTTP response.
   */
  private Response processSuggestQuery(String index, String content, HttpServletRequest request,
                                HttpHeaders headers, DocumentStoreInterface documentStore) {

    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.setSerializationInclusion(Include.NON_EMPTY);

      // Make sure that we were supplied a payload before proceeding.
      if (content == null) {
        return handleError(request, content, Status.BAD_REQUEST);
      }

      // Validate that the request has the appropriate authorization.
      boolean isValid;
      try {
        isValid = searchService.validateRequest(headers, request, ApiUtils.Action.POST,
            ApiUtils.SEARCH_AUTH_POLICY_NAME);

      } catch (Exception e) {
        logger.info(SearchDbMsgs.EXCEPTION_DURING_METHOD_CALL,
            "processQuery",
            e.getMessage());
        return handleError(request, content, Status.FORBIDDEN);
      }

      if (!isValid) {
        return handleError(request, content, Status.FORBIDDEN);
      }

      SuggestionStatement suggestionStatement;

      try {
        // Marshall the supplied request payload into a search statement
        // object.
    	  suggestionStatement = mapper.readValue(content, SuggestionStatement.class);

      } catch (Exception e) {
        return handleError(request, e.getMessage(), Status.BAD_REQUEST);
      }

      // Now, submit the search statement, translated into
      // ElasticSearch syntax, to the document store DAO.
      SearchOperationResult result = documentStore.suggestionQueryWithPayload(index,
    		  suggestionStatement.toElasticSearch());
      String output = null;
      if (result.getResultCode() >= 200 && result.getResultCode() <= 299) {
    	  output = prepareSuggestOutput(mapper, result);
      } else {
        output = result.getError() != null
            ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.getError())
            : result.getFailureCause();
      }
      Response response = Response.status(result.getResultCode()).entity(output).build();

      // Clear the MDC context so that no other transaction inadvertently
      // uses our transaction id.
      ApiUtils.clearMdcContext();

      return response;

    } catch (Exception e) {
      return handleError(request, e.getMessage(), Status.INTERNAL_SERVER_ERROR);
    }
  }

  
  /**
   * Checks the supplied HTTP headers to see if we should allow the underlying document 
   * store to implicitly create the index referenced in a document PUT or POST if it
   * does not already exist in the data store.
   * 
   * @param headers - The HTTP headers to examine.
   * 
   * @return - true if the headers indicate that missing indices should be implicitly created,
   *           false otherwise.
   */
  private boolean implicitlyCreateIndex(HttpHeaders headers) {
    
    boolean createIndexIfNotPresent = false;
    String implicitIndexCreationHeader = 
        headers.getRequestHeaders().getFirst(REQUEST_HEADER_ALLOW_IMPLICIT_INDEX_CREATION);
    
    if( (implicitIndexCreationHeader != null) && (implicitIndexCreationHeader.equals("true")) ) {
      createIndexIfNotPresent = true;
    }
    
    return createIndexIfNotPresent;
  }
  
  
  private String prepareOutput(ObjectMapper mapper, SearchOperationResult result)
      throws JsonProcessingException {
    StringBuffer output = new StringBuffer();
    output.append("{\r\n\"searchResult\":");
    output.append(mapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(result.getSearchResult()));
    AggregationResults aggs = result.getAggregationResult();
    if (aggs != null) {
      output.append(",\r\n\"aggregationResult\":");
      output.append(mapper.setSerializationInclusion(Include.NON_NULL)
          .writerWithDefaultPrettyPrinter().writeValueAsString(aggs));
    }
    output.append("\r\n}");
    return output.toString();
  }
  
  private String prepareSuggestOutput(ObjectMapper mapper, SearchOperationResult result)
	      throws JsonProcessingException {
	    StringBuffer output = new StringBuffer();
	    output.append("{\r\n\"searchResult\":");
	    output.append(mapper.writerWithDefaultPrettyPrinter()
	        .writeValueAsString(result.getSuggestResult()));
	    AggregationResults aggs = result.getAggregationResult();
	    if (aggs != null) {
	      output.append(",\r\n\"aggregationResult\":");
	      output.append(mapper.setSerializationInclusion(Include.NON_NULL)
	          .writerWithDefaultPrettyPrinter().writeValueAsString(aggs));
	    }
	    output.append("\r\n}");
	    return output.toString();
	  }

  private Response handleError(HttpServletRequest request, String message, Status status) {
    logResult(request, status);
    return Response.status(status).entity(message).type(MediaType.APPLICATION_JSON).build();
  }

  void logResult(HttpServletRequest request, Response.Status status) {

    logger.info(SearchDbMsgs.PROCESS_REST_REQUEST, (request != null) ? request.getMethod() : "",
        (request != null) ? request.getRequestURL().toString() : "",
        (request != null) ? request.getRemoteHost() : "", Integer.toString(status.getStatusCode()));

    auditLogger.info(SearchDbMsgs.PROCESS_REST_REQUEST,
        new LogFields()
            .setField(LogLine.DefinedFields.RESPONSE_CODE, status.getStatusCode())
            .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, status.getReasonPhrase()),
        (request != null) ? request.getMethod() : "",
        (request != null) ? request.getRequestURL().toString() : "",
        (request != null) ? request.getRemoteHost() : "", Integer.toString(status.getStatusCode()));

    // Clear the MDC context so that no other transaction inadvertently
    // uses our transaction id.
    ApiUtils.clearMdcContext();
  }
}
