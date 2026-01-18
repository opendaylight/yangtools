/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;

/**
 * Specialization of {@link AbstractStatementSupport} for {@link Empty} statement arguments.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
public abstract class AbstractEmptyStatementSupport<D extends DeclaredStatement<Empty>,
        E extends EffectiveStatement<Empty, D>> extends AbstractStatementSupport<Empty, D, E> {
    protected AbstractEmptyStatementSupport(final StatementDefinition publicDefinition,
            final StatementPolicy<Empty, D> policy, final SubtreePolicy subtreePolicy,
            final YangParserConfiguration config, final @Nullable SubstatementValidator validator) {
        super(publicDefinition, policy, subtreePolicy, config, validator);
    }

    protected AbstractEmptyStatementSupport(final StatementDefinition publicDefinition,
            final StatementPolicy<Empty, D> policy, final YangParserConfiguration config,
            final @Nullable SubstatementValidator validator) {
        super(publicDefinition, policy, config, validator);
    }

    @Override
    public final Empty parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        if (value != null) {
            throw new InferenceException("Unexpected argument value " + value, ctx);
        }
        return Empty.value();
    }
}
