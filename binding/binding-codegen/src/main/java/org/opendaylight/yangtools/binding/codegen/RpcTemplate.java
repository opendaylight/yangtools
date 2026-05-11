/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.OpaqueObject;
import org.opendaylight.yangtools.binding.Rpc;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.RpcArchetype;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Template for a {@link OpaqueObject} interface generated for an {@code anydata} or {@code anyxml} statement.
 */
@NonNullByDefault
final class RpcTemplate extends ArchetypeTemplate<RpcArchetype> {
    record Builder(RpcArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public RpcTemplate build() {
            return new RpcTemplate(type, root);
        }
    }

    private static final JavaTypeName LISTENABLE_FUTURE = JavaTypeName.create(ListenableFuture.class);
    private static final JavaTypeName RPC = JavaTypeName.create(Rpc.class);
    private static final JavaTypeName RPC_RESULT = JavaTypeName.create(RpcResult.class);

    private RpcTemplate(final RpcArchetype archetype, final DataRootArchetype root) {
        super(GeneratedClass.of(archetype), archetype, root);
    }

    @Override
    BlockBuilder body() {
        final var type = archetype();
        final var simpleName = type.simpleName();
        final var override = importedName(OVERRIDE);
        final var input = importedName(type.input());
        final var output = importedName(type.output());

        return newBodyBuilder(type.statement())
            .eol("@java.lang.FunctionalInterface")
            .str("public interface ").str(simpleName).str(" extends ").gen(importedName(RPC), input, output).oB()
                .eol("/**")
                .eol(" * The YANG identifier of the {@code rpc} statement represented by this class.")
                .eol(" */")
                .frg(qnameConstant(type))
                .nl()
                .at().eol(override)
                .str("default ").gen(importedName(CLASS), simpleName).str(" implementedInterface()").oB()
                    .str("return ").str(simpleName).eol(".class;")
                .cB()
                .nl()
                .at().eol(override)
                .str(importedName(LISTENABLE_FUTURE)).str("<").gen(importedName(RPC_RESULT), output).str("> invoke(")
                    .str(input).eol(" input);")
            .cB();
    }
}
