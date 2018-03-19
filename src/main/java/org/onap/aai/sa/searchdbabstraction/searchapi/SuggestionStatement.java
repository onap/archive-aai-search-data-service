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


import org.radeox.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents the structure of a search statement.
 *
 * <p>The expected JSON structure to represent a Completion suggest search statement is as follows:
 *
 *  { "suggest-vnf" : { "text" : "VNFs", "completion" : { "field" : "entity_suggest", "size": 1 } } }
 */
public class SuggestionStatement {

	
	  @JsonProperty("results-size")
	  private Integer size;
	  
	  @JsonProperty("suggest-field")
	  private String field;
	  
	  @JsonProperty("suggest-text")
	  private String text;

	  public Integer getSize() {
	    return size;
	  }

	  public void setSize(Integer size) {
	    this.size = size;
	  }
	  
	  public String getField() {
		  return field;
		  }

	  public void setField(String field) {
		  this.field = field;
		  }
		  
	  public String getText() {
		  return text;
		  }

	  public void setText(String text) {
		  this.text = text;
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
	  
	    sb.append("{"); 
	    sb.append("\"suggest-vnf\": {");
	    sb.append("\"text\": ").append("\"" + text + "\"").append(", ");
	    sb.append("\"completion\": {");
	    sb.append("\"field\": ").append("\"" + field + "\"").append(", ");
	    sb.append("\"size\": ").append(size);
	    sb.append("}");
	    sb.append("}");
	    sb.append("}");

	    Logger.debug("Generated raw ElasticSearch suggest statement: " + sb.toString());
	    return sb.toString();
	  }

	 
}
