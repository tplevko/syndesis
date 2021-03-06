/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.server.connector.generator.swagger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import io.swagger.models.ArrayModel;
import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.syndesis.common.model.Violation;

/**
 * This class contains Syndesis custom validation rules for swagger definitions.
 */
public final class SyndesisSwaggerValidationRules implements Function<SwaggerModelInfo, SwaggerModelInfo> {

    private static final SyndesisSwaggerValidationRules INSTANCE = new SyndesisSwaggerValidationRules();

    private static final Set<String> SUPPORTED_AUTH_TYPES = new HashSet<>(Arrays.asList("basic", "oauth2"));

    private final List<Function<SwaggerModelInfo, SwaggerModelInfo>> rules = new ArrayList<>();

    private SyndesisSwaggerValidationRules() {
        rules.add(this::validateResponses);
        rules.add(this::validateAuthTypes);
    }

    @Override
    public SwaggerModelInfo apply(final SwaggerModelInfo swaggerModelInfo) {
        return rules.stream().reduce(Function::compose).map(f -> f.apply(swaggerModelInfo)).orElse(swaggerModelInfo);
    }

    /**
     * Check if all operations contains valid authentication types
     */
    private SwaggerModelInfo validateAuthTypes(final SwaggerModelInfo swaggerModelInfo) {

        if (swaggerModelInfo.getModel() == null) {
            return swaggerModelInfo;
        }

        final SwaggerModelInfo.Builder withWarnings = new SwaggerModelInfo.Builder().createFrom(swaggerModelInfo);

        for (final Map.Entry<String, SecuritySchemeDefinition> definitionEntry : notNull(
            swaggerModelInfo.getModel().getSecurityDefinitions()).entrySet()) {
            final String authType = definitionEntry.getValue().getType();
            if (!SUPPORTED_AUTH_TYPES.contains(authType)) {
                withWarnings.addWarning(new Violation.Builder()//
                    .property("")//
                    .error("unsupported-auth")//
                    .message("Authentication type " + authType + " is currently not supported")//
                    .build());
            }
        }

        return withWarnings.build();
    }

    /**
     * Check if a request/response JSON schema is present
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
    private SwaggerModelInfo validateResponses(final SwaggerModelInfo swaggerModelInfo) {
        if (swaggerModelInfo.getModel() == null) {
            return swaggerModelInfo;
        }

        final SwaggerModelInfo.Builder withWarnings = new SwaggerModelInfo.Builder().createFrom(swaggerModelInfo);

        for (final Map.Entry<String, Path> pathEntry : notNull(swaggerModelInfo.getModel().getPaths()).entrySet()) {
            for (final Map.Entry<HttpMethod, Operation> operationEntry : notNull(pathEntry.getValue().getOperationMap()).entrySet()) {

                // Check requests
                for (final Parameter parameter : notNull(operationEntry.getValue().getParameters())) {
                    if (!(parameter instanceof BodyParameter)) {
                        continue;
                    }
                    final BodyParameter bodyParameter = (BodyParameter) parameter;
                    final Model schema = bodyParameter.getSchema();
                    if (schemaIsNotSpecified(schema)) {
                        final String message = "Operation " + operationEntry.getKey() + " " + pathEntry.getKey()
                            + " does not provide a schema for the body parameter";

                        withWarnings.addWarning(new Violation.Builder()//
                            .property("")//
                            .error("missing-parameter-schema")//
                            .message(message)//
                            .build());
                    }
                }

                // Check responses
                for (final Map.Entry<String, Response> responseEntry : notNull(operationEntry.getValue().getResponses()).entrySet()) {
                    if (responseEntry.getKey().charAt(0) != '2') {
                        continue; // check only correct responses
                    }

                    if (responseEntry.getValue().getSchema() == null) {
                        final String message = "Operation " + operationEntry.getKey() + " " + pathEntry.getKey()
                            + " does not provide a response schema for code " + responseEntry.getKey();

                        withWarnings.addWarning(new Violation.Builder()//
                            .property("")//
                            .error("missing-response-schema")//
                            .message(message)//
                            .build());
                    }
                }
                // Assume that operations without 2xx responses do not provide a
                // response

            }
        }

        return withWarnings.build();
    }

    public static SyndesisSwaggerValidationRules getInstance() {
        return INSTANCE;
    }

    private static <T> List<T> notNull(final List<T> value) {
        return value != null ? value : Collections.emptyList();
    }

    private static <K, V> Map<K, V> notNull(final Map<K, V> value) {
        return value != null ? value : Collections.emptyMap();
    }

    private static boolean schemaIsNotSpecified(final Model schema) {
        if (schema == null) {
            return true;
        }

        if (schema instanceof ArrayModel) {
            return ((ArrayModel) schema).getItems() == null;
        }

        final Map<String, Property> properties = schema.getProperties();

        final boolean noProperties = properties == null || properties.isEmpty();

        final boolean noReference = schema.getReference() == null;

        return noProperties && noReference;
    }

}
