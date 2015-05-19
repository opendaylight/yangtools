/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.IdentityRefSpecificationEffectiveStatementImpl;

import javax.annotation.Nonnull;
import java.util.Map;

public class IdentityRefSpecificationImpl extends AbstractDeclaredStatement<String> implements TypeStatement.IdentityRefSpecification {

    protected IdentityRefSpecificationImpl(
            StmtContext<String, TypeStatement.IdentityRefSpecification, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, TypeStatement.IdentityRefSpecification, EffectiveStatement<String, TypeStatement.IdentityRefSpecification>> {

        public Definition() {
            super(Rfc6020Mapping.TYPE);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value)
                throws SourceException {
            return value;
        }

        @Override
        public TypeStatement.IdentityRefSpecification createDeclared(
                StmtContext<String, TypeStatement.IdentityRefSpecification, ?> ctx) {
            return new IdentityRefSpecificationImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, TypeStatement.IdentityRefSpecification> createEffective(
                StmtContext<String, TypeStatement.IdentityRefSpecification, EffectiveStatement<String, TypeStatement.IdentityRefSpecification>> ctx) {
            return new IdentityRefSpecificationEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(StmtContext.Mutable<String, IdentityRefSpecification, EffectiveStatement<String, IdentityRefSpecification>> stmt) throws SourceException {
            //FIXME:
//            StatementContextBase<?, ?, ?> stmtCtx = (StatementContextBase<?, ?, ?>) stmt.getFromNamespace(IdentityNamespace.class, Utils.qNameFromArgument(StmtContextUtils.findFirstDeclaredSubstatement(stmt, BaseStatement.class), (String) StmtContextUtils.findFirstDeclaredSubstatement(stmt, BaseStatement.class).getStatementArgument()));
//            Preconditions.checkArgument(stmtCtx != null, "Identity '%s' doesn't exist in given scope (module, imported submodules)", stmt.getStatementArgument());
        }
    }

    @Override
    public String getName() {
        return argument();
    }

    @Nonnull
    @Override
    public BaseStatement getBase() {
        return firstDeclared(BaseStatement.class);
    }

}
