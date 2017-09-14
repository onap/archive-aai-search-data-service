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
package org.onap.aai.sa.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.onap.aai.sa.searchdbabstraction.util.SearchDbConstants;
import org.openecomp.cl.api.Logger;
import org.openecomp.cl.eelf.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class SearchDbServiceAuthCore {

  private static Logger logger = LoggerFactory.getInstance()
      .getLogger(SearchDbServiceAuthCore.class.getName());

  private static String GlobalAuthFileName = SearchDbConstants.SDB_AUTH_CONFIG_FILENAME;

  private static enum HTTP_METHODS {
    POST, GET, PUT, DELETE
  }

  ;

  // Don't instantiate
  private SearchDbServiceAuthCore() {
  }

  private static boolean usersInitialized = false;
  private static HashMap<String, SearchDbAuthUser> users;
  private static boolean timerSet = false;
  private static Timer timer = null;

  public synchronized static void init() {


    SearchDbServiceAuthCore.getConfigFile();
    SearchDbServiceAuthCore.reloadUsers();

  }

  public static void cleanup() {
    timer.cancel();
  }

  public static String getConfigFile() {
    if (GlobalAuthFileName == null) {
      String nc = SearchDbConstants.SDB_AUTH_CONFIG_FILENAME;
      if (nc == null) {
        nc = "/home/aaiadmin/etc/aaipolicy.json";
      }

      GlobalAuthFileName = nc;
    }
    return GlobalAuthFileName;
  }

  public synchronized static void reloadUsers() {
    users = new HashMap<String, SearchDbAuthUser>();


    ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
    JSONParser parser = new JSONParser();
    try {
      Object obj = parser.parse(new FileReader(GlobalAuthFileName));
      // aailogger.debug(logline, "Reading from " + GlobalAuthFileName);
      JsonNode rootNode = mapper.readTree(new File(GlobalAuthFileName));
      JsonNode rolesNode = rootNode.path("roles");

      for (JsonNode roleNode : rolesNode) {
        String roleName = roleNode.path("name").asText();

        TabularAuthRole authRole = new TabularAuthRole();
        JsonNode usersNode = roleNode.path("users");
        JsonNode functionsNode = roleNode.path("functions");
        for (JsonNode functionNode : functionsNode) {
          String function = functionNode.path("name").asText();
          JsonNode methodsNode = functionNode.path("methods");
          boolean hasMethods = false;
          for (JsonNode methodNode : methodsNode) {
            String methodName = methodNode.path("name").asText();
            hasMethods = true;
            String thisFunction = methodName + ":" + function;

            authRole.addAllowedFunction(thisFunction);
          }

          if (hasMethods == false) {
            // iterate the list from HTTP_METHODS
            for (HTTP_METHODS meth : HTTP_METHODS.values()) {
              String thisFunction = meth.toString() + ":" + function;

              authRole.addAllowedFunction(thisFunction);
            }
          }

        }
        for (JsonNode userNode : usersNode) {
          // make the user lower case
          String username = userNode.path("username").asText().toLowerCase();
          SearchDbAuthUser authUser = null;
          if (users.containsKey(username)) {
            authUser = users.get(username);
          } else {
            authUser = new SearchDbAuthUser();
          }


          authUser.setUser(username);
          authUser.addRole(roleName, authRole);
          users.put(username, authUser);
        }
      }
    } catch (FileNotFoundException fnfe) {
      logger.debug("Failed to load the policy file ");

    } catch (ParseException e) {
      logger.debug("Failed to Parse the policy file ");

    } catch (JsonProcessingException e) {
      logger.debug("JSON processing error while parsing policy file: " + e.getMessage());

    } catch (IOException e) {
      logger.debug("IO Exception while parsing policy file: " + e.getMessage());
    }

    usersInitialized = true;

  }

  public static class SearchDbAuthUser {
    public SearchDbAuthUser() {
      this.roles = new HashMap<String, TabularAuthRole>();
    }

    private String username;
    private HashMap<String, TabularAuthRole> roles;

    public String getUser() {
      return this.username;
    }

    public HashMap<String, TabularAuthRole> getRoles() {
      return this.roles;
    }

    public void addRole(String roleName, TabularAuthRole authRole) {
      this.roles.put(roleName, authRole);
    }

    public boolean checkAllowed(String checkFunc) {
      for (Map.Entry<String, TabularAuthRole> roleEntry : this.roles.entrySet()) {
        TabularAuthRole role = roleEntry.getValue();
        if (role.hasAllowedFunction(checkFunc)) {
          // break out as soon as we find it
          return true;
        }
      }
      // we would have got positive confirmation had it been there
      return false;
    }

    public void setUser(String myuser) {
      this.username = myuser;
    }

  }

  public static class TabularAuthRole {
    public TabularAuthRole() {
      this.allowedFunctions = new ArrayList<String>();
    }

    private List<String> allowedFunctions;

    public void addAllowedFunction(String func) {
      this.allowedFunctions.add(func);
    }

    public void delAllowedFunction(String delFunc) {
      if (this.allowedFunctions.contains(delFunc)) {
        this.allowedFunctions.remove(delFunc);
      }
    }

    public boolean hasAllowedFunction(String afunc) {
      if (this.allowedFunctions.contains(afunc)) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static HashMap<String, SearchDbAuthUser> getUsers(String key) {
    if (!usersInitialized || (users == null)) {
      reloadUsers();
    }
    return users;
  }

  public static boolean authorize(String username, String authFunction) {
    // logline.init(component, transId, fromAppId, "authorize()");

    if (!usersInitialized || (users == null)) {
      init();
    }
    if (users.containsKey(username)) {
      if (users.get(username).checkAllowed(authFunction) == true) {

        return true;
      } else {


        return false;
      }
    } else {

      return false;
    }
  }
}
