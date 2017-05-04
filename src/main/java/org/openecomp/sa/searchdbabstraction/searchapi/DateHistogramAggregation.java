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
package org.openecomp.sa.searchdbabstraction.searchapi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An example of elasticsearch date_histogram aggregation:
 *
 * <p><pre>
 * {
 *    "aggs": {
 *        "my_group": {
 *            "date_histogram" : {
 *               "field" : "date",
 *               "interval" : "month"
 *           }
 *        }
 *    }
 * }
 * </pre>
 */

public class DateHistogramAggregation extends AbstractAggregation {

  private String interval;

  private String format;

  @JsonProperty("time-zone")
  private String timeZone;


  public String getInterval() {
    return interval;
  }

  public void setInterval(String interval) {
    this.interval = interval;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  @Override
  public String toElasticSearch() {
    StringBuilder sb = new StringBuilder();

    sb.append("\"date_histogram\": {\"field\": \"");
    sb.append(field);
    sb.append("\"");
    if (interval != null) {
      sb.append(", \"interval\": \"");
      sb.append(interval);
      sb.append("\"");
    }
    if (format != null) {
      sb.append(", \"format\": \"");
      sb.append(format);
      sb.append("\"");
    }
    if (timeZone != null) {
      sb.append(", \"time_zone\": \"");
      sb.append(timeZone);
      sb.append("\"");
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
    return "DateHistogramAggregation: [field=" + field + ", interval=" + interval + ", format="
        + format + ", timeZone=" + timeZone + ", size=" + size + " minThreshold=" + minThreshold;
  }
}
