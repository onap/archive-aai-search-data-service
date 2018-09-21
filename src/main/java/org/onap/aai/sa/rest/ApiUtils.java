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

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.onap.aai.cl.mdc.MdcContext;
import org.onap.aai.sa.searchdbabstraction.util.SearchDbConstants;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

// Spring Imports

public class ApiUtils {

    public static final String SEARCH_AUTH_POLICY_NAME = "search";
    public static final String URL_PREFIX = "services/search-data-service/v1/search";

    public enum Action {
        POST,
        GET,
        PUT,
        DELETE
    };

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

            if ((transId == null) || (transId.equals(""))) {
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

        // If the URI starts with a leading '/' character, remove it.
        uri = uri.startsWith("/") ? uri.substring(1) : uri;

        // Now, tokenize the URI string.
        String[] tokens = uri.split("/");

        return (tokens.length == 6) && (tokens[4].equals("indexes"));

    }

    public static boolean validateDocumentUri(String uri, boolean requireId) {

        // If the URI starts with a leading '/' character, remove it.
        uri = uri.startsWith("/") ? uri.substring(1) : uri;

        // Now, tokenize the URI string.
        String[] tokens = uri.split("/");

        if (requireId) {
            return (tokens.length == 8) && (tokens[4].equals("indexes") && (tokens[6].equals("documents")));
        } else {
            return ((tokens.length == 8) || (tokens.length == 7))
                    && (tokens[4].equals("indexes") && (tokens[6].equals("documents")));
        }
    }

    public static String extractIndexFromUri(String uri) {

        // If the URI starts with a leading '/' character, remove it.
        uri = uri.startsWith("/") ? uri.substring(1) : uri;

        // Now, tokenize the URI string.
        String[] tokens = uri.split("/");

        int i = 0;
        for (String token : tokens) {
            if (token.equals("indexes")) {
                if (i + 1 < tokens.length) {
                    return tokens[i + 1];
                }
            }
            i++;
        }

        return null;
    }

    public static String extractIdFromUri(String uri) {

        // If the URI starts with a leading '/' character, remove it.
        uri = uri.startsWith("/") ? uri.substring(1) : uri;

        // Now, tokenize the URI string.
        String[] tokens = uri.split("/");

        int i = 0;
        for (String token : tokens) {
            if (token.equals("documents")) {
                if (i + 1 < tokens.length) {
                    return tokens[i + 1];
                }
            }
            i++;
        }

        return null;
    }

    public static String getHttpStatusString(int httpStatusCode) {
        // Some of the status codes we use are still in draft state in the standards, and are not
        // recognized in the javax library. We need to manually translate these to human-readable
        // strings.
        String statusString = "Unknown";
        HttpStatus status = null;

        try {
            status = HttpStatus.valueOf(httpStatusCode);
        } catch (IllegalArgumentException e) {
        }


        if (status == null) {
            switch (httpStatusCode) {
                case 207:
                    statusString = "Multi Status";
                    break;
                default:
            }
        } else {
            statusString = status.getReasonPhrase();
        }

        return statusString;
    }
}
