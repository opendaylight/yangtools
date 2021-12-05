/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;

public final class RegularListEffectiveStatement extends AbstractListEffectiveStatement {
    private final ElementCountConstraint elementCountConstraint;
    private final @NonNull QName argument;

    public RegularListEffectiveStatement(final ListStatement declared, final QName argument, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final ImmutableList<QName> keyDefinition, final ElementCountConstraint elementCountConstraint) {
        super(declared, flags, substatements, keyDefinition);
        this.argument = requireNonNull(argument);
        this.elementCountConstraint = elementCountConstraint;
    }

    public RegularListEffectiveStatement(final RegularListEffectiveStatement originalEffective, final QName argument,
            final int flags) {
        super(originalEffective, flags);
        this.argument = requireNonNull(argument);
        elementCountConstraint = originalEffective.elementCountConstraint;
    }

    public RegularListEffectiveStatement(final EmptyListEffectiveStatement originalEffective, final QName argument,
            final int flags) {
        super(originalEffective, flags);
        this.argument = requireNonNull(argument);
        elementCountConstraint = null;
    }

    @Override
    public QName argument() {
        return argument;
    }

    @Override
    public Optional<ElementCountConstraint> getElementCountConstraint() {
        return Optional.ofNullable(elementCountConstraint);
    }
}
