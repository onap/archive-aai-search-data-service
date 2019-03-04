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

import org.junit.Assert;
import org.junit.Test;


public class SettingConfigurationTest {

    @Test
    public void settingConfigTest() throws Exception {
        SettingConfiguration config = new SettingConfiguration();
        config.init("src/test/resources/json/settings-config.json");
        String settings = config.getSettings();
        System.out.println("SettingsConfig:\n" + settings);
        Assert.assertTrue(settings.contains("number_of_shards"));
    }
    
    @Test
    public void settingConfigAnalysisTest() throws Exception {
        AnalysisConfiguration ac = new AnalysisConfiguration();
        ac.init("src/test/resources/json/filter-config.json", "src/test/resources/json/analysis-config.json");
        System.out.println("AnalysisConfig:\n" + ac.buildEsIndexSettings());
        
        SettingConfiguration config = new SettingConfiguration();
        config.init("src/test/resources/json/settings-config.json");
        String settings = config.getSettingsWithAnalysis(ac);
        System.out.println("SettingsAnalysisConfig:\n" + settings);
        Assert.assertTrue(settings.contains("number_of_shards"));
        Assert.assertTrue(settings.contains("nGram_analyzer"));
        
        config = new SettingConfiguration();
        config.init("src/test/resources/json/missing-file.json");
        settings = config.getSettingsWithAnalysis(ac);
        System.out.println("SettingsAnalysisConfigMissing:\n" + settings);
        Assert.assertFalse(ac.getEsIndexSettings().isEmpty());
    }
}
