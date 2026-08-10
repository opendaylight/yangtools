/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Grouping;
import org.opendaylight.yangtools.binding.model.impl.GroupingArchetype00;
import org.opendaylight.yangtools.binding.model.impl.GroupingArchetype0N;
import org.opendaylight.yangtools.binding.model.impl.GroupingArchetypeN0;
import org.opendaylight.yangtools.binding.model.impl.GroupingArchetypeNN;
import org.opendaylight.yangtools.binding.model.impl.TypeMethods;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link Grouping} specializations.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface GroupingArchetype extends DataContainerArchetype.Partial
        permits GroupingArchetype00, GroupingArchetype0N, GroupingArchetypeN0, GroupingArchetypeNN {
    @Override
    default Class<Grouping> contract() {
        return Grouping.class;
    }

    @Override
    GroupingEffectiveStatement statement();

    static GroupingArchetype of(final TypeName typeName, final GroupingEffectiveStatement statement,
            final List<GroupingArchetype> groupings, final List<TypeObjectArchetype<?>> typeObjects,
            final List<GetterMethod> getters) {
        final var gtrs = TypeMethods.copyList(getters);
        return switch (groupings.size()) {
            case 0 -> of0x(typeName, statement, gtrs, typeObjects);
            default -> ofNx(typeName, statement, gtrs, TypeMethods.copyList(groupings), typeObjects);
        };
    }

    private static GroupingArchetype of0x(final TypeName typeName, final GroupingEffectiveStatement statement,
                final List<GetterMethod> getters, final List<TypeObjectArchetype<?>> typeObjects) {
        return switch (typeObjects.size()) {
            case 0 -> new GroupingArchetype00(typeName, statement, getters);
            default -> new GroupingArchetype0N(typeName, statement, getters, TypeMethods.copyList(typeObjects));
        };
    }

    private static GroupingArchetype ofNx(final TypeName typeName, final GroupingEffectiveStatement statement,
            final List<GetterMethod> getters, final List<Partial> partials,
            final List<TypeObjectArchetype<?>> typeObjects) {
        return switch (typeObjects.size()) {
            case 0 -> new GroupingArchetypeN0(typeName, statement, getters, partials);
            default -> new GroupingArchetypeNN(typeName, statement, getters, partials,
                TypeMethods.copyList(typeObjects));
        };
    }
}
