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
package org.onap.aai.sa.searchdbabstraction.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.io.IOUtils;
import org.onap.aai.sa.rest.DocumentFieldSchema;
import org.onap.aai.sa.rest.DocumentSchema;

public class DocumentSchemaUtil {

    private static String dynamicCustomMapping = null;
    private static final String DYNAMIC_CUSTOM_TEMPALTE_FILE =
            System.getProperty("CONFIG_HOME") + File.separator + "dynamic-custom-template.json";

    public static String generateDocumentMappings(String documentSchema)
            throws JsonParseException, JsonMappingException, IOException {

        // Unmarshal the json content into a document schema object.
        ObjectMapper mapper = new ObjectMapper();
        DocumentSchema schema = mapper.readValue(documentSchema, DocumentSchema.class);

        return generateDocumentMappings(schema);
    }

    public static String generateDocumentMappings(DocumentSchema schema) throws IOException {

        // Adding dynamic template to add fielddata=true to dynamic fields of type "string"
        // in order to avoid aggregation queries breaking in ESv6.1.2
        if (dynamicCustomMapping == null) {
            try {
                dynamicCustomMapping = IOUtils.toString(new FileInputStream(DYNAMIC_CUSTOM_TEMPALTE_FILE), "UTF-8")
                        .replaceAll("\\s+", "");
            } catch (IOException e) {
                throw new IOException(
                        "Dynamic Custom template configuration went wrong! Please check for the correct template file.",
                        e);
            }
        }

        // Now, generate the Elastic Search mapping json and return it.
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        // Adding custom mapping which adds fielddata=true to dynamic fields of type "string"
        sb.append(dynamicCustomMapping != null ? dynamicCustomMapping : "");
        sb.append("\"properties\": {");

        generateFieldMappings(schema.getFields(), sb);

        sb.append("}");
        sb.append("}");

        return sb.toString();
    }


    private static void generateFieldMappings(List<DocumentFieldSchema> fields, StringBuilder sb) {

        AtomicBoolean firstField = new AtomicBoolean(true);

        for (DocumentFieldSchema field : fields) {

            // If this isn't the first field in the list, prepend it with a ','
            if (!firstField.compareAndSet(true, false)) {
                sb.append(", ");
            }

            // Now, append the translated field contents.
            generateFieldMapping(field, sb);
        }
    }

    private static void generateFieldMapping(DocumentFieldSchema fieldSchema, StringBuilder sb) {

        sb.append("\"").append(fieldSchema.getName()).append("\": {");

        // The field type is mandatory.
        sb.append("\"type\": \"").append(fieldSchema.getDataType()).append("\"");

        // For date type fields we may optionally supply a format specifier.
        if (fieldSchema.getDataType().equals("date")) {
            if (fieldSchema.getFormat() != null) {
                sb.append(", \"format\": \"").append(fieldSchema.getFormat()).append("\"");
            }
        }

        // If the index field was specified, then append it.
        if (fieldSchema.getSearchable() != null) {
            sb.append(", \"index\": \"").append(fieldSchema.getSearchable() ? "analyzed" : "not_analyzed").append("\"");
        }

        // If a search analyzer was specified, then append it.
        if (fieldSchema.getSearchAnalyzer() != null) {
            sb.append(", \"search_analyzer\": \"").append(fieldSchema.getSearchAnalyzer()).append("\"");
        }

        // If an indexing analyzer was specified, then append it.
        if (fieldSchema.getIndexAnalyzer() != null) {
            sb.append(", \"analyzer\": \"").append(fieldSchema.getIndexAnalyzer()).append("\"");
        }


        if (fieldSchema.getDataType().equals("nested")) {

            sb.append(", \"properties\": {");
            generateFieldMappings(fieldSchema.getSubFields(), sb);
            sb.append("}");
        }

        sb.append("}");
    }

}

