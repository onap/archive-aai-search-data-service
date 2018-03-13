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
package org.onap.aai.sa.searchdbabstraction.searchapi;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class AggregationStatement {

  @JsonProperty("group-by")
  private GroupByAggregation groupBy;

  @JsonProperty("date-range")
  private DateRangeAggregation dateRange;

  @JsonProperty("date-histogram")
  private DateHistogramAggregation dateHist;

  @JsonProperty("nested")
  private Aggregation[] nested;

  @JsonProperty("sub-aggregations")
  private Aggregation[] subAggregations;

  public GroupByAggregation getGroupBy() {
    return groupBy;
  }

  public void setGroupBy(GroupByAggregation groupBy) {
    this.groupBy = groupBy;
  }

  public DateRangeAggregation getDateRange() {
    return dateRange;
  }

  public void setDateRange(DateRangeAggregation dateRange) {
    this.dateRange = dateRange;
  }

  public DateHistogramAggregation getDateHist() {
    return dateHist;
  }

  public void setDateHist(DateHistogramAggregation dateHist) {
    this.dateHist = dateHist;
  }

  public Aggregation[] getNested() {
    return nested;
  }

  public void setNested(Aggregation[] nested) {
    this.nested = nested;
  }

  public Aggregation[] getSubAggregations() {
    return subAggregations;
  }

  public void setSubAggregations(Aggregation[] subAggregations) {
    this.subAggregations = subAggregations;
  }

  public String toElasticSearch() {
    StringBuffer sb = new StringBuffer();

    sb.append("{");

    if (nested != null && nested.length > 0) {
      sb.append("\"nested\": {\"path\": \"");
      if (nested[0].getStatement() != null) {
        sb.append(nested[0].getStatement().getNestedPath());
      }
      sb.append("\"}, \"aggs\": {");
      for (int i = 0; i < nested.length; i++) {
        if (i > 0) {
          sb.append(",");
        }
        sb.append(nested[i].toElasticSearch());
      }

      sb.append("}");
    } else {
      if (groupBy != null) {
        sb.append(groupBy.toElasticSearch());
      } else if (dateRange != null) {
        sb.append(dateRange.toElasticSearch());
      } else if (dateHist != null) {
        sb.append(dateHist.toElasticSearch());
      }

      if (subAggregations != null && subAggregations.length > 0) {
        sb.append(", \"aggs\": {");
        for (int i = 0; i < subAggregations.length; i++) {
          if (i > 0) {
            sb.append(",");
          }
          sb.append(subAggregations[i].toElasticSearch());
        }
        sb.append("}");
      }
    }

    sb.append("}");

    return sb.toString();
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();

    if (nested != null) {
      sb.append("{nested: ");
      sb.append(Arrays.toString(nested));
    } else if (groupBy != null) {
      sb.append(groupBy.toString());
    } else if (dateHist != null) {
      sb.append(dateHist.toString());
    } else if (dateRange != null) {
      sb.append(dateRange.toString());
    }

    if (subAggregations != null) {
      sb.append(", sub-aggregations: ");
      sb.append(Arrays.toString(subAggregations));
    }

    sb.append("}");

    return sb.toString();
  }

  public String getNestedPath() {
    String path = null;
    String fieldName = null;

    if (groupBy != null) {
      fieldName = groupBy.getField();
    } else if (dateRange != null) {
      fieldName = dateRange.getField();
    } else if (dateHist != null) {
      fieldName = dateHist.getField();
    }

    if (fieldName != null && fieldName.contains(".")) {
      // we have nested field
      path = fieldName.substring(0, fieldName.indexOf("."));
    }

    return path;
  }

}
