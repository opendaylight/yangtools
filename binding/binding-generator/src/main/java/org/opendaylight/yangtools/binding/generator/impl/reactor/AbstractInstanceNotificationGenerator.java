/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.NotificationBodyArchetype;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * Abstract base generator corresponding to a {@code notification} statement used somewhere else than the top-level,
 * e.g. the semantics introduced in RFC7950.
 */
abstract sealed class AbstractInstanceNotificationGenerator extends AbstractNotificationGenerator
        permits InstanceNotificationGenerator, KeyedListNotificationGenerator {
    @NonNullByDefault
    AbstractInstanceNotificationGenerator(final NotificationEffectiveStatement statement,
            final DataContainerGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    final DataContainerArchetype.OfNotification createTypeImpl(final JavaTypeName typeName,
            final NotificationEffectiveStatement statement) {
        final var parentName = getParent().typeName();
        final var orig = getOriginal();
        return equals(orig) ? createTypeImpl(typeName, statement, parentName)
            : createTypeImpl(typeName, statement, parentName, (NotificationBodyArchetype) orig.getGeneratedType());
    }

    @NonNullByDefault
    abstract DataContainerArchetype.OfNotification createTypeImpl(JavaTypeName typeName,
        NotificationEffectiveStatement statement, JavaTypeName parentName);

    @NonNullByDefault
    abstract DataContainerArchetype.OfNotification createTypeImpl(JavaTypeName typeName,
        NotificationEffectiveStatement statement, JavaTypeName parentName, NotificationBodyArchetype original);
}
