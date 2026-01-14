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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ElementCountMatcher;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;

abstract class AbstractNonEmptyLeafListEffectiveStatement extends AbstractLeafListEffectiveStatement {
    private final @Nullable ElementCountMatcher elementCountMatcher;
    private final @NonNull QName argument;

    AbstractNonEmptyLeafListEffectiveStatement(final LeafListStatement declared, final QName argument, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final ElementCountMatcher elementCountMatcher) {
        super(declared, flags, substatements);
        this.argument = requireNonNull(argument);
        this.elementCountMatcher = elementCountMatcher;
    }

    AbstractNonEmptyLeafListEffectiveStatement(final AbstractNonEmptyLeafListEffectiveStatement originalEffecive,
            final QName argument, final int flags) {
        super(originalEffecive, flags);
        this.argument = requireNonNull(argument);
        elementCountMatcher = originalEffecive.elementCountMatcher;
    }

    AbstractNonEmptyLeafListEffectiveStatement(final EmptyLeafListEffectiveStatement originalEffective,
            final QName argument, final int flags) {
        super(originalEffective, flags);
        this.argument = requireNonNull(argument);
        elementCountMatcher = null;
    }

    @Override
    public final QName argument() {
        return argument;
    }

    @Override
    public final ElementCountMatcher elementCountMatcher() {
        return elementCountMatcher;
    }
}
