/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;

public final class EmptyContainerEffectiveStatement extends AbstractContainerEffectiveStatement {
    public EmptyContainerEffectiveStatement(final ContainerStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final int flags) {
        super(declared, substatements, flags);
    }

    public EmptyContainerEffectiveStatement(final AbstractContainerEffectiveStatement original, final int flags) {
        super(original, flags);
    }

    @Override
    public QName argument() {
        return declared().argument();
    }
}
