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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.onap.aai.sa.rest.ApiUtils;
import org.onap.aai.sa.rest.SearchServiceApi;

@Path("test/")
public class SearchServiceApiHarness extends SearchServiceApi {


  public static final String FAIL_AUTHENTICATION_TRIGGER = "FAIL AUTHENTICATION";

  private boolean authenticationShouldSucceed = true;


  /**
   * Performs all one-time initialization required for the end point.
   */
  @Override
  public void init() {

    // Instantiate our Document Store DAO.
    documentStore = new StubEsController();
  }


  @PUT
  @Path("/indexes/{index}")
  @Consumes({MediaType.APPLICATION_JSON})
  @Override
  public Response processCreateIndex(String requestBody,
                                     @Context HttpServletRequest request,
                                     @Context HttpHeaders headers,
                                     @PathParam("index") String index) {

    return super.processCreateIndex(requestBody, request, headers, index);
  }

  @PUT
  @Path("/indexes/dynamic/{index}")
  @Consumes({MediaType.APPLICATION_JSON})
  @Override
  public Response processCreateDynamicIndex(String requestBody,
                                     @Context HttpServletRequest request,
                                     @Context HttpHeaders headers,
                                     @PathParam("index") String index) {

    return super.processCreateDynamicIndex(requestBody, request, headers, index);
  }

  @DELETE
  @Path("/indexes/{index}")
  @Consumes({MediaType.APPLICATION_JSON})
  @Override
  public Response processDeleteIndex(String requestBody,
                                     @Context HttpServletRequest request,
                                     @Context HttpHeaders headers,
                                     @PathParam("index") String index) {

    return super.processDeleteIndex(requestBody, request, headers, index);
  }

  @GET
  @Path("/indexes/{index}/documents/{id}")
  @Consumes({MediaType.APPLICATION_JSON})
  @Override
  public Response processGetDocument(String requestBody,
                                     @Context HttpServletRequest request,
                                     @Context HttpServletResponse httpResponse,
                                     @Context HttpHeaders headers,
                                     @PathParam("index") String index,
                                     @PathParam("id") String id) {

    return super.processGetDocument(requestBody, request, httpResponse, headers, index, id);
  }

  @POST
  @Path("/indexes/{index}/documents")
  @Consumes({MediaType.APPLICATION_JSON})
  @Override
  public Response processCreateDocWithoutId(String requestBody,
                                            @Context HttpServletRequest request,
                                            @Context HttpServletResponse httpResponse,
                                            @Context HttpHeaders headers,
                                            @PathParam("index") String index) {

    return super.processCreateDocWithoutId(requestBody, request, httpResponse, headers, index);
  }

  @PUT
  @Path("/indexes/{index}/documents/{id}")
  @Consumes({MediaType.APPLICATION_JSON})
  @Override
  public Response processUpsertDoc(String requestBody,
                                   @Context HttpServletRequest request,
                                   @Context HttpServletResponse httpResponse,
                                   @Context HttpHeaders headers,
                                   @PathParam("index") String index,
                                   @PathParam("id") String id) {

    return super.processUpsertDoc(requestBody, request, httpResponse, headers, index, id);
  }

  @DELETE
  @Path("/indexes/{index}/documents/{id}")
  @Consumes({MediaType.APPLICATION_JSON})
  @Override
  public Response processDeleteDoc(String requestBody,
                                   @Context HttpServletRequest request,
                                   @Context HttpServletResponse httpResponse,
                                   @Context HttpHeaders headers,
                                   @PathParam("index") String index,
                                   @PathParam("id") String id) {

    return super.processDeleteDoc(requestBody, request, httpResponse, headers, index, id);
  }

  @GET
  @Path("/indexes/{index}/query/{queryText}")
  @Consumes({MediaType.APPLICATION_JSON})
  @Override
  public Response processInlineQuery(String requestBody,
                                     @Context HttpServletRequest request,
                                     @Context HttpHeaders headers,
                                     @PathParam("index") String index,
                                     @PathParam("queryText") String queryText) {

    return super.processInlineQuery(requestBody, request, headers, index, queryText);
  }

  @GET
  @Path("/indexes/{index}/query")
  @Consumes({MediaType.APPLICATION_JSON})
  @Override
  public Response processQueryWithGet(String requestBody,
                                      @Context HttpServletRequest request,
                                      @Context HttpHeaders headers,
                                      @PathParam("index") String index) {

    return super.processQueryWithGet(requestBody, request, headers, index);
  }

  @POST
  @Path("/indexes/{index}/query")
  @Consumes({MediaType.APPLICATION_JSON})
  @Override
  public Response processQuery(String requestBody,
                               @Context HttpServletRequest request,
                               @Context HttpHeaders headers,
                               @PathParam("index") String index) {

    return super.processQuery(requestBody, request, headers, index);
  }

  @POST
  @Path("/bulk")
  @Consumes({MediaType.APPLICATION_JSON})
  @Override
  public Response processBulkRequest(String requestBody,
                                     @Context HttpServletRequest request,
                                     @Context HttpHeaders headers,
                                     @PathParam("index") String index) {

    // If the operations string contains a special keyword, set the
    // harness to fail the authentication validation.
    if (requestBody.contains(FAIL_AUTHENTICATION_TRIGGER)) {
      authenticationShouldSucceed = false;
    }

    // Just pass the request up to the parent, since that is the code
    // that we really want to test.
    //return super.processPost(operations, request, headers, index);
    return super.processBulkRequest(requestBody, request, headers, index);
  }

  @Override
  protected boolean validateRequest(HttpHeaders headers,
                                    HttpServletRequest req,
                                    ApiUtils.Action action,
                                    String authPolicyFunctionName) throws Exception {

    return authenticationShouldSucceed;
  }
}
