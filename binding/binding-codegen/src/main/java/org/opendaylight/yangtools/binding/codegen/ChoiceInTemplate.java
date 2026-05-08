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
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.model.api.ChoiceInArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

/**
 * Template for a {@link ChoiceIn} interface generated for a {@code choice} statement.
 */
@NonNullByDefault
final class ChoiceInTemplate extends ArchetypeTemplate<ChoiceInArchetype> {
    record Builder(ChoiceInArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public ChoiceInTemplate build() {
            return new ChoiceInTemplate(type, root);
        }
    }

    private static final JavaTypeName CHOICE_IN = JavaTypeName.create(ChoiceIn.class);

    private ChoiceInTemplate(final ChoiceInArchetype archetype, final DataRootArchetype root) {
        super(GeneratedClass.of(archetype), archetype, root);
    }

    @Override
    BlockBuilder body() {
        final var type = archetype();

        return newBodyBuilder(type.statement().toDataSchemaNode())
            .str("public interface ").str(type.simpleName()).str(" extends ")
                .gen(importedName(CHOICE_IN), importedName(type.choiceIn())).oB()
                .eol("/**")
                .eol(" * The name of the {@code choice} represented by this class.")
                .eol(" */")
                .frg(qnameConstant(type))
            .cB();
    }
}
