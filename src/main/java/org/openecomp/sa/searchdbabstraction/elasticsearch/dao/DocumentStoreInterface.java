/**
 * ============LICENSE_START=======================================================
 * Search Data Service
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property.
 * Copyright © 2017 Amdocs
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License ati
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 * ECOMP and OpenECOMP are trademarks
 * and service marks of AT&T Intellectual Property.
 */
package org.openecomp.sa.searchdbabstraction.elasticsearch.dao;


import org.openecomp.sa.rest.BulkRequest;
import org.openecomp.sa.rest.DocumentSchema;
import org.openecomp.sa.searchdbabstraction.elasticsearch.exception.DocumentStoreOperationException;
import org.openecomp.sa.searchdbabstraction.entity.DocumentOperationResult;
import org.openecomp.sa.searchdbabstraction.entity.OperationResult;
import org.openecomp.sa.searchdbabstraction.entity.SearchOperationResult;


public interface DocumentStoreInterface {

  public OperationResult createIndex(String index, DocumentSchema documentSchema);

  public OperationResult deleteIndex(String indexName) throws DocumentStoreOperationException;

  public DocumentOperationResult createDocument(String indexName, DocumentStoreDataEntity document)
      throws DocumentStoreOperationException;

  public DocumentOperationResult updateDocument(String indexName, DocumentStoreDataEntity document)
      throws DocumentStoreOperationException;

  public DocumentOperationResult deleteDocument(String indexName, DocumentStoreDataEntity document)
      throws DocumentStoreOperationException;

  public DocumentOperationResult getDocument(String indexName, DocumentStoreDataEntity document)
      throws DocumentStoreOperationException;

  public SearchOperationResult search(String indexName, String queryText)
      throws DocumentStoreOperationException;

  public SearchOperationResult searchWithPayload(String indexName, String query)
      throws DocumentStoreOperationException;


  /**
   * Forwards a set of operations to the document store as a single, bulk
   * request.
   *
   * @param anIndex    - The index to apply the operations to.
   * @param operations - A java object containing the set of operations to
   *                   be performed.
   * @return - An operation result.
   * @throws DocumentStoreOperationException
   */
  public OperationResult performBulkOperations(BulkRequest[] request)
      throws DocumentStoreOperationException;
}
