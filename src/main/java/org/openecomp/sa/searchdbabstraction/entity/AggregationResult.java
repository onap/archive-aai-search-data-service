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
package org.openecomp.sa.searchdbabstraction.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.emory.mathcs.backport.java.util.Arrays;

public class AggregationResult {
  private String name;

  private Number count;

  private AggregationBucket[] buckets;

  @JsonProperty("nested-aggregations")
  private AggregationResult[] nestedAggregations;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AggregationBucket[] getBuckets() {
    return buckets;
  }

  public void setBuckets(AggregationBucket[] buckets) {
    this.buckets = buckets;
  }

  public AggregationResult[] getNestedAggregations() {
    return nestedAggregations;
  }

  public void setNestedAggregations(AggregationResult[] nestedAggregations) {
    this.nestedAggregations = nestedAggregations;
  }

  public Number getCount() {
    return count;
  }

  public void setCount(Number count) {
    this.count = count;
  }

  @Override
  public String toString() {
    return "AggregationResult [name=" + name + ", count=" + count + ", buckets="
        + Arrays.toString(buckets) + ", nestedAggregations=" + Arrays.toString(nestedAggregations)
        + "]";
  }

}
