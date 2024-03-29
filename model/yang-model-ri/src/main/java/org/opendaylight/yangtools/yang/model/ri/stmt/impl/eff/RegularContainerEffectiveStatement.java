/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;

public final class RegularContainerEffectiveStatement extends AbstractContainerEffectiveStatement {
    private final @NonNull QName argument;

    public RegularContainerEffectiveStatement(final ContainerStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final QName argument,
            final int flags) {
        super(declared, substatements, flags);
        this.argument = requireNonNull(argument);
    }

    public RegularContainerEffectiveStatement(final AbstractContainerEffectiveStatement original, final QName argument,
            final int flags) {
        super(original, flags);
        this.argument = requireNonNull(argument);
    }

    @Override
    public QName argument() {
        return argument;
    }
}
