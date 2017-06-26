/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.TypeDefEffectiveStatementImpl;

public class TypedefStatementImpl extends AbstractDeclaredStatement<QName> implements TypedefStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .TYPEDEF)
            .addOptional(YangStmtMapping.DEFAULT)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addMandatory(YangStmtMapping.TYPE)
            .addOptional(YangStmtMapping.UNITS)
            .build();

    protected TypedefStatementImpl(final StmtContext<QName, TypedefStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractQNameStatementSupport<TypedefStatement, EffectiveStatement<QName, TypedefStatement>> {

        public Definition() {
            super(YangStmtMapping.TYPEDEF);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return StmtContextUtils.qnameFromArgument(ctx, value);
        }

        @Override
        public TypedefStatement createDeclared(final StmtContext<QName, TypedefStatement, ?> ctx) {
            return new TypedefStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, TypedefStatement> createEffective(
                final StmtContext<QName, TypedefStatement, EffectiveStatement<QName, TypedefStatement>> ctx) {
            return new TypeDefEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final StmtContext.Mutable<QName, TypedefStatement,
                EffectiveStatement<QName, TypedefStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);

            if (stmt != null && stmt.getParentContext() != null) {
                final StmtContext<?, TypedefStatement, TypedefEffectiveStatement> existing = stmt.getParentContext()
                        .getFromNamespace(TypeNamespace.class, stmt.getStatementArgument());
                SourceException.throwIf(existing != null, stmt.getStatementSourceReference(),
                        "Duplicate name for typedef %s", stmt.getStatementArgument());

                stmt.getParentContext().addContext(TypeNamespace.class, stmt.getStatementArgument(), stmt);
            }
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
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
