/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.binding.model.api.InstanceNotificationArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.NotificationBodyArchetype;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * A {@link NotificationGenerator} producing {@link InstanceNotification}s.
 */
final class InstanceNotificationGenerator extends AbstractInstanceNotificationGenerator {
    @NonNullByDefault
    InstanceNotificationGenerator(final NotificationEffectiveStatement statement,
            final DataContainerGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    InstanceNotificationArchetype createTypeImpl(final JavaTypeName typeName,
            final NotificationEffectiveStatement statement, final JavaTypeName parentName) {
        final var builder = InstanceNotificationArchetype.builder(typeName, statement, parentName);
        addUsesInterfaces(builder);
        addGetterMethods(builder);
        return builder.build();
    }

    @Override
    InstanceNotificationArchetype createTypeImpl(final JavaTypeName typeName,
            final NotificationEffectiveStatement statement, final JavaTypeName parentName,
            final NotificationBodyArchetype original) {
        return InstanceNotificationArchetype.builder(typeName, statement, parentName)
            .addImplementsType(original)
            .build();
    }
}
