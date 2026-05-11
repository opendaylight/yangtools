/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.List;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.EnumTypeObject;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

/**
 * The {@link Archetype} for {@link EnumTypeObject} specializations.
 * @since 15.0.0
 */
@NonNullByDefault
public record EnumTypeObjectArchetype(
        JavaTypeName name,
        TypeEffectiveStatement.MandatoryIn<?, ?> statement,
        EnumTypeDefinition typeDefinition)
        implements TypeObjectArchetype<EnumTypeObject>, Archetype.Compat<EffectiveStatement<?,?>> {

    /**
     * {@return the injective mapping from YANG {@code enum} assigned name to its assigned Java {@code enum} constant,
     * with iteration order matching {@code typeDefinition().getValues()}}
     */
    public ImmutableBiMap<EnumPair, String> valueToConstant() {
        return mapNames(typeDefinition.getValues());
    }

    /**
     * Returns Java identifiers, conforming to JLS9 Section 3.8 to use for specified YANG assigned names
     * (RFC7950 Section 9.6.4). This method considers two distinct encodings: one the pre-Fluorine mapping, which is
     * okay and convenient for sane strings, and an escaping-based bijective mapping which works for all possible
     * Unicode strings.
     *
     * @param assignedNames Collection of assigned names
     * @return A BiMap keyed by assigned name, with Java identifiers as values
     * @throws IllegalArgumentException if any of the names is empty
     */
    private static ImmutableBiMap<EnumPair, String> mapNames(final List<EnumPair> values) {
        /*
         * Original mapping assumed strings encountered are identifiers, hence it used getClassName to map the names
         * and that function is not an injection -- this is evidenced in MDSAL-208 and results in a failure to compile
         * generated code. If we encounter such a conflict or if the result is not a valid identifier (like '*'), we
         * abort and switch the mapping schema to mapEnumAssignedName(), which is a bijection.
         *
         * Note that assignedNames can contain duplicates, which must not trigger a duplication fallback.
         */
        final var javaToYang = HashBiMap.<String, String>create(values.size());
        for (var pair : values) {
            final var name = pair.getName();
            checkArgument(!name.isEmpty());
            if (!javaToYang.containsValue(name)) {
                final var mappedName = Naming.getClassName(name);
                if (!Naming.isValidJavaIdentifier(mappedName) || javaToYang.forcePut(mappedName, name) != null) {
                    // Fall back to bijective mapping
                    return bijectiveMapNames(values);
                }
            }
        }

        final var yangToJava = javaToYang.inverse();
        return values.stream().collect(ImmutableBiMap.toImmutableBiMap(Function.identity(),
            value -> yangToJava.get(value.getName())));
    }

    private static ImmutableBiMap<EnumPair, String> bijectiveMapNames(final List<EnumPair> values) {
        return values.stream().collect(ImmutableBiMap.toImmutableBiMap(Function.identity(),
            value -> Naming.mapEnumAssignedName(value.getName())));
    }

    @Override
    public final int hashCode() {
        return name.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Type other && name.equals(other.name());
    }

    @Override
    public final String toString() {
        final var helper = MoreObjects.toStringHelper(this).add("name", name);
        final var values = typeDefinition.getValues();
        if (!values.isEmpty()) {
            helper.add("values", values);
        }
        return helper.toString();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<AnnotationType> getAnnotations() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<Type> getImplements() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<GeneratedType> getEnclosedTypes() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<EnumTypeObjectArchetype> getEnumerations() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<Constant> getConstantDefinitions() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<MethodSignature> getMethodDefinitions() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<GeneratedProperty> getProperties() {
        return List.of();
    }
}
