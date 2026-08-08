/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.KeyedListNotification;
import org.opendaylight.yangtools.binding.model.api.GetterMethod;
import org.opendaylight.yangtools.binding.model.api.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.KeyedListNotificationArchetype;
import org.opendaylight.yangtools.binding.model.api.NotificationBodyArchetype;
import org.opendaylight.yangtools.binding.model.api.TypeObjectArchetype;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * A {@link NotificationGenerator} generating {@link KeyedListNotification}s.
 */
@NonNullByDefault
final class KeyedListNotificationGenerator extends AbstractInstanceNotificationGenerator {
    KeyedListNotificationGenerator(final NotificationEffectiveStatement statement, final EntryObjectGenerator parent) {
        super(statement, parent);
    }

    @Override
    KeyedListNotificationArchetype createTypeImpl(final JavaTypeName typeName,
            final NotificationEffectiveStatement statement, final JavaTypeName parentName,
            final List<GroupingArchetype> groupings, final List<TypeObjectArchetype<?>> typeObjects,
            final List<GetterMethod> getters) {
        return KeyedListNotificationArchetype.of(typeName, statement, parentName, groupings, typeObjects, getters);
    }

    @Override
    KeyedListNotificationArchetype createTypeImpl(final JavaTypeName typeName,
            final NotificationEffectiveStatement statement, final JavaTypeName parentName,
            final NotificationBodyArchetype notificationBody) {
        return KeyedListNotificationArchetype.of(typeName, statement, parentName, notificationBody);
    }
}
