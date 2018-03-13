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


/**
 * This class represents a single instance of a request from the search client
 * that would be part of a bundle of such operations sent in a single bulk
 * request.
 */
public class BulkRequest {

  public enum OperationType {
    CREATE,
    UPDATE,
    DELETE
  }

  private BulkOperation create;
  private BulkOperation update;
  private BulkOperation delete;

  public BulkOperation getCreate() {
    return create;
  }

  public void setCreate(BulkOperation create) {
    this.create = create;
  }

  public BulkOperation getUpdate() {
    return update;
  }

  public void setUpdate(BulkOperation update) {
    this.update = update;
  }

  public BulkOperation getDelete() {
    return delete;
  }

  public void setDelete(BulkOperation delete) {
    this.delete = delete;
  }

  public OperationType getOperationType() {

    if (create != null) {
      return OperationType.CREATE;
    } else if (update != null) {
      return OperationType.UPDATE;
    } else if (delete != null) {
      return OperationType.DELETE;
    } else {
      return null;
    }
  }

  public BulkOperation getOperation() {
    if (create != null) {
      return create;
    } else if (update != null) {
      return update;
    } else if (delete != null) {
      return delete;
    } else {
      return null;
    }
  }

  public String getIndex() {
    return ApiUtils.extractIndexFromUri(getOperation().getMetaData().getUrl());
  }

  public String getId() {
    return ApiUtils.extractIdFromUri(getOperation().getMetaData().getUrl());
  }

  @Override
  public String toString() {

    if (create != null) {
      return "create: [" + create.toString() + "]\n";
    } else if (update != null) {
      return "update: [" + update.toString() + "]\n";
    } else if (delete != null) {
      return "delete: [" + delete.toString() + "]\n";
    } else {
      return "UNDEFINED";
    }
  }
}
