/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import org.opendaylight.yangtools.rfc6643.model.api.AliasStatement;
import org.opendaylight.yangtools.rfc6643.model.api.OidStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class AliasStatementImpl extends AbstractDeclaredStatement<String> implements AliasStatement {
    AliasStatementImpl(final StmtContext<String, AliasStatement, ?> context) {
        super(context);
    }

    @Override
    public OidStatement getOidStatement() {
        return findFirstDeclaredSubstatement(OidStatement.class).get();
    }
}
