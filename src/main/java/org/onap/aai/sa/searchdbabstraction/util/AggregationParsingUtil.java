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
package org.onap.aai.sa.searchdbabstraction.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Iterator;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.onap.aai.sa.searchdbabstraction.entity.AggregationBucket;
import org.onap.aai.sa.searchdbabstraction.entity.AggregationResult;

public class AggregationParsingUtil {
    public static AggregationResult[] parseAggregationResults(JSONObject aggregations) throws JsonProcessingException {

        // Obtain the set of aggregation names
        Set keySet = aggregations.keySet();
        AggregationResult[] aggResults = new AggregationResult[keySet.size()];

        int index = 0;
        for (Iterator it = keySet.iterator(); it.hasNext();) {
            String key = (String) it.next();
            AggregationResult aggResult = new AggregationResult();
            aggResult.setName(key);

            JSONObject bucketsOrNested = (JSONObject) aggregations.get(key);
            Object buckets = bucketsOrNested.get("buckets");
            if (buckets == null) {
                // we have a nested
                Number count = (Number) bucketsOrNested.remove("doc_count");
                aggResult.setCount(count);
                AggregationResult[] nestedResults = parseAggregationResults(bucketsOrNested);
                aggResult.setNestedAggregations(nestedResults);
            } else {
                AggregationBucket[] aggBuckets = parseAggregationBuckets((JSONArray) buckets);
                aggResult.setBuckets(aggBuckets);
            }

            aggResults[index] = aggResult;
            index++;
        }

        return aggResults;

    }

    private static AggregationBucket[] parseAggregationBuckets(JSONArray buckets) throws JsonProcessingException {
        AggregationBucket[] aggBuckets = new AggregationBucket[buckets.size()];
        for (int i = 0; i < buckets.size(); i++) {
            AggregationBucket aggBucket = new AggregationBucket();
            JSONObject bucketContent = (JSONObject) buckets.get(i);
            Object key = bucketContent.remove("key");
            aggBucket.setKey(key);
            Object formatted = bucketContent.remove("key_as_string");
            if (formatted != null) {
                aggBucket.setFormattedKey((String) formatted);
            }
            Object count = bucketContent.remove("doc_count");
            if (count != null) {
                aggBucket.setCount((Number) count);
            }
            bucketContent.remove("from");
            bucketContent.remove("from_as_string");
            bucketContent.remove("to");
            bucketContent.remove("to_as_string");


            if (!bucketContent.entrySet().isEmpty()) {
                // we have results from sub-aggregation
                AggregationResult[] subResult = parseAggregationResults(bucketContent);
                if (subResult != null) {
                    aggBucket.setSubAggregationResult(subResult);
                }
            }
            aggBuckets[i] = aggBucket;
        }

        return aggBuckets;
    }

}
