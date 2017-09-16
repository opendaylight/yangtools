/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModulesDeviatedByModules;
import org.opendaylight.yangtools.yang.parser.spi.source.ModulesDeviatedByModules.SupportedModules;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
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
        private static final Map<String, DeviateKind> KEYWORD_TO_DEVIATE_MAP;

        static {
            final Builder<String, DeviateKind> keywordToDeviateMapBuilder = ImmutableMap.builder();
            for (final DeviateKind deviate : DeviateKind.values()) {
                keywordToDeviateMapBuilder.put(deviate.getKeyword(), deviate);
            }
            KEYWORD_TO_DEVIATE_MAP = keywordToDeviateMapBuilder.build();
        }

        private static final Set<YangStmtMapping> SINGLETON_STATEMENTS = ImmutableSet.of(
                YangStmtMapping.UNITS, YangStmtMapping.CONFIG, YangStmtMapping.MANDATORY,
                YangStmtMapping.MIN_ELEMENTS, YangStmtMapping.MAX_ELEMENTS);

        public Definition() {
            super(YangStmtMapping.DEVIATE);
        }

        @Override
        public DeviateKind parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return SourceException.throwIfNull(KEYWORD_TO_DEVIATE_MAP.get(value),
                ctx.getStatementSourceReference(), "String '%s' is not valid deviate argument", value);
        }

        @Override
        public DeviateStatement createDeclared(final StmtContext<DeviateKind, DeviateStatement, ?> ctx) {
            return new DeviateStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<DeviateKind, DeviateStatement> createEffective(
                final StmtContext<DeviateKind, DeviateStatement, EffectiveStatement<DeviateKind,
                        DeviateStatement>> ctx) {
            return new DeviateEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final Mutable<DeviateKind, DeviateStatement,
                EffectiveStatement<DeviateKind, DeviateStatement>> deviateStmtCtx) {
            final DeviateKind deviateKind = deviateStmtCtx.getStatementArgument();
            getSubstatementValidatorForDeviate(deviateKind).validate(deviateStmtCtx);

            final SchemaNodeIdentifier deviationTarget =
                    (SchemaNodeIdentifier) deviateStmtCtx.getParentContext().getStatementArgument();

            if (!isDeviationSupported(deviateStmtCtx, deviationTarget)) {
                return;
            }

            final ModelActionBuilder deviateAction = deviateStmtCtx.newInferenceAction(
                    ModelProcessingPhase.EFFECTIVE_MODEL);

            final Prerequisite<StmtContext<DeviateKind, DeviateStatement, EffectiveStatement<DeviateKind,
                    DeviateStatement>>> sourceCtxPrerequisite =
                    deviateAction.requiresCtx(deviateStmtCtx, ModelProcessingPhase.EFFECTIVE_MODEL);

            final Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> targetCtxPrerequisite =
                    deviateAction.mutatesEffectiveCtx(deviateStmtCtx.getRoot(),
                        SchemaNodeIdentifierBuildNamespace.class,  deviationTarget);

            deviateAction.apply(new InferenceAction() {
                @Override
                public void apply(final InferenceContext ctx) throws InferenceException {
                    // FIXME once BUG-7760 gets fixed, there will be no need for these dirty casts
                    final StatementContextBase<?, ?, ?> sourceNodeStmtCtx =
                            (StatementContextBase<?, ?, ?>) sourceCtxPrerequisite.resolve(ctx);
                    final StatementContextBase<?, ?, ?> targetNodeStmtCtx =
                            (StatementContextBase<?, ?, ?>) targetCtxPrerequisite.resolve(ctx);

                    switch (deviateKind) {
                        case NOT_SUPPORTED:
                            targetNodeStmtCtx.setIsSupportedToBuildEffective(false);
                            break;
                        case ADD:
                            performDeviateAdd(sourceNodeStmtCtx, targetNodeStmtCtx);
                            break;
                        case REPLACE:
                            performDeviateReplace(sourceNodeStmtCtx, targetNodeStmtCtx);
                            break;
                        case DELETE:
                            performDeviateDelete(sourceNodeStmtCtx, targetNodeStmtCtx);
                    }
                }

                @Override
                public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                    throw new InferenceException(deviateStmtCtx.getParentContext().getStatementSourceReference(),
                        "Deviation target '%s' not found.", deviationTarget);
                }
            });
        }

        private static boolean isDeviationSupported(final Mutable<DeviateKind, DeviateStatement,
                EffectiveStatement<DeviateKind, DeviateStatement>> deviateStmtCtx,
                final SchemaNodeIdentifier deviationTarget) {
            final Map<QNameModule, Set<QNameModule>> modulesDeviatedByModules = deviateStmtCtx.getFromNamespace(
                    ModulesDeviatedByModules.class, SupportedModules.SUPPORTED_MODULES);
            if (modulesDeviatedByModules == null) {
                return true;
            }

            final QNameModule currentModule = deviateStmtCtx.getFromNamespace(ModuleCtxToModuleQName.class,
                    deviateStmtCtx.getRoot());
            final QNameModule targetModule = deviationTarget.getLastComponent().getModule();

            final Set<QNameModule> deviationModulesSupportedByTargetModule = modulesDeviatedByModules.get(targetModule);
            if (deviationModulesSupportedByTargetModule != null) {
                return deviationModulesSupportedByTargetModule.contains(currentModule);
            }

            return false;
        }

        private static void performDeviateAdd(final StatementContextBase<?, ?, ?> deviateStmtCtx,
                final StatementContextBase<?, ?, ?> targetCtx) {
            for (Mutable<?, ?, ?> originalStmtCtx : deviateStmtCtx.mutableDeclaredSubstatements()) {
                validateDeviationTarget(originalStmtCtx, targetCtx);
                addStatement(originalStmtCtx, targetCtx);
            }
        }

        private static void addStatement(final Mutable<?, ?, ?> stmtCtxToBeAdded,
                final StatementContextBase<?, ?, ?> targetCtx) {
            if (!StmtContextUtils.isUnknownStatement(stmtCtxToBeAdded)) {
                final StatementDefinition stmtToBeAdded = stmtCtxToBeAdded.getPublicDefinition();
                if (SINGLETON_STATEMENTS.contains(stmtToBeAdded) || YangStmtMapping.DEFAULT.equals(stmtToBeAdded)
                        && YangStmtMapping.LEAF.equals(targetCtx.getPublicDefinition())) {
                    for (final StmtContext<?, ?, ?> targetCtxSubstatement : targetCtx.allSubstatements()) {
                        InferenceException.throwIf(stmtToBeAdded.equals(targetCtxSubstatement.getPublicDefinition()),
                            stmtCtxToBeAdded.getStatementSourceReference(),
                            "Deviation cannot add substatement %s to target node %s because it is already defined "
                            + "in target and can appear only once.",
                            stmtToBeAdded.getStatementName(), targetCtx.getStatementArgument());
                    }
                }
            }

            targetCtx.addEffectiveSubstatement(targetCtx.childCopyOf(stmtCtxToBeAdded, CopyType.ORIGINAL));
        }

        private static void performDeviateReplace(final StatementContextBase<?, ?, ?> deviateStmtCtx,
                final StatementContextBase<?, ?, ?> targetCtx) {
            for (Mutable<?, ?, ?> originalStmtCtx : deviateStmtCtx.mutableDeclaredSubstatements()) {
                validateDeviationTarget(originalStmtCtx, targetCtx);
                replaceStatement(originalStmtCtx, targetCtx);
            }
        }

        private static void replaceStatement(final Mutable<?, ?, ?> stmtCtxToBeReplaced,
                final StatementContextBase<?, ?, ?> targetCtx) {
            final StatementDefinition stmtToBeReplaced = stmtCtxToBeReplaced.getPublicDefinition();

            if (YangStmtMapping.DEFAULT.equals(stmtToBeReplaced)
                    && YangStmtMapping.LEAF_LIST.equals(targetCtx.getPublicDefinition())) {
                LOG.error("Deviation cannot replace substatement {} in target leaf-list {} because a leaf-list can "
                        + "have multiple default statements. At line: {}", stmtToBeReplaced.getStatementName(),
                        targetCtx.getStatementArgument(), stmtCtxToBeReplaced.getStatementSourceReference());
                return;
            }

            for (final StmtContext<?, ?, ?> targetCtxSubstatement : targetCtx.effectiveSubstatements()) {
                if (stmtToBeReplaced.equals(targetCtxSubstatement.getPublicDefinition())) {
                    targetCtx.removeStatementFromEffectiveSubstatements(stmtToBeReplaced);
                    targetCtx.addEffectiveSubstatement(targetCtx.childCopyOf(stmtCtxToBeReplaced, CopyType.ORIGINAL));
                    return;
                }
            }

            for (final Mutable<?, ?, ?> targetCtxSubstatement : targetCtx.mutableDeclaredSubstatements()) {
                if (stmtToBeReplaced.equals(targetCtxSubstatement.getPublicDefinition())) {
                    targetCtxSubstatement.setIsSupportedToBuildEffective(false);
                    targetCtx.addEffectiveSubstatement(targetCtx.childCopyOf(stmtCtxToBeReplaced, CopyType.ORIGINAL));
                    return;
                }
            }

            throw new InferenceException(stmtCtxToBeReplaced.getStatementSourceReference(), "Deviation cannot replace "
                    + "substatement %s in target node %s because it does not exist in target node.",
                    stmtToBeReplaced.getStatementName(), targetCtx.getStatementArgument());
        }

        private static void performDeviateDelete(final StatementContextBase<?, ?, ?> deviateStmtCtx,
                final StatementContextBase<?, ?, ?> targetCtx) {
            for (Mutable<?, ?, ?> originalStmtCtx : deviateStmtCtx.mutableDeclaredSubstatements()) {
                validateDeviationTarget(originalStmtCtx, targetCtx);
                deleteStatement(originalStmtCtx, targetCtx);
            }
        }

        private static void deleteStatement(final StmtContext<?, ?, ?> stmtCtxToBeDeleted,
                final StatementContextBase<?, ?, ?> targetCtx) {
            final StatementDefinition stmtToBeDeleted = stmtCtxToBeDeleted.getPublicDefinition();
            final String stmtArgument = stmtCtxToBeDeleted.rawStatementArgument();

            for (final Mutable<?, ?, ?> targetCtxSubstatement : targetCtx.mutableEffectiveSubstatements()) {
                if (statementsAreEqual(stmtToBeDeleted, stmtArgument, targetCtxSubstatement.getPublicDefinition(),
                        targetCtxSubstatement.rawStatementArgument())) {
                    targetCtx.removeStatementFromEffectiveSubstatements(stmtToBeDeleted, stmtArgument);
                    return;
                }
            }

            for (final Mutable<?, ?, ?> targetCtxSubstatement : targetCtx.mutableDeclaredSubstatements()) {
                if (statementsAreEqual(stmtToBeDeleted, stmtArgument, targetCtxSubstatement.getPublicDefinition(),
                        targetCtxSubstatement.rawStatementArgument())) {
                    targetCtxSubstatement.setIsSupportedToBuildEffective(false);
                    return;
                }
            }

            LOG.error("Deviation cannot delete substatement {} with argument '{}' in target node {} because it does "
                    + "not exist in the target node. At line: {}", stmtToBeDeleted.getStatementName(), stmtArgument,
                    targetCtx.getStatementArgument(), stmtCtxToBeDeleted.getStatementSourceReference());
        }

        private static boolean statementsAreEqual(final StatementDefinition firstStmtDef, final String firstStmtArg,
                final StatementDefinition secondStmtDef, final String secondStmtArg) {
            return firstStmtDef.equals(secondStmtDef) && Objects.equals(firstStmtArg, secondStmtArg);
        }

        private static void validateDeviationTarget(final StmtContext<?, ?, ?> deviateSubStmtCtx,
                final StmtContext<?, ?, ?> targetCtx) {
            InferenceException.throwIf(!isSupportedDeviationTarget(deviateSubStmtCtx, targetCtx,
                    targetCtx.getRootVersion()), deviateSubStmtCtx.getStatementSourceReference(),
                    "%s is not a valid deviation target for substatement %s.",
                    targetCtx.getStatementArgument(), deviateSubStmtCtx.getPublicDefinition().getStatementName());
        }

        private static boolean isSupportedDeviationTarget(final StmtContext<?, ?, ?> deviateSubstatementCtx,
                final StmtContext<?, ?, ?> deviateTargetCtx, final YangVersion yangVersion) {
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

    @Nonnull
    @Override
    public DeviateKind getValue() {
        return argument();
    }
}
