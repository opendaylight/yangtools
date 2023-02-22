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
import java.util.List;
import java.util.Map;
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
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.yangtools.util.LazyCollections;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
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

    public abstract void setYangSourceDefinition(YangSourceDefinition yangSourceDefinition);

    abstract AbstractPair createEnumPair(String name, String mappedName, int value, Status status, String description,
            String reference);

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).add("values", values);
    }

    @Override
    public final void updateEnumPairsFromEnumTypeDef(final EnumTypeDefinition enumTypeDef) {
        final List<EnumPair> enums = enumTypeDef.getValues();
        final Map<String, String> valueIds = Naming.mapEnumAssignedNames(enums.stream().map(EnumPair::getName)
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
            return name;
        }

        @Override
        public final String getMappedName() {
            return mappedName;
        }

        @Override
        public final int getValue() {
            return value;
        }

        @Override
        public final int hashCode() {
            return name.hashCode() * 31 + value;
        }

        @Override
        public final boolean equals(final Object obj) {
            return obj == this || obj instanceof AbstractPair other && name.equals(other.name) && value == other.value;
        }

        @Override
        public final String toString() {
            return new StringBuilder().append("EnumPair [name=").append(name)
                .append(", mappedName=").append(getMappedName())
                .append(", value=").append(value)
                .append("]").toString();
        }
    }

    abstract static class AbstractEnumeration extends AbstractType implements Enumeration {
        private final List<AnnotationType> annotations;
        private final List<Pair> values;

        AbstractEnumeration(final AbstractEnumerationBuilder builder) {
            super(builder.getIdentifier());
            values = ImmutableList.copyOf(builder.values);
            annotations = builder.annotationBuilders.stream()
                .map(AnnotationTypeBuilder::build)
                .collect(ImmutableList.toImmutableList());
        }

        @Override
        public final List<Pair> getValues() {
            return values;
        }

        @Override
        public final List<AnnotationType> getAnnotations() {
            return annotations;
        }

        @Override
        public final String toFormattedString() {
            final var sb = new StringBuilder().append("public enum ").append(getName()).append(" {\n");

            int offset = 0;
            for (var valPair : values) {
                sb.append("\t ").append(valPair.getMappedName()).append(" (").append(valPair.getValue()).append(" )")
                    .append(offset == values.size() - 1 ? ';' : ',');
                ++offset;
            }
            return sb.append("\n}").toString();
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
            return List.of();
        }

        @Override
        public final List<GeneratedType> getEnclosedTypes() {
            return List.of();
        }

        @Override
        public final List<Enumeration> getEnumerations() {
            return List.of();
        }

        @Override
        public final List<Constant> getConstantDefinitions() {
            return List.of();
        }

        @Override
        public final List<MethodSignature> getMethodDefinitions() {
            return List.of();
        }

        @Override
        public final List<GeneratedProperty> getProperties() {
            return List.of();
        }
    }
}
