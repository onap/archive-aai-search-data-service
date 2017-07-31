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
package org.openecomp.sa.searchdbabstraction.searchapi;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * This is the common parent from which all aggregation types inherit.  It defines
 * the common fields that all aggregations must include.
 */
public abstract class AbstractAggregation {

  /**
   * The name of the field to apply the aggregation against.
   */
  protected String field;

  /**
   * Optionally allows the number of buckets for the aggregation to be
   * specified.
   */
  protected Integer size;

  /**
   * Optionally sets the minimum number of matches that must occur before
   * a particular bucket is included in the aggregation result.
   */
  @JsonProperty("min-threshold")
  protected Integer minThreshold;


  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public Integer getMinThreshold() {
    return minThreshold;
  }

  public void setMinThreshold(Integer minThreshold) {
    this.minThreshold = minThreshold;
  }

  public abstract String toElasticSearch();

  public abstract String toString();
}
