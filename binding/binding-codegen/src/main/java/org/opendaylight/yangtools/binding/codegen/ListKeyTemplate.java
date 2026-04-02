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
        final var bb = newBlockBuilder().txt("""
            /**
             * Constructs an instance.
             *
            """);
        for (var prop : allProperties) {
            bb.str(" * @param ").str(fieldName(prop)).str(" the entity ").eol(prop.getName());
        }

        bb.txt("""
             * @throws NullPointerException if any of the arguments are null
             */
            """)
            .str("public ").str(type().simpleName()).str("(").str(asNonNullArgumentsDeclaration(allProperties)).str(")")
                .oB();

        for (var prop : allProperties) {
            final var fieldName = fieldName(prop);
            bb.str("    this.").str(fieldName).str(" = ").str(importedName(CODEHELPERS)).str(".requireKeyProp(")
                .str(fieldName).str(", ").quoted(prop.getName()).str(")", cloneOrNull(prop)).eS();
        }

        for (var prop : properties) {
            final var restrictions = generateRestrictions(type(), fieldName(prop), prop.getReturnType());
            if (restrictions != null) {
                bb.blk(restrictions);
            }
        }

        return bb.cB();
    }

    @Override
    BlockBuilder asGetterMethod(final GeneratedProperty field) {
        final var fieldName = field.getName();
        final var returnType = field.getReturnType();

        return newBlockBuilder()
            // FIXME: emit a {@return .. } javadoc
            .eol("/**")
            .str(" * Return ").str(fieldName).eol(", guaranteed to be non-null.")
            .eol(" *")
            .str(" * @return {@code ").str(importedName(returnType)).str("} ").str(fieldName)
                .eol(", guaranteed to be non-null.")
            .eol(" */")
            .str("public ").str(importedNonNull(returnType)).sp().str(getterMethodName(field)).str("()").jBlock(bb -> {
                bb.ind("return ").str(fieldName(field), cloneOrNull(field)).eS();
            }).nl();
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
