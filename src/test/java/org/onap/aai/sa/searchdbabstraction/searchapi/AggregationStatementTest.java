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

public class AggregationStatementTest {

  private static ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testGroupBy() {
    String input = "{\r\n    \"group-by\": {\r\n      \"field\": \"entityType\"\r\n    }\r\n  }";

    String expected = "{\"terms\": {\"field\": \"entityType\"}}";

    AggregationStatement actual;
    try {
      actual = mapper.readValue(input, AggregationStatement.class);
      assertEquals(expected, actual.toElasticSearch());
    } catch (Exception e) {
      fail("Exception occurred: " + e.getMessage());
    }

  }

  @Test
  public void testDateRange() {
    String input = "{\r\n  \"date-range\": {\r\n    \"field\": \"mydate\",\r\n    \"ranges\": [\r\n      {\r\n        \"from\": \"2016-12-19T00:00:00.738-05:00\",\r\n        \"to\": \"2016-12-23T23:59:59.738-05:00\"\r\n      }\r\n    ],\r\n    \"format\": \"MM-yyy\",\r\n    \"size\": \"5\"\r\n  }\r\n}";

    String expected = "{\"date_range\": {\"field\": \"mydate\", \"format\": \"MM-yyy\", \"ranges\": [{\"from\": \"2016-12-19T00:00:00.738-05:00\", \"to\": \"2016-12-23T23:59:59.738-05:00\"}], \"size\": 5}}";

    AggregationStatement actual;
    try {
      actual = mapper.readValue(input, AggregationStatement.class);
      assertEquals(expected, actual.toElasticSearch());
    } catch (Exception e) {
      fail("Exception occurred: " + e.getMessage());
    }

  }

  @Test
  public void testDateHistogram() {
    String input = "{\r\n  \"date-histogram\": {\r\n    \"field\": \"mydate\",\r\n    \"interval\": \"day\"\r\n  }\r\n}";

    String expected = "{\"date_histogram\": {\"field\": \"mydate\", \"interval\": \"day\"}}";

    AggregationStatement actual;
    try {
      actual = mapper.readValue(input, AggregationStatement.class);
      assertEquals(expected, actual.toElasticSearch());
    } catch (Exception e) {
      fail("Exception occurred: " + e.getMessage());
    }

  }

  @Test
  public void testSubAggregation1() {
    String input = "{\r\n  \"group-by\": {\r\n    \"field\": \"severity\"\r\n  },\r\n  \"sub-aggregations\": [\r\n    {\r\n      \"name\": \"byType\",\r\n      \"aggregation\": {\r\n        \"group-by\": {\r\n          \"field\": \"entityType\"\r\n        }\r\n      }\r\n    }\r\n  ]\r\n}";
    String expected = "{\"terms\": {\"field\": \"severity\"}, \"aggs\": {\"byType\": {\"terms\": {\"field\": \"entityType\"}}}}";

    AggregationStatement actual;
    try {
      actual = mapper.readValue(input, AggregationStatement.class);
      assertEquals(expected, actual.toElasticSearch());
    } catch (Exception e) {
      fail("Exception occurred: " + e.getMessage());
    }

  }

  @Test
  public void testSubAggregation2() {
    String input = "{\r\n  \"group-by\": {\r\n    \"field\": \"severity\"\r\n  },\r\n  \"sub-aggregations\": [\r\n    {\r\n      \"name\": \"byType\",\r\n      \"aggregation\": {\r\n        \"group-by\": {\r\n          \"field\": \"violationType\"\r\n        }\r\n      }\r\n    },\r\n    {\r\n      \"name\": \"byRule\",\r\n      \"aggregation\": {\r\n        \"group-by\": {\r\n          \"field\": \"validationRule\"\r\n        }\r\n      }\r\n    }\r\n  ]\r\n}";
    String expected = "{\"terms\": {\"field\": \"severity\"}, \"aggs\": {\"byType\": {\"terms\": {\"field\": \"violationType\"}},\"byRule\": {\"terms\": {\"field\": \"validationRule\"}}}}";

    AggregationStatement actual;
    try {
      actual = mapper.readValue(input, AggregationStatement.class);
      assertEquals(expected, actual.toElasticSearch());
    } catch (Exception e) {
      fail("Exception occurred: " + e.getMessage());
    }

  }


  @Test
  public void testNestedAggregation1() {
    String input = "{\r\n  \"nested\": [{\r\n    \"name\": \"by_severity\",\r\n    \"aggregation\": {\r\n      \"group-by\": {\r\n        \"field\": \"violations.severity\"\r\n      }\r\n    }\r\n  }]\r\n}";
    String expected = "{\"nested\": {\"path\": \"violations\"}, \"aggs\": {\"by_severity\": {\"terms\": {\"field\": \"violations.severity\"}}}}";

    AggregationStatement actual;
    try {
      actual = mapper.readValue(input, AggregationStatement.class);
      assertEquals(expected, actual.toElasticSearch());
    } catch (Exception e) {
      fail("Exception occurred: " + e.getMessage());
    }

  }

  @Test
  public void testNestedAggregation2() {
    String input = "{\r\n  \"nested\": [\r\n    {\r\n      \"name\": \"by_severity\",\r\n      \"aggregation\": {\r\n        \"group-by\": {\r\n          \"field\": \"violations.severity\"\r\n        }\r\n      }\r\n    },\r\n    {\r\n      \"name\": \"by_type\",\r\n      \"aggregation\": {\r\n        \"group-by\": {\r\n          \"field\": \"violations.violationType\"\r\n        }\r\n      }\r\n    }\r\n  ]\r\n}";
    String expected = "{\"nested\": {\"path\": \"violations\"}, \"aggs\": {\"by_severity\": {\"terms\": {\"field\": \"violations.severity\"}},\"by_type\": {\"terms\": {\"field\": \"violations.violationType\"}}}}";

    AggregationStatement actual;
    try {
      actual = mapper.readValue(input, AggregationStatement.class);
      assertEquals(expected, actual.toElasticSearch());
    } catch (Exception e) {
      fail("Exception occurred: " + e.getMessage());
    }

  }


}
