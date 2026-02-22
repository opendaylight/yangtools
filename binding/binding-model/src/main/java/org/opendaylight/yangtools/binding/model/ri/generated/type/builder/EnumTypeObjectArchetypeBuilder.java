/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.AbstractType;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.yangtools.util.LazyCollections;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

/**
 * Base class for {@link EnumTypeObjectArchetype.Builder} implementations.
 */
public abstract sealed class EnumTypeObjectArchetypeBuilder extends AbstractTypeBuilder
        implements EnumTypeObjectArchetype.Builder
        permits CodegenEnumerationBuilder, RuntimeEnumerationBuilder {
    private List<EnumTypeObjectArchetype.Pair> values = ImmutableList.of();
    private List<AnnotationTypeBuilder> annotationBuilders = ImmutableList.of();

    @NonNullByDefault
    EnumTypeObjectArchetypeBuilder(final JavaTypeName tyoeName) {
        super(tyoeName);
    }

    @Override
    public final AnnotationTypeBuilder addAnnotation(final JavaTypeName identifier) {
        final var builder = new AnnotationTypeBuilderImpl(identifier);
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

    abstract AbstractPair createEnumPair(String name, String mappedName, int value, Status status, String description,
            String reference);

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        super.addToStringAttributes(helper);
        addToStringAttribute(helper, "values", values);
        return helper;
    }

    @Override
    public final void updateEnumPairsFromEnumTypeDef(final EnumTypeDefinition enumTypeDef) {
        final var enums = enumTypeDef.getValues();
        final var valueIds = mapEnumAssignedNames(enums.stream().map(EnumPair::getName).collect(Collectors.toList()));

        for (var enumPair : enums) {
            addValue(enumPair.getName(), valueIds.get(enumPair.getName()), enumPair.getValue(), enumPair.getStatus(),
                enumPair.getDescription().orElse(null), enumPair.getReference().orElse(null));
        }
    }

    @Override
    public final EnumTypeObjectArchetype build() {
        return build(ImmutableList.copyOf(values),
            annotationBuilders.stream().map(AnnotationTypeBuilder::build).collect(ImmutableList.toImmutableList()));
    }

    @NonNullByDefault
    abstract EnumTypeObjectArchetype build(List<EnumTypeObjectArchetype.Pair> values, List<AnnotationType> annotations);

    /**
     * Returns Java identifiers, conforming to JLS9 Section 3.8 to use for specified YANG assigned names
     * (RFC7950 Section 9.6.4). This method considers two distinct encodings: one the pre-Fluorine mapping, which is
     * okay and convenient for sane strings, and an escaping-based bijective mapping which works for all possible
     * Unicode strings.
     *
     * @param assignedNames Collection of assigned names
     * @return A BiMap keyed by assigned name, with Java identifiers as values
     * @throws NullPointerException if assignedNames is null or contains null items
     * @throws IllegalArgumentException if any of the names is empty
     */
    private static BiMap<String, String> mapEnumAssignedNames(final Collection<String> assignedNames) {
        /*
         * Original mapping assumed strings encountered are identifiers, hence it used getClassName to map the names
         * and that function is not an injection -- this is evidenced in MDSAL-208 and results in a failure to compile
         * generated code. If we encounter such a conflict or if the result is not a valid identifier (like '*'), we
         * abort and switch the mapping schema to mapEnumAssignedName(), which is a bijection.
         *
         * Note that assignedNames can contain duplicates, which must not trigger a duplication fallback.
         */
        final var javaToYang = HashBiMap.<String, String>create(assignedNames.size());
        boolean valid = true;
        for (final String name : assignedNames) {
            checkArgument(!name.isEmpty());
            if (!javaToYang.containsValue(name)) {
                final String mappedName = Naming.getClassName(name);
                if (!Naming.isValidJavaIdentifier(mappedName) || javaToYang.forcePut(mappedName, name) != null) {
                    valid = false;
                    break;
                }
            }
        }
        if (!valid) {
            // Fall back to bijective mapping
            javaToYang.clear();
            for (final String name : assignedNames) {
                javaToYang.put(Naming.mapEnumAssignedName(name), name);
            }
        }
        return javaToYang.inverse();
    }

    abstract static class AbstractEnumeration extends AbstractType implements EnumTypeObjectArchetype {
        private final @NonNull List<AnnotationType> annotations;
        private final @NonNull List<Pair> values;

        @NonNullByDefault
        AbstractEnumeration(final JavaTypeName name, final List<Pair> values, final List<AnnotationType> annotations) {
            super(name);
            this.values = requireNonNull(values);
            this.annotations = requireNonNull(annotations);
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
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            super.addToStringAttributes(helper);
            addToStringAttribute(helper, "values", values);
            return helper;
        }
    }
}
