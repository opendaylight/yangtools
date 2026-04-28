/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.KeyArchetypeBuilder;

/**
 * An archetype for a {@link Key} attached to an {@link EntryObject}.
 */
@NonNullByDefault
public non-sealed interface KeyArchetype extends Archetype {
    /**
     * A builder of {@link KeyArchetype} instances.
     */
    sealed interface Builder extends GeneratedTypeBuilderBase<Builder> permits KeyArchetypeBuilder {

        Builder setSerialVersionUID(long serialVersionUID);

        @Override
        KeyArchetype build();
    }

    /**
     * {@return the {@link JavaTypeName} of the associated {@link EntryObject} type}
     */
    JavaTypeName entryObject();

    /**
     * {@return the {@code serialVersionUID} field value}
     */
    long serialVersionUID();

    @Override
    default @Nullable TypeComment getComment() {
        return null;
    }

    @Override
    default List<AnnotationType> getAnnotations() {
        return List.of();
    }

    @Override
    default boolean isAbstract() {
        return false;
    }

    @Override
    default List<Type> getImplements() {
        return List.of();
    }

    @Override
    default List<GeneratedType> getEnclosedTypes() {
        return List.of();
    }

    @Override
    default List<EnumTypeObjectArchetype> getEnumerations() {
        return List.of();
    }

    @Override
    default List<Constant> getConstantDefinitions() {
        return List.of();
    }

    @Override
    default List<MethodSignature> getMethodDefinitions() {
        return List.of();
    }

    @Override
    default Optional<YangSourceDefinition> getYangSourceDefinition() {
        return Optional.empty();
    }
}
