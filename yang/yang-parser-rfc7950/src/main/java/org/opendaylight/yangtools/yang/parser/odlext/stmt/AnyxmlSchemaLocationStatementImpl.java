/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.odlext.stmt;

import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class AnyxmlSchemaLocationStatementImpl extends AbstractDeclaredStatement<SchemaNodeIdentifier>
        implements AnyxmlSchemaLocationStatement {
    AnyxmlSchemaLocationStatementImpl(
            final StmtContext<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement, ?> context) {
        super(context);
    }

    @Override
    public SchemaNodeIdentifier getArgument() {
        return argument();
    }
}
