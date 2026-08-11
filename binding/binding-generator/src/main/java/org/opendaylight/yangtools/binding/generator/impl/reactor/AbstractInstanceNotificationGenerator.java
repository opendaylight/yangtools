/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import com.google.common.base.VerifyException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.AugmentationArchetype;
import org.opendaylight.yangtools.binding.model.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.NotificationBodyArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.TypeObjectArchetype;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * Abstract base generator corresponding to a {@code notification} statement used somewhere else than the top-level,
 * e.g. the semantics introduced in RFC7950.
 */
@NonNullByDefault
abstract sealed class AbstractInstanceNotificationGenerator extends AbstractNotificationGenerator
        permits InstanceNotificationGenerator, KeyedListNotificationGenerator {
    AbstractInstanceNotificationGenerator(final NotificationEffectiveStatement statement,
            final CompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    final DataContainerArchetype.OfNotification createTypeImpl(final TypeName typeName,
            final NotificationEffectiveStatement statement, final List<GroupingArchetype> groupings,
            final List<AugmentationArchetype> augments) {
        final var parentName = getParent().typeName();
        final var orig = getOriginal();
        if (orig.equals(this)) {
            return createTypeImpl(typeName, statement, parentName, groupings, collectTypeObjects(), collectGetters());
        }

        final var origArchetype = orig.getGeneratedType();
        if (!(origArchetype instanceof NotificationBodyArchetype notificationBody)) {
            throw new VerifyException("Unexpected original " + origArchetype);
        }
        if (!groupings.isEmpty()) {
            throw new VerifyException("Unexpected groupings in " + statement);
        }
        return createTypeImpl(typeName, statement, parentName, notificationBody);
    }

    abstract DataContainerArchetype.OfNotification createTypeImpl(TypeName typeName,
        NotificationEffectiveStatement statement, TypeName parentName, List<GroupingArchetype> groupings,
        List<TypeObjectArchetype<?>> typeObjects, List<GetterMethod> getters);

    abstract DataContainerArchetype.OfNotification createTypeImpl(TypeName typeName,
        NotificationEffectiveStatement statement, TypeName parentName, NotificationBodyArchetype notificationBody);
}
