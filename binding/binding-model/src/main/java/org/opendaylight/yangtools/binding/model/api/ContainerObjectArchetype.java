/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link ChildOf} specializations generated for {@code container} statements.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface ContainerObjectArchetype extends ChildOfArchetype permits ContainerObjectArchetypeImpl {
    @Override
    ContainerEffectiveStatement statement();

    static ContainerObjectArchetype of(final JavaTypeName typeName, final ContainerEffectiveStatement statement,
            final JavaTypeName parentName, final List<GroupingArchetype> groupings,
            final List<TypeObjectArchetype<?>> typeObjects, final List<MethodSignature> methods) {
        return new ContainerObjectArchetypeImpl(typeName, statement, parentName, TypeMethods.copyList(groupings),
            TypeMethods.copyList(typeObjects), TypeMethods.copyList(methods));
    }
}
