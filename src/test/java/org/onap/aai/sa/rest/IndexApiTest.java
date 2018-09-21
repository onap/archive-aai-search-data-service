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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Before;
// import org.glassfish.jersey.server.ResourceConfig;
// import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.exception.DocumentStoreOperationException;
import org.onap.aai.sa.searchdbabstraction.entity.OperationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

//import javax.ws.rs.core.Application;
//import javax.ws.rs.core.Response;


/**
 * This suite of tests is intended to exercise the set of REST endpoints
 * associated with manipulating Indexes in the document store.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class IndexApiTest {

  private final String TOP_URI = "/test/indexes/";
  private final String SIMPLE_DOC_SCHEMA_JSON = "src/test/resources/json/simpleDocument.json";
  private final String DYNAMIC_INDEX_PAYLOAD = "src/test/resources/json/dynamicIndex.json";

  @Autowired
  private MockMvc mockMvc;

//
//  @Override
//  protected Application configure() {
//
//    // Make sure that our test endpoint is on the resource path
//    // for Jersey Test.
//    return new ResourceConfig(SearchServiceApiHarness.class);
//  }
//
//
  
  @Before
  public void setup() throws Exception {
	  System.setProperty("CONFIG_HOME", System.getProperty("user.dir")+ File.separator + "src/test/resources/json");
  }

  /**
   * Tests the dynamic shcema creation flow that send the request
   * JSON to the data store without any JSON validation against a schema
   *
   * @throws IOException
   */
  @Test
  public void createDynamicIndexTest() throws Exception {
    String indexName = "super-ultra-dynamic-mega-index";
    String dynamicUri = TOP_URI + "dynamic/";
    File indexFile = new File(DYNAMIC_INDEX_PAYLOAD);
    String indexPayload = TestUtils.readFileToString(indexFile);

//    String result = target(dynamicUri + indexName).request().put(Entity.json(indexPayload), String.class);
    MvcResult result = this.mockMvc.perform ( put (dynamicUri + indexName)
            .contentType ( MediaType.APPLICATION_JSON ).content ( indexPayload )).andReturn ();

    assertEquals(indexPayload, result.getResponse ().getContentAsString ());
  }


  /**
   * This test validates that the {@link IndexApi} is able to convert {@link OperationResult}
   * obects to standard REST {@link ResponseEntity} objects.
   *
   * @throws FileNotFoundException
   * @throws IOException
   * @throws DocumentStoreOperationException
   */
  @Test
  public void responseFromOperationResultTest() throws FileNotFoundException, IOException, DocumentStoreOperationException {

    int SUCCESS_RESULT_CODE = 200;
    String SUCCESS_RESULT_STRING = "Everything is ay-okay!";
    int FAILURE_RESULT_CODE = 500;
    String FAILURE_CAUSE_STRING = "Something went wrong!";


    // Create an instance of the index API endpoint that we will test against.
    // We will override the init() method because we don't want it to try to
    // connect to a real document store.
    IndexApi indexApi = new IndexApi(new SearchServiceApiHarness()) {
      @Override
      public void init() { /* do nothing */ }
    };
//
    //Construct an OperationResult instance with a success code and string.
    OperationResult successResult = new OperationResult();
    successResult.setResultCode(SUCCESS_RESULT_CODE);
    successResult.setResult(SUCCESS_RESULT_STRING);

    // Convert our success OperationResult to a standard REST Response...
    ResponseEntity successResponse = indexApi.responseFromOperationResult(successResult);

    // ...and validate that the Response is correctly populated.
    assertEquals("Unexpected result code", SUCCESS_RESULT_CODE, successResponse.getStatusCodeValue ());
    assertTrue("Incorrect result string", ((String) successResponse.getBody ()).equals(SUCCESS_RESULT_STRING));

    // Construct an OperationResult instance with an error code and failure
    // cause.
    OperationResult failureResult = new OperationResult();
    failureResult.setResultCode(FAILURE_RESULT_CODE);
    failureResult.setFailureCause(FAILURE_CAUSE_STRING);

    // Convert our failure OperationResult to a standard REST Response...
    ResponseEntity failureResponse = indexApi.responseFromOperationResult(failureResult);

    // ...and validate that the Response is correctly populated.
    assertEquals("Unexpected result code", FAILURE_RESULT_CODE, failureResponse.getStatusCodeValue ());
    assertTrue("Incorrect result string", ((String) failureResponse.getBody ()).equals(FAILURE_CAUSE_STRING));
  }
//
//
//  /**
//   * This test validates the behaviour of the 'Create Index' POST request
//   * endpoint.
//   *
//   * @throws IOException
//   */
  @Test
  public void createIndexTest() throws Exception {

    String INDEX_NAME = "test-index";
    String EXPECTED_SETTINGS =
        "{\"analysis\": "
            + "{\"filter\": "
            + "{\"nGram_filter\": { "
            + "\"type\": \"nGram\", "
            + "\"min_gram\": 1, "
            + "\"max_gram\": 50, "
            + "\"token_chars\": [ \"letter\", \"digit\", \"punctuation\", \"symbol\" ]}},"
            + "\"analyzer\": {"
            + "\"nGram_analyzer\": "
            + "{\"type\": \"custom\","
            + "\"tokenizer\": \"whitespace\","
            + "\"filter\": [\"lowercase\",\"asciifolding\",\"nGram_filter\"]},"
            + "\"whitespace_analyzer\": "
            + "{\"type\": \"custom\","
            + "\"tokenizer\": \"whitespace\","
            + "\"filter\": [\"lowercase\",\"asciifolding\"]}}}}";
    String EXPECTED_MAPPINGS =
        "{\"dynamic_templates\":[{\"strings\":{\"match_mapping_type\":\"string\",\"match\":\"*\",\"mapping\":{\"type\":\"text\",\"fielddata\":true}}}]"
        + ",\"properties\": {"
            + "\"serverName\": {"
            + "\"type\": \"string\", "
            + "\"index\": \"analyzed\", "
            + "\"search_analyzer\": \"whitespace\"}, "
            + "\"serverComplex\": {"
            + "\"type\": \"string\", "
            + "\"search_analyzer\": \"whitespace\"}}}";

    // Read a valid document schema from a json file.
    File schemaFile = new File(SIMPLE_DOC_SCHEMA_JSON);
    String documentJson = TestUtils.readFileToString(schemaFile);

    // Send a request to our 'create index' endpoint, using the schema
    // which we just read.
    // String result = target(TOP_URI + INDEX_NAME).request().put(Entity.json(documentJson), String.class);
    MvcResult result = this.mockMvc.perform ( put ( TOP_URI + INDEX_NAME ).contentType ( MediaType.APPLICATION_JSON )
            .content ( documentJson) ).andReturn ();


    // Our stub document store DAO returns the parameters that it was
    // passed as the result string, so now we can validate that our
    // endpoint invoked it with the correct parameters.
    String[] tokenizedResult = result.getResponse ().getContentAsString ().split("@");
    assertTrue("Unexpected Index Name '" + tokenizedResult[0] + "' passed to doc store DAO",
        tokenizedResult[0].equals(INDEX_NAME));
    assertTrue("Unexpected settings string '" + tokenizedResult[1] + "' passed to doc store DAO",
        tokenizedResult[1].equals(EXPECTED_SETTINGS));
    assertTrue("Unexpected mappings string '" + tokenizedResult[2] + "' passed to doc store DAO",
        tokenizedResult[2].equals(EXPECTED_MAPPINGS));
  }
//
//
  /**
   * This test validates that a 'create index' request with an improperly
   * formatted document schema as the payload will result in an
   * appropriate error being returned from the endpoint.
   */
  @Test
  public void createIndexWithMangledSchemaTest() throws Exception {

    String INDEX_NAME = "test-index";
    int BAD_REQUEST_CODE = 400;

    String invalidSchemaString = "this is definitely not json!";

    // ResponseEntity result = target(TOP_URI + INDEX_NAME).request().put(Entity.json(invalidSchemaString), ResponseEntity.class);
    MvcResult result = this.mockMvc.perform ( put ( TOP_URI + INDEX_NAME ).contentType ( MediaType.APPLICATION_JSON )
            .content ( invalidSchemaString) ).andReturn ();

    assertEquals("Invalid document schema should result in a 400 error",
        BAD_REQUEST_CODE, result.getResponse ().getStatus ());
  }
//
//
  /**
   * This test validates the behaviour of the 'Delete Index' end point.
   */
  @Test
  public void deleteIndexTest() throws Exception {

    String INDEX_NAME = "test-index";

    // Send a request to the 'delete index' endpoint.
    // String result = target(TOP_URI + INDEX_NAME).request().delete(String.class);

    MvcResult result = this.mockMvc.perform ( delete ( TOP_URI + INDEX_NAME )
            .contentType ( MediaType.APPLICATION_JSON )
            .header ( "If-Match", "1" )
            .content ( "Some Json" ) ).andReturn ( );

    // Validate that the expected parameters were passed to the document
    // store DAO.
    assertTrue("Unexpected index name '" + result.getResponse ().getContentAsString () + "' passed to doc store DAO",
        result.getResponse ().getContentAsString ().equals(INDEX_NAME));
  }
//
//
//  /**
//   * This test validates that attempting to delete an index which does not
//   * exist results in a 404 error.
//   */
  @Test
  public void deleteIndexDoesNotExistTest() throws Exception {

    int NOT_FOUND_CODE = 404;

    // Send a request to the 'delete index' endpoint, specifying a
    // non-existent index.
    // ResponseEntity result = target(TOP_URI + StubEsController.DOES_NOT_EXIST_INDEX).request().delete(ResponseEntity.class);

    MvcResult result = this.mockMvc.perform ( delete ( TOP_URI + StubEsController.DOES_NOT_EXIST_INDEX )
            .contentType ( MediaType.APPLICATION_JSON )
            .header ( "If-Match", "1" )
            .content ( "Some Json" ) ).andReturn ( );


    // Validate that a 404 error code is returned from the end point.
    assertEquals("Deleting an index which does not exist should result in a 404 error",
        NOT_FOUND_CODE, result.getResponse ().getStatus ());
  }
}
