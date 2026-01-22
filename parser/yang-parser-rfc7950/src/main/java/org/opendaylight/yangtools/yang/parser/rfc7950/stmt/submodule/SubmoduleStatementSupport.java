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

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractUnqualifiedStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class SubmoduleStatementSupport
        extends AbstractUnqualifiedStatementSupport<SubmoduleStatement, SubmoduleEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.SUBMODULE)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.AUGMENT)
            .addMandatory(YangStmtMapping.BELONGS_TO)
            .addAny(YangStmtMapping.CHOICE)
            .addOptional(ContactStatement.DEFINITION)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(DescriptionStatement.DEFINITION)
            .addAny(YangStmtMapping.DEVIATION)
            .addAny(YangStmtMapping.EXTENSION)
            .addAny(FeatureStatement.DEFINITION)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.IDENTITY)
            .addAny(YangStmtMapping.IMPORT)
            .addAny(YangStmtMapping.INCLUDE)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addAny(YangStmtMapping.NOTIFICATION)
            .addOptional(OrganizationStatement.DEFINITION)
            .addOptional(ReferenceStatement.DEFINITION)
            .addAny(YangStmtMapping.REVISION)
            .addAny(RpcStatement.DEFINITION)
            .addAny(YangStmtMapping.TYPEDEF)
            .addAny(YangStmtMapping.USES)
            .addOptional(YangVersionStatement.DEFINITION)
            .build();
    private static final SubstatementValidator RFC7950_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.SUBMODULE)
            .addAny(YangStmtMapping.ANYDATA)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.AUGMENT)
            .addMandatory(YangStmtMapping.BELONGS_TO)
            .addAny(YangStmtMapping.CHOICE)
            .addOptional(ContactStatement.DEFINITION)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(DescriptionStatement.DEFINITION)
            .addAny(YangStmtMapping.DEVIATION)
            .addAny(YangStmtMapping.EXTENSION)
            .addAny(FeatureStatement.DEFINITION)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.IDENTITY)
            .addAny(YangStmtMapping.IMPORT)
            .addAny(YangStmtMapping.INCLUDE)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addAny(YangStmtMapping.NOTIFICATION)
            .addOptional(OrganizationStatement.DEFINITION)
            .addOptional(ReferenceStatement.DEFINITION)
            .addAny(YangStmtMapping.REVISION)
            .addAny(RpcStatement.DEFINITION)
            .addAny(YangStmtMapping.TYPEDEF)
            .addAny(YangStmtMapping.USES)
            .addOptional(YangVersionStatement.DEFINITION)
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
            StmtContextUtils.latestRevisionIn(stmt.declaredSubstatements())));
    }

    @Override
    public void onLinkageDeclared(final Mutable<Unqualified, SubmoduleStatement, SubmoduleEffectiveStatement> stmt) {
        final var submoduleIdentifier = new SourceIdentifier(stmt.getArgument(),
            StmtContextUtils.latestRevisionIn(stmt.declaredSubstatements()));

        final var possibleDuplicateSubmodule = stmt.namespaceItem(ParserNamespaces.SUBMODULE, submoduleIdentifier);
        if (possibleDuplicateSubmodule != null && possibleDuplicateSubmodule != stmt) {
            throw new SourceException(stmt, "Submodule name collision: %s. At %s", stmt.rawArgument(),
                possibleDuplicateSubmodule.sourceReference());
        }

        stmt.addToNs(ParserNamespaces.SUBMODULE, submoduleIdentifier, stmt);

        final var belongsToModuleName = firstAttributeOf(stmt.declaredSubstatements(), BelongsToStatement.class);
        final var prefixSubStmtCtx = SourceException.throwIfNull(
            findFirstDeclaredSubstatement(stmt, 0, BelongsToStatement.class, PrefixStatement.class), stmt,
            "Prefix of belongsTo statement is missing in submodule [%s]", stmt.rawArgument());

        final var prefix = prefixSubStmtCtx.rawArgument();
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
