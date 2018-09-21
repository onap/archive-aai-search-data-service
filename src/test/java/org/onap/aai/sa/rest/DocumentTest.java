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

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.DocumentStoreDataEntity;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.DocumentStoreInterface;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.exception.DocumentStoreOperationException;
import org.onap.aai.sa.searchdbabstraction.entity.DocumentOperationResult;
import org.onap.aai.sa.searchdbabstraction.entity.ErrorResult;
import org.onap.aai.sa.searchdbabstraction.entity.SearchHits;
import org.onap.aai.sa.searchdbabstraction.entity.SearchOperationResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class DocumentTest {

    @Mock
    SearchServiceApi searchServiceApi;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpHeaders headers;

    @Mock
    HttpServletResponse httpResponse;

    @Mock
    DocumentStoreInterface documentStore;

    @Mock
    MultivaluedMap<String, String> multivaluedMap;

    @InjectMocks
    IndexApi indexApi;

    DocumentApi documentApi;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        documentApi = new DocumentApi(searchServiceApi);
    }

    @Test
    public void testDocumentClass_AllMethods() throws JsonProcessingException {
        Document doc = new Document();
        doc.setField("name-1", "value-1");
        Assert.assertTrue(doc.getFields().size() == 1);
        Assert.assertTrue(doc.toJson().contains("value-1"));
        Assert.assertNotNull(doc.toString());
        Assert.assertTrue(doc.toString().contains("name-1"));
    }

    @Test
    public void testProcessPost_NullContent() {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = null;
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        ResponseEntity<String> response =
                documentApi.processPost(content, request, headers, httpResponse, "index", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.BAD_REQUEST.value() == response.getStatusCodeValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessPost_NotNullContent() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenThrow(IllegalArgumentException.class);
        ResponseEntity<?> response =
                documentApi.processPost(content, request, headers, httpResponse, "index", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FORBIDDEN.value() == response.getStatusCodeValue());
    }

    //
    @Test
    public void testProcessPost_ValidRequest() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        DocumentOperationResult result = new DocumentOperationResult();
        result.setResultCode(150);
        result.setError(new ErrorResult("type-1", "reason-1"));
        result.setFailureCause("test-failure");
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(documentStore.createDocument(Mockito.anyString(), Mockito.any(DocumentStoreDataEntity.class),
                Mockito.anyBoolean())).thenReturn(result);
        Mockito.doNothing().when(httpResponse).setHeader(Mockito.anyString(), Mockito.anyString());
        ResponseEntity<String> response =
                documentApi.processPost(content, request, headers, httpResponse, "index", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.INTERNAL_SERVER_ERROR.value() == response.getStatusCodeValue());
    }

    //
    @Test
    public void testProcessSearchWithGet_Created() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        SearchOperationResult result = new SearchOperationResult();
        result.setResultCode(201);
        SearchHits hits = new SearchHits();
        hits.setTotalHits("2");
        result.setSearchResult(hits);
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(documentStore.search(Mockito.anyString(), Mockito.anyString())).thenReturn(result);
        ResponseEntity<String> response =
                documentApi.processSearchWithGet(content, request, headers, "index-1", "query-text", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.CREATED.value() == response.getStatusCodeValue());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessSearchWithGet_ValidateThrowsException() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        SearchOperationResult result = new SearchOperationResult();
        result.setResultCode(201);
        SearchHits hits = new SearchHits();
        hits.setTotalHits("2");
        result.setSearchResult(hits);
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenThrow(IllegalArgumentException.class);
        Mockito.when(documentStore.search(Mockito.anyString(), Mockito.anyString())).thenReturn(result);
        ResponseEntity<String> response =
                documentApi.processSearchWithGet(content, request, headers, "index-1", "query-text", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FORBIDDEN.value() == response.getStatusCodeValue());

    }

    @Test
    public void testProcessSearchWithGet_ValidateIsFalse() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        SearchOperationResult result = new SearchOperationResult();
        result.setResultCode(201);
        SearchHits hits = new SearchHits();
        hits.setTotalHits("2");
        result.setSearchResult(hits);
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenReturn(false);
        Mockito.when(documentStore.search(Mockito.anyString(), Mockito.anyString())).thenReturn(result);
        ResponseEntity<String> response =
                documentApi.processSearchWithGet(content, request, headers, "index-1", "query-text", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FORBIDDEN.value() == response.getStatusCodeValue());

    }

    @Test
    public void testProcessSearchWithGet_InvalidResult() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        SearchOperationResult result = new SearchOperationResult();
        result.setResultCode(302);
        SearchHits hits = new SearchHits();
        hits.setTotalHits("2");
        result.setSearchResult(hits);
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(documentStore.search(Mockito.anyString(), Mockito.anyString())).thenReturn(result);
        ResponseEntity<String> response =
                documentApi.processSearchWithGet(content, request, headers, "index-1", "query-text", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FOUND.value() == response.getStatusCodeValue());

    }

    @Test
    public void testProcessPut_NullContent() {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = null;
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        ResponseEntity<String> response =
                documentApi.processPut(content, request, headers, httpResponse, "index", "id-1", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.BAD_REQUEST.value() == response.getStatusCodeValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessPut_RequestThrowsException() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenThrow(IllegalArgumentException.class);
        ResponseEntity<String> response =
                documentApi.processPut(content, request, headers, httpResponse, "index", "id-1", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FORBIDDEN.value() == response.getStatusCodeValue());
    }

    @Test
    public void testProcessPut_RequestInvalid() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenReturn(false);
        ResponseEntity<String> response =
                documentApi.processPut(content, request, headers, httpResponse, "index", "id-1", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FORBIDDEN.value() == response.getStatusCodeValue());
    }

    @Test
    public void testProcessPut_ResultInvalid() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        DocumentOperationResult result = new DocumentOperationResult();
        result.setResultCode(302);
        result.setError(new ErrorResult("type-1", "reason-1"));
        result.setFailureCause("test-failure");
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(documentStore.createDocument(Mockito.anyString(), Mockito.any(DocumentStoreDataEntity.class),
                Mockito.anyBoolean())).thenReturn(result);
        ResponseEntity<String> response =
                documentApi.processPut(content, request, headers, httpResponse, "index", "id-1", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FOUND.value() == response.getStatusCodeValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessDelete_RequestThrowsException() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenThrow(IllegalArgumentException.class);
        ResponseEntity<String> response =
                documentApi.processDelete(content, request, headers, httpResponse, "index", "id-1", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FORBIDDEN.value() == response.getStatusCodeValue());
    }

    @Test
    public void testProcessDelete_RequestInvalid() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenReturn(false);
        ResponseEntity<String> response =
                documentApi.processDelete(content, request, headers, httpResponse, "index", "id-1", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FORBIDDEN.value() == response.getStatusCodeValue());
    }

    @Ignore
    @Test
    public void testProcessDelete_ResultInvalid() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        DocumentOperationResult result = new DocumentOperationResult();
        result.setResultCode(302);
        result.setError(new ErrorResult("type-1", "reason-1"));
        result.setFailureCause("test-failure");
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(documentStore.deleteDocument(Mockito.anyString(), Mockito.any(DocumentStoreDataEntity.class)))
                .thenReturn(result);
        ResponseEntity<String> response =
                documentApi.processDelete(content, request, headers, httpResponse, "index", "id-1", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FOUND.value() == response.getStatusCodeValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessGet_RequestThrowsException() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenThrow(IllegalArgumentException.class);
        ResponseEntity<String> response =
                documentApi.processGet(content, request, headers, httpResponse, "index", "id-1", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FORBIDDEN.value() == response.getStatusCodeValue());
    }

    @Test
    public void testProcessGet_RequestInvalid() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenReturn(false);
        ResponseEntity<String> response =
                documentApi.processGet(content, request, headers, httpResponse, "index", "id-1", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FORBIDDEN.value() == response.getStatusCodeValue());
    }

    @Test
    public void testProcessGet_ResultInvalid() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        DocumentOperationResult result = new DocumentOperationResult();
        result.setResultCode(302);
        result.setError(new ErrorResult("type-1", "reason-1"));
        result.setFailureCause("test-failure");
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(documentStore.getDocument(Mockito.anyString(), Mockito.any(DocumentStoreDataEntity.class)))
                .thenReturn(result);
        ResponseEntity<String> response =
                documentApi.processGet(content, request, headers, httpResponse, "index", "id-1", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FOUND.value() == response.getStatusCodeValue());
    }

    @Test
    public void testQueryWithGetWithPayload_NullContent() {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = null;
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        ResponseEntity<String> response =
                documentApi.queryWithGetWithPayload(content, request, headers, "index-1", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.BAD_REQUEST.value() == response.getStatusCodeValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testQueryWithGetWithPayload_RequestThrowsException() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenThrow(IllegalArgumentException.class);
        ResponseEntity<String> response =
                documentApi.queryWithGetWithPayload(content, request, headers, "index-1", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FORBIDDEN.value() == response.getStatusCodeValue());
    }

    @Test
    public void testQueryWithGetWithPayload_RequestInvalid() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String content = "content";
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenReturn(false);
        ResponseEntity<String> response =
                documentApi.queryWithGetWithPayload(content, request, headers, "index-1", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FORBIDDEN.value() == response.getStatusCodeValue());
    }

    @Test
    public void testCreateProcessIndex_IndexApi_RequestInvalid() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenReturn(false);
        ResponseEntity<String> response =
                indexApi.processCreateIndex("document-1", request, headers, "index-1", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FORBIDDEN.value() == response.getStatusCodeValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateProcessIndex_IndexApi_RequestThrowsException() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenThrow(IllegalArgumentException.class);
        ResponseEntity<String> response =
                indexApi.processCreateIndex("document-1", request, headers, "index-1", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FORBIDDEN.value() == response.getStatusCodeValue());
    }

    @Test
    public void testCreateProcessIndex_IndexApi_NullDocument() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        String documentSchema = null;
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenReturn(true);
        ResponseEntity<String> response =
                indexApi.processCreateIndex(documentSchema, request, headers, "index-1", documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.INTERNAL_SERVER_ERROR.value() == response.getStatusCodeValue());
    }

    @Test
    public void testProcessDelete_IndexApi_RequestInvalid() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenReturn(false);
        ResponseEntity<String> response = indexApi.processDelete("document-1", request, headers, documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FORBIDDEN.value() == response.getStatusCodeValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessDelete_IndexApi_RequestThrowsException() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenThrow(IllegalArgumentException.class);
        ResponseEntity<String> response = indexApi.processDelete("document-1", request, headers, documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.FORBIDDEN.value() == response.getStatusCodeValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessDelete_IndexApi_DeleteIndexException() throws Exception {
        String transactionId = "transactionId-1";
        String remoteAddr = "http://127.0.0.1";
        // Mockito.when(headers.getRequestHeaders()).thenReturn(multivaluedMap);;
        Mockito.when(multivaluedMap.getFirst(Mockito.anyString())).thenReturn(transactionId);
        Mockito.when(request.getRemoteAddr()).thenReturn(remoteAddr);
        Mockito.when(request.getMethod()).thenReturn("testMethod");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1"));
        Mockito.when(request.getRemoteHost()).thenReturn("localhost");
        Mockito.when(searchServiceApi.validateRequest(Mockito.any(HttpHeaders.class),
                Mockito.any(HttpServletRequest.class), Mockito.any(ApiUtils.Action.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(documentStore.deleteIndex(Mockito.anyString())).thenThrow(DocumentStoreOperationException.class);
        ResponseEntity<String> response = indexApi.processDelete("document-1", request, headers, documentStore);
        Assert.assertNotNull(response);
        Assert.assertTrue(HttpStatus.INTERNAL_SERVER_ERROR.value() == response.getStatusCodeValue());
    }
}
