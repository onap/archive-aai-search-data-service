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
package org.onap.aai.sa.searchdbabstraction.searchapi;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a simple parsed query statement.
 *
 * <p>A 'parsed query' specifies a document field to inspect and a query
 * string which will be parsed by the document store to generate the
 * exact query to be performed.
 *
 * <p>The query string will be tokenized into 'terms' and 'operators' where:
 *
 * <p>Terms may be any of the following:
 * <ul>
 * <li> single words </li>
 * <li> exact phrases (denoted by surrounding the phrase with '"' characters) </li>
 * <li> regular expressions (denoted by surrounding the phrase with '/' characters) </li>
 * </ul>
 *
 * <p>Operators may be any of the following:
 * <ul>
 * <li> +   -- The term to the right of the operator MUST be present to produce a match. </li>
 * <li> -   -- The term to the right of the operator MUST NOT be present to produce a match. </li>
 * <li> AND -- Both the terms to the left and right of the operator MUST be present to produce a match. </li>
 * <li> OR  -- Either the term to the left or right of the operator MUST be present to produce a match. </li>
 * <li> NOT -- The term to the right of the operator MUST NOT be present to produce a match. </li>
 * </ul>
 *
 * <p>The expected JSON structure for a parsed query is as follows:
 * <pre>
 *     {
 *         "parsed-query": {
 *             "field": "fieldname",
 *             "query-string": "string"
 *         }
 *     }
 * </pre>
 */
public class ParsedQuery {

  /**
   * The name of the field which the query is to be applied to.
   */
  private String field;

  /**
   * The string to be parsed to generate the full query.
   */
  @JsonProperty("query-string")
  private String queryString;


  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public String getQueryString() {
    return queryString;
  }

  public void setQueryString(String queryString) {
    this.queryString = queryString;
  }


  /**
   * This method returns a string which represents this query in syntax
   * that is understandable by ElasticSearch and is suitable for inclusion
   * in an ElasticSearch query string.
   *
   * @return - ElasticSearch syntax string.
   */
  public String toElasticSearch() {

    StringBuilder sb = new StringBuilder();

    sb.append("{");
    sb.append("\"query_string\": {");
    sb.append("\"default_field\": \"").append(field).append("\", ");
    sb.append("\"query\": \"").append(queryString).append("\"");
    sb.append("}");
    sb.append("}");

    return sb.toString();
  }

  @Override
  public String toString() {
    return "{field:" + field + ", query-string: '" + queryString + "'}";
  }
}
