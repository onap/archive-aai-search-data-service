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


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


/**
 * This suite of tests validates the behaviour of the bulk operations REST end point.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = org.onap.aai.sa.Application.class)
@AutoConfigureMockMvc
public class BulkApiTest {

    private final String TOP_URI = "/test/bulk";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void authenticationFailureTest() throws Exception {

        this.mockMvc
                .perform(post(TOP_URI).contentType(MediaType.APPLICATION_JSON)
                        .content(SearchServiceApiHarness.FAIL_AUTHENTICATION_TRIGGER))
                .andExpect(status().isForbidden());
    }


    /**
     * This test validates that properly constructed json payloads are correctly validated and that improperly
     * contructed payloads will be rejected with the appropriate response code returned to the client.
     *
     * @throws IOException
     */
    @Test
    public void payloadValidationTest() throws Exception {

        // Post a request to the bulk operations endpoint with a valid
        // operations list payload.
        File validBulkOpsFile = new File("src/test/resources/json/bulk-ops-valid.json");
        String validPayloadStr = TestUtils.readFileToString(validBulkOpsFile);

        // Validate that the payload is accepted as expected.
        this.mockMvc.perform(post(TOP_URI).contentType(MediaType.APPLICATION_JSON).content(validPayloadStr))
                .andExpect(status().isOk());


        // Post a request to the bulk operations endpoint with an invalid
        // operations list payload.
        File inValidBulkOpsFile = new File("src/test/resources/json/bulk-ops-invalid.json");
        String inValidPayloadStr = TestUtils.readFileToString(inValidBulkOpsFile);
        ResultActions invalid =
                this.mockMvc.perform(post(TOP_URI).contentType(MediaType.APPLICATION_JSON).content(inValidPayloadStr))
                        .andExpect(status().isBadRequest());
    }
}
