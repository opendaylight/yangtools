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
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.model.api.ActionArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.TypeRef;

/**
 * Template for {@link Action} specializations.
 */
@NonNullByDefault
final class ActionTemplate extends ArchetypeTemplate<ActionArchetype> {
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

    private static final JavaTypeName ACTION = JavaTypeName.create(Action.class);

    private ActionTemplate(final ActionArchetype archetype, final DataRootArchetype root) {
        super(GeneratedClass.of(archetype), archetype, root);
    }

    @Override
    BlockBuilder body() {

        final var simpleName = archetype.simpleName();
        final var override = importedName(OVERRIDE);
        final var input = importedName(archetype.input());
        final var output = importedName(archetype.output());
        final var parent =

        return newBodyBuilder(archetype.statement())
            .eol("@java.lang.FunctionalInterface")
//          public interface Bar extends Action<DataObjectIdentifier<Grpcont>, BarInput, BarOutput> {
            .str("public interface ").str(simpleName).str(" extends ").gen(importedName(ACTION), input, output).oB()
                .frg(new QNameConstant.InInterface(this, archetype.statement().argument()))
                .nl()
                .at().eol(override)
                .str("default ").gen(importedName(CLASS), simpleName).str(" implementedInterface()").oB()
                    .str("return ").str(simpleName).eol(".class;")
                .cB()

//              @Override
//              ListenableFuture<RpcResult<BarOutput>> invoke(DataObjectIdentifier<Grpcont> path, BarInput input);

            .cB();
    }
}
