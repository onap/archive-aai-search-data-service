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
import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class represents a simple term query.
 *
 * <p>A term query takes an operator, a field to apply the query to and a value to match
 * against the query contents.
 *
 * <p>Valid operators include:
 * <ul>
 * <li> match - Field must contain the supplied value to produce a match. </li>
 * <li> not-match - Field must NOT contain the supplied value to produce a match. </li>
 * </ul>
 * The following examples illustrate the structure of a few variants of the
 * term query:
 *
 * <p><pre>
 *     // Single Field Match Query:
 *     {
 *         "match": {"field": "searchTags", "value": "abcd"}
 *     }
 *
 *     // Single Field Not-Match query:
 *     {
 *         "not-match": {"field": "searchTags", "value": "efgh"}
 *     }
 * </pre>
 *
 * <p><pre>
 *     // Multi Field Match Query With A Single Value:
 *     {
 *         "match": {"field": "entityType searchTags", "value": "pserver"}
 *     }
 *
 *     // Multi Field Match Query With Multiple Values:
 *     {
 *         "match": {"field": "entityType searchTags", "value": "pserver tenant"}
 *     }
 * </pre>
 */
public class TermQuery {

  /**
   * The name of the field to apply the term query to.
   */
  private String field;

  /**
   * The value which the field must contain in order to have a match.
   */
  private Object value;

  /**
   * For multi field queries only.  Determines the rules for whether or not a document matches
   * the query, as follows:
   *
   * <p>"and" - At least one occurrence of every supplied value must be present in any of the
   * supplied fields.
   *
   * <p>"or"  - At least one occurrence of any of the supplied values must be present in any of
   * the supplied fields.
   */
  private String operator;

  @JsonProperty("analyzer")
  private String searchAnalyzer;


  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  private boolean isNumericValue() {
    return ((value instanceof Integer) || (value instanceof Double));
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public String getSearchAnalyzer() {
    return searchAnalyzer;
  }

  public void setSearchAnalyzer(String searchAnalyzer) {
    this.searchAnalyzer = searchAnalyzer;
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

    // Are we generating a multi field query?
    if (isMultiFieldQuery()) {

      // For multi field queries, we have to be careful about how we handle
      // nested fields, so check to see if any of the specified fields are
      // nested.
      if (field.contains(".")) {

        // Build the equivalent of a multi match query across one or more nested fields.
        toElasticSearchNestedMultiMatchQuery(sb);

      } else {

        // Build a real multi match query, since we don't need to worry about nested fields.
        toElasticSearchMultiFieldQuery(sb);
      }
    } else {

      // Single field query.

      // Add the necessary wrapping if this is a query against a nested field.
      if (fieldIsNested(field)) {
        sb.append("{\"nested\": { \"path\": \"").append(pathForNestedField(field))
            .append("\", \"query\": ");
      }

      // Build the query.
      toElasticSearchSingleFieldQuery(sb);

      if (fieldIsNested(field)) {
        sb.append("}}");
      }
    }

    sb.append("}");

    return sb.toString();
  }


  /**
   * Determines whether or not the client has specified a term query with
   * multiple fields.
   *
   * @return - true if the query is referencing multiple fields, false, otherwise.
   */
  private boolean isMultiFieldQuery() {

    return (field.split(" ").length > 1);
  }


  /**
   * Constructs a single field term query in ElasticSearch syntax.
   *
   * @param sb - The string builder to assemble the query string with.
   * @return - The single term query.
   */
  private void toElasticSearchSingleFieldQuery(StringBuilder sb) {

    sb.append("\"term\": {\"").append(field).append("\" : ");

    // For numeric values, don't enclose the value in quotes.
    if (!isNumericValue()) {
      sb.append("\"").append(value).append("\"");
    } else {
      sb.append(value);
    }

    sb.append("}");
  }


  /**
   * Constructs a multi field query in ElasticSearch syntax.
   *
   * @param sb - The string builder to assemble the query string with.
   * @return - The multi field query.
   */
  private void toElasticSearchMultiFieldQuery(StringBuilder sb) {

    sb.append("\"multi_match\": {");

    sb.append("\"query\": \"").append(value).append("\", ");
    sb.append("\"type\": \"cross_fields\",");
    sb.append("\"fields\": [");

    List<String> fields = Arrays.asList(field.split(" "));
    AtomicBoolean firstField = new AtomicBoolean(true);
    for (String f : fields) {
      if (!firstField.compareAndSet(true, false)) {
        sb.append(", ");
      }
      sb.append("\"").append(f.trim()).append("\"");
    }
    sb.append("],");

    sb.append("\"operator\": \"").append((operator != null)
        ? operator.toLowerCase() : "and").append("\"");

    if (searchAnalyzer != null) {
      sb.append(", \"analyzer\": \"").append(searchAnalyzer).append("\"");
    }

    sb.append("}");
  }


  /**
   * Constructs the equivalent of an ElasticSearch multi match query across
   * multiple nested fields.
   *
   * <p>Since ElasticSearch doesn't really let you do that, we have to be clever
   * and construct an equivalent query using boolean operators to produce
   * the same result.
   *
   * @param sb - The string builder to use to build the query.
   */
  public void toElasticSearchNestedMultiMatchQuery(StringBuilder sb) {

    // Break out our whitespace delimited list of fields and values into a actual lists.
    List<String> fields = Arrays.asList(field.split(" "));
    List<String> values = Arrays.asList(((String) value).split(" ")); // GDF: revisit this cast.

    sb.append("\"bool\": {");

    if (operator != null) {

      if (operator.toLowerCase().equals("and")) {
        sb.append("\"must\": [");
      } else if (operator.toLowerCase().equals("or")) {
        sb.append("\"should\": [");
      }

    } else {
      sb.append("\"must\": [");
    }

    AtomicBoolean firstField = new AtomicBoolean(true);
    for (String f : fields) {

      if (!firstField.compareAndSet(true, false)) {
        sb.append(", ");
      }

      sb.append("{ ");

      // Is this a nested field?
      if (fieldIsNested(f)) {

        sb.append("\"nested\": {");
        sb.append("\"path\": \"").append(pathForNestedField(f)).append("\", ");
        sb.append("\"query\": ");
      }

      sb.append("{\"bool\": {");
      sb.append("\"should\": [");

      AtomicBoolean firstValue = new AtomicBoolean(true);
      for (String v : values) {
        if (!firstValue.compareAndSet(true, false)) {
          sb.append(", ");
        }
        sb.append("{\"match\": { \"");
        sb.append(f).append("\": {\"query\": \"").append(v).append("\"");

        if (searchAnalyzer != null) {
          sb.append(", \"analyzer\": \"").append(searchAnalyzer).append("\"");
        }
        sb.append("}}}");
      }

      sb.append("]");
      sb.append("}");

      if (fieldIsNested(f)) {
        sb.append("}");
        sb.append("}");
      }

      sb.append("}");
    }

    sb.append("]");
    sb.append("}");
  }


  @Override
  public String toString() {
    return "field: " + field + ", value: " + value + " (" + value.getClass().getName() + ")";
  }

  public boolean fieldIsNested(String field) {
    return field.contains(".");
  }

  public String pathForNestedField(String field) {
    int index = field.lastIndexOf('.');
    return field.substring(0, index);
  }
}
