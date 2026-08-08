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
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link EntryObject} specializations.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface EntryObjectArchetype extends ChildOfArchetype.OfList permits EntryObjectArchetypeImpl {
    /**
     * {@return the {link KeyArchetype} associated with this archetype}
     */
    JavaTypeName keyName();

    static EntryObjectArchetype of(final JavaTypeName typeName, final ListEffectiveStatement statement,
            final JavaTypeName parentName, final JavaTypeName keyName, final List<GroupingArchetype> groupings,
            final List<TypeObjectArchetype<?>> typeObjects, final List<GetterMethod> getters) {
        return new EntryObjectArchetypeImpl(typeName, statement, parentName, keyName, TypeMethods.copyList(groupings),
            TypeMethods.copyList(typeObjects), TypeMethods.copyList(getters));
    }
}
