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
package org.onap.aai.sa.searchdbabstraction.searchapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.onap.aai.sa.searchdbabstraction.searchapi.Aggregation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AggregationTest {
  private static ObjectMapper mapper = new ObjectMapper();
//
  @Test
  public void test() {
    String input = "{\r\n  \"name\": \"byDate\",\r\n  \"aggregation\": {\r\n    \"date-range\": {\r\n      \"field\": \"mydate\",\r\n      \"ranges\": [\r\n        {\r\n          \"from\": \"2016-12-19T00:00:00.738-05:00\",\r\n          \"to\": \"2016-12-23T23:59:59.738-05:00\"\r\n        }\r\n      ]\r\n    },\r\n    \"sub-aggregations\": [{\r\n        \"name\": \"byTerm\",\r\n        \"aggregation\": {\r\n          \"group-by\": {\r\n            \"field\": \"myterm\"\r\n          }\r\n        }\r\n      }]\r\n  }\r\n}";

    String expected = "\"byDate\": {\"date_range\": {\"field\": \"mydate\", \"ranges\": [{\"from\": \"2016-12-19T00:00:00.738-05:00\", \"to\": \"2016-12-23T23:59:59.738-05:00\"}]}, \"aggs\": {\"byTerm\": {\"terms\": {\"field\": \"myterm\"}}}}";

    Aggregation actual;
    try {
      actual = mapper.readValue(input, Aggregation.class);
      assertEquals(expected, actual.toElasticSearch());
    } catch (Exception e) {
      fail("Exception occurred: " + e.getMessage());
    }

  }

}
