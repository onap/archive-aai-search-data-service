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
package org.onap.aai.sa.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.sa.searchdbabstraction.logging.SearchDbMsgs;
import org.onap.aai.sa.searchdbabstraction.util.SearchDbConstants;

/**
 * This class encapsulates the configuration of the predefined Analyzer and Filter behaviours that help to tell the
 * document store how to index the documents that are provided to it.
 */
public class AnalysisConfiguration {

    /**
     * Contains all of the predefined indexing filters.
     */
    private FilterSchema[] customFilters;

    /**
     * Contains all of the predefined indexing analyzers.
     */
    private AnalyzerSchema[] customAnalysers;

    /**
     * Indicates whether or not we have imported the filter and analyzer configurations.
     */
    private AtomicBoolean configured = new AtomicBoolean(false);

    /**
     * A json format string which is readable by Elastic Search and defines all of the custom filters and analyzers that
     * we need Elastic Search to know about.
     */
    private String esSettings = null;

    private static Logger logger = LoggerFactory.getInstance().getLogger(AnalysisConfiguration.class.getName());


    /**
     * Imports the filter and analyzer configuration files and builds an Elastic Search readable settings file from the
     * contents.
     *
     * @param filterConfigFile - Location of filter configuration json file
     * @param analyzerConfigFile - Location of analyzer configuration json file
     */
    public void init(String filterConfigFile, String analyzerConfigFile) {

        if (configured.compareAndSet(false, true)) {
            ObjectMapper mapper = new ObjectMapper();

            File filtersConfig = new File(filterConfigFile);
            try {
                customFilters = mapper.readValue(filtersConfig, FilterSchema[].class);
            } catch (IOException e) {

                // generate log
                logger.warn(SearchDbMsgs.FILTERS_CONFIG_FAILURE, filterConfigFile, e.getMessage());
            }

            File analysersConfig = new File(analyzerConfigFile);
            try {
                customAnalysers = mapper.readValue(analysersConfig, AnalyzerSchema[].class);
            } catch (IOException e) {

                // generate log
                logger.warn(SearchDbMsgs.ANALYSYS_CONFIG_FAILURE, analyzerConfigFile, e.getMessage());
            }

            esSettings = buildEsIndexSettings();
        }
    }


    /**
     * Returns the set of pre-configured filters.
     *
     * @return - An array of filters.
     */
    public FilterSchema[] getFilters() {
        return customFilters;
    }


    /**
     * Returns the set of pre-configured analyzers.
     *
     * @return - An array of analyzers.
     */
    public AnalyzerSchema[] getAnalyzers() {
        init(SearchDbConstants.SDB_FILTER_CONFIG_FILE, SearchDbConstants.SDB_ANALYSIS_CONFIG_FILE);
        return customAnalysers;
    }


    /**
     * Imports the filter and analyzer configurations and translates those into a settings string that will be parseable
     * by Elastic Search.
     *
     * @return - Elastic Search formatted settings string.
     */
    public String getEsIndexSettings() {

        // Generate the es-settings string from our filter and analyzer
        // configurations if we have not already done so.
        init(SearchDbConstants.SDB_FILTER_CONFIG_FILE, SearchDbConstants.SDB_ANALYSIS_CONFIG_FILE);

        // Now, return the es-settings string.
        return esSettings;
    }


    /**
     * Constructs a settings string that is readable by Elastic Search based on the contents of the filter and analyzer
     * configuration files.
     *
     * @return Elastic Search formatted settings string.
     */
    public String buildEsIndexSettings() {

        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\"analysis\": {");

        // Define the custom filters.
        boolean atLeastOneFilter = false;
        sb.append("\"filter\": {");
        AtomicBoolean firstFilter = new AtomicBoolean(true);
        for (FilterSchema filter : customFilters) {

            // Append a comma before the next entry, unless it is the
            // first one.
            if (!firstFilter.compareAndSet(true, false)) {
                sb.append(", ");
            }

            // Now, build the filter entry.
            buildFilterEntry(filter, sb);
            atLeastOneFilter = true;
        }
        sb.append((atLeastOneFilter) ? "}," : "}");

        // Define the custom analyzers.
        sb.append("\"analyzer\": {");
        AtomicBoolean firstAnalyzer = new AtomicBoolean(true);
        for (AnalyzerSchema analyzer : customAnalysers) {

            // Prepend a comma before the entry, unless it is the
            // first one.
            if (!firstAnalyzer.compareAndSet(true, false)) {
                sb.append(",");
            }

            // Now, construct the entry for this analyzer.
            buildAnalyzerEntry(analyzer, sb);
        }
        sb.append("}");

        sb.append("}");
        sb.append("}");

        return sb.toString();
    }


    /**
     * Constructs an ElasticSearch friendly custom filter definition.
     *
     * @param filter - The filter to generate ElasticSearch json for.
     * @param sb - The string builder to append the filter definition to.
     */
    private void buildFilterEntry(FilterSchema filter, StringBuilder sb) {

        sb.append("\"" + filter.getName()).append("\": {");

        sb.append(filter.getConfiguration());

        sb.append("}");
    }


    /**
     * Constructs an ElasticSearch friendly custom analyzer definition.
     *
     * @param analyzer - The analyzer to generate ElasticSearch json for.
     * @param sb - The string builder to append the analyzer definition to.
     */
    private void buildAnalyzerEntry(AnalyzerSchema analyzer, StringBuilder sb) {

        sb.append("\"").append(analyzer.getName()).append("\": {");
        sb.append("\"type\": \"custom\",");
        sb.append("\"tokenizer\": ").append("\"").append(analyzer.getTokenizer()).append("\",");
        sb.append("\"filter\": [");
        boolean firstFilter = true;
        for (String filter : analyzer.getFilters()) {
            if (!firstFilter) {
                sb.append(",");
            } else {
                firstFilter = false;
            }
            sb.append("\"").append(filter).append("\"");
        }
        sb.append("]");
        sb.append("}");
    }
}
