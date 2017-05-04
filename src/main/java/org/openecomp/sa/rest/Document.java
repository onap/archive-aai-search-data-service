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
package org.openecomp.sa.rest;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class Document {
  private Map<String, Object> fields = new HashMap<String, Object>();

  @JsonAnyGetter
  public Map<String, Object> getFields() {
    return fields;
  }

  @JsonAnySetter
  public void setField(String name, Object value) {
    fields.put(name, value);
  }

  public String toJson() throws JsonProcessingException {

    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(this);
  }


  @Override
  public String toString() {
    String str = "Document: [";
    for (String key : fields.keySet()) {
      str += key + ": " + fields.get(key);
    }
    str += "]";

    return str;
  }
}
