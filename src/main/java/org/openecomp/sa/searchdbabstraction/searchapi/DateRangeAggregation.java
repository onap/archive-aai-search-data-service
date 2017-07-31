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
 * An example of a date_range aggregation:
 *
 * <p><pre>
 * {
 *    "aggs": {
 *        "range": {
 *            "date_range": {
 *                "field": "date",
 *                "format": "MM-yyy",
 *                "ranges": [
 *                    { "to": "now-10M/M" },
 *                    { "from": "now-10M/M" }
 *                ]
 *            }
 *        }
 *    }
 * }
 * </pre>
 *
 * @author sye
 */
public class DateRangeAggregation extends AbstractAggregation {


  private String format;

  @JsonProperty("ranges")
  private DateRange[] dateRanges;


  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public DateRange[] getDateRanges() {
    return dateRanges;
  }

  public void setDateRanges(DateRange[] dateRanges) {
    this.dateRanges = dateRanges;
  }

  @Override
  public String toElasticSearch() {
    StringBuilder sb = new StringBuilder();

    sb.append("\"date_range\": {\"field\": \"");
    sb.append(field);
    sb.append("\"");

    if (format != null) {
      sb.append(", \"format\": \"");
      sb.append(format);
      sb.append("\"");
    }

    if (dateRanges != null && dateRanges.length > 0) {
      sb.append(", \"ranges\": [");

      for (int i = 0; i < dateRanges.length; i++) {
        if (i > 0) {
          sb.append(",");
        }
        sb.append(dateRanges[i].toElasticSearch());
      }

      sb.append("]");
    }

    if (size != null) {
      sb.append(", \"size\": ");
      sb.append(size);
    }

    if (minThreshold != null) {
      sb.append(", \"min_doc_count\": ").append(minThreshold);
    }

    sb.append("}");

    return sb.toString();
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("date-range: {field: " + field + ", format: " + format + ", size: " + size
        + ", minThreshold: " + minThreshold + "ranges: [");
    for (int i = 0; i < dateRanges.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(dateRanges[i].toString());
    }
    sb.append("]");

    return sb.toString();
  }

}
