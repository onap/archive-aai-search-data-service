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
package org.openecomp.sa.searchdbabstraction.entity;

public class SearchOperationResult extends OperationResult {

  private SearchHits searchResult;
  private AggregationResults aggregationResult;

  public SearchHits getSearchResult() {
    return searchResult;
  }

  public AggregationResults getAggregationResult() {
    return aggregationResult;
  }

  public void setAggregationResult(AggregationResults aggregations) {
    this.aggregationResult = aggregations;
  }

  public void setSearchResult(SearchHits hits) {
    this.searchResult = hits;
  }

  @Override
  public String toString() {
    return "SearchOperationResult [searchResult=" + searchResult
        + ", aggregationResult=" + aggregationResult;
  }

}
