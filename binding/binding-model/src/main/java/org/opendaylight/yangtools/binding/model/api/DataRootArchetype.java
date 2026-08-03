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
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link DataRoot} specializations.
 *
 * @since 15.0.0
 */
@NonNullByDefault
public sealed interface DataRootArchetype extends DataContainerArchetype permits DataRootArchetypeImpl {
    @Override
    ModuleEffectiveStatement statement();

    static DataRootArchetype of(final JavaTypeName typeName, final ModuleEffectiveStatement statement,
            final List<GroupingArchetype> groupings, final List<TypeObjectArchetype<?>> typeObjects,
            final List<MethodSignature> methods) {
        return new DataRootArchetypeImpl(typeName, statement, TypeMethods.copyList(groupings),
            TypeMethods.copyList(typeObjects), TypeMethods.copyList(methods));
    }
}
