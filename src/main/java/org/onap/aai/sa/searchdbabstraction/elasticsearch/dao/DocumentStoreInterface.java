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

import org.onap.aai.sa.rest.BulkRequest;
import org.onap.aai.sa.searchdbabstraction.elasticsearch.exception.DocumentStoreOperationException;
import org.onap.aai.sa.searchdbabstraction.entity.DocumentOperationResult;
import org.onap.aai.sa.searchdbabstraction.entity.OperationResult;
import org.onap.aai.sa.searchdbabstraction.entity.SearchOperationResult;
import org.onap.aai.sa.rest.DocumentSchema;

public interface DocumentStoreInterface {

  public OperationResult createIndex(String index, DocumentSchema documentSchema);

  public OperationResult createDynamicIndex(String index, String dynamicSchema);

  public OperationResult deleteIndex(String indexName) throws DocumentStoreOperationException;

  public DocumentOperationResult createDocument(String indexName, DocumentStoreDataEntity document,
      boolean allowImplicitIndexCreation) throws DocumentStoreOperationException;

  public DocumentOperationResult updateDocument(String indexName, DocumentStoreDataEntity document,
      boolean allowImplicitIndexCreation) throws DocumentStoreOperationException;

  public DocumentOperationResult deleteDocument(String indexName, DocumentStoreDataEntity document)
      throws DocumentStoreOperationException;

  public DocumentOperationResult getDocument(String indexName, DocumentStoreDataEntity document)
      throws DocumentStoreOperationException;

  public SearchOperationResult search(String indexName, String queryText)
      throws DocumentStoreOperationException;

  public SearchOperationResult searchWithPayload(String indexName, String query)
      throws DocumentStoreOperationException;

  public SearchOperationResult suggestionQueryWithPayload(String indexName, String query)
      throws DocumentStoreOperationException;

  /**
   * Forwards a set of operations to the document store as a single, bulk request.
   *
   * @param anIndex - The index to apply the operations to.
   * @param operations - A java object containing the set of operations to be performed.
   * @return - An operation result.
   * @throws DocumentStoreOperationException
   */
  public OperationResult performBulkOperations(BulkRequest[] request)
      throws DocumentStoreOperationException;
}
