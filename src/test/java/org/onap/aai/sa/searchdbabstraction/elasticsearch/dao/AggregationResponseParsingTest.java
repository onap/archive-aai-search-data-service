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
package org.onap.aai.sa.searchdbabstraction.elasticsearch.dao;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.onap.aai.sa.searchdbabstraction.entity.AggregationResult;
import org.onap.aai.sa.searchdbabstraction.entity.AggregationResults;
import org.onap.aai.sa.searchdbabstraction.util.AggregationParsingUtil;

public class AggregationResponseParsingTest {

    @Test
    public void testParseAggregationResponse() {
        JSONParser parser = new JSONParser();
        JSONObject root;

        String input =
                "{\r\n  \"aggregations\": {\r\n    \"violations\": {\r\n      \"doc_count\": 2,\r\n      \"by_Timestamp\": {\r\n        \"doc_count_error_upper_bound\": 0,\r\n        \"sum_other_doc_count\": 0,\r\n        \"buckets\": [\r\n          {\r\n            \"key\": 7199992,\r\n            \"key_as_string\": \"Jan 1 1970 01:59:59\",\r\n            \"doc_count\": 2\r\n          }\r\n        ]\r\n      }\r\n    }\r\n  }\r\n}";

        try {
            root = (JSONObject) parser.parse(input);
            JSONObject aggregations = (JSONObject) root.get("aggregations");
            AggregationResult[] results = AggregationParsingUtil.parseAggregationResults(aggregations);
            AggregationResults aggs = new AggregationResults();
            ObjectMapper mapper = new ObjectMapper();
            aggs.setAggregations(results);
            System.out.println(mapper.setSerializationInclusion(Include.NON_NULL).writerWithDefaultPrettyPrinter()
                    .writeValueAsString(aggs));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testParseAggregationResponse2() {
        JSONParser parser = new JSONParser();
        JSONObject root;

        String input =
                "{\r\n  \"aggregations\": {\r\n    \"entityType\": {\r\n      \"doc_count_error_upper_bound\": 0,\r\n      \"sum_other_doc_count\": 0,\r\n      \"buckets\": [\r\n        {\r\n          \"key\": \"entity1\",\r\n          \"doc_count\": 5,\r\n          \"byVersion\": {\r\n            \"doc_count_error_upper_bound\": 0,\r\n            \"sum_other_doc_count\": 0,\r\n            \"buckets\": [\r\n              {\r\n                \"key\": \"0\",\r\n                \"doc_count\": 5\r\n              }\r\n            ]\r\n          }\r\n        }\r\n      ]\r\n    }\r\n  }\r\n}";

        try {
            root = (JSONObject) parser.parse(input);
            JSONObject aggregations = (JSONObject) root.get("aggregations");
            AggregationResult[] results = AggregationParsingUtil.parseAggregationResults(aggregations);
            AggregationResults aggs = new AggregationResults();
            ObjectMapper mapper = new ObjectMapper();
            aggs.setAggregations(results);
            System.out.println(mapper.setSerializationInclusion(Include.NON_NULL).writerWithDefaultPrettyPrinter()
                    .writeValueAsString(aggs));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testParseAggregationResponse3() {
        JSONParser parser = new JSONParser();
        JSONObject root;

        String input =
                "{\r\n  \"aggregations\": {\r\n    \"validateTimes\": {\r\n      \"buckets\": [\r\n        {\r\n          \"key\": \"Jan 10 2017 21:6:6-Jan 24 2017 13:43:5\",\r\n          \"from\": 1484082366000,\r\n          \"from_as_string\": \"Jan 10 2017 21:6:6\",\r\n          \"to\": 1485265385000,\r\n          \"to_as_string\": \"Jan 24 2017 13:43:5\",\r\n          \"doc_count\": 95\r\n        },\r\n        {\r\n          \"key\": \"Feb 3 2017 18:27:39-*\",\r\n          \"from\": 1486146459000,\r\n          \"from_as_string\": \"Feb 3 2017 18:27:39\",\r\n          \"doc_count\": 2\r\n        }\r\n      ]\r\n    }\r\n  }\r\n}";

        try {
            root = (JSONObject) parser.parse(input);
            JSONObject aggregations = (JSONObject) root.get("aggregations");
            AggregationResult[] results = AggregationParsingUtil.parseAggregationResults(aggregations);
            AggregationResults aggs = new AggregationResults();
            ObjectMapper mapper = new ObjectMapper();
            aggs.setAggregations(results);
            System.out.println(mapper.setSerializationInclusion(Include.NON_NULL).writerWithDefaultPrettyPrinter()
                    .writeValueAsString(aggs));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
