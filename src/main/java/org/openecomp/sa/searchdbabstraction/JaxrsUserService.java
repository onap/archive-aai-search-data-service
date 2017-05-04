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
package org.openecomp.sa.searchdbabstraction;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/user")
public class JaxrsUserService {

  private static final Map<String, String> userIdToNameMap;

  static {
    userIdToNameMap = new HashMap<String, String>();
    userIdToNameMap.put("dw113c", "Doug Wait");
    userIdToNameMap.put("so401q", "Stuart O'Day");
  }

  @GET
  @Path("/{userId}")
  @Produces("text/plain")
  public String lookupUser(@PathParam("userId") String userId) {
    String name = userIdToNameMap.get(userId);
    return name != null ? name : "unknown id";
  }

}