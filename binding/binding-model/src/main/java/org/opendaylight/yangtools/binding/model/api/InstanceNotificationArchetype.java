/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
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
        permits InstanceNotificationArchetypeImpl {
    /**
     * A builder of {@link InstanceNotificationArchetype}s.
     */
    final class Builder extends DataContainerArchetypeBuilder<Builder, NotificationEffectiveStatement> {
        private final JavaTypeName parentName;

        private Builder(final JavaTypeName typeName, final NotificationEffectiveStatement statement,
                final JavaTypeName parentName, final List<? extends DataContainerArchetype> implementsTypes) {
            super(typeName, statement, implementsTypes);
            this.parentName = requireNonNull(parentName);
        }

        @Override
        public InstanceNotificationArchetype build() {
            return new InstanceNotificationArchetypeImpl(typeName, statement, parentName, implementsTypes,
                methodDefinitions(), enclosedTypes());
        }

        @Override
        Class<InstanceNotificationArchetype> archetypeClass() {
            return InstanceNotificationArchetype.class;
        }

        @Override
        Builder thisInstance() {
            return this;
        }
    }

    static Builder builder(final JavaTypeName typeName, final NotificationEffectiveStatement statement,
            final JavaTypeName parentName, final List<GroupingArchetype> groupings) {
        return new Builder(typeName, statement, parentName, groupings);
    }

    static InstanceNotificationArchetype of(final JavaTypeName typeName, final NotificationEffectiveStatement statement,
            final JavaTypeName parentName, final NotificationBodyArchetype notificationBody) {
        return new InstanceNotificationArchetypeImpl(typeName, statement, parentName,
            Collections.singletonList(requireNonNull(notificationBody)), List.of(), List.of());
    }

    /**
     * {@return the {@link JavaTypeName} of the archetype in which this notification is defined}
     */
    JavaTypeName parentName();
}
