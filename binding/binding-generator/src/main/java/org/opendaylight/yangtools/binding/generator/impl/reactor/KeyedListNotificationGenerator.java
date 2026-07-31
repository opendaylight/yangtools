/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.KeyedListNotification;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.KeyedListNotificationArchetype;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * A {@link NotificationGenerator} generating {@link KeyedListNotification}s.
 */
final class KeyedListNotificationGenerator extends AbstractInstanceNotificationGenerator {
    @NonNullByDefault
    KeyedListNotificationGenerator(final NotificationEffectiveStatement statement, final EntryObjectGenerator parent) {
        super(statement, parent);
    }

    @Override
    KeyedListNotificationArchetype createTypeImpl(final JavaTypeName typeName,
            final NotificationEffectiveStatement statement, final JavaTypeName parentName) {
        final var builder = newBuilder(typeName, statement, parentName);
        addUsesInterfaces(builder);
        addGetterMethods(builder);
        return builder.build();
    }

    @Override
    KeyedListNotificationArchetype createTypeImpl(final JavaTypeName typeName,
            final NotificationEffectiveStatement statement, final JavaTypeName parentName, final Archetype original) {
        return newBuilder(typeName, statement, parentName).addImplementsType(original).build();
    }

    @NonNullByDefault
    private static KeyedListNotificationArchetype.Builder newBuilder(final JavaTypeName typeName,
            final NotificationEffectiveStatement statement, final JavaTypeName parentName) {
        final var builder = KeyedListNotificationArchetype.builder(typeName, statement, parentName);
        addAugmentable(builder);
        defaultImplementedInterace(builder);
        return builder;
    }
}
