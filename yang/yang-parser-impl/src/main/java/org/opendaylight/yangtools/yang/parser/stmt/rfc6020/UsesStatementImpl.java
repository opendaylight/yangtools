/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
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
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UsesEffectiveStatementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsesStatementImpl extends AbstractDeclaredStatement<QName> implements UsesStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .USES)
            .addAny(YangStmtMapping.AUGMENT)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addAny(YangStmtMapping.REFINE)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addOptional(YangStmtMapping.WHEN)
            .build();

    private static final Logger LOG = LoggerFactory.getLogger(UsesStatementImpl.class);

    protected UsesStatementImpl(final StmtContext<QName, UsesStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> {

        public Definition() {
            super(YangStmtMapping.USES);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return StmtContextUtils.qnameFromArgument(ctx, value);
        }

        @Override
        public void onFullDefinitionDeclared(
                final Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode) {
            if (!usesNode.isSupportedByFeatures()) {
                return;
            }
            super.onFullDefinitionDeclared(usesNode);

            final ModelActionBuilder usesAction = usesNode.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
            final QName groupingName = usesNode.getStatementArgument();

            final Prerequisite<StmtContext<?, ?, ?>> sourceGroupingPre = usesAction.requiresCtx(usesNode,
                    GroupingNamespace.class, groupingName, ModelProcessingPhase.EFFECTIVE_MODEL);
            final Prerequisite<? extends StmtContext.Mutable<?, ?, ?>> targetNodePre = usesAction.mutatesEffectiveCtx(
                    usesNode.getParentContext());

            usesAction.apply(new InferenceAction() {

                @Override
                public void apply(final InferenceContext ctx) {
                    final StatementContextBase<?, ?, ?> targetNodeStmtCtx = (StatementContextBase<?, ?, ?>) targetNodePre.resolve(ctx);
                    final StatementContextBase<?, ?, ?> sourceGrpStmtCtx = (StatementContextBase<?, ?, ?>) sourceGroupingPre.resolve(ctx);

                    try {
                        copyFromSourceToTarget(sourceGrpStmtCtx, targetNodeStmtCtx, usesNode);
                        resolveUsesNode(usesNode, targetNodeStmtCtx);
                        StmtContextUtils.validateIfFeatureAndWhenOnListKeys(usesNode);
                    } catch (final SourceException e) {
                        LOG.warn(e.getMessage(), e);
                        throw e;
                    }
                }

                @Override
                public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                    InferenceException.throwIf(failed.contains(sourceGroupingPre),
                            usesNode.getStatementSourceReference(), "Grouping '%s' was not resolved.", groupingName);
                    throw new InferenceException("Unknown error occurred.", usesNode.getStatementSourceReference());
                }
            });
        }

        @Override
        public UsesStatement createDeclared(final StmtContext<QName, UsesStatement, ?> ctx) {
            return new UsesStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName, UsesStatement> createEffective(
                final StmtContext<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> ctx) {
            return new UsesEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }

    }

    @Nonnull
    @Override
    public QName getName() {
        return argument();
    }

    @Override
    public WhenStatement getWhenStatement() {
        return firstDeclared(WhenStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends IfFeatureStatement> getIfFeatures() {
        return allDeclared(IfFeatureStatement.class);
    }

    @Override
    public StatusStatement getStatus() {
        return firstDeclared(StatusStatement.class);
    }

    @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends AugmentStatement> getAugments() {
        return allDeclared(AugmentStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends RefineStatement> getRefines() {
        return allDeclared(RefineStatement.class);
    }

    /**
     * @param sourceGrpStmtCtx
     *            source grouping statement context
     * @param targetCtx
     *            target context
     * @param usesNode
     *            uses node
     * @throws SourceException
     *             instance of SourceException
     */
    private static void copyFromSourceToTarget(final Mutable<?, ?, ?> sourceGrpStmtCtx,
            final StatementContextBase<?, ?, ?> targetCtx,
            final StmtContext.Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode) {
        final Collection<? extends Mutable<?, ?, ?>> declared = sourceGrpStmtCtx.mutableDeclaredSubstatements();
        final Collection<? extends Mutable<?, ?, ?>> effective = sourceGrpStmtCtx.mutableEffectiveSubstatements();
        final Collection<Mutable<?, ?, ?>> buffer = new ArrayList<>(declared.size() + effective.size());
        final QNameModule newQNameModule = getNewQNameModule(targetCtx, sourceGrpStmtCtx);

        for (final Mutable<?, ?, ?> original : declared) {
            if (original.isSupportedByFeatures()) {
                copyStatement(original, targetCtx, newQNameModule, buffer);
            }
        }

        for (final Mutable<?, ?, ?> original : effective) {
            copyStatement(original, targetCtx, newQNameModule, buffer);
        }

        targetCtx.addEffectiveSubstatements(buffer);
        usesNode.addAsEffectOfStatement(buffer);
    }

    private static void copyStatement(final Mutable<?, ?, ?> original,
            final StatementContextBase<?, ?, ?> targetCtx, final QNameModule targetModule,
            final Collection<Mutable<?, ?, ?>> buffer) {
        if (needToCopyByUses(original)) {
            final Mutable<?, ?, ?> copy = targetCtx.childCopyOf(original, CopyType.ADDED_BY_USES, targetModule);
            buffer.add(copy);
        } else if (isReusedByUsesOnTop(original)) {
            buffer.add(original);
        }
    }

    private static final Set<YangStmtMapping> TOP_REUSED_DEF_SET = ImmutableSet.of(
        YangStmtMapping.TYPE,
        YangStmtMapping.TYPEDEF);

    private static boolean isReusedByUsesOnTop(final StmtContext<?, ?, ?> stmtContext) {
        return TOP_REUSED_DEF_SET.contains(stmtContext.getPublicDefinition());
    }

    private static final Set<YangStmtMapping> NOCOPY_FROM_GROUPING_SET = ImmutableSet.of(
        YangStmtMapping.DESCRIPTION,
        YangStmtMapping.REFERENCE,
        YangStmtMapping.STATUS);
    private static final Set<YangStmtMapping> REUSED_DEF_SET = ImmutableSet.of(
        YangStmtMapping.TYPE,
        YangStmtMapping.TYPEDEF,
        YangStmtMapping.USES);

    public static boolean needToCopyByUses(final StmtContext<?, ?, ?> stmtContext) {
        final StatementDefinition def = stmtContext.getPublicDefinition();
        if (REUSED_DEF_SET.contains(def)) {
            LOG.debug("Will reuse {} statement {}", def, stmtContext);
            return false;
        }
        if (NOCOPY_FROM_GROUPING_SET.contains(def)) {
            return !YangStmtMapping.GROUPING.equals(stmtContext.getParentContext().getPublicDefinition());
        }

        LOG.debug("Will copy {} statement {}", def, stmtContext);
        return true;
    }

    public static void resolveUsesNode(
            final Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode,
            final StatementContextBase<?, ?, ?> targetNodeStmtCtx) {
        for (final Mutable<?, ?, ?> subStmtCtx : usesNode.mutableDeclaredSubstatements()) {
            if (StmtContextUtils.producesDeclared(subStmtCtx, RefineStatement.class)
                    && areFeaturesSupported(subStmtCtx)) {
                performRefine(subStmtCtx, targetNodeStmtCtx);
            }
        }
    }

    private static boolean areFeaturesSupported(final Mutable<?, ?, ?> subStmtCtx) {
        /*
         * In case of Yang 1.1, checks whether features are supported.
         */
        return !YangVersion.VERSION_1_1.equals(subStmtCtx.getRootVersion()) || subStmtCtx.isSupportedByFeatures();
    }

    private static void performRefine(final Mutable<?, ?, ?> subStmtCtx,
            final StatementContextBase<?, ?, ?> usesParentCtx) {

        final Object refineArgument = subStmtCtx.getStatementArgument();
        InferenceException.throwIf(!(refineArgument instanceof SchemaNodeIdentifier),
            subStmtCtx.getStatementSourceReference(),
            "Invalid refine argument %s. It must be instance of SchemaNodeIdentifier.", refineArgument);

        final SchemaNodeIdentifier refineTargetNodeIdentifier = (SchemaNodeIdentifier) refineArgument;
        final StatementContextBase<?, ?, ?> refineTargetNodeCtx = Utils.findNode(usesParentCtx,
                refineTargetNodeIdentifier);

        InferenceException.throwIfNull(refineTargetNodeCtx, subStmtCtx.getStatementSourceReference(),
            "Refine target node %s not found.", refineTargetNodeIdentifier);

        if (StmtContextUtils.isUnknownStatement(refineTargetNodeCtx)) {
            LOG.debug(
                    "Refine node '{}' in uses '{}' has target node unknown statement '{}'. Refine has been skipped. At line: {}",
                    subStmtCtx.getStatementArgument(), subStmtCtx.getParentContext().getStatementArgument(),
                    refineTargetNodeCtx.getStatementArgument(), subStmtCtx.getStatementSourceReference());
            subStmtCtx.addAsEffectOfStatement(refineTargetNodeCtx);
            return;
        }

        addOrReplaceNodes(subStmtCtx, refineTargetNodeCtx);
        subStmtCtx.addAsEffectOfStatement(refineTargetNodeCtx);
    }

    private static void addOrReplaceNodes(final Mutable<?, ?, ?> subStmtCtx,
            final StatementContextBase<?, ?, ?> refineTargetNodeCtx) {
        for (final Mutable<?, ?, ?> refineSubstatementCtx : subStmtCtx.mutableDeclaredSubstatements()) {
            if (isSupportedRefineSubstatement(refineSubstatementCtx)) {
                addOrReplaceNode(refineSubstatementCtx, refineTargetNodeCtx);
            }
        }
    }

    private static void addOrReplaceNode(final Mutable<?, ?, ?> refineSubstatementCtx,
            final StatementContextBase<?, ?, ?> refineTargetNodeCtx) {

        final StatementDefinition refineSubstatementDef = refineSubstatementCtx.getPublicDefinition();

        SourceException.throwIf(!isSupportedRefineTarget(refineSubstatementCtx, refineTargetNodeCtx),
                refineSubstatementCtx.getStatementSourceReference(),
                "Error in module '%s' in the refine of uses '%s': can not perform refine of '%s' for the target '%s'.",
                refineSubstatementCtx.getRoot().getStatementArgument(), refineSubstatementCtx.getParentContext()
                        .getStatementArgument(), refineSubstatementCtx.getPublicDefinition(), refineTargetNodeCtx
                        .getPublicDefinition());

        if (isAllowedToAddByRefine(refineSubstatementDef)) {
            refineTargetNodeCtx.addEffectiveSubstatement(refineSubstatementCtx);
        } else {
            refineTargetNodeCtx.removeStatementFromEffectiveSubstatements(refineSubstatementDef);
            refineTargetNodeCtx.addEffectiveSubstatement(refineSubstatementCtx);
        }
    }

    private static final Set<YangStmtMapping> ALLOWED_TO_ADD_BY_REFINE_DEF_SET = ImmutableSet.of(YangStmtMapping.MUST);

    private static boolean isAllowedToAddByRefine(final StatementDefinition publicDefinition) {
        return ALLOWED_TO_ADD_BY_REFINE_DEF_SET.contains(publicDefinition);
    }

    private static boolean isSupportedRefineSubstatement(final StmtContext<?, ?, ?> refineSubstatementCtx) {
        final Collection<?> supportedRefineSubstatements = refineSubstatementCtx.getFromNamespace(
                ValidationBundlesNamespace.class, ValidationBundleType.SUPPORTED_REFINE_SUBSTATEMENTS);

        return supportedRefineSubstatements == null || supportedRefineSubstatements.isEmpty()
                || supportedRefineSubstatements.contains(refineSubstatementCtx.getPublicDefinition())
                || StmtContextUtils.isUnknownStatement(refineSubstatementCtx);
    }

    private static boolean isSupportedRefineTarget(final StmtContext<?, ?, ?> refineSubstatementCtx,
            final StmtContext<?, ?, ?> refineTargetNodeCtx) {
        final Collection<?> supportedRefineTargets = YangValidationBundles.SUPPORTED_REFINE_TARGETS
                .get(refineSubstatementCtx.getPublicDefinition());

        return supportedRefineTargets == null || supportedRefineTargets.isEmpty()
                || supportedRefineTargets.contains(refineTargetNodeCtx.getPublicDefinition());
    }


    private static QNameModule getNewQNameModule(final StmtContext<?, ?, ?> targetCtx,
            final StmtContext<?, ?, ?> stmtContext) {
        if (targetCtx.getParentContext() == null) {
            return targetCtx.getFromNamespace(ModuleCtxToModuleQName.class, targetCtx);
        }
        if (targetCtx.getPublicDefinition() == YangStmtMapping.AUGMENT) {
            return StmtContextUtils.getRootModuleQName(targetCtx);
        }

        final Object targetStmtArgument = targetCtx.getStatementArgument();
        final Object sourceStmtArgument = stmtContext.getStatementArgument();
        if (targetStmtArgument instanceof QName && sourceStmtArgument instanceof QName) {
            return ((QName) targetStmtArgument).getModule();
        }

        return null;
    }

}
