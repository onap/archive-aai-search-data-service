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
package org.onap.aai.sa.searchdbabstraction.util;

public class SearchDbConstants {

    public static final String SDB_FILESEP =
            (System.getProperty("file.separator") == null) ? "/" : System.getProperty("file.separator");
    public static final String SDB_BUNDLECONFIG_NAME =
            (System.getProperty("BUNDLECONFIG_DIR") == null) ? "bundleconfig" : System.getProperty("BUNDLECONFIG_DIR");

    public static final String SDB_HOME_BUNDLECONFIG = (System.getProperty("AJSC_HOME") == null)
            ? SDB_FILESEP + "opt" + SDB_FILESEP + "app" + SDB_FILESEP + "searchdb" + SDB_FILESEP + SDB_BUNDLECONFIG_NAME
            : System.getProperty("AJSC_HOME") + SDB_FILESEP + SDB_BUNDLECONFIG_NAME;

    public static final String SDB_HOME_ETC = SDB_HOME_BUNDLECONFIG + SDB_FILESEP + "etc" + SDB_FILESEP;
    public static final String SDB_CONFIG_APP_LOCATION = SDB_HOME_ETC + "appprops" + SDB_FILESEP;

    // Elastic Search related
    public static final String SDB_SPECIFIC_CONFIG = (System.getProperty("CONFIG_HOME") == null)
            ? SDB_CONFIG_APP_LOCATION : System.getProperty("CONFIG_HOME") + SDB_FILESEP;
    public static final String ES_CONFIG_FILE = SDB_SPECIFIC_CONFIG + SDB_FILESEP + "elastic-search.properties";
    public static final String SDB_AUTH = SDB_SPECIFIC_CONFIG + "auth" + SDB_FILESEP;
    public static final String SDB_AUTH_CONFIG_FILENAME = SDB_AUTH + "search_policy.json";
    public static final String SDB_FILTER_CONFIG_FILE = SDB_SPECIFIC_CONFIG + "filter-config.json";
    public static final String SDB_ANALYSIS_CONFIG_FILE = SDB_SPECIFIC_CONFIG + "analysis-config.json";

    // Logging related
    public static final String SDB_SERVICE_NAME = "SearchDataService";

    private SearchDbConstants() { // Do not instantiate
    }

}
