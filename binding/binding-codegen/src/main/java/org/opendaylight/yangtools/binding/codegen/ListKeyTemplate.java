/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;

/**
 * Template for generating JAVA class.
 */
final class ListKeyTemplate extends ClassTemplate {
    /**
     * Creates instance of this class with concrete <code>genType</code>.
     *
     * @param genType generated transfer object which will be transformed to JAVA class source code
     */
    @NonNullByDefault
    ListKeyTemplate(final GeneratedTransferObject genType) {
        super(genType);
    }

    @Override
    String finalClass() {
        return " final ";
    }

    @Override
    BlockBuilder allValuesConstructor() {
        // FIXME: use BlockBuilder
        final var sb = new StringBuilder().append("""
            /**
             * Constructs an instance.
             *
            """);
        for (var prop : allProperties) {
            sb.append(" * @param ").append(fieldName(prop)).append(" the entity ").append(prop.getName()).append('\n');
        }

        sb.append("""
             * @throws NullPointerException if any of the arguments are null
             */
            public\s""").append(type().simpleName()).append('(').append(asNonNullArgumentsDeclaration(allProperties))
                .append(") {\n");

        for (var prop : allProperties) {
            final var fieldName = fieldName(prop);
            sb.append("    this.").append(fieldName).append(" = ")
                .append(importedName(CODEHELPERS)).append(".requireKeyProp(").append(fieldName).append(", \"")
                .append(prop.getName()).append("\")").append(cloneCall(prop)).append(";\n");
        }

        for (var prop : properties) {
            final var restrictions = generateRestrictions(type(), fieldName(prop), prop.getReturnType());
            if (restrictions != null) {
                sb.append(restrictions);
            }
        }

        final var bb = new BlockBuilder();
        bb.append(sb.append("}\n"));
        return bb;
    }

    @Override
    StringBuilder asGetterMethod(final GeneratedProperty field) {
        final var fieldName = field.getName();
        final var returnType = field.getReturnType();

        return new StringBuilder()
            .append("/**\n")
            .append(" * Return ").append(fieldName).append(", guaranteed to be non-null.\n")
            .append(" *\n")
            .append(" * @return {@code ").append(importedName(returnType)).append("} ").append(fieldName)
                .append(", guaranteed to be non-null.\n")
            .append(" */\n")
            .append("public ").append(importedNonNull(returnType)).append(' ').append(getterMethodName(field))
                .append("() {\n")
            .append("    return ").append(fieldName(field)).append(cloneCall(field)).append(";\n")
            .append("}\n");
    }

    @Override
    String formatDataForJavaDoc(final GeneratedType type) {
        final var listType = findListType(type);
        if (listType == null) {
            return "";
        }

        final var importedName = importedName(listType);
        return "This class represents the key of {@link " + importedName + "} class.\n"
            +  '\n'
            +  "@see " + importedName + '\n';
    }

    private static @Nullable Type findListType(final @NonNull GeneratedType type) {
        for (var impl : type.getImplements()) {
            if (impl instanceof ParameterizedType paramType) {
                final var identifiable = BindingTypes.extractKeyType(paramType);
                if (identifiable != null) {
                    return identifiable;
                }
            }
        }
        return null;
    }
}
