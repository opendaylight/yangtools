/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import com.google.common.collect.ImmutableList;
import java.util.stream.Stream;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentBindingException;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractAugmentStatementSupport
        extends AbstractStatementSupport<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> {
    AbstractAugmentStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(YangStmtMapping.AUGMENT, StatementPolicy.copyDeclared(
            (copy, current, substatements) -> copy.getArgument().equals(current.getArgument())),
            config, validator);
    }

    @Override
    public final SchemaNodeIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        // As per:
        //   https://www.rfc-editor.org/rfc/rfc6020#section-7.15
        //   https://www.rfc-editor.org/rfc/rfc7950#section-7.17
        //
        // The argument is either Absolute or Descendant based on whether the statement is declared within a 'uses'
        // statement. The mechanics differs wildly between the two cases, so let's start by ensuring our argument
        // is in the correct domain.
        final var parsers = ctx.commonParsers();

        if (ctx.coerceParentContext().producesDeclared(UsesStatement.class)) {
            try {
                return parsers.descendantSchemaNodeId().parseArgument(value);
            } catch (ArgumentSyntaxException e) {
                throw new SourceException(ctx, e, "'%s' is not a valid descendant-schema-nodeid at position %s: %s",
                    value, e.getPosition(), e.getMessage());
            } catch (ArgumentBindingException e) {
                throw new InferenceException(
                    "'%s' cannot be bound as a descendant-schema-nodeid: %s".formatted(value, e.getMessage()), ctx, e);
            }
        }

        try {
            return parsers.absoluteSchemaNodeId().parseArgument(value);
        } catch (ArgumentSyntaxException e) {
            throw new SourceException(ctx, e, "'%s' is not a valid absolute-schema-nodeid at position %s: %s", value,
                e.getPosition(), e.getMessage());
        } catch (ArgumentBindingException e) {
            throw new InferenceException(
                "'%s' cannot be bound as a absolute-schema-nodeid: %s".formatted(value, e.getMessage()), ctx, e);
        }
    }

    @Override
    public final void onFullDefinitionDeclared(
            final Mutable<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> augmentNode) {
        if (!augmentNode.isSupportedByFeatures()) {
            // We need this augment node to be present, but it should not escape to effective world
            augmentNode.setUnsupported();
        }

        super.onFullDefinitionDeclared(augmentNode);

        if (StmtContextUtils.isInExtensionBody(augmentNode)) {
            return;
        }

        final ModelActionBuilder augmentAction = augmentNode.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        augmentAction.requiresCtx(augmentNode, ModelProcessingPhase.EFFECTIVE_MODEL);
        final Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> target = augmentAction.mutatesEffectiveCtxPath(
            getSearchRoot(augmentNode), ParserNamespaces.schemaTree(), augmentNode.getArgument().getNodeIdentifiers());

        augmentAction.apply(new AugmentInferenceAction(this, augmentNode, target));
    }

    @Override
    protected final AugmentStatement createDeclared(final BoundStmtCtx<SchemaNodeIdentifier> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createAugment(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected final AugmentStatement attachDeclarationReference(final AugmentStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateAugment(stmt, reference);
    }

    @Override
    protected final Stream<? extends StmtContext<?, ?, ?>> statementsToBuild(
            final Current<SchemaNodeIdentifier, AugmentStatement> stmt,
            final Stream<? extends StmtContext<?, ?, ?>> substatements) {
        // Pick up the marker left by onFullDefinitionDeclared() inference action. If it is present we need to pass our
        // children through target's implicit wrapping.
        final var implicitDef = stmt.namespaceItem(AugmentImplicitHandlingNamespace.INSTANCE, Empty.value());
        return implicitDef == null ? substatements
            : substatements.map(subCtx -> implicitDef.wrapWithImplicit(subCtx));
    }

    @Override
    protected final AugmentEffectiveStatement createEffective(
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

    abstract boolean allowsMandatory(StmtContext<?, ?, ?> ctx);

    static StmtContext<?, ?, ?> getSearchRoot(final StmtContext<?, ?, ?> augmentContext) {
        // Augment is in uses - we need to augment instantiated nodes in parent.
        final StmtContext<?, ?, ?> parent = augmentContext.coerceParentContext();
        if (YangStmtMapping.USES == parent.publicDefinition()) {
            return parent.getParentContext();
        }
        return parent;
    }

    static boolean hasWhenSubstatement(final StmtContext<?, ?, ?> ctx) {
        return ctx.hasSubstatement(WhenEffectiveStatement.class);
    }
}
