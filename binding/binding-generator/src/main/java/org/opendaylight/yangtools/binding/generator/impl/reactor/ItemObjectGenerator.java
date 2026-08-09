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
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultListRuntimeType;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.ItemObjectArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ListRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

/**
 * A {@link ListGenerator} producing {@link ItemObjectArchetype}.
 */
final class ItemObjectGenerator extends ListGenerator {
    @NonNullByDefault
    ItemObjectGenerator(final ListEffectiveStatement statement, final DataContainerGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    ItemObjectArchetype methodReturnType() {
        return (ItemObjectArchetype) getGeneratedType();
    }

    @Override
    ItemObjectArchetype createTypeImpl(final TypeName typeName, final ListEffectiveStatement statement,
            final List<@NonNull GroupingArchetype> groupings, final List<@NonNull TypeObjectArchetype<?>> typeObjects,
            final List<@NonNull GetterMethod> getters) {
        return ItemObjectArchetype.of(typeName, statement, parentNameForChildOf(), groupings, typeObjects, getters);
    }

    @Override
    CompositeRuntimeTypeBuilder<ListEffectiveStatement, ListRuntimeType> createBuilder(
            final ListEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            ListRuntimeType build(final Archetype type, final ListEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                return new DefaultListRuntimeType((ItemObjectArchetype) type, statement, children, augments);
            }
        };
    }
}
