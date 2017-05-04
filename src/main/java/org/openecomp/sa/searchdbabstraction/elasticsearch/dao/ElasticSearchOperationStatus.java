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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

public class ElasticSearchOperationStatus {

  private String index;
  private String type;
  private String id;
  private String version;
  private ElasticSearchShardStatus shards;
  private Integer status;
  private ElasticSearchError error;

  private Map<String, Object> additionalProperties = new HashMap<String, Object>();


  public ElasticSearchError getError() {
    return error;
  }

  public void setError(ElasticSearchError error) {
    this.error = error;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public ElasticSearchShardStatus getShards() {
    return shards;
  }

  public void setShards(ElasticSearchShardStatus shards) {
    this.shards = shards;
  }

  public String getIndex() {
    return index;
  }

  public void setIndex(String index) {
    this.index = index;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperties(String name, Object value) {
    additionalProperties.put(name, value);
  }

  @Override
  public String toString() {
    return "ElasticSearchIndexStatus [index=" + index + ", type=" + type + ", id="
        + id + ", version=" + version + ", shards=" + shards + ", status=" + status + "]";
  }


}
