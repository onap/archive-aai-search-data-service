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

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.io.IOException;
//import org.glassfish.jersey.server.ResourceConfig;
//import org.glassfish.jersey.test.JerseyTest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DocumentApiTest {

    private static final String INDEXES_URI = "/test/indexes/";
    private static final String DOCUMENT_URI = "documents/";

    private static final String SEARCH_URI = "query/";
    private static final String INDEX_NAME = "test-index";
    private static final String DOC_ID = "test-1";
    private static final String SIMPLE_QUERY =
            "\"parsed-query\": {\"my-field\": \"something\", \"query-string\": \"string\"}";
    private static final String COMPLEX_QUERY = "{" + "\"filter\": {" + "\"all\": ["
            + "{\"match\": {\"field\": \"searchTags\", \"value\": \"a\"}}" + "]" + "}," + "\"queries\": ["
            + "{\"may\": {\"parsed-query\": {\"field\": \"searchTags\", \"query-string\": \"b\"}}}" + "]" + "}";

    private static final String CREATE_JSON_CONTENT = "creation content";

    @Autowired
    private MockMvc mockMvc;

    /**
     * This test validates the behaviour of the 'Create Document' POST request endpoint.
     *
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void createDocumentTest() throws Exception {

        MvcResult result = this.mockMvc.perform(post(INDEXES_URI + INDEX_NAME + "/" + DOCUMENT_URI)
                .contentType(MediaType.APPLICATION_JSON).content(CREATE_JSON_CONTENT)).andReturn();

        // Our stub document store DAO returns the parameters that it was
        // passed as the result string, so now we can validate that our
        // endpoint invoked it with the correct parameters.

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(result.getResponse().getContentAsString());

        assertTrue("Unexpected Result ", !json.get("etag").toString().isEmpty());
    }

    /**
     * This test validates the behaviour of the 'Create Document' PUT request endpoint.
     *
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void updateDocumentTest() throws Exception {
        // WebTarget target = target(INDEXES_URI + INDEX_NAME + "/" + DOCUMENT_URI + DOC_ID);
        // Builder request = target.request().header("If-Match", "1");
        // String result = request.put(Entity.json(CREATE_JSON_CONTENT), String.class);

        MvcResult result = this.mockMvc
                .perform(put(INDEXES_URI + INDEX_NAME + "/" + DOCUMENT_URI + DOC_ID)
                        .contentType(MediaType.APPLICATION_JSON).header("If-Match", "1").content(CREATE_JSON_CONTENT))
                .andReturn();

        // Our stub document store DAO returns the parameters that it was
        // passed as the result string, so now we can validate that our
        // endpoint invoked it with the correct parameters.
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(result.getResponse().getContentAsString());

        assertTrue("Unexpected Result ", !json.get("etag").toString().isEmpty());
    }

    /**
     * This test validates the behaviour of the 'Get Document' GET request endpoint.
     *
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void getDocumentTest() throws Exception {
        // String result = target(INDEXES_URI + INDEX_NAME + "/" + DOCUMENT_URI + DOC_ID).request().get(String.class);

        // MvcResult result = this.mockMvc.perform ( get ( INDEXES_URI + INDEX_NAME + "/" + DOCUMENT_URI + DOC_ID )
        // ).andReturn ();
        MvcResult result = this.mockMvc
                .perform(get(INDEXES_URI + INDEX_NAME + "/" + DOCUMENT_URI + DOC_ID)
                        .contentType(MediaType.APPLICATION_JSON).header("If-Match", "1").content(CREATE_JSON_CONTENT))
                .andReturn();


        // Our stub document store DAO returns the parameters that it was
        // passed as the result string, so now we can validate that our
        // endpoint invoked it with the correct parameters.
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(result.getResponse().getContentAsString());

        assertTrue("Unexpected Result ", !json.get("etag").toString().isEmpty());

    }

    //
    // /**
    // * This test validates the behaviour of the 'Delete Document' DELETE request
    // * endpoint.
    // *
    // * @throws IOException
    // * @throws ParseException
    // */
    @Test
    public void deleteDocumentTest() throws Exception {
        // WebTarget target = target(INDEXES_URI + INDEX_NAME + "/" + DOCUMENT_URI + DOC_ID);
        // Builder request = target.request().header("If-Match", "1");
        // String result = request.delete(String.class);
        MvcResult result = this.mockMvc
                .perform(delete(INDEXES_URI + INDEX_NAME + "/" + DOCUMENT_URI + DOC_ID)
                        .contentType(MediaType.APPLICATION_JSON).header("If-Match", "1").content(CREATE_JSON_CONTENT))
                .andReturn();



        // Our stub document store DAO returns the parameters that it was
        // passed as the result string, so now we can validate that our
        // endpoint invoked it with the correct parameters.
        assertTrue("Unexpected Result ", result.getResponse().getContentAsString().isEmpty());

    }

    //
    // /**
    // * This test validates the behaviour of the 'Search Documents' GET request
    // * endpoint.
    // *
    // * @throws IOException
    // * @throws ParseException
    // */
    @Ignore
    @Test
    public void searchDocumentTest1() throws Exception {
        // String result = target(INDEXES_URI + INDEX_NAME + "/" + SEARCH_URI +
        // SIMPLE_QUERY).request().get(String.class);

        MvcResult result = this.mockMvc
                .perform(get(INDEXES_URI + INDEX_NAME + "/" + SEARCH_URI + SIMPLE_QUERY)
                        .contentType(MediaType.APPLICATION_JSON).header("If-Match", "1").content(CREATE_JSON_CONTENT))
                .andReturn();

        // Our stub document store DAO returns the parameters that it was
        // passed as the result string, so now we can validate that our
        // endpoint invoked it with the correct parameters.
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(result.getResponse().getContentAsString());

        assertTrue("Unexpected Result ", json.get("totalHits").toString().equals("1"));


    }

    //
    /**
     * This test validates the behaviour of the 'Search Documents' GET request endpoint.
     *
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void searchDocumentTest2() throws Exception {
        // String result = target(INDEXES_URI + INDEX_NAME + "/" +
        // SEARCH_URI).request().post(Entity.json(COMPLEX_QUERY), String.class);

        MvcResult result = this.mockMvc.perform(get(INDEXES_URI + INDEX_NAME + "/" + SEARCH_URI)
                .contentType(MediaType.APPLICATION_JSON).content(COMPLEX_QUERY)).andReturn();

        // Our stub document store DAO returns the parameters that it was
        // passed as the result string, so now we can validate that our
        // endpoint invoked it with the correct parameters.
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(result.getResponse().getContentAsString());
        JSONObject resultJson = (JSONObject) json.get("searchResult");

        assertTrue("Unexpected Result ", resultJson.get("totalHits").toString().equals("1"));

    }

}
