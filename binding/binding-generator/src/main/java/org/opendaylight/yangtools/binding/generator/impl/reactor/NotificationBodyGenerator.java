/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * A composite generator producing {@link NotificationBody}s for {@code notifications} declared in {@code grouping}s.
 */
final class NotificationBodyGenerator
        extends CompositeSchemaTreeGenerator<NotificationEffectiveStatement, NotificationBodyRuntimeType> {
    NotificationBodyGenerator(final NotificationEffectiveStatement statement, final GroupingGenerator parent) {
        super(statement, parent);
    }

//    @Override
//    Type notificationType(final GeneratedTypeBuilder builder, final TypeBuilderFactory builderFactory) {
//        // FIXME: proper type
//        // FIXME: shape is not shared
//        return BindingTypes.groupingNotification(null);
//    }
}
