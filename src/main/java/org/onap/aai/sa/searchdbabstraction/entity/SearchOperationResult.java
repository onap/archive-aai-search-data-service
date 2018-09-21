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
package org.onap.aai.sa.searchdbabstraction.entity;

public class SearchOperationResult extends OperationResult {

    private SearchHits searchResult;
    private AggregationResults aggregationResult;
    private SuggestHits suggestResult;

    public SearchHits getSearchResult() {
        return searchResult;
    }

    public SuggestHits getSuggestResult() {
        return suggestResult;
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

    public void setSuggestResult(SuggestHits hits) {
        this.suggestResult = hits;
    }

    @Override
    public String toString() {
        return "SearchOperationResult [searchResult=" + searchResult + ", aggregationResult=" + aggregationResult
                + ", suggestResult=" + suggestResult;
    }

}
