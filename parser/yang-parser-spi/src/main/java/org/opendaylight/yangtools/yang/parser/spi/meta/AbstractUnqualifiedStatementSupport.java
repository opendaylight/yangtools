/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;

/**
 * Specialization of {@link AbstractStatementSupport} for Unqualified statement arguments. Note this (mostly) implies
 * context-independence.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public abstract class AbstractUnqualifiedStatementSupport<D extends DeclaredStatement<Unqualified>,
        E extends EffectiveStatement<Unqualified, D>> extends AbstractStatementSupport<Unqualified, D, E> {
    protected AbstractUnqualifiedStatementSupport(final StatementDefinition publicDefinition,
            final StatementPolicy<Unqualified, D> policy, final YangParserConfiguration config,
            final @Nullable SubstatementValidator validator) {
        super(publicDefinition, policy, config, validator);
    }

    @Override
    public final Unqualified parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return Unqualified.of(value).intern();
        } catch (IllegalArgumentException e) {
            throw ctx.newSourceException("Invalid argument value '" + value + "'", e);
        }
    }
}
