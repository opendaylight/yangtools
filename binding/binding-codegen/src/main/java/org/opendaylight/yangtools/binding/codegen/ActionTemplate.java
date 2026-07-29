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
import org.opendaylight.yangtools.binding.model.api.ActionArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;

/**
 * Template for {@link Action} specializations.
 */
@NonNullByDefault
final class ActionTemplate extends InterfaceTemplate<ActionArchetype> {
    record Builder(ActionArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public ActionTemplate build() {
            return new ActionTemplate(type, root);
        }
    }

    private ActionTemplate(final ActionArchetype archetype, final DataRootArchetype root) {
        super(archetype, root);
    }

    @Override
    void appendConstants(final BlockBuilder bb) {
        appendQNameConstant(bb, archetype.statement().argument());
    }
}
