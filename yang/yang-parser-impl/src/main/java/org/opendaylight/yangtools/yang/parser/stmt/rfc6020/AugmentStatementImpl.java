/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.AugmentToChoiceNamespace;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StmtOrderingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.RootStatementContext;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AugmentEffectiveStatementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AugmentStatementImpl extends AbstractDeclaredStatement<SchemaNodeIdentifier> implements AugmentStatement {
    private static final Logger LOG = LoggerFactory.getLogger(AugmentStatementImpl.class);
    private static final Pattern PATH_REL_PATTERN1 = Pattern.compile("\\.\\.?\\s*/(.+)");
    private static final Pattern PATH_REL_PATTERN2 = Pattern.compile("//.*");
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator
            .builder(Rfc6020Mapping.AUGMENT)
            .addAny(Rfc6020Mapping.ANYXML)
            .addAny(Rfc6020Mapping.CASE)
            .addAny(Rfc6020Mapping.CHOICE)
            .addAny(Rfc6020Mapping.CONTAINER)
            .addOptional(Rfc6020Mapping.DESCRIPTION)
            .addAny(Rfc6020Mapping.IF_FEATURE)
            .addAny(Rfc6020Mapping.LEAF)
            .addAny(Rfc6020Mapping.LEAF_LIST)
            .addAny(Rfc6020Mapping.LIST)
            .addOptional(Rfc6020Mapping.REFERENCE)
            .addOptional(Rfc6020Mapping.STATUS)
            .addAny(Rfc6020Mapping.USES)
            .addOptional(Rfc6020Mapping.WHEN)
            .build();

    protected AugmentStatementImpl(final StmtContext<SchemaNodeIdentifier, AugmentStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> {

        public Definition() {
            super(Rfc6020Mapping.AUGMENT);
        }

        @Override
        public SchemaNodeIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            Preconditions.checkArgument(!PATH_REL_PATTERN1.matcher(value).matches()
                && !PATH_REL_PATTERN2.matcher(value).matches(),
                "An argument for augment can be only absolute path; or descendant if used in uses");

            return Utils.nodeIdentifierFromPath(ctx, value);
        }

        @Override
        public AugmentStatement createDeclared(
                final StmtContext<SchemaNodeIdentifier, AugmentStatement, ?> ctx) {
            return new AugmentStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<SchemaNodeIdentifier, AugmentStatement> createEffective(
                final StmtContext<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> ctx) {
            return new AugmentEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(
                final StmtContext.Mutable<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> augmentNode) {
            if (!StmtContextUtils.areFeaturesSupported(augmentNode)) {
                return;
            }

            SUBSTATEMENT_VALIDATOR.validate(augmentNode);

            if (StmtContextUtils.isInExtensionBody(augmentNode)) {
                return;
            }

            final ModelActionBuilder augmentAction = augmentNode.newInferenceAction(
                ModelProcessingPhase.EFFECTIVE_MODEL);
            final ModelActionBuilder.Prerequisite<StmtContext<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>>> sourceCtxPrereq =
                    augmentAction.requiresCtx(augmentNode, ModelProcessingPhase.EFFECTIVE_MODEL);
            final Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> target =
                    augmentAction.mutatesEffectiveCtx(getSearchRoot(augmentNode), SchemaNodeIdentifierBuildNamespace.class, augmentNode.getStatementArgument());
            augmentAction.apply(new ModelActionBuilder.InferenceAction() {

                @Override
                public void apply() {
                    final StatementContextBase<?, ?, ?> augmentTargetCtx = (StatementContextBase<?, ?, ?>) target.get();
                    if (!isSupportedAugmentTarget(augmentTargetCtx)
                            || StmtContextUtils.isInExtensionBody(augmentTargetCtx)) {
                        augmentNode.setIsSupportedToBuildEffective(false);
                        return;
                    }
                    /**
                     * Marks case short hand in augment
                     */
                    if (augmentTargetCtx.getPublicDefinition() == Rfc6020Mapping.CHOICE) {
                        augmentNode.addToNs(AugmentToChoiceNamespace.class, augmentNode, Boolean.TRUE);
                    }

                    // FIXME: this is a workaround for models which augment a node which is added via an extension
                    //        which we do not handle. This needs to be reworked in terms of unknown schema nodes.
                    final StatementContextBase<?, ?, ?> augmentSourceCtx = (StatementContextBase<?, ?, ?>) augmentNode;
                    try {
                        copyFromSourceToTarget(augmentSourceCtx, augmentTargetCtx);
                        augmentTargetCtx.addEffectiveSubstatement(augmentSourceCtx);
                        updateAugmentOrder(augmentSourceCtx);
                    } catch (final SourceException e) {
                        LOG.debug("Failed to add augmentation {} defined at {}",
                            augmentTargetCtx.getStatementSourceReference(),
                                augmentSourceCtx.getStatementSourceReference(), e);
                    }
                }

                private void updateAugmentOrder(final StatementContextBase<?, ?, ?> augmentSourceCtx) {
                    Integer currentOrder = augmentSourceCtx.getFromNamespace(StmtOrderingNamespace.class,
                        Rfc6020Mapping.AUGMENT);
                    if (currentOrder == null) {
                        currentOrder = 1;
                    } else {
                        currentOrder++;
                    }

                    augmentSourceCtx.setOrder(currentOrder);
                    augmentSourceCtx.addToNs(StmtOrderingNamespace.class, Rfc6020Mapping.AUGMENT, currentOrder);
                }

                @Override
                public void prerequisiteFailed(final Collection<? extends ModelActionBuilder.Prerequisite<?>> failed) {
                    /*
                     * Do not fail, if it is an uses-augment to an unknown node.
                     */
                    if (Rfc6020Mapping.USES == augmentNode.getParentContext().getPublicDefinition()) {
                        final StatementContextBase<?, ?, ?> targetNode = Utils.findNode(getSearchRoot(augmentNode),
                                augmentNode.getStatementArgument());
                        if (Utils.isUnknownNode(targetNode)) {
                            augmentNode.setIsSupportedToBuildEffective(false);
                            LOG.warn(
                                    "Uses-augment to unknown node {}. Augmentation has not been performed. At line: {}",
                                    augmentNode.getStatementArgument(), augmentNode.getStatementSourceReference());
                            return;
                        }
                    }

                    throw new InferenceException(augmentNode.getStatementSourceReference(),
                            "Augment target '%s' not found", augmentNode.getStatementArgument());
                }
            });
        }

        private static Mutable<?, ?, ?> getSearchRoot(final Mutable<?, ?, ?> augmentContext) {
            final Mutable<?, ?, ?> parent = augmentContext.getParentContext();
            // Augment is in uses - we need to augment instantiated nodes in parent.
            if (Rfc6020Mapping.USES == parent.getPublicDefinition()) {
                return parent.getParentContext();
            }
            return parent;
        }

        public static void copyFromSourceToTarget(final StatementContextBase<?, ?, ?> sourceCtx,
                final StatementContextBase<?, ?, ?> targetCtx) {
            final CopyType typeOfCopy = UsesStatement.class.equals(sourceCtx.getParentContext().getPublicDefinition()
                    .getDeclaredRepresentationClass()) ? CopyType.ADDED_BY_USES_AUGMENTATION
                    : CopyType.ADDED_BY_AUGMENTATION;

            final Collection<StatementContextBase<?, ?, ?>> declared = sourceCtx.declaredSubstatements();
            final Collection<StatementContextBase<?, ?, ?>> effective = sourceCtx.effectiveSubstatements();
            final Collection<StatementContextBase<?, ?, ?>> buffer = new ArrayList<>(declared.size() + effective.size());

            for (final StatementContextBase<?, ?, ?> originalStmtCtx : declared) {
                if (StmtContextUtils.areFeaturesSupported(originalStmtCtx)) {
                    copyStatement(originalStmtCtx, targetCtx, typeOfCopy, buffer);
                }
            }
            for (final StatementContextBase<?, ?, ?> originalStmtCtx : effective) {
                copyStatement(originalStmtCtx, targetCtx, typeOfCopy, buffer);
            }

            targetCtx.addEffectiveSubstatements(buffer);
        }

        private static void copyStatement(final StatementContextBase<?, ?, ?> original,
                final StatementContextBase<?, ?, ?> target, final CopyType typeOfCopy,
                final Collection<StatementContextBase<?, ?, ?>> buffer) {
            if (needToCopyByAugment(original)) {
                validateNodeCanBeCopiedByAugment(original, target, typeOfCopy);

                final StatementContextBase<?, ?, ?> copy = original.createCopy(target, typeOfCopy);
                buffer.add(copy);
            } else if (isReusedByAugment(original)) {
                buffer.add(original);
            }
        }

        private static void validateNodeCanBeCopiedByAugment(final StatementContextBase<?, ?, ?> sourceCtx,
                final StatementContextBase<?, ?, ?> targetCtx, final CopyType typeOfCopy) {

            if (WhenStatement.class.equals(sourceCtx.getPublicDefinition().getDeclaredRepresentationClass())) {
                return;
            }

            if (typeOfCopy == CopyType.ADDED_BY_AUGMENTATION && reguiredCheckOfMandatoryNodes(sourceCtx, targetCtx)) {
                checkForMandatoryNodes(sourceCtx);
            }

            final List<StatementContextBase<?, ?, ?>> targetSubStatements = new Builder<StatementContextBase<?, ?, ?>>()
                    .addAll(targetCtx.declaredSubstatements()).addAll(targetCtx.effectiveSubstatements()).build();

            for (final StatementContextBase<?, ?, ?> subStatement : targetSubStatements) {

                final boolean sourceIsDataNode = DataDefinitionStatement.class.isAssignableFrom(sourceCtx
                        .getPublicDefinition().getDeclaredRepresentationClass());
                final boolean targetIsDataNode = DataDefinitionStatement.class.isAssignableFrom(subStatement
                        .getPublicDefinition().getDeclaredRepresentationClass());
                final boolean qNamesEqual = sourceIsDataNode && targetIsDataNode
                        && Objects.equals(sourceCtx.getStatementArgument(), subStatement.getStatementArgument());

                InferenceException.throwIf(qNamesEqual, sourceCtx.getStatementSourceReference(),
                        "An augment cannot add node named '%s' because this name is already used in target",
                        sourceCtx.rawStatementArgument());
            }
        }

        private static void checkForMandatoryNodes(final StatementContextBase<?, ?, ?> sourceCtx) {
            if (StmtContextUtils.isNonPresenceContainer(sourceCtx)) {
                /*
                 * We need to iterate over both declared and effective sub-statements,
                 * because a mandatory node can be:
                 * a) declared in augment body
                 * b) added to augment body also via uses of a grouping and
                 * such sub-statements are stored in effective sub-statements collection.
                 */
                for (final StatementContextBase<?, ?, ?> sourceSubStatement : Iterables.concat(
                        sourceCtx.declaredSubstatements(), sourceCtx.declaredSubstatements())) {
                    checkForMandatoryNodes(sourceSubStatement);
                }
            }

            InferenceException.throwIf(StmtContextUtils.isMandatoryNode(sourceCtx),
                    sourceCtx.getStatementSourceReference(),
                    "An augment cannot add node '%s' because it is mandatory and in module different than target",
                    sourceCtx.rawStatementArgument());
        }

        private static boolean reguiredCheckOfMandatoryNodes(final StatementContextBase<?, ?, ?> sourceCtx,
                StatementContextBase<?, ?, ?> targetCtx) {
            /*
             * If the statement argument is not QName, it cannot be mandatory
             * statement, therefore return false and skip mandatory nodes validation
             */
            if (!(sourceCtx.getStatementArgument() instanceof QName)) {
                return false;
            }
            final QName sourceStmtQName = (QName) sourceCtx.getStatementArgument();

            final RootStatementContext<?, ?, ?> root = targetCtx.getRoot();
            do {
                Verify.verify(targetCtx.getStatementArgument() instanceof QName,
                        "Argument of augment target statement must be QName.");
                final QName targetStmtQName = (QName) targetCtx.getStatementArgument();
                /*
                 * If target is from another module, return true and perform
                 * mandatory nodes validation
                 */
                if (!Utils.belongsToTheSameModule(targetStmtQName, sourceStmtQName)) {
                    return true;
                }

                /*
                 * If target or one of its parent is a presence container from
                 * the same module, return false and skip mandatory nodes
                 * validation
                 */
                if (StmtContextUtils.isPresenceContainer(targetCtx)) {
                    return false;
                }
            } while ((targetCtx = targetCtx.getParentContext()) != root);

            /*
             * All target node's parents belong to the same module as source node,
             * therefore return false and skip mandatory nodes validation.
             */
            return false;
        }

        private static final Set<Rfc6020Mapping> NOCOPY_DEF_SET = ImmutableSet.of(Rfc6020Mapping.USES, Rfc6020Mapping.WHEN,
                Rfc6020Mapping.DESCRIPTION, Rfc6020Mapping.REFERENCE, Rfc6020Mapping.STATUS);

        public static boolean needToCopyByAugment(final StmtContext<?, ?, ?> stmtContext) {
            return !NOCOPY_DEF_SET.contains(stmtContext.getPublicDefinition());
        }

        private static final Set<Rfc6020Mapping> REUSED_DEF_SET = ImmutableSet.of(Rfc6020Mapping.TYPEDEF);

        public static boolean isReusedByAugment(final StmtContext<?, ?, ?> stmtContext) {
            return REUSED_DEF_SET.contains(stmtContext.getPublicDefinition());
        }

        static boolean isSupportedAugmentTarget(final StatementContextBase<?, ?, ?> substatementCtx) {

            /*
             * :TODO Substatement must be allowed augment target type e.g.
             * Container, etc... and must not be for example grouping, identity etc.
             * It is problem in case when more than one substatements have the same
             * QName, for example Grouping and Container are siblings and they have
             * the same QName. We must find the Container and the Grouping must be
             * ignored as disallowed augment target.
             */

            final Collection<?> allowedAugmentTargets = substatementCtx.getFromNamespace(ValidationBundlesNamespace.class,
                    ValidationBundleType.SUPPORTED_AUGMENT_TARGETS);

            // if no allowed target is returned we consider all targets allowed
            return allowedAugmentTargets == null || allowedAugmentTargets.isEmpty()
                    || allowedAugmentTargets.contains(substatementCtx.getPublicDefinition());
        }

    }

    @Nonnull
    @Override
    public SchemaNodeIdentifier getTargetNode() {
        return argument();
    }

    @Override
    public Collection<? extends DataDefinitionStatement> getDataDefinitions() {
        return allDeclared(DataDefinitionStatement.class);
    }
}
