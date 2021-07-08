/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.list;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;

final class RegularListEffectiveStatement extends AbstractListEffectiveStatement {
    private final ElementCountConstraint elementCountConstraint;
    private final ListSchemaNode original;

    RegularListEffectiveStatement(final ListStatement declared, final QName argument, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final ImmutableList<QName> keyDefinition, final ElementCountConstraint elementCountConstraint,
            final ListSchemaNode original) {
        super(declared, argument, flags, substatements, keyDefinition);
        this.elementCountConstraint = elementCountConstraint;
        this.original = original;
    }

    RegularListEffectiveStatement(final RegularListEffectiveStatement originalEffective, final ListSchemaNode original,
            final QName argument, final int flags) {
        super(originalEffective, argument, flags);
        this.elementCountConstraint = originalEffective.elementCountConstraint;
        this.original = original;
    }

    RegularListEffectiveStatement(final EmptyListEffectiveStatement originalEffective, final ListSchemaNode original,
            final QName argument, final int flags) {
        super(originalEffective, argument, flags);
        this.elementCountConstraint = null;
        this.original = original;
    }

    @Override
    public Optional<ListSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public Optional<ElementCountConstraint> getElementCountConstraint() {
        return Optional.ofNullable(elementCountConstraint);
    }
}
