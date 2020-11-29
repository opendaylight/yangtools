/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.spi.SchemaTreeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inference action, split out of {@link AbstractAugmentStatementSupport} for clarity and potential specialization.
 */
final class AugmentInferenceAction implements InferenceAction {
    private static final Logger LOG = LoggerFactory.getLogger(AugmentInferenceAction.class);
    private static final ImmutableSet<YangStmtMapping> NOCOPY_DEF_SET = ImmutableSet.of(YangStmtMapping.USES,
        YangStmtMapping.WHEN, YangStmtMapping.DESCRIPTION, YangStmtMapping.REFERENCE, YangStmtMapping.STATUS);

    private final Mutable<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> augmentNode;
    private final Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> target;
    private final AbstractAugmentStatementSupport statementSupport;

    AugmentInferenceAction(final AbstractAugmentStatementSupport statementSupport,
            final Mutable<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> augmentNode,
            final Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> target) {
        this.statementSupport = requireNonNull(statementSupport);
        this.augmentNode = requireNonNull(augmentNode);
        this.target = requireNonNull(target);
    }

    @Override
    public void apply(final InferenceContext ctx) {
        final StatementContextBase<?, ?, ?> augmentTargetCtx = (StatementContextBase<?, ?, ?>) target.resolve(ctx);
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

        // FIXME: this is a workaround for models which augment a node which is added via an extension
        //        which we do not handle. This needs to be reworked in terms of unknown schema nodes.
        try {
            copyFromSourceToTarget((StatementContextBase<?, ?, ?>) augmentNode, augmentTargetCtx);
            augmentTargetCtx.addEffectiveSubstatement(augmentNode.replicaAsChildOf(augmentTargetCtx));
        } catch (final SourceException e) {
            LOG.warn("Failed to add augmentation {} defined at {}", augmentTargetCtx.sourceReference(),
                augmentNode.sourceReference(), e);
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
                AbstractAugmentStatementSupport.getSearchRoot(augmentNode), augmentArg);
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

    private void copyFromSourceToTarget(final StatementContextBase<?, ?, ?> sourceCtx,
            final StatementContextBase<?, ?, ?> targetCtx) {
        final CopyType typeOfCopy = sourceCtx.coerceParentContext().producesDeclared(UsesStatement.class)
            ? CopyType.ADDED_BY_USES_AUGMENTATION : CopyType.ADDED_BY_AUGMENTATION;
        /*
         * Since Yang 1.1, if an augmentation is made conditional with a
         * "when" statement, it is allowed to add mandatory nodes.
         */
        final boolean skipCheckOfMandatoryNodes = statementSupport.allowsMandatory(sourceCtx);
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
        } else if (!unsupported && original.publicDefinition() == YangStmtMapping.TYPEDEF) {
            buffer.add(original.replicaAsChildOf(target));
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
            sourceCtx.allSubstatementsStream().forEach(AugmentInferenceAction::checkForMandatoryNodes);
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
                    verify(origArg instanceof QName, "Unexpected statement argument %s", origArg);

                    if (sourceStmtQName.getModule().equals(((QName) origArg).getModule())
                        && AbstractAugmentStatementSupport.hasWhenSubstatement(getParentAugmentation(original))) {
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
        StmtContext<?, ?, ?> parent = verifyNotNull(child.getParentContext(), "Child %s has not parent", child);
        while (parent.publicDefinition() != YangStmtMapping.AUGMENT) {
            parent = verifyNotNull(parent.getParentContext(), "Failed to find augmentation parent of %s", child);
        }
        return parent;
    }

    private static boolean needToCopyByAugment(final StmtContext<?, ?, ?> stmtContext) {
        return !NOCOPY_DEF_SET.contains(stmtContext.publicDefinition());
    }

    private static boolean isSupportedAugmentTarget(final StmtContext<?, ?, ?> substatementCtx) {
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
