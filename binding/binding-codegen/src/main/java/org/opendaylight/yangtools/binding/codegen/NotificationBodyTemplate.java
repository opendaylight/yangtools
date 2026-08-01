/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.NotificationBody;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.NotificationBodyArchetype;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * Template for {@link NotificationBody} specializations.
 */
@NonNullByDefault
final class NotificationBodyTemplate extends InterfaceTemplate<NotificationBodyArchetype> {
    record Builder(NotificationBodyArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public NotificationBodyTemplate build() {
            return new NotificationBodyTemplate(type, root);
        }
    }

    private static final ConcreteType NOTIFICATION_BODY = ConcreteType.ofClass(NotificationBody.class);

    private NotificationBodyTemplate(final NotificationBodyArchetype archetype, final DataRootArchetype root) {
        super(archetype, root, DataContainerContract.NARROW, false);
    }

    @Override
    @Nullable NotificationBodyArchetype builderTarget() {
        return null;
    }

    @Override
    Iterator<? extends Type> extendsTypes() {
        return Iterators.concat(
            Iterators.singletonIterator(ParameterizedType.of(NOTIFICATION_BODY, archetype)),
            super.extendsTypes());
    }
}
