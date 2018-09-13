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
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.sa.rest.TestUtils;

public class ElasticSearchPayloadTranslatorTest {
	
	private final String SIMPLE_DOC_SCHEMA_JSON = "src/test/resources/json/index-mapping.json";

	@Before
	public void setup() throws Exception {
		System.setProperty("CONFIG_HOME", System.getProperty("user.dir")+ File.separator + "src/test/resources/json");
	}
	
	@Test
	public void testPayloadTranslation() throws Exception {
		String expectedErrMsg = "Sample error message for whitespace check";
		File schemaFile = new File(SIMPLE_DOC_SCHEMA_JSON);
	    String documentJson = IOUtils.toString(new FileInputStream(schemaFile), "UTF-8");
	    assertTrue(documentJson.contains("\"type\": \"string\""));
		assertTrue(documentJson.contains("\"index\": \"analyzed\""));
		String translatedPayload = ElasticSearchPayloadTranslator.translateESPayload(documentJson);
		assertTrue(translatedPayload.contains("\"type\":\"text\""));
		assertTrue(translatedPayload.contains("\"index\":true"));
		assertTrue(translatedPayload.contains("\"fielddata\":true"));
		assertFalse(documentJson.contains("\"index\":\"analyzed\""));
		assertTrue(translatedPayload.contains("\"errMsg\":\""+expectedErrMsg+"\""));
	}
}
