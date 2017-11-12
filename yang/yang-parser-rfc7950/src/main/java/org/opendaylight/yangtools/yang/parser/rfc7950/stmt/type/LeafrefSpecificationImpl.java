/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.stmt.PathStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.LeafrefSpecification;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class LeafrefSpecificationImpl extends AbstractDeclaredStatement<String> implements LeafrefSpecification {
    LeafrefSpecificationImpl(final StmtContext<String, LeafrefSpecification, ?> context) {
        super(context);
    }

    @Nonnull
    @Override
    public String getName() {
        return argument();
    }

    @Nonnull
    @Override
    public PathStatement getPath() {
        return firstDeclared(PathStatement.class);
    }

    @Nullable
    @Override
    public RequireInstanceStatement getRequireInstance() {
        return firstDeclared(RequireInstanceStatement.class);
    }

}
