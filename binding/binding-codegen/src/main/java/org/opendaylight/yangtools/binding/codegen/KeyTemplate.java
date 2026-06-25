/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.model.ri.Types.PRIMITIVE_BOOLEAN;
import static org.opendaylight.yangtools.binding.model.ri.Types.STRING;

import com.google.common.base.VerifyException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * A template for {@link Key} specializations.
 */
@NonNullByDefault
final class KeyTemplate extends ArchetypeTemplate<KeyArchetype> {
    record Builder(KeyArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public KeyTemplate build() {
            return new KeyTemplate(type, root);
        }
    }

    private static final JavaTypeName KEY = JavaTypeName.create(Key.class);
    /**
     * {@code java.lang.Boolean} as a JavaTypeName.
     */
    private static final JavaTypeName BOOLEAN = JavaTypeName.create(Boolean.class);

    private KeyTemplate(final KeyArchetype archetype, final DataRootArchetype root) {
        super(GeneratedClass.of(archetype), archetype, root);
    }

    @Override
    BlockBuilder body() {
        final var type = (KeyArchetype) type();
        final var typeName = type.simpleName();
        final var entryObject = importedName(type.entryObject());

        return newBlockBuilder()
            // FIXME: take advantage of javadocBlock() to add a module reference and a snippet
            .eol("/**")
            .str(" * This class represents the key of {@link ").str(entryObject).eol("} class.")
            .eol(" *")
            .str(" * @see ").eol(entryObject)
            .eol(" */")
            .eol(generatedAnnotation())
            // FIXME: YANGTOOLS-1812: generate deprecated annotation once we have the EntryObject's Archetype available
            .str("public final class ").str(typeName).str(" implements ").gen(importedName(KEY), entryObject)
                .jBlock(this::classBody).nl();
    }

    // Split out to keep indentation in check
    private void classBody(final BlockBuilder bb) {
        final var type = (KeyArchetype) type();

        bb
            .eol("@java.io.Serial")
            .str("private static final long serialVersionUID = ").jLong(type.serialVersionUID()).eS()
            .newLine();

        // Fields
        // FIXME: generate checker methods for each property
        final var props = type.getProperties();
        for (var prop : props) {
            bb.str("private final ").str(importedNonNull(prop.getReturnType())).sp().str(fieldName(prop)).eS();
        }

        // All values constructor
        final var sortedProps = props.stream().sorted(PROP_COMPARATOR).toList();
        bb
            .nl()
            .eol("/**")
            .eol(" * Constructs an instance.")
            .eol(" *");
        for (var prop : sortedProps) {
            bb.str(" * @param ").str(fieldName(prop)).str(" the entity ").eol(prop.getName());
        }
        bb
            .eol(" */")
            .str("public ").str(type.simpleName()).str("(").str(asNonNullArgumentsDeclaration(sortedProps)).str(")")
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
                // TODO: addComment(propBuilder, leaf) or as we should be able to look up the EbtryObjectArchetype and
                //       get the leaf from there: and then we do not need to store the types at all
                .str("public ").str(importedNonNull(returnType)).sp().str(getterMethodName(field)).str("()")
                .oB()
                .str("return ").str(fieldName(field)).frg(cloneOrNull(field)).eS()
                .cB();
        } while (it.hasNext());

        appendEquality(bb, javaType(), props, false);
    }

    /**
     * Template method which generates method parameters with their types from <code>parameters</code>, annotating them
     * with {@link NonNull}.
     *
     * @param parameters group of generated property instances which are transformed to the method parameters
     * @return string with the list of the method parameters with their types in JAVA format
     */
    private String asNonNullArgumentsDeclaration(final List<GeneratedProperty> parameters) {
        final var it = parameters.iterator();
        if (!it.hasNext()) {
            return "";
        }

        final var sb = new StringBuilder();
        while (true) {
            final var parameter = it.next();
            sb.append(importedNonNull(parameter.getReturnType())).append(' ').append(fieldName(parameter));
            if (!it.hasNext()) {
                return sb.toString();
            }
            sb.append(", ");
        }
    }

    // FIXME: YANGTOOLS-1621: hide this method and then inline itno classBody(): there is a number of invariants we can
    //                        propagate: asFinal == false, clazz == this.javaType(), hence importedName(), etc.
    static void appendEquality(final BlockBuilder bb, final GeneratedClass clazz,
            final List<GeneratedProperty> props, final boolean asFinal) {
        final int size = props.size();
        if (size == 0) {
            throw new VerifyException("empty properties in " + clazz.name());
        }
        final var declInfix = asFinal ? " final " : " ";

        appendHashCode(bb.nl(), clazz, props, size, declInfix);
        appendEquals(bb.nl(), clazz, props, declInfix);
        appendToString(bb.nl(), clazz, props, size, declInfix);
    }

    private static void appendHashCode(final BlockBuilder bb, final GeneratedClass clazz,
            final List<GeneratedProperty> props, final int size, final String declInfix) {
        bb
            .at().eol(clazz.getReferenceString(OVERRIDE))
            .str("public").str(declInfix).str("int hashCode()").oB();

        switch (size) {
            case 1 -> {
                bb.str("return ");
                final var prop = props.getFirst();
                if (PRIMITIVE_BOOLEAN.equals(prop.getReturnType())) {
                    bb.str(clazz.getReferenceString(BOOLEAN)).str(".hashCode(");
                } else {
                    bb.str(clazz.getReferenceString(CODEHELPERS)).str(".wrapperHashCode(");
                }
                bb.str(fieldName(prop)).eol(");");
            }
            default -> {
                bb
                    .eol("final int prime = 31;")
                    .eol("int result = 1;");
                for (var property : props) {
                    final var type = property.getReturnType();
                    final var receiver = type.equals(PRIMITIVE_BOOLEAN)
                        // FIXME: unified perhaps?
                        ? clazz.getReferenceString(BOOLEAN) : importedUtilClass(clazz, type);

                    bb.str("result = prime * result + ").str(receiver).str(".hashCode(").str(fieldName(property))
                    .eol(");");
                }
                bb.eol("return result;");
            }
        }

        bb.cB();
    }

    private static void appendEquals(final BlockBuilder bb, final GeneratedClass clazz,
            final List<GeneratedProperty> props, final String declInfix) {
        // FIXME: use selfRef()
        final var selfRef = clazz.name().simpleName();

        bb
            .at().eol(clazz.getReferenceString(OVERRIDE))
            .str("public").str(declInfix).str("boolean equals(").str(clazz.getReferenceString(OBJECT)).str(" obj)").oB()
                .str("return this == obj || obj instanceof ").str(selfRef).str(" other");

        for (var prop : props) {
            bb.nl().str("    && ");

            final var fieldName = fieldName(prop);
            final var type = prop.getReturnType();
            if (type.equals(PRIMITIVE_BOOLEAN)) {
                bb.str(fieldName).str(" == other.").str(fieldName);
            } else {
                bb.str(importedUtilClass(clazz, type)).str(".equals(").str(fieldName).str(", other.").str(fieldName)
                .str(")");
            }
        }
        bb
            .eS()
            .cB();
    }

    private static void appendToString(final BlockBuilder bb, final GeneratedClass clazz,
            final List<GeneratedProperty> props, final int size, final String declInfix) {
        // FIXME: use selfRef
        final var selfRef = clazz.getReferenceString(clazz.name());

        bb
            .at().eol(clazz.getReferenceString(OVERRIDE))
            .str("public").str(declInfix).str(clazz.getReferenceString(STRING)).str(" toString()").oB()
                .str("return ").str(clazz.getReferenceString(CODEHELPERS));
        switch (size) {
            case 1 -> appendTS1(bb, selfRef, props.iterator().next());
            default -> appendTSN(bb, selfRef, props);
        }
        bb.cB();
    }

    private static void appendTS1(final BlockBuilder bb, final String selfRef, final GeneratedProperty prop) {
        verifyNotBit(prop);

        final var name = prop.getName();
        if (name.equals("value")) {
            // Special case equivalent to ScalarTypeObject.toString()
            bb.str(".stoTS(").str(selfRef).str(".class, ");
        } else {
            bb.str(".jcTS1(").str(selfRef).str(".class, ").jStr(prop.getName()).str(", ");
        }
        bb.str(fieldName(prop)).eol(");");
    }

    private static void appendTSN(final BlockBuilder bb, final String selfRef, final List<GeneratedProperty> props) {
        bb.str(".jcTSB(").str(selfRef).eol(".class)");
        for (var prop : props) {
            verifyNotBit(prop);
            bb.ind(".prop(").jStr(prop.getName()).str(", ").str(fieldName(prop)).eol(")");
        }
        bb.ind(".build();").newLine();
    }

    private static String importedUtilClass(final GeneratedClass clazz, final Type type) {
        return clazz.getReferenceString(type.isArray() ? JU_ARRAYS : JU_OBJECTS);
    }

    // FIXME: YANGTOOLS-1794: remove this method
    private static void verifyNotBit(final GeneratedProperty prop) {
        if (PRIMITIVE_BOOLEAN.equals(prop.getReturnType())) {
            throw new VerifyException("unexpected boolean in " + prop);
        }
    }
}
