/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;

import org.opendaylight.yangtools.yang.parser.stmt.reactor.RootStatementContext;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

public final class GroupingUtils {

    private GroupingUtils() {
    }

    /**
     * @param sourceGrpStmtCtx
     * @param targetCtx
     * @throws SourceException
     */
    public static void copyFromSourceToTarget(
            StatementContextBase<?, ?, ?> sourceGrpStmtCtx,
            StatementContextBase<?, ?, ?> targetCtx,
            StmtContext.Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode)
            throws SourceException {

        QNameModule newQNameModule = getNewQNameModule(targetCtx,
                sourceGrpStmtCtx);
        copyDeclaredStmts(sourceGrpStmtCtx, targetCtx, usesNode, newQNameModule);
        copyEffectiveStmts(sourceGrpStmtCtx, targetCtx, usesNode,
                newQNameModule);

    }

    public static void copyDeclaredStmts(
            StatementContextBase<?, ?, ?> sourceGrpStmtCtx,
            StatementContextBase<?, ?, ?> targetCtx,
            StmtContext.Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode,
            QNameModule newQNameModule) throws SourceException {
        Collection<? extends StatementContextBase<?, ?, ?>> declaredSubstatements = sourceGrpStmtCtx
                .declaredSubstatements();
        for (StatementContextBase<?, ?, ?> originalStmtCtx : declaredSubstatements) {
            if (needToCopyByUses(originalStmtCtx)) {
                StatementContextBase<?, ?, ?> copy = originalStmtCtx
                        .createCopy(newQNameModule, targetCtx,
                                TypeOfCopy.ADDED_BY_USES);
                targetCtx.addEffectiveSubstatement(copy);
                usesNode.addAsEffectOfStatement(copy);
            } else if (isReusedByUsesOnTop(originalStmtCtx)) {
                targetCtx.addEffectiveSubstatement(originalStmtCtx);
                usesNode.addAsEffectOfStatement(originalStmtCtx);
            }
        }
    }

    public static void copyEffectiveStmts(
            StatementContextBase<?, ?, ?> sourceGrpStmtCtx,
            StatementContextBase<?, ?, ?> targetCtx,
            StmtContext.Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode,
            QNameModule newQNameModule) throws SourceException {
        Collection<? extends StatementContextBase<?, ?, ?>> effectiveSubstatements = sourceGrpStmtCtx
                .effectiveSubstatements();
        for (StatementContextBase<?, ?, ?> originalStmtCtx : effectiveSubstatements) {
            if (needToCopyByUses(originalStmtCtx)) {
                StatementContextBase<?, ?, ?> copy = originalStmtCtx
                        .createCopy(newQNameModule, targetCtx,
                                TypeOfCopy.ADDED_BY_USES);
                targetCtx.addEffectiveSubstatement(copy);
                usesNode.addAsEffectOfStatement(copy);
            } else if (isReusedByUsesOnTop(originalStmtCtx)) {
                targetCtx.addEffectiveSubstatement(originalStmtCtx);
                usesNode.addAsEffectOfStatement(originalStmtCtx);
            }
        }
    }

    public static QNameModule getNewQNameModule(
            StatementContextBase<?, ?, ?> targetCtx,
            StmtContext<?, ?, ?> stmtContext) {
        if (needToCreateNewQName(stmtContext.getPublicDefinition())) {
            if (targetCtx.isRootContext()) {
                return targetCtx.getFromNamespace(
                        ModuleCtxToModuleQName.class,
                        targetCtx);
            }
            if(targetCtx.getPublicDefinition() == Rfc6020Mapping.AUGMENT) {
                RootStatementContext<?, ?, ?> root = targetCtx.getRoot();
                return targetCtx.getFromNamespace(
                        ModuleCtxToModuleQName.class,
                        root);
            }

            Object targetStmtArgument = targetCtx.getStatementArgument();
            Object sourceStmtArgument = stmtContext.getStatementArgument();
            if (targetStmtArgument instanceof QName
                    && sourceStmtArgument instanceof QName) {
                QName targetQName = (QName) targetStmtArgument;
                QNameModule targetQNameModule = targetQName.getModule();

                return targetQNameModule;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static boolean needToCreateNewQName(
            StatementDefinition publicDefinition) {
        return true;
    }

    public static boolean needToCopyByUses(StmtContext<?, ?, ?> stmtContext) {

        Set<StatementDefinition> noCopyDefSet = new HashSet<>();
        noCopyDefSet.add(Rfc6020Mapping.USES);
        noCopyDefSet.add(Rfc6020Mapping.TYPEDEF);
        noCopyDefSet.add(Rfc6020Mapping.TYPE);

        final Set<StatementDefinition> noCopyFromGroupingSet = new HashSet<>();
        noCopyFromGroupingSet.add(Rfc6020Mapping.DESCRIPTION);
        noCopyFromGroupingSet.add(Rfc6020Mapping.REFERENCE);

        StatementDefinition def = stmtContext.getPublicDefinition();
        boolean dontCopyFromParentGrouping = noCopyFromGroupingSet.contains(def) && stmtContext.getParentContext()
                .getPublicDefinition().equals(Rfc6020Mapping.GROUPING);

        return !noCopyDefSet.contains(def) && !dontCopyFromParentGrouping;
    }

    public static boolean isReusedByUses(StmtContext<?, ?, ?> stmtContext) {

        Set<StatementDefinition> reusedDefSet = new HashSet<>();
        reusedDefSet.add(Rfc6020Mapping.TYPEDEF);
        reusedDefSet.add(Rfc6020Mapping.TYPE);
        reusedDefSet.add(Rfc6020Mapping.USES);

        StatementDefinition def = stmtContext.getPublicDefinition();
        return reusedDefSet.contains(def);
    }

    public static boolean isReusedByUsesOnTop(StmtContext<?, ?, ?> stmtContext) {

        Set<StatementDefinition> reusedDefSet = new HashSet<>();
        reusedDefSet.add(Rfc6020Mapping.TYPEDEF);
        reusedDefSet.add(Rfc6020Mapping.TYPE);

        StatementDefinition def = stmtContext.getPublicDefinition();
        return reusedDefSet.contains(def);
    }

    public static void resolveUsesNode(
            Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode,
            StatementContextBase<?, ?, ?> targetNodeStmtCtx)
            throws SourceException {

        Collection<StatementContextBase<?, ?, ?>> declaredSubstatements = usesNode
                .declaredSubstatements();
        for (StatementContextBase<?, ?, ?> subStmtCtx : declaredSubstatements) {
            if (StmtContextUtils.producesDeclared(subStmtCtx,
                    RefineStatement.class)) {
                performRefine(subStmtCtx, targetNodeStmtCtx);
            }
        }
    }

    private static void performRefine(StatementContextBase<?, ?, ?> refineCtx,
            StatementContextBase<?, ?, ?> usesParentCtx) {

        Object refineArgument = refineCtx.getStatementArgument();

        SchemaNodeIdentifier refineTargetNodeIdentifier;
        if (refineArgument instanceof SchemaNodeIdentifier) {
            refineTargetNodeIdentifier = (SchemaNodeIdentifier) refineArgument;
        } else {
            throw new IllegalArgumentException(
                    "Invalid refine argument. It must be instance of SchemaNodeIdentifier");
        }

        StatementContextBase<?, ?, ?> refineTargetNodeCtx = Utils.findNode(
                usesParentCtx, refineTargetNodeIdentifier);

        if (refineTargetNodeCtx == null) {
            throw new IllegalArgumentException(
                    "Refine target node not found. Path: "
                            + refineTargetNodeIdentifier);
        }

        addOrReplaceNodes(refineCtx, refineTargetNodeCtx);
        refineCtx.addAsEffectOfStatement(refineTargetNodeCtx);

    }

    private static void addOrReplaceNodes(
            StatementContextBase<?, ?, ?> refineCtx,
            StatementContextBase<?, ?, ?> refineTargetNodeCtx) {

        Collection<StatementContextBase<?, ?, ?>> declaredSubstatements = refineCtx
                .declaredSubstatements();
        for (StatementContextBase<?, ?, ?> refineSubstatementCtx : declaredSubstatements) {
            if (isSupportedRefineSubstatement(refineSubstatementCtx)) {
                addOrReplaceNode(refineSubstatementCtx, refineTargetNodeCtx);
            }
        }
    }

    private static void addOrReplaceNode(
            StatementContextBase<?, ?, ?> refineSubstatementCtx,
            StatementContextBase<?, ?, ?> refineTargetNodeCtx) {

        StatementDefinition refineSubstatementDef = refineSubstatementCtx
                .getPublicDefinition();
        StatementDefinition refineTargetNodeDef = refineTargetNodeCtx
                .getPublicDefinition();

        if (!isSupportedRefineTarget(refineSubstatementCtx, refineTargetNodeCtx)) {
            throw new SourceException("Error in module '"
                    + refineSubstatementCtx.getRoot().getStatementArgument()
                    + "' in the refine of uses '"
                    + refineSubstatementCtx.getParentContext()
                            .getStatementArgument()
                    + "': can not perform refine of '"
                    + refineSubstatementCtx.getPublicDefinition()
                    + "' for the target '"
                    + refineTargetNodeCtx.getPublicDefinition() + "'.",
                    refineSubstatementCtx.getStatementSourceReference());
        }

        if (isAllowedToAddByRefine(refineSubstatementDef)) {
            refineTargetNodeCtx.addEffectiveSubstatement(refineSubstatementCtx);
        } else {
            refineTargetNodeCtx.removeStatementFromEffectiveSubstatements(refineSubstatementDef);
            refineTargetNodeCtx.addEffectiveSubstatement(refineSubstatementCtx);
        }
    }

    private static boolean isAllowedToAddByRefine(
            StatementDefinition publicDefinition) {
        Set<StatementDefinition> allowedToAddByRefineDefSet = new HashSet<>();
        allowedToAddByRefineDefSet.add(Rfc6020Mapping.MUST);

        return allowedToAddByRefineDefSet.contains(publicDefinition);
    }

    private static boolean isSupportedRefineSubstatement(
            StatementContextBase<?, ?, ?> refineSubstatementCtx) {

        Collection<?> supportedRefineSubstatements = refineSubstatementCtx
                .getFromNamespace(ValidationBundlesNamespace.class,
                        ValidationBundleType.SUPPORTED_REFINE_SUBSTATEMENTS);

        return supportedRefineSubstatements == null
                || supportedRefineSubstatements.isEmpty()
                || supportedRefineSubstatements.contains(refineSubstatementCtx
                        .getPublicDefinition())
                || StmtContextUtils.isUnknownStatement(refineSubstatementCtx);
    }

    private static boolean isSupportedRefineTarget(
            StatementContextBase<?, ?, ?> refineSubstatementCtx,
            StatementContextBase<?, ?, ?> refineTargetNodeCtx) {

        Collection<?> supportedRefineTargets = YangValidationBundles.SUPPORTED_REFINE_TARGETS
                .get(refineSubstatementCtx.getPublicDefinition());

        return supportedRefineTargets == null
                || supportedRefineTargets.isEmpty()
                || supportedRefineTargets.contains(refineTargetNodeCtx
                        .getPublicDefinition());
    }

}
