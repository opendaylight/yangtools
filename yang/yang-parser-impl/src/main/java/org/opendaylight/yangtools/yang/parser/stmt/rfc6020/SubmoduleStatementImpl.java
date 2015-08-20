/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.findFirstDeclaredSubstatement;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;

import java.net.URI;
import java.util.Date;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.spi.SubmoduleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.SubmoduleEffectiveStatementImpl;
import com.google.common.base.Optional;

public class SubmoduleStatementImpl extends
        AbstractRootStatement<SubmoduleStatement> implements SubmoduleStatement {

    protected SubmoduleStatementImpl(
            StmtContext<String, SubmoduleStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> {

        public Definition() {
            super(Rfc6020Mapping.SUBMODULE);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public SubmoduleStatement createDeclared(
                StmtContext<String, SubmoduleStatement, ?> ctx) {
            return new SubmoduleStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, SubmoduleStatement> createEffective(
                StmtContext<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> ctx) {
            return new SubmoduleEffectiveStatementImpl(ctx);
        }

        @Override
        public void onLinkageDeclared(
                Mutable<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> stmt)
                throws SourceException {

            Optional<Date> revisionDate = Optional.fromNullable(Utils.getLatestRevision(stmt.declaredSubstatements()));
            if (!revisionDate.isPresent()) {
                revisionDate = Optional.of(SimpleDateFormatUtil.DEFAULT_DATE_REV);
            }

            ModuleIdentifier submoduleIdentifier = new ModuleIdentifierImpl(
                    stmt.getStatementArgument(), Optional.<URI> absent(),
                    revisionDate);

            stmt.addContext(SubmoduleNamespace.class, submoduleIdentifier, stmt);

            String belongsToModuleName = firstAttributeOf(
                    stmt.declaredSubstatements(), BelongsToStatement.class);
            StmtContext<?, ?, ?> prefixSubStmtCtx = findFirstDeclaredSubstatement(
                    stmt, 0, BelongsToStatement.class, PrefixStatement.class);

            if (prefixSubStmtCtx == null) {
                throw new IllegalArgumentException(
                        "Prefix of belongsTo statement is missing in submodule ["
                                + stmt.getStatementArgument() + "].");
            }

            String prefix = (String) prefixSubStmtCtx.getStatementArgument();

            stmt.addToNs(BelongsToPrefixToModuleName.class, prefix,
                    belongsToModuleName);
        }
    }

    @Override
    public String getName() {
        return rawArgument();
    }

    @Override
    public YangVersionStatement getYangVersion() {
        return firstDeclared(YangVersionStatement.class);
    }

    @Override
    public BelongsToStatement getBelongsTo() {
        return firstDeclared(BelongsToStatement.class);
    }

}
