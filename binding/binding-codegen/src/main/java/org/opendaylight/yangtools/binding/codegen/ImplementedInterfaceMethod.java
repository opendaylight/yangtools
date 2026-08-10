/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.CLASS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.OVERRIDE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BindingContract;

/**
 * A {@link BlockFragment} emitting a default method implementing {@link BindingContract#implementedInterface()}.
 */
// FIXME: YANGTOOLS-1808: use selfRef() and make this a simple record
@NonNullByDefault
abstract sealed class ImplementedInterfaceMethod implements BlockFragment {
    static final class Canonical extends ImplementedInterfaceMethod {
        Canonical(final ArchetypeTemplate<?> template) {
            super(template, template.archetype.canonicalName());
        }
    }

    static final class Simple extends ImplementedInterfaceMethod {
        Simple(final ArchetypeTemplate<?> template) {
            super(template, template.archetype.simpleName());
        }
    }

    private final ArchetypeTemplate<?> template;
    private final String selfRef;

    private ImplementedInterfaceMethod(final ArchetypeTemplate<?> template, final String selfRef) {
        this.template = requireNonNull(template);
        this.selfRef = requireNonNull(selfRef);
    }

    @Override
    public final void appendTo(final BlockBuilder bb) {
        bb
            .at().eol(template.importedName(OVERRIDE))
            .str("default ").gen(template.importedName(CLASS), selfRef).str(" implementedInterface()").oB()
                .str("return ").str(selfRef).eol(".class;")
            .cB();
    }
}
