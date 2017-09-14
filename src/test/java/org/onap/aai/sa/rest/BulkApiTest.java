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
package org.onap.aai.sa.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertEquals;


/**
 * This suite of tests validates the behaviour of the bulk operations REST
 * end point.
 */
public class BulkApiTest extends JerseyTest {

  private final String TOP_URI = "/test/bulk/";


  @Override
  protected Application configure() {

    // Make sure that our test endpoint is on the resource path
    // for Jersey Test.
    return new ResourceConfig(SearchServiceApiHarness.class);
  }


  /**
   * This test validates that the expected response codes are returned
   * to the client in the event of an authentication failure.
   */
  @Test
  public void authenticationFailureTest() {

    // Send a request to the end point, with a special trigger in the
    // payload that tells our test harness to force the authentication
    // to fail.
    Response result = target(TOP_URI).request().post(Entity.json(SearchServiceApiHarness.FAIL_AUTHENTICATION_TRIGGER), Response.class);

    // Validate that a failure to authenticate results in the expected
    // response code returned to the client.
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), result.getStatus());
  }


  /**
   * This test validates that properly constructed json payloads are
   * correctly validated and that improperly contructed payloads will
   * be rejected with the appropriate response code returned to the
   * client.
   *
   * @throws IOException
   */
  @Test
  public void payloadValidationTest() throws IOException {

    // Post a request to the bulk operations endpoint with a valid
    // operations list payload.
    File validBulkOpsFile = new File("src/test/resources/json/bulk-ops-valid.json");
    String validPayloadStr = TestUtils.readFileToString(validBulkOpsFile);
    Response validResult = target(TOP_URI).request().post(Entity.json(validPayloadStr), Response.class);

    // Validate that the payload is accepted as expected.
    assertEquals("Valid operations payload was rejected",
        Response.Status.OK.getStatusCode(), validResult.getStatus());

    // Post a request to the bulk operations endpoint with an invalid
    // operations list payload.
    File inValidBulkOpsFile = new File("src/test/resources/json/bulk-ops-invalid.json");
    String inValidPayloadStr = TestUtils.readFileToString(inValidBulkOpsFile);
    Response invalidResult = target(TOP_URI).request().post(Entity.json(inValidPayloadStr), Response.class);

    // Validate that the payload is rejected as expected.
    assertEquals("Invalid operations payload was not rejected",
        Response.Status.BAD_REQUEST.getStatusCode(), invalidResult.getStatus());
  }
}
