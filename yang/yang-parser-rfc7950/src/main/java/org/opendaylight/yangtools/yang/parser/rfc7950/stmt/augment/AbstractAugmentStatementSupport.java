/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.spi.SchemaTreeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

abstract class AbstractAugmentStatementSupport
        extends BaseStatementSupport<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> {
    private static final Pattern PATH_REL_PATTERN1 = Pattern.compile("\\.\\.?\\s*/(.+)");
    private static final Pattern PATH_REL_PATTERN2 = Pattern.compile("//.*");

    AbstractAugmentStatementSupport() {
        super(YangStmtMapping.AUGMENT);
    }

    @Override
    public final SchemaNodeIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        SourceException.throwIf(PATH_REL_PATTERN1.matcher(value).matches()
            || PATH_REL_PATTERN2.matcher(value).matches(), ctx.sourceReference(),
            "Augment argument \'%s\' is not valid, it can be only absolute path; or descendant if used in uses",
            value);

        // As per:
        //   https://tools.ietf.org/html/rfc6020#section-7.15
        //   https://tools.ietf.org/html/rfc7950#section-7.17
        //
        // The argument is either Absolute or Descendant based on whether the statement is declared within a 'uses'
        // statement. The mechanics differs wildly between the two cases, so let's start by ensuring our argument
        // is in the correct domain.
        final SchemaNodeIdentifier result = ArgumentUtils.nodeIdentifierFromPath(ctx, value);
        final StatementDefinition parent = ctx.coerceParentContext().publicDefinition();
        if (parent == YangStmtMapping.USES) {
            SourceException.throwIf(result instanceof Absolute, ctx.sourceReference(),
                "Absolute schema node identifier is not allowed when used within a uses statement");
        } else {
            SourceException.throwIf(result instanceof Descendant, ctx.sourceReference(),
                "Descendant schema node identifier is not allowed when used outside of a uses statement");
        }
        return result;
    }

    @Override
    public final void onFullDefinitionDeclared(
            final Mutable<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> augmentNode) {
        if (!augmentNode.isSupportedByFeatures()) {
            // We need this augment node to be present, but it should not escape to effective world
            augmentNode.setIsSupportedToBuildEffective(false);
        }

        super.onFullDefinitionDeclared(augmentNode);

        if (StmtContextUtils.isInExtensionBody(augmentNode)) {
            return;
        }

        final ModelActionBuilder augmentAction = augmentNode.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        augmentAction.requiresCtx(augmentNode, ModelProcessingPhase.EFFECTIVE_MODEL);
        final Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> target = augmentAction.mutatesEffectiveCtxPath(
            getSearchRoot(augmentNode), SchemaTreeNamespace.class, augmentNode.getArgument().getNodeIdentifiers());

        augmentAction.apply(new AugmentInferenceAction(this, augmentNode, target));
    }

    @Override
    protected final AugmentStatement createDeclared(final StmtContext<SchemaNodeIdentifier, AugmentStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularAugmentStatement(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected final AugmentStatement createEmptyDeclared(
            final StmtContext<SchemaNodeIdentifier, AugmentStatement, ?> ctx) {
        return new EmptyAugmentStatement(ctx.getRawArgument(), ctx.getArgument());
    }

    @Override
    protected final List<? extends StmtContext<?, ?, ?>> statementsToBuild(
            final Current<SchemaNodeIdentifier, AugmentStatement> stmt,
            final List<? extends StmtContext<?, ?, ?>> substatements) {
        // Pick up the marker left by onFullDefinitionDeclared() inference action. If it is present we need to pass our
        // children through target's implicit wrapping.
        final StatementContextBase<?, ?, ?> implicitDef = stmt.getFromNamespace(AugmentImplicitHandlingNamespace.class,
            stmt.caerbannog());
        return implicitDef == null ? substatements : Lists.transform(substatements, subCtx -> {
            verify(subCtx instanceof StatementContextBase);
            return implicitDef.wrapWithImplicit((StatementContextBase<?, ?, ?>) subCtx);
        });
    }

    @Override
    protected final AugmentEffectiveStatement createEffective(
            final Current<SchemaNodeIdentifier, AugmentStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final int flags = new FlagsBuilder()
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .toFlags();

        try {
            return new AugmentEffectiveStatementImpl(stmt.declared(), stmt.getArgument(), flags,
                StmtContextUtils.getRootModuleQName(stmt.caerbannog()), substatements,
                (AugmentationSchemaNode) stmt.original());
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt.sourceReference(), e);
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
