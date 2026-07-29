/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.NotificationArchetype;

/**
 * Template for a {@link NotificationArchetype}.
 */
@NonNullByDefault
final class NotificationTemplate extends InterfaceTemplate<NotificationArchetype> {
    record Builder(NotificationArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public NotificationTemplate build() {
            return new NotificationTemplate(type, root);
        }
    }

    private NotificationTemplate(final NotificationArchetype archetype, final DataRootArchetype root) {
        super(archetype, root);
    }

    @Override
    void appendConstants(final BlockBuilder bb) {
        appendQNameConstant(bb, archetype.statement().argument());
    }
}
