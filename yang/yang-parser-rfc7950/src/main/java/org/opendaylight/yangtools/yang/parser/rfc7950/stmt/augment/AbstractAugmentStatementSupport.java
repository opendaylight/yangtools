/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import static com.google.common.base.Verify.verify;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.spi.SchemaTreeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractAugmentStatementSupport
        extends BaseStatementSupport<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAugmentStatementSupport.class);

    AbstractAugmentStatementSupport() {
        super(YangStmtMapping.AUGMENT);
    }

    @Override
    public final SchemaNodeIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        // As per:
        //   https://tools.ietf.org/html/rfc6020#section-7.15
        //   https://tools.ietf.org/html/rfc7950#section-7.17
        //
        // The argument is either Absolute or Descendant based on whether the statement is declared within a 'uses'
        // statement. The mechanics differs wildly between the two cases, so let's start by ensuring our argument
        // is in the correct domain.

        final StatementDefinition parent = ctx.coerceParentContext().publicDefinition();
        final boolean absolute = value.startsWith("/");
        if (parent == YangStmtMapping.USES) {
            SourceException.throwIf(absolute, ctx.sourceReference(),
                "Absolute schema node identifier is not allowed when used within a uses statement");
            return ArgumentUtils.parseDescendantSchemaNodeIdentifier(ctx, value);
        }

        SourceException.throwIf(!absolute, ctx.sourceReference(),
            "Descendant schema node identifier is not allowed when used outside of a uses statement");
        return ArgumentUtils.parseAbsoluteSchemaNodeIdentifier(ctx, value);
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

        augmentAction.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                final StatementContextBase<?, ?, ?> augmentTargetCtx =
                        (StatementContextBase<?, ?, ?>) target.resolve(ctx);
                if (!isSupportedAugmentTarget(augmentTargetCtx)
                        || StmtContextUtils.isInExtensionBody(augmentTargetCtx)) {
                    augmentNode.setIsSupportedToBuildEffective(false);
                    return;
                }

                // We are targeting a context which is creating implicit nodes. In order to keep things consistent,
                // we will need to circle back when creating effective statements.
                if (augmentTargetCtx.hasImplicitParentSupport()) {
                    augmentNode.addToNs(AugmentImplicitHandlingNamespace.class, augmentNode, augmentTargetCtx);
                }

                final StatementContextBase<?, ?, ?> augmentSourceCtx = (StatementContextBase<?, ?, ?>) augmentNode;
                // FIXME: this is a workaround for models which augment a node which is added via an extension
                //        which we do not handle. This needs to be reworked in terms of unknown schema nodes.
                try {
                    copyFromSourceToTarget(augmentSourceCtx, augmentTargetCtx);
                    augmentTargetCtx.addEffectiveSubstatement(augmentSourceCtx);
                } catch (final SourceException e) {
                    LOG.warn("Failed to add augmentation {} defined at {}", augmentTargetCtx.sourceReference(),
                            augmentSourceCtx.sourceReference(), e);
                }
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                /*
                 * Do not fail, if it is an uses-augment to an unknown node.
                 */
                if (YangStmtMapping.USES == augmentNode.coerceParentContext().publicDefinition()) {
                    final SchemaNodeIdentifier augmentArg = augmentNode.getArgument();
                    final Optional<StmtContext<?, ?, ?>> targetNode = SchemaTreeNamespace.findNode(
                        getSearchRoot(augmentNode), augmentArg);
                    if (targetNode.isPresent() && StmtContextUtils.isUnknownStatement(targetNode.get())) {
                        augmentNode.setIsSupportedToBuildEffective(false);
                        LOG.warn("Uses-augment to unknown node {}. Augmentation has not been performed. At line: {}",
                            augmentArg, augmentNode.sourceReference());
                        return;
                    }
                }

                throw new InferenceException(augmentNode.sourceReference(), "Augment target '%s' not found",
                    augmentNode.argument());
            }
        });
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

    private static StmtContext<?, ?, ?> getSearchRoot(final StmtContext<?, ?, ?> augmentContext) {
        // Augment is in uses - we need to augment instantiated nodes in parent.
        final StmtContext<?, ?, ?> parent = augmentContext.coerceParentContext();
        if (YangStmtMapping.USES == parent.publicDefinition()) {
            return parent.getParentContext();
        }
        return parent;
    }

    final void copyFromSourceToTarget(final StatementContextBase<?, ?, ?> sourceCtx,
            final StatementContextBase<?, ?, ?> targetCtx) {
        final CopyType typeOfCopy = sourceCtx.coerceParentContext().producesDeclared(UsesStatement.class)
                ? CopyType.ADDED_BY_USES_AUGMENTATION : CopyType.ADDED_BY_AUGMENTATION;
        /*
         * Since Yang 1.1, if an augmentation is made conditional with a
         * "when" statement, it is allowed to add mandatory nodes.
         */
        final boolean skipCheckOfMandatoryNodes = allowsMandatory(sourceCtx);
        final boolean unsupported = !sourceCtx.isSupportedByFeatures();

        final Collection<? extends Mutable<?, ?, ?>> declared = sourceCtx.mutableDeclaredSubstatements();
        final Collection<? extends Mutable<?, ?, ?>> effective = sourceCtx.mutableEffectiveSubstatements();
        final Collection<Mutable<?, ?, ?>> buffer = new ArrayList<>(declared.size() + effective.size());

        for (final Mutable<?, ?, ?> originalStmtCtx : declared) {
            copyStatement(originalStmtCtx, targetCtx, typeOfCopy, buffer, skipCheckOfMandatoryNodes,
                unsupported || !originalStmtCtx.isSupportedByFeatures());
        }
        for (final Mutable<?, ?, ?> originalStmtCtx : effective) {
            copyStatement(originalStmtCtx, targetCtx, typeOfCopy, buffer, skipCheckOfMandatoryNodes, unsupported);
        }

        targetCtx.addEffectiveSubstatements(buffer);
    }

    abstract boolean allowsMandatory(StmtContext<?, ?, ?> ctx);

    static boolean hasWhenSubstatement(final StmtContext<?, ?, ?> ctx) {
        return ctx.hasSubstatement(WhenEffectiveStatement.class);
    }

    private static void copyStatement(final Mutable<?, ?, ?> original, final StatementContextBase<?, ?, ?> target,
            final CopyType typeOfCopy, final Collection<Mutable<?, ?, ?>> buffer,
            final boolean skipCheckOfMandatoryNodes, final boolean unsupported) {
        // We always copy statements, but if either the source statement or the augmentation which causes it are not
        // supported to build we also mark the target as such.
        if (needToCopyByAugment(original)) {
            validateNodeCanBeCopiedByAugment(original, target, typeOfCopy, skipCheckOfMandatoryNodes);

            final Mutable<?, ?, ?> copy = target.childCopyOf(original, typeOfCopy);
            if (unsupported) {
                copy.setIsSupportedToBuildEffective(false);
            }
            buffer.add(copy);
        } else if (isReusedByAugment(original) && !unsupported) {
            buffer.add(original);
        }
    }

    private static void validateNodeCanBeCopiedByAugment(final StmtContext<?, ?, ?> sourceCtx,
            final StatementContextBase<?, ?, ?> targetCtx, final CopyType typeOfCopy,
            final boolean skipCheckOfMandatoryNodes) {
        if (!skipCheckOfMandatoryNodes && typeOfCopy == CopyType.ADDED_BY_AUGMENTATION
                && requireCheckOfMandatoryNodes(sourceCtx, targetCtx)) {
            checkForMandatoryNodes(sourceCtx);
        }

        // Data definition statements must not collide on their namespace
        if (sourceCtx.producesDeclared(DataDefinitionStatement.class)) {
            for (final StmtContext<?, ?, ?> subStatement : targetCtx.allSubstatements()) {
                if (subStatement.producesDeclared(DataDefinitionStatement.class)) {
                    InferenceException.throwIf(Objects.equals(sourceCtx.argument(), subStatement.argument()),
                        sourceCtx.sourceReference(),
                        "An augment cannot add node named '%s' because this name is already used in target",
                        sourceCtx.rawArgument());
                }
            }
        }
    }

    private static void checkForMandatoryNodes(final StmtContext<?, ?, ?> sourceCtx) {
        if (StmtContextUtils.isNonPresenceContainer(sourceCtx)) {
            /*
             * We need to iterate over both declared and effective sub-statements,
             * because a mandatory node can be:
             * a) declared in augment body
             * b) added to augment body also via uses of a grouping and
             * such sub-statements are stored in effective sub-statements collection.
             */
            sourceCtx.allSubstatementsStream().forEach(AbstractAugmentStatementSupport::checkForMandatoryNodes);
        }

        InferenceException.throwIf(StmtContextUtils.isMandatoryNode(sourceCtx), sourceCtx.sourceReference(),
            "An augment cannot add node '%s' because it is mandatory and in module different than target",
            sourceCtx.rawArgument());
    }

    private static boolean requireCheckOfMandatoryNodes(final StmtContext<?, ?, ?> sourceCtx,
            Mutable<?, ?, ?> targetCtx) {
        /*
         * If the statement argument is not QName, it cannot be mandatory
         * statement, therefore return false and skip mandatory nodes validation
         */
        final Object arg = sourceCtx.argument();
        if (!(arg instanceof QName)) {
            return false;
        }
        final QName sourceStmtQName = (QName) arg;

        // RootStatementContext, for example
        final Mutable<?, ?, ?> root = targetCtx.getRoot();
        do {
            final Object targetArg = targetCtx.argument();
            verify(targetArg instanceof QName, "Argument of augment target statement must be QName, not %s", targetArg);
            final QName targetStmtQName = (QName) targetArg;
            /*
             * If target is from another module, return true and perform mandatory nodes validation
             */
            if (!targetStmtQName.getModule().equals(sourceStmtQName.getModule())) {
                return true;
            }

            /*
             * If target or one of the target's ancestors from the same namespace
             * is a presence container
             * or is non-mandatory choice
             * or is non-mandatory list
             * return false and skip mandatory nodes validation, because these nodes
             * are not mandatory node containers according to RFC 6020 section 3.1.
             */
            if (StmtContextUtils.isPresenceContainer(targetCtx)
                    || StmtContextUtils.isNotMandatoryNodeOfType(targetCtx, YangStmtMapping.CHOICE)
                    || StmtContextUtils.isNotMandatoryNodeOfType(targetCtx, YangStmtMapping.LIST)) {
                return false;
            }

            // This could be an augmentation stacked on top of a previous augmentation from the same module, which is
            // conditional -- in which case we do not run further checks
            if (targetCtx.getCopyHistory().getLastOperation() == CopyType.ADDED_BY_AUGMENTATION) {
                final Optional<? extends StmtContext<?, ?, ?>> optPrevCopy = targetCtx.getPreviousCopyCtx();
                if (optPrevCopy.isPresent()) {
                    final StmtContext<?, ?, ?> original = optPrevCopy.get();
                    final Object origArg = original.getArgument();
                    Verify.verify(origArg instanceof QName, "Unexpected statement argument %s", origArg);

                    if (sourceStmtQName.getModule().equals(((QName) origArg).getModule())
                            && hasWhenSubstatement(getParentAugmentation(original))) {
                        return false;
                    }
                }
            }
        } while ((targetCtx = targetCtx.getParentContext()) != root);

        /*
         * All target node's parents belong to the same module as source node,
         * therefore return false and skip mandatory nodes validation.
         */
        return false;
    }

    private static StmtContext<?, ?, ?> getParentAugmentation(final StmtContext<?, ?, ?> child) {
        StmtContext<?, ?, ?> parent = Verify.verifyNotNull(child.getParentContext(), "Child %s has not parent", child);
        while (parent.publicDefinition() != YangStmtMapping.AUGMENT) {
            parent = Verify.verifyNotNull(parent.getParentContext(), "Failed to find augmentation parent of %s", child);
        }
        return parent;
    }

    private static final ImmutableSet<YangStmtMapping> NOCOPY_DEF_SET = ImmutableSet.of(YangStmtMapping.USES,
        YangStmtMapping.WHEN, YangStmtMapping.DESCRIPTION, YangStmtMapping.REFERENCE, YangStmtMapping.STATUS);

    private static boolean needToCopyByAugment(final StmtContext<?, ?, ?> stmtContext) {
        return !NOCOPY_DEF_SET.contains(stmtContext.publicDefinition());
    }

    private static final ImmutableSet<YangStmtMapping> REUSED_DEF_SET = ImmutableSet.of(YangStmtMapping.TYPEDEF);

    private static boolean isReusedByAugment(final StmtContext<?, ?, ?> stmtContext) {
        return REUSED_DEF_SET.contains(stmtContext.publicDefinition());
    }

    static boolean isSupportedAugmentTarget(final StmtContext<?, ?, ?> substatementCtx) {
        /*
         * :TODO Substatement must be allowed augment target type e.g.
         * Container, etc... and must not be for example grouping, identity etc.
         * It is problem in case when more than one substatements have the same
         * QName, for example Grouping and Container are siblings and they have
         * the same QName. We must find the Container and the Grouping must be
         * ignored as disallowed augment target.
         */
        final Collection<?> allowedAugmentTargets = substatementCtx.getFromNamespace(
            ValidationBundlesNamespace.class, ValidationBundleType.SUPPORTED_AUGMENT_TARGETS);

        // if no allowed target is returned we consider all targets allowed
        return allowedAugmentTargets == null || allowedAugmentTargets.isEmpty()
                || allowedAugmentTargets.contains(substatementCtx.publicDefinition());
    }
}
