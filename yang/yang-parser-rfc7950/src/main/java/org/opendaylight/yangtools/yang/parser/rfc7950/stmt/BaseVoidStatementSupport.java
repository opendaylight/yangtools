/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@Beta
public abstract class BaseVoidStatementSupport<D extends DeclaredStatement<Void>, E extends EffectiveStatement<Void, D>>
        extends BaseStatementSupport<Void, D, E> {
    protected BaseVoidStatementSupport(final StatementDefinition publicDefinition) {
        super(publicDefinition, CopyPolicy.CONTEXT_INDEPENDENT);
    }

    @Override
    public final Void parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return null;
    }
}
