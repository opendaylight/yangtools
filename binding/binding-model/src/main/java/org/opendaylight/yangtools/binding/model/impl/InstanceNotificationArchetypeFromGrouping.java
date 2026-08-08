/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.impl;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.GetterMethod;
import org.opendaylight.yangtools.binding.model.api.InstanceNotificationArchetype;
import org.opendaylight.yangtools.binding.model.api.NotificationBodyArchetype;
import org.opendaylight.yangtools.binding.model.api.TypeObjectArchetype;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

@NonNullByDefault
public record InstanceNotificationArchetypeFromGrouping(
        TypeName name,
        NotificationEffectiveStatement statement,
        TypeName parentName,
        NotificationBodyArchetype notificationBody) implements InstanceNotificationArchetype {
    public InstanceNotificationArchetypeFromGrouping {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(parentName);
        requireNonNull(notificationBody);
    }

    @Override
    public List<Partial> partials() {
        return List.of(notificationBody);
    }

    @Override
    public List<TypeObjectArchetype<?>> typeObjects() {
        return List.of();
    }

    @Override
    public List<GetterMethod> getters() {
        return List.of();
    }

    @Override
    public int hashCode() {
        return TypeMethods.hashCode(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return TypeMethods.equals(this, obj);
    }

    @Override
    public String toString() {
        return TypeMethods.toStringHelper(InstanceNotificationArchetype.class, this)
            .add("parentName", parentName)
            .toString();
    }
}
