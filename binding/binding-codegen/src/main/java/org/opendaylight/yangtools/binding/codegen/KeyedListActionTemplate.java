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
import org.opendaylight.yangtools.binding.KeyedListAction;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.KeyedListActionArchetype;

/**
 * Template for {@link KeyedListAction} specializations.
 */
@NonNullByDefault
final class KeyedListActionTemplate extends InterfaceTemplate<KeyedListActionArchetype> {
    record Builder(KeyedListActionArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public KeyedListActionTemplate build() {
            return new KeyedListActionTemplate(type, root);
        }
    }

    private KeyedListActionTemplate(final KeyedListActionArchetype archetype, final DataRootArchetype root) {
        super(archetype, root);
    }
}
