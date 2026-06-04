/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.IdentifierBinding;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class AugmentStatementSupport
        extends AbstractStatementSupport<SchemaNodeIdentifier, @NonNull AugmentStatement, AugmentEffectiveStatement> {
    private static final @NonNull SubstatementValidator RFC6020_VALIDATOR =
        SubstatementValidator.builder(AugmentStatement.DEF)
            .addAny(AnyxmlStatement.DEF)
            .addAny(CaseStatement.DEF)
            .addAny(ChoiceStatement.DEF)
            .addAny(ContainerStatement.DEF)
            .addOptional(DescriptionStatement.DEF)
            .addAny(IfFeatureStatement.DEF)
            .addAny(LeafStatement.DEF)
            .addAny(LeafListStatement.DEF)
            .addAny(ListStatement.DEF)
            .addOptional(ReferenceStatement.DEF)
            .addOptional(StatusStatement.DEF)
            .addAny(UsesStatement.DEF)
            .addOptional(WhenStatement.DEF)
            .build();

    private static final @NonNull SubstatementValidator RFC7950_VALIDATOR =
        SubstatementValidator.builder(AugmentStatement.DEF)
            .addAny(ActionStatement.DEF)
            .addAny(AnydataStatement.DEF)
            .addAny(AnyxmlStatement.DEF)
            .addAny(CaseStatement.DEF)
            .addAny(ChoiceStatement.DEF)
            .addAny(ContainerStatement.DEF)
            .addOptional(DescriptionStatement.DEF)
            .addAny(IfFeatureStatement.DEF)
            .addAny(LeafStatement.DEF)
            .addAny(LeafListStatement.DEF)
            .addAny(ListStatement.DEF)
            .addAny(NotificationStatement.DEF)
            .addOptional(ReferenceStatement.DEF)
            .addOptional(StatusStatement.DEF)
            .addAny(UsesStatement.DEF)
            .addOptional(WhenStatement.DEF)
            .build();

    private final @NonNull AugmentStrategyResolver strategyResolver;

    @NonNullByDefault
    private AugmentStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator,
            final AugmentStrategyResolver strategyResolver) {
        super(AugmentStatement.DEF, StatementPolicy.copyDeclared(
            (copy, current, substatements) -> copy.getArgument().equals(current.getArgument())),
            SubtreePolicy.template(), config, validator);
        this.strategyResolver = requireNonNull(strategyResolver);
    }

    public static @NonNull AugmentStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new AugmentStatementSupport(config, RFC6020_VALIDATOR, AugmentStrategyResolver.RFC6020);
    }

    public static @NonNull AugmentStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new AugmentStatementSupport(config, RFC7950_VALIDATOR, AugmentStrategyResolver.RFC7950);
    }

    @Override
    public SchemaNodeIdentifier parseArgumentValue(final CommonStmtCtx stmt, final IdentifierBinding binding,
            final String rawArgument) {
        // As per:
        //   https://www.rfc-editor.org/rfc/rfc6020#section-7.15
        //   https://www.rfc-editor.org/rfc/rfc7950#section-7.17
        //
        // The argument is either Absolute or Descendant based on whether the statement is declared within a 'uses'
        // statement. The mechanics differs wildly between the two cases, so let's start by ensuring our argument
        // is in the correct domain.
        return stmt.coerceParentContext().produces(UsesStatement.DEF)
            ? binding.parseDescendantSchemaNodeidAs("uses-augment-arg", stmt, rawArgument)
            : binding.parseAbsoluteSchemaNodeidAs("augment-arg", stmt, rawArgument);
    }

    @Override
    public void onFullDefinitionDeclared(
            final Mutable<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> augmentNode) {
        if (!augmentNode.isSupportedByFeatures()) {
            // We need this augment node to be present, but it should not escape to effective world
            augmentNode.setUnsupported();
        }

        super.onFullDefinitionDeclared(augmentNode);

        if (isInExtensionBody(augmentNode)) {
            return;
        }

        final var augmentAction = augmentNode.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        augmentAction.requiresCtx(augmentNode, ModelProcessingPhase.EFFECTIVE_MODEL);
        final var target = augmentAction.mutatesEffectiveCtxPath(getSearchRoot(augmentNode),
            ParserNamespaces.schemaTree(), augmentNode.getArgument().getNodeIdentifiers());

        augmentAction.apply(new AugmentInferenceAction(strategyResolver, augmentNode, target));
    }

    @Override
    protected AugmentStatement createDeclared(final BoundStmtCtx<SchemaNodeIdentifier> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createAugment(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected AugmentStatement attachDeclarationReference(final AugmentStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateAugment(stmt, reference);
    }

    @Override
    protected Stream<? extends StmtContext<?, ?, ?>> statementsToBuild(
            final Current<SchemaNodeIdentifier, @NonNull AugmentStatement> stmt,
            final Stream<? extends StmtContext<?, ?, ?>> substatements) {
        // Pick up the marker left by onFullDefinitionDeclared() inference action. If it is present we need to pass our
        // children through target's implicit wrapping.
        final var implicitDef = stmt.namespaceItem(AugmentImplicitHandlingNamespace.INSTANCE, Empty.value());
        return implicitDef == null ? substatements
            : substatements.map(subCtx -> implicitDef.wrapWithImplicit(subCtx));
    }

    @Override
    protected AugmentEffectiveStatement createEffective(
            final Current<SchemaNodeIdentifier, AugmentStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final int flags = new FlagsBuilder()
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .toFlags();

        try {
            return EffectiveStatements.createAugment(stmt.declared(), stmt.getArgument(), flags, substatements);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    static StmtContext<?, ?, ?> getSearchRoot(final StmtContext<?, ?, ?> augmentContext) {
        // Augment is in uses - we need to augment instantiated nodes in parent.
        final var parent = augmentContext.coerceParentContext();
        return parent.produces(UsesStatement.DEF) ? parent.getParentContext() : parent;
    }

    // FIXME: 8.0.0: This method goes back as far as YANGTOOLS-365, when we were build EffectiveStatements for
    //               unsupported YANG extensions. We are not doing that anymore, do we still need this method? Also, it
    //               is only used in augment support to disable mechanics on unknown nodes.
    //
    //               It would seem we can move this method to AbstractAugmentStatementSupport at the very least, but
    //               also: augments are defined to operate on schema tree nodes, hence even if we have an
    //               UnknownStatement, but its EffectiveStatement projection supports SchemaTreeAwareEffectiveStatement
    //               we should operate normally -- the StatementSupport exposing such semantics is responsible for
    //               arranging the backend details.
    static boolean isInExtensionBody(final StmtContext<?, ?, ?> stmtCtx) {
        var current = stmtCtx;

        while (true) {
            final var parent = current.coerceParentContext();
            if (parent.getParentContext() == null) {
                return false;
            }
            if (parent.producesExtension()) {
                return true;
            }
            current = parent;
        }
    }
}
