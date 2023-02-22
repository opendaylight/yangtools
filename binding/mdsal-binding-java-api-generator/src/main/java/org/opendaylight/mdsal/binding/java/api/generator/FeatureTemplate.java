/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.Constant;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.YangFeature;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.common.QName;

final class FeatureTemplate extends ClassTemplate {
    private static final @NonNull JavaTypeName QNAME = JavaTypeName.create(QName.class);
    private static final @NonNull JavaTypeName YANG_FEATURE = JavaTypeName.create(YangFeature.class);

    private final @NonNull Type dataRoot;

    FeatureTemplate(final GeneratedTransferObject genType, final Type dataRoot) {
        super(genType);
        this.dataRoot = requireNonNull(dataRoot);
    }

    @Override
    protected String generateClassDeclaration(final boolean isInnerClass) {
        final var typeName = type().getName();

        return "@" + importedName(NONNULL_BY_DEFAULT) + '\n'
            + "public final class " + typeName + " extends " + importedName(YANG_FEATURE) + '<' + typeName + ", "
            + importedName(dataRoot) + '>';
    }

    @Override
    protected String constructors() {
        final var typeName = type().getName();

        return "private " + typeName + "() {\n"
            + "    // Hidden on purpose\n"
            + "}";
    }

    @SuppressWarnings("checkstyle:ParameterName")
    @Override
    protected CharSequence emitConstant(final Constant c) {
        if (!Naming.VALUE_STATIC_FIELD_NAME.equals(c.getName()) || !YangFeature.class.equals(c.getValue())) {
            return super.emitConstant(c);
        }

        final var type = type();
        final var typeName = type.getName();
        return "/**\n"
            + " * {@link " + typeName + "} singleton instance.\n"
            + " */\n"
            + "public static final " + importedName(type) + ' ' + Naming.VALUE_STATIC_FIELD_NAME + " = new "
            + type.getName() + "();";
    }

    @Override
    protected String propertyMethods() {
        final var override = importedName(OVERRIDE);
        final var typeName = type().getName();
        final var clazz = importedName(CLASS);
        final var rootName = importedName(dataRoot);

        return '@' + override + '\n'
            + "public " + clazz + '<' + typeName + "> " + Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME + "() {\n"
            + "    return " + typeName + ".class;\n"
            + "}\n"
            + '\n'
            + '@' + override + '\n'
            + "public " + importedName(QNAME) + " qname() {\n"
            + "    return " + Naming.QNAME_STATIC_FIELD_NAME + ";\n"
            + "}\n"
            + '\n'
            + '@' + override + '\n'
            + "public " + clazz + '<' + rootName + "> definingModule() {\n"
            + "    return " + rootName + ".class;\n"
            + "}\n";
    }
}
