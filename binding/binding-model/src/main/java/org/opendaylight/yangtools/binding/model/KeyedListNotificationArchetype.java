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
import org.opendaylight.yangtools.binding.KeyedListNotification;
import org.opendaylight.yangtools.binding.model.impl.KeyedListNotificationArchetypeFromGrouping;
import org.opendaylight.yangtools.binding.model.impl.KeyedListNotificationArchetypeImpl;
import org.opendaylight.yangtools.binding.model.impl.TypeMethods;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link KeyedListNotification} specializations.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface KeyedListNotificationArchetype extends DataContainerArchetype.OfNotification
        permits KeyedListNotificationArchetypeFromGrouping, KeyedListNotificationArchetypeImpl {
    @Override
    @SuppressWarnings("rawtypes")
    default Class<KeyedListNotification> contract() {
        return KeyedListNotification.class;
    }

    /**
     * {@return the {@link TypeName} of the archetype in which this notification is defined}
     */
    TypeName parentName();

    static KeyedListNotificationArchetype of(final TypeName typeName,
            final NotificationEffectiveStatement statement, final TypeName parentName,
            final List<GroupingArchetype> groupings, final List<TypeObjectArchetype<?>> typeObjects,
            final List<GetterMethod> getters) {
        return new KeyedListNotificationArchetypeImpl(typeName, statement, parentName, TypeMethods.copyList(groupings),
            TypeMethods.copyList(typeObjects), TypeMethods.copyList(getters));
    }

    static KeyedListNotificationArchetype of(final TypeName typeName,
            final NotificationEffectiveStatement statement, final TypeName parentName,
            final NotificationBodyArchetype notificationBody) {
        return new KeyedListNotificationArchetypeFromGrouping(typeName, statement, parentName, notificationBody);
    }
}
