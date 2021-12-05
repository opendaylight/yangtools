/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;

public final class EmptyLeafListEffectiveStatement extends AbstractLeafListEffectiveStatement {
    public EmptyLeafListEffectiveStatement(final LeafListStatement declared, final QName argument, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, argument, flags, substatements);
    }

    public EmptyLeafListEffectiveStatement(final EmptyLeafListEffectiveStatement original, final QName argument,
            final int flags) {
        super(original, argument, flags);
    }

    @Override
    public Collection<Object> getDefaults() {
        return ImmutableSet.of();
    }

    @Override
    public Optional<ElementCountConstraint> getElementCountConstraint() {
        return Optional.empty();
    }
}
