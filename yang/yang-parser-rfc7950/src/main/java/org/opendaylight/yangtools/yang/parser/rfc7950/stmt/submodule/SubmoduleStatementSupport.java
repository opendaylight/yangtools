/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.submodule;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.findFirstDeclaredSubstatement;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.spi.SubmoduleNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class SubmoduleStatementSupport
        extends BaseStatementSupport<UnqualifiedQName, SubmoduleStatement, SubmoduleEffectiveStatement> {
    private static final @NonNull SubmoduleStatementSupport RFC6020_INSTANCE = new SubmoduleStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.SUBMODULE)
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
            .build());
    private static final @NonNull SubmoduleStatementSupport RFC7950_INSTANCE = new SubmoduleStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.SUBMODULE)
            .addAny(YangStmtMapping.ANYDATA)
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
            .build());

    private final SubstatementValidator validator;

    private SubmoduleStatementSupport(final SubstatementValidator validator) {
        super(YangStmtMapping.SUBMODULE, CopyPolicy.REJECT);
        this.validator = requireNonNull(validator);
    }

    public static @NonNull SubmoduleStatementSupport rfc6020Instance() {
        return RFC6020_INSTANCE;
    }

    public static @NonNull SubmoduleStatementSupport rfc7950Instance() {
        return RFC7950_INSTANCE;
    }

    @Override
    public UnqualifiedQName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return UnqualifiedQName.of(value);
        } catch (IllegalArgumentException e) {
            throw new SourceException(e.getMessage(), ctx, e);
        }
    }

    @Override
    public void onPreLinkageDeclared(
            final Mutable<UnqualifiedQName, SubmoduleStatement, SubmoduleEffectiveStatement> stmt) {
        stmt.setRootIdentifier(RevisionSourceIdentifier.create(stmt.getRawArgument(),
            StmtContextUtils.getLatestRevision(stmt.declaredSubstatements())));
    }

    @Override
    public void onLinkageDeclared(
            final Mutable<UnqualifiedQName, SubmoduleStatement, SubmoduleEffectiveStatement> stmt) {
        final SourceIdentifier submoduleIdentifier = RevisionSourceIdentifier.create(stmt.getRawArgument(),
            StmtContextUtils.getLatestRevision(stmt.declaredSubstatements()));

        final StmtContext<?, SubmoduleStatement, SubmoduleEffectiveStatement>
            possibleDuplicateSubmodule = stmt.getFromNamespace(SubmoduleNamespace.class, submoduleIdentifier);
        if (possibleDuplicateSubmodule != null && possibleDuplicateSubmodule != stmt) {
            throw new SourceException(stmt, "Submodule name collision: %s. At %s", stmt.rawArgument(),
                possibleDuplicateSubmodule.sourceReference());
        }

        stmt.addContext(SubmoduleNamespace.class, submoduleIdentifier, stmt);

        final String belongsToModuleName = firstAttributeOf(stmt.declaredSubstatements(), BelongsToStatement.class);
        final StmtContext<?, ?, ?> prefixSubStmtCtx = SourceException.throwIfNull(
            findFirstDeclaredSubstatement(stmt, 0, BelongsToStatement.class, PrefixStatement.class), stmt,
            "Prefix of belongsTo statement is missing in submodule [%s]", stmt.rawArgument());

        final String prefix = prefixSubStmtCtx.rawArgument();
        stmt.addToNs(BelongsToPrefixToModuleName.class, prefix, belongsToModuleName);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected SubmoduleStatement createDeclared(final StmtContext<UnqualifiedQName, SubmoduleStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new SubmoduleStatementImpl(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected SubmoduleStatement createEmptyDeclared(
            final StmtContext<UnqualifiedQName, SubmoduleStatement, ?> ctx) {
        throw noBelongsTo(ctx);
    }

    @Override
    protected SubmoduleEffectiveStatement createEffective(final Current<UnqualifiedQName, SubmoduleStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        if (substatements.isEmpty()) {
            throw noBelongsTo(stmt);
        }
        try {
            return new SubmoduleEffectiveStatementImpl(stmt, substatements);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    private static SourceException noBelongsTo(final CommonStmtCtx stmt) {
        return new SourceException("No belongs-to declared in submodule", stmt);
    }
}
