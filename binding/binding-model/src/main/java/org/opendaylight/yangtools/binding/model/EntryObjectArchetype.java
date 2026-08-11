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
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.model.impl.EntryObjectArchetype000;
import org.opendaylight.yangtools.binding.model.impl.EntryObjectArchetype0N0;
import org.opendaylight.yangtools.binding.model.impl.EntryObjectArchetypeN00;
import org.opendaylight.yangtools.binding.model.impl.EntryObjectArchetypeNN0;
import org.opendaylight.yangtools.binding.model.impl.TypeMethods;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link EntryObject} specializations.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public sealed interface EntryObjectArchetype extends ChildOfArchetype.OfList, ReturnType
        permits EntryObjectArchetype000, EntryObjectArchetype0N0, EntryObjectArchetypeN00, EntryObjectArchetypeNN0 {
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
        final var gtrs = TypeMethods.copyList(getters);
        return switch (groupings.size()) {
            case 0 -> of0xx(typeName, statement, parentName, keyName, gtrs, typeObjects);
            default -> ofNxx(typeName, statement, parentName, keyName, gtrs, TypeMethods.copyList(groupings),
                typeObjects);
        };
    }

    private static EntryObjectArchetype of0xx(final TypeName typeName, final ListEffectiveStatement statement,
            final TypeName parentName, final TypeName keyName, final List<GetterMethod> getters,
            final List<TypeObjectArchetype<?>> typeObjects) {
        return switch (typeObjects.size()) {
            case 0 -> new EntryObjectArchetype000(typeName, statement, parentName, keyName, getters);
            default -> new EntryObjectArchetype0N0(typeName, statement, parentName, keyName, getters,
                TypeMethods.copyList(typeObjects));
        };
    }

    private static EntryObjectArchetype ofNxx(final TypeName typeName, final ListEffectiveStatement statement,
            final TypeName parentName, final TypeName keyName, final List<GetterMethod> getters,
            final List<Partial> partials, final List<TypeObjectArchetype<?>> typeObjects) {
        return switch (typeObjects.size()) {
            case 0 -> new EntryObjectArchetypeN00(typeName, statement, parentName, keyName, getters, partials);
            default -> new EntryObjectArchetypeNN0(typeName, statement, parentName, keyName, getters, partials,
                TypeMethods.copyList(typeObjects));
        };
    }
}
