/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;

import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
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
    public static void copyFromSourceToTarget(StatementContextBase<?, ?, ?> sourceGrpStmtCtx,
            StatementContextBase<?, ?, ?> targetCtx) throws SourceException {

        QNameModule newQNameModule = getNewQNameModule(targetCtx, sourceGrpStmtCtx);
        copyDeclaredStmts(sourceGrpStmtCtx, targetCtx, newQNameModule);
        copyEffectiveStmts(sourceGrpStmtCtx, targetCtx, newQNameModule);

    }

    public static void copyDeclaredStmts(StatementContextBase<?, ?, ?> sourceGrpStmtCtx,
            StatementContextBase<?, ?, ?> targetCtx, QNameModule newQNameModule) throws SourceException {
        Collection<? extends StatementContextBase<?, ?, ?>> declaredSubstatements = sourceGrpStmtCtx
                .declaredSubstatements();
        for (StatementContextBase<?, ?, ?> originalStmtCtx : declaredSubstatements) {
            if (needToCopyByUses(originalStmtCtx)) {
                StatementContextBase<?, ?, ?> copy = originalStmtCtx.createCopy(newQNameModule, targetCtx, TypeOfCopy.ADDED_BY_USES);
                targetCtx.addEffectiveSubstatement(copy);
            } else if (isReusedByUses(originalStmtCtx)) {
                targetCtx.addEffectiveSubstatement(originalStmtCtx);
            }
        }
    }

    public static void copyEffectiveStmts(StatementContextBase<?, ?, ?> sourceGrpStmtCtx,
            StatementContextBase<?, ?, ?> targetCtx, QNameModule newQNameModule) throws SourceException {
        Collection<? extends StatementContextBase<?, ?, ?>> effectiveSubstatements = sourceGrpStmtCtx
                .effectiveSubstatements();
        for (StatementContextBase<?, ?, ?> originalStmtCtx : effectiveSubstatements) {
            if (needToCopyByUses(originalStmtCtx)) {
                StatementContextBase<?, ?, ?> copy = originalStmtCtx.createCopy(newQNameModule, targetCtx, TypeOfCopy.ADDED_BY_USES);
                targetCtx.addEffectiveSubstatement(copy);
            } else if (isReusedByUses(originalStmtCtx)) {
                targetCtx.addEffectiveSubstatement(originalStmtCtx);
            }
        }
    }

    public static QNameModule getNewQNameModule(StatementContextBase<?, ?, ?> targetCtx,
            StmtContext<?, ?, ?> stmtContext) {
        if (needToCreateNewQName(stmtContext.getPublicDefinition())) {
            if(targetCtx.isRootContext()) {
                return targetCtx.getFromNamespace(ModuleNameToModuleQName.class, targetCtx.rawStatementArgument());
            }
            Object targetStmtArgument = targetCtx.getStatementArgument();
            Object sourceStmtArgument = stmtContext.getStatementArgument();
            if (targetStmtArgument instanceof QName && sourceStmtArgument instanceof QName) {
                QName targetQName = (QName) targetStmtArgument;
                QNameModule targetQNameModule = targetQName.getModule();

                QName sourceQName = (QName) sourceStmtArgument;
                QNameModule sourceQNameModule = sourceQName.getModule();

                if (targetQNameModule.equals(sourceQNameModule)) {
                    return null;
                } else {
                    return targetQNameModule;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static boolean needToCreateNewQName(StatementDefinition publicDefinition) {
        return true;
    }

    public static boolean needToCopyByUses(StmtContext<?, ?, ?> stmtContext) {

        Set<StatementDefinition> noCopyDefSet = new HashSet<StatementDefinition>();
        noCopyDefSet.add(Rfc6020Mapping.USES);

        StatementDefinition def = stmtContext.getPublicDefinition();
        return !noCopyDefSet.contains(def);
    }

    public static boolean isReusedByUses(StmtContext<?, ?, ?> stmtContext) {

        Set<StatementDefinition> reusedDefSet = new HashSet<StatementDefinition>();
        reusedDefSet.add(Rfc6020Mapping.TYPEDEF);

        StatementDefinition def = stmtContext.getPublicDefinition();
        return reusedDefSet.contains(def);
    }

    public static void resolveUsesNode(
            Mutable<QName, UsesStatement, EffectiveStatement<QName, UsesStatement>> usesNode,
            StatementContextBase<?, ?, ?> targetNodeStmtCtx) throws SourceException {

        Collection<StatementContextBase<?, ?, ?>> declaredSubstatements = usesNode.declaredSubstatements();
        for (StatementContextBase<?, ?, ?> subStmtCtx : declaredSubstatements) {
            if (StmtContextUtils.producesDeclared(subStmtCtx, WhenStatement.class)) {
                StatementContextBase<?, ?, ?> copy = subStmtCtx.createCopy(null, targetNodeStmtCtx, TypeOfCopy.ADDED_BY_USES);
                targetNodeStmtCtx.addEffectiveSubstatement(copy);
            }
            if (StmtContextUtils.producesDeclared(subStmtCtx, RefineStatement.class)) {
                // :TODO resolve and perform refine statement
            }
            if (StmtContextUtils.producesDeclared(subStmtCtx, AugmentStatement.class)) {
                // :TODO find target node and perform augmentation
            }
            // :TODO resolve other uses substatements
        }
    }

}
