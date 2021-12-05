/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;

abstract class AbstractNonEmptyLeafListEffectiveStatement extends AbstractLeafListEffectiveStatement {
    private final @Nullable ElementCountConstraint elementCountConstraint;

    AbstractNonEmptyLeafListEffectiveStatement(final LeafListStatement declared, final QName argument, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final ElementCountConstraint elementCountConstraint) {
        super(declared, argument, flags, substatements);
        this.elementCountConstraint = elementCountConstraint;
    }

    AbstractNonEmptyLeafListEffectiveStatement(final AbstractNonEmptyLeafListEffectiveStatement originalEffecive,
            final QName argument, final int flags) {
        super(originalEffecive, argument, flags);
        elementCountConstraint = originalEffecive.elementCountConstraint;
    }

    AbstractNonEmptyLeafListEffectiveStatement(final EmptyLeafListEffectiveStatement originalEffective,
            final QName argument, final int flags) {
        super(originalEffective, argument, flags);
        elementCountConstraint = null;
    }

    @Override
    public final Optional<ElementCountConstraint> getElementCountConstraint() {
        return Optional.ofNullable(elementCountConstraint);
    }
}
