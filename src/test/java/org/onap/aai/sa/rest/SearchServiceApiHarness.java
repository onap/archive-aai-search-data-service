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

 import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RestController
@RequestMapping("/test")
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

  @Override
  @RequestMapping (value="/indexes/dynamic/{index}",
          method = RequestMethod.PUT,
          consumes = { "application/json"})
  public ResponseEntity<String> processCreateDynamicIndex(@RequestBody String requestBody,
                                            HttpServletRequest request,
                                            @RequestHeader HttpHeaders headers,
                                            @PathVariable("index") String index) {

    return super.processCreateDynamicIndex(requestBody, request, headers, index);
  }


  @Override
  @RequestMapping (value="/indexes/{index}",
          method = RequestMethod.PUT,
          consumes = { "application/json"})
  public ResponseEntity<String> processCreateIndex(@RequestBody  String requestBody,
                                     HttpServletRequest request,
                                     @RequestHeader HttpHeaders headers,
                                     @PathVariable("index") String index) {

    return super.processCreateIndex(requestBody, request, headers, index);
  }

  @Override
  @RequestMapping (value="/indexes/{index}",
          method = RequestMethod.DELETE,
          consumes = { "application/json"})
  public ResponseEntity<String> processDeleteIndex(HttpServletRequest request,
                                     @RequestHeader HttpHeaders headers,
                                     @PathVariable("index") String index) {

    return super.processDeleteIndex(request, headers, index);
  }

  @Override
  @RequestMapping (value="/indexes/{index}/documents/{id}",
          method = RequestMethod.GET,
          produces = { "application/json"},
          consumes = { "application/json", "application/xml" })
  public ResponseEntity<String> processGetDocument(
          HttpServletRequest request,
          HttpServletResponse httpResponse,
          @RequestHeader HttpHeaders headers,
          @PathVariable("index") String index,
          @PathVariable("id") String id) {

    return super.processGetDocument(request, httpResponse, headers, index, id);
  }

  @Override
  @RequestMapping (value="/indexes/{index}/documents",
                   method = RequestMethod.POST,
                   consumes = { "application/json", "application/xml" })
  public ResponseEntity<String> processCreateDocWithoutId(@RequestBody String requestBody,
                                                          HttpServletRequest request,
                                                          HttpServletResponse httpResponse,
                                                          @RequestHeader HttpHeaders headers,
                                                          @PathVariable("index") String index) {

    return super.processCreateDocWithoutId(requestBody, request, httpResponse, headers, index);
  }

  @Override
  @RequestMapping (value="/indexes/{index}/documents/{id}",
          method = RequestMethod.PUT,
          consumes = { "application/json", "application/xml" })
  public ResponseEntity<String> processUpsertDoc(@RequestBody  String requestBody,
                                    HttpServletRequest request,
                                   HttpServletResponse httpResponse,
                                   @RequestHeader  HttpHeaders headers,
                                   @PathVariable("index") String index,
                                   @PathVariable("id") String id) {

    return super.processUpsertDoc(requestBody, request, httpResponse, headers, index, id);
  }

  @Override
  @RequestMapping(value = "/indexes/{index}/documents/{id}",
          method = RequestMethod.DELETE,
          consumes = { "application/json"})
  public ResponseEntity<String> processDeleteDoc(@RequestBody String requestBody,
                                   HttpServletRequest request,
                                   HttpServletResponse httpResponse,
                                   @RequestHeader HttpHeaders headers,
                                   @PathVariable("index") String index,
                                   @PathVariable("id") String id) {

    return super.processDeleteDoc(requestBody, request, httpResponse, headers, index, id);
  }

  @Override
  @RequestMapping(value = "/indexes/{index}/query/{queryText}",
          method = RequestMethod.GET,
          consumes = { "application/json"})
  public ResponseEntity<String> processInlineQuery(@RequestBody String requestBody,
                                     HttpServletRequest request,
                                     @RequestHeader HttpHeaders headers,
                                     @PathVariable("index") String index,
                                     @PathVariable("queryText") String queryText) {

    return super.processInlineQuery(requestBody, request, headers, index, queryText);
  }

  @Override
  @RequestMapping(value = "/indexes/{index}/query",
          method = RequestMethod.GET,
          consumes = { "application/json"})
  public ResponseEntity<String> processQueryWithGet(@RequestBody String requestBody,
                                                    HttpServletRequest request,
                                      @RequestHeader HttpHeaders headers,
                                      @PathVariable("index") String index) {

    return super.processQueryWithGet(requestBody, request, headers, index);
  }

  @Override
  @RequestMapping(value = "/indexes/{index}/query",
          method = RequestMethod.POST,
          consumes = { "application/json"})
  public ResponseEntity<String> processQuery(@RequestBody String requestBody,
                                             HttpServletRequest request,
                                             @RequestHeader HttpHeaders headers,
                                             @PathVariable("index") String index) {

    return super.processQuery(requestBody, request, headers, index);
  }

  @Override
  @RequestMapping(value = "/bulk",
                  method = RequestMethod.POST,
                  consumes = { "application/json", "application/xml" })
  public ResponseEntity<String> processBulkRequest(@RequestBody String requestBody,
                                                   HttpServletRequest request,
                                                   @RequestHeader HttpHeaders headers) {

    // If the operations string contains a special keyword, set the
    // harness to fail the authentication validation.
    if (requestBody.contains(FAIL_AUTHENTICATION_TRIGGER)) {
      authenticationShouldSucceed = false;
    }

    // Just pass the request up to the parent, since that is the code
    // that we really want to test.
    //return super.processPost(operations, request, headers, index);
    return super.processBulkRequest(requestBody, request, headers);
  }

  @Override
  protected boolean validateRequest(HttpHeaders headers,
                                    HttpServletRequest req,
                                    ApiUtils.Action action,
                                    String authPolicyFunctionName) throws Exception {

    return authenticationShouldSucceed;
  }
}
