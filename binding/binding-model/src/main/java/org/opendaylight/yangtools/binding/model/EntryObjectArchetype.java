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
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.model.impl.EntryObjectArchetypeImpl;
import org.opendaylight.yangtools.binding.model.impl.TypeMethods;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link EntryObject} specializations.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface EntryObjectArchetype extends ChildOfArchetype.OfList, ReturnType
        permits EntryObjectArchetypeImpl {
    @Override
    @SuppressWarnings("rawtypes")
    default Class<EntryObject> contract() {
        return EntryObject.class;
    }

    /**
     * {@return the {link KeyArchetype} associated with this archetype}
     */
    TypeName keyName();

    static EntryObjectArchetype of(final TypeName typeName, final ListEffectiveStatement statement,
            final TypeName parentName, final TypeName keyName, final List<GroupingArchetype> groupings,
            final List<TypeObjectArchetype<?>> typeObjects, final List<GetterMethod> getters) {
        return new EntryObjectArchetypeImpl(typeName, statement, parentName, keyName, TypeMethods.copyList(groupings),
            TypeMethods.copyList(typeObjects), TypeMethods.copyList(getters));
    }
}
