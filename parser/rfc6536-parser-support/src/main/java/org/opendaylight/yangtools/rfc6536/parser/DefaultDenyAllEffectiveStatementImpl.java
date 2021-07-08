/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllSchemaNode;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllStatement;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

final class DefaultDenyAllEffectiveStatementImpl extends UnknownEffectiveStatementBase<Empty, DefaultDenyAllStatement>
        implements DefaultDenyAllEffectiveStatement, DefaultDenyAllSchemaNode {
    private final @NonNull QName qname;

    DefaultDenyAllEffectiveStatementImpl(final Current<Empty, DefaultDenyAllStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt, substatements);
        qname = stmt.publicDefinition().getStatementName();
    }

    @Override
    public QName getQName() {
        return qname;
    }

    @Override
    public DefaultDenyAllEffectiveStatement asEffectiveStatement() {
        return this;
    }
}