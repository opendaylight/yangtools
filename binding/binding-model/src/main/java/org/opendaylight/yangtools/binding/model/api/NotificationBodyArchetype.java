/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.NotificationBody;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * The {@link DataContainerArchetype} for {@link NotificationBody} specializations.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface NotificationBodyArchetype extends DataContainerArchetype.Partial
        permits NotificationBodyArchetypeImpl {
    @Override
    NotificationEffectiveStatement statement();

    static NotificationBodyArchetype of(final TypeName typeName, final NotificationEffectiveStatement statement,
            final List<GroupingArchetype> groupings, final List<TypeObjectArchetype<?>> typeObjects,
            final List<GetterMethod> getters) {
        return new NotificationBodyArchetypeImpl(typeName, statement, TypeMethods.copyList(groupings),
            TypeMethods.copyList(typeObjects), TypeMethods.copyList(getters));
    }
}
