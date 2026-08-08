/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.CODEHELPERS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.JU_ARRAYS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.JU_OBJECTS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.OVERRIDE;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.STRING;

import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.lib.JavaDataContainer;
import org.opendaylight.yangtools.binding.model.TypeName;

/**
 * A {@link BlockFragment} generating {@link JavaDataContainer} method implementations.
 */
@NonNullByDefault
record JavaDataContainerMethods(
        GeneratedClass javaType,
        DataContainerGetters getters,
        boolean augmentable) implements BlockFragment {
    JavaDataContainerMethods {
        requireNonNull(javaType);
        requireNonNull(getters);
    }

    @Override
    public void appendTo(final BlockBuilder bb) {
        javaHC(bb);
        bb.newLine();
        javaEQ(bb);
        bb.newLine();
        javaTS(bb);
    }

    private String importedName(final TypeName type) {
        return javaType.getReferenceString(type);
    }

    private void javaHC(final BlockBuilder bbb) {
        bbb
            .at().eol(importedName(OVERRIDE))
            // FIXME: do not use jBlock, it is not really useful here
            .str("default int javaHC()").jBlock(bb -> {
                final var methods = getters.allMethods().sorted().toList();
                switch (methods.size()) {
                    case 0 -> {
                        if (augmentable) {
                            bb.str("return ").str(importedName(CODEHELPERS)).eol(".jcHC0(this);");
                        } else {
                            bb.eol("return 1;");
                        }
                    }
                    case 1 -> {
                        final var getter = methods.getFirst();
                        bb.str("return ").str(importedName(CODEHELPERS)).str(".jcHC1(");
                        if (augmentable) {
                            bb.str("this, ");
                        }
                        bb.str(getter.name()).eol("());");
                    }
                    // TODO: consider specializing for N=2 (single line) for the cost of 8 new methods in CodeHelpers
                    default -> javaHC(bb, methods);
                }
            }).nl();
    }

    private void javaHC(final BlockBuilder bb, final List<GetterShape> methods) {
        // determine the composition of getters: 'type binary' fields map to byte[] and therefore have to be hashed
        // via Arrays.hashCode(), not Objects.hashCode()
        final int size = methods.size();
        final boolean[] isBinary = new boolean[size];
        int cnt = 0;
        int binaryCount = 0;
        for (var method : methods) {
            final var tmp = method.isBinary();
            if (tmp) {
                binaryCount++;
            }
            isBinary[cnt++] = tmp;
        }

        // either all are byte[] or none are: we can use CodeHelpers.jcHCN()
        final boolean useN = binaryCount == 0 || binaryCount == size;

        bb.str("return ").str(importedName(CODEHELPERS)).str(useN ? ".jcHCN(" : ".jcHC(");
        if (augmentable) {
            bb.eol("this,");
        } else {
            bb.newLine();
        }

        final var it = methods.iterator();
        if (useN) {
            appendCodeHelpersHCN(bb, it);
        } else {
            appendCodeHelpersHC(bb, it, isBinary);
        }
        bb.eol(");");
    }

    // all getters are the same: just pass them down to CodeHelpers
    private static void appendCodeHelpersHCN(final BlockBuilder bb, final Iterator<GetterShape> it) {
        while (true) {
            final var getter = it.next();
            bb.ind(getter.name()).str("()");
            if (!it.hasNext()) {
                break;
            }
            bb.eol(",");
        }
    }

    // we have at least one Object and one byte[] getter: compute their hashCode() ourselves
    private void appendCodeHelpersHC(final BlockBuilder bb, final Iterator<GetterShape> it, final boolean[] isBinary) {
        final var arrays = importedName(JU_ARRAYS);
        final var objects = importedName(JU_OBJECTS);

        int cnt = 0;
        while (true) {
            final var getter = it.next();
            bb.ind(isBinary[cnt++] ? arrays : objects).str(".hashCode(").str(getter.name()).str("())");
            if (!it.hasNext()) {
                break;
            }
            bb.eol(",");
        }
    }

    private void javaEQ(final BlockBuilder bbb) {
        bbb
            .at().eol(importedName(OVERRIDE))
            // FIXME: do not use jBlock, it is not useful
            // FIXME: selfref instead of canonicalName
            .str("default boolean javaEQ(").str(javaType.name().canonicalName()).str(" obj)").jBlock(bb -> {
                final var it = getters.allMethods().sorted(ByTypeMemberComparator.INSTANCE).iterator();
                if (!it.hasNext()) {
                    // single method
                    if (!augmentable) {
                        bb.str(importedName(JU_OBJECTS)).eol(".requireNonNull(obj);");
                        bb.eol("return true;");
                    } else {
                        bb.eol("return augmentations().equals(obj.augmentations());");
                    }
                    return;
                }

                appendEQ(bb.str("return "), it.next());
                while (it.hasNext()) {
                    appendEQ(bb.nl().ind("&& "), it.next());
                }
                if (augmentable) {
                    bb.nl().ind("&& augmentations().equals(obj.augmentations())");
                }
                bb.eS();
            }).nl();
    }

    private void appendEQ(final BlockBuilder bb, final GetterShape getter) {
        final var name = getter.name();
        bb.str(importedName(getter.isBinary() ? JU_ARRAYS : JU_OBJECTS)).str(".equals(").str(name).str("(), obj.")
            .str(name).str("())");
    }

    private void javaTS(final BlockBuilder bbb) {
        bbb
            .at().eol(importedName(OVERRIDE))
            // FIXME: do not use jBlock, it is not useful
            .str("default ").str(importedName(STRING)).str(" javaTS()").jBlock(bb -> {
                bb.str("return ").str(importedName(CODEHELPERS));

                final var it = getters.allMethods().sorted().iterator();
                if (!it.hasNext()) {
                    // no methods
                    appendAllocateTS(bb.str(".jcTS0(")).eol(");");
                    return;
                }

                final var first = it.next();
                if (!it.hasNext()) {
                    // one method
                    appendAllocateTS(bb.str(".jcTS1(")).str(", ").jStr(first.propName()).str(", ").str(first.name())
                        .eol("());");
                    return;
                }

                // more methods
                appendTS(appendAllocateTS(bb.str(".jcTSB(")).eol(")"), first);
                do {
                    appendTS(bb, it.next());
                } while (it.hasNext());
                bb.ind().eol(".build();");
            }).nl();
    }

    private BlockBuilder appendAllocateTS(final BlockBuilder bb) {
        if (augmentable) {
            return bb.str("this");
        }
        // FIXME: use selfRef()
        return bb.str(javaType.name().canonicalName()).str(".class");
    }

    private static void appendTS(final BlockBuilder bb, final GetterShape getter) {
        bb.ind(".prop(").jStr(getter.propName()).str(", ").str(getter.name()).eol("())");
    }

}
