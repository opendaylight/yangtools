/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RefineEffectiveStatementImpl;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import javax.annotation.Nullable;

public class RefineStatementImpl extends AbstractDeclaredStatement<SchemaNodeIdentifier> implements RefineStatement {

    protected RefineStatementImpl(
            StmtContext<SchemaNodeIdentifier, RefineStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<SchemaNodeIdentifier, RefineStatement, EffectiveStatement<SchemaNodeIdentifier, RefineStatement>> {

        public Definition() {
            super(Rfc6020Mapping.REFINE);
        }

        @Override
        public SchemaNodeIdentifier parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            return SchemaNodeIdentifier.create(Utils.parseXPath(ctx, value), Utils.isXPathAbsolute(ctx, value));
        }

        @Override
        public RefineStatement createDeclared(StmtContext<SchemaNodeIdentifier, RefineStatement, ?> ctx) {
            return new RefineStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<SchemaNodeIdentifier, RefineStatement> createEffective(StmtContext<SchemaNodeIdentifier, RefineStatement, EffectiveStatement<SchemaNodeIdentifier, RefineStatement>> ctx) {
            return new RefineEffectiveStatementImpl(ctx);
        }
    }

    @Override
    public String getTargetNode() {
        return rawArgument();
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
}
