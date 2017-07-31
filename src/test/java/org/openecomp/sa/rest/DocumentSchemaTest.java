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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.openecomp.sa.rest.DocumentSchema;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertTrue;


public class DocumentSchemaTest {

  private final String SIMPLE_DOC_SCHEMA_JSON = "src/test/resources/json/simpleDocument.json";
  private final String NESTED_DOC_SCHEMA_JSON = "src/test/resources/json/nested-document.json";


  /**
   * This test validates that we convert document definitions back and
   * forth between json strings and POJOs without any loss of data.
   *
   * @throws com.fasterxml.jackson.core.JsonParseException
   * @throws com.fasterxml.jackson.databind.JsonMappingException
   * @throws IOException
   */
  @Test
  public void simpleDocSchemaFromJsonFileTest() throws com.fasterxml.jackson.core.JsonParseException, com.fasterxml.jackson.databind.JsonMappingException, IOException {

    // Import our json format document schema from a file.
    File schemaFile = new File(SIMPLE_DOC_SCHEMA_JSON);
    String fileString = TestUtils.readFileToString(schemaFile);

    // Unmarshall that to a Java POJO
    ObjectMapper mapper = new ObjectMapper();
    DocumentSchema docSchema = mapper.readValue(schemaFile, DocumentSchema.class);

    // Now, for the purposes of comparison, produce a JSON string from
    // our Java object.
    String jsonString = mapper.writeValueAsString(docSchema);

    // Assert that the raw JSON that we read from the file matches the marshalled
    // JSON we generated from our Java object (ie: validate that we didn't lose
    // anything going in either direction).
    assertTrue("Marshalled object does not match the original json source that produced it",
        fileString.equals(jsonString));
  }


  /**
   * This test validates that we convert document definitions back and
   * forth between json strings and POJOs without any loss of data in
   * the case of document schemas which contain nested fields.
   *
   * @throws com.fasterxml.jackson.core.JsonParseException
   * @throws com.fasterxml.jackson.databind.JsonMappingException
   * @throws IOException
   */
  @Test
  public void nestedDocSchemaFromJsonFileTest() throws JsonParseException, JsonMappingException, IOException {

    // Import our json format document schema from a file.
    File schemaFile = new File(NESTED_DOC_SCHEMA_JSON);
    String fileString = TestUtils.readFileToString(schemaFile);

    // Unmarshall that to a Java POJO
    ObjectMapper mapper = new ObjectMapper();
    DocumentSchema docSchema = mapper.readValue(schemaFile, DocumentSchema.class);

    String jsonString = mapper.writeValueAsString(docSchema);

    // Assert that the raw JSON that we read from the file matches the marshalled
    // JSON we generated from our Java object (ie: validate that we didn't lose
    // anything going in either direction).
    assertTrue("Marshalled object does not match the original json source that produced it",
        fileString.equals(jsonString));
  }
}