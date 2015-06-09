/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.TypeDefEffectiveStatementImpl;

public class TypedefStatementImpl extends AbstractDeclaredStatement<QName> implements TypedefStatement {

    protected TypedefStatementImpl(StmtContext<QName, TypedefStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<QName, TypedefStatement, EffectiveStatement<QName, TypedefStatement>> {

        public Definition() {
            super(Rfc6020Mapping.TYPEDEF);
        }

        @Override
        public QName parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public TypedefStatement createDeclared(StmtContext<QName, TypedefStatement, ?> ctx) {
            return new TypedefStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, TypedefStatement> createEffective(
                StmtContext<QName, TypedefStatement, EffectiveStatement<QName, TypedefStatement>> ctx) {
            return new TypeDefEffectiveStatementImpl(ctx);
        }

        @Override
        public void onStatementDefinitionDeclared(
                StmtContext.Mutable<QName, TypedefStatement, EffectiveStatement<QName, TypedefStatement>> stmt)
                throws SourceException {
            if (stmt != null && stmt.getParentContext() != null) {
                if (stmt.getParentContext().getFromNamespace(TypeNamespace.class, stmt.getStatementArgument()) != null) {
                    throw new IllegalArgumentException(String.format("Duplicate name for typedef %s",
                            stmt.getStatementArgument()));
                }

                stmt.getParentContext().addContext(TypeNamespace.class, stmt.getStatementArgument(), stmt);
            }
        }
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

    @Override
    public TypeStatement getType() {
        return firstDeclared(TypeStatement.class);
    }

    @Override
    public UnitsStatement getUnits() {
        return firstDeclared(UnitsStatement.class);
    }

    @Override
    public QName getName() {
        return argument();
    }

}
