/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviation;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class DeviationStatementImpl extends AbstractDeclaredStatement<SchemaNodeIdentifier>
        implements DeviationStatement {
    DeviationStatementImpl(final StmtContext<SchemaNodeIdentifier, DeviationStatement, ?> context) {
        super(context);
    }

    @Nonnull
    @Override
    public SchemaNodeIdentifier getTargetNode() {
        return argument();
    }

    @Nonnull
    @Override
    public Collection<? extends DeviateStatement> getDeviateStatements() {
        return allDeclared(DeviateStatement.class);
    }
}
