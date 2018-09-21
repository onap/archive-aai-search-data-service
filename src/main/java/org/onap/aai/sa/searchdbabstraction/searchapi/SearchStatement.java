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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.radeox.util.logging.Logger;

/**
 * This class represents the structure of a search statement.
 *
 * <p>The expected JSON structure to represent a search statement is as follows:
 *
 * <p><pre>
 *     {
 *         "results-start": int,  - Optional: index of starting point in result set.
 *         "results-size": int,   - Optional: maximum number of documents to include in result set.
 *
 *         "filter": {
 *             { filter structure - see {@link Filter} }
 *         },
 *
 *         "queries": [
 *             { query structure - see {@link QueryStatement} },
 *             { query structure - see {@link QueryStatement} },
 *                              .
 *                              .
 *             { query structure - see {@link QueryStatement} },
 *         ],
 *
 *         "aggregations": [
 *             { aggregation structure - see {@link AggregationStatement} },
 *             { aggregation structure - see {@link AggregationStatement} },
 *                              .
 *                              .
 *             { aggregation structure - see {@link AggregationStatement} },
 *         ]
 *     }
 * </pre>
 */
public class SearchStatement {

  /**
   * Defines the filters that should be applied before running the
   * actual queries.  This is optional.
   */
  private Filter filter;

  /**
   * The list of queries to be applied to the document store.
   */
  private Query[] queries;

  /**
   * The list of aggregations to be applied to the search
   */
  private Aggregation[] aggregations;

  /**
   * Defines the sort criteria to apply to the query result set.
   * This is optional.
   */
  private Sort sort;

  @JsonProperty("results-start")
  private Integer resultsStart;

  @JsonProperty("results-size")
  private Integer size;

  public Filter getFilter() {
    return filter;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public Query[] getQueries() {
    return queries;
  }

  public void setQueries(Query[] queries) {
    this.queries = queries;
  }

  public Sort getSort() {
    return sort;
  }

  public void setSort(Sort sort) {
    this.sort = sort;
  }

  public boolean isFiltered() {
    return filter != null;
  }

  public Aggregation[] getAggregations() {
    return aggregations;
  }

  public void setAggregations(Aggregation[] aggregations) {
    this.aggregations = aggregations;
  }

  public boolean hasAggregations() {
    return aggregations != null && aggregations.length > 0;
  }

  public Integer getFrom() {
    return resultsStart;
  }

  public void setFrom(Integer from) {
    this.resultsStart = from;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  /**
   * This method returns a string which represents this statement in syntax
   * that is understandable by ElasticSearch and is suitable for inclusion
   * in an ElasticSearch query string.
   *
   * @return - ElasticSearch syntax string.
   */
  public String toElasticSearch() {

    StringBuilder sb = new StringBuilder();
    List<QueryStatement> notMatchQueries = new ArrayList<QueryStatement>();
    List<QueryStatement> mustQueries = new ArrayList<QueryStatement>();
    List<QueryStatement> shouldQueries = new ArrayList<QueryStatement>();

    createQueryLists(queries, mustQueries, shouldQueries, notMatchQueries);

    sb.append("{");

    sb.append("\"version\": true,");

    // If the client has specified an index into the results for the first
    // document in the result set then include that in the ElasticSearch
    // query.
    if (resultsStart != null) {
      sb.append("\"from\": ").append(resultsStart).append(", ");
    }

    // If the client has specified a maximum number of documents to be returned
    // in the result set then include that in the ElasticSearch query.
    if (size != null) {
      sb.append("\"size\": ").append(size).append(", ");
    }

    sb.append("\"query\": {");
    sb.append("\"bool\": {");

    sb.append("\"must\": [");
    AtomicBoolean firstQuery = new AtomicBoolean(true);
    for (QueryStatement query : mustQueries) {

      if (!firstQuery.compareAndSet(true, false)) {
        sb.append(", ");
      }

      sb.append(query.toElasticSearch());
    }
    sb.append("], ");

    sb.append("\"should\": [");

    firstQuery = new AtomicBoolean(true);
    for (QueryStatement query : shouldQueries) {

      if (!firstQuery.compareAndSet(true, false)) {
        sb.append(", ");
      }

      sb.append(query.toElasticSearch());
    }

    sb.append("],"); // close should list

    sb.append("\"must_not\": [");
    firstQuery.set(true);
    for (QueryStatement query : notMatchQueries) {
      sb.append(query.toElasticSearch());
    }
    sb.append("]");

    // Add the filter stanza, if one is required.
    if (isFiltered()) {
      sb.append(", \"filter\": ").append(filter.toElasticSearch());
    }

    sb.append("}"); // close bool clause
    sb.append("}"); // close query clause

    // Add the sort directive, if one is required.
    if (sort != null) {
      sb.append(", \"sort\": ").append(sort.toElasticSearch());
    }

    // Add aggregations
    if (hasAggregations()) {
      sb.append(", \"aggs\": {");

      for (int i = 0; i < aggregations.length; i++) {
        if (i > 0) {
          sb.append(",");
        }
        sb.append(aggregations[i].toElasticSearch());
      }

      sb.append("}");
    }

    sb.append("}");

    Logger.debug("Generated raw ElasticSearch query statement: " + sb.toString());
    return sb.toString();
  }

  private void createQueryLists(Query[] queries, List<QueryStatement> mustList,
                                List<QueryStatement> mayList, List<QueryStatement> mustNotList) {

    for (Query query : queries) {

      if (query.isMust()) {

        if (query.getQueryStatement().isNotMatch()) {
          mustNotList.add(query.getQueryStatement());
        } else {
          mustList.add(query.getQueryStatement());
        }
      } else {

        if (query.getQueryStatement().isNotMatch()) {
          mustNotList.add(query.getQueryStatement());
        } else {
          mayList.add(query.getQueryStatement());
        }
      }
    }
  }


  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();

    sb.append("SEARCH STATEMENT: {");

    if (size != null) {
      sb.append("from: ").append(resultsStart).append(", size: ").append(size).append(", ");
    }

    if (filter != null) {
      sb.append("filter: ").append(filter.toString()).append(", ");
    }

    sb.append("queries: [");
    AtomicBoolean firstQuery = new AtomicBoolean(true);
    if (queries != null) {
      for (Query query : queries) {

        if (!firstQuery.compareAndSet(true, false)) {
          sb.append(", ");
        }
        sb.append(query.toString());
      }
    }
    sb.append("]");

    sb.append("aggregations: [");
    firstQuery = new AtomicBoolean(true);

    if (aggregations != null) {
      for (Aggregation agg : aggregations) {

        if (!firstQuery.compareAndSet(true, false)) {
          sb.append(", ");
        }
        sb.append(agg.toString());
      }
    }
    sb.append("]");

    sb.append("]}");

    return sb.toString();
  }

}
