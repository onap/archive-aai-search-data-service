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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.sa.searchdbabstraction.logging.SearchDbMsgs;


/**
 * This class as the name suggests is to translate the payload of PUT & POST requests
 * to ElasticSearch (ES) to its compatible syntax, specially compatible with ES v6 or above.
 * 
 * For example, data type such as "string" is now replaced by "text" or "keyword". 
 * 
 * So this class will make those translations reading off from a json configuration file, therefore
 * the configuration can be updated with new translations as and when required without touching the code. 
 * 
 * @author EDWINL
 *
 */
public class ElasticSearchPayloadTranslator {

	private static Logger logger = LoggerFactory.getInstance().getLogger(ElasticSearchPayloadTranslator.class.getName());
	private static final String CONFIG_DIRECTORY = System.getProperty("CONFIG_HOME");
	private static final String ES_PAYLOAD_TRANSLATION_FILE = "es-payload-translation.json";


	/**
	 *  Using String replacement to translate the payload to ES compatible version
	 * 
	 * @param source
	 * @return translated payload in String
	 * @throws IOException
	 */
	public static String translateESPayload(String source) throws IOException {
		logger.info(SearchDbMsgs.PROCESS_PAYLOAD_QUERY, "translateESPayload, method-params[ source=" + source + "]");
		String pathToTranslationFile = CONFIG_DIRECTORY + File.separator + ES_PAYLOAD_TRANSLATION_FILE;
		
		String translatedPayload = source.replaceAll("\\s+", ""); // removing whitespaces
		JSONObject translationConfigPayload = new JSONObject(IOUtils.toString(
				new FileInputStream(new File(pathToTranslationFile)), "UTF-8"));

		JSONArray attrTranslations = translationConfigPayload.getJSONArray("attr-translations");

		for(Object obj : attrTranslations) {
			JSONObject jsonObj = ((JSONObject)obj);
			String from = jsonObj.get("from").toString();
			String to = jsonObj.get("to").toString();
			if(translatedPayload.indexOf(from) > 0) {
				translatedPayload = translatedPayload.replaceAll(from, to);
			}
		}

		logger.info(SearchDbMsgs.PROCESS_PAYLOAD_QUERY, "Payload after translation: "+translatedPayload);
		return translatedPayload;
	}
}
