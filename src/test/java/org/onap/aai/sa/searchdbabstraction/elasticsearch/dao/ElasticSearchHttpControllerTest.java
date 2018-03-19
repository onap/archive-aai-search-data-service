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

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.config.ElasticSearchConfig;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.DocumentStoreDataEntity;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.DocumentStoreDataEntityImpl;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.dao.ElasticSearchHttpController;
import org.onap.aai.sa.searchdbabstraction.entity.OperationResult;

import java.util.Properties;

@Ignore("All tests in this classes require an Elasticsearch instance to run locally")
public class ElasticSearchHttpControllerTest {

	private static ElasticSearchHttpController elasticSearch;
	private static AAIEntityTestObject testDocument;

	private static final String indexMappings = "{\r\n    \"properties\": {\r\n        \"entityType\": {\r\n            \"type\": \"string\"\r\n        },\r\n        \"edgeTagQueryEntityFieldName\": {\r\n            \"type\": \"string\",\r\n            \"index\": \"no\"\r\n        },\r\n        \"edgeTagQueryEntityFieldValue\": {\r\n            \"type\": \"string\",\r\n            \"index\": \"no\"\r\n        },\r\n        \"searchTagIDs\" : {\r\n            \"type\" : \"string\"\r\n          },\r\n        \"searchTags\": {\r\n            \"type\": \"string\",\r\n            \"analyzer\": \"nGram_analyzer\",\r\n            \"search_analyzer\": \"whitespace_analyzer\"\r\n        }\r\n    }\r\n}";
	private static final String indexSettings = "{\r\n    \"analysis\": {\r\n        \"filter\": {\r\n            \"nGram_filter\": {\r\n                \"type\": \"nGram\",\r\n                \"min_gram\": 1,\r\n                \"max_gram\": 50,\r\n                \"token_chars\": [\r\n                    \"letter\",\r\n                    \"digit\",\r\n                    \"punctuation\",\r\n                    \"symbol\"\r\n                ]\r\n            }\r\n        },\r\n        \"analyzer\": {\r\n            \"nGram_analyzer\": {\r\n                \"type\": \"custom\",\r\n                \"tokenizer\": \"whitespace\",\r\n                \"filter\": [\r\n                    \"lowercase\",\r\n                    \"asciifolding\",\r\n                    \"nGram_filter\"\r\n                ]\r\n            },\r\n            \"whitespace_analyzer\": {\r\n                \"type\": \"custom\",\r\n                \"tokenizer\": \"whitespace\",\r\n                \"filter\": [\r\n                    \"lowercase\",\r\n                    \"asciifolding\"\r\n                ]\r\n            }\r\n        }\r\n    }\r\n}";

	@Before
	public void setUp() throws Exception {
		Properties properties = new Properties();
		properties.put(ElasticSearchConfig.ES_IP_ADDRESS, "127.0.0.1");
		properties.put(ElasticSearchConfig.ES_HTTP_PORT, "9200");
		ElasticSearchConfig config = new ElasticSearchConfig(properties);
		elasticSearch = new ElasticSearchHttpController(config);

		testDocument = new AAIEntityTestObject();
		testDocument.setId("test123");
		testDocument.setEntityType("service-instance");
		testDocument.setEdgeTagQueryEntityFieldName("service-instance.service-instance-id");
		testDocument.setEdgeTagQueryEntityFieldValue("123456");
		testDocument.setSearchTagIDs("0");
		testDocument.setSearchTags("service-instance-id");

	}

	@Test
	public void testCreateTable() throws Exception {
		OperationResult result = elasticSearch.createTable("test", "aai-entities", indexSettings, indexMappings);
		System.out.println(result);
	}

	@Test
	public void testCreateDocument() throws Exception {
		OperationResult result = elasticSearch.createDocument("test", testDocument, false);
		System.out.println(result);

		DocumentStoreDataEntityImpl ds = new DocumentStoreDataEntityImpl();
		ds.setId(testDocument.getId());

		result = elasticSearch.getDocument("test", ds);
		System.out.println(result);
	}

	@Test
	public void testUpdateDocument() throws Exception {
		testDocument.setEdgeTagQueryEntityFieldValue("567890");

		OperationResult result = elasticSearch.updateDocument("test", testDocument, false);
		System.out.println(result);

		result = elasticSearch.getDocument("test", testDocument);
		System.out.println(result);
	}

	@Test
	public void testDeleteDocument() throws Exception {
		OperationResult result = elasticSearch.deleteDocument("test", testDocument);
		System.out.println(result);

		result = elasticSearch.getDocument("test", testDocument);
		System.out.println(result);
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

			OperationResult result = elasticSearch.createDocument("test", doc, false);
			System.out.println(result);
		}
	}

	@Test
	public void serchByEntityType() throws Exception {
		OperationResult result = elasticSearch.search("test", "q=instance");
		System.out.println(result);
	}

	@Test
	public void serchByTagIDs() throws Exception {
		OperationResult result = elasticSearch.search("test", "q=9");
		System.out.println(result);
	}

	@Test
	public void serchByTags() throws Exception {
		OperationResult result = elasticSearch.search("test", "q=service");
		System.out.println(result);
	}

	@Test
	public void testCreateDocumentWithoutId() throws Exception {
		AAIEntityTestObject doc = new AAIEntityTestObject();
		doc.setEntityType("service-instance");
		doc.setEdgeTagQueryEntityFieldName("service-instance.service-instance-id");
		doc.setEdgeTagQueryEntityFieldValue("1111111");
		doc.setSearchTagIDs("321");
		doc.setSearchTags("service-instance-id");

		OperationResult result = elasticSearch.createDocument("test", doc, false);
		System.out.println(result);
	}

	@Test
	public void testsuggestionQueryWithPayload() throws Exception {

		Assert.assertNotNull(elasticSearch.suggestionQueryWithPayload("autoSuggest", "suggest-index"));

	}

	@Test
	public void testDeleteIndex() throws Exception {
		OperationResult result = elasticSearch.deleteIndex("test");
		System.out.println(result);
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
			try {
				return new JSONObject().put("entityType", entityType)
						.put("edgeTagQueryEntityFieldName", edgeTagQueryEntityFieldName)
						.put("edgeTagQueryEntityFieldValue", edgeTagQueryEntityFieldValue)
						.put("searchTagIDs", searchTagIDs).put("searchTags", searchTags).toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

	}

}
