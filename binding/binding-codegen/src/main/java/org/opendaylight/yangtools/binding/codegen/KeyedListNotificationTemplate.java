/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.KeyedListNotification;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.KeyArchetype;
import org.opendaylight.yangtools.binding.model.KeyedListNotificationArchetype;
import org.opendaylight.yangtools.binding.model.Type;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.TypeRef;

/**
 * Template for a {@link KeyedListNotificationArchetype}.
 */
@NonNullByDefault
final class KeyedListNotificationTemplate extends InterfaceTemplate<KeyedListNotificationArchetype>
        implements ArchetypeTemplate.WithBuilder {
    private static final ConcreteType KEYED_LIST_NOTIFICATION = ConcreteType.ofClass(KeyedListNotification.class);

    private final TypeName keyName;

    KeyedListNotificationTemplate(final DataRootArchetype root, final KeyedListNotificationArchetype archetype,
            final KeyArchetype key) {
        super(root, archetype);
        keyName = key.name();
    }

    @Override
    public BuilderTemplate newBuilderTemplate() {
        return BuilderTemplate.of(this);
    }

    @Override
    Iterator<? extends Type> extendsTypes() {
        return Iterators.concat(
            Iterators.singletonIterator(
                ParameterizedType.of(KEYED_LIST_NOTIFICATION, archetype, TypeRef.of(archetype.parentName()),
                    TypeRef.of(keyName))),
            super.extendsTypes());
    }

    @Override
    QNameConstant constants() {
        return new QNameConstant.InInterface(this, archetype.statement().argument());
    }

    @Override
    BlockBuilder contractMethods(final BlockBuilder bb) {
        return bb
            .nl()
            .frg(new ImplementedInterfaceMethod.Canonical(this))
            .nl()
            .frg(new JavaDataContainerMethods(javaType(), getters, true));
    }
}
