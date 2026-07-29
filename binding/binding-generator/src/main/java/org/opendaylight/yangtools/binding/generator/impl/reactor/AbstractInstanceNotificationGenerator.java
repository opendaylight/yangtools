/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.InterfaceArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * Abstract base generator corresponding to a {@code notification} statement used somewhere else than the top-level,
 * e.g. the semantics introduced in RFC7950.
 */
abstract sealed class AbstractInstanceNotificationGenerator<R extends CompositeRuntimeType>
        extends AbstractNotificationGenerator<R> permits InstanceNotificationGenerator, KeyedListNotificationGenerator {
    @NonNullByDefault
    AbstractInstanceNotificationGenerator(final NotificationEffectiveStatement statement,
            final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    final InterfaceArchetype createTypeImpl(final JavaTypeName typeName,
            final NotificationEffectiveStatement statement) {
        final var parent = getParent();
        final var orig = getOriginal();
        return equals(orig) ? createTypeImpl(typeName, statement, parent)
            : createTypeImpl(typeName, statement, parent, orig.getGeneratedType());
    }

    @NonNullByDefault
    abstract InterfaceArchetype createTypeImpl(JavaTypeName typeName, NotificationEffectiveStatement statement,
        AbstractCompositeGenerator<?, ?> parent);

    @NonNullByDefault
    abstract InterfaceArchetype createTypeImpl(JavaTypeName typeName, NotificationEffectiveStatement statement,
        AbstractCompositeGenerator<?, ?> parent, Archetype original);
}
