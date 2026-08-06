/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.codegen.TypeNames.CODEHELPERS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.JU_ARRAYS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.JU_OBJECTS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.OBJECT;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.OVERRIDE;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.STRING;

import com.google.common.base.VerifyException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility method for code sharing between {@link KeyTemplate} and {@link UnionTypeObjectTemplate}.
 */
// FIXME: inline this class back into KeyTemplate.classBody()
@NonNullByDefault
abstract class ObjectEquality<T> {
    private final boolean asFinal;

    ObjectEquality(final boolean asFinal) {
        this.asFinal = asFinal;
    }

    abstract String fieldName(T obj);

    abstract String propName(T obj);

    abstract boolean isBinaryType(T obj);

    final void append(final BlockBuilder bb, final GeneratedClass clazz, final List<T> props) {
        final int size = props.size();
        if (size == 0) {
            throw new VerifyException("empty properties in " + clazz.name());
        }
        final var declInfix = asFinal ? " final " : " ";

        appendHashCode(bb.nl(), clazz, props, size, declInfix);
        appendEquals(bb.nl(), clazz, props, declInfix);
        appendToString(bb.nl(), clazz, props, size, declInfix);
    }

    private void appendHashCode(final BlockBuilder bb, final GeneratedClass clazz, final List<T> props, final int size,
            final String declInfix) {
        bb
            .at().eol(clazz.getReferenceString(OVERRIDE))
            .str("public").str(declInfix).str("int hashCode()").oB();

        switch (size) {
            case 1 -> {
                bb.str("return ");
                final var prop = props.getFirst();
                bb.str(clazz.getReferenceString(CODEHELPERS)).str(".wrapperHashCode(").str(fieldName(prop)).eol(");");
            }
            default -> {
                bb
                    .eol("final int prime = 31;")
                    .eol("int result = 1;");
                for (var prop : props) {
                    bb
                        .str("result = prime * result + ").str(importedUtilClass(clazz, prop)).str(".hashCode(")
                            .str(fieldName(prop)).eol(");");
                }
                bb.eol("return result;");
            }
        }

        bb.cB();
    }

    private void appendEquals(final BlockBuilder bb, final GeneratedClass clazz, final List<T> props,
            final String declInfix) {
        // FIXME: use selfRef()
        final var selfRef = clazz.name().simpleName();

        bb
            .at().eol(clazz.getReferenceString(OVERRIDE))
            .str("public").str(declInfix).str("boolean equals(").str(clazz.getReferenceString(OBJECT)).str(" obj)").oB()
                .str("return this == obj || obj instanceof ").str(selfRef).str(" other");

        for (var prop : props) {
            final var fieldName = fieldName(prop);
            bb
                .nl()
                .str("    && ").str(importedUtilClass(clazz, prop)).str(".equals(").str(fieldName).str(", other.")
                    .str(fieldName).str(")");
        }
        bb
            .eS()
            .cB();
    }

    private void appendToString(final BlockBuilder bb, final GeneratedClass clazz, final List<T> props, final int size,
            final String declInfix) {
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

    private void appendTS1(final BlockBuilder bb, final String selfRef, final T prop) {
        final var name = propName(prop);
        if (name.equals("value")) {
            // Special case equivalent to ScalarTypeObject.toString()
            bb.str(".stoTS(").str(selfRef).str(".class, ");
        } else {
            bb.str(".jcTS1(").str(selfRef).str(".class, ").jStr(name).str(", ");
        }
        bb.str(fieldName(prop)).eol(");");
    }

    private void appendTSN(final BlockBuilder bb, final String selfRef, final List<T> props) {
        bb.str(".jcTSB(").str(selfRef).eol(".class)");
        for (var prop : props) {
            bb.ind(".prop(").jStr(propName(prop)).str(", ").str(fieldName(prop)).eol(")");
        }
        bb.ind(".build();").newLine();
    }

    private String importedUtilClass(final GeneratedClass clazz, final T prop) {
        return clazz.getReferenceString(isBinaryType(prop) ? JU_ARRAYS : JU_OBJECTS);
    }
}
