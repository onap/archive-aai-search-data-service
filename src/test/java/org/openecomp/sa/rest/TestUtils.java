/**
 * ============LICENSE_START=======================================================
 * Search Data Service
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property.
 * Copyright © 2017 Amdocs
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License ati
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 * ECOMP and OpenECOMP are trademarks
 * and service marks of AT&T Intellectual Property.
 */
package org.openecomp.sa.rest;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TestUtils {

  /**
   * This helper method reads the contents of a file into a
   * simple string.
   *
   * @param aFile - The file to be imported.
   *
   * @return - The file contents expressed as a simple string.
   *
   * @throws IOException
   */
  public static String readFileToString(File aFile) throws IOException {

    BufferedReader br = new BufferedReader(new FileReader(aFile));
    try {
      StringBuilder sb = new StringBuilder();
      String line = br.readLine();

      while (line != null) {
        sb.append(line);
        line = br.readLine();
      }

      return sb.toString().replaceAll("\\s+", "");
    } finally {
      try {
        br.close();
      } catch (IOException e) {
        fail("Unexpected IOException: " + e.getMessage());
      }
    }
  }
}