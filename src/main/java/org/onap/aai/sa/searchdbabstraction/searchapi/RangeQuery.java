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
 * This class represents a simple range query.
 *
 * <p>A range query is composed of one or more operator/value pairs which define
 * the upper and lower bounds of the range, and a field to apply the query to.
 *
 * <p>Operators may be one of the following:
 * <ul>
 * <li>gt  - Greater than. </li>
 * <li>gte - Greater than or equal to. </li>
 * <li>lt  - Less than. </li>
 * <li>lte - Less than or equal to. </li>
 * </ul>
 * Values may be either numeric values (Integer or Double) or Strings representing
 * dates.
 *
 * <p>The following examples illustrate a couple of variants of the range query:
 *
 * <p><pre>
 *     // A simple numeric range query:
 *     {
 *         "range": {
 *             "field": "fieldname",
 *             "gte": 5,
 *             "lte": 10
 *         }
 *     }
 *
 *     // A simple date range query:
 *     {
 *         "range": {
 *             "field": "fieldname",
 *             "gt": "2016-10-06T00:00:00.558+03:00",
 *             "lt": "2016-10-06T23:59:59.558+03:00"
 *         }
 *     }
 * </pre>
 */
public class RangeQuery {

  /**
   * The name of the field to apply the range query against.
   */
  private String field;

  /**
   * The value of the field must be greater than this value to be a match.<br>
   * NOTE: Only one of 'gt' or 'gte' should be set on any single {@link RangeQuery}
   * instance.
   */
  private Object gt;

  /**
   * The value of the field must be greater than or equal to this value to be a match.<br>
   * NOTE: Only one of 'gt' or 'gte' should be set on any single {@link RangeQuery}
   * instance.
   */
  private Object gte;

  /**
   * The value of the field must be less than this value to be a match.<br>
   * NOTE: Only one of 'lt' or 'lte' should be set on any single {@link RangeQuery}
   * instance.
   */
  private Object lt;

  /**
   * The value of the field must be less than or equal to than this value to be a match.<br>
   * NOTE: Only one of 'lt' or 'lte' should be set on any single {@link RangeQuery}
   * instance.
   */
  private Object lte;

  private String format;

  @JsonProperty("time-zone")
  private String timeZone;

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public Object getGt() {
    return gt;
  }

  public void setGt(Object gt) {

    // It does not make sense to assign a value to both the 'greater than'
    // and 'greater than or equal' operations, so make sure we are not
    // trying to do that.
    if (gte == null) {

      // Make sure that we are not trying to mix both numeric and date
      // type values in the same queries.
      if (((lt != null) && !typesMatch(gt, lt))
          || ((lte != null) && !typesMatch(gt, lte))) {
        throw new IllegalArgumentException("Cannot mix date and numeric values in the same ranged query");
      }

      // If we made it here, then we're all good.  Store the value.
      this.gt = gt;
    } else {
      throw new IllegalArgumentException("Cannot assign both 'gt' and 'gte' fields in the same ranged query");
    }
  }


  public Object getGte() {
    return gte;
  }

  public void setGte(Object gte) {

    // It does not make sense to assign a value to both the 'greater than'
    // and 'greater than or equal' operations, so make sure we are not
    // trying to do that.
    if (gt == null) {

      // Make sure that we are not trying to mix both numeric and date
      // type values in the same queries.
      if (((lt != null) && !typesMatch(gte, lt))
          || ((lte != null) && !typesMatch(gte, lte))) {
        throw new IllegalArgumentException("Cannot mix date and numeric values in the same ranged query");
      }

      // If we made it here, then we're all good.  Store the value.
      this.gte = gte;

    } else {
      throw new IllegalArgumentException("Cannot assign both 'gt' and 'gte' fields in the same ranged query");
    }
  }

  public Object getLt() {
    return lt;
  }

  public void setLt(Object lt) {

    // It does not make sense to assign a value to both the 'less than'
    // and 'less than or equal' operations, so make sure we are not
    // trying to do that.
    if (lte == null) {

      // Make sure that we are not trying to mix both numeric and date
      // type values in the same queries.
      if (((gt != null) && !typesMatch(lt, gt))
          || ((gte != null) && !typesMatch(lt, gte))) {
        throw new IllegalArgumentException("Cannot mix date and numeric values in the same ranged query");
      }

      // If we made it here, then we're all good.  Store the value.

      this.lt = lt;
    } else {
      throw new IllegalArgumentException("Cannot assign both 'lt' and 'lte' fields in the same ranged query");
    }
  }

  public Object getLte() {
    return lte;
  }

  public void setLte(Object lte) {

    // It does not make sense to assign a value to both the 'greater than'
    // and 'greater than or equal' operations, so make sure we are not
    // trying to do that.
    if (lt == null) {

      // Make sure that we are not trying to mix both numeric and date
      // type values in the same queries.
      if (((gt != null) && !typesMatch(lte, gt))
          || ((gte != null) && !typesMatch(lte, gte))) {
        throw new IllegalArgumentException("Cannot mix date and numeric values in the same ranged query");
      }

      // If we made it here, then we're all good.  Store the value.

      this.lte = lte;
    } else {
      throw new IllegalArgumentException("Cannot assign both 'lt' and 'lte' fields in the same ranged query");
    }
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  /**
   * This convenience method determines whether or not the supplied
   * value needs to be enclosed in '"' characters when generating
   * ElasticSearch compatible syntax.
   *
   * @param val - The value to check.
   * @return - A string representation of the value for inclusion
   *     in an ElasticSearch syntax string.
   */
  private String formatStringOrNumericVal(Object val) {

    if (val instanceof String) {
      return "\"" + val.toString() + "\"";
    } else {
      return val.toString();
    }
  }


  /**
   * This convenience method verifies that the supplied objects are
   * of classes considered to be compatible for a ranged query.
   *
   * @param value1 - The first value to check.
   * @param value2 - The second value to check.
   * @return - True if the two objects are compatible for inclusion in the
   *     same ranged query, False, otherwise.
   */
  boolean typesMatch(Object value1, Object value2) {

    return ((value1 instanceof String) && (value2 instanceof String))
        || (!(value1 instanceof String) && !(value2 instanceof String));
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
    sb.append("\"range\": {");
    sb.append("\"").append(field).append("\": {");

    // We may have one or zero of 'greater than' or 'greater
    // than or equal'
    boolean needComma = false;
    if (gte != null) {
      sb.append("\"gte\": ").append(formatStringOrNumericVal(gte));
      needComma = true;
    } else if (gt != null) {
      sb.append("\"gt\": ").append(formatStringOrNumericVal(gt));
      needComma = true;
    }

    // We may have one or zero of 'less than' or 'less
    // than or equal'
    if (lte != null) {
      if (needComma) {
        sb.append(", ");
      }
      sb.append("\"lte\": ").append(formatStringOrNumericVal(lte));
    } else if (lt != null) {
      if (needComma) {
        sb.append(", ");
      }
      sb.append("\"lt\": ").append(formatStringOrNumericVal(lt));
    }

    // Append the format specifier if one was provided.
    if (format != null) {
      sb.append(", \"format\": \"").append(format).append("\"");
    }

    // Append the time zone specifier if one was provided.
    if (timeZone != null) {
      sb.append(", \"time_zone\": \"").append(timeZone).append("\"");
    }

    sb.append("}");
    sb.append("}");
    sb.append("}");

    return sb.toString();
  }

  @Override
  public String toString() {

    String str = "{ field: " + field + ", ";

    if (gt != null) {
      str += "gt: " + gt;
    } else if (gte != null) {
      str += "gte: " + gte;
    }

    if (lt != null) {
      str += (((gt != null) || (gte != null)) ? ", " : "") + "lt: " + lt;
    } else if (lte != null) {
      str += (((gt != null) || (gte != null)) ? ", " : "") + "lte: " + lte;
    }

    str += "}";

    return str;
  }
}
