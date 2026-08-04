/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.codegen.TypeNames.LISTENABLE_FUTURE;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.OVERRIDE;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.RPC_RESULT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.OpaqueObject;
import org.opendaylight.yangtools.binding.Rpc;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.RpcArchetype;

/**
 * Template for a {@link OpaqueObject} interface generated for an {@code anydata} or {@code anyxml} statement.
 */
@NonNullByDefault
final class RpcTemplate extends ArchetypeTemplate<RpcArchetype> {
    private static final JavaTypeName RPC = JavaTypeName.create(Rpc.class);

    private RpcTemplate(final RpcArchetype archetype, final DataRootArchetype root) {
        super(GeneratedClass.of(archetype), archetype, root);
    }

    static RpcTemplate of(final DataRootArchetype root, final RpcArchetype type) {
        return new RpcTemplate(type, root);
    }

    @Override
    BlockBuilder body() {
        final var input = importedName(archetype.input());
        final var output = importedName(archetype.output());

        return newBodyBuilder(archetype.statement())
            .eol("@java.lang.FunctionalInterface")
            .str("public interface ").str(archetype.simpleName()).str(" extends ").gen(importedName(RPC), input, output)
                .oB()
                .frg(new QNameConstant.InInterface(this, archetype.statement().argument()))
                .nl()
                .frg(new ImplementedInterfaceMethod.Simple(this))
                .nl()
                .at().eol(importedName(OVERRIDE))
                .str(importedName(LISTENABLE_FUTURE)).str("<").gen(importedName(RPC_RESULT), output).str("> invoke(")
                    .str(input).eol(" input);")
            .cB();
    }
}
