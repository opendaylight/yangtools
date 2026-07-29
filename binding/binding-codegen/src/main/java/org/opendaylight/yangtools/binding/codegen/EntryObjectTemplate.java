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
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.EntryObjectArchetype;

/**
 * Template for {@link EntryObject} specializations.
 */
@NonNullByDefault
final class EntryObjectTemplate extends InterfaceTemplate<EntryObjectArchetype> {
    record Builder(EntryObjectArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public EntryObjectTemplate build() {
            return new EntryObjectTemplate(type, root);
        }
    }

    private EntryObjectTemplate(final EntryObjectArchetype archetype, final DataRootArchetype root) {
        super(archetype, root);
    }

    @Override
    void appendConstants(final BlockBuilder bb) {
        appendQNameConstant(bb, archetype.statement().argument());
    }
}
