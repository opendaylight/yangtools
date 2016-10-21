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
import java.util.Collection;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
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

public final class GroupingUtils {

    private static final Logger LOG = LoggerFactory.getLogger(GroupingUtils.class);

    private GroupingUtils() {
        throw new UnsupportedOperationException();
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
    public static void copyFromSourceToTarget(final StatementContextBase<?, ?, ?> sourceGrpStmtCtx,
            final StatementContextBase<?, ?, ?> targetCtx,
            final StmtContext.Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode) {

        final QNameModule newQNameModule = getNewQNameModule(targetCtx, sourceGrpStmtCtx);
        for (final StatementContextBase<?, ?, ?> original : sourceGrpStmtCtx.declaredSubstatements()) {
            if (StmtContextUtils.areFeaturesSupported(original)) {
                copyStatement(original, targetCtx, usesNode, newQNameModule);
            }
        }

        for (final StatementContextBase<?, ?, ?> original : sourceGrpStmtCtx.effectiveSubstatements()) {
            copyStatement(original, targetCtx, usesNode, newQNameModule);
        }
    }

    private static void copyStatement(final StatementContextBase<?, ?, ?> original,
            final StatementContextBase<?, ?, ?> targetCtx,
            final StmtContext.Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> targetUses,
            final QNameModule targetModule) {
        if (needToCopyByUses(original)) {
            final StatementContextBase<?, ?, ?> copy = original.createCopy(targetModule, targetCtx,
                    CopyType.ADDED_BY_USES);
            targetCtx.addEffectiveSubstatement(copy);
            targetUses.addAsEffectOfStatement(copy);
        } else if (isReusedByUsesOnTop(original)) {
            targetCtx.addEffectiveSubstatement(original);
            targetUses.addAsEffectOfStatement(original);
        }
    }

    public static QNameModule getNewQNameModule(final StatementContextBase<?, ?, ?> targetCtx,
            final StmtContext<?, ?, ?> stmtContext) {
        if (needToCreateNewQName(stmtContext.getPublicDefinition())) {
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
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static boolean needToCreateNewQName(final StatementDefinition publicDefinition) {
        return true;
    }

    private static final Set<Rfc6020Mapping> NOCOPY_DEF_SET = ImmutableSet.of(Rfc6020Mapping.USES,
            Rfc6020Mapping.TYPEDEF, Rfc6020Mapping.TYPE);
    private static final Set<Rfc6020Mapping> NOCOPY_FROM_GROUPING_SET = ImmutableSet.of(Rfc6020Mapping.DESCRIPTION,
            Rfc6020Mapping.REFERENCE, Rfc6020Mapping.STATUS);
    private static final Set<Rfc6020Mapping> REUSED_DEF_SET = ImmutableSet.of(Rfc6020Mapping.TYPEDEF,
            Rfc6020Mapping.TYPE, Rfc6020Mapping.USES);
    private static final Set<Rfc6020Mapping> TOP_REUSED_DEF_SET = ImmutableSet.of(Rfc6020Mapping.TYPEDEF,
            Rfc6020Mapping.TYPE);

    public static boolean needToCopyByUses(final StmtContext<?, ?, ?> stmtContext) {
        final StatementDefinition def = stmtContext.getPublicDefinition();

        return !(NOCOPY_DEF_SET.contains(def) || (NOCOPY_FROM_GROUPING_SET.contains(def) && Rfc6020Mapping.GROUPING
                .equals(stmtContext.getParentContext().getPublicDefinition())));
    }

    public static boolean isReusedByUses(final StmtContext<?, ?, ?> stmtContext) {
        return REUSED_DEF_SET.contains(stmtContext.getPublicDefinition());
    }

    public static boolean isReusedByUsesOnTop(final StmtContext<?, ?, ?> stmtContext) {
        return TOP_REUSED_DEF_SET.contains(stmtContext.getPublicDefinition());
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
}
