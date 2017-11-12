/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.typedef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class TypedefStatementImpl extends AbstractDeclaredStatement<QName> implements TypedefStatement {
    TypedefStatementImpl(final StmtContext<QName, TypedefStatement, ?> context) {
        super(context);
    }

    @Nullable
    @Override
    public StatusStatement getStatus() {
        return firstDeclared(StatusStatement.class);
    }

    @Nullable
    @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Nullable
    @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }

    @Nonnull
    @Override
    public TypeStatement getType() {
        return firstDeclared(TypeStatement.class);
    }

    @Override
    public UnitsStatement getUnits() {
        return firstDeclared(UnitsStatement.class);
    }

    @Nonnull
    @Override
    public QName getName() {
        return argument();
    }

    @Nullable
    @Override
    public DefaultStatement getDefault() {
        return firstDeclared(DefaultStatement.class);
    }
}
