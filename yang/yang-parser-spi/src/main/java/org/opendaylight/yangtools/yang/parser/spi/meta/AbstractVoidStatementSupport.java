/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

@Beta
public abstract class AbstractVoidStatementSupport<D extends DeclaredStatement<Void>,
        E extends EffectiveStatement<Void, D>> extends AbstractStatementSupport<Void, D, E> {
    protected AbstractVoidStatementSupport(final StatementDefinition publicDefinition,
            final StatementPolicy<Void, D> policy) {
        super(publicDefinition, policy);
    }

    @Override
    public final Void parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return null;
    }
}
