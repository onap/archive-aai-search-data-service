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
package org.openecomp.sa.searchdbabstraction.searchapi;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class SortTest {

  @Test
  public void sortFieldTest() throws JsonParseException, JsonMappingException, IOException {

    String field = "fieldname";
    String order = "ascending";
    String json = "{\"field\": \"" + field + "\", \"order\": \"" + order + "\"}";

    ObjectMapper mapper = new ObjectMapper();
    Sort sort = mapper.readValue(json, Sort.class);

    assertTrue("Unexpected field name in marshalled object.  Expected: " + field + " Actual: " + sort.getField(),
        field.equals(sort.getField()));
    assertTrue("Unexpected order field in marshalled object.  Expected: " + order + " Actual: " + sort.getOrder(),
        order.equals(sort.getOrder().toString()));

  }
}
