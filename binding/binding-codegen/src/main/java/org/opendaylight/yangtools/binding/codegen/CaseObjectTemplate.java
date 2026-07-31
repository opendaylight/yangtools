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
import org.opendaylight.yangtools.binding.model.api.CaseObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;

/**
 * Template for a (non-existing) {@code CaseObject}.
 */
@NonNullByDefault
final class CaseObjectTemplate extends InterfaceTemplate<CaseObjectArchetype> {
    record Builder(CaseObjectArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public CaseObjectTemplate build() {
            return new CaseObjectTemplate(type, root);
        }
    }

    private CaseObjectTemplate(final CaseObjectArchetype archetype, final DataRootArchetype root) {
        super(archetype, root);
    }

    @Override
    QNameConstant constants() {
        return new QNameConstant.InInterface(this, archetype.statement().argument());
    }
}
