/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;

/**
 * Specialization of {@link AbstractStatementSupport} for QName statement arguments.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
public abstract class AbstractQNameStatementSupport<D extends DeclaredStatement<QName>,
        E extends EffectiveStatement<QName, D>> extends AbstractStatementSupport<QName, D, E> {
    protected AbstractQNameStatementSupport(final StatementDefinition publicDefinition,
            final StatementPolicy<QName, D> policy, final YangParserConfiguration config,
            final @Nullable SubstatementValidator validator) {
        super(publicDefinition, policy, config, validator);
    }

    // Non-final to allow customization
    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return ctx.identifierBinding().parseIdentifierArg(ctx, value);
    }

    @Override
    public final QName adaptArgumentValue(final StmtContext<QName, D, E> ctx, final QNameModule targetModule) {
        return ctx.getArgument().bindTo(targetModule).intern();
    }
}
