/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BodyDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LinkageDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MetaDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionAwareDeclaredStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithArgument.WithSubstatements;

abstract class AbstractDeclaredEffectiveRootStatement<D extends DeclaredStatement>
        extends WithSubstatements<Unqualified> implements LinkageDeclaredStatement,
                MetaDeclaredStatement, RevisionAwareDeclaredStatement, BodyDeclaredStatement {
    protected AbstractDeclaredEffectiveRootStatement(final String rawArgument, final Unqualified argument,
            final ImmutableList<? extends DeclaredStatement> substatements) {
        super(rawArgument, argument, substatements);
    }
}
