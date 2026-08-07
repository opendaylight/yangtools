/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.codegen.TypeNames.OVERRIDE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.model.api.ChoiceInArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

/**
 * Template for a {@link ChoiceIn} interface generated for a {@code choice} statement.
 */
@NonNullByDefault
final class ChoiceInTemplate extends ArchetypeTemplate<ChoiceInArchetype> {
    private static final JavaTypeName CHOICE_IN = JavaTypeName.create(ChoiceIn.class);

    ChoiceInTemplate(final DataRootArchetype root, final ChoiceInArchetype archetype) {
        super(root, archetype);
    }

    @Override
    BlockBuilder body() {
        final var simpleName = archetype.simpleName();
        final var stmt = archetype.statement();
        return newBodyBuilder(stmt)
            .str("public interface ").str(simpleName).str(" extends ")
                .gen(importedName(CHOICE_IN), importedName(archetype.parentName()), importedName(archetype)).oB()
                .frg(new QNameConstant.InInterface(this, stmt.argument()))
                .nl()
                .at().eol(importedName(OVERRIDE))
                .str("default Class<").str(simpleName).str("> implementedChoice()").oB()
                    .str("return ").str(simpleName).eol(".class;")
                .cB()
            .cB();
    }
}
