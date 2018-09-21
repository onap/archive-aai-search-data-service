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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

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
