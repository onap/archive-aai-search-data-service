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
package org.openecomp.sa.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class DocumentApiTest extends JerseyTest {

  private static final String INDEXES_URI = "/test/indexes/";
  private static final String DOCUMENT_URI = "documents/";

  private static final String SEARCH_URI = "query/";
  private static final String INDEX_NAME = "test-index";
  private static final String DOC_ID = "test-1";
  private static final String SIMPLE_QUERY = "\"parsed-query\": {\"my-field\": \"something\", \"query-string\": \"string\"}";
  private static final String COMPLEX_QUERY =
      "{"
          + "\"filter\": {"
          + "\"all\": ["
          + "{\"match\": {\"field\": \"searchTags\", \"value\": \"a\"}}"
          + "]"
          + "},"
          + "\"queries\": ["
          + "{\"may\": {\"parsed-query\": {\"field\": \"searchTags\", \"query-string\": \"b\"}}}"
          + "]"
          + "}";

  private static final String CREATE_JSON_CONTENT = "creation content";


  @Override
  protected Application configure() {

    // Make sure that our test endpoint is on the resource path
    // for Jersey Test.
    return new ResourceConfig(SearchServiceApiHarness.class);
  }

  /**
   * This test validates the behaviour of the 'Create Document' POST request
   * endpoint.
   *
   * @throws IOException
   * @throws ParseException
   */
  @Test
  public void createDocumentTest() throws IOException, ParseException {
    String result = target(INDEXES_URI + INDEX_NAME + "/" + DOCUMENT_URI).request().post(Entity.json(CREATE_JSON_CONTENT), String.class);


    // Our stub document store DAO returns the parameters that it was
    // passed as the result string, so now we can validate that our
    // endpoint invoked it with the correct parameters.

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);

    assertTrue("Unexpected Result ", !json.get("etag").toString().isEmpty());
  }

  /**
   * This test validates the behaviour of the 'Create Document' PUT request
   * endpoint.
   *
   * @throws IOException
   * @throws ParseException
   */
  @Test
  public void updateDocumentTest() throws IOException, ParseException {
    WebTarget target = target(INDEXES_URI + INDEX_NAME + "/" + DOCUMENT_URI + DOC_ID);
    Builder request = target.request().header("If-Match", "1");
    String result = request.put(Entity.json(CREATE_JSON_CONTENT), String.class);

    // Our stub document store DAO returns the parameters that it was
    // passed as the result string, so now we can validate that our
    // endpoint invoked it with the correct parameters.
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);

    assertTrue("Unexpected Result ", !json.get("etag").toString().isEmpty());
  }

  /**
   * This test validates the behaviour of the 'Get Document' GET request
   * endpoint.
   *
   * @throws IOException
   * @throws ParseException
   */
  @Test
  public void getDocumentTest() throws IOException, ParseException {
    String result = target(INDEXES_URI + INDEX_NAME + "/" + DOCUMENT_URI + DOC_ID).request().get(String.class);

    // Our stub document store DAO returns the parameters that it was
    // passed as the result string, so now we can validate that our
    // endpoint invoked it with the correct parameters.
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);

    assertTrue("Unexpected Result ", !json.get("etag").toString().isEmpty());

  }

  /**
   * This test validates the behaviour of the 'Delete Document' DELETE request
   * endpoint.
   *
   * @throws IOException
   * @throws ParseException
   */
  @Test
  public void deleteDocumentTest() throws IOException, ParseException {
    WebTarget target = target(INDEXES_URI + INDEX_NAME + "/" + DOCUMENT_URI + DOC_ID);
    Builder request = target.request().header("If-Match", "1");
    String result = request.delete(String.class);


    // Our stub document store DAO returns the parameters that it was
    // passed as the result string, so now we can validate that our
    // endpoint invoked it with the correct parameters.
    assertTrue("Unexpected Result ", result.isEmpty());

  }

  /**
   * This test validates the behaviour of the 'Search Documents' GET request
   * endpoint.
   *
   * @throws IOException
   * @throws ParseException
   */
  @Ignore
  @Test
  public void searchDocumentTest1() throws IOException, ParseException {
    String result = target(INDEXES_URI + INDEX_NAME + "/" + SEARCH_URI + SIMPLE_QUERY).request().get(String.class);

    // Our stub document store DAO returns the parameters that it was
    // passed as the result string, so now we can validate that our
    // endpoint invoked it with the correct parameters.
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);

    assertTrue("Unexpected Result ", json.get("totalHits").toString().equals("1"));


  }

  /**
   * This test validates the behaviour of the 'Search Documents' GET request
   * endpoint.
   *
   * @throws IOException
   * @throws ParseException
   */
  @Test
  public void searchDocumentTest2() throws IOException, ParseException {
    String result = target(INDEXES_URI + INDEX_NAME + "/" + SEARCH_URI).request().post(Entity.json(COMPLEX_QUERY), String.class);

    // Our stub document store DAO returns the parameters that it was
    // passed as the result string, so now we can validate that our
    // endpoint invoked it with the correct parameters.
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    JSONObject resultJson = (JSONObject) json.get("searchResult");

    assertTrue("Unexpected Result ", resultJson.get("totalHits").toString().equals("1"));

  }

}
