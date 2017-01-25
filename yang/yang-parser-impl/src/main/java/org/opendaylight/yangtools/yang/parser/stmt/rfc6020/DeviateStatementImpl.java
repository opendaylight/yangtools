/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeviateEffectiveStatementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviateStatementImpl extends AbstractDeclaredStatement<DeviateKind> implements DeviateStatement {
    private static final Logger LOG = LoggerFactory.getLogger(DeviateStatementImpl.class);

    private static final SubstatementValidator DEVIATE_NOT_SUPPORTED_SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.DEVIATE).build();

    private static final SubstatementValidator DEVIATE_ADD_SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.DEVIATE)
                .addOptional(YangStmtMapping.CONFIG)
                .addOptional(YangStmtMapping.DEFAULT)
                .addOptional(YangStmtMapping.MANDATORY)
                .addOptional(YangStmtMapping.MAX_ELEMENTS)
                .addOptional(YangStmtMapping.MIN_ELEMENTS)
                .addAny(YangStmtMapping.MUST)
                .addAny(YangStmtMapping.UNIQUE)
                .addOptional(YangStmtMapping.UNITS)
                .build();

    private static final SubstatementValidator DEVIATE_REPLACE_SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.DEVIATE)
                .addOptional(YangStmtMapping.CONFIG)
                .addOptional(YangStmtMapping.DEFAULT)
                .addOptional(YangStmtMapping.MANDATORY)
                .addOptional(YangStmtMapping.MAX_ELEMENTS)
                .addOptional(YangStmtMapping.MIN_ELEMENTS)
                .addOptional(YangStmtMapping.TYPE)
                .addOptional(YangStmtMapping.UNITS)
                .build();

    private static final SubstatementValidator DEVIATE_DELETE_SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.DEVIATE)
                .addOptional(YangStmtMapping.DEFAULT)
                .addAny(YangStmtMapping.MUST)
                .addAny(YangStmtMapping.UNIQUE)
                .addOptional(YangStmtMapping.UNITS)
                .build();

    protected DeviateStatementImpl(final StmtContext<DeviateKind, DeviateStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<DeviateKind, DeviateStatement,
            EffectiveStatement<DeviateKind, DeviateStatement>> {

        private static final Set<YangStmtMapping> SINGLETON_STATEMENTS = ImmutableSet.of(
                YangStmtMapping.UNITS, YangStmtMapping.CONFIG, YangStmtMapping.MANDATORY,
                YangStmtMapping.MIN_ELEMENTS, YangStmtMapping.MAX_ELEMENTS);

        public Definition() {
            super(YangStmtMapping.DEVIATE);
        }

        @Override public DeviateKind parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return Utils.parseDeviateFromString(ctx, value);
        }

        @Override public DeviateStatement createDeclared(final StmtContext<DeviateKind, DeviateStatement, ?> ctx) {
            return new DeviateStatementImpl(ctx);
        }

        @Override public EffectiveStatement<DeviateKind, DeviateStatement> createEffective(
                final StmtContext<DeviateKind, DeviateStatement, EffectiveStatement<DeviateKind,
                        DeviateStatement>> ctx) {
            return new DeviateEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final StmtContext.Mutable<DeviateKind, DeviateStatement,
                EffectiveStatement<DeviateKind, DeviateStatement>> deviateStmtCtx) {
            final DeviateKind deviateKind = deviateStmtCtx.getStatementArgument();
            getSubstatementValidatorForDeviate(deviateKind).validate(deviateStmtCtx);

            final ModelActionBuilder deviateAction = deviateStmtCtx.newInferenceAction(
                    ModelProcessingPhase.EFFECTIVE_MODEL);

            final SchemaNodeIdentifier deviationTarget =
                    (SchemaNodeIdentifier) deviateStmtCtx.getParentContext().getStatementArgument();

            final Prerequisite<StmtContext<DeviateKind, DeviateStatement, EffectiveStatement<DeviateKind,
                    DeviateStatement>>> sourceCtxPrerequisite =
                    deviateAction.requiresCtx(deviateStmtCtx, ModelProcessingPhase.EFFECTIVE_MODEL);

            final Prerequisite<StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>>> targetCtxPrerequisite =
                    (Prerequisite<StmtContext.Mutable<?, ?, EffectiveStatement<?, ?>>>) deviateAction
                    .mutatesEffectiveCtx(deviateStmtCtx.getRoot(), SchemaNodeIdentifierBuildNamespace.class,
                            deviationTarget);

                    deviateAction.apply(new InferenceAction() {
                        @Override
                        public void apply() throws InferenceException {
                            final StatementContextBase<?, ?, ?> sourceNodeStmtCtx =
                                    (StatementContextBase<?, ?, ?>) sourceCtxPrerequisite.get();
                            final StatementContextBase<?, ?, ?> targetNodeStmtCtx =
                                    (StatementContextBase<?, ?, ?>) targetCtxPrerequisite.get();

                            if (deviateKind == DeviateKind.NOT_SUPPORTED) {
                                targetNodeStmtCtx.setIsSupportedToBuildEffective(false);
                            } else if (deviateKind == DeviateKind.ADD) {
                                performDeviateAdd(sourceNodeStmtCtx, targetNodeStmtCtx);
                            } else if (deviateKind == DeviateKind.REPLACE) {
                                performDeviateReplace(sourceNodeStmtCtx, targetNodeStmtCtx);
                            } else {
                                performDeviateDelete(sourceNodeStmtCtx, targetNodeStmtCtx);
                            }
                        }

                        @Override
                        public void prerequisiteFailed(Collection<? extends Prerequisite<?>> failed) {
                            throw new InferenceException(deviateStmtCtx.getParentContext().getStatementSourceReference(),
                                    "Deviation target '%s' not found.", deviationTarget);
                        }
                    });
        }

        private static void performDeviateAdd(final StatementContextBase<?, ?, ?> deviateStmtCtx,
                final StatementContextBase<?, ?, ?> targetCtx) {
            for (StatementContextBase<?, ?, ?> originalStmtCtx : deviateStmtCtx.declaredSubstatements()) {
                InferenceException.throwIf(!isSupportedDeviationTarget(originalStmtCtx, targetCtx,
                        targetCtx.getRootVersion()), originalStmtCtx.getStatementSourceReference(),
                        "%s is not a valid deviation target for substatement %s.",
                        targetCtx.getStatementArgument(), originalStmtCtx.getPublicDefinition().getStatementName());

                addStatement(originalStmtCtx, targetCtx);
            }
        }

        private static void addStatement(final StatementContextBase<?, ?, ?> stmtCtxToBeAdded,
                final StatementContextBase<?, ?, ?> targetCtx) {
            if (StmtContextUtils.isUnknownStatement(stmtCtxToBeAdded)) {
                targetCtx.addEffectiveSubstatement(stmtCtxToBeAdded.createCopy(targetCtx, CopyType.ORIGINAL));
                return;
            }

            final StatementDefinition stmtToBeAdded = stmtCtxToBeAdded.getPublicDefinition();
            final Iterable<StatementContextBase<?, ?, ?>> targetCtxSubstatements = Iterables.concat(
                    targetCtx.declaredSubstatements(), targetCtx.effectiveSubstatements());

            if (SINGLETON_STATEMENTS.contains(stmtToBeAdded) || (stmtToBeAdded.equals(YangStmtMapping.DEFAULT)
                    && targetCtx.getPublicDefinition().equals(YangStmtMapping.LEAF))) {
                for (final StatementContextBase<?, ?, ?> targetCtxSubstatement : targetCtxSubstatements) {
                    InferenceException.throwIf(stmtToBeAdded.equals(targetCtxSubstatement.getPublicDefinition()),
                            stmtCtxToBeAdded.getStatementSourceReference(), "Deviation cannot add substatement %s " +
                            "to target node %s because it is already defined in target and can appear only once.",
                            stmtToBeAdded.getStatementName(), targetCtx.getStatementArgument());
                }
            }

            targetCtx.addEffectiveSubstatement(stmtCtxToBeAdded.createCopy(targetCtx, CopyType.ORIGINAL));
        }

        private static void performDeviateReplace(final StatementContextBase<?, ?, ?> deviateStmtCtx,
                final StatementContextBase<?, ?, ?> targetCtx) {
            for (StatementContextBase<?, ?, ?> originalStmtCtx : deviateStmtCtx.declaredSubstatements()) {
                InferenceException.throwIf(!isSupportedDeviationTarget(originalStmtCtx, targetCtx,
                        targetCtx.getRootVersion()), originalStmtCtx.getStatementSourceReference(),
                        "%s is not a valid deviation target for substatement %s.",
                        targetCtx.getStatementArgument(), originalStmtCtx.getStatementArgument());

                replaceStatement(originalStmtCtx, targetCtx);
            }
        }

        private static void replaceStatement(final StatementContextBase<?, ?, ?> stmtCtxToBeReplaced,
                final StatementContextBase<?, ?, ?> targetCtx) {
            final StatementDefinition stmtToBeReplaced = stmtCtxToBeReplaced.getPublicDefinition();

            if (stmtToBeReplaced.equals(YangStmtMapping.DEFAULT)
                    && targetCtx.getPublicDefinition().equals(YangStmtMapping.LEAF_LIST)) {
                LOG.error("Deviation cannot replace substatement {} in target leaf-list {} because a leaf-list can " +
                        "have multiple default statements. At line: {}", stmtToBeReplaced.getStatementName(),
                        targetCtx.getStatementArgument(), stmtCtxToBeReplaced.getStatementSourceReference());
                return;
            }

            for (final StatementContextBase<?, ?, ?> targetCtxSubstatement : targetCtx.effectiveSubstatements()) {
                if (stmtToBeReplaced.equals(targetCtxSubstatement.getPublicDefinition())) {
                    targetCtx.removeStatementFromEffectiveSubstatements(stmtToBeReplaced);
                    targetCtx.addEffectiveSubstatement(stmtCtxToBeReplaced.createCopy(targetCtx, CopyType.ORIGINAL));
                    return;
                }
            }

            for (final StatementContextBase<?, ?, ?> targetCtxSubstatement : targetCtx.declaredSubstatements()) {
                if (stmtToBeReplaced.equals(targetCtxSubstatement.getPublicDefinition())) {
                    targetCtxSubstatement.setIsSupportedToBuildEffective(false);
                    targetCtx.addEffectiveSubstatement(stmtCtxToBeReplaced.createCopy(targetCtx, CopyType.ORIGINAL));
                    return;
                }
            }

            throw new InferenceException(stmtCtxToBeReplaced.getStatementSourceReference(), "Deviation cannot " +
                    "replace substatement %s in target node %s because it does not exist in target node.",
                    stmtToBeReplaced.getStatementName(), targetCtx.getStatementArgument());
        }

        private static void performDeviateDelete(final StatementContextBase<?, ?, ?> deviateStmtCtx,
                final StatementContextBase<?, ?, ?> targetCtx) {
            for (StatementContextBase<?, ?, ?> originalStmtCtx : deviateStmtCtx.declaredSubstatements()) {
                InferenceException.throwIf(!isSupportedDeviationTarget(originalStmtCtx, targetCtx,
                        targetCtx.getRootVersion()), originalStmtCtx.getStatementSourceReference(),
                        "%s is not a valid deviation target for substatement %s.",
                        targetCtx.getStatementArgument(), originalStmtCtx.getStatementArgument());

                deleteStatement(originalStmtCtx, targetCtx);
            }
        }

        private static void deleteStatement(final StatementContextBase<?, ?, ?> stmtCtxToBeDeleted,
                final StatementContextBase<?, ?, ?> targetCtx) {
            final StatementDefinition stmtToBeDeleted = stmtCtxToBeDeleted.getPublicDefinition();
            final String stmtArgument = stmtCtxToBeDeleted.rawStatementArgument();

            for (final StatementContextBase<?, ?, ?> targetCtxSubstatement : targetCtx.effectiveSubstatements()) {
                if (statementsAreEqual(stmtToBeDeleted, stmtArgument, targetCtxSubstatement.getPublicDefinition(),
                        targetCtxSubstatement.rawStatementArgument())) {
                    targetCtx.removeStatementFromEffectiveSubstatements(stmtToBeDeleted, stmtArgument);
                    return;
                }
            }

            for (final StatementContextBase<?, ?, ?> targetCtxSubstatement : targetCtx.declaredSubstatements()) {
                if (statementsAreEqual(stmtToBeDeleted, stmtArgument, targetCtxSubstatement.getPublicDefinition(),
                        targetCtxSubstatement.rawStatementArgument())) {
                    targetCtxSubstatement.setIsSupportedToBuildEffective(false);
                    return;
                }
            }

            LOG.error("Deviation cannot delete substatement {} with argument '{}' in target node {} because it does " +
                    "not exist in the target node. At line: {}", stmtToBeDeleted.getStatementName(), stmtArgument,
                    targetCtx.getStatementArgument(), stmtCtxToBeDeleted.getStatementSourceReference());
        }

        private static boolean statementsAreEqual(final StatementDefinition firstStmtDef, final String firstStmtArg,
                final StatementDefinition secondStmtDef, final String secondStmtArg) {
            return Objects.equals(firstStmtDef, secondStmtDef) && Objects.equals(firstStmtArg, secondStmtArg);
        }

        private static boolean isSupportedDeviationTarget(final StatementContextBase<?, ?, ?> deviateSubstatementCtx,
                final StatementContextBase<?, ?, ?> deviateTargetCtx, final YangVersion yangVersion) {
            Set<StatementDefinition> supportedDeviationTargets =
                    YangValidationBundles.SUPPORTED_DEVIATION_TARGETS.get(deviateTargetCtx.getRootVersion(),
                            deviateSubstatementCtx.getPublicDefinition());

            if (supportedDeviationTargets == null) {
                supportedDeviationTargets = YangValidationBundles.SUPPORTED_DEVIATION_TARGETS.get(YangVersion.VERSION_1,
                        deviateSubstatementCtx.getPublicDefinition());
            }

            // if supportedDeviationTargets is null, it means that the deviate substatement is an unknown statement
            return supportedDeviationTargets == null || supportedDeviationTargets.contains(
                    deviateTargetCtx.getPublicDefinition());
        }

        protected SubstatementValidator getSubstatementValidatorForDeviate(final DeviateKind deviateKind) {
            switch (deviateKind) {
                case NOT_SUPPORTED:
                    return DEVIATE_NOT_SUPPORTED_SUBSTATEMENT_VALIDATOR;
                case ADD:
                    return DEVIATE_ADD_SUBSTATEMENT_VALIDATOR;
                case REPLACE:
                    return DEVIATE_REPLACE_SUBSTATEMENT_VALIDATOR;
                case DELETE:
                    return DEVIATE_DELETE_SUBSTATEMENT_VALIDATOR;
                default:
                    throw new IllegalStateException(String.format(
                            "Substatement validator for deviate %s has not been defined.", deviateKind));
            }
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return null;
        }
    }

    @Nonnull @Override
    public DeviateKind getValue() {
        return argument();
    }
}
