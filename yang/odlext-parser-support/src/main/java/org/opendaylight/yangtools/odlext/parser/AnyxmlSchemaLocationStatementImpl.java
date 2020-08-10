/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.odlext.model.api.AnyxmlSchemaLocationStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class AnyxmlSchemaLocationStatementImpl extends WithSubstatements<SchemaNodeIdentifier>
        implements AnyxmlSchemaLocationStatement {
    AnyxmlSchemaLocationStatementImpl(
            final StmtContext<SchemaNodeIdentifier, AnyxmlSchemaLocationStatement, ?> context,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(context, substatements);
    }
}
