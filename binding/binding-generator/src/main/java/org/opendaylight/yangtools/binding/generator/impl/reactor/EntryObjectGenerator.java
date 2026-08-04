/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verifyNotNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultMapRuntimeType;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ListRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

/**
 * A {@link ListGenerator} producing {@link EntryObjectArchetype}.
 */
final class EntryObjectGenerator extends ListGenerator {
    private final @NonNull KeyGenerator keyGenerator;

    @NonNullByDefault
    EntryObjectGenerator(final ListEffectiveStatement statement, final KeyEffectiveStatement key,
            final DataContainerGenerator<?, ?> parent) {
        super(statement, parent);
        keyGenerator = new KeyGenerator(key, parent, this);
    }

    @NonNull KeyGenerator keyGenerator() {
        // guard against invocations during construction
        return verifyNotNull(keyGenerator);
    }

    @Override
    ParameterizedType methodReturnType() {
        final var archetype = getGeneratedType();
        return switch (statement().effectiveOrdering()) {
            case SYSTEM -> Types.mapTypeFor(keyGenerator.getArchetype(), archetype);
            case USER -> Types.listTypeFor(archetype);
        };
    }

    @Override
    EntryObjectArchetype getGeneratedType() {
        return (EntryObjectArchetype) super.getGeneratedType();
    }

    @Override
    EntryObjectArchetype createTypeImpl(final JavaTypeName typeName, final ListEffectiveStatement statement,
            final List<@NonNull GroupingArchetype> groupings, final List<@NonNull TypeObjectArchetype<?>> typeObjects,
            final List<@NonNull MethodSignature> methods) {
        return EntryObjectArchetype.of(typeName, statement, parentNameForChildOf(), keyGenerator.typeName(), groupings,
            typeObjects, methods);
    }

    @Override
    CompositeRuntimeTypeBuilder<ListEffectiveStatement, ListRuntimeType> createBuilder(
            final ListEffectiveStatement statement) {
        final var keyType = keyGenerator.getArchetype();

        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            ListRuntimeType build(final Archetype type, final ListEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                return new DefaultMapRuntimeType((EntryObjectArchetype) type, statement, children, augments, keyType);
            }
        };
    }
}
