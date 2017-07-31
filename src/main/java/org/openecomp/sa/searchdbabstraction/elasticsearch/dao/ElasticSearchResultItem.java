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
package org.openecomp.sa.searchdbabstraction.elasticsearch.dao;

import org.openecomp.sa.rest.ApiUtils;

public class ElasticSearchResultItem {

  public static final String REQUEST_URL = "REQUEST_URL";

  private ElasticSearchOperationStatus create;
  private ElasticSearchOperationStatus index;
  private ElasticSearchOperationStatus delete;

  public ElasticSearchOperationStatus getCreate() {
    return create;
  }

  public void setCreate(ElasticSearchOperationStatus index) {
    this.create = index;
  }

  public ElasticSearchOperationStatus getIndex() {
    return index;
  }

  public void setIndex(ElasticSearchOperationStatus index) {
    this.index = index;
  }

  public ElasticSearchOperationStatus getDelete() {
    return delete;
  }

  public void setDelete(ElasticSearchOperationStatus delete) {
    this.delete = delete;
  }

  public String operationType() {

    if (create != null) {
      return "create";
    }
    if (index != null) {
      return "update";
    }
    if (delete != null) {
      return "delete";
    }

    return "unknown";
  }

  public ElasticSearchOperationStatus operationStatus() {

    if (create != null) {
      return create;
    }
    if (index != null) {
      return index;
    }
    if (delete != null) {
      return delete;
    }

    return null;
  }


  public String toJson() {
    StringBuilder sb = new StringBuilder();

    sb.append("{");

    sb.append("\"operation\": \"").append(operationType()).append("\", ");

    if (operationStatus().getAdditionalProperties().containsKey(REQUEST_URL)) {
      sb.append("\"url\": \"").append(operationStatus().getAdditionalProperties()
          .get(REQUEST_URL)).append("\", ");
    } else {
      sb.append("\"url\": \"").append(ApiUtils.buildDocumentUri(operationStatus()
          .getIndex(), operationStatus().getId())).append("\", ");
    }

    // We don't want to include an etag field in the response in
    // the case of a delete, since that would imply that the client
    // could still access that version of the file in some manner
    // (which we are not supporting).
    if (!operationType().equals("delete")) {
      sb.append("\"etag\": \"").append(operationStatus().getVersion()).append("\", ");
    }
    sb.append("\"status-code\": \"").append(operationStatus().getStatus()).append("\", ");

    sb.append("\"status-message\": \"");

    if ((operationStatus().getStatus() >= 200) && (operationStatus().getStatus() < 300)) {
      sb.append("OK");
    } else {
      // Sometimes the error object doesn't get populated, so check
      // before we try to reference it...
      if (operationStatus().getError() != null) {
        sb.append(operationStatus().getError().getReason());
      } else {
        sb.append("");
      }
    }
    sb.append("\"");
    sb.append("}");

    return sb.toString();
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("ElasticSearchItemStatus [");
    if (create != null) {
      sb.append("create " + create);
    } else if (index != null) {
      sb.append("index " + index);
    } else if (delete != null) {
      sb.append("delete " + index);
    }
    sb.append("]");
    return sb.toString();
  }

}
