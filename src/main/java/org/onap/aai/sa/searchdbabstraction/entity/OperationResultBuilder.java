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

package org.onap.aai.sa.searchdbabstraction.entity;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class OperationResultBuilder {

    public enum Type {
        DOCUMENT,
        SEARCH
    }

    private static final String INTERNAL_SERVER_ERROR_ELASTIC_SEARCH_OPERATION_FAULT =
            "Internal Error: ElasticSearch operation fault occurred";

    private OperationResult opResult;

    public OperationResultBuilder() {
        opResult = new OperationResult();
    }

    public OperationResultBuilder(Type type) {
        switch (type) {
            case DOCUMENT:
                opResult = new DocumentOperationResult();
                break;
            case SEARCH:
                opResult = new SearchOperationResult();
                break;
            default:
                opResult = new OperationResult();
        }
    }

    public OperationResult build() {
        return opResult;
    }

    public OperationResultBuilder useDefaults() {
        opResult.setResultCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        opResult.setResult(INTERNAL_SERVER_ERROR_ELASTIC_SEARCH_OPERATION_FAULT);
        return this;
    }

    public OperationResultBuilder resultCode(int resultCode) {
        opResult.setResultCode(resultCode);
        return this;
    }

    public OperationResultBuilder status(Status status) {
        return resultCode(status.getStatusCode());
    }

    public OperationResultBuilder failureCause(String failureCause) {
        opResult.setFailureCause(failureCause);
        return this;
    }

    public OperationResultBuilder result(String resultMsg) {
        opResult.setResult(resultMsg);
        return this;
    }

    public OperationResultBuilder resultVersion(String resultVersion) {
        opResult.setResultVersion(resultVersion);
        return this;
    }

}
