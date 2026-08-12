/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.codegen.TypeNames.CLASS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.NONNULL_BY_DEFAULT;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.OVERRIDE;
import static org.opendaylight.yangtools.binding.contract.Naming.QNAME_STATIC_FIELD_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.VALUE_STATIC_FIELD_NAME;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.YangFeature;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.FeatureArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Template for a {@link YangFeature} class generated for a {@code feature} statement.
 */
@NonNullByDefault
final class FeatureTemplate extends ArchetypeTemplate<FeatureArchetype> {
    private static final TypeName QNAME = TypeName.ofClass(QName.class);
    private static final TypeName YANG_FEATURE = TypeName.ofClass(YangFeature.class);

    FeatureTemplate(final DataRootArchetype root, final FeatureArchetype type) {
        super(root, type);
    }

    @Override
    BlockBuilder body() {
        final var simpleName = archetype.simpleName();
        final var rootName = importedName(root.name());
        final var stmt = archetype.statement();

        return newBodyBuilder(stmt, stmt.toSchemaNode())
            .at().eol(importedName(NONNULL_BY_DEFAULT))
            .str("public final class ").str(simpleName).str(" extends ").str(importedName(YANG_FEATURE)).lt()
                .str(simpleName).cs().str(rootName).gt().jBlock(bb -> {
                    final var override = importedName(OVERRIDE);
                    final var clazz = importedName(CLASS);

                    bb
                        .frg(new QNameConstant.InClass(this, stmt.argument()))
                        .eol("/**")
                        .eol(" * The singleton instance.")
                        .eol(" */")
                        .str("public static final ").str(importedNonNull(archetype))
                            .str(" " + VALUE_STATIC_FIELD_NAME + " = new ").str(simpleName).eol("();")
                        .nl()
                        .str("private ").str(simpleName).str("()").oB()
                            .eol("// Hidden on purpose")
                        .cB()
                        .nl()
                        .at().eol(override)
                        .str("public ").str(clazz).lt().str(simpleName).str("> implementedInterface()").oB()
                            .str("return ").str(simpleName).eol(".class;")
                        .cB()
                        .nl()
                        .at().eol(override)
                        .str("public ").str(importedName(QNAME)).str(" qname()").oB()
                            .eol("return " + QNAME_STATIC_FIELD_NAME + ";")
                        .cB()
                        .nl()
                        .at().eol(override)
                        .str("public ").str(clazz).lt().str(rootName).str("> definingModule()").oB()
                            .str("return ").str(rootName).eol(".class;")
                        .cB();
                }).nl();
    }
}
