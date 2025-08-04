/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.submodule;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractUnqualifiedStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class SubmoduleStatementSupport
        extends AbstractUnqualifiedStatementSupport<SubmoduleStatement, SubmoduleEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR =
        SubstatementValidator.builder(SubmoduleStatement.DEF)
            .addAny(AnyxmlStatement.DEF)
            .addAny(AugmentStatement.DEF)
            .addMandatory(BelongsToStatement.DEF)
            .addAny(ChoiceStatement.DEF)
            .addOptional(ContactStatement.DEF)
            .addAny(ContainerStatement.DEF)
            .addOptional(DescriptionStatement.DEF)
            .addAny(DeviationStatement.DEF)
            .addAny(ExtensionStatement.DEF)
            .addAny(FeatureStatement.DEF)
            .addAny(GroupingStatement.DEF)
            .addAny(IdentityStatement.DEF)
            .addAny(ImportStatement.DEF)
            .addAny(IncludeStatement.DEF)
            .addAny(LeafStatement.DEF)
            .addAny(LeafListStatement.DEF)
            .addAny(ListStatement.DEF)
            .addAny(NotificationStatement.DEF)
            .addOptional(OrganizationStatement.DEF)
            .addOptional(ReferenceStatement.DEF)
            .addAny(RevisionStatement.DEF)
            .addAny(RpcStatement.DEF)
            .addAny(TypedefStatement.DEF)
            .addAny(UsesStatement.DEF)
            .addOptional(YangVersionStatement.DEF)
            .build();
    private static final SubstatementValidator RFC7950_VALIDATOR =
        SubstatementValidator.builder(SubmoduleStatement.DEF)
            .addAny(AnydataStatement.DEF)
            .addAny(AnyxmlStatement.DEF)
            .addAny(AugmentStatement.DEF)
            .addMandatory(BelongsToStatement.DEF)
            .addAny(ChoiceStatement.DEF)
            .addOptional(ContactStatement.DEF)
            .addAny(ContainerStatement.DEF)
            .addOptional(DescriptionStatement.DEF)
            .addAny(DeviationStatement.DEF)
            .addAny(ExtensionStatement.DEF)
            .addAny(FeatureStatement.DEF)
            .addAny(GroupingStatement.DEF)
            .addAny(IdentityStatement.DEF)
            .addAny(ImportStatement.DEF)
            .addAny(IncludeStatement.DEF)
            .addAny(LeafStatement.DEF)
            .addAny(LeafListStatement.DEF)
            .addAny(ListStatement.DEF)
            .addAny(NotificationStatement.DEF)
            .addOptional(OrganizationStatement.DEF)
            .addOptional(ReferenceStatement.DEF)
            .addAny(RevisionStatement.DEF)
            .addAny(RpcStatement.DEF)
            .addAny(TypedefStatement.DEF)
            .addAny(UsesStatement.DEF)
            .addOptional(YangVersionStatement.DEF)
            .build();

    private SubmoduleStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(SubmoduleStatement.DEF, StatementPolicy.reject(), config, validator);
    }

    public static @NonNull SubmoduleStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new SubmoduleStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull SubmoduleStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new SubmoduleStatementSupport(config, RFC7950_VALIDATOR);
    }

//    @Override
//    public void onLinkageDeclared(final Mutable<Unqualified, SubmoduleStatement, SubmoduleEffectiveStatement> stmt) {
//        final var submoduleIdentifier = new SourceIdentifier(stmt.getArgument(),
//            StmtContextUtils.latestRevisionIn(stmt.declaredSubstatements()));
//
//        final var possibleDuplicateSubmodule = stmt.namespaceItem(ParserNamespaces.SUBMODULE, submoduleIdentifier);
//        if (possibleDuplicateSubmodule != null && possibleDuplicateSubmodule != stmt) {
//            throw new SourceException(stmt, "Submodule name collision: %s. At %s", stmt.rawArgument(),
//                possibleDuplicateSubmodule.sourceReference());
//        }
//
//        stmt.addToNs(ParserNamespaces.SUBMODULE, submoduleIdentifier, stmt);
//
//        final var belongsToModuleName = firstAttributeOf(stmt.declaredSubstatements(), BelongsToStatement.class);
//        final var prefixSubStmtCtx = SourceException.throwIfNull(
//            findFirstDeclaredSubstatement(stmt, 0, BelongsToStatement.class, PrefixStatement.class), stmt,
//            "Prefix of belongsTo statement is missing in submodule [%s]", stmt.rawArgument());
//
//        final var prefix = prefixSubStmtCtx.rawArgument();
//        stmt.addToNs(ParserNamespaces.BELONGSTO_PREFIX_TO_MODULE_NAME, prefix, belongsToModuleName);
//    }

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
