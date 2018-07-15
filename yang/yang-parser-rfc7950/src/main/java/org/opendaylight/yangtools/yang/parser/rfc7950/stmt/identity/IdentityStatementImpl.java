/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.identity;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class IdentityStatementImpl extends AbstractDeclaredStatement<QName> implements IdentityStatement {
    IdentityStatementImpl(final StmtContext<QName, IdentityStatement, ?> context) {
        super(context);
    }

    @Nonnull
    @Override
    public Collection<? extends IfFeatureStatement> getIfFeatures() {
        return allDeclared(IfFeatureStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends BaseStatement> getBases() {
        return allDeclared(BaseStatement.class);
    }

    @Nonnull
    @Override
    public QName getName() {
        return argument();
    }
}
