/**
 * ============LICENSE_START=======================================================
 * Search Data Service
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property.
 * Copyright © 2017 Amdocs
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License ati
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 * ECOMP and OpenECOMP are trademarks
 * and service marks of AT&T Intellectual Property.
 */
package org.openecomp.sa.searchdbabstraction.searchapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GroupByAggregationTest {
  private static ObjectMapper mapper = new ObjectMapper();

  @Test
  public void test() {
    String input = "{\"field\" : \"entityType\", \"size\": 20}\r\n";

    String expected = "\"terms\": {\"field\": \"entityType\", \"size\": 20}";

    GroupByAggregation actual;
    try {
      actual = mapper.readValue(input, GroupByAggregation.class);
      assertEquals(expected, actual.toElasticSearch());
    } catch (Exception e) {
      fail("Exception occurred: " + e.getMessage());
    }
  }

  @Test
  public void testNoSize() {
    String input = "{\"field\" : \"entityType\"}\r\n";

    String expected = "\"terms\": {\"field\": \"entityType\"}";

    GroupByAggregation actual;
    try {
      actual = mapper.readValue(input, GroupByAggregation.class);
      assertEquals(expected, actual.toElasticSearch());
    } catch (Exception e) {
      fail("Exception occurred: " + e.getMessage());
    }
  }

}
