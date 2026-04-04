/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.contract.Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.QNAME_STATIC_FIELD_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.VALUE_STATIC_FIELD_NAME;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.YangFeature;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.common.QName;

final class FeatureTemplate extends ClassTemplate {
    private static final @NonNull JavaTypeName QNAME = JavaTypeName.create(QName.class);
    private static final @NonNull JavaTypeName YANG_FEATURE = JavaTypeName.create(YangFeature.class);

    private final @NonNull Type dataRoot;

    @NonNullByDefault
    FeatureTemplate(final GeneratedTransferObject genType, final Type dataRoot) {
        super(genType);
        this.dataRoot = requireNonNull(dataRoot);
    }

    @Override
    protected BlockBuilder generateClassDeclaration(final boolean isInnerClass) {
        final var typeName = type().simpleName();

        return newBlockBuilder()
            .at().eol(importedName(NONNULL_BY_DEFAULT))
            .str("public final class ").str(typeName).str(" extends ")
                .gen(importedName(YANG_FEATURE), typeName, importedName(dataRoot));
    }

    @Override
    BlockBuilder constructors() {
        return newBlockBuilder()
            .nl()
            .str("private ").str(type().simpleName()).str("()").oB()
                .str("// Hidden on purpose").nl()
            .cB();
    }

    @Override
    String emitValueConstant(final String name, final Type type) {
        final var typeName = importedName(type());
        return "/**\n"
            +  " * {@link " + typeName + "} singleton instance.\n"
            +  " */\n"
            +  "public static final " + typeName + ' ' + VALUE_STATIC_FIELD_NAME + " = new " + typeName + "();\n";
    }

    @Override
    BlockBuilder propertyMethods() {
        final var override = importedName(OVERRIDE);
        final var typeName = type().simpleName();
        final var clazz = importedName(CLASS);
        final var rootName = importedName(dataRoot);

        return newBlockBuilder()
            .nl()
            .at().eol(override)
            .str("public ").gen(clazz, typeName).str(" " + BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME + "()").oB()
                .ind("return ").str(typeName).eol(".class;")
            .cB()
            .nl()
            .at().eol(override)
            .str("public ").str(importedName(QNAME)).str(" qname()").oB()
                .ind("return " + QNAME_STATIC_FIELD_NAME + ';').nl()
            .cB()
            .nl()
            .at().eol(override)
            .str("public ").gen(clazz, rootName).str(" definingModule()").oB()
                .ind("return ").str(rootName).eol(".class;")
            .cB();
    }
}
