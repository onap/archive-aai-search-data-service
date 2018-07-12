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

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.onap.aai.sa.rest.TestUtils;

public class ElasticSearchPayloadTranslatorTest {
	
	private final String SIMPLE_DOC_SCHEMA_JSON = "src/test/resources/json/simpleDocument.json";

	@Before
	public void setup() throws Exception {
		System.setProperty("CONFIG_HOME", System.getProperty("user.dir")+ File.separator + "appconfig-local");
	}
	
	@Test
	public void testPayloadTranslation_FromStringToText() throws Exception {
		File schemaFile = new File(SIMPLE_DOC_SCHEMA_JSON);
	    String documentJson = TestUtils.readFileToString(schemaFile);
	    assertTrue(documentJson.contains("\"data-type\":\"string\""));
		assertTrue(documentJson.contains("\"searchable\":true"));
		String translatedPayload = ElasticSearchPayloadTranslator.translateESPayload(documentJson);
		assertTrue(translatedPayload.contains("\"data-type\":\"text\""));
		assertTrue(translatedPayload.contains("\"index\":true"));
	}
}
