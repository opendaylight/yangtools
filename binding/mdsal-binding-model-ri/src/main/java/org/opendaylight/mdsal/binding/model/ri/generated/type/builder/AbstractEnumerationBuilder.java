/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.model.api.AbstractType;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.Constant;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.util.LazyCollections;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

// FIXME: public because EnumBuilder does not have setters we are exposing
public abstract class AbstractEnumerationBuilder extends AbstractType implements EnumBuilder {
    private List<Enumeration.Pair> values = ImmutableList.of();
    private List<AnnotationTypeBuilder> annotationBuilders = ImmutableList.of();

    AbstractEnumerationBuilder(final JavaTypeName identifier) {
        super(identifier);
    }

    @Override
    public final AnnotationTypeBuilder addAnnotation(final JavaTypeName identifier) {
        final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(identifier);
        if (!annotationBuilders.contains(builder)) {
            annotationBuilders = LazyCollections.lazyAdd(annotationBuilders, builder);
            return builder;
        }
        return null;
    }

    @VisibleForTesting
    final void addValue(final String name, final String mappedName, final int value, final Status status,
            final String description, final String reference) {
        values = LazyCollections.lazyAdd(values,
            createEnumPair(name, mappedName, value, status, description, reference));
    }

    public abstract void setReference(String reference);

    public abstract void setModuleName(String moduleName);

    public abstract void setSchemaPath(SchemaPath schemaPath);

    abstract AbstractPair createEnumPair(String name, String mappedName, int value, Status status, String description,
            String reference);

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("values", values);
    }

    @Override
    public final void updateEnumPairsFromEnumTypeDef(final EnumTypeDefinition enumTypeDef) {
        final List<EnumPair> enums = enumTypeDef.getValues();
        final Map<String, String> valueIds = BindingMapping.mapEnumAssignedNames(enums.stream().map(EnumPair::getName)
            .collect(Collectors.toList()));

        for (EnumPair enumPair : enums) {
            addValue(enumPair.getName(), valueIds.get(enumPair.getName()), enumPair.getValue(), enumPair.getStatus(),
                enumPair.getDescription().orElse(null), enumPair.getReference().orElse(null));
        }
    }

    abstract static class AbstractPair implements Enumeration.Pair {
        private final String name;
        private final String mappedName;
        private final int value;

        AbstractPair(final String name, final String mappedName, final int value) {
            this.name = requireNonNull(name);
            this.mappedName = requireNonNull(mappedName);
            this.value = value;
        }

        @Override
        public final String getName() {
            return this.name;
        }

        @Override
        public final String getMappedName() {
            return this.mappedName;
        }

        @Override
        public final int getValue() {
            return this.value;
        }

        @Override
        public final int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(this.name);
            result = prime * result + Objects.hashCode(this.value);
            return result;
        }

        @Override
        public final boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AbstractPair)) {
                return false;
            }
            final AbstractPair other = (AbstractPair) obj;
            return Objects.equals(this.name, other.name) && Objects.equals(this.value, other.value);
        }

        @Override
        public final String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("EnumPair [name=");
            builder.append(this.name);
            builder.append(", mappedName=");
            builder.append(getMappedName());
            builder.append(", value=");
            builder.append(this.value);
            builder.append("]");
            return builder.toString();
        }
    }

    abstract static class AbstractEnumeration extends AbstractType implements Enumeration {
        private final List<AnnotationType> annotations;
        private final List<Pair> values;

        AbstractEnumeration(final AbstractEnumerationBuilder builder) {
            super(builder.getIdentifier());
            this.values = ImmutableList.copyOf(builder.values);

            final ArrayList<AnnotationType> a = new ArrayList<>();
            for (final AnnotationTypeBuilder b : builder.annotationBuilders) {
                a.add(b.build());
            }
            this.annotations = ImmutableList.copyOf(a);
        }

        @Override
        public final List<Pair> getValues() {
            return this.values;
        }

        @Override
        public final List<AnnotationType> getAnnotations() {
            return this.annotations;
        }

        @Override
        public final String toFormattedString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("public enum");
            builder.append(" ");
            builder.append(getName());
            builder.append(" {");
            builder.append("\n");

            int offset = 0;
            for (final Enumeration.Pair valPair : this.values) {
                builder.append("\t ");
                builder.append(valPair.getMappedName());
                builder.append(" (");
                builder.append(valPair.getValue());

                if (offset == this.values.size() - 1) {
                    builder.append(" );");
                } else {
                    builder.append(" ),");
                }
                ++offset;
            }
            builder.append("\n}");
            return builder.toString();
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
            return super.addToStringAttributes(toStringHelper).add("values", values);
        }

        @Override
        public final boolean isAbstract() {
            return false;
        }

        @Override
        public final List<Type> getImplements() {
            return Collections.emptyList();
        }

        @Override
        public final List<GeneratedType> getEnclosedTypes() {
            return Collections.emptyList();
        }

        @Override
        public final List<Enumeration> getEnumerations() {
            return Collections.emptyList();
        }

        @Override
        public final List<Constant> getConstantDefinitions() {
            return Collections.emptyList();
        }

        @Override
        public final List<MethodSignature> getMethodDefinitions() {
            return Collections.emptyList();
        }

        @Override
        public final List<GeneratedProperty> getProperties() {
            return Collections.emptyList();
        }
    }
}
