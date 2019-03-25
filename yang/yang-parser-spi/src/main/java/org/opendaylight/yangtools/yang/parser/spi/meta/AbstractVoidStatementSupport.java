/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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

/**
 * Class providing necessary support for processing a YANG statements which does not have an argument. In addition
 * to functions of {@link AbstractStatementSupport}, it provide the argument value.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public abstract class AbstractVoidStatementSupport<D extends DeclaredStatement<Void>,
        E extends EffectiveStatement<Void, D>> extends AbstractStatementSupport<Void, D, E> {

    protected AbstractVoidStatementSupport(final StatementDefinition publicDefinition) {
        super(publicDefinition);
    }

    @Override
    public final Void parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return null;
    }
}
