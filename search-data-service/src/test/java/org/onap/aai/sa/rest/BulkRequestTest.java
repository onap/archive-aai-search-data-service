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
package org.onap.aai.sa.rest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BulkRequestTest {

    @Test
    public void testBulkRequest_Delete() {
        BulkRequest request = new BulkRequest();
        BulkOperation operation = new BulkOperation();
        Document document = new Document();
        BulkMetaData metaData = getMetaData();
        operation.setMetaData(metaData);
        operation.setDocument(document);
        Assert.assertNotNull(operation.getDocument());
        Assert.assertNotNull(operation.getMetaData());
        Assert.assertNotNull(operation.toString());

        request.setDelete(operation);
        Assert.assertNotNull(request.getDelete());
        Assert.assertEquals(operation, request.getOperation());
        Assert.assertEquals(BulkRequest.OperationType.DELETE, request.getOperationType());
        Assert.assertTrue(request.toString().contains("delete:"));


    }

    @Test
    public void testBulkRequest_Update() {
        BulkRequest request = new BulkRequest();
        BulkOperation operation = new BulkOperation();
        Document document = new Document();
        BulkMetaData metaData = getMetaData();
        operation.setMetaData(metaData);
        operation.setDocument(document);
        Assert.assertNotNull(operation.getDocument());
        Assert.assertNotNull(operation.getMetaData());
        Assert.assertNotNull(operation.toString());

        request.setUpdate(operation);
        Assert.assertNotNull(request.getUpdate());
        Assert.assertEquals(operation, request.getOperation());
        Assert.assertEquals(BulkRequest.OperationType.UPDATE, request.getOperationType());
        Assert.assertTrue(request.toString().contains("update:"));

    }

    @Test
    public void testBulkRequest_Create() {
        BulkRequest request = new BulkRequest();
        BulkOperation operation = new BulkOperation();
        Document document = new Document();
        BulkMetaData metaData = getMetaData();
        operation.setMetaData(metaData);
        operation.setDocument(document);
        Assert.assertNotNull(operation.getDocument());
        Assert.assertNotNull(operation.getMetaData());
        Assert.assertNotNull(operation.toString());

        request.setCreate(operation);
        Assert.assertNotNull(request.getCreate());
        Assert.assertEquals(operation, request.getOperation());
        Assert.assertEquals(BulkRequest.OperationType.CREATE, request.getOperationType());
        Assert.assertTrue(request.toString().contains("create:"));

    }

    @Test
    public void testBulkRequest_Undefined() {
        BulkRequest request = new BulkRequest();
        Assert.assertNull(request.getOperation());
        Assert.assertNull(request.getOperationType());
        Assert.assertEquals("UNDEFINED", request.toString());
    }

    @Test
    public void testGetIndex() {
        BulkRequest request = new BulkRequest();
        BulkOperation operation = new BulkOperation();
        BulkMetaData metaData = new BulkMetaData();
        metaData.setUrl("/test/indexes/index1");
        operation.setMetaData(metaData);
        request.setCreate(operation);
        String index = request.getIndex();
        Assert.assertEquals(index, "index1");
    }

    @Test
    public void testGetId() {
        BulkRequest request = new BulkRequest();
        BulkOperation operation = new BulkOperation();
        BulkMetaData metaData = new BulkMetaData();
        metaData.setUrl("/test/documents/document1");
        operation.setMetaData(metaData);
        request.setCreate(operation);
        String index = request.getId();
        Assert.assertEquals(index, "document1");
    }

    @Test
    public void testApiUtils() {
        Assert.assertEquals("services/search-data-service/v1/search/indexes/index1", ApiUtils.buildIndexUri("index1"));
        Assert.assertEquals("services/search-data-service/v1/search/indexes/index1/documents/document1",
                ApiUtils.buildDocumentUri("index1", "document1"));
        Assert.assertTrue(ApiUtils.validateIndexUri("services/search-data-service/v1/search/indexes/index1"));
        Assert.assertTrue(ApiUtils.validateDocumentUri(
                "services/search-data-service/v1/search/indexes/index1/documents/document1", true));
        Assert.assertTrue(ApiUtils.validateDocumentUri(
                "services/search-data-service/v1/search/indexes/index1/documents/document1", false));
    }

    private BulkMetaData getMetaData() {
        BulkMetaData metaData = new BulkMetaData();
        metaData.setUrl("http://127.0.0.1");
        metaData.setEtag("etag-1");
        Assert.assertEquals(metaData.getUrl(), "http://127.0.0.1");
        Assert.assertEquals(metaData.getEtag(), "etag-1");
        Assert.assertNotNull(metaData.toString());
        return metaData;
    }
}
