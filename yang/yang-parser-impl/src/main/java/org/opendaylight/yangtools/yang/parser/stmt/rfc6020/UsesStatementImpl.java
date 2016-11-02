/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UsesEffectiveStatementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UsesStatementImpl extends AbstractDeclaredStatement<QName> implements UsesStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .USES)
            .addAny(Rfc6020Mapping.AUGMENT)
            .addOptional(Rfc6020Mapping.DESCRIPTION)
            .addAny(Rfc6020Mapping.IF_FEATURE)
            .addAny(Rfc6020Mapping.REFINE)
            .addOptional(Rfc6020Mapping.REFERENCE)
            .addOptional(Rfc6020Mapping.STATUS)
            .addOptional(Rfc6020Mapping.WHEN)
            .build();

    private static final Logger LOG = LoggerFactory.getLogger(UsesStatementImpl.class);

    protected UsesStatementImpl(final StmtContext<QName, UsesStatement, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> {

        public Definition() {
            super(Rfc6020Mapping.USES);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return Utils.qNameFromArgument(ctx, value);
        }

        @Override
        public void onFullDefinitionDeclared(
                final StmtContext.Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode) {
            if (!StmtContextUtils.areFeaturesSupported(usesNode)) {
                return;
            }

            SUBSTATEMENT_VALIDATOR.validate(usesNode);

            if (StmtContextUtils.isInExtensionBody(usesNode)) {
                return;
            }

            ModelActionBuilder usesAction = usesNode.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
            final QName groupingName = usesNode.getStatementArgument();

            final Prerequisite<StmtContext<?, ?, ?>> sourceGroupingPre = usesAction.requiresCtx(usesNode,
                    GroupingNamespace.class, groupingName, ModelProcessingPhase.EFFECTIVE_MODEL);
            final Prerequisite<? extends StmtContext.Mutable<?, ?, ?>> targetNodePre = usesAction.mutatesEffectiveCtx(
                    usesNode.getParentContext());

            usesAction.apply(new InferenceAction() {

                @Override
                public void apply() {
                    StatementContextBase<?, ?, ?> targetNodeStmtCtx = (StatementContextBase<?, ?, ?>) targetNodePre.get();
                    StatementContextBase<?, ?, ?> sourceGrpStmtCtx = (StatementContextBase<?, ?, ?>) sourceGroupingPre.get();

                    try {
                        copyFromSourceToTarget(sourceGrpStmtCtx, targetNodeStmtCtx, usesNode);
                        resolveUsesNode(usesNode, targetNodeStmtCtx);
                    } catch (SourceException e) {
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
    private static void copyFromSourceToTarget(final StatementContextBase<?, ?, ?> sourceGrpStmtCtx,
            final StatementContextBase<?, ?, ?> targetCtx,
            final StmtContext.Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode) {
        final Collection<StatementContextBase<?, ?, ?>> declared = sourceGrpStmtCtx.declaredSubstatements();
        final Collection<StatementContextBase<?, ?, ?>> effective = sourceGrpStmtCtx.effectiveSubstatements();
        final Collection<StatementContextBase<?, ?, ?>> buffer = new ArrayList<>(declared.size() + effective.size());
        final QNameModule newQNameModule = getNewQNameModule(targetCtx, sourceGrpStmtCtx);

        for (final StatementContextBase<?, ?, ?> original : declared) {
            if (StmtContextUtils.areFeaturesSupported(original)) {
                copyStatement(original, targetCtx, newQNameModule, buffer);
            }
        }

        for (final StatementContextBase<?, ?, ?> original : effective) {
            copyStatement(original, targetCtx, newQNameModule, buffer);
        }

        targetCtx.addEffectiveSubstatements(buffer);
        usesNode.addAsEffectOfStatement(buffer);
    }

    private static void copyStatement(final StatementContextBase<?, ?, ?> original,
            final StatementContextBase<?, ?, ?> targetCtx, final QNameModule targetModule,
            final Collection<StatementContextBase<?, ?, ?>> buffer) {
        if (needToCopyByUses(original)) {
            final StatementContextBase<?, ?, ?> copy = original.createCopy(targetModule, targetCtx,
                    CopyType.ADDED_BY_USES);
            buffer.add(copy);
        } else if (isReusedByUsesOnTop(original)) {
            buffer.add(original);
        }
    }

    private static final Set<Rfc6020Mapping> TOP_REUSED_DEF_SET = ImmutableSet.of(
        Rfc6020Mapping.TYPE,
        Rfc6020Mapping.TYPEDEF);

    private static boolean isReusedByUsesOnTop(final StmtContext<?, ?, ?> stmtContext) {
        return TOP_REUSED_DEF_SET.contains(stmtContext.getPublicDefinition());
    }

    private static final Set<Rfc6020Mapping> NOCOPY_FROM_GROUPING_SET = ImmutableSet.of(
        Rfc6020Mapping.DESCRIPTION,
        Rfc6020Mapping.REFERENCE,
        Rfc6020Mapping.STATUS);
    private static final Set<Rfc6020Mapping> REUSED_DEF_SET = ImmutableSet.of(
        Rfc6020Mapping.TYPE,
        Rfc6020Mapping.TYPEDEF,
        Rfc6020Mapping.USES);

    public static boolean needToCopyByUses(final StmtContext<?, ?, ?> stmtContext) {
        final StatementDefinition def = stmtContext.getPublicDefinition();
        if (REUSED_DEF_SET.contains(def)) {
            LOG.debug("Will reuse {} statement {}", def, stmtContext);
            return false;
        }
        if (NOCOPY_FROM_GROUPING_SET.contains(def)) {
            return !Rfc6020Mapping.GROUPING.equals(stmtContext.getParentContext().getPublicDefinition());
        }

        LOG.debug("Will copy {} statement {}", def, stmtContext);
        return true;
    }

    public static void resolveUsesNode(
            final Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode,
            final StatementContextBase<?, ?, ?> targetNodeStmtCtx) {
        for (final StatementContextBase<?, ?, ?> subStmtCtx : usesNode.declaredSubstatements()) {
            if (StmtContextUtils.producesDeclared(subStmtCtx, RefineStatement.class)) {
                performRefine(subStmtCtx, targetNodeStmtCtx);
            }
        }
    }

    private static void performRefine(final StatementContextBase<?, ?, ?> refineCtx,
            final StatementContextBase<?, ?, ?> usesParentCtx) {

        final Object refineArgument = refineCtx.getStatementArgument();
        Preconditions.checkArgument(refineArgument instanceof SchemaNodeIdentifier,
                "Invalid refine argument %s. It must be instance of SchemaNodeIdentifier. At %s", refineArgument,
                refineCtx.getStatementSourceReference());

        final SchemaNodeIdentifier refineTargetNodeIdentifier = (SchemaNodeIdentifier) refineArgument;
        final StatementContextBase<?, ?, ?> refineTargetNodeCtx = Utils.findNode(usesParentCtx,
                refineTargetNodeIdentifier);
        Preconditions.checkArgument(refineTargetNodeCtx != null, "Refine target node %s not found. At %s",
                refineTargetNodeIdentifier, refineCtx.getStatementSourceReference());
        if (StmtContextUtils.isUnknownStatement(refineTargetNodeCtx)) {
            LOG.debug(
                    "Refine node '{}' in uses '{}' has target node unknown statement '{}'. Refine has been skipped. At line: {}",
                    refineCtx.getStatementArgument(), refineCtx.getParentContext().getStatementArgument(),
                    refineTargetNodeCtx.getStatementArgument(), refineCtx.getStatementSourceReference());
            refineCtx.addAsEffectOfStatement(refineTargetNodeCtx);
            return;
        }

        addOrReplaceNodes(refineCtx, refineTargetNodeCtx);
        refineCtx.addAsEffectOfStatement(refineTargetNodeCtx);
    }

    private static void addOrReplaceNodes(final StatementContextBase<?, ?, ?> refineCtx,
            final StatementContextBase<?, ?, ?> refineTargetNodeCtx) {
        for (final StatementContextBase<?, ?, ?> refineSubstatementCtx : refineCtx.declaredSubstatements()) {
            if (isSupportedRefineSubstatement(refineSubstatementCtx)) {
                addOrReplaceNode(refineSubstatementCtx, refineTargetNodeCtx);
            }
        }
    }

    private static void addOrReplaceNode(final StatementContextBase<?, ?, ?> refineSubstatementCtx,
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

    private static final Set<Rfc6020Mapping> ALLOWED_TO_ADD_BY_REFINE_DEF_SET = ImmutableSet.of(Rfc6020Mapping.MUST);

    private static boolean isAllowedToAddByRefine(final StatementDefinition publicDefinition) {
        return ALLOWED_TO_ADD_BY_REFINE_DEF_SET.contains(publicDefinition);
    }

    private static boolean isSupportedRefineSubstatement(final StatementContextBase<?, ?, ?> refineSubstatementCtx) {
        final Collection<?> supportedRefineSubstatements = refineSubstatementCtx.getFromNamespace(
                ValidationBundlesNamespace.class, ValidationBundleType.SUPPORTED_REFINE_SUBSTATEMENTS);

        return supportedRefineSubstatements == null || supportedRefineSubstatements.isEmpty()
                || supportedRefineSubstatements.contains(refineSubstatementCtx.getPublicDefinition())
                || StmtContextUtils.isUnknownStatement(refineSubstatementCtx);
    }

    private static boolean isSupportedRefineTarget(final StatementContextBase<?, ?, ?> refineSubstatementCtx,
            final StatementContextBase<?, ?, ?> refineTargetNodeCtx) {

        final Collection<?> supportedRefineTargets = YangValidationBundles.SUPPORTED_REFINE_TARGETS
                .get(refineSubstatementCtx.getPublicDefinition());

        return supportedRefineTargets == null || supportedRefineTargets.isEmpty()
                || supportedRefineTargets.contains(refineTargetNodeCtx.getPublicDefinition());
    }


    private static QNameModule getNewQNameModule(final StatementContextBase<?, ?, ?> targetCtx,
            final StmtContext<?, ?, ?> stmtContext) {
        if (targetCtx.isRootContext()) {
            return targetCtx.getFromNamespace(ModuleCtxToModuleQName.class, targetCtx);
        }
        if (targetCtx.getPublicDefinition() == Rfc6020Mapping.AUGMENT) {
            return targetCtx.getFromNamespace(ModuleCtxToModuleQName.class, targetCtx.getRoot());
        }

        final Object targetStmtArgument = targetCtx.getStatementArgument();
        final Object sourceStmtArgument = stmtContext.getStatementArgument();
        if (targetStmtArgument instanceof QName && sourceStmtArgument instanceof QName) {
            return ((QName) targetStmtArgument).getModule();
        }

        return null;
    }

}
