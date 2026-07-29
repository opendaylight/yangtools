/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * The {@link InterfaceArchetype} for {@link Notification} specializations.
 *
 * @since 16.0.0
 */
public sealed interface NotificationArchetype extends InterfaceArchetype.OfNotification
        permits NotificationArchetypeImpl {
    /**
     * A builder of {@link NotificationArchetype}s.
     */
    @NonNullByDefault
    final class Builder extends InterfaceArchetypeBuilder<Builder, NotificationEffectiveStatement> {
        private Builder(final JavaTypeName typeName, final NotificationEffectiveStatement statement) {
            super(typeName, statement);
            // FIXME: NotificationTemplate should be performing the equivalent of these
            addImplementsType(BindingTypes.DATA_OBJECT);
            addImplementsType(BindingTypes.notification(TypeRef.of(typeName)));
        }

        @Override
        public NotificationArchetype build() {
            return new NotificationArchetypeImpl(typeName, statement, annotations(), implementsTypes(),
                methodDefinitions(), enclosedTypes());
        }

        @Override
        Class<NotificationArchetype> archetypeClass() {
            return NotificationArchetype.class;
        }

        @Override
        Builder thisInstance() {
            return this;
        }
    }

    @NonNullByDefault
    static Builder builder(final JavaTypeName typeName, final NotificationEffectiveStatement statement) {
        return new Builder(typeName, statement);
    }
}
