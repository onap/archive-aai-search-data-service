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
package org.onap.aai.sa.searchdbabstraction.searchapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class TermQueryTest {

    @Test
    public void testAllMethods() throws IOException {

        String field = "searchTags.nested";
        String stringValue = "theValue.nested";
        String termQueryWithStringValueJson = "{\"field\": \"" + field + "\", \"value\": \"" + stringValue + "\"}";
        String termQueryWithStringValueExpectedES = "{\"term\": {\"" + field + "\" : \"" + stringValue + "\"}}";

        ObjectMapper mapper = new ObjectMapper();
        TermQuery stringTermQuery = mapper.readValue(termQueryWithStringValueJson, TermQuery.class);
        Assert.assertEquals(stringValue, stringTermQuery.getValue());
        Assert.assertEquals("searchTags.nested", stringTermQuery.getField());
        stringTermQuery.setOperator("operator-1");
        Assert.assertEquals("operator-1", stringTermQuery.getOperator());
        stringTermQuery.setSearchAnalyzer("search-1");
        Assert.assertEquals("search-1", stringTermQuery.getSearchAnalyzer());

        String field1 = "searchTags-1 searchTags.second";
        String stringValue1 = "theValue-1 theValue.second";
        String multiFieldTermQueryJSon = "{\"field\": \"" + field1 + "\", \"value\": \"" + stringValue1 + "\"}";
        TermQuery multiFieldTermQuery = mapper.readValue(multiFieldTermQueryJSon, TermQuery.class);
        multiFieldTermQuery.setOperator("and");
        multiFieldTermQuery.setSearchAnalyzer("search-1");
        Assert.assertNotNull(multiFieldTermQuery.toElasticSearch());
        Assert.assertNotNull(multiFieldTermQuery.pathForNestedField(field1));

        String field2 = "search11 search2";
        String stringValue2 = "theValue1 theValue2";
        String multiFieldTermJSon = "{\"field\": \"" + field2 + "\", \"value\": \"" + stringValue2 + "\"}";
        TermQuery multiFieldTerm = mapper.readValue(multiFieldTermJSon, TermQuery.class);
        multiFieldTerm.setOperator("or");
        multiFieldTerm.setSearchAnalyzer("search-1");
        Assert.assertNotNull(multiFieldTerm.toElasticSearch());
    }

}
