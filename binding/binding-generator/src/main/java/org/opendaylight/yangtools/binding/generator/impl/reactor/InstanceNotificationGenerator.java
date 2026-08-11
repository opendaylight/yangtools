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
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.InstanceNotificationArchetype;
import org.opendaylight.yangtools.binding.model.NotificationBodyArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.TypeObjectArchetype;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * A {@link NotificationGenerator} producing {@link InstanceNotification}s.
 */
@NonNullByDefault
final class InstanceNotificationGenerator extends AbstractInstanceNotificationGenerator {
    InstanceNotificationGenerator(final NotificationEffectiveStatement statement,
            final CompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    InstanceNotificationArchetype createTypeImpl(final TypeName typeName,
            final NotificationEffectiveStatement statement, final TypeName parentName,
            final List<GroupingArchetype> groupings, final List<TypeObjectArchetype<?>> typeObjects,
            final List<GetterMethod> getters) {
        return InstanceNotificationArchetype.of(typeName, statement, parentName, groupings, typeObjects, getters);
    }

    @Override
    InstanceNotificationArchetype createTypeImpl(final TypeName typeName,
            final NotificationEffectiveStatement statement, final TypeName parentName,
            final NotificationBodyArchetype notificationBody) {
        return InstanceNotificationArchetype.of(typeName, statement, parentName, notificationBody);
    }
}
