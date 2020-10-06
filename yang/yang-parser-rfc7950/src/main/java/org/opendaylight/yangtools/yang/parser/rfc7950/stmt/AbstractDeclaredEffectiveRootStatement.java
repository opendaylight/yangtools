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
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BodyDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LinkageDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MetaDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionAwareDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@Beta
public abstract class AbstractDeclaredEffectiveRootStatement<D extends DeclaredStatement<UnqualifiedQName>>
        extends WithSubstatements<UnqualifiedQName> implements LinkageDeclaredStatement,
                MetaDeclaredStatement<UnqualifiedQName>, RevisionAwareDeclaredStatement, BodyDeclaredStatement {
    protected AbstractDeclaredEffectiveRootStatement(final StmtContext<UnqualifiedQName, ?, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(ctx, substatements);
    }
}
