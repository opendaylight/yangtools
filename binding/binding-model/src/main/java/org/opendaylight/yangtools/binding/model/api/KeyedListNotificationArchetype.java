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
import org.opendaylight.yangtools.binding.KeyedListNotification;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link KeyedListNotification} specializations.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface KeyedListNotificationArchetype extends DataContainerArchetype.OfNotification
        permits KeyedListNotificationArchetypeFromGrouping, KeyedListNotificationArchetypeImpl {
    /**
     * {@return the {@link JavaTypeName} of the archetype in which this notification is defined}
     */
    JavaTypeName parentName();

    static KeyedListNotificationArchetype of(final JavaTypeName typeName,
            final NotificationEffectiveStatement statement, final JavaTypeName parentName,
            final List<GroupingArchetype> groupings, final List<TypeObjectArchetype<?>> typeObjects,
            final List<GetterMethod> getters) {
        return new KeyedListNotificationArchetypeImpl(typeName, statement, parentName, TypeMethods.copyList(groupings),
            TypeMethods.copyList(typeObjects), TypeMethods.copyList(getters));
    }

    static KeyedListNotificationArchetype of(final JavaTypeName typeName,
            final NotificationEffectiveStatement statement, final JavaTypeName parentName,
            final NotificationBodyArchetype notificationBody) {
        return new KeyedListNotificationArchetypeFromGrouping(typeName, statement, parentName, notificationBody);
    }
}
