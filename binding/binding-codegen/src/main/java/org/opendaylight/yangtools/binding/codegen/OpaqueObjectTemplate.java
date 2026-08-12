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
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.OpaqueObjectArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;

/**
 * Template for a {@link OpaqueObject} interface generated for an {@code anydata} or {@code anyxml} statement.
 */
@NonNullByDefault
final class OpaqueObjectTemplate extends ArchetypeTemplate<OpaqueObjectArchetype<?>> {
    private static final TypeName OPAQUE_OBJECT = TypeName.ofClass(OpaqueObject.class);

    OpaqueObjectTemplate(final DataRootArchetype root, final OpaqueObjectArchetype<?> archetype) {
        super(root, archetype);
    }

    @Override
    BlockBuilder body() {
        final var simpleName = archetype.simpleName();

        return newBodyBuilder(archetype.statement())
            .str("public interface ").str(simpleName).str(" extends ").str(importedName(OPAQUE_OBJECT)).lt()
                .str(simpleName).gt().oB()
                .frg(new QNameConstant.InInterface(this, archetype.statement().argument()))
                .nl()
                .frg(new ImplementedInterfaceMethod.Simple(this))
            .cB();
    }
}
