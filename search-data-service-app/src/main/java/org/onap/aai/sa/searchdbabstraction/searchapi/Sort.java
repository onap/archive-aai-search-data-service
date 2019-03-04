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

public class Sort {

    private String field;
    private SortDirection order = null;

    public enum SortDirection {
        ascending,
        descending
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public SortDirection getOrder() {
        return order;
    }

    public void setOrder(SortDirection order) {
        this.order = order;
    }

    public String toElasticSearch() {

        StringBuilder sb = new StringBuilder();

        sb.append("{ \"").append(field).append("\": { \"order\": ");

        // If a sort order wasn't explicitly supplied, default to 'ascending'.
        if (order != null) {
            switch (order) {
                case ascending:
                    sb.append("\"asc\"}}");
                    break;
                case descending:
                    sb.append("\"desc\"}}");
                    break;
                default:
            }
        } else {
            sb.append("\"asc\"}}");
        }

        return sb.toString();
    }
}
