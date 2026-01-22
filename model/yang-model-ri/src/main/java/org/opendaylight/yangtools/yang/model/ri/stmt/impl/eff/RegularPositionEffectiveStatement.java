/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;

public final class RegularPositionEffectiveStatement extends WithSubstatements<Uint32, @NonNull PositionStatement>
        implements PositionEffectiveStatement {
    public RegularPositionEffectiveStatement(final @NonNull PositionStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, substatements);
    }
}
