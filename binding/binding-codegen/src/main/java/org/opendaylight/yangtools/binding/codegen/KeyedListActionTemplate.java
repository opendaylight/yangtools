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
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.KeyedListAction;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.model.api.KeyedListActionArchetype;

/**
 * Template for {@link KeyedListAction} specializations.
 */
@NonNullByDefault
final class KeyedListActionTemplate extends ArchetypeTemplate<KeyedListActionArchetype> {
    private static final JavaTypeName KEYED_LIST_ACTION = JavaTypeName.create(KeyedListAction.class);
    private static final JavaTypeName WITH_KEY = JavaTypeName.create(DataObjectIdentifier.WithKey.class);

    private final JavaTypeName keyName;

    KeyedListActionTemplate(final DataRootArchetype root, final KeyedListActionArchetype archetype,
            final KeyArchetype key) {
        super(GeneratedClass.of(archetype), archetype, root);
        keyName = key.name();
    }

    @Override
    BlockBuilder body() {
        final var simpleName = archetype.simpleName();
        final var input = importedName(archetype.input());
        final var output = importedName(archetype.output());
        final var parent = importedName(archetype.parentName());
        final var key = importedName(keyName);

        return newBodyBuilder(archetype.statement())
            .eol("@java.lang.FunctionalInterface")
            .str("public interface ").str(simpleName).str(" extends ")
                .gen(importedName(KEYED_LIST_ACTION), key, parent, input, output).oB()
                .frg(new QNameConstant.InInterface(this, archetype.statement().argument()))
                .nl()
                .frg(new ImplementedInterfaceMethod.Simple(this))
                .nl()
                .at().eol(importedName(OVERRIDE))
                .str(importedName(LISTENABLE_FUTURE)).str("<").gen(importedName(RPC_RESULT), output).str("> invoke(")
                    .gen(importedName(WITH_KEY), parent, key).str(" path, ").str(input).eol(" input);")
            .cB();
    }
}
