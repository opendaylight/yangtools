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

import java.util.Date;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.model.util.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.spi.SubmoduleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.SubmoduleEffectiveStatementImpl;

public class SubmoduleStatementImpl extends AbstractRootStatement<SubmoduleStatement> implements SubmoduleStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .SUBMODULE)
            .addAny(Rfc6020Mapping.ANYXML)
            .addAny(Rfc6020Mapping.AUGMENT)
            .addMandatory(Rfc6020Mapping.BELONGS_TO)
            .addAny(Rfc6020Mapping.CHOICE)
            .addOptional(Rfc6020Mapping.CONTACT)
            .addAny(Rfc6020Mapping.CONTAINER)
            .addOptional(Rfc6020Mapping.DESCRIPTION)
            .addAny(Rfc6020Mapping.DEVIATION)
            .addAny(Rfc6020Mapping.EXTENSION)
            .addAny(Rfc6020Mapping.FEATURE)
            .addAny(Rfc6020Mapping.GROUPING)
            .addAny(Rfc6020Mapping.IDENTITY)
            .addAny(Rfc6020Mapping.IMPORT)
            .addAny(Rfc6020Mapping.INCLUDE)
            .addAny(Rfc6020Mapping.LEAF)
            .addAny(Rfc6020Mapping.LEAF_LIST)
            .addAny(Rfc6020Mapping.LIST)
            .addAny(Rfc6020Mapping.NOTIFICATION)
            .addOptional(Rfc6020Mapping.ORGANIZATION)
            .addOptional(Rfc6020Mapping.REFERENCE)
            .addAny(Rfc6020Mapping.REVISION)
            .addAny(Rfc6020Mapping.RPC)
            .addAny(Rfc6020Mapping.TYPEDEF)
            .addAny(Rfc6020Mapping.USES)
            .addOptional(Rfc6020Mapping.YANG_VERSION)
            .build();
    private static final Optional<Date> DEFAULT_REVISION = Optional.of(SimpleDateFormatUtil.DEFAULT_DATE_REV);

    protected SubmoduleStatementImpl(final StmtContext<String, SubmoduleStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<String, SubmoduleStatement,
            EffectiveStatement<String, SubmoduleStatement>> {

        public Definition() {
            super(Rfc6020Mapping.SUBMODULE);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public SubmoduleStatement createDeclared(
                final StmtContext<String, SubmoduleStatement, ?> ctx) {
            return new SubmoduleStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, SubmoduleStatement> createEffective(
                final StmtContext<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> ctx) {
            return new SubmoduleEffectiveStatementImpl(ctx);
        }

        @Override
        public void onLinkageDeclared(
                final Mutable<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> stmt) {
            final Date maybeDate = Utils.getLatestRevision(stmt.declaredSubstatements());
            final Optional<Date> revisionDate = maybeDate == null ? Optional.of(maybeDate) : DEFAULT_REVISION;

            ModuleIdentifier submoduleIdentifier = ModuleIdentifierImpl.create(stmt.getStatementArgument(),
                Optional.empty(), revisionDate);

            stmt.addContext(SubmoduleNamespace.class, submoduleIdentifier, stmt);

            String belongsToModuleName = firstAttributeOf(
                    stmt.declaredSubstatements(), BelongsToStatement.class);
            StmtContext<?, ?, ?> prefixSubStmtCtx = findFirstDeclaredSubstatement(
                    stmt, 0, BelongsToStatement.class, PrefixStatement.class);
            SourceException.throwIfNull(prefixSubStmtCtx, stmt.getStatementSourceReference(),
                "Prefix of belongsTo statement is missing in submodule [%s]", stmt.getStatementArgument());

            String prefix = (String) prefixSubStmtCtx.getStatementArgument();

            stmt.addToNs(BelongsToPrefixToModuleName.class, prefix, belongsToModuleName);
        }

        @Override
        public void onFullDefinitionDeclared(final Mutable<String, SubmoduleStatement,
                EffectiveStatement<String, SubmoduleStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
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
