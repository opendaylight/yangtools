/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * A {@link NotificationGenerator} producing {@link InstanceNotification}s.
 */
final class InstanceNotificationGenerator extends AbstractNotificationGenerator {
    InstanceNotificationGenerator(final NotificationEffectiveStatement statement,
            final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    Type notificationType(final GeneratedTypeBuilder builder, final TypeBuilderFactory builderFactory) {
        return BindingTypes.instanceNotification(Type.of(builder.typeName()), Type.of(getParent().typeName()));
    }
}
