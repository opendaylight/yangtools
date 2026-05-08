/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.OpaqueObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DataSchemaCompat;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;

/**
 * An {@link Archetype} for a {@link OpaqueObject} specialization.
 *
 * @since 16.0.0
 */
public sealed interface OpaqueObjectArchetype<S extends DataTreeEffectiveStatement<?> & DataSchemaCompat<QName, ?>>
        extends Archetype.WithQName<S> {
    /**
     * An {@link OpaqueObjectArchetype} for an {@link AnydataEffectiveStatement}.
     *
     * @param name this type's {@link JavaTypeName}}
     * @param statement the {@link AnydataEffectiveStatement}
     * @since 16.0.0
     */
    @NonNullByDefault
    record Anydata(
            JavaTypeName name,
            AnydataEffectiveStatement statement) implements OpaqueObjectArchetype<AnydataEffectiveStatement> {
        public Anydata {
            requireNonNull(name);
            requireNonNull(statement);
        }
    }

    /**
     * An {@link OpaqueObjectArchetype} for an {@link AnyxmlEffectiveStatement}.
     *
     * @param name this type's {@link JavaTypeName}}
     * @param statement the {@link AnyxmlEffectiveStatement}
     * @since 16.0.0
     */
    @NonNullByDefault
    record Anyxml(
            JavaTypeName name,
            AnyxmlEffectiveStatement statement) implements OpaqueObjectArchetype<AnyxmlEffectiveStatement> {
        public Anyxml {
            requireNonNull(name);
            requireNonNull(statement);
        }
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<AnnotationType> getAnnotations() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<Constant> getConstantDefinitions() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<GeneratedType> getEnclosedTypes() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<EnumTypeObjectArchetype> getEnumerations() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<Type> getImplements() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<MethodSignature> getMethodDefinitions() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<GeneratedProperty> getProperties() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default boolean isAbstract() {
        return true;
    }
}
