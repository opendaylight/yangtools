/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
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
import org.opendaylight.yangtools.yang.parser.spi.source.StmtOrderingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractAugmentStatementSupport extends AbstractStatementSupport<SchemaNodeIdentifier, AugmentStatement,
        EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAugmentStatementSupport.class);
    private static final Pattern PATH_REL_PATTERN1 = Pattern.compile("\\.\\.?\\s*/(.+)");
    private static final Pattern PATH_REL_PATTERN2 = Pattern.compile("//.*");

    AbstractAugmentStatementSupport() {
        super(YangStmtMapping.AUGMENT);
    }

    @Override
    public final SchemaNodeIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        SourceException.throwIf(PATH_REL_PATTERN1.matcher(value).matches()
            || PATH_REL_PATTERN2.matcher(value).matches(), ctx.getStatementSourceReference(),
            "Augment argument \'%s\' is not valid, it can be only absolute path; or descendant if used in uses",
            value);

        return ArgumentUtils.nodeIdentifierFromPath(ctx, value);
    }

    @Override
    public final AugmentStatement createDeclared(final StmtContext<SchemaNodeIdentifier, AugmentStatement, ?> ctx) {
        return new AugmentStatementImpl(ctx);
    }

    @Override
    public final EffectiveStatement<SchemaNodeIdentifier, AugmentStatement> createEffective(
            final StmtContext<SchemaNodeIdentifier, AugmentStatement,
            EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> ctx) {
        return new AugmentEffectiveStatementImpl(ctx);
    }

    @Override
    public final void onFullDefinitionDeclared(final Mutable<SchemaNodeIdentifier, AugmentStatement,
            EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> augmentNode) {
        if (!augmentNode.isSupportedByFeatures()) {
            return;
        }

        super.onFullDefinitionDeclared(augmentNode);

        if (StmtContextUtils.isInExtensionBody(augmentNode)) {
            return;
        }

        final ModelActionBuilder augmentAction = augmentNode.newInferenceAction(
            ModelProcessingPhase.EFFECTIVE_MODEL);
        final Prerequisite<StmtContext<SchemaNodeIdentifier, AugmentStatement,
            EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>>> sourceCtxPrereq =
                augmentAction.requiresCtx(augmentNode, ModelProcessingPhase.EFFECTIVE_MODEL);
        final Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> target =
                augmentAction.mutatesEffectiveCtxPath(getSearchRoot(augmentNode),
                    ChildSchemaNodeNamespace.class, augmentNode.coerceStatementArgument().getPathFromRoot());

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

                /*
                 * FIXME: The mechanics here are a bit contrived: we need to check if this augmentation is introducing
                 *        any statements which induce an implicit statement to be created. If that is the case, we need
                 *        to rework the definition of this statement to account for those implicit statements.
                 *
                 *        We are in API freeze and we should have a quick way of checking if the statement support
                 *        wishes to customize any of our nodes :( We need to come up with something...
                 */
                final StatementContextBase<?, ?, ?> augmentSourceCtx = (StatementContextBase<?, ?, ?>) augmentNode;



                // FIXME: this is a workaround for models which augment a node which is added via an extension
                //        which we do not handle. This needs to be reworked in terms of unknown schema nodes.
                try {
                    copyFromSourceToTarget(augmentSourceCtx, augmentTargetCtx);
                    augmentTargetCtx.addEffectiveSubstatement(augmentSourceCtx);
                    updateAugmentOrder(augmentSourceCtx);
                } catch (final SourceException e) {
                    LOG.warn("Failed to add augmentation {} defined at {}",
                        augmentTargetCtx.getStatementSourceReference(),
                            augmentSourceCtx.getStatementSourceReference(), e);
                }
            }

            private void updateAugmentOrder(final StatementContextBase<?, ?, ?> augmentSourceCtx) {
                Integer currentOrder = augmentSourceCtx.getFromNamespace(StmtOrderingNamespace.class,
                    YangStmtMapping.AUGMENT);
                if (currentOrder == null) {
                    currentOrder = 1;
                } else {
                    currentOrder++;
                }

                augmentSourceCtx.addToNs(StmtOrderingNamespace.class, YangStmtMapping.AUGMENT, currentOrder);
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                /*
                 * Do not fail, if it is an uses-augment to an unknown node.
                 */
                if (YangStmtMapping.USES == augmentNode.coerceParentContext().getPublicDefinition()) {
                    final SchemaNodeIdentifier augmentArg = augmentNode.coerceStatementArgument();
                    final Optional<StmtContext<?, ?, ?>> targetNode = ChildSchemaNodeNamespace.findNode(
                        getSearchRoot(augmentNode), augmentArg);
                    if (targetNode.isPresent() && StmtContextUtils.isUnknownStatement(targetNode.get())) {
                        augmentNode.setIsSupportedToBuildEffective(false);
                        LOG.warn("Uses-augment to unknown node {}. Augmentation has not been performed. At line: {}",
                            augmentArg, augmentNode.getStatementSourceReference());
                        return;
                    }
                }

                throw new InferenceException(augmentNode.getStatementSourceReference(),
                        "Augment target '%s' not found", augmentNode.getStatementArgument());
            }
        });
    }

    private static StmtContext<?, ?, ?> getSearchRoot(final StmtContext<?, ?, ?> augmentContext) {
        // Augment is in uses - we need to augment instantiated nodes in parent.
        final StmtContext<?, ?, ?> parent = augmentContext.coerceParentContext();
        if (YangStmtMapping.USES == parent.getPublicDefinition()) {
            return parent.getParentContext();
        }
        return parent;
    }

    static void copyFromSourceToTarget(final StatementContextBase<?, ?, ?> sourceCtx,
            final StatementContextBase<?, ?, ?> targetCtx) {
        final CopyType typeOfCopy = UsesStatement.class.equals(sourceCtx.coerceParentContext().getPublicDefinition()
                .getDeclaredRepresentationClass()) ? CopyType.ADDED_BY_USES_AUGMENTATION
                : CopyType.ADDED_BY_AUGMENTATION;
        /*
         * Since Yang 1.1, if an augmentation is made conditional with a
         * "when" statement, it is allowed to add mandatory nodes.
         */
        final boolean skipCheckOfMandatoryNodes = YangVersion.VERSION_1_1.equals(sourceCtx.getRootVersion())
                && isConditionalAugmentStmt(sourceCtx);

        final Collection<? extends Mutable<?, ?, ?>> declared = sourceCtx.mutableDeclaredSubstatements();
        final Collection<? extends Mutable<?, ?, ?>> effective = sourceCtx.mutableEffectiveSubstatements();
        final Collection<Mutable<?, ?, ?>> buffer = new ArrayList<>(declared.size() + effective.size());

        for (final Mutable<?, ?, ?> originalStmtCtx : declared) {
            if (originalStmtCtx.isSupportedByFeatures()) {
                copyStatement(originalStmtCtx, targetCtx, typeOfCopy, buffer, skipCheckOfMandatoryNodes);
            }
        }
        for (final Mutable<?, ?, ?> originalStmtCtx : effective) {
            copyStatement(originalStmtCtx, targetCtx, typeOfCopy, buffer, skipCheckOfMandatoryNodes);
        }

        targetCtx.addEffectiveSubstatements(buffer);
    }

    /**
     * Checks whether supplied statement context is conditional augment
     * statement.
     *
     * @param ctx
     *            statement context to be checked
     *
     * @return true if supplied statement context is conditional augment
     *         statement, otherwise false
     */
    private static boolean isConditionalAugmentStmt(final StmtContext<?, ?, ?> ctx) {
        return ctx.getPublicDefinition() == YangStmtMapping.AUGMENT
                && StmtContextUtils.findFirstSubstatement(ctx, WhenStatement.class) != null;
    }

    private static void copyStatement(final Mutable<?, ?, ?> original, final StatementContextBase<?, ?, ?> target,
            final CopyType typeOfCopy, final Collection<Mutable<?, ?, ?>> buffer,
            final boolean skipCheckOfMandatoryNodes) {
        if (needToCopyByAugment(original)) {
            validateNodeCanBeCopiedByAugment(original, target, typeOfCopy, skipCheckOfMandatoryNodes);

            buffer.add(target.childCopyOf(original, typeOfCopy));
        } else if (isReusedByAugment(original)) {
            buffer.add(original);
        }
    }

    private static void validateNodeCanBeCopiedByAugment(final StmtContext<?, ?, ?> sourceCtx,
            final StatementContextBase<?, ?, ?> targetCtx, final CopyType typeOfCopy,
            final boolean skipCheckOfMandatoryNodes) {

        if (WhenStatement.class.equals(sourceCtx.getPublicDefinition().getDeclaredRepresentationClass())) {
            return;
        }

        if (!skipCheckOfMandatoryNodes && typeOfCopy == CopyType.ADDED_BY_AUGMENTATION
                && reguiredCheckOfMandatoryNodes(sourceCtx, targetCtx)) {
            checkForMandatoryNodes(sourceCtx);
        }

        // Data definition statements must not collide on their namespace
        if (DataDefinitionStatement.class.isAssignableFrom(
            sourceCtx.getPublicDefinition().getDeclaredRepresentationClass())) {
            for (final StmtContext<?, ?, ?> subStatement : targetCtx.allSubstatements()) {
                if (DataDefinitionStatement.class.isAssignableFrom(
                    subStatement.getPublicDefinition().getDeclaredRepresentationClass())) {

                    InferenceException.throwIf(
                        Objects.equals(sourceCtx.getStatementArgument(), subStatement.getStatementArgument()),
                        sourceCtx.getStatementSourceReference(),
                        "An augment cannot add node named '%s' because this name is already used in target",
                        sourceCtx.rawStatementArgument());
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

        InferenceException.throwIf(StmtContextUtils.isMandatoryNode(sourceCtx),
                sourceCtx.getStatementSourceReference(),
                "An augment cannot add node '%s' because it is mandatory and in module different than target",
                sourceCtx.rawStatementArgument());
    }

    private static boolean reguiredCheckOfMandatoryNodes(final StmtContext<?, ?, ?> sourceCtx,
            Mutable<?, ?, ?> targetCtx) {
        /*
         * If the statement argument is not QName, it cannot be mandatory
         * statement, therefore return false and skip mandatory nodes validation
         */
        if (!(sourceCtx.getStatementArgument() instanceof QName)) {
            return false;
        }
        final QName sourceStmtQName = (QName) sourceCtx.getStatementArgument();

        // RootStatementContext, for example
        final Mutable<?, ?, ?> root = targetCtx.getRoot();
        do {
            Verify.verify(targetCtx.getStatementArgument() instanceof QName,
                    "Argument of augment target statement must be QName.");
            final QName targetStmtQName = (QName) targetCtx.getStatementArgument();
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
        } while ((targetCtx = targetCtx.getParentContext()) != root);

        /*
         * All target node's parents belong to the same module as source node,
         * therefore return false and skip mandatory nodes validation.
         */
        return false;
    }

    private static final ImmutableSet<YangStmtMapping> NOCOPY_DEF_SET = ImmutableSet.of(YangStmtMapping.USES,
        YangStmtMapping.WHEN, YangStmtMapping.DESCRIPTION, YangStmtMapping.REFERENCE, YangStmtMapping.STATUS);

    private static boolean needToCopyByAugment(final StmtContext<?, ?, ?> stmtContext) {
        return !NOCOPY_DEF_SET.contains(stmtContext.getPublicDefinition());
    }

    private static final ImmutableSet<YangStmtMapping> REUSED_DEF_SET = ImmutableSet.of(YangStmtMapping.TYPEDEF);

    private static boolean isReusedByAugment(final StmtContext<?, ?, ?> stmtContext) {
        return REUSED_DEF_SET.contains(stmtContext.getPublicDefinition());
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
                || allowedAugmentTargets.contains(substatementCtx.getPublicDefinition());
    }
}
