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

/**
 * This class represents the ranges specification in an date_range statement.
 * <p>
 * The expected JSON structure for a ranges is as follows:
 * <p>
 * <pre>
 * {
 *  "from": <from-date>
 * }
 * </pre>
 * <p>
 * or
 * <p>
 * <pre>
 * {
 *  "to": <to-date>
 * }
 * </pre>
 * <p>
 * or
 * <p>
 * <pre>
 * {
 *  "from": <from-date>,
 *  "to": <to-date>
 * }
 * </pre>
 *
 * @author sye
 */
public class DateRange {

  @JsonProperty("from")
  private String fromDate;

  @JsonProperty("to")
  private String toDate;

  public String getFromDate() {
    return fromDate;
  }

  public void setFromDate(String fromDate) {
    this.fromDate = fromDate;
  }

  public String getToDate() {
    return toDate;
  }

  public void setToDate(String toDate) {
    this.toDate = toDate;
  }

  public String toElasticSearch() {
    StringBuilder sb = new StringBuilder();

    sb.append("{");

    if (fromDate != null) {
      sb.append("\"from\": \"");
      sb.append(fromDate.toString());
      sb.append("\"");
    }

    if (toDate != null) {
      if (fromDate != null) {
        sb.append(", \"to\": \"");
        sb.append(toDate.toString());
        sb.append("\"");
      } else {
        sb.append("\"to\": \"");
        sb.append(toDate.toString());
        sb.append("\"");
      }
    }

    sb.append("}");

    return sb.toString();
  }

  public String toString() {
    return "{from: " + fromDate + ", to: " + toDate + "}";
  }

}
