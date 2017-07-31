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

public class QueryStatement {

  private TermQuery match;

  @JsonProperty("not-match")
  private TermQuery notMatch;

  @JsonProperty("parsed-query")
  private ParsedQuery parsedQuery;

  private RangeQuery range;

  public TermQuery getMatch() {
    return match;
  }

  public void setMatch(TermQuery match) {
    this.match = match;
  }

  public TermQuery getNotMatch() {
    return notMatch;
  }

  public void setNotMatch(TermQuery notMatch) {
    this.notMatch = notMatch;
  }

  public ParsedQuery getParsedQuery() {
    return parsedQuery;
  }

  public void setParsedQuery(ParsedQuery parsedQuery) {
    this.parsedQuery = parsedQuery;
  }

  public RangeQuery getRange() {
    return range;
  }

  public void setRange(RangeQuery range) {
    this.range = range;
  }

  public boolean isNotMatch() {
    return (notMatch != null);
  }

  public String toElasticSearch() {

    if (match != null) {
      return match.toElasticSearch();

    } else if (notMatch != null) {
      return notMatch.toElasticSearch();

    } else if (parsedQuery != null) {

      // We need some special wrapping if this query is against a nested field.
      if (fieldIsNested(parsedQuery.getField())) {
        return "{\"nested\": { \"path\": \"" + pathForNestedField(parsedQuery.getField())
            + "\", \"query\": " + parsedQuery.toElasticSearch() + "}}";
      } else {
        return parsedQuery.toElasticSearch();
      }

    } else if (range != null) {

      // We need some special wrapping if this query is against a nested field.
      if (fieldIsNested(range.getField())) {
        return "{\"nested\": { \"path\": \"" + pathForNestedField(range.getField())
            + "\", \"query\": " + range.toElasticSearch() + "}}";
      } else {
        return range.toElasticSearch();
      }

    } else {
      // throw an exception?
      return null;
    }
  }

  private boolean fieldIsNested(String field) {
    return field.contains(".");
  }

  private String pathForNestedField(String field) {
    int index = field.lastIndexOf('.');
    return field.substring(0, index);
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();

    sb.append("{");

    if (match != null) {
      sb.append("TERM QUERY: { match: {").append(match.toString()).append("}}");
    } else if (notMatch != null) {
      sb.append("TERM QUERY: { not-match: {").append(match.toString()).append("}}");
    } else if (parsedQuery != null) {
      sb.append("PARSED QUERY: { ").append(parsedQuery.toString()).append("}");
    } else if (range != null) {
      sb.append("RANGE QUERY: { ").append(range.toString()).append("}");
    } else {
      sb.append("UNDEFINED");
    }

    sb.append("}");
    return sb.toString();
  }
}
