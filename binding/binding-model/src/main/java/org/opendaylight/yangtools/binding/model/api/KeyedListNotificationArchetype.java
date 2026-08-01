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
import org.opendaylight.yangtools.binding.KeyedListNotification;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link KeyedListNotification} specializations.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface KeyedListNotificationArchetype extends DataContainerArchetype.OfNotification
        permits KeyedListNotificationArchetypeImpl {
    /**
     * A builder of {@link KeyedListNotificationArchetype}s.
     */
    final class Builder extends DataContainerArchetypeBuilder<Builder, NotificationEffectiveStatement> {
        private final JavaTypeName parentName;

        private Builder(final JavaTypeName typeName, final NotificationEffectiveStatement statement,
                final JavaTypeName parentName, final List<GroupingArchetype> groupings) {
            super(typeName, statement, groupings);
            this.parentName = requireNonNull(parentName);
        }

        @Override
        public KeyedListNotificationArchetype build() {
            return new KeyedListNotificationArchetypeImpl(typeName, statement, parentName, implementsTypes,
                methodDefinitions(), enclosedTypes());
        }

        @Override
        Class<KeyedListNotificationArchetype> archetypeClass() {
            return KeyedListNotificationArchetype.class;
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

    static KeyedListNotificationArchetype of(final JavaTypeName typeName,
            final NotificationEffectiveStatement statement, final JavaTypeName parentName,
            final NotificationBodyArchetype notificationBody) {
        return new KeyedListNotificationArchetypeImpl(typeName, statement, parentName,
            Collections.singletonList(requireNonNull(notificationBody)), List.of(), List.of());
    }

    /**
     * {@return the {@link JavaTypeName} of the archetype in which this notification is defined}
     */
    JavaTypeName parentName();
}
