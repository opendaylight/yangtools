/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.KeyedListNotification;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * The {@link InterfaceArchetype} for {@link KeyedListNotification} specializations.
 *
 * @since 16.0.0
 */
public sealed interface KeyedListNotificationArchetype extends InterfaceArchetype.OfNotification
        permits KeyedListNotificationArchetypeImpl {
    /**
     * A builder of {@link KeyedListNotificationArchetype}s.
     */
    @NonNullByDefault
    final class Builder extends InterfaceArchetypeBuilder<Builder, NotificationEffectiveStatement> {
        private Builder(final JavaTypeName typeName, final NotificationEffectiveStatement statement,
                final JavaTypeName parentName, final KeyArchetype key) {
            super(typeName, statement);
            // FIXME: InstanceNotificationTemplate should be performing the equivalent of these
            addImplementsType(BindingTypes.DATA_OBJECT);
            addImplementsType(BindingTypes.keyedListNotification(TypeRef.of(typeName), TypeRef.of(parentName), key));
        }

        @Override
        public KeyedListNotificationArchetype build() {
            return new KeyedListNotificationArchetypeImpl(typeName, statement, annotations(), implementsTypes(),
                constants(), methodDefinitions(), enclosedTypes());
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

    @NonNullByDefault
    static Builder builder(final JavaTypeName typeName, final NotificationEffectiveStatement statement,
            final JavaTypeName parentName, final KeyArchetype key) {
        return new Builder(typeName, statement, parentName, key);
    }
}
