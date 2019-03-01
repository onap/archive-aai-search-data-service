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

import com.google.common.base.Strings;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status.Family;
import org.onap.aai.cl.mdc.MdcContext;
import org.onap.aai.sa.searchdbabstraction.util.SearchDbConstants;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * Spring Imports.
 *
 */
public class ApiUtils {

    public static final String URL_PREFIX = "services/search-data-service/v1/search";
    public static final String SEARCH_AUTH_POLICY_NAME = "search";

    private static final String URI_SEGMENT_INDEXES = "indexes";
    private static final String URI_SEGMENT_DOCUMENTS = "documents";

    public enum Action {
        POST,
        GET,
        PUT,
        DELETE
    }

    /**
     * This method uses the contents of the supplied HTTP headers and request structures to populate the MDC Context
     * used for logging purposes.
     *
     * @param httpReq - HTTP request structure.
     * @param headers - HTTP headers
     */
    protected static void initMdcContext(HttpServletRequest httpReq, HttpHeaders headers) {
        // Auto generate a transaction if we were not provided one.
        String transId = null;
        if (headers != null) {
            transId = headers.getFirst("X-TransactionId");
            if (Strings.isNullOrEmpty(transId)) {
                transId = UUID.randomUUID().toString();
            }
        }

        String fromIp = (httpReq != null) ? httpReq.getRemoteHost() : "";
        String fromApp = (headers != null) ? headers.getFirst("X-FromAppId") : "";

        MdcContext.initialize(transId, SearchDbConstants.SDB_SERVICE_NAME, "", fromApp, fromIp);
    }

    protected static void clearMdcContext() {
        MDC.clear();
    }

    public static String buildIndexUri(String index) {
        return (URL_PREFIX + "/indexes/") + index;
    }

    public static String buildDocumentUri(String index, String documentId) {
        return buildIndexUri(index) + "/documents/" + documentId;
    }

    public static boolean validateIndexUri(String uri) {
        uri = uri.startsWith("/") ? uri.substring(1) : uri;
        String[] tokens = uri.split("/");
        return (tokens.length == 6) && (tokens[4].equals(URI_SEGMENT_INDEXES));
    }

    public static boolean validateDocumentUri(String uri, boolean requireId) {
        uri = uri.startsWith("/") ? uri.substring(1) : uri;
        String[] tokens = uri.split("/");

        if (requireId) {
            return (tokens.length == 8)
                    && (tokens[4].equals(URI_SEGMENT_INDEXES) && (tokens[6].equals(URI_SEGMENT_DOCUMENTS)));
        } else {
            return ((tokens.length == 8) || (tokens.length == 7))
                    && (tokens[4].equals(URI_SEGMENT_INDEXES) && (tokens[6].equals(URI_SEGMENT_DOCUMENTS)));
        }
    }

    public static String extractIndexFromUri(String uri) {
        uri = uri.startsWith("/") ? uri.substring(1) : uri;

        String[] tokens = uri.split("/");

        int i = 0;
        for (String token : tokens) {
            if (token.equals(URI_SEGMENT_INDEXES) && i + 1 < tokens.length) {
                return tokens[i + 1];
            }
            i++;
        }

        return null;
    }

    public static String extractIdFromUri(String uri) {
        uri = uri.startsWith("/") ? uri.substring(1) : uri;

        String[] tokens = uri.split("/");

        int i = 0;
        for (String token : tokens) {
            if (token.equals(URI_SEGMENT_DOCUMENTS) && i + 1 < tokens.length) {
                return tokens[i + 1];
            }
            i++;
        }

        return null;
    }

    public static String getHttpStatusString(int httpStatusCode) {
        try {
            return HttpStatus.valueOf(httpStatusCode).getReasonPhrase();
        } catch (IllegalArgumentException e) {
            if (207 == httpStatusCode) {
                return "Multi-Status";
            } else {
                return "Unknown";
            }
        }
    }

    public static boolean isSuccessStatusCode(int statusCode) {
        return Family.familyOf(statusCode).equals(Family.SUCCESSFUL);
    }
}
