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

public class Query {

  private QueryStatement may;
  private QueryStatement must;

  public QueryStatement getMay() {
    return may;
  }

  public void setMay(QueryStatement may) {
    this.may = may;
  }

  public QueryStatement getMust() {
    return must;
  }

  public void setMust(QueryStatement must) {
    this.must = must;
  }

  public QueryStatement getQueryStatement() {
    if (isMust()) {
      return must;
    } else if (isMay()) {
      return may;
    } else {
      return null;
    }
  }

  public boolean isMust() {
    return must != null;
  }

  public boolean isMay() {
    return may != null;
  }

  public String toElasticSearch() {

    if (isMust()) {
      return must.toElasticSearch();
    } else if (isMay()) {
      return may.toElasticSearch();
    } else {
      return ""; // throw an exception?
    }
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();

    sb.append("Query:[");
    if (isMust()) {
      sb.append("must: ").append(must.toString());
    } else if (isMay()) {
      sb.append("may: ").append(may.toString());
    } else {
      sb.append("INVALID");
    }
    sb.append("]");

    return sb.toString();
  }
}
