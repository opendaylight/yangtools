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
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;

@Beta
public abstract class AbstractEmptyStatementSupport<D extends DeclaredStatement<Empty>,
        E extends EffectiveStatement<Empty, D>> extends AbstractStatementSupport<Empty, D, E> {
    protected AbstractEmptyStatementSupport(final StatementDefinition publicDefinition,
            final StatementPolicy<Empty, D> policy, final YangParserConfiguration config,
            final @Nullable SubstatementValidator validator) {
        super(publicDefinition, policy, config, validator);
    }

    @Override
    public final Empty parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        ctx.inferNull(value, "Unexpected argument value %s", value);
        return Empty.value();
    }
}
