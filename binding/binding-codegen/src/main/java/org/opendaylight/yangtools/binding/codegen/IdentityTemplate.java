/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.codegen.TypeNames.CLASS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.CODEHELPERS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.OBJECT;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.OVERRIDE;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.STRING;
import static org.opendaylight.yangtools.binding.contract.Naming.VALUE_STATIC_FIELD_NAME;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.IdentityArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;

/**
 * Template for a {@link BaseIdentity} interface generated for a {@code identity} statement.
 */
@NonNullByDefault
final class IdentityTemplate extends ArchetypeTemplate<IdentityArchetype> {
    private static final TypeName BASE_IDENTITY = TypeName.ofClass(BaseIdentity.class);

    IdentityTemplate(final DataRootArchetype root, final IdentityArchetype type) {
        super(root, type);
    }

    @Override
    BlockBuilder body() {
        final var typeName = archetype.simpleName();
        final var clazz = importedName(CLASS);
        final var object = importedName(OBJECT);
        final var override = importedName(OVERRIDE);
        final var codeHelpers = importedName(CODEHELPERS);
        final var stmt = archetype.statement();

        return newBodyBuilder(stmt, stmt.toSchemaNode())
            // TODO: multi-line for more than one extended interfaces, just as we do for other interfaces
            .str("public interface ").str(typeName).str(" extends ").frg(this::appendInterfaces).oB()
                .frg(new QNameConstant.InInterface(this, stmt.argument()))
                .eol("/**")
                .str(" * Singleton value representing the {@link ").str(typeName).eol("} identity.")
                .eol(" */")
                .str(importedNonNull(archetype)).str(" " + VALUE_STATIC_FIELD_NAME + " = new ").str(typeName).str("()")
                    .oB()
                    .eol("@java.io.Serial")
                    .eol("private static final long serialVersionUID = 1L;")
                    .nl()
                    .at().eol(override)
                    .str("public ").str(clazz).lt().str(typeName).str("> implementedInterface()").oB()
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
                    .str("public ").str(importedName(STRING)).str(" toString()").oB()
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
        final var it = archetype.baseIdentities().iterator();
        if (it.hasNext()) {
            bb.str(importedName(it.next()));
            while (it.hasNext()) {
                bb.cs().str(importedName(it.next()));
            }
        } else {
            bb.str(importedName(BASE_IDENTITY));
        }
    }
}
