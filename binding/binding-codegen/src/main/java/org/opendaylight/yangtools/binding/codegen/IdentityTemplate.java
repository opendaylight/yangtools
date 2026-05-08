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
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.IdentityArchetype;

/**
 * Template for a {@link BaseIdentity} interface generated for a {@code identity} statement.
 */
@NonNullByDefault
final class IdentityTemplate extends InterfaceTemplate {
    record Builder(IdentityArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public IdentityTemplate build() {
            return new IdentityTemplate(type, root);
        }
    }

    private final DataRootArchetype root;

    private IdentityTemplate(final IdentityArchetype archetype, final DataRootArchetype root) {
        super(archetype);
        this.root = requireNonNull(root);
    }
}
