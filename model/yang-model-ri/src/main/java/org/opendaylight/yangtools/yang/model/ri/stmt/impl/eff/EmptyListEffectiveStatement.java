/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.ElementCountMatcher;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;

public final class EmptyListEffectiveStatement extends AbstractListEffectiveStatement {
    public EmptyListEffectiveStatement(final ListStatement declared, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final ImmutableList<QName> keyDefinition) {
        super(declared, flags, substatements, keyDefinition);
    }

    @Override
    public QName argument() {
        return declared().argument();
    }

    @Override
    public ElementCountMatcher elementCountMatcher() {
        return null;
    }
}
