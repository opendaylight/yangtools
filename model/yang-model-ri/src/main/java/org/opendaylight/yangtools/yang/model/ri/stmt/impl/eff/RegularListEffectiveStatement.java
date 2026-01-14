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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ElementCountMatcher;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;

public final class RegularListEffectiveStatement extends AbstractListEffectiveStatement {
    private final ElementCountMatcher elementCountMatcher;
    private final @NonNull QName argument;

    public RegularListEffectiveStatement(final ListStatement declared, final QName argument, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final ImmutableList<QName> keyDefinition, final ElementCountMatcher elementCountMatcher) {
        super(declared, flags, substatements, keyDefinition);
        this.argument = requireNonNull(argument);
        this.elementCountMatcher = elementCountMatcher;
    }

    public RegularListEffectiveStatement(final RegularListEffectiveStatement originalEffective, final QName argument,
            final int flags) {
        super(originalEffective, flags);
        this.argument = requireNonNull(argument);
        elementCountMatcher = originalEffective.elementCountMatcher;
    }

    public RegularListEffectiveStatement(final EmptyListEffectiveStatement originalEffective, final QName argument,
            final int flags) {
        super(originalEffective, flags);
        this.argument = requireNonNull(argument);
        elementCountMatcher = null;
    }

    @Override
    public QName argument() {
        return argument;
    }

    @Override
    public ElementCountMatcher elementCountMatcher() {
        return elementCountMatcher;
    }
}
