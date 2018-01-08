/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017 Amdocs
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.onap.aai.sa.searchdbabstraction.elasticsearch.dao;

import org.junit.Assert;
import org.junit.Test;

public class ElasticSearchResultItemTest {

    @Test
    public void testAllMethods(){
        ElasticSearchShardStatus shardStatus = new ElasticSearchShardStatus();
        shardStatus.setTotal(10);
        shardStatus.setSuccessful(0);
        shardStatus.setFailed(1);
        Assert.assertEquals(shardStatus.getTotal(), 10);
        Assert.assertEquals(shardStatus.getSuccessful(), 0);
        Assert.assertEquals(shardStatus.getFailed(), 1);

        ElasticSearchCause cause = new ElasticSearchCause();
        cause.setType("type-1");
        cause.setReason("reason-1");
        Assert.assertEquals(cause.getType(), "type-1");
        Assert.assertEquals(cause.getReason(), "reason-1");

        ElasticSearchError error = new ElasticSearchError();
        error.setType("type-1");
        error.setReason("reason-1");
        error.setCausedBy(cause);
        Assert.assertEquals(error.getType(), "type-1");
        Assert.assertEquals(error.getReason(), "reason-1");
        Assert.assertNotNull(error.getCausedBy());
        error.setAdditionalProperties("name-1", "value-1");
        Assert.assertNotNull(error.getAdditionalProperties());

        //Create Status
        ElasticSearchResultItem resultItem1 = new ElasticSearchResultItem();
        resultItem1.setCreate(getStatus(shardStatus, error));
        Assert.assertNotNull(resultItem1.getCreate());
        Assert.assertEquals(resultItem1.operationType(), "create");
        Assert.assertEquals(resultItem1.operationStatus(), resultItem1.getCreate());
        Assert.assertTrue(resultItem1.toString().contains("create"));
        Assert.assertNotNull(resultItem1.toJson());

        //Index Status
        ElasticSearchResultItem resultItem2 = new ElasticSearchResultItem();
        resultItem2.setIndex(getStatus(shardStatus, error));
        Assert.assertNotNull(resultItem2.getIndex());
        Assert.assertEquals(resultItem2.operationType(), "update");
        Assert.assertEquals(resultItem2.operationStatus(), resultItem2.getIndex());
        Assert.assertTrue(resultItem2.toString().contains("index"));
        Assert.assertNotNull(resultItem2.toJson());

        //Delete Status
        ElasticSearchResultItem resultItem3 = new ElasticSearchResultItem();
        resultItem3.setDelete(getStatus(shardStatus, error));
        Assert.assertNotNull(resultItem3.getDelete());
        Assert.assertEquals(resultItem3.operationType(), "delete");
        Assert.assertEquals(resultItem3.operationStatus(), resultItem3.getDelete());
        Assert.assertTrue(resultItem3.toString().contains("delete"));
        Assert.assertNotNull(resultItem3.toJson());

        //Unknown Status
        ElasticSearchResultItem resultItem4 = new ElasticSearchResultItem();
        Assert.assertEquals(resultItem4.operationType(), "unknown");
        Assert.assertNull(resultItem4.operationStatus());

        //ElasticSearchBulkOperationResult
        ElasticSearchResultItem[] resultItems = {resultItem1, resultItem2, resultItem3};
        ElasticSearchBulkOperationResult result = new ElasticSearchBulkOperationResult();
        result.setErrors(true);
        result.setTook(new Integer(10));
        result.setItems(resultItems);
        Assert.assertTrue(result.getErrors());
        Assert.assertEquals(result.getTook(), new Integer(10));
        Assert.assertNotNull(result.getItems());
        Assert.assertNotNull(result.toString());
    }

    private ElasticSearchOperationStatus getStatus(ElasticSearchShardStatus shardStatus, ElasticSearchError error) {
        ElasticSearchOperationStatus status = new ElasticSearchOperationStatus();
        status.setIndex("index-1");
        status.setType("type-1");
        status.setId("id-1");
        status.setVersion("1.0");
        status.setShards(shardStatus);
        status.setStatus(new Integer(1));
        status.setError(error);
        status.setAdditionalProperties("REQUEST_URL", "http://127.0.0.1");
        Assert.assertEquals(status.getIndex(), "index-1");
        Assert.assertEquals(status.getType(), "type-1");
        Assert.assertEquals(status.getId(), "id-1");
        Assert.assertEquals(status.getVersion(), "1.0");
        Assert.assertEquals(status.getStatus(), new Integer(1));
        Assert.assertNotNull(status.getShards());
        Assert.assertNotNull(status.getError());
        Assert.assertNotNull(status.getAdditionalProperties());
        return status;
    }

}
