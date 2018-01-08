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
package org.onap.aai.sa.auth;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.sa.searchdbabstraction.util.SearchDbConstants;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class SearchDbServiceAuthTest {

    @Mock
    HttpHeaders headers;

    @Mock
    Cookie mockedCookie;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException, IOException {
        MockitoAnnotations.initMocks(this);
        System.setProperty("AJSC_HOME", new File(".").getCanonicalPath().replace('\\', '/'));
        setFinalStatic(System.getProperty("AJSC_HOME")+"/src/test/resources/json/search_policy.json");
    }

    @Test
    public void testAuthUser(){
        SearchDbServiceAuth aaiAuth = new SearchDbServiceAuth();
        String auth = aaiAuth.authUser(headers, "user-1", "function-1");
        Assert.assertEquals(auth, "AAI_9101");
    }

    @Test
    public void testAuthCookie_NullCookie(){
        SearchDbServiceAuth aaiAuth = new SearchDbServiceAuth();
        Cookie cookie = null;
        Assert.assertFalse(aaiAuth.authCookie(cookie, "function-1", new StringBuilder("user-1")));
    }

    @Test
    public void testAuthCookie_NotNullCookie(){
        SearchDbServiceAuth aaiAuth = new SearchDbServiceAuth();
        boolean retValue = aaiAuth.authCookie(mockedCookie, "GET:testFunction", new StringBuilder("testuser"));
        Assert.assertTrue(retValue);
    }

    static void setFinalStatic(String fieldValue) throws NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        Field configField = SearchDbConstants.class.getDeclaredField("SDB_AUTH_CONFIG_FILENAME");
        configField.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField( "modifiers" );
        modifiersField.setAccessible( true );
        modifiersField.setInt( configField, configField.getModifiers() & ~Modifier.FINAL );

        configField.set(null, fieldValue);
    }
}
