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

package org.onap.aai.sa.searchdbabstraction;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes REST endpoints for a simple echo service.
 */
@RestController
@RequestMapping("/services/search-data-service/v1")
public class RestEchoService {

    /**
     * REST endpoint for a simple echo service.
     *
     * @param input - The value to be echoed back.
     * @return - The input value.
     */
    @RequestMapping(value = "/echo/{input}", method = {RequestMethod.GET})
    public String ping(@PathVariable("input") String input) {
        return "[Search Database Abstraction Micro Service] - Echo Service: " + input + ".";
    }

}
