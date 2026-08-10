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
        permits GroupingArchetype00, GroupingArchetypeNN {
    @Override
    default Class<Grouping> contract() {
        return Grouping.class;
    }

    @Override
    GroupingEffectiveStatement statement();

    static GroupingArchetype of(final TypeName typeName, final GroupingEffectiveStatement statement,
            final List<GroupingArchetype> groupings, final List<TypeObjectArchetype<?>> typeObjects,
            final List<GetterMethod> getters) {
        if (groupings.isEmpty() && typeObjects.isEmpty()) {
            return new GroupingArchetype00(typeName, statement, TypeMethods.copyList(getters));
        }
        return new GroupingArchetypeNN(typeName, statement, TypeMethods.copyList(groupings),
            TypeMethods.copyList(typeObjects), TypeMethods.copyList(getters));
    }
}
