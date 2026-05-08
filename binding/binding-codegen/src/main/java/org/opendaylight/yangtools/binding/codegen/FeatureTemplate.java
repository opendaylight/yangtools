/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.contract.Naming.VALUE_STATIC_FIELD_NAME;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.YangFeature;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.FeatureArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.common.QName;


/**
 * Template for a {@link YangFeature} class generated for a {@code feature} statement.
 */
@NonNullByDefault
final class FeatureTemplate extends ArchetypeTemplate<FeatureArchetype> {
    record Builder(FeatureArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public FeatureTemplate build() {
            return new FeatureTemplate(type, root);
        }
    }

    private static final JavaTypeName QNAME = JavaTypeName.create(QName.class);
    private static final JavaTypeName YANG_FEATURE = JavaTypeName.create(YangFeature.class);

    private FeatureTemplate(final FeatureArchetype archetype, final DataRootArchetype root) {
        super(GeneratedClass.of(archetype), archetype, root);
    }

    @Override
    BlockBuilder body() {
        final var type = archetype();
        final var typeName = type.simpleName();
        final var rootName = importedName(root.name());

        return newBodyBuilder(type.statement().toSchemaNode())
            .at().eol(importedName(NONNULL_BY_DEFAULT))
            .str("public final class ").str(typeName).str(" extends ")
                .gen(importedName(YANG_FEATURE), typeName, rootName).jBlock(bb -> {
                    final var override = importedName(OVERRIDE);
                    final var clazz = importedName(CLASS);
                    final var nonNull = importedName(NONNULL);
                    final var qname = type.statement().argument();

                    bb
                        .eol("/**")
                        .eol(" * The name of the {@code feature} represented by this class.")
                        .eol(" */")
                        .str("public static final ").frg(fbb -> appendQNameField(fbb, qname))
                        .eol("/**")
                        .eol(" * The singleton instance.")
                        .eol(" */")
                        .str("public static final @").str(nonNull).sp().str(typeName)
                            .str(" " + VALUE_STATIC_FIELD_NAME + " = new ").str(typeName).eol("();")
                        .nl()
                        .str("private ").str(typeName).str("()").oB()
                            .eol("// Hidden on purpose")
                        .cB()
                        .nl()
                        .at().eol(override)
                        .str("public ").gen(clazz, typeName).str(" implementedInterface()").oB()
                            .str("return ").str(typeName).eol(".class;")
                        .cB()
                        .nl()
                        .at().eol(override)
                        .str("public ").str(importedName(QNAME)).str(" qname()").oB()
                            .eol("return QNAME;")
                        .cB()
                        .nl()
                        .at().eol(override)
                        .str("public ").gen(clazz, rootName).str(" definingModule()").oB()
                            .str("return ").str(rootName).eol(".class;")
                        .cB();
                }).nl();
    }
}
