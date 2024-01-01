/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.submodule;

import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.findFirstDeclaredSubstatement;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractUnqualifiedStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class SubmoduleStatementSupport
        extends AbstractUnqualifiedStatementSupport<SubmoduleStatement, SubmoduleEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR =
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
            .build();
    private static final SubstatementValidator RFC7950_VALIDATOR =
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
            .build();

    private SubmoduleStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(YangStmtMapping.SUBMODULE, StatementPolicy.reject(), config, validator);
    }

    public static @NonNull SubmoduleStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new SubmoduleStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull SubmoduleStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new SubmoduleStatementSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    public void onPreLinkageDeclared(final Mutable<Unqualified, SubmoduleStatement, SubmoduleEffectiveStatement> stmt) {
        stmt.setRootIdentifier(new SourceIdentifier(stmt.getArgument(),
            StmtContextUtils.getLatestRevision(stmt.declaredSubstatements()).orElse(null)));
    }

    @Override
    public void onLinkageDeclared(final Mutable<Unqualified, SubmoduleStatement, SubmoduleEffectiveStatement> stmt) {
        final SourceIdentifier submoduleIdentifier = new SourceIdentifier(stmt.getArgument(),
            StmtContextUtils.getLatestRevision(stmt.declaredSubstatements()).orElse(null));

        // FIXME: This check should become superfluous once we operate in YangModuleInfo world
        final var possibleDuplicateSubmodule = stmt.namespaceItem(ParserNamespaces.SUBMODULE, submoduleIdentifier);
        if (possibleDuplicateSubmodule != null && possibleDuplicateSubmodule != stmt) {
            throw new SourceException(stmt, "Submodule name collision: %s. At %s", stmt.rawArgument(),
                possibleDuplicateSubmodule.sourceReference());
        }
        stmt.addToNs(ParserNamespaces.SUBMODULE, submoduleIdentifier, stmt);

        final Unqualified belongsToModuleName = firstAttributeOf(stmt.declaredSubstatements(),
            BelongsToStatement.class);
        final StmtContext<?, ?, ?> prefixSubStmtCtx = SourceException.throwIfNull(
            findFirstDeclaredSubstatement(stmt, 0, BelongsToStatement.class, PrefixStatement.class), stmt,
            "Prefix of belongsTo statement is missing in submodule [%s]", stmt.rawArgument());

        final String prefix = prefixSubStmtCtx.rawArgument();
        stmt.addToNs(ParserNamespaces.BELONGSTO_PREFIX_TO_MODULE_NAME, prefix, belongsToModuleName);
    }

    @Override
    protected SubmoduleStatement createDeclared(final BoundStmtCtx<Unqualified> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        if (substatements.isEmpty()) {
            throw noBelongsTo(ctx);
        }
        return DeclaredStatements.createSubmodule(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected SubmoduleStatement attachDeclarationReference(final SubmoduleStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateSubmodule(stmt, reference);
    }

    @Override
    protected SubmoduleEffectiveStatement createEffective(final Current<Unqualified, SubmoduleStatement> stmt,
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
