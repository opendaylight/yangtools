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
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link InstanceNotification} specializations.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface InstanceNotificationArchetype extends DataContainerArchetype.OfNotification
        permits InstanceNotificationArchetypeFromGrouping, InstanceNotificationArchetypeImpl {
    /**
     * {@return the {@link JavaTypeName} of the archetype in which this notification is defined}
     */
    JavaTypeName parentName();

    static InstanceNotificationArchetype of(final JavaTypeName typeName,
            final NotificationEffectiveStatement statement, final JavaTypeName parentName,
            final List<GroupingArchetype> groupings, final List<TypeObjectArchetype<?>> typeObjects,
            final List<MethodSignature> methods) {
        return new InstanceNotificationArchetypeImpl(typeName, statement, parentName, TypeMethods.copyList(groupings),
            TypeMethods.copyList(typeObjects), TypeMethods.copyList(methods));
    }

    static InstanceNotificationArchetype of(final JavaTypeName typeName, final NotificationEffectiveStatement statement,
            final JavaTypeName parentName, final NotificationBodyArchetype notificationBody) {
        return new InstanceNotificationArchetypeFromGrouping(typeName, statement, parentName, notificationBody);
    }
}
