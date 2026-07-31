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
import org.opendaylight.yangtools.binding.model.api.ContainerArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;

/**
 * Template for a {@link ContainerArchetype}.
 */
@NonNullByDefault
final class ContainerTemplate extends ChildOfTemplate<ContainerArchetype> {
    record Builder(ContainerArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public ContainerTemplate build() {
            return new ContainerTemplate(type, root);
        }
    }

    private ContainerTemplate(final ContainerArchetype archetype, final DataRootArchetype root) {
        super(archetype, root);
    }

    @Override
    QNameConstant constants() {
        return new QNameConstant.InInterface(this, archetype.statement().argument());
    }
}
