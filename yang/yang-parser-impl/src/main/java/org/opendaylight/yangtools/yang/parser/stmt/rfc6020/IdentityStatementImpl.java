/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.IdentityEffectiveStatementImpl;

public class IdentityStatementImpl extends AbstractDeclaredStatement<QName>
        implements IdentityStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .IDENTITY)
            .addOptional(YangStmtMapping.BASE)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .build();

    protected IdentityStatementImpl(
            final StmtContext<QName, IdentityStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<QName, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> {

        public Definition() {
            super(YangStmtMapping.IDENTITY);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return StmtContextUtils.qnameFromArgument(ctx, value);
        }

        @Override
        public IdentityStatement createDeclared(
                final StmtContext<QName, IdentityStatement, ?> ctx) {
            return new IdentityStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, IdentityStatement> createEffective(
                final StmtContext<QName, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> ctx) {
            return new IdentityEffectiveStatementImpl(ctx);
        }

        @Override
        public void onStatementDefinitionDeclared(final StmtContext.Mutable<QName, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> stmt) {
            stmt.addToNs(IdentityNamespace.class, stmt.getStatementArgument(), stmt);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Override
    public StatusStatement getStatus() {
        return firstDeclared(StatusStatement.class);
    }

    @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends IfFeatureStatement> getIfFeatures() {
        return allDeclared(IfFeatureStatement.class);
    }

    @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }

    @Override
    public BaseStatement getBase() {
        return firstDeclared(BaseStatement.class);
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
