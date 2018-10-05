/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BodyDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LinkageDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MetaDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionAwareDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement.WithArgument;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class AbstractRootStatement<T extends DeclaredStatement<String>>
        extends WithArgument<String> implements LinkageDeclaredStatement, MetaDeclaredStatement<String>,
        RevisionAwareDeclaredStatement, BodyDeclaredStatement {

    protected AbstractRootStatement(final StmtContext<String, T,?> context) {
        super(context);
    }
}
