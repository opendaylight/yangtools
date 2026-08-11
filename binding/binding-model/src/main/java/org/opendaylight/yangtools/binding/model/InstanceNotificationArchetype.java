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
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.binding.model.impl.InstanceNotificationArchetypeFromGrouping;
import org.opendaylight.yangtools.binding.model.impl.InstanceNotificationArchetypeImpl;
import org.opendaylight.yangtools.binding.model.impl.TypeMethods;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link InstanceNotification} specializations.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public sealed interface InstanceNotificationArchetype extends DataContainerArchetype.OfNotification
        permits InstanceNotificationArchetypeFromGrouping, InstanceNotificationArchetypeImpl {
    @Override
    @SuppressWarnings("rawtypes")
    default Class<InstanceNotification> contract() {
        return InstanceNotification.class;
    }

    /**
     * {@return the {@link TypeName} of the archetype in which this notification is defined}
     */
    TypeName parentName();

    static InstanceNotificationArchetype of(final TypeName typeName,
            final NotificationEffectiveStatement statement, final TypeName parentName,
            final List<GroupingArchetype> groupings, final List<TypeObjectArchetype<?>> typeObjects,
            final List<GetterMethod> getters) {
        return new InstanceNotificationArchetypeImpl(typeName, statement, parentName, TypeMethods.copyList(groupings),
            TypeMethods.copyList(typeObjects), TypeMethods.copyList(getters));
    }

    static InstanceNotificationArchetype of(final TypeName typeName, final NotificationEffectiveStatement statement,
            final TypeName parentName, final NotificationBodyArchetype notificationBody) {
        return new InstanceNotificationArchetypeFromGrouping(typeName, statement, parentName, notificationBody);
    }
}
