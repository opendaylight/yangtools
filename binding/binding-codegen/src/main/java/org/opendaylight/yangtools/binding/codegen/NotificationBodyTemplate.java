/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.NotificationBody;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.NotificationBodyArchetype;
import org.opendaylight.yangtools.binding.model.Type;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;

/**
 * Template for {@link NotificationBody} specializations.
 */
@NonNullByDefault
final class NotificationBodyTemplate extends InterfaceTemplate<NotificationBodyArchetype> {
    private static final ConcreteType NOTIFICATION_BODY = ConcreteType.ofClass(NotificationBody.class);

    NotificationBodyTemplate(final DataRootArchetype root, final NotificationBodyArchetype archetype) {
        super(root, archetype);
    }

    @Override
    Iterator<? extends Type> extendsTypes() {
        return Iterators.concat(
            Iterators.singletonIterator(ParameterizedType.of(NOTIFICATION_BODY, archetype)),
            super.extendsTypes());
    }
}
