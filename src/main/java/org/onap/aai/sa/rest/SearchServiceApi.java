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

import org.onap.aai.sa.auth.SearchDbServiceAuth;
import org.onap.aai.sa.rest.ApiUtils.Action;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.DocumentStoreInterface;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.ElasticSearchHttpController;

import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


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

  @PUT
  @Path("/indexes/{index}")
  @Consumes({MediaType.APPLICATION_JSON})
  public Response processCreateIndex(String requestBody,
                                     @Context HttpServletRequest request,
                                     @Context HttpHeaders headers,
                                     @PathParam("index") String index) {

    // Forward the request to our index API to create the index.
    IndexApi indexApi = new IndexApi(this);
    return indexApi.processCreateIndex(requestBody, request, headers, index, documentStore);
  }


  @DELETE
  @Path("/indexes/{index}")
  @Consumes({MediaType.APPLICATION_JSON})
  public Response processDeleteIndex(String requestBody,
                                     @Context HttpServletRequest request,
                                     @Context HttpHeaders headers,
                                     @PathParam("index") String index) {

    // Forward the request to our index API to delete the index.
    IndexApi indexApi = new IndexApi(this);
    return indexApi.processDelete(index, request, headers, documentStore);
  }


  @GET
  @Path("/indexes/{index}/documents/{id}")
  @Consumes({MediaType.APPLICATION_JSON})
  public Response processGetDocument(String requestBody,
                                     @Context HttpServletRequest request,
                                     @Context HttpServletResponse httpResponse,
                                     @Context HttpHeaders headers,
                                     @PathParam("index") String index,
                                     @PathParam("id") String id) {

    // Forward the request to our document API to retrieve the document.
    DocumentApi documentApi = new DocumentApi(this);
    return documentApi.processGet(requestBody, request, headers, httpResponse,
        index, id, documentStore);
  }

  @POST
  @Path("/indexes/{index}/documents")
  @Consumes({MediaType.APPLICATION_JSON})
  public Response processCreateDocWithoutId(String requestBody,
                                            @Context HttpServletRequest request,
                                            @Context HttpServletResponse httpResponse,
                                            @Context HttpHeaders headers,
                                            @PathParam("index") String index) {

    // Forward the request to our document API to create the document.
    DocumentApi documentApi = new DocumentApi(this);
    return documentApi.processPost(requestBody, request, headers, httpResponse,
        index, documentStore);
  }

  @PUT
  @Path("/indexes/{index}/documents/{id}")
  @Consumes({MediaType.APPLICATION_JSON})
  public Response processUpsertDoc(String requestBody,
                                   @Context HttpServletRequest request,
                                   @Context HttpServletResponse httpResponse,
                                   @Context HttpHeaders headers,
                                   @PathParam("index") String index,
                                   @PathParam("id") String id) {

    // Forward the request to our document API to upsert the document.
    DocumentApi documentApi = new DocumentApi(this);
    return documentApi.processPut(requestBody, request, headers, httpResponse,
        index, id, documentStore);
  }

  @DELETE
  @Path("/indexes/{index}/documents/{id}")
  @Consumes({MediaType.APPLICATION_JSON})
  public Response processDeleteDoc(String requestBody,
                                   @Context HttpServletRequest request,
                                   @Context HttpServletResponse httpResponse,
                                   @Context HttpHeaders headers,
                                   @PathParam("index") String index,
                                   @PathParam("id") String id) {

    // Forward the request to our document API to delete the document.
    DocumentApi documentApi = new DocumentApi(this);
    return documentApi.processDelete(requestBody, request, headers, httpResponse,
        index, id, documentStore);
  }


  @GET
  @Path("/indexes/{index}/query/{queryText}")
  @Consumes({MediaType.APPLICATION_JSON})
  public Response processInlineQuery(String requestBody,
                                     @Context HttpServletRequest request,
                                     @Context HttpHeaders headers,
                                     @PathParam("index") String index,
                                     @PathParam("queryText") String queryText) {

    // Forward the request to our document API to delete the document.
    DocumentApi documentApi = new DocumentApi(this);
    return documentApi.processSearchWithGet(requestBody, request, headers,
        index, queryText, documentStore);
  }


  @GET
  @Path("/indexes/{index}/query")
  @Consumes({MediaType.APPLICATION_JSON})
  public Response processQueryWithGet(String requestBody,
                                      @Context HttpServletRequest request,
                                      @Context HttpHeaders headers,
                                      @PathParam("index") String index) {

    // Forward the request to our document API to delete the document.
    DocumentApi documentApi = new DocumentApi(this);
    return documentApi.queryWithGetWithPayload(requestBody, request, headers, index, documentStore);
  }

  @POST
  @Path("/indexes/{index}/query")
  @Consumes({MediaType.APPLICATION_JSON})
  public Response processQuery(String requestBody,
                               @Context HttpServletRequest request,
                               @Context HttpHeaders headers,
                               @PathParam("index") String index) {

    // Forward the request to our document API to delete the document.
    DocumentApi documentApi = new DocumentApi(this);
    return documentApi.processSearchWithPost(requestBody, request, headers, index, documentStore);
  }


  @POST
  @Path("/bulk")
  @Consumes({MediaType.APPLICATION_JSON})
  public Response processBulkRequest(String requestBody,
                                     @Context HttpServletRequest request,
                                     @Context HttpHeaders headers,
                                     @PathParam("index") String index) {

    // Forward the request to our document API to delete the document.
    BulkApi bulkApi = new BulkApi(this);
    return bulkApi.processPost(requestBody, request, headers, documentStore, apiUtils);
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
