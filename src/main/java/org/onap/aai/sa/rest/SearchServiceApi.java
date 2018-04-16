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

import org.onap.aai.sa.auth.SearchDbServiceAuth;
import org.onap.aai.sa.rest.ApiUtils.Action;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.DocumentStoreInterface;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.ElasticSearchHttpController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.cert.X509Certificate;

@Component
@EnableWebSecurity
@RestController
@RequestMapping("/services/search-data-service/v1/search")
public class SearchServiceApi {

  /**
   * The Data Access Object that we will use to interact with the
   * document store.
   */
  protected DocumentStoreInterface documentStore = null;
  protected ApiUtils apiUtils = null;

  /**
   * Create a new instance of the end point.
   */
  public SearchServiceApi() {

    // Perform one-time initialization.
    init();
  }


  /**
   * Performs all one-time initialization required for the end point.
   */
  public void init() {

    // Instantiate our Document Store DAO.
    documentStore = ElasticSearchHttpController.getInstance();

    apiUtils = new ApiUtils();
  }

  @RequestMapping(value = "/indexes/{index}",
                  method = RequestMethod.PUT,
                  produces = { "application/json" })
  public ResponseEntity<String> processCreateIndex(@RequestBody String requestBody,
                                                   HttpServletRequest request,
                                                   @RequestHeader HttpHeaders headers,
                                                   @PathVariable("index") String index) {

    // Forward the request to our index API to create the index.
    IndexApi indexApi = new IndexApi(this);
    return indexApi.processCreateIndex(requestBody, request, headers, index, documentStore);
  }

  @RequestMapping(value = "/indexes/{index}",
                  method = RequestMethod.DELETE,
                  consumes = {"application/json"},
                  produces = {"application/json"})
  public ResponseEntity<String> processDeleteIndex(HttpServletRequest request,
                                                   @RequestHeader HttpHeaders headers,
                                                   @PathVariable ("index") String index) {

    // Forward the request to our index API to delete the index.
    IndexApi indexApi = new IndexApi(this);
    return indexApi.processDelete(index, request, headers, documentStore);
  }


  @RequestMapping(value = "/indexes/{index}/documents",
                  method = RequestMethod.POST,
                  consumes = {"application/json"})
                  public ResponseEntity<String> processCreateDocWithoutId(@RequestBody String requestBody,
                                                                          HttpServletRequest request,
                                                                          HttpServletResponse httpResponse,
                                                                          @RequestHeader HttpHeaders headers,
                                                                          @PathVariable ("index") String index) {

    // Forward the request to our document API to create the document.
    DocumentApi documentApi = new DocumentApi(this);
    return documentApi.processPost(requestBody, request, headers, httpResponse,
                                   index, documentStore);
  }

  @RequestMapping(value = "/indexes/{index}/documents/{id}",
                  method = RequestMethod.PUT,
                  consumes = {"application/json"})
                  public ResponseEntity<String> processUpsertDoc(@RequestBody String requestBody,
                                                                 HttpServletRequest request,
                                                                 HttpServletResponse httpResponse,
                                                                 @RequestHeader HttpHeaders headers,
                                                                 @PathVariable ("index") String index,
                                                                 @PathVariable ("id") String id) {

    // Forward the request to our document API to upsert the document.
    DocumentApi documentApi = new DocumentApi(this);
    return documentApi.processPut(requestBody, request, headers, httpResponse,
                                  index, id, documentStore);
  }

  @RequestMapping(value = "/indexes/{index}/documents/{id}",
          method = RequestMethod.GET)
  public ResponseEntity<String> processGetDocument(HttpServletRequest request,
                                                   HttpServletResponse httpResponse,
                                                   @RequestHeader HttpHeaders headers,
                                                   @PathVariable ("index") String index,
                                                   @PathVariable ("id") String id) {

    // Forward the request to our document API to retrieve the document.
    DocumentApi documentApi = new DocumentApi(this);
    return documentApi.processGet("", request, headers, httpResponse,
            index, id, documentStore);
  }

  @RequestMapping(value = "/indexes/{index}/documents/{id}",
                  method = RequestMethod.DELETE,
                  consumes = {"application/json"})
  public ResponseEntity<String> processDeleteDoc(HttpServletRequest request,
                                                 HttpServletResponse httpResponse,
                                                 @RequestHeader HttpHeaders headers,
                                                 @PathVariable ("index") String index,
                                                 @PathVariable ("id") String id) {

    // Forward the request to our document API to delete the document.
    DocumentApi documentApi = new DocumentApi(this);
    return documentApi.processDelete("", request, headers, httpResponse,
                                     index, id, documentStore);
  }

  @RequestMapping(value = "/indexes/{index}/query/{queryText}",
                  method = RequestMethod.GET)
  public ResponseEntity<String> processInlineQuery(HttpServletRequest request,
                                                   @RequestHeader HttpHeaders headers,
                                                   @PathVariable ("index") String index,
                                                   @PathVariable ("queryText") String queryText) {

    // Forward the request to our document API to delete the document.
    DocumentApi documentApi = new DocumentApi(this);
    return documentApi.processSearchWithGet("", request, headers,
                                            index, queryText, documentStore);
  }

  @RequestMapping(value = "/indexes/{index}/query",
                  method = RequestMethod.GET,
                  consumes = {"application/json"})
  public ResponseEntity<String> processQueryWithGet(@RequestBody String requestBody,
                                                    HttpServletRequest request,
                                                    @RequestHeader HttpHeaders headers,
                                                    @PathVariable ("index") String index) {

    // Forward the request to our document API to delete the document.
    DocumentApi documentApi = new DocumentApi(this);
    return documentApi.queryWithGetWithPayload(requestBody, request, headers, index, documentStore);
  }

  @RequestMapping(value = "/indexes/{index}/query",
                  method = RequestMethod.POST,
                  consumes = {"application/json"})
  public ResponseEntity<String> processQuery(@RequestBody String requestBody,
                                                             HttpServletRequest request,
                                                             @RequestHeader HttpHeaders headers,
                                                             @PathVariable ("index") String index) {

    // Forward the request to our document API to delete the document.
    DocumentApi documentApi = new DocumentApi(this);
    return documentApi.processSearchWithPost(requestBody, request, headers, index, documentStore);
  }

  @RequestMapping(value = "/indexes/{index}/suggest",
          method = RequestMethod.POST,
          consumes = {"application/json"})
  public ResponseEntity<String> processSuggestQuery(@RequestBody String requestBody, HttpServletRequest request,
                                      @RequestHeader HttpHeaders headers, @PathVariable("index") String index) {
    // Forward the request to our document API to query suggestions in the
    // document.
    DocumentApi documentApi = new DocumentApi(this);
    return documentApi.processSuggestQueryWithPost(requestBody, request, headers, index,
            documentStore);
  }

  @RequestMapping(value = "/indexes/dynamic/{index}",
          method = RequestMethod.PUT,
          consumes = {"application/json"})
  public ResponseEntity<String> processCreateDynamicIndex(@RequestBody String requestBody,
                                                          HttpServletRequest request,
                                                          @RequestHeader HttpHeaders headers,
                                            @PathVariable ("index") String index) {

    // Forward the request to our index API to create the index.
    IndexApi indexApi = new IndexApi(this);
    return indexApi.processCreateDynamicIndex(requestBody, request, headers, index, documentStore);
  }

  @RequestMapping(value = "/bulk",
                  method = RequestMethod.POST,
                  consumes = {"application/json"},
                  produces = { "application/json"})
  public ResponseEntity<String> processBulkRequest(@RequestBody String requestBody,
                                                   HttpServletRequest request,
                                                   @RequestHeader HttpHeaders headers) {

    // Forward the request to our document API to delete the document.
    BulkApi bulkApi = new BulkApi(this);
    ResponseEntity<String> dbugResp = bulkApi.processPost(requestBody, request, headers, documentStore, apiUtils);
    return dbugResp;
  }

  protected boolean validateRequest(HttpHeaders headers,
                                    HttpServletRequest req,
                                    Action action,
                                    String authPolicyFunctionName) throws Exception {

    SearchDbServiceAuth serviceAuth = new SearchDbServiceAuth();

    String cipherSuite = (String) req.getAttribute("javax.servlet.request.cipher_suite");
    String authUser = null;
    if (cipherSuite != null) {
      Object x509CertAttribute = req.getAttribute("javax.servlet.request.X509Certificate");
      if (x509CertAttribute != null) {
        X509Certificate[] certChain = (X509Certificate[]) x509CertAttribute;
        X509Certificate clientCert = certChain[0];
        X500Principal subjectDn = clientCert.getSubjectX500Principal();
        authUser = subjectDn.toString();
      }
    }

    if (authUser == null) {
      return false;
    }

    String status = serviceAuth.authUser(headers, authUser.toLowerCase(),
                                         action.toString() + ":" + authPolicyFunctionName);
    if (!status.equals("OK")) {
      return false;
    }

    return true;
  }
}
