/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.uses;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.YangValidationBundles;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UsesStatementImpl extends AbstractDeclaredStatement<QName> implements UsesStatement {
    private static final Logger LOG = LoggerFactory.getLogger(UsesStatementImpl.class);

    UsesStatementImpl(final StmtContext<QName, UsesStatement, ?> context) {
        super(context);
    }

    /**
     * Copy statements from a grouping to a target node.
     *
     * @param sourceGrpStmtCtx
     *            source grouping statement context
     * @param targetCtx
     *            target context
     * @param usesNode
     *            uses node
     * @throws SourceException
     *             instance of SourceException
     */
    static void copyFromSourceToTarget(final Mutable<?, ?, ?> sourceGrpStmtCtx,
            final StatementContextBase<?, ?, ?> targetCtx,
            final Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode) {
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
            LOG.trace("Will reuse {} statement {}", def, stmtContext);
            return false;
        }
        if (NOCOPY_FROM_GROUPING_SET.contains(def)) {
            return !YangStmtMapping.GROUPING.equals(stmtContext.coerceParentContext().getPublicDefinition());
        }

        LOG.trace("Will copy {} statement {}", def, stmtContext);
        return true;
    }

    public static void resolveUsesNode(
            final Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode,
            final StmtContext<?, ?, ?> targetNodeStmtCtx) {
        for (final Mutable<?, ?, ?> subStmtCtx : usesNode.mutableDeclaredSubstatements()) {
            if (StmtContextUtils.producesDeclared(subStmtCtx, RefineStatement.class)
                    && areFeaturesSupported(subStmtCtx)) {
                performRefine(subStmtCtx, targetNodeStmtCtx);
            }
        }
    }

    private static boolean areFeaturesSupported(final StmtContext<?, ?, ?> subStmtCtx) {
        /*
         * In case of Yang 1.1, checks whether features are supported.
         */
        return !YangVersion.VERSION_1_1.equals(subStmtCtx.getRootVersion()) || subStmtCtx.isSupportedByFeatures();
    }

    private static void performRefine(final Mutable<?, ?, ?> subStmtCtx, final StmtContext<?, ?, ?> usesParentCtx) {
        final Object refineArgument = subStmtCtx.getStatementArgument();
        InferenceException.throwIf(!(refineArgument instanceof SchemaNodeIdentifier),
            subStmtCtx.getStatementSourceReference(),
            "Invalid refine argument %s. It must be instance of SchemaNodeIdentifier.", refineArgument);

        final Optional<StmtContext<?, ?, ?>> optRefineTargetCtx = ChildSchemaNodeNamespace.findNode(
            usesParentCtx, (SchemaNodeIdentifier) refineArgument);
        InferenceException.throwIf(!optRefineTargetCtx.isPresent(), subStmtCtx.getStatementSourceReference(),
            "Refine target node %s not found.", refineArgument);

        final StmtContext<?, ?, ?> refineTargetNodeCtx = optRefineTargetCtx.get();
        if (StmtContextUtils.isUnknownStatement(refineTargetNodeCtx)) {
            LOG.trace("Refine node '{}' in uses '{}' has target node unknown statement '{}'. "
                + "Refine has been skipped. At line: {}", subStmtCtx.getStatementArgument(),
                subStmtCtx.coerceParentContext().getStatementArgument(),
                refineTargetNodeCtx.getStatementArgument(), subStmtCtx.getStatementSourceReference());
            subStmtCtx.addAsEffectOfStatement(refineTargetNodeCtx);
            return;
        }

        Verify.verify(refineTargetNodeCtx instanceof StatementContextBase);
        addOrReplaceNodes(subStmtCtx, (StatementContextBase<?, ?, ?>) refineTargetNodeCtx);
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
                refineSubstatementCtx.getRoot().getStatementArgument(),
                refineSubstatementCtx.coerceParentContext().getStatementArgument(),
                refineSubstatementCtx.getPublicDefinition(), refineTargetNodeCtx.getPublicDefinition());

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
