/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultMapRuntimeType;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
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
            final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
        keyGenerator = new KeyGenerator(key, parent, this);
    }

    @NonNull KeyGenerator keyGenerator() {
        return keyGenerator;
    }

    @Override
    Type methodReturnType() {
        final var generatedType = super.methodReturnType();
        return switch (statement().effectiveOrdering()) {
            case SYSTEM -> Types.mapTypeFor(keyGenerator.getGeneratedType(), generatedType);
            case USER -> Types.listTypeFor(generatedType);
        };
    }

    @Override
    EntryObjectArchetype createTypeImpl() {
        final var builder = EntryObjectArchetype.builder(typeName(), statement());
        addImplementsChildOf(builder);
        addUsesInterfaces(builder);
        addConcreteInterfaceMethods(builder);

        addQNameConstant(builder, localName());

        builder.addImplementsType(BindingTypes.entryObject(builder.typeRef(), keyGenerator.getArchetype()));

        addGetterMethods(builder);

        return builder.build();
    }

    @Override
    CompositeRuntimeTypeBuilder<ListEffectiveStatement, ListRuntimeType> createBuilder(
            final ListEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            ListRuntimeType build(final Archetype type, final ListEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                return new DefaultMapRuntimeType((EntryObjectArchetype) type, statement, children, augments,
                    // FIXME: the key here is not rebased correctly :(
                    keyGenerator.getRuntimeType());
            }
        };
    }
}
