/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRanges;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.ConstraintMetaDefinitionMixin;

public final class RegularRangeEffectiveStatement extends WithSubstatements<ValueRanges, @NonNull RangeStatement>
        implements RangeEffectiveStatement, ConstraintMetaDefinitionMixin<ValueRanges, @NonNull RangeStatement> {
    public RegularRangeEffectiveStatement(final @NonNull RangeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, substatements);
    }
}
