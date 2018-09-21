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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

public class AggregationBucket {
    private Object key;

    @JsonProperty("formatted-key")
    private String formattedKey;

    private Number count;

    @JsonProperty("sub-aggregations")
    private AggregationResult[] subAggregationResult;

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public String getFormattedKey() {
        return formattedKey;
    }

    public void setFormattedKey(String formattedKey) {
        this.formattedKey = formattedKey;
    }

    public Number getCount() {
        return count;
    }

    public void setCount(Number count) {
        this.count = count;
    }

    public AggregationResult[] getSubAggregationResult() {
        return subAggregationResult;
    }

    public void setSubAggregationResult(AggregationResult[] subAggregationResult) {
        this.subAggregationResult = subAggregationResult;
    }

    @Override
    public String toString() {
        return "AggregationBucket [key=" + key + ", formattedKey=" + formattedKey + ", count=" + count
                + ", subAggregationResult=" + Arrays.toString(subAggregationResult) + "]";
    }

}
