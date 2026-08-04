/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.OpaqueObject;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.OpaqueObjectArchetype;

/**
 * Template for a {@link OpaqueObject} interface generated for an {@code anydata} or {@code anyxml} statement.
 */
@NonNullByDefault
final class OpaqueObjectTemplate extends ArchetypeTemplate<OpaqueObjectArchetype<?>> {
    private static final JavaTypeName OPAQUE_OBJECT = JavaTypeName.create(OpaqueObject.class);

    OpaqueObjectTemplate(final DataRootArchetype root, final OpaqueObjectArchetype<?> archetype) {
        super(root, archetype);
    }

    @Override
    BlockBuilder body() {
        final var simpleName = archetype.simpleName();

        return newBodyBuilder(archetype.statement())
            .str("public interface ").str(simpleName).str(" extends ").gen(importedName(OPAQUE_OBJECT), simpleName).oB()
                .frg(new QNameConstant.InInterface(this, archetype.statement().argument()))
                .nl()
                .frg(new ImplementedInterfaceMethod.Simple(this))
            .cB();
    }
}
