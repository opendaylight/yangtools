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
import org.opendaylight.yangtools.binding.OpaqueObject;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.OpaqueObjectArchetype;

/**
 * Template for a {@link OpaqueObject} interface generated for an {@code anydata} or {@code anyxml} statement.
 */
@NonNullByDefault
final class OpaqueObjectTemplate extends ArchetypeTemplate<OpaqueObjectArchetype<?>> {
    record Builder(OpaqueObjectArchetype<?> type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public OpaqueObjectTemplate build() {
            return new OpaqueObjectTemplate(type, root);
        }
    }

    private static final JavaTypeName OPAQUE_OBJECT = JavaTypeName.create(OpaqueObject.class);

    private OpaqueObjectTemplate(final OpaqueObjectArchetype<?> archetype, final DataRootArchetype root) {
        super(GeneratedClass.of(archetype), archetype, root);
    }

    @Override
    BlockBuilder body() {
        final var type = archetype();
        final var simpleName = type.simpleName();
        final var stmt = switch (type) {
            case OpaqueObjectArchetype.Anydata anydata -> "anydata";
            case OpaqueObjectArchetype.Anyxml anyxml -> "anyxml";
        };

        return newBodyBuilder(type.statement())
            .str("public interface ").str(simpleName).str(" extends ").gen(importedName(OPAQUE_OBJECT), simpleName).oB()
                .eol("/**")
                .str(" * The YANG identifier of the {@code ").str(stmt).eol("} represented by this class.")
                .eol(" */")
                .frg(qnameConstant(type))
                .nl()
                .at().eol(importedName(OVERRIDE))
                .str("default ").gen(importedName(CLASS), simpleName).str(" implementedInterface()").oB()
                    .str("return ").str(simpleName).eol(".class;")
                .cB()
            .cB();
    }
}
