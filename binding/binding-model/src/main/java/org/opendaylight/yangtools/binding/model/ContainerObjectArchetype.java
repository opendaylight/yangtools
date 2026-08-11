/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ContainerObject;
import org.opendaylight.yangtools.binding.model.impl.ContainerObjectArchetype000;
import org.opendaylight.yangtools.binding.model.impl.ContainerObjectArchetype0N0;
import org.opendaylight.yangtools.binding.model.impl.ContainerObjectArchetypeN00;
import org.opendaylight.yangtools.binding.model.impl.ContainerObjectArchetypeNN0;
import org.opendaylight.yangtools.binding.model.impl.TypeMethods;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for a {@link ContainerObject}.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public sealed interface ContainerObjectArchetype extends ChildOfArchetype, ReturnType
        permits ContainerObjectArchetype000, ContainerObjectArchetype0N0, ContainerObjectArchetypeN00,
                ContainerObjectArchetypeNN0 {
    @Override
    @SuppressWarnings("rawtypes")
    default Class<ContainerObject> contract() {
        return ContainerObject.class;
    }

    @Override
    ContainerEffectiveStatement statement();

    static ContainerObjectArchetype of(final TypeName typeName, final ContainerEffectiveStatement statement,
            final TypeName parentName, final List<GroupingArchetype> groupings,
            final List<TypeObjectArchetype<?>> typeObjects, final List<GetterMethod> getters) {
        final var gtrs = TypeMethods.copyList(getters);
        return switch (groupings.size()) {
            case 0 -> of0xx(typeName, statement, parentName, gtrs, typeObjects);
            default -> ofNxx(typeName, statement, parentName, gtrs, TypeMethods.copyList(groupings), typeObjects);
        };
    }

    private static ContainerObjectArchetype of0xx(final TypeName typeName, final ContainerEffectiveStatement statement,
            final TypeName parentName, final List<GetterMethod> getters,
            final List<TypeObjectArchetype<?>> typeObjects) {
        return switch (typeObjects.size()) {
            case 0 -> new ContainerObjectArchetype000(typeName, statement, parentName, getters);
            default -> new ContainerObjectArchetype0N0(typeName, statement, parentName, getters,
                TypeMethods.copyList(typeObjects));
        };
    }

    private static ContainerObjectArchetype ofNxx(final TypeName typeName, final ContainerEffectiveStatement statement,
            final TypeName parentName, final List<GetterMethod> getters, final List<Partial> partials,
            final List<TypeObjectArchetype<?>> typeObjects) {
        return switch (typeObjects.size()) {
            case 0 -> new ContainerObjectArchetypeN00(typeName, statement, parentName, getters, partials);
            default -> new ContainerObjectArchetypeNN0(typeName, statement, parentName, getters, partials,
                TypeMethods.copyList(typeObjects));
        };
    }
}
