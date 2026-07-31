/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link InstanceNotification} specializations.
 *
 * @since 16.0.0
 */
public sealed interface InstanceNotificationArchetype extends DataContainerArchetype.OfNotification
        permits InstanceNotificationArchetypeImpl {
    /**
     * A builder of {@link InstanceNotificationArchetype}s.
     */
    @NonNullByDefault
    final class Builder extends DataContainerArchetypeBuilder<Builder, NotificationEffectiveStatement> {
        private Builder(final JavaTypeName typeName, final NotificationEffectiveStatement statement,
                final JavaTypeName parentName) {
            super(typeName, statement);
            // FIXME: InstanceNotificationTemplate should be performing the equivalent of these
            addImplementsType(BindingTypes.DATA_OBJECT);
            addImplementsType(BindingTypes.instanceNotification(TypeRef.of(typeName), TypeRef.of(parentName)));
        }

        @Override
        public InstanceNotificationArchetype build() {
            return new InstanceNotificationArchetypeImpl(typeName, statement, implementsTypes(), methodDefinitions(),
                enclosedTypes());
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

    @NonNullByDefault
    static Builder builder(final JavaTypeName typeName, final NotificationEffectiveStatement statement,
            final JavaTypeName parentName) {
        return new Builder(typeName, statement, parentName);
    }
}
