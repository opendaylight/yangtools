/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.AnnotationType.Parameter;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.yangtools.util.LazyCollections;

public final class AnnotationTypeBuilderImpl extends AbstractTypeBuilder implements AnnotationTypeBuilder {
    private List<Parameter> parameters = List.of();

    @NonNullByDefault
    public AnnotationTypeBuilderImpl(final JavaTypeName typeName) {
        super(typeName);
    }

    private boolean addParameter(final ParameterImpl param) {
        if (parameters.contains(param)) {
            return false;
        }
        parameters = LazyCollections.lazyAdd(parameters, param);
        return true;
    }

    @Override
    public boolean addParameter(final String paramName, final String value) {
        if (paramName != null && value != null) {
            return addParameter(new ParameterImpl(paramName, value));
        }
        return false;
    }

    @Override
    public boolean addParameters(final String paramName, final List<String> values) {
        if (paramName != null && values != null) {
            return addParameter(new ParameterImpl(paramName, values));
        }
        return false;
    }

    @Override
    public AnnotationType build() {
        return new AnnotationTypeImpl(typeName(), parameters);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        super.addToStringAttributes(helper);

        addToStringAttribute(helper, "parameters", parameters);

        return helper;
    }

    private static final class AnnotationTypeImpl implements AnnotationType {
        private final @NonNull JavaTypeName name;
        private final List<Parameter> parameters;

        AnnotationTypeImpl(final JavaTypeName name, final List<Parameter> parameters) {
            this.name = requireNonNull(name);
            this.parameters = ImmutableList.copyOf(parameters);
        }

        @Override
        public JavaTypeName name() {
            return name;
        }

        @Override
        public Parameter getParameter(final String paramName) {
            if (paramName != null) {
                for (var parameter : parameters) {
                    if (parameter.getName().equals(paramName)) {
                        return parameter;
                    }
                }
            }
            return null;
        }

        @Override
        public List<Parameter> getParameters() {
            return parameters;
        }

        @Override
        public List<String> getParameterNames() {
            return Lists.transform(parameters, Parameter::getName);
        }

        @Override
        public boolean containsParameters() {
            return !parameters.isEmpty();
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return this == obj || obj instanceof Type other && name.equals(other.name());
        }

        @Override
        public String toString() {
            final var helper = MoreObjects.toStringHelper(AnnotationType.class).add("name", name);

            addToStringAttribute(helper, "parameters", parameters);

            return helper.toString();
        }
    }

    private static final class ParameterImpl implements Parameter {
        private final String name;
        private final String value;
        private final List<String> values;

        ParameterImpl(final String name, final String value) {
            this.name = name;
            this.value = value;
            values = Collections.emptyList();
        }

        ParameterImpl(final String name, final List<String> values) {
            this.name = name;
            this.values = values;
            value = null;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public List<String> getValues() {
            return values;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(name);
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ParameterImpl other = (ParameterImpl) obj;
            return Objects.equals(name, other.name);
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("ParameterImpl [name=");
            builder.append(name);
            builder.append(", value=");
            builder.append(value);
            builder.append(", values=");
            builder.append(values);
            builder.append("]");
            return builder.toString();
        }
    }
}
