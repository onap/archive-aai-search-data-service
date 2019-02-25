/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2019 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2019 Amdocs
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
package org.onap.aai.sa.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import org.onap.aai.sa.searchdbabstraction.util.SearchDbConstants;


public class SettingConfiguration {

    /**
     * Indicates whether or not we have imported the filter and analyzer configurations.
     */
    private AtomicBoolean configured = new AtomicBoolean(false);

    /**
     * A json format string which is readable by Elastic Search and defines all of the custom filters and analyzers that
     * we need Elastic Search to know about.
     */
    private String settings;

    public void init(String settingConfigFile) {

        if (configured.compareAndSet(false, true)) {
            try {
                Path path = Paths.get(settingConfigFile);
                settings = new String(Files.readAllBytes(path));
                
                // Remove the enclosing brackets from the json blob.
                settings = settings.replaceFirst("\\{", "");
                settings = settings.substring(0, settings.lastIndexOf("}"));        
            } catch (IOException e) {
                // It is valid not to have a settings file.
                settings = "";
            }
        }
    }


    /**
     * Returns the set of pre-configured settings.
     *
     * @return - settings.
     */
    public String getSettings() {
        init(SearchDbConstants.SDB_SETTINGS_CONFIG_FILE);
        return settings;
    }
    
    public String getSettingsWithAnalysis(AnalysisConfiguration analysisConfig) {
        String ac = analysisConfig.getEsIndexSettings();
        StringBuilder sb = new StringBuilder();
        sb.append(ac.substring(0, ac.lastIndexOf("}")));
        
        if (!getSettings().trim().isEmpty()) {
            sb.append(", " + getSettings());
        }
        
        sb.append(" }");
        return sb.toString();
    }
}
