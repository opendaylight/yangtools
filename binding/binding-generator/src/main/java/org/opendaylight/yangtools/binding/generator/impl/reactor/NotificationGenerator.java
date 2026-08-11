/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.binding.model.AugmentationArchetype;
import org.opendaylight.yangtools.binding.model.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.NotificationArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * Generator corresponding to a {@code notification} statement in a {@code module} or {@code submodule}, resulting in
 * a {@link Notification}.
 */
final class NotificationGenerator extends AbstractNotificationGenerator {
    @NonNullByDefault
    NotificationGenerator(final NotificationEffectiveStatement statement, final ModuleGenerator parent) {
        super(statement, parent);
    }

    @Override
    NotificationArchetype createTypeImpl(final TypeName typeName,
            final NotificationEffectiveStatement statement, final List<@NonNull GroupingArchetype> groupings,
            final List<@NonNull AugmentationArchetype> augments) {
        return NotificationArchetype.of(typeName, statement, groupings, collectTypeObjects(), collectGetters());
    }
}
