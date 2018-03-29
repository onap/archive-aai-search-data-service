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

import com.google.common.base.Throwables;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.onap.aai.sa.rest.AnalysisConfiguration;
import org.onap.aai.sa.rest.ApiUtils;
import org.onap.aai.sa.rest.BulkRequest;
import org.onap.aai.sa.rest.BulkRequest.OperationType;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.config.ElasticSearchConfig;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.exception.DocumentStoreOperationException;
import org.onap.aai.sa.searchdbabstraction.entity.AggregationResult;
import org.onap.aai.sa.searchdbabstraction.entity.AggregationResults;
import org.onap.aai.sa.searchdbabstraction.entity.Document;
import org.onap.aai.sa.searchdbabstraction.entity.DocumentOperationResult;
import org.onap.aai.sa.searchdbabstraction.entity.ErrorResult;
import org.onap.aai.sa.searchdbabstraction.entity.OperationResult;
import org.onap.aai.sa.searchdbabstraction.entity.SearchHit;
import org.onap.aai.sa.searchdbabstraction.entity.SearchHits;
import org.onap.aai.sa.searchdbabstraction.entity.SearchOperationResult;
import org.onap.aai.sa.searchdbabstraction.logging.SearchDbMsgs;
import org.onap.aai.sa.searchdbabstraction.util.AggregationParsingUtil;
import org.onap.aai.sa.searchdbabstraction.util.DocumentSchemaUtil;
import org.onap.aai.sa.searchdbabstraction.util.SearchDbConstants;
import org.onap.aai.cl.api.LogFields;
import org.onap.aai.cl.api.LogLine;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.cl.mdc.MdcContext;
import org.onap.aai.cl.mdc.MdcOverride;
import org.onap.aai.sa.rest.DocumentSchema;
import org.onap.aai.sa.searchdbabstraction.entity.SuggestHit;
import org.onap.aai.sa.searchdbabstraction.entity.SuggestHits;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.ws.rs.core.Response.Status;


/**
 * This class has the Elasticsearch implementation of the
 * DB operations defined in DocumentStoreInterface.
 */
public class ElasticSearchHttpController implements DocumentStoreInterface {

  private static final String BULK_CREATE_WITHOUT_INDEX_TEMPLATE =
      "{\"create\":{\"_index\" : \"%s\", \"_type\" : \"%s\"} }\n";
  private static final String BULK_CREATE_WITH_INDEX_TEMPLATE =
      "{\"create\":{\"_index\" : \"%s\", \"_type\" : \"%s\", \"_id\" : \"%s\" } }\n";
  private static final String BULK_IMPORT_INDEX_TEMPLATE =
      "{\"index\":{\"_index\":\"%s\",\"_type\":\"%s\",\"_id\":\"%s\", \"_version\":\"%s\"}}\n";
  private static final String BULK_DELETE_TEMPLATE =
      "{ \"delete\": { \"_index\": \"%s\", \"_type\": \"%s\", \"_id\": \"%s\", \"_version\":\"%s\"}}\n";

  private static final String INTERNAL_SERVER_ERROR_ELASTIC_SEARCH_OPERATION_FAULT =
      "Internal Error: ElasticSearch operation fault occurred";
  private static final Logger logger = LoggerFactory.getInstance()
      .getLogger(ElasticSearchHttpController.class.getName());
  private static final Logger metricsLogger = LoggerFactory.getInstance()
      .getMetricsLogger(ElasticSearchHttpController.class.getName());
  private final ElasticSearchConfig config;

  private static final String DEFAULT_TYPE = "default";

  private static ElasticSearchHttpController instance = null;

  protected AnalysisConfiguration analysisConfig;

  public static ElasticSearchHttpController getInstance() {

    synchronized (ElasticSearchHttpController.class) {

      if (instance == null) {

        Properties properties = new Properties();
        File file = new File(SearchDbConstants.ES_CONFIG_FILE);
        try {
          properties.load(new FileInputStream(file));
        } catch (Exception e) {
          logger.error(SearchDbMsgs.EXCEPTION_DURING_METHOD_CALL,
            "ElasticSearchHTTPController.getInstance",
            e.getLocalizedMessage());
        }

        ElasticSearchConfig config = new ElasticSearchConfig(properties);
        instance = new ElasticSearchHttpController(config);
      }
    }

    return instance;
  }

  public ElasticSearchHttpController(ElasticSearchConfig config) {
    this.config = config;
    analysisConfig = new AnalysisConfiguration();

    try {
      logger.info(SearchDbMsgs.ELASTIC_SEARCH_CONNECTION_ATTEMPT, getFullUrl("", false));
      checkConnection();
      logger.info(SearchDbMsgs.ELASTIC_SEARCH_CONNECTION_SUCCESS, getFullUrl("", false));
    } catch (Exception e) {
      logger.error(SearchDbMsgs.ELASTIC_SEARCH_CONNECTION_FAILURE, null, e,
          getFullUrl("", false), e.getMessage());
    }
  }


  public AnalysisConfiguration getAnalysisConfig() {
    return analysisConfig;
  }

  @Override
  public OperationResult createIndex(String index, DocumentSchema documentSchema) {

    OperationResult result = new OperationResult();
    result.setResultCode(500);

    try {

      // Submit the request to ElasticSearch to create the index using a
      // default document type.
      result = createTable(index,
          DEFAULT_TYPE,
          analysisConfig.getEsIndexSettings(),
          DocumentSchemaUtil.generateDocumentMappings(documentSchema));

      // ElasticSearch will return us a 200 code on success when we
      // want to report a 201, so translate the result here.
      result.setResultCode((result.getResultCode() == 200) ? 201 : result.getResultCode());
      if (isSuccess(result)) {
        result.setResult("{\"url\": \"" + ApiUtils.buildIndexUri(index) + "\"}");
        //result.setResult("{\"index\": \"" + index + ", \"type\": \"" + DEFAULT_TYPE + "\"}");
      }

    } catch (DocumentStoreOperationException e) {

      result.setFailureCause("Document store operation failure.  Cause: " + e.getMessage());
    }

    return result;
  }

  @Override
  public OperationResult createDynamicIndex(String index, String dynamicSchema) {
    OperationResult result = new OperationResult();
    result.setResultCode(500);

    try {
      result = createTable(index, dynamicSchema);

      // ElasticSearch will return us a 200 code on success when we
      // want to report a 201, so translate the result here.
      result.setResultCode((result.getResultCode() == 200) ? 201 : result.getResultCode());
      if (isSuccess(result)) {
        result.setResult("{\"url\": \"" + ApiUtils.buildIndexUri(index) + "\"}");
      }
    } catch (DocumentStoreOperationException e) {
      result.setFailureCause("Document store operation failure.  Cause: " + e.getMessage());
    }

    return result;
  }


  @Override
  public OperationResult deleteIndex(String indexName) throws DocumentStoreOperationException {

    //Initialize operation result with a failure codes / fault string
    OperationResult opResult = new OperationResult();
    opResult.setResultCode(500);
    opResult.setResult(INTERNAL_SERVER_ERROR_ELASTIC_SEARCH_OPERATION_FAULT);

    // Grab the current time so we can use it to generate a metrics log.
    MdcOverride override = getStartTime(new MdcOverride());

    String fullUrl = getFullUrl("/" + indexName + "/", false);
    HttpURLConnection conn = initializeConnection(fullUrl);

    logger.debug("\nSending 'DELETE' request to URL : " + conn.getURL());

    try {
      conn.setRequestMethod("DELETE");
    } catch (ProtocolException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to set HTTP request method to DELETE.", e);
    }

    handleResponse(conn, opResult);

    // Generate a metrics log so we can track how long the operation took.
    metricsLogger.info(SearchDbMsgs.DELETE_INDEX_TIME,
        new LogFields()
            .setField(LogLine.DefinedFields.RESPONSE_CODE, opResult.getResultCode())
            .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, opResult.getResult()),
        override,
        indexName);

    shutdownConnection(conn);

    return opResult;
  }


  private OperationResult checkConnection() throws Exception {

    String fullUrl = getFullUrl("/_cluster/health", false);
    URL url = null;
    HttpURLConnection conn = null;

    url = new URL(fullUrl);
    conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setDoOutput(true);
    logger.debug("getClusterHealth(), Sending 'GET' request to URL : " + url);

    int resultCode = conn.getResponseCode();
    logger.debug("getClusterHealth() response Code : " + resultCode);
    OperationResult opResult = new OperationResult();
    opResult.setResultCode(resultCode);

    shutdownConnection(conn);

    return opResult;
  }

  private String getFullUrl(String resourceUrl, boolean isSecure) {

    final String host = config.getIpAddress();
    final String port = config.getHttpPort();

    if (isSecure) {
      return String.format("https://%s:%s%s", host, port, resourceUrl);
    } else {
      return String.format("http://%s:%s%s", host, port, resourceUrl);
    }
  }

  private void shutdownConnection(HttpURLConnection connection) {
    if (connection == null) {
      return;
    }

    InputStream inputstream = null;
    OutputStream outputstream = null;

    try {
      inputstream = connection.getInputStream();
    } catch (IOException e) {
      logger.debug(SearchDbMsgs.EXCEPTION_DURING_METHOD_CALL, "shutdownConnection", e.getLocalizedMessage());
    } finally {
      if (inputstream != null) {
        try {
          inputstream.close();
        } catch (IOException e) {
          logger.debug(SearchDbMsgs.EXCEPTION_DURING_METHOD_CALL, "shutdownConnection",
              e.getLocalizedMessage());
        }
      }
    }

    try {
      outputstream = connection.getOutputStream();
    } catch (IOException e) {
      logger.debug(SearchDbMsgs.EXCEPTION_DURING_METHOD_CALL, "shutdownConnection", e.getLocalizedMessage());
    } finally {
      if (outputstream != null) {
        try {
          outputstream.close();
        } catch (IOException e) {
          logger.debug(SearchDbMsgs.EXCEPTION_DURING_METHOD_CALL, "shutdownConnection",
              e.getLocalizedMessage());
        }
      }
    }

    connection.disconnect();
  }

  //@Override
  protected OperationResult createTable(String indexName, String typeName,
                                        String indexSettings, String indexMappings)
      throws DocumentStoreOperationException {

    if (indexSettings == null) {
      logger.debug("No settings provided.");
    }

    if (indexMappings == null) {
      logger.debug("No mappings provided.");
    }

    OperationResult opResult = new OperationResult();

    // Initialize operation result with a failure codes / fault string
    opResult.setResultCode(500);
    opResult.setResult(INTERNAL_SERVER_ERROR_ELASTIC_SEARCH_OPERATION_FAULT);

    // Grab the current time so we can use it to generate a metrics log.
    MdcOverride override = getStartTime(new MdcOverride());

    String fullUrl = getFullUrl("/" + indexName + "/", false);
    HttpURLConnection conn = initializeConnection(fullUrl);

    try {
      conn.setRequestMethod("PUT");
    } catch (ProtocolException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to set HTTP request method to PUT.", e);
    }

    StringBuilder sb = new StringBuilder(128);
    sb.append("{ \"settings\" : ");
    sb.append(indexSettings);
    sb.append(",");

    sb.append("\"mappings\" : {");
    sb.append("\"" + typeName + "\" :");
    sb.append(indexMappings);
    sb.append("}}");

    attachContent(conn, sb.toString());

    logger.debug("\ncreateTable(), Sending 'PUT' request to URL : " + conn.getURL());
    logger.debug("Request content: " + sb.toString());

    handleResponse(conn, opResult);

    shutdownConnection(conn);

    // Generate a metrics log so we can track how long the operation took.
    metricsLogger.info(SearchDbMsgs.CREATE_INDEX_TIME,
        new LogFields()
            .setField(LogLine.DefinedFields.RESPONSE_CODE, opResult.getResultCode())
            .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, opResult.getResultCode()),
        override,
        indexName);

    return opResult;
  }

  /**
   * Will send the passed in JSON payload to Elasticsearch using the
   * provided index name in an attempt to create the index.
   *
   * @param indexName - The name of the index to be created
   * @param settingsAndMappings - The actual JSON object that will define the index
   * @return - The operation result of writing into Elasticsearch
   * @throws DocumentStoreOperationException
   */
  protected OperationResult createTable(String indexName, String settingsAndMappings) throws DocumentStoreOperationException {
    OperationResult result = new OperationResult();
    result.setResultCode(500);
    result.setResult(INTERNAL_SERVER_ERROR_ELASTIC_SEARCH_OPERATION_FAULT);

    // Grab the current time so we can use it to generate a metrics log.
    MdcOverride override = getStartTime(new MdcOverride());

    String fullUrl = getFullUrl("/" + indexName + "/", false);
    HttpURLConnection conn = initializeConnection(fullUrl);

    try {
      conn.setRequestMethod("PUT");
    } catch (ProtocolException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to set HTTP request method to PUT.", e);
    }

    attachContent(conn, settingsAndMappings);
    handleResponse(conn, result);

    // Generate a metrics log so we can track how long the operation took.
    metricsLogger.info(SearchDbMsgs.CREATE_INDEX_TIME,
            new LogFields()
                    .setField(LogLine.DefinedFields.RESPONSE_CODE, result.getResultCode())
                    .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, result.getResultCode()),
            override,
            indexName);

    return result;
  }

  @Override
  public DocumentOperationResult createDocument(String                  indexName,
                                                DocumentStoreDataEntity document,
                                                boolean                 allowImplicitIndexCreation)
      throws DocumentStoreOperationException {

    if(!allowImplicitIndexCreation) {

      // Before we do anything, make sure that the specified index actually exists in the
      // document store - we don't want to rely on ElasticSearch to fail the document
      // create because it could be configured to implicitly create a non-existent index,
      // which can lead to hard-to-debug behaviour with queries down the road.
      OperationResult indexExistsResult = checkIndexExistence(indexName);
      if ((indexExistsResult.getResultCode() < 200) || (indexExistsResult.getResultCode() >= 300)) {

        DocumentOperationResult opResult = new DocumentOperationResult();
        opResult.setResultCode(Status.NOT_FOUND.getStatusCode());
        opResult.setResult("Document Index '" + indexName + "' does not exist.");
        opResult.setFailureCause("Document Index '" + indexName + "' does not exist.");
        return opResult;
      }
    }

    if (document.getId() == null || document.getId().isEmpty()) {
      return createDocumentWithoutId(indexName, document);
    } else {
      return createDocumentWithId(indexName, document);
    }
  }

  private DocumentOperationResult createDocumentWithId(String indexName,
                                                       DocumentStoreDataEntity document)
      throws DocumentStoreOperationException {
    // check if the document already exists
    DocumentOperationResult opResult = checkDocumentExistence(indexName, document.getId());


    if (opResult.getResultCode() != Status.NOT_FOUND.getStatusCode()) {
      if (opResult.getResultCode() == Status.OK.getStatusCode()) {
        opResult.setFailureCause("A document with the same id already exists.");
      } else {
        opResult.setFailureCause("Failed to verify a document with the specified id does not already exist.");
      }
      opResult.setResultCode(Status.CONFLICT.getStatusCode());
      return opResult;
    }

    opResult = new DocumentOperationResult();
    // Initialize operation result with a failure codes / fault string
    opResult.setResultCode(500);
    opResult.setResult(INTERNAL_SERVER_ERROR_ELASTIC_SEARCH_OPERATION_FAULT);

    // Grab the current time so we can use it to generate a metrics log.
    MdcOverride override = getStartTime(new MdcOverride());

    String fullUrl = getFullUrl("/" + indexName + "/" + DEFAULT_TYPE
        + "/" + document.getId(), false);
    HttpURLConnection conn = initializeConnection(fullUrl);

    try {
      conn.setRequestMethod("PUT");
    } catch (ProtocolException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to set HTTP request method to PUT.", e);
    }

    attachDocument(conn, document);

    logger.debug("Sending 'PUT' request to: " + conn.getURL());

    handleResponse(conn, opResult);
    buildDocumentResult(opResult, indexName);

    // Generate a metrics log so we can track how long the operation took.
    metricsLogger.info(SearchDbMsgs.CREATE_DOCUMENT_TIME,
        new LogFields()
            .setField(LogLine.DefinedFields.RESPONSE_CODE, opResult.getResultCode())
            .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, opResult.getResult()),
        override,
        indexName);

    shutdownConnection(conn);

    return opResult;

  }

  private DocumentOperationResult createDocumentWithoutId(String indexName,
                                                          DocumentStoreDataEntity document)
      throws DocumentStoreOperationException {

    DocumentOperationResult response = new DocumentOperationResult();
    // Initialize operation result with a failure codes / fault string
    response.setResultCode(500);
    response.setResult(INTERNAL_SERVER_ERROR_ELASTIC_SEARCH_OPERATION_FAULT);

    // Grab the current time so we can use it to generate a metrics log.
    MdcOverride override = getStartTime(new MdcOverride());

    String fullUrl = getFullUrl("/" + indexName + "/" + DEFAULT_TYPE, false);
    HttpURLConnection conn = initializeConnection(fullUrl);

    try {
      conn.setRequestMethod("POST");
    } catch (ProtocolException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to set HTTP request method to POST.", e);
    }

    attachDocument(conn, document);

    logger.debug("Sending 'POST' request to: " + conn.getURL());

    handleResponse(conn, response);
    buildDocumentResult(response, indexName);

    // Generate a metrics log so we can track how long the operation took.
    metricsLogger.info(SearchDbMsgs.CREATE_DOCUMENT_TIME,
        new LogFields()
            .setField(LogLine.DefinedFields.RESPONSE_CODE, response.getResultCode())
            .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, response.getResult()),
        override,
        indexName);

    shutdownConnection(conn);

    return response;
  }

  private void attachDocument(HttpURLConnection conn, DocumentStoreDataEntity doc)
      throws DocumentStoreOperationException {
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    conn.setRequestProperty("Connection", "Close");

    attachContent(conn, doc.getContentInJson());
  }

  private DocumentOperationResult checkDocumentExistence(String indexName,
                                                         String docId)
      throws DocumentStoreOperationException {
    DocumentOperationResult opResult = new DocumentOperationResult();

    // Initialize operation result with a failure codes / fault string
    opResult.setResultCode(500);

    // Grab the current time so we can use it to generate a metrics log.
    MdcOverride override = getStartTime(new MdcOverride());

    String fullUrl = getFullUrl("/" + indexName + "/" + DEFAULT_TYPE + "/" + docId, false);
    HttpURLConnection conn = initializeConnection(fullUrl);

    try {
      conn.setRequestMethod("HEAD");
    } catch (ProtocolException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to set HTTP request method to HEAD.", e);
    }

    logger.debug("Sending 'HEAD' request to: " + conn.getURL());

    int resultCode;
    try {
      resultCode = conn.getResponseCode();
    } catch (IOException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to get the response code from the connection.", e);
    }

    logger.debug("Response Code : " + resultCode);

    opResult.setResultCode(resultCode);

    // Generate a metrics log so we can track how long the operation took.
    metricsLogger.info(SearchDbMsgs.GET_DOCUMENT_TIME,
        new LogFields()
            .setField(LogLine.DefinedFields.RESPONSE_CODE, opResult.getResultCode())
            .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, opResult.getResult()),
        override,
        indexName,
        docId);

    shutdownConnection(conn);

    return opResult;
  }

  @Override
  public DocumentOperationResult updateDocument(String                  indexName,
                                                DocumentStoreDataEntity document,
                                                boolean                 allowImplicitIndexCreation)
      throws DocumentStoreOperationException {

    if(!allowImplicitIndexCreation) {

      // Before we do anything, make sure that the specified index actually exists in the
      // document store - we don't want to rely on ElasticSearch to fail the document
      // create because it could be configured to implicitly create a non-existent index,
      // which can lead to hard-to-debug behaviour with queries down the road.
      OperationResult indexExistsResult = checkIndexExistence(indexName);
      if ((indexExistsResult.getResultCode() < 200) || (indexExistsResult.getResultCode() >= 300)) {

        DocumentOperationResult opResult = new DocumentOperationResult();
        opResult.setResultCode(Status.NOT_FOUND.getStatusCode());
        opResult.setResult("Document Index '" + indexName + "' does not exist.");
        opResult.setFailureCause("Document Index '" + indexName + "' does not exist.");
        return opResult;
      }
    }

    DocumentOperationResult opResult = new DocumentOperationResult();

    // Initialize operation result with a failure codes / fault string
    opResult.setResultCode(500);
    opResult.setResult(INTERNAL_SERVER_ERROR_ELASTIC_SEARCH_OPERATION_FAULT);

    // Grab the current time so we can use it to generate a metrics log.
    MdcOverride override = getStartTime(new MdcOverride());

    String fullUrl = getFullUrl("/" + indexName + "/" + DEFAULT_TYPE + "/" + document.getId()
        + "?version=" + document.getVersion(), false);
    HttpURLConnection conn = initializeConnection(fullUrl);

    try {
      conn.setRequestMethod("PUT");
    } catch (ProtocolException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to set HTTP request method to PUT.", e);
    }

    attachDocument(conn, document);

    logger.debug("Sending 'PUT' request to: " + conn.getURL());

    handleResponse(conn, opResult);
    buildDocumentResult(opResult, indexName);

    // Generate a metrics log so we can track how long the operation took.
    metricsLogger.info(SearchDbMsgs.UPDATE_DOCUMENT_TIME,
        new LogFields()
            .setField(LogLine.DefinedFields.RESPONSE_CODE, opResult.getResultCode())
            .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, opResult.getResult()),
        override,
        indexName,
        document.getId());

    shutdownConnection(conn);

    return opResult;
  }

  @Override
  public DocumentOperationResult deleteDocument(String indexName, DocumentStoreDataEntity document)
      throws DocumentStoreOperationException {
    DocumentOperationResult opResult = new DocumentOperationResult();

    // Initialize operation result with a failure codes / fault string
    opResult.setResultCode(500);
    opResult.setResult(INTERNAL_SERVER_ERROR_ELASTIC_SEARCH_OPERATION_FAULT);

    // Grab the current time so we can use it to generate a metrics log.
    MdcOverride override = getStartTime(new MdcOverride());

    String fullUrl = getFullUrl("/" + indexName + "/" + DEFAULT_TYPE + "/" + document.getId()
        + "?version=" + document.getVersion(), false);
    HttpURLConnection conn = initializeConnection(fullUrl);

    try {
      conn.setRequestMethod("DELETE");
    } catch (ProtocolException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to set HTTP request method to DELETE.", e);
    }

    logger.debug("\nSending 'DELETE' request to " + conn.getURL());

    handleResponse(conn, opResult);
    buildDocumentResult(opResult, indexName);
    //supress the etag and url in response for delete as they are not required
    if (opResult.getDocument() != null) {
      opResult.getDocument().setEtag(null);
      opResult.getDocument().setUrl(null);
    }

    // Generate a metrics log so we can track how long the operation took.
    metricsLogger.info(SearchDbMsgs.DELETE_DOCUMENT_TIME,
        new LogFields()
            .setField(LogLine.DefinedFields.RESPONSE_CODE, opResult.getResult())
            .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, opResult.getResultCode()),
        override,
        indexName,
        document.getId());

    shutdownConnection(conn);

    return opResult;
  }

  @Override
  public DocumentOperationResult getDocument(String indexName, DocumentStoreDataEntity document)
      throws DocumentStoreOperationException {
    DocumentOperationResult opResult = new DocumentOperationResult();

    // Initialize operation result with a failure codes / fault string
    opResult.setResultCode(500);
    opResult.setResult(INTERNAL_SERVER_ERROR_ELASTIC_SEARCH_OPERATION_FAULT);

    // Grab the current time so we can use it to generate a metrics log.
    MdcOverride override = getStartTime(new MdcOverride());

    String fullUrl = null;
    if (document.getVersion() == null) {
      fullUrl = getFullUrl("/" + indexName + "/" + DEFAULT_TYPE + "/" + document.getId(), false);
    } else {
      fullUrl = getFullUrl("/" + indexName + "/" + DEFAULT_TYPE + "/" + document.getId()
          + "?version=" + document.getVersion(), false);
    }
    HttpURLConnection conn = initializeConnection(fullUrl);

    logger.debug("\nSending 'GET' request to: " + conn.getURL());

    handleResponse(conn, opResult);
    buildDocumentResult(opResult, indexName);

    // Generate a metrics log so we can track how long the operation took.
    metricsLogger.info(SearchDbMsgs.GET_DOCUMENT_TIME,
        new LogFields()
            .setField(LogLine.DefinedFields.RESPONSE_CODE, opResult.getResultCode())
            .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, opResult.getResult()),
        override,
        indexName,
        document.getId());

    shutdownConnection(conn);

    return opResult;
  }

  public SearchOperationResult search(String indexName, String queryString)
      throws DocumentStoreOperationException {
    SearchOperationResult opResult = new SearchOperationResult();

    // Initialize operation result with a failure codes / fault string
    opResult.setResultCode(500);
    opResult.setResult(INTERNAL_SERVER_ERROR_ELASTIC_SEARCH_OPERATION_FAULT);

    String fullUrl = getFullUrl("/" + indexName + "/_search" + "?" + queryString, false);

    // Grab the current time so we can use it to generate a metrics log.
    MdcOverride override = getStartTime(new MdcOverride());

    HttpURLConnection conn = initializeConnection(fullUrl);

    try {
      conn.setRequestMethod("GET");
    } catch (ProtocolException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to set HTTP request method to GET.", e);
    }

    logger.debug("\nsearch(), Sending 'GET' request to URL : " + conn.getURL());

    handleResponse(conn, opResult);
    buildSearchResult(opResult, indexName);


    metricsLogger.info(SearchDbMsgs.QUERY_DOCUMENT_TIME,
        new LogFields()
            .setField(LogLine.DefinedFields.RESPONSE_CODE, opResult.getResultCode())
            .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, opResult.getResult()),
        override,
        indexName,
        queryString);

    return opResult;
  }

  public SearchOperationResult searchWithPayload(String indexName, String query)
      throws DocumentStoreOperationException {
    SearchOperationResult opResult = new SearchOperationResult();

    if (logger.isDebugEnabled()) {
      logger.debug("Querying index: " + indexName + " with query string: " + query);
    }

    // Initialize operation result with a failure codes / fault string
    opResult.setResultCode(500);
    opResult.setResult(INTERNAL_SERVER_ERROR_ELASTIC_SEARCH_OPERATION_FAULT);

    String fullUrl = getFullUrl("/" + indexName + "/_search", false);

    // Grab the current time so we can use it to generate a metrics log.
    MdcOverride override = getStartTime(new MdcOverride());

    HttpURLConnection conn = initializeConnection(fullUrl);

    try {
      conn.setRequestMethod("POST");
    } catch (ProtocolException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to set HTTP request method to POST.", e);
    }

    attachContent(conn, query);

    logger.debug("\nsearch(), Sending 'POST' request to URL : " + conn.getURL());
    logger.debug("Request body =  Elasticsearch query = " + query);

    handleResponse(conn, opResult);
    buildSearchResult(opResult, indexName);

    metricsLogger.info(SearchDbMsgs.QUERY_DOCUMENT_TIME,
        new LogFields()
            .setField(LogLine.DefinedFields.RESPONSE_CODE, opResult.getResultCode())
            .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, opResult.getResult()),
        override,
        indexName,
        query);

    shutdownConnection(conn);

    return opResult;
  }


  public SearchOperationResult suggestionQueryWithPayload(String indexName, String query)
          throws DocumentStoreOperationException {

    SearchOperationResult opResult = new SearchOperationResult();

    if (logger.isDebugEnabled()) {
      logger.debug("Querying Suggestion index: " + indexName + " with query string: " + query);
    }

    // Initialize operation result with a failure codes / fault string
    opResult.setResultCode(500);
    opResult.setResult(INTERNAL_SERVER_ERROR_ELASTIC_SEARCH_OPERATION_FAULT);

    String fullUrl = getFullUrl("/" + indexName + "/_suggest", false);

    // Grab the current time so we can use it to generate a metrics log.
    MdcOverride override = getStartTime(new MdcOverride());

    HttpURLConnection conn = initializeConnection(fullUrl);

    try {
      conn.setRequestMethod("POST");
    } catch (ProtocolException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to set HTTP request method to POST.", e);
    }

    attachContent(conn, query);

    logger.debug("\nsearch(), Sending 'POST' request to URL : " + conn.getURL());
    logger.debug("Request body =  Elasticsearch query = " + query);

    handleResponse(conn, opResult);
    buildSuggestResult(opResult, indexName);

    metricsLogger.info(SearchDbMsgs.QUERY_DOCUMENT_TIME,
            new LogFields().setField(LogLine.DefinedFields.RESPONSE_CODE, opResult.getResultCode())
                    .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, opResult.getResult()),
            override, indexName, query);

    shutdownConnection(conn);

    return opResult;
  }

  private void attachContent(HttpURLConnection conn, String content)
      throws DocumentStoreOperationException {
    OutputStream outputStream = null;
    OutputStreamWriter out = null;

    try {
      outputStream = conn.getOutputStream();
    } catch (IOException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to get connection output stream.", e);
    }

    out = new OutputStreamWriter(outputStream);

    try {
      out.write(content);
      out.close();
    } catch (IOException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to write to the output stream.", e);
    }
  }

  private HttpURLConnection initializeConnection(String fullUrl)
      throws DocumentStoreOperationException {
    URL url = null;
    HttpURLConnection conn = null;

    try {
      url = new URL(fullUrl);
    } catch (MalformedURLException e) {
      throw new DocumentStoreOperationException("Error building a URL with " + url, e);
    }

    try {
      conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
    } catch (IOException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to open connection to URL " + url, e);
    }

    return conn;
  }

  private void handleResponse(HttpURLConnection conn, OperationResult opResult)
      throws DocumentStoreOperationException {
    int resultCode = 200;

    try {
      resultCode = conn.getResponseCode();
    } catch (IOException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to get the response code from the connection.", e);
    }

    logger.debug("Response Code : " + resultCode);

    InputStream inputStream = null;

    if (!(resultCode >= 200 && resultCode <= 299)) { // 2xx response indicates success
      inputStream = conn.getErrorStream();
    } else {
      try {
        inputStream = conn.getInputStream();
      } catch (IOException e) {
        shutdownConnection(conn);
        throw new DocumentStoreOperationException("Failed to get the response input stream.", e);
      }
    }

    InputStreamReader inputstreamreader = new InputStreamReader(inputStream);
    BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

    StringBuilder result = new StringBuilder(128);
    String string = null;

    try {
      while ((string = bufferedreader.readLine()) != null) {
        result.append(string).append("\n");
      }
    } catch (IOException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed getting the response body payload.", e);
    }

    if (resultCode == Status.CONFLICT.getStatusCode()) {
      opResult.setResultCode(Status.PRECONDITION_FAILED.getStatusCode());
    } else {
      opResult.setResultCode(resultCode);
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Raw result string from ElasticSearch = " + result.toString());
    }
    opResult.setResult(result.toString());
    opResult.setResultVersion(extractVersion(result.toString()));
  }

  private String extractVersion(String result) throws DocumentStoreOperationException {

    JSONParser parser = new JSONParser();
    String version = null;
    try {
      JSONObject root = (JSONObject) parser.parse(result);
      if (root.get("_version") != null) {
        version = root.get("_version").toString();
      }

    } catch (ParseException e) {

      // Not all responses from ElasticSearch include a version, so
      // if we don't get one back, just return an empty string rather
      // than trigger a false failure.
      version = "";
    }
    return version;
  }

  /**
   * This convenience method gets the current system time and stores
   * it in an attribute in the supplied {@link MdcOverride} object so
   * that it can be used later by the metrics logger.
   *
   * @param override - The {@link MdcOverride} object to update.
   * @return - The supplied {@link MdcOverride} object.
   */
  private MdcOverride getStartTime(MdcOverride override) {

    // Grab the current time...
    long startTimeInMs = System.currentTimeMillis();

    // ...and add it as an attribute to the supplied MDC Override
    // object.
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    override.addAttribute(MdcContext.MDC_START_TIME, formatter.format(startTimeInMs));

    // Return the MdcOverride object that we were passed.
    // This looks odd, but it allows us to do stuff like:
    //
    //    MdcOverride ov = getStartTime(new MdcOverride())
    //
    // which is quite handy, but also allows us to pass in an existing
    // MdcOverride object which already has some attributes set.
    return override;
  }

  private boolean isSuccess(OperationResult result) {

    return isSuccessCode(result.getResultCode());
  }


  private boolean isSuccessCode(int statusCode) {
    return ((statusCode >= 200) && (statusCode < 300));
  }


  @Override
  public OperationResult performBulkOperations(BulkRequest[] requests)
      throws DocumentStoreOperationException {

    if (logger.isDebugEnabled()) {
      String dbgString = "ESController: performBulkOperations - Operations: ";

      for (BulkRequest request : requests) {
        dbgString += "[" + request.toString() + "] ";
      }

      logger.debug(dbgString);
    }

    // Grab the current time so we can use it to generate a metrics log.
    MdcOverride override = getStartTime(new MdcOverride());

    // Parse the supplied set of operations.
    // Iterate over the list of operations which we were provided and
    // translate them into a format that ElasticSearh understands.
    int opCount = 0;
    StringBuilder esOperationSet = new StringBuilder(128);
    List<ElasticSearchResultItem> rejected = new ArrayList<ElasticSearchResultItem>();
    for (BulkRequest request : requests) {

      // Convert the request to the syntax ElasticSearch likes.
      if (buildEsOperation(request, esOperationSet, rejected)) {
        opCount++;
      }
    }

    ElasticSearchBulkOperationResult opResult = null;
    if (opCount > 0) {

      // Open an HTTP connection to the ElasticSearch back end.
      String fullUrl = getFullUrl("/_bulk", false);
      URL url;
      HttpURLConnection conn;
      try {

        url = new URL(fullUrl);
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Connection", "Close");

      } catch (IOException e) {

        logger.warn(SearchDbMsgs.BULK_OPERATION_FAILURE, e.getMessage());
        if (logger.isDebugEnabled()) {
          logger.debug(Throwables.getStackTraceAsString(e));
        }

        throw new DocumentStoreOperationException("Failed to open connection to document store.  Cause: "
            + e.getMessage(), e);
      }

      StringBuilder bulkResult = new StringBuilder(128);
      try {
        // Create an output stream to write our request to.
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        ;

        if (logger.isDebugEnabled()) {
          logger.debug("ESController: Sending 'BULK' request to " + conn.getURL());
          logger.debug("ESController: operations: " + esOperationSet.toString().replaceAll("\n",
              "\\n"));
        }

        // Write the resulting request string to our output stream. (this sends the request to ES?)
        out.write(esOperationSet.toString());
        out.close();

        // Open an input stream on our connection in order to read back the results.
        InputStream is = conn.getInputStream();
        InputStreamReader inputstreamreader = new InputStreamReader(is);
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

        // Read the contents of the input stream into our result string...
        String esResponseString = null;

        while ((esResponseString = bufferedreader.readLine()) != null) {
          bulkResult.append(esResponseString).append("\n");
        }

      } catch (IOException e) {

        logger.warn(SearchDbMsgs.BULK_OPERATION_FAILURE, e.getMessage());
        if (logger.isDebugEnabled()) {
          StringWriter sw = new StringWriter();
          e.printStackTrace(new PrintWriter(sw));
          logger.debug(sw.toString());
        }

        throw new DocumentStoreOperationException("Failure interacting with document store.  Cause: "
            + e.getMessage(), e);
      }

      if (logger.isDebugEnabled()) {
        logger.debug("ESController: Received result string from ElasticSearch: = "
            + bulkResult.toString());
      }

      // ...and marshal the resulting string into a Java object.
      try {
        opResult = marshallEsBulkResult(bulkResult.toString());

      } catch (IOException e) {

        logger.warn(SearchDbMsgs.BULK_OPERATION_FAILURE, e.getMessage());
        if (logger.isDebugEnabled()) {
          logger.debug(Throwables.getStackTraceAsString(e));
        }

        throw new DocumentStoreOperationException("Failed to marshal response body.  Cause: "
            + e.getMessage(), e);
      }
    }

    // Finally, build the operation result and return it to the caller.
    OperationResult result = new OperationResult();
    result.setResultCode(207);
    result.setResult(buildGenericBulkResultSet(opResult, rejected));

    // In the success case we don't want the entire result string to be
    // dumped into the metrics log, so concatenate it.
    String resultStringForMetricsLog = result.getResult();
    if ((result.getResultCode() >= 200) && (result.getResultCode() < 300)) {
      resultStringForMetricsLog = resultStringForMetricsLog.substring(0,
          Math.max(resultStringForMetricsLog.length(), 85)) + "...";
    }

    metricsLogger.info(SearchDbMsgs.BULK_OPERATIONS_TIME,
        new LogFields()
            .setField(LogLine.DefinedFields.RESPONSE_CODE, result.getResultCode())
            .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, resultStringForMetricsLog),
        override);

    return result;
  }


  /**
   * This method converts a {@link BulkRequest} object into a json structure
   * which can be understood by ElasticSearch.
   *
   * @param request - The request to be performed.
   * @param sb      - The string builder to append the json data to
   * @throws DocumentStoreOperationException
   */
  private boolean buildEsOperation(BulkRequest request, StringBuilder sb,
                                   List<ElasticSearchResultItem> fails)
      throws DocumentStoreOperationException {

    boolean retVal = true;
    OperationResult indexExistsResult = null;

    // What kind of operation are we performing?
    switch (request.getOperationType()) {

      // Create a new document.
      case CREATE:

        // Make sure that we were supplied a document payload.
        if (request.getOperation().getDocument() == null) {

          fails.add(generateRejectionEntry(request.getOperationType(),
              "Missing document payload",
              request.getIndex(),
              request.getId(),
              400,
              request.getOperation().getMetaData().getUrl()));
          return false;
        }

        // Make sure that the supplied document URL is formatted
        // correctly.
        if (!ApiUtils.validateDocumentUri(request.getOperation().getMetaData().getUrl(), false)) {
          fails.add(generateRejectionEntry(request.getOperationType(),
              "Invalid document URL: " + request.getOperation().getMetaData().getUrl(),
              request.getIndex(),
              "",
              400,
              request.getOperation().getMetaData().getUrl()));
          return false;
        }

        // Validate that the specified index actually exists before we
        // try to perform the create.
        if (!indexExists(ApiUtils.extractIndexFromUri(request.getOperation().getMetaData().getUrl()))) {

          fails.add(generateRejectionEntry(request.getOperationType(),
              "Specified resource does not exist: "
                  + request.getOperation().getMetaData().getUrl(),
              request.getIndex(),
              request.getId(),
              404,
              request.getOperation().getMetaData().getUrl()));
          return false;
        }

        // If we were supplied an id for the new document, then
        // include it in the bulk operation to Elastic Search
        if (request.getId() == null) {

          sb.append(String.format(BULK_CREATE_WITHOUT_INDEX_TEMPLATE,
              request.getIndex(),
              DEFAULT_TYPE));

          // Otherwise, we just leave that parameter off and ElasticSearch
          // will generate one for us.
        } else {
          sb.append(String.format(BULK_CREATE_WITH_INDEX_TEMPLATE,
              request.getIndex(),
              DEFAULT_TYPE,
              request.getId()));
        }

        try {
          // Append the document that we want to create.
          sb.append(request.getOperation().getDocument().toJson()).append("\n");
        } catch (JsonProcessingException e) {
          throw new DocumentStoreOperationException("Failure parsing document to json", e);
        }

        break;

      // Update an existing document.
      case UPDATE:

        // Make sure that we were supplied a document payload.
        if (request.getOperation().getDocument() == null) {

          fails.add(generateRejectionEntry(request.getOperationType(),
              "Missing document payload",
              request.getIndex(),
              request.getId(),
              400,
              request.getOperation().getMetaData().getUrl()));
          return false;
        }

        // Make sure that the supplied document URL is formatted
        // correctly.
        if (!ApiUtils.validateDocumentUri(request.getOperation().getMetaData().getUrl(), true)) {
          fails.add(generateRejectionEntry(request.getOperationType(),
              "Invalid document URL: " + request.getOperation().getMetaData().getUrl(),
              request.getIndex(),
              "",
              400,
              request.getOperation().getMetaData().getUrl()));
          return false;
        }

        // Validate that the specified index actually exists before we
        // try to perform the update.
        if (!indexExists(request.getIndex())) {

          fails.add(generateRejectionEntry(request.getOperationType(),
              "Specified resource does not exist: "
                  + request.getOperation().getMetaData().getUrl(),
              request.getIndex(),
              request.getId(),
              404,
              request.getOperation().getMetaData().getUrl()));
          return false;
        }

        // Validate that the document we are trying to update actually
        // exists before we try to perform the update.
        if (!documentExists(request.getIndex(), request.getId())) {

          fails.add(generateRejectionEntry(request.getOperationType(),
              "Specified resource does not exist: "
                  + request.getOperation().getMetaData().getUrl(),
              request.getIndex(),
              request.getId(),
              404,
              request.getOperation().getMetaData().getUrl()));
          return false;
        }

        // It is mandatory that a version be supplied for an update operation,
        // so validate that now.
        if (request.getOperation().getMetaData().getEtag() == null) {

          fails.add(generateRejectionEntry(request.getOperationType(),
              "Missing mandatory ETag field",
              request.getIndex(),
              request.getId(),
              400,
              request.getOperation().getMetaData().getUrl()));
          return false;
        }

        // Generate the update request...
        sb.append(String.format(BULK_IMPORT_INDEX_TEMPLATE,
            request.getIndex(),
            DEFAULT_TYPE,
            request.getId(),
            request.getOperation().getMetaData().getEtag()));

        // ...and append the document that we want to update.
        try {
          sb.append(request.getOperation().getDocument().toJson()).append("\n");
        } catch (JsonProcessingException e) {
          throw new DocumentStoreOperationException("Failure parsing document to json", e);
        }
        break;

      // Delete an existing document.
      case DELETE:

        // Make sure that the supplied document URL is formatted
        // correctly.
        if (!ApiUtils.validateDocumentUri(request.getOperation().getMetaData().getUrl(), true)) {
          fails.add(generateRejectionEntry(request.getOperationType(),
              "Invalid document URL: " + request.getOperation().getMetaData().getUrl(),
              request.getIndex(),
              "",
              400,
              request.getOperation().getMetaData().getUrl()));
          return false;
        }

        // Validate that the specified index actually exists before we
        // try to perform the delete.
        if (!indexExists(request.getIndex())) {

          fails.add(generateRejectionEntry(request.getOperationType(),
              "Specified resource does not exist: "
                  + request.getOperation().getMetaData().getUrl(),
              request.getIndex(),
              request.getId(),
              404,
              request.getOperation().getMetaData().getUrl()));
          return false;
        }

        // Validate that the document we are trying to update actually
        // exists before we try to perform the delete.
        if (!documentExists(request.getIndex(), request.getId())) {

          fails.add(generateRejectionEntry(request.getOperationType(),
              "Specified resource does not exist: "
                  + request.getOperation().getMetaData().getUrl(),
              request.getIndex(),
              request.getId(),
              404,
              request.getOperation().getMetaData().getUrl()));
          return false;
        }

        // It is mandatory that a version be supplied for a delete operation,
        // so validate that now.
        if (request.getOperation().getMetaData().getEtag() == null) {

          fails.add(generateRejectionEntry(request.getOperationType(),
              "Missing mandatory ETag field",
              request.getIndex(),
              request.getId(),
              400,
              request.getOperation().getMetaData().getUrl()));
          return false;
        }

        // Generate the delete request.
        sb.append(String.format(BULK_DELETE_TEMPLATE,
            request.getIndex(),
            DEFAULT_TYPE,
            request.getId(),
            request.getOperation().getMetaData().getEtag()));
        break;
      default:
    }

    return retVal;
  }

  private boolean indexExists(String index) throws DocumentStoreOperationException {

    OperationResult indexExistsResult = checkIndexExistence(index);

    return ((indexExistsResult.getResultCode() >= 200)
        && (indexExistsResult.getResultCode() < 300));
  }

  private boolean documentExists(String index, String id) throws DocumentStoreOperationException {

    OperationResult docExistsResult = checkDocumentExistence(index, id);

    return ((docExistsResult.getResultCode() >= 200) && (docExistsResult.getResultCode() < 300));
  }

  /**
   * This method constructs a status entry for a bulk operation which has
   * been rejected before even sending it to the document store.
   *
   * @param rejectReason - A message describing why the operation was rejected.
   * @param anId         - The identifier associated with the document being
   *                     acted on.
   * @param statusCode  - An HTTP status code.
   * @return - A result set item.
   */
  private ElasticSearchResultItem generateRejectionEntry(OperationType opType,
                                                         String rejectReason,
                                                         String index,
                                                         String anId,
                                                         int statusCode,
                                                         String originalUrl) {

    ElasticSearchError err = new ElasticSearchError();
    err.setReason(rejectReason);

    ElasticSearchOperationStatus op = new ElasticSearchOperationStatus();
    op.setIndex(index);
    op.setId(anId);
    op.setStatus(statusCode);
    op.setError(err);
    op.setAdditionalProperties(ElasticSearchResultItem.REQUEST_URL, originalUrl);

    ElasticSearchResultItem rejectionResult = new ElasticSearchResultItem();

    switch (opType) {
      case CREATE:
        rejectionResult.setCreate(op);
        break;
      case UPDATE:
        rejectionResult.setIndex(op);
        break;
      case DELETE:
        rejectionResult.setDelete(op);
        break;
      default:
    }

    return rejectionResult;
  }


  /**
   * This method takes the json structure returned from ElasticSearch in
   * response to a bulk operations request and marshals it into a Java
   * object.
   *
   * @param jsonResult - The bulk operations response returned from
   *                   ElasticSearch.
   * @return - The marshalled response.
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  private ElasticSearchBulkOperationResult marshallEsBulkResult(String jsonResult)
      throws JsonParseException, JsonMappingException, IOException {

    if (jsonResult != null) {
      if (logger.isDebugEnabled()) {
        logger.debug("ESController: Marshalling ES result set from json: "
            + jsonResult.replaceAll("\n", ""));
      }

      ObjectMapper mapper = new ObjectMapper();
      mapper.setSerializationInclusion(Include.NON_EMPTY);

      return mapper.readValue(jsonResult, ElasticSearchBulkOperationResult.class);
    }

    return null;
  }


  /**
   * This method takes the marshalled ElasticSearch bulk response and
   * converts it into a generic response payload.
   *
   * @param esResult - ElasticSearch bulk operations response.
   * @return - A generic result set.
   */
  private String buildGenericBulkResultSet(ElasticSearchBulkOperationResult esResult,
                                           List<ElasticSearchResultItem> rejectedOps) {

    int totalOps = 0;
    int totalSuccess = 0;
    int totalFails = 0;

    if (logger.isDebugEnabled()) {

      logger.debug("ESController: Build generic result set.  ES Results: "
          + ((esResult != null) ? esResult.toString() : "[]")
          + " Rejected Ops: " + rejectedOps.toString());
    }

    // Build a combined list of result items from the results returned
    // from ElasticSearch and the list of operations that we rejected
    // without sending to ElasticSearch.
    List<ElasticSearchResultItem> combinedResults = new ArrayList<ElasticSearchResultItem>();
    if (esResult != null) {
      combinedResults.addAll(Arrays.asList(esResult.getItems()));
    }
    combinedResults.addAll(rejectedOps);

    // Iterate over the individual results in the resulting result set.
    StringBuilder resultsBuilder = new StringBuilder();
    AtomicBoolean firstItem = new AtomicBoolean(true);
    for (ElasticSearchResultItem item : combinedResults) {

      // Increment the operation counts.
      totalOps++;
      if (isSuccessCode(item.operationStatus().getStatus())) {
        totalSuccess++;
      } else {
        totalFails++;
      }

      // Prepend a comma to our response string unless this it the
      // first result in the set.
      if (!firstItem.compareAndSet(true, false)) {
        resultsBuilder.append(", ");
      }

      // Append the current result as a generic json structure.
      resultsBuilder.append(item.toJson());
    }

    // Now, build the result string and return it.
    String responseBody = "{ \"total_operations\": " + totalOps + ", "
        + "\"total_success\": " + totalSuccess + ", "
        + "\"total_fails\": " + totalFails + ", "
        + "\"results\": ["
        + resultsBuilder.toString()
        + "]}";

    return responseBody;
  }


  /**
   * This method queryies ElasticSearch to determine if the supplied
   * index is present in the document store.
   *
   * @param indexName - The index to look for.
   * @return - An operation result indicating the success or failure of
   * the check.
   * @throws DocumentStoreOperationException
   */
  public OperationResult checkIndexExistence(String indexName)
      throws DocumentStoreOperationException {

    // Initialize operation result with a failure codes / fault string
    OperationResult opResult = new OperationResult();
    opResult.setResultCode(500);

    // Grab the current time so we can use it to generate a metrics log.
    MdcOverride override = getStartTime(new MdcOverride());

    String fullUrl = getFullUrl("/" + indexName, false);
    HttpURLConnection conn = initializeConnection(fullUrl);

    try {
      conn.setRequestMethod("HEAD");

    } catch (ProtocolException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to set HTTP request method to HEAD.", e);
    }

    logger.debug("Sending 'HEAD' request to: " + conn.getURL());

    int resultCode;
    try {
      resultCode = conn.getResponseCode();
    } catch (IOException e) {
      shutdownConnection(conn);
      throw new DocumentStoreOperationException("Failed to get the response code from the connection.", e);
    }
    logger.debug("Response Code : " + resultCode);

    opResult.setResultCode(resultCode);

    // Generate a metrics log so we can track how long the operation took.
    metricsLogger.info(SearchDbMsgs.CHECK_INDEX_TIME,
        new LogFields()
            .setField(LogLine.DefinedFields.RESPONSE_CODE, opResult.getResultCode())
            .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, opResult.getResultCode()),
        override,
        indexName);

    shutdownConnection(conn);

    return opResult;
  }


  private void buildDocumentResult(DocumentOperationResult result, String index)
      throws DocumentStoreOperationException {

    JSONParser parser = new JSONParser();
    JSONObject root;
    try {
      root = (JSONObject) parser.parse(result.getResult());

      if (result.getResultCode() >= 200 && result.getResultCode() <= 299) {
        // Success response object
        Document doc = new Document();
        doc.setEtag(result.getResultVersion());
        doc.setUrl(buildDocumentResponseUrl(index, root.get("_id").toString()));

        doc.setContent((JSONObject) root.get("_source"));
        result.setDocument(doc);

      } else {
        // Error response object
        JSONObject error = (JSONObject) root.get("error");
        if (error != null) {
          result.setError(new ErrorResult(error.get("type").toString(),
              error.get("reason").toString()));
        }

      }
    } catch (Exception e) {
      throw new DocumentStoreOperationException("Failed to parse Elastic Search response."
          + result.getResult());
    }


  }

  private String buildDocumentResponseUrl(String index, String id) {
    return ApiUtils.buildDocumentUri(index, id);
  }

  private void buildSearchResult(SearchOperationResult result, String index)
      throws DocumentStoreOperationException {

    JSONParser parser = new JSONParser();
    JSONObject root;

    try {
      root = (JSONObject) parser.parse(result.getResult());
      if (result.getResultCode() >= 200 && result.getResultCode() <= 299) {
        JSONObject hits = (JSONObject) root.get("hits");
        JSONArray hitArray = (JSONArray) hits.get("hits");
        SearchHits searchHits = new SearchHits();
        searchHits.setTotalHits(hits.get("total").toString());
        ArrayList<SearchHit> searchHitArray = new ArrayList<SearchHit>();

        for (int i = 0; i < hitArray.size(); i++) {
          JSONObject hit = (JSONObject) hitArray.get(i);
          SearchHit searchHit = new SearchHit();
          searchHit.setScore((hit.get("_score") != null) ? hit.get("_score").toString() : "");
          Document doc = new Document();
          if (hit.get("_version") != null) {
            doc.setEtag((hit.get("_version") != null) ? hit.get("_version").toString() : "");
          }

          doc.setUrl(buildDocumentResponseUrl(index, (hit.get("_id") != null)
              ? hit.get("_id").toString() : ""));
          doc.setContent((JSONObject) hit.get("_source"));
          searchHit.setDocument(doc);
          searchHitArray.add(searchHit);
        }
        searchHits.setHits(searchHitArray.toArray(new SearchHit[searchHitArray.size()]));
        result.setSearchResult(searchHits);

        JSONObject aggregations = (JSONObject) root.get("aggregations");
        if (aggregations != null) {
          AggregationResult[] aggResults =
              AggregationParsingUtil.parseAggregationResults(aggregations);
          AggregationResults aggs = new AggregationResults();
          aggs.setAggregations(aggResults);
          result.setAggregationResult(aggs);
        }

        // success
      } else {
        JSONObject error = (JSONObject) root.get("error");
        if (error != null) {
          result.setError(new ErrorResult(error.get("type").toString(),
              error.get("reason").toString()));
        }
      }
    } catch (Exception e) {
      throw new DocumentStoreOperationException("Failed to parse Elastic Search response."
          + result.getResult());
    }

  }

  private void buildSuggestResult(SearchOperationResult result, String index)
          throws DocumentStoreOperationException {

    JSONParser parser = new JSONParser ();
    JSONObject root;
    try {
      root = (JSONObject) parser.parse ( result.getResult () );
      if (result.getResultCode () >= 200 && result.getResultCode () <= 299) {
        JSONArray hitArray = (JSONArray) root.get ( "suggest-vnf" );
        JSONObject hitdata = (JSONObject) hitArray.get ( 0 );
        JSONArray optionsArray = (JSONArray) hitdata.get ( "options" );
        SuggestHits suggestHits = new SuggestHits ();
        suggestHits.setTotalHits ( String.valueOf ( optionsArray.size () ) );

        ArrayList<SuggestHit> suggestHitArray = new ArrayList<SuggestHit> ();

        for (int i = 0; i < optionsArray.size (); i++) {
          JSONObject hit = (JSONObject) optionsArray.get ( i );

          SuggestHit suggestHit = new SuggestHit ();
          suggestHit.setScore ( (hit.get ( "score" ) != null) ? hit.get ( "score" ).toString () : "" );
          suggestHit.setText ( (hit.get ( "text" ) != null) ? hit.get ( "text" ).toString () : "" );
          Document doc = new Document ();
          if (hit.get ( "_version" ) != null) {
            doc.setEtag ( (hit.get ( "_version" ) != null) ? hit.get ( "_version" ).toString () : "" );
          }
          doc.setUrl ( buildDocumentResponseUrl ( index,
                  (hit.get ( "_id" ) != null) ? hit.get ( "_id" ).toString () : "" ) );

          doc.setContent ( (JSONObject) hit.get ( "payload" ) );
          suggestHit.setDocument ( doc );
          suggestHitArray.add ( suggestHit );
        }
        suggestHits.setHits ( suggestHitArray.toArray ( new SuggestHit[suggestHitArray.size ()] ) );
        result.setSuggestResult ( suggestHits );

        JSONObject aggregations = (JSONObject) root.get ( "aggregations" );
        if (aggregations != null) {
          AggregationResult[] aggResults =
                  AggregationParsingUtil.parseAggregationResults ( aggregations );
          AggregationResults aggs = new AggregationResults ();
          aggs.setAggregations ( aggResults );
          result.setAggregationResult ( aggs );
        }

        // success
      } else {
        JSONObject error = (JSONObject) root.get ( "error" );
        if (error != null) {
          result.setError (
                  new ErrorResult ( error.get ( "type" ).toString (), error.get ( "reason" ).toString () ) );
        }
      }
    } catch (Exception e) {
      throw new DocumentStoreOperationException (
              "Failed to parse Elastic Search response." + result.getResult () );
    }
  }


  }
