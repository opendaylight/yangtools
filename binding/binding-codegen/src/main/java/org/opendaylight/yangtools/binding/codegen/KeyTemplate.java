/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;

/**
 * Template for generating JAVA class.
 */
final class KeyTemplate extends BaseTemplate {
    @NonNullByDefault
    KeyTemplate(final KeyArchetype archetype) {
        super(archetype);
    }

    @Override
    BlockBuilder body() {
        final var type = (KeyArchetype) type();
        final var typeName = type.simpleName();
        final var impl = type.getImplements().getFirst();

        return newBlockBuilder()
            .blk(wrapToDocumentation(formatDataForJavaDoc(type())))
            .blk(annotationDeclaration())
            .eol(generatedAnnotation())
            .str("public final class ").str(typeName).str(" implements ").str(importedName(impl)).jBlock(bb -> {
                bb
                    .eol("@java.io.Serial")
                    .str("private static final long serialVersionUID = ").str(type.getSUID().getValue()).eol("L;")
                    .newLine();

                final var props = type.getProperties();

                // FIXME: generate checker methods for each property

                // Fields
                for (var prop : props) {
                    bb.str("private final ").str(importedNonNull(prop.getReturnType())).sp().str(fieldName(prop)).eS();
                }

                // All values constructor
                final var sortedProps = props.stream()
                    .sorted(PROP_COMPARATOR)
                    .collect(Collectors.toUnmodifiableList());

                bb
                    .nl()
                    .eol("/**")
                    .eol(" * Constructs an instance.")
                    .eol(" *");
                for (var prop : sortedProps) {
                    bb.str(" * @param ").str(fieldName(prop)).str(" the entity ").eol(prop.getName());
                }
                bb
                    .eol(" * @throws NullPointerException if any of the arguments is {@code null}")
                    .eol(" */")
                    .at().eol(importedName(NONNULL_BY_DEFAULT))
                    .str("public ").str(type().simpleName()).str("(").str(asArgumentsDeclaration(sortedProps)).str(")")
                        .oB();
                for (var prop : sortedProps) {
                    final var fieldName = fieldName(prop);
                    bb.str("this.").str(fieldName).str(" = ").str(importedName(CODEHELPERS)).str(".requireKeyProp(")
                        .str(fieldName).str(", ").jStr(prop.getName()).str(")").frg(cloneOrNull(prop)).eS();
                    // FIXME: generate checker method invocation
                }
                bb.cB();

                final var it = props.iterator();
                do {
                    final var field = it.next();
                    final var fieldName = field.getName();
                    final var returnType = field.getReturnType();

                    bb
                        .nl()
                        // FIXME: emit a {@return .. } javadoc
                        .eol("/**")
                        .str(" * Return ").str(fieldName).eol(", guaranteed to be non-null.")
                        .eol(" *")
                        .str(" * @return {@code ").str(importedName(returnType)).str("} ").str(fieldName)
                            .eol(", guaranteed to be non-null.")
                        .eol(" */")
                        .str("public ").str(importedNonNull(returnType)).sp().str(getterMethodName(field)).str("()")
                            .oB()
                            .str("return ").str(fieldName(field)).frg(cloneOrNull(field)).eS()
                        .cB();
                } while (it.hasNext());

                bb
                    .nl()
                    .blk(generateHashCode(props));

                // FIXME: equals()
                // FIXME: toString()
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
