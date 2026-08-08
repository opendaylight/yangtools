/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.NONNULL;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.QNAMEOF_METHOD_NAME;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.yangModuleInfoOf;
import static org.opendaylight.yangtools.binding.contract.Naming.QNAME_STATIC_FIELD_NAME;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * A {@link BlockFragment} appending {@link #QNAME_STATIC_FIELD_NAME}.
 */
@NonNullByDefault
abstract sealed class QNameConstant implements BlockFragment {
    static final class InClass extends QNameConstant {
        InClass(final ArchetypeTemplate<?> template, final QName value) {
            super(template, value, "class.");
        }

        @Override
        BlockBuilder appendModifiers(final BlockBuilder bb) {
            return bb.str("public static final ");
        }
    }

    static final class InInterface extends QNameConstant {
        InInterface(final ArchetypeTemplate<?> template, final QName qname) {
            super(template, qname, "interface.");
        }

        @Override
        BlockBuilder appendModifiers(final BlockBuilder bb) {
            return bb;
        }
    }

    private static final TypeName QNAME = TypeName.ofClass(QName.class);

    private final ArchetypeTemplate<?> template;
    private final QName value;
    private final String tail;

    private QNameConstant(final ArchetypeTemplate<?> template, final QName value, final String tail) {
        this.template = requireNonNull(template);
        this.value = requireNonNull(value);
        this.tail = requireNonNull(tail);
    }

    @Override
    public void appendTo(final BlockBuilder bb) {
        final var module = template.root.statement().localQNameModule();
        verify(module.equals(value.getModule()));

        bb
            .eol("/**")
            .str(" * The YANG identifier of the {@code ")
                .str(template.archetype.statement().statementDefinition().simpleName())
                .str("} statement represented by this ").eol(tail)
            .eol(" */");
        appendModifiers(bb).at().str(template.importedName(NONNULL)).sp().str(template.importedName(QNAME))
            .str(" " + QNAME_STATIC_FIELD_NAME + " = ").str(template.importedName(yangModuleInfoOf(module)))
            .str("." + QNAMEOF_METHOD_NAME + "(").jStr(value.getLocalName()).eol(");");
    }

    abstract BlockBuilder appendModifiers(BlockBuilder bb);
}
