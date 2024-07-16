/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

/**
 * An archetype for an {@link InstanceNotification}.
 */
@NonNullByDefault
public final class InstanceNotificationArchetype extends DataObjectArchetype<InstanceNotificationArchetype> {
    public InstanceNotificationArchetype(final JavaTypeName typeName) {
        super(typeName);
    }

    @Override
    public Class<InstanceNotificationArchetype> archetypeContract() {
        return InstanceNotificationArchetype.class;
    }
}
