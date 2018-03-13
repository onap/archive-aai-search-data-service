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

import org.json.simple.JSONObject;
import org.onap.aai.sa.rest.AnalysisConfiguration;
import org.onap.aai.sa.rest.BulkRequest;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.DocumentStoreDataEntity;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.DocumentStoreInterface;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.exception.DocumentStoreOperationException;
import org.onap.aai.sa.searchdbabstraction.entity.*;
import org.onap.aai.sa.searchdbabstraction.util.DocumentSchemaUtil;
import org.onap.aai.sa.searchdbabstraction.entity.Document;
import org.onap.aai.sa.rest.DocumentSchema;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a stubbed version of the document store DAO so
 * that we can run unit tests without trying to connect to a real
 * document store.
 */
public class StubEsController implements DocumentStoreInterface {

  public static final String DOES_NOT_EXIST_INDEX = "index-does-not-exist";

  private AnalysisConfiguration analysisConfig = null;

  /**
   *
   */
  //private IndexAPIHarness indexAPIHarness;

  StubEsController() {
    analysisConfig = new AnalysisConfiguration();
    analysisConfig.init("src/test/resources/json/filter-config.json",
        "src/test/resources/json/analysis-config.json");
  }


  @Override
  public OperationResult createIndex(String         index, 
                                     DocumentSchema documentSchema) {

    // Just return an OK result, with the parameters that we were passed
    // bundled in the response string. This allows unit tests to validate
    // that those parameters match what they expected to be passed.
    OperationResult opResult = new OperationResult();
    opResult.setResultCode(200);

    opResult.setResult(index + "@" + analysisConfig.getEsIndexSettings() + "@"
        + DocumentSchemaUtil.generateDocumentMappings(documentSchema));

    return opResult;
  }


  @Override
  public OperationResult deleteIndex(String indexName) throws DocumentStoreOperationException {

    OperationResult opResult = new OperationResult();

    if (indexName.equals(DOES_NOT_EXIST_INDEX)) {
      opResult.setResultCode(404);
    } else {
      opResult.setResultCode(200);
      opResult.setResult(indexName);
    }

    return opResult;
  }

  @Override
  public DocumentOperationResult createDocument(String                  indexName,
                                                DocumentStoreDataEntity document,
                                                boolean                 allowImplicitIndexCreation) 
    throws DocumentStoreOperationException {
    
    DocumentOperationResult opResult = buildSampleDocumentOperationResult();

    if (indexName.equals(DOES_NOT_EXIST_INDEX)) {
      opResult.setResultCode(404);
    } else {
      opResult.setResultCode(200);
      String id = "dummy";
      if (document.getId() != null) {
        id = document.getId();
      }
      opResult.setResultVersion("1");
    }

    return opResult;
  }

  @Override
  public DocumentOperationResult updateDocument(String                  indexName,
                                                DocumentStoreDataEntity document,
                                                boolean                 allowImplicitIndexCreation) 
    throws DocumentStoreOperationException {
    
    DocumentOperationResult opResult = buildSampleDocumentOperationResult();

    if (indexName.equals(DOES_NOT_EXIST_INDEX)) {
      opResult.setResultCode(404);
    } else {
      opResult.setResultCode(200);
      String version = "1";
      if (document.getVersion() != null) {
        version = String.valueOf(Integer.parseInt(document.getVersion()) + 1);
      }
      opResult.setResultVersion(version);
    }

    return opResult;
  }

  @Override
  public DocumentOperationResult deleteDocument(String indexName,
                                                DocumentStoreDataEntity document) throws DocumentStoreOperationException {
    DocumentOperationResult opResult = buildSampleDocumentOperationResult();


    if (indexName.equals(DOES_NOT_EXIST_INDEX)) {
      opResult.setResultCode(404);
    } else {
      if (opResult.getDocument() != null) {
        opResult.getDocument().setEtag(null);
        opResult.getDocument().setUrl(null);
      }
      opResult.setResultCode(200);
      opResult.setResult(indexName + "@" + document.getId());
    }

    return opResult;
  }

  @Override
  public DocumentOperationResult getDocument(String indexName,
                                             DocumentStoreDataEntity document) throws DocumentStoreOperationException {
    DocumentOperationResult opResult = buildSampleDocumentOperationResult();

    if (indexName.equals(DOES_NOT_EXIST_INDEX)) {
      opResult.setResultCode(404);
    } else {
      opResult.setResultCode(200);
    }

    return opResult;
  }

  @Override
  public SearchOperationResult search(String indexName,
                                      String queryText) throws DocumentStoreOperationException {

    SearchOperationResult opResult = buildSampleSearchOperationResult();

    if (indexName.equals(DOES_NOT_EXIST_INDEX)) {
      opResult.setResultCode(404);
    } else {
      opResult.setResultCode(200);
      opResult.setResult(indexName + "@" + queryText);
    }

    return opResult;
  }

  @Override
  public SearchOperationResult searchWithPayload(String indexName,
                                                 String query) throws DocumentStoreOperationException {
    SearchOperationResult opResult = buildSampleSearchOperationResult();

    if (indexName.equals(DOES_NOT_EXIST_INDEX)) {
      opResult.setResultCode(404);
    } else {
      opResult.setResultCode(200);
      opResult.setResult(indexName + "@" + query);
    }

    return opResult;
  }

  @Override
  public OperationResult performBulkOperations(BulkRequest[] requests) throws DocumentStoreOperationException {

    OperationResult opResult = new OperationResult();
    opResult.setResultCode(200);

    return opResult;
  }

  private DocumentOperationResult buildSampleDocumentOperationResult() {
    DocumentOperationResult result = new DocumentOperationResult();
    Document doc = new Document();
    doc.setEtag("etag1");

    doc.setContent(new JSONObject());
    result.setDocument(doc);
    return result;
  }

  private SearchOperationResult buildSampleSearchOperationResult() {
    SearchOperationResult result = new SearchOperationResult();

    SearchHits searchHits = new SearchHits();
    SearchHit[] searchHitArray = new SearchHit[1];
    SearchHit searchHit = new SearchHit();
    Document doc = new Document();
    doc.setEtag("etag1");
    Map<String, Object> content = new HashMap<String, Object>();
    content.put("key1", "value1");
    doc.setContent(new JSONObject());
    searchHit.setDocument(doc);
    searchHitArray[0] = searchHit;

    searchHits.setHits(searchHitArray);
    searchHits.setTotalHits("1");
    result.setSearchResult(searchHits);

    return result;

  }

}