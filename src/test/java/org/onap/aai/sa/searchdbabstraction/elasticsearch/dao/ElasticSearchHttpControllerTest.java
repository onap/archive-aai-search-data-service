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

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.Properties;
import org.eclipse.jetty.util.security.Password;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.aai.sa.rest.DocumentSchema;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.config.ElasticSearchConfig;
import org.onap.aai.sa.searchdbabstraction.entity.OperationResult;

@Ignore("All tests in this classes require an Elasticsearch instance to run locally")
public class ElasticSearchHttpControllerTest {

    static {
        // Set the location of the payload translation JSON file.
        System.setProperty("CONFIG_HOME", "src/test/resources/json");
    }

    private static ElasticSearchHttpController elasticSearch;
    private static AAIEntityTestObject testDocument;

    private static final String TEST_INDEX_NAME = "test";

    private static final String indexMappings =
            "{\r\n    \"properties\": {\r\n        \"entityType\": {\r\n            \"type\": \"text\"\r\n        },\r\n"
                    + "        \"edgeTagQueryEntityFieldName\": {\r\n            \"type\": \"text\",\r\n            \"index\": \"false\"\r\n        },\r\n"
                    + "        \"edgeTagQueryEntityFieldValue\": {\r\n            \"type\": \"text\",\r\n            \"index\": \"false\"\r\n        },\r\n        \"searchTagIDs\" : {\r\n            \"type\" : \"text\"\r\n          },\r\n        \"searchTags\": {\r\n            \"type\": \"text\",\r\n            \"analyzer\": \"nGram_analyzer\",\r\n            \"search_analyzer\": \"whitespace_analyzer\"\r\n        }\r\n    }\r\n}";
    private static final String indexSettings =
            "{\r\n    \"analysis\": {\r\n        \"filter\": {\r\n            \"nGram_filter\": {\r\n                \"type\": \"nGram\",\r\n                \"min_gram\": 1,\r\n                \"max_gram\": 50,\r\n                \"token_chars\": [\r\n                    \"letter\",\r\n                    \"digit\",\r\n                    \"punctuation\",\r\n                    \"symbol\"\r\n                ]\r\n            }\r\n        },\r\n        \"analyzer\": {\r\n            \"nGram_analyzer\": {\r\n                \"type\": \"custom\",\r\n                \"tokenizer\": \"whitespace\",\r\n                \"filter\": [\r\n                    \"lowercase\",\r\n                    \"asciifolding\",\r\n                    \"nGram_filter\"\r\n                ]\r\n            },\r\n            \"whitespace_analyzer\": {\r\n                \"type\": \"custom\",\r\n                \"tokenizer\": \"whitespace\",\r\n                \"filter\": [\r\n                    \"lowercase\",\r\n                    \"asciifolding\"\r\n                ]\r\n            }\r\n        }\r\n    }\r\n}";

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.put(ElasticSearchConfig.ES_IP_ADDRESS, "127.0.0.1");
        properties.put(ElasticSearchConfig.ES_HTTP_PORT, "9200");
        properties.put(ElasticSearchConfig.ES_URI_SCHEME, "http");
        properties.put(ElasticSearchConfig.ES_AUTH_USER, "your_user_here");
        properties.put(ElasticSearchConfig.ES_AUTH_ENC, Password.obfuscate("your_password_here"));
        elasticSearch = new ElasticSearchHttpController(new ElasticSearchConfig(properties));

        testDocument = new AAIEntityTestObject();
        testDocument.setId("test123");
        testDocument.setEntityType("service-instance");
        testDocument.setEdgeTagQueryEntityFieldName("service-instance.service-instance-id");
        testDocument.setEdgeTagQueryEntityFieldValue("123456");
        testDocument.setSearchTagIDs("0");
        testDocument.setSearchTags("service-instance-id");
    }

    @Test
    public void testGetInstance() throws Exception {
        ElasticSearchHttpController.getInstance();
    }

    @Test
    public void testCreateIndex() throws Exception {
        testDeleteIndex();
        OperationResult result = elasticSearch.createIndex(TEST_INDEX_NAME, new DocumentSchema());
        assertThat(result.getResult(), containsString("/search/indexes/test"));
        assertThat(result.getResultCode(), is(201));

        result = elasticSearch.createIndex(TEST_INDEX_NAME, new DocumentSchema());
        assertThat(result.getResult(), containsString("already exists"));
        assertThat(result.getResultCode(), is(400));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDynamicIndexEmptySchema() throws Exception {
        elasticSearch.createDynamicIndex(TEST_INDEX_NAME, "");
    }

    @Test
    public void testCreateDynamicIndex() throws Exception {
        String indexName = "test_dynamic";
        elasticSearch.deleteIndex(indexName);

        OperationResult result = elasticSearch.createDynamicIndex(indexName,
                "{\"mappings\":{\"_doc\":{\"dynamic_templates\":[{\"strings_as_text\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"text\"}}}]}}}");
        assertThat(result.getResult(), containsString("/search/indexes/test"));
        assertThat(result.getResultCode(), is(201));

        elasticSearch.deleteIndex(indexName);
    }

    @Test
    public void testCreateTable() throws Exception {
        OperationResult result =
                elasticSearch.createTable(TEST_INDEX_NAME, "aai-entities", indexSettings, indexMappings);
        assertThat(result.getResult(), containsString("\"index\":\"test\"}"));
        assertThat(result.getResultCode(), either(is(200)).or(is(400)));
    }

    @Test
    public void testCreateDocument() throws Exception {
        OperationResult result = elasticSearch.createDocument(TEST_INDEX_NAME, testDocument, false);
        assertThat(result.getResult(), not(equalTo("")));

        DocumentStoreDataEntityImpl ds = new DocumentStoreDataEntityImpl();
        ds.setId(testDocument.getId());

        result = elasticSearch.getDocument(TEST_INDEX_NAME, ds);
        assertThat(result.getResult(), not(equalTo("")));
    }

    @Test
    public void testCreateDocumentInvalidIndex() throws Exception {
        OperationResult result = elasticSearch.createDocument("index_does_not_exist", testDocument, false);
        assertThat(result.getResultCode(), is(404));
        assertThat(result.getResult(), not(equalTo("")));
    }

    @Test
    public void testUpdateDocument() throws Exception {
        testDocument.setEdgeTagQueryEntityFieldValue("567890");

        OperationResult result = elasticSearch.getDocument(TEST_INDEX_NAME, testDocument);
        if (result.getResultCode() == 404) {
            testCreateDocument();
        }
        // assertThat(result.getResultCode(), anyOf(equalTo(200), equalTo(412)));
        // assertThat(result.getResult(), containsString("\"found\":true"));

        result = elasticSearch.updateDocument(TEST_INDEX_NAME, testDocument, false);
        assertThat(result.getResultCode(), anyOf(equalTo(200), equalTo(412)));

        result = elasticSearch.getDocument(TEST_INDEX_NAME, testDocument);
        assertThat(result.getResult(), containsString("test123"));
    }

    @Test
    public void testDeleteDocument() throws Exception {
        OperationResult result = elasticSearch.getDocument(TEST_INDEX_NAME, testDocument);
        if (result.getResultCode() == 404) {
            testCreateDocument();
        }

        result = elasticSearch.deleteDocument(TEST_INDEX_NAME, testDocument);
        assertThat(result.getResult(), containsString(TEST_INDEX_NAME));

        result = elasticSearch.getDocument(TEST_INDEX_NAME, testDocument);
        assertThat(result.getResult(), containsString("test123"));
    }

    @Test
    public void testBulkCreateDocuments() throws Exception {
        for (int i = 0; i < 10; i++) {
            AAIEntityTestObject doc = new AAIEntityTestObject();
            doc.setId("test-" + i);
            doc.setEntityType("service-instance");
            doc.setEdgeTagQueryEntityFieldName("service-instance.service-instance-id");
            doc.setEdgeTagQueryEntityFieldValue("123456" + i);
            doc.setSearchTagIDs("" + i);
            doc.setSearchTags("service-instance-id");

            OperationResult result = elasticSearch.createDocument(TEST_INDEX_NAME, doc, false);
            assertThat(result.getResultCode(), anyOf(equalTo(201), equalTo(400)));
        }
    }

    @Test
    public void serchByEntityType() throws Exception {
        OperationResult result = elasticSearch.search(TEST_INDEX_NAME, "q=instance");
        assertThat(result.getResult(), not(equalTo("")));
    }

    @Test
    public void serchByTagIDs() throws Exception {
        OperationResult result = elasticSearch.search(TEST_INDEX_NAME, "q=9");
        assertThat(result.getResult(), not(equalTo("")));
    }

    @Test
    public void serchByTags() throws Exception {
        OperationResult result = elasticSearch.search(TEST_INDEX_NAME, "q=service");
        assertThat(result.getResult(), not(equalTo("")));
    }

    @Test
    public void searchWithPayload() throws Exception {
        testCreateIndex();
        OperationResult result =
                elasticSearch.searchWithPayload(TEST_INDEX_NAME, "{\"query\":{\"term\":{\"user\":\"fred\"}}}");
        assertThat(result.getResult(), containsString("successful"));
        assertThat(result.getResultCode(), is(equalTo(200)));
    }

    /**
     * The _suggest endpoint appears to be deprecated in ES 5.x and above.
     */
    @Test
    public void suggestionQueryWithPayload() throws Exception {
        testCreateIndex();
        OperationResult result = elasticSearch.suggestionQueryWithPayload(TEST_INDEX_NAME,
                "{\"my-suggestion\":{\"text\":\"fred\",\"term\":{\"field\":\"body\"}}}");
        assertThat(result.getResult(), containsString("error"));
        assertThat(result.getResultCode(), is(equalTo(400)));
    }

    @Test
    public void testCreateDocumentWithoutId() throws Exception {
        AAIEntityTestObject doc = new AAIEntityTestObject();
        doc.setEntityType("service-instance");
        doc.setEdgeTagQueryEntityFieldName("service-instance.service-instance-id");
        doc.setEdgeTagQueryEntityFieldValue("1111111");
        doc.setSearchTagIDs("321");
        doc.setSearchTags("service-instance-id");

        OperationResult result = elasticSearch.createDocument(TEST_INDEX_NAME, doc, false);
        assertThat(result.getResult(), not(equalTo("")));
    }

    @Test
    public void testDeleteIndex() throws Exception {
        OperationResult result = elasticSearch.deleteIndex(TEST_INDEX_NAME);
        assertThat(result.getResultCode(), anyOf(equalTo(200), equalTo(404)));
        assertThat(result.getResult(), not(equalTo("")));
    }

    class AAIEntityTestObject implements DocumentStoreDataEntity {
        private String id;
        private String entityType;
        private String edgeTagQueryEntityFieldName;
        private String edgeTagQueryEntityFieldValue;
        private String searchTagIDs;
        private String searchTags;

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return this.id;
        }

        public String getEntityType() {
            return entityType;
        }

        public void setEntityType(String entityType) {
            this.entityType = entityType;
        }

        public String getEdgeTagQueryEntityFieldName() {
            return edgeTagQueryEntityFieldName;
        }

        public void setEdgeTagQueryEntityFieldName(String edgeTagQueryEntityFieldName) {
            this.edgeTagQueryEntityFieldName = edgeTagQueryEntityFieldName;
        }

        public String getEdgeTagQueryEntityFieldValue() {
            return edgeTagQueryEntityFieldValue;
        }

        public void setEdgeTagQueryEntityFieldValue(String edgeTagQueryEntityFieldValue) {
            this.edgeTagQueryEntityFieldValue = edgeTagQueryEntityFieldValue;
        }

        public String getSearchTagIDs() {
            return searchTagIDs;
        }

        public void setSearchTagIDs(String searchTagIDs) {
            this.searchTagIDs = searchTagIDs;
        }

        public String getSearchTags() {
            return searchTags;
        }

        public void setSearchTags(String searchTags) {
            this.searchTags = searchTags;
        }

        @Override
        public String getVersion() {
            return "1";
        }

        @Override
        public String getContentInJson() {
            return new JSONObject(). //
                    put("entityType", entityType) //
                    .put("edgeTagQueryEntityFieldName", edgeTagQueryEntityFieldName)
                    .put("edgeTagQueryEntityFieldValue", edgeTagQueryEntityFieldValue) //
                    .put("searchTagIDs", searchTagIDs) //
                    .put("searchTags", searchTags) //
                    .toString();
        }
    }

}
