/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Class providing necessary support for processing a YANG statements which has a {@link QName} argument. In addition
 * to functions of {@link AbstractStatementSupport}, it takes care of adapting QNames across modules.
 *
 * @param <D>
 *            Declared Statement representation
 * @param <E>
 *            Effective Statement representation
 */
@Beta
public abstract class AbstractQNameStatementSupport<D extends DeclaredStatement<QName>,
        E extends EffectiveStatement<QName, D>> extends AbstractStatementSupport<QName, D, E> {

    protected AbstractQNameStatementSupport(final StatementDefinition publicDefinition) {
        super(publicDefinition);
    }

    @Override
    public QName adaptArgumentValue(final StmtContext<QName, D, E> ctx, final QNameModule targetModule) {
        return ctx.getFromNamespace(QNameCacheNamespace.class,
            QName.create(targetModule, ctx.getStatementArgument().getLocalName()));
    }
}
