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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.onap.aai.sa.rest.TestUtils;

public class SearchStatementTest {

    @Test
    public void simpleQueryTest() throws JsonParseException, JsonMappingException, IOException {

        String field = "searchTags";
        String queryString = "aai3255";
        String queryJson = "{" + "\"queries\": [" + "{\"may\": {\"parsed-query\": {" + "\"field\": \"" + field + "\","
                + "\"query-string\": \"" + queryString + "\"}}}" + "]" + "}" + "}";

        String queryES = "{" + "\"version\": true," + "\"query\": {" + "\"bool\": {" + "\"must\": [], "
                + "\"should\": [" + "{\"query_string\": {\"default_field\": \"searchTags\", \"query\": \"aai3255\"}}"
                + "]," + "\"must_not\": []}" + "}" + "}";

        // Marshal our simple query JSON to a SearchStatement object.
        ObjectMapper mapper = new ObjectMapper();
        SearchStatement ss = mapper.readValue(queryJson, SearchStatement.class);

        // We expect to have a search statement with one query.
        assertEquals("Unexpected number of queries in marshalled result", 1, ss.getQueries().length);

        // Validate that the query is of the expected type and contains the
        // expected values.
        QueryStatement query = ss.getQueries()[0].getQueryStatement();
        assertNotNull("Expected marshalled statement to contain a 'parsed query'", query.getParsedQuery());
        assertTrue("Unexpected field name in marshalled query.  Expected: " + field + " Actual: "
                + query.getParsedQuery().getField(), field.equals(query.getParsedQuery().getField()));
        assertTrue(
                "Unexpected query string in marshalled query.  Expected: " + queryString + " Actual: "
                        + query.getParsedQuery().getQueryString(),
                queryString.equals(query.getParsedQuery().getQueryString()));

        // Validate that we are able to produce the expected ElasticSearch
        // query syntax from the search statement.
        assertTrue("Unexpected ElasticSearch syntax.  Expected: " + queryES + " Actual: " + ss.toElasticSearch(),
                queryES.equals(ss.toElasticSearch()));
    }


    @Test
    public void simpleSortedQueryTest() throws JsonParseException, JsonMappingException, IOException {

        String field = "searchTags";
        String queryString = "aai3255";
        String queryJson = "{" + "\"queries\": [" + "{\"may\": {\"parsed-query\": {" + "\"field\": \"" + field + "\","
                + "\"query-string\": \"" + queryString + "\"}}}" + "],"
                + "\"sort\": { \"field\": \"date\", \"order\": \"ascending\" }" + "}";


        String queryES = "{" + "\"version\": true," + "\"query\": {" + "\"bool\": {" + "\"must\": [], "
                + "\"should\": [" + "{\"query_string\": {\"default_field\": \"searchTags\", \"query\": \"aai3255\"}}"
                + "]," + "\"must_not\": []" + "}" + "}, " + "\"sort\": { \"date\": { \"order\": \"asc\"}}" + "}";

        // Marshal our simple query JSON to a SearchStatement object.
        ObjectMapper mapper = new ObjectMapper();
        SearchStatement ss = mapper.readValue(queryJson, SearchStatement.class);

        // We expect to have a search statement with one query.
        assertEquals("Unexpected number of queries in marshalled result", 1, ss.getQueries().length);

        // Validate that the query is of the expected type and contains the
        // expected values.
        QueryStatement query = ss.getQueries()[0].getQueryStatement();
        assertNotNull("Expected marshalled statement to contain a 'parsed query'", query.getParsedQuery());
        assertTrue("Unexpected field name in marshalled query.  Expected: " + field + " Actual: "
                + query.getParsedQuery().getField(), field.equals(query.getParsedQuery().getField()));
        assertTrue(
                "Unexpected query string in marshalled query.  Expected: " + queryString + " Actual: "
                        + query.getParsedQuery().getQueryString(),
                queryString.equals(query.getParsedQuery().getQueryString()));
        System.out.println("GDF: ES = " + ss.toElasticSearch());
        // Validate that we are able to produce the expected ElasticSearch
        // query syntax from the search statement.
        assertTrue("Unexpected ElasticSearch syntax.  Expected: " + queryES + " Actual: " + ss.toElasticSearch(),
                queryES.equals(ss.toElasticSearch()));
        assertNull(ss.getAggregations());
    }

    @Test
    public void filteredQueryTest() throws JsonParseException, JsonMappingException, IOException {

        String filterField1 = "field1";
        String filterField2 = "field2";
        String filterField3 = "field3";
        String filterValue1 = "a";
        String filterValue2 = "b";
        String filterValue3 = "string";
        String filterJson = "{ \"any\": [ " + "{\"match\": {\"field\": \"" + filterField1 + "\", \"value\": \""
                + filterValue1 + "\"}}," + "{\"match\": {\"field\": \"" + filterField2 + "\", \"value\": \""
                + filterValue2 + "\"}}" + "]," + "\"all\": [" + "{\"parsed-query\": {\"field\": \"" + filterField3
                + "\", \"query-string\": \"" + filterValue3 + "\"}}" + "]" + "}";

        String filterStanzaJson = "\"filter\": " + filterJson;

        String queryStanzaJson = "\"queries\": [ "
                + "{\"may\": {\"match\": {\"field\": \"searchTags\", \"value\": \"a\"}}},"
                + "{\"may\": {\"match\": {\"field\": \"searchTags\", \"value\": \"b\"}}},"
                + "{\"may\": {\"parsed-query\": {\"field\": \"fieldname\", \"query-string\": \"string\"}}}" + "]";

        String queryES = "{" + "\"version\": true," + "\"query\": {" + "\"bool\": {" + "\"must\": [], "
                + "\"should\": [" + "{\"term\": {\"searchTags\" : \"a\"}}, " + "{\"term\": {\"searchTags\" : \"b\"}}, "
                + "{\"query_string\": {\"default_field\": \"fieldname\", \"query\": \"string\"}}" + "],"
                + "\"must_not\": [], " + "\"filter\": {" + "\"bool\": {" + "\"must\": ["
                + "{\"query_string\": {\"default_field\": \"field3\", \"query\": \"string\"}}" + "],"
                + "\"must_not\": []," + "\"should\": [" + "{\"term\": {\"field1\" : \"a\"}}, "
                + "{\"term\": {\"field2\" : \"b\"}}" + "]," + "\"must_not\": []" + "}" + "}" + "}" + "}" + "}";

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(filterStanzaJson).append(", ");
        sb.append(queryStanzaJson);
        sb.append("}");

        ObjectMapper mapper = new ObjectMapper();
        SearchStatement ss = mapper.readValue(sb.toString(), SearchStatement.class);

        assertEquals("Unexpected number of queries in the 'any' list for this statement's filter", 2,
                ss.getFilter().getAny().length);
        assertEquals("Unexpected number of queries in the 'all' list for this statement's filter", 1,
                ss.getFilter().getAll().length);

        assertTrue("Unexpected ElasticSearch syntax.  Expected: " + queryES + " Actual: " + ss.toElasticSearch(),
                queryES.equals(ss.toElasticSearch()));

        assertNull(ss.getAggregations());
    }

    @Test
    public void aggregationTest() {
        String input =
                "{\r\n  \"queries\": [\r\n    {\r\n      \"must\": {\r\n        \"match\": {\r\n          \"field\": \"searchTags\",\r\n          \"value\": \"a\"\r\n        }\r\n      }\r\n    }\r\n  ],\r\n  \"aggregations\": [\r\n    {\r\n      \"name\": \"byDate\",\r\n      \"aggregation\": {\r\n        \"date-range\": {\r\n          \"field\": \"mydate\",\r\n          \"ranges\": [\r\n            {\r\n              \"from\": \"2016-12-19T00:00:00.738-05:00\",\r\n              \"to\": \"2016-12-23T23:59:59.738-05:00\"\r\n            }\r\n          ]\r\n        },\r\n        \"sub-aggregations\": [\r\n          {\r\n            \"name\": \"byTerm\",\r\n            \"aggregation\": {\r\n              \"group-by\": {\r\n                \"field\": \"myterm\"\r\n              }\r\n            }\r\n          },\r\n          {\r\n            \"name\": \"byDate\",\r\n            \"aggregation\": {\r\n              \"date-histogram\": {\r\n                \"field\": \"myDate\",\r\n                \"interval\": \"myInterval\"\r\n              }\r\n            }\r\n          }\r\n        ]\r\n      }\r\n    },\r\n    {\r\n      \"name\": \"2nd\",\r\n      \"aggregation\": {\r\n        \"group-by\": {\r\n          \"field\": \"anotherTerm\"\r\n        }\r\n      }\r\n    }\r\n  ]\r\n}";

        ObjectMapper mapper = new ObjectMapper();
        try {
            SearchStatement ss = mapper.readValue(input, SearchStatement.class);
            Aggregation[] aggs = ss.getAggregations();
            assertNotNull(aggs);
            assertEquals("Unexpected number aggregations", 2, aggs.length);
            assertEquals("byDate", aggs[0].getName());
            assertNotNull(aggs[0].getStatement().getDateRange());
            assertEquals("mydate", aggs[0].getStatement().getDateRange().getField());
            assertNotNull(aggs[0].getStatement().getSubAggregations());
            assertEquals(2, aggs[0].getStatement().getSubAggregations().length);
            assertEquals("byTerm", aggs[0].getStatement().getSubAggregations()[0].getName());
            assertEquals("byDate", aggs[0].getStatement().getSubAggregations()[1].getName());
            assertNull(aggs[0].getStatement().getGroupBy());
            assertEquals("2nd", aggs[1].getName());
            assertNotNull(aggs[1].getStatement().getGroupBy());
            assertEquals("anotherTerm", aggs[1].getStatement().getGroupBy().getField());
            assertNull(aggs[1].getStatement().getDateRange());
            assertNull(aggs[1].getStatement().getSubAggregations());

        } catch (Exception e) {
            fail("Encountered exception: " + e.getMessage());
        }
    }

    @Test
    public void resultSetRangeTest() throws IOException {

        // Simple query with a result set subrange specified.
        File queryWithSubrangeFile = new File("src/test/resources/json/queries/query-with-subrange.json");
        String queryWithSubrangeStr = TestUtils.readFileToString(queryWithSubrangeFile);
        String queryWithSubrangeExpectedESString =
                "{\"version\": true,\"from\": 0, \"size\": 10, \"query\": {\"bool\": {\"must\": [{\"term\": {\"field1\" : \"Bob\"}}], \"should\": [],\"must_not\": []}}}";

        ObjectMapper mapper = new ObjectMapper();
        SearchStatement ss = mapper.readValue(queryWithSubrangeStr, SearchStatement.class);

        assertEquals("Unexpected index for result set start", ss.getFrom(), (Integer) 0);
        assertEquals("Unexpected value for result set size", ss.getSize(), (Integer) 10);
        assertTrue("Unexpected elastic search query generated from search statement",
                ss.toElasticSearch().equals(queryWithSubrangeExpectedESString));
    }
}
