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
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.model.util.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.spi.SubmoduleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.SubmoduleEffectiveStatementImpl;

public class SubmoduleStatementImpl extends AbstractRootStatement<SubmoduleStatement> implements SubmoduleStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .SUBMODULE)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.AUGMENT)
            .addMandatory(YangStmtMapping.BELONGS_TO)
            .addAny(YangStmtMapping.CHOICE)
            .addOptional(YangStmtMapping.CONTACT)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.DEVIATION)
            .addAny(YangStmtMapping.EXTENSION)
            .addAny(YangStmtMapping.FEATURE)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.IDENTITY)
            .addAny(YangStmtMapping.IMPORT)
            .addAny(YangStmtMapping.INCLUDE)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addAny(YangStmtMapping.NOTIFICATION)
            .addOptional(YangStmtMapping.ORGANIZATION)
            .addOptional(YangStmtMapping.REFERENCE)
            .addAny(YangStmtMapping.REVISION)
            .addAny(YangStmtMapping.RPC)
            .addAny(YangStmtMapping.TYPEDEF)
            .addAny(YangStmtMapping.USES)
            .addOptional(YangStmtMapping.YANG_VERSION)
            .build();
    private static final Optional<Date> DEFAULT_REVISION = Optional.of(SimpleDateFormatUtil.DEFAULT_DATE_REV);

    protected SubmoduleStatementImpl(final StmtContext<String, SubmoduleStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<String, SubmoduleStatement,
            EffectiveStatement<String, SubmoduleStatement>> {

        public Definition() {
            super(YangStmtMapping.SUBMODULE);
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
        public void onPreLinkageDeclared(
                final Mutable<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> stmt) {
            stmt.setRootIdentifier(getSubmoduleIdentifier(stmt));
        }

        @Override
        public void onLinkageDeclared(
                final Mutable<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> stmt) {
            final ModuleIdentifier submoduleIdentifier = getSubmoduleIdentifier(stmt);

            final StmtContext<?, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> possibleDuplicateSubmodule =
                    stmt.getFromNamespace(SubmoduleNamespace.class, submoduleIdentifier);
            if (possibleDuplicateSubmodule != null && possibleDuplicateSubmodule != stmt) {
                throw new SourceException(stmt.getStatementSourceReference(), "Submodule name collision: %s. At %s",
                        stmt.getStatementArgument(), possibleDuplicateSubmodule.getStatementSourceReference());
            }

            stmt.addContext(SubmoduleNamespace.class, submoduleIdentifier, stmt);

            final String belongsToModuleName = firstAttributeOf(stmt.declaredSubstatements(), BelongsToStatement.class);
            final StmtContext<?, ?, ?> prefixSubStmtCtx = findFirstDeclaredSubstatement(stmt, 0,
                    BelongsToStatement.class, PrefixStatement.class);
            SourceException.throwIfNull(prefixSubStmtCtx, stmt.getStatementSourceReference(),
                    "Prefix of belongsTo statement is missing in submodule [%s]", stmt.getStatementArgument());

            final String prefix = (String) prefixSubStmtCtx.getStatementArgument();

            stmt.addToNs(BelongsToPrefixToModuleName.class, prefix, belongsToModuleName);
        }

        private static ModuleIdentifier getSubmoduleIdentifier(
                final Mutable<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> stmt) {
            final Date maybeDate = StmtContextUtils.getLatestRevision(stmt.declaredSubstatements());
            final Optional<Date> revisionDate = maybeDate != null ? Optional.of(maybeDate) : DEFAULT_REVISION;

            final ModuleIdentifier submoduleIdentifier = ModuleIdentifierImpl.create(stmt.getStatementArgument(),
                    Optional.empty(), revisionDate);
            return submoduleIdentifier;
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return rawArgument();
    }

    @Override
    public YangVersionStatement getYangVersion() {
        return firstDeclared(YangVersionStatement.class);
    }

    @Nonnull
    @Override
    public BelongsToStatement getBelongsTo() {
        return firstDeclared(BelongsToStatement.class);
    }

}
