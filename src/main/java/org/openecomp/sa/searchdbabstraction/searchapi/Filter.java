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

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the filter stanza in a search statement.
 *
 * <p>The expected JSON structure for a filter stanza is as follows:
 * <pre>
 * {
 *     "filter": {
 *        "all": [ {query structure}, {query structure}, ... {query structure} ],
 *        "any": [ {query structure}, {query structure}, ... {query structure} ]
 *     }
 * }
 * </pre>
 */
public class Filter {

  /**
   * All queries in this list must evaluate to true for the filter to pass.
   */
  private QueryStatement[] all;

  /**
   * Any one of the queries in this list must evaluate to true for the
   * filter to pass.
   */
  private QueryStatement[] any;


  public QueryStatement[] getAll() {
    return all;
  }

  public void setAll(QueryStatement[] all) {
    this.all = all;
  }

  public QueryStatement[] getAny() {
    return any;
  }

  public void setAny(QueryStatement[] any) {
    this.any = any;
  }

  /**
   * This method returns a string which represents this filter in syntax
   * that is understandable by ElasticSearch and is suitable for inclusion
   * in an ElasticSearch query string.
   *
   * @return - ElasticSearch syntax string.
   */
  public String toElasticSearch() {

    StringBuilder sb = new StringBuilder();

    List<QueryStatement> notMatchQueries = new ArrayList<QueryStatement>();
    sb.append("{");
    sb.append("\"bool\": {");

    // Add the queries from our 'all' list.
    int matchQueriesCount = 0;
    int notMatchQueriesCount = 0;
    if (all != null) {
      sb.append("\"must\": [");

      for (QueryStatement query : all) {
        if (matchQueriesCount > 0) {
          sb.append(", ");
        }

        if (query.isNotMatch()) {
          notMatchQueries.add(query);
        } else {
          sb.append(query.toElasticSearch());
          matchQueriesCount++;
        }
      }
      sb.append("],");


      sb.append("\"must_not\": [");
      for (QueryStatement query : notMatchQueries) {
        if (notMatchQueriesCount > 0) {
          sb.append(", ");
        }
        sb.append(query.toElasticSearch());
        notMatchQueriesCount++;
      }
      sb.append("]");
    }

    // Add the queries from our 'any' list.
    notMatchQueries.clear();
    if (any != null) {
      if (all != null) {
        sb.append(",");
      }
      sb.append("\"should\": [");

      matchQueriesCount = 0;
      for (QueryStatement query : any) {
        //if(!firstQuery.compareAndSet(true, false)) {
        if (matchQueriesCount > 0) {
          sb.append(", ");
        }

        if (query.isNotMatch()) {
          notMatchQueries.add(query);
        } else {
          sb.append(query.toElasticSearch());
          matchQueriesCount++;
        }
      }
      sb.append("],");

      //firstQuery.set(true);
      notMatchQueriesCount = 0;
      sb.append("\"must_not\": [");
      for (QueryStatement query : notMatchQueries) {
        //if(!firstQuery.compareAndSet(true, false)) {
        if (notMatchQueriesCount > 0) {
          sb.append(", ");
        }
        sb.append(query.toElasticSearch());
        notMatchQueriesCount++;
      }
      sb.append("]");
    }
    sb.append("}");
    sb.append("}");

    return sb.toString();
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();

    sb.append("{");

    sb.append("all: [");
    if (all != null) {
      for (QueryStatement query : all) {
        sb.append(query.toString());
      }
    }
    sb.append("], ");

    sb.append("any: [");
    if (any != null) {
      for (QueryStatement query : any) {
        sb.append(query.toString());
      }
    }
    sb.append("] ");

    sb.append("}");

    return sb.toString();
  }
}
