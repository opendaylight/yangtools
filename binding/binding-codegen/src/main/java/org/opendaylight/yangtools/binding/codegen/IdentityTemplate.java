/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.contract.Naming.VALUE_STATIC_FIELD_NAME;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.IdentityArchetype;
import org.opendaylight.yangtools.binding.model.ri.Types;

/**
 * Template for a {@link BaseIdentity} interface generated for a {@code identity} statement.
 */
@NonNullByDefault
final class IdentityTemplate extends ArchetypeTemplate<IdentityArchetype> {
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

    private IdentityTemplate(final IdentityArchetype archetype, final DataRootArchetype root) {
        super(GeneratedClass.of(archetype), archetype, root);
    }

    @Override
    BlockBuilder body() {
        final var type = archetype();
        final var typeName = type.simpleName();
        final var clazz = importedName(CLASS);
        final var object = importedName(Types.objectType());
        final var override = importedName(OVERRIDE);
        final var codeHelpers = importedName(CODEHELPERS);
        final var stmt = type.statement();

        return newBodyBuilder(stmt, stmt.toSchemaNode())
            .str("public interface ").str(typeName).str(" extends ").frg(this::appendInterfaces).oB()
                .eol("/**")
                .eol(" * The name of the {@code identity} represented by this class.")
                .eol(" */")
                .frg(qnameConstant(type))
                .eol("/**")
                .str(" * Singleton value representing the {@link ").str(typeName).eol("} identity.")
                .eol(" */")
                .str(importedNonNull(type)).str(" " + VALUE_STATIC_FIELD_NAME + " = new ").str(typeName).str("()").oB()
                    .eol("@java.io.Serial")
                    .eol("private static final long serialVersionUID = 1L;")
                    .nl()
                    .at().eol(override)
                    .str("public ").gen(clazz, typeName).str(" implementedInterface()").oB()
                        .str("return ").str(typeName).eol(".class;")
                    .cB()
                    .nl()
                    .at().eol(override)
                    .str("public int hashCode()").oB()
                        .str("return ").str(typeName).eol(".class.hashCode();")
                    .cB()
                    .nl()
                    .at().eol(override)
                    .str("public boolean equals(").str(object).str(" obj)").oB()
                        .str("return ").str(codeHelpers).eol(".biEQ(this, obj);")
                    .cB()
                    .nl()
                    .at().eol(override)
                    .str("public ").str(importedName(Types.STRING)).str(" toString()").oB()
                        .str("return ").str(codeHelpers).str(".biTS(").str(typeName).eol(".class, QNAME);")
                    .cB()
                    .nl()
                    .eol("@java.io.Serial")
                    .str("private ").str(object).str(" readResolve() throws java.io.ObjectStreamException").oB()
                        .eol("return " + VALUE_STATIC_FIELD_NAME + ";")
                    .cB()
                .cb().eS()
                .nl()
                .at().eol(override)
                .str(clazz).str("<? extends ").str(typeName).eol("> implementedInterface();")
            .cB();
    }

    private void appendInterfaces(final BlockBuilder bb) {
        final var it = archetype().interfaces().iterator();
        bb.str(importedName(it.next()));
        while (it.hasNext()) {
            bb.str(", ").str(importedName(it.next()));
        }
    }
}
