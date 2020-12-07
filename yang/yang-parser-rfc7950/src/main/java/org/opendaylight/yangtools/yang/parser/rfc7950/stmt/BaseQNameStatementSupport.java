/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Specialization of {@link BaseStatementSupport} for QName statement arguments.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public abstract class BaseQNameStatementSupport<D extends DeclaredStatement<QName>,
        E extends EffectiveStatement<QName, D>> extends BaseStatementSupport<QName, D, E> {
    protected BaseQNameStatementSupport(final StatementDefinition publicDefinition,
            final StatementPolicy<QName, D> policy) {
        super(publicDefinition, policy);
    }

    @Deprecated
    protected BaseQNameStatementSupport(final StatementDefinition publicDefinition, final CopyPolicy copyPolicy) {
        super(publicDefinition, copyPolicy);
    }

    @Override
    public QName adaptArgumentValue(final StmtContext<QName, D, E> ctx, final QNameModule targetModule) {
        return ctx.getArgument().bindTo(targetModule).intern();
    }

    protected static final int historyAndStatusFlags(final CopyHistory history,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
                .setHistory(history)
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .toFlags();
    }
}
