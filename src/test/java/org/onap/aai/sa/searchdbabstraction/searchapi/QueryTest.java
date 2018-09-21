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
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;


public class QueryTest {

    /**
     * This test validates that we are able to marshal json structures representing term queries into POJOs and that we
     * can then unmarshal those POJOs into ElasticSearch syntax.
     *
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    @Test
    public void termQueryTest() throws JsonParseException, JsonMappingException, IOException {

        Integer intValue = 1;
        String field = "searchTags";
        String termQueryWithIntegerValueJson = "{\"field\": \"" + field + "\", \"value\": " + intValue + "}";
        String termQueryWithIntegerValueExpectedES = "{\"term\": {\"" + field + "\" : " + intValue + "}}";

        Double doubleValue = 5.7;
        String termQueryWithDoubleValueJson = "{\"field\": \"" + field + "\", \"value\": " + doubleValue + "}";
        String termQueryWithDoubleValueExpectedES = "{\"term\": {\"" + field + "\" : " + doubleValue + "}}";

        String stringValue = "theValue";
        String termQueryWithStringValueJson = "{\"field\": \"" + field + "\", \"value\": \"" + stringValue + "\"}";
        String termQueryWithStringValueExpectedES = "{\"term\": {\"" + field + "\" : \"" + stringValue + "\"}}";

        ObjectMapper mapper = new ObjectMapper();


        // Validate that we can marshal a term query where the supplied value
        // is an Integer.
        TermQuery integerTermQuery = mapper.readValue(termQueryWithIntegerValueJson, TermQuery.class);
        assertTrue(
                "Expected value to be of type Integer, but was type "
                        + integerTermQuery.getValue().getClass().getName(),
                integerTermQuery.getValue() instanceof Integer);
        assertEquals(intValue, integerTermQuery.getValue());

        assertTrue("ElasticSearch term query translation does not match the expected result",
                termQueryWithIntegerValueExpectedES.equals(integerTermQuery.toElasticSearch()));

        // Validate that we can marshal a term query where the supplied value
        // is a Double.
        TermQuery doubleTermQuery = mapper.readValue(termQueryWithDoubleValueJson, TermQuery.class);
        assertTrue(
                "Expected value to be of type Double, but was type " + doubleTermQuery.getValue().getClass().getName(),
                doubleTermQuery.getValue() instanceof Double);
        assertEquals(doubleValue, doubleTermQuery.getValue());
        assertTrue("ElasticSearch term query translation does not match the expected result",
                termQueryWithDoubleValueExpectedES.equals(doubleTermQuery.toElasticSearch()));

        // Validate that we can marshal a term query where the supplied value
        // is a String literal.
        TermQuery stringTermQuery = mapper.readValue(termQueryWithStringValueJson, TermQuery.class);
        assertTrue(
                "Expected value to be of type String, but was type " + stringTermQuery.getValue().getClass().getName(),
                stringTermQuery.getValue() instanceof String);
        assertEquals(stringValue, stringTermQuery.getValue());
        assertTrue("ElasticSearch term query translation does not match the expected result",
                termQueryWithStringValueExpectedES.equals(stringTermQuery.toElasticSearch()));


    }


    /**
     * This test validates that we are able to marshal json structures representing parsed queries into POJOs and that
     * we can then unmarshal those POJOs into ElasticSearch syntax.
     *
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    @Test
    public void parsedQueryTest() throws JsonParseException, JsonMappingException, IOException {

        String field = "fieldname";
        String queryString = "The query string";

        String queryJson = "{\"field\": \"" + field + "\", \"query-string\": \"" + queryString + "\"}";
        String queryExpectedES =
                "{\"query_string\": {\"default_field\": \"" + field + "\", \"query\": \"" + queryString + "\"}}";

        ObjectMapper mapper = new ObjectMapper();
        ParsedQuery pq = mapper.readValue(queryJson, ParsedQuery.class);

        assertTrue("Unexpected marshalled value for 'field' - expected: " + field + " actual: " + pq.getField(),
                field.equals(pq.getField()));
        assertTrue("Unexpected marshalled value for 'query-string' - expected: " + queryString + " actual: "
                + pq.getQueryString(), queryString.equals(pq.getQueryString()));
        assertTrue(
                "Unexpected ElasticSearch syntax.  Expected: " + queryExpectedES + " Actual: " + pq.toElasticSearch(),
                queryExpectedES.equals(pq.toElasticSearch()));
    }


    /**
     * This test validates that a ranged query cannot be parsed with values for both the 'gte' and 'gt' fields or the
     * 'lte' and 'lt' fields, and that we do not allow mixing of numeric and date types in the same query.
     *
     * @throws JsonParseException
     * @throws IOException
     */
    @Test
    public void rangeQueryConflictingBoundsTest() throws JsonParseException, IOException {

        String invalidGTAndGTE =
                "{ \"field\": \"timestamp\", \"gte\": \"2016-10-06T00:00:00.558+03:00\", \"gt\": \"2016-10-06T23:59:59.558+03:00\"}";
        String invalidLTAndLTE =
                "{ \"field\": \"timestamp\", \"lte\": \"2016-10-06T00:00:00.558+03:00\", \"lt\": \"2016-10-06T23:59:59.558+03:00\"}";
        String invalidTypes = "{ \"field\": \"timestamp\", \"lte\": 5, \"gte\": \"2016-10-06T23:59:59.558+03:00\"}";

        ObjectMapper mapper = new ObjectMapper();

        // Attempt to parse a query where we are setting values for both the
        // 'greater than' and 'greater than and equal to' operators.
        boolean gotExpectedException = false;
        try {
            RangeQuery badRangeQuery = mapper.readValue(invalidGTAndGTE, RangeQuery.class);
        } catch (JsonMappingException e) {
            gotExpectedException = true;
        }
        assertTrue("Attempting to set both a 'gt' and 'gte' value on the same query should not have been allowed",
                gotExpectedException);

        // Attempt to parse a query where we are setting values for both the
        // 'less than' and 'less than and equal to' operators.
        gotExpectedException = false;
        try {
            RangeQuery badRangeQuery = mapper.readValue(invalidLTAndLTE, RangeQuery.class);
        } catch (JsonMappingException e) {
            gotExpectedException = true;
        }
        assertTrue("Attempting to set both a 'lt' and 'lte' value on the same query should not have been allowed",
                gotExpectedException);

        // Attempt to parse a query where we are mixing numeric and date values
        // in the same query.
        gotExpectedException = false;
        try {
            RangeQuery badRangeQuery = mapper.readValue(invalidTypes, RangeQuery.class);
        } catch (JsonMappingException e) {
            gotExpectedException = true;
        }
        assertTrue("Attempting to mix numeric and date values in the same query should not have been allowed",
                gotExpectedException);


    }


    /**
     * This test validates that date range queries can be marshalled to a Java POJO and unmarshalled to ElasticSearch
     * syntax.
     *
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    @Test
    public void dateRangeQueryTest() throws JsonParseException, JsonMappingException, IOException {

        String field = "timestamp";
        String greaterThanDate = "2016-10-06T00:00:00.558+03:00";
        String lessThanDate = "2016-10-06T23:59:59.558+03:00";

        ObjectMapper mapper = new ObjectMapper();

        // Generate a date range query using 'greater than or equal' and 'less
        // than or equal' operations.
        String dateRangeJson = "{ \"field\": \"" + field + "\", \"gte\": \"" + greaterThanDate + "\", \"lte\": \""
                + lessThanDate + "\"}";
        String dateRangeExpectedES =
                "{\"range\": {\"timestamp\": {\"gte\": \"2016-10-06T00:00:00.558+03:00\", \"lte\": \"2016-10-06T23:59:59.558+03:00\"}}}";

        // Validate that the query is marshalled correctly to the POJO and that
        // the generated ElasticSearch syntax looks as expected.
        RangeQuery dateRangeQuery = mapper.readValue(dateRangeJson, RangeQuery.class);

        assertTrue("Unexpected marshalled value for 'field'.  Expected: " + field + " Actual: "
                + dateRangeQuery.getField(), field.equals(dateRangeQuery.getField()));
        assertTrue("Unexpected type for 'gte' value.  Expected: String  Actual: "
                + dateRangeQuery.getGte().getClass().getName(), dateRangeQuery.getGte() instanceof String);
        assertTrue("Unexpected type for 'lte' value.  Expected: String  Actual: "
                + dateRangeQuery.getLte().getClass().getName(), dateRangeQuery.getLte() instanceof String);
        assertTrue("Unexpected marshalled value for 'gte'.  Expected: " + greaterThanDate + " Actual: "
                + dateRangeQuery.getGte(), greaterThanDate.equals(dateRangeQuery.getGte()));
        assertTrue("Unexpected marshalled value for 'lte'.  Expected: " + lessThanDate + " Actual: "
                + dateRangeQuery.getLte(), lessThanDate.equals(dateRangeQuery.getLte()));
        assertTrue(
                "Unexpected ElasticSearch syntax.  Expected: " + dateRangeExpectedES + " Actual: "
                        + dateRangeQuery.toElasticSearch(),
                dateRangeExpectedES.equals(dateRangeQuery.toElasticSearch()));


        // Generate a date range query using 'greater than' and 'less than or
        // equal' operations.
        dateRangeJson = "{ \"field\": \"" + field + "\", \"gt\": \"" + greaterThanDate + "\", \"lte\": \""
                + lessThanDate + "\"}";
        dateRangeExpectedES =
                "{\"range\": {\"timestamp\": {\"gt\": \"2016-10-06T00:00:00.558+03:00\", \"lte\": \"2016-10-06T23:59:59.558+03:00\"}}}";

        // Validate that the query is marshalled correctly to the POJO and that
        // the generated ElasticSearch syntax looks as expected.
        dateRangeQuery = mapper.readValue(dateRangeJson, RangeQuery.class);

        assertTrue("Unexpected marshalled value for 'field'.  Expected: " + field + " Actual: "
                + dateRangeQuery.getField(), field.equals(dateRangeQuery.getField()));

        assertTrue("Unexpected type for 'gt' value.  Expected: String  Actual: "
                + dateRangeQuery.getGt().getClass().getName(), dateRangeQuery.getGt() instanceof String);

        assertTrue("Unexpected type for 'lte' value.  Expected: String  Actual: "
                + dateRangeQuery.getLte().getClass().getName(), dateRangeQuery.getLte() instanceof String);

        assertTrue("Unexpected marshalled value for 'gt'.  Expected: " + greaterThanDate + " Actual: "
                + dateRangeQuery.getGt(), greaterThanDate.equals(dateRangeQuery.getGt()));

        assertTrue("Unexpected marshalled value for 'lte'.  Expected: " + lessThanDate + " Actual: "
                + dateRangeQuery.getLte(), lessThanDate.equals(dateRangeQuery.getLte()));

        assertTrue(
                "Unexpected ElasticSearch syntax.  Expected: " + dateRangeExpectedES + " Actual: "
                        + dateRangeQuery.toElasticSearch(),
                dateRangeExpectedES.equals(dateRangeQuery.toElasticSearch()));


        // Generate a date range query using only a 'greater than' operation.
        dateRangeJson = "{ \"field\": \"" + field + "\", \"gt\": \"" + greaterThanDate + "\"}";
        dateRangeExpectedES = "{\"range\": {\"timestamp\": {\"gt\": \"2016-10-06T00:00:00.558+03:00\"}}}";

        // Validate that the query is marshalled correctly to the POJO and that
        // the generated ElasticSearch syntax looks as expected.
        dateRangeQuery = mapper.readValue(dateRangeJson, RangeQuery.class);

        assertTrue("Unexpected marshalled value for 'field'.  Expected: " + field + " Actual: "
                + dateRangeQuery.getField(), field.equals(dateRangeQuery.getField()));

        assertTrue("Unexpected type for 'gt' value.  Expected: String  Actual: "
                + dateRangeQuery.getGt().getClass().getName(), dateRangeQuery.getGt() instanceof String);

        assertTrue("Unexpected marshalled value for 'gt'.  Expected: " + greaterThanDate + " Actual: "
                + dateRangeQuery.getGt(), greaterThanDate.equals(dateRangeQuery.getGt()));

        assertTrue(
                "Unexpected ElasticSearch syntax.  Expected: " + dateRangeExpectedES + " Actual: "
                        + dateRangeQuery.toElasticSearch(),
                dateRangeExpectedES.equals(dateRangeQuery.toElasticSearch()));

    }

    /**
     * This test validates that numeric range queries can be marshalled to a Java POJO and unmarshalled to ElasticSearch
     * syntax.
     *
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    @Test
    public void numericRangeQueryTest() throws JsonParseException, JsonMappingException, IOException {

        String field = "version";
        Integer greaterThanInt = 5;
        Integer lessThanInt = 100;

        ObjectMapper mapper = new ObjectMapper();

        // Generate a numeric range query using 'greater than or equal' and 'less
        // than or equal' operations.
        String numericRangeJson =
                "{ \"field\": \"" + field + "\", \"gte\": " + greaterThanInt + ", \"lte\": " + lessThanInt + "}";
        String numericRangeExpectedES =
                "{\"range\": {\"" + field + "\": {\"gte\": " + greaterThanInt + ", \"lte\": " + lessThanInt + "}}}";

        // Validate that the query is marshalled correctly to the POJO and that
        // the generated ElasticSearch syntax looks as expected.
        RangeQuery numericRangeQuery = mapper.readValue(numericRangeJson, RangeQuery.class);

        assertTrue("Unexpected marshalled value for 'field'.  Expected: " + field + " Actual: "
                + numericRangeQuery.getField(), field.equals(numericRangeQuery.getField()));
        assertTrue(
                "Unexpected type for 'gte' value.  Expected: Integer  Actual: "
                        + numericRangeQuery.getGte().getClass().getName(),
                numericRangeQuery.getGte() instanceof Integer);
        assertTrue(
                "Unexpected type for 'lte' value.  Expected: Integer  Actual: "
                        + numericRangeQuery.getLte().getClass().getName(),
                numericRangeQuery.getLte() instanceof Integer);
        assertEquals("Unexpected marshalled value for 'gte'.  Expected: " + greaterThanInt + " Actual: "
                + numericRangeQuery.getGte(), greaterThanInt, numericRangeQuery.getGte());
        assertEquals("Unexpected marshalled value for 'lte'.  Expected: " + lessThanInt + " Actual: "
                + numericRangeQuery.getLte(), lessThanInt, numericRangeQuery.getLte());
        assertTrue(
                "Unexpected ElasticSearch syntax.  Expected: " + numericRangeExpectedES + " Actual: "
                        + numericRangeQuery.toElasticSearch(),
                numericRangeExpectedES.equals(numericRangeQuery.toElasticSearch()));


        Double greaterThanDouble = 5.0;
        Double lessThanDouble = 100.0;

        // Generate a date range query using 'greater than' and 'less than or
        // equal' operations.
        numericRangeJson =
                "{ \"field\": \"" + field + "\", \"gt\": " + greaterThanDouble + ", \"lte\": " + lessThanDouble + "}";
        numericRangeExpectedES = "{\"range\": {\"" + field + "\": {\"gt\": " + greaterThanDouble + ", \"lte\": "
                + lessThanDouble + "}}}";

        // Validate that the query is marshalled correctly to the POJO and that
        // the generated ElasticSearch syntax looks as expected.
        numericRangeQuery = mapper.readValue(numericRangeJson, RangeQuery.class);

        assertTrue("Unexpected marshalled value for 'field'.  Expected: " + field + " Actual: "
                + numericRangeQuery.getField(), field.equals(numericRangeQuery.getField()));

        assertTrue("Unexpected type for 'gt' value.  Expected: Double  Actual: "
                + numericRangeQuery.getGt().getClass().getName(), numericRangeQuery.getGt() instanceof Double);

        assertTrue(
                "Unexpected type for 'lte' value.  Expected: Double  Actual: "
                        + numericRangeQuery.getLte().getClass().getName(),
                numericRangeQuery.getLte() instanceof Double);

        assertEquals("Unexpected marshalled value for 'gt'.  Expected: " + greaterThanDouble + " Actual: "
                + numericRangeQuery.getGt(), greaterThanDouble, numericRangeQuery.getGt());

        assertEquals("Unexpected marshalled value for 'lte'.  Expected: " + lessThanDouble + " Actual: "
                + numericRangeQuery.getLte(), lessThanDouble, numericRangeQuery.getLte());

        assertTrue(
                "Unexpected ElasticSearch syntax.  Expected: " + numericRangeExpectedES + " Actual: "
                        + numericRangeQuery.toElasticSearch(),
                numericRangeExpectedES.equals(numericRangeQuery.toElasticSearch()));
    }

}
