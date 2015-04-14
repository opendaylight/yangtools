/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * <p/>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Collection;
import java.util.HashSet;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.NamespaceToModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

public class AugmentUtils {

    public static void copyFromSourceToTarget(StatementContextBase<?, ?, ?> sourceCtx,
            StatementContextBase<?, ?, ?> targetCtx) throws SourceException {

        QNameModule newQNameModule = getNewQNameModule(targetCtx, sourceCtx);
        copyDeclaredStmts(sourceCtx, targetCtx, newQNameModule);
        copyEffectiveStmts(sourceCtx, targetCtx, newQNameModule);

    }

    public static void copyDeclaredStmts(StatementContextBase<?, ?, ?> sourceCtx,
            StatementContextBase<?, ?, ?> targetCtx, QNameModule newQNameModule) throws SourceException {
        Collection<? extends StatementContextBase<?, ?, ?>> declaredSubstatements = sourceCtx.declaredSubstatements();
        for (StatementContextBase<?, ?, ?> originalStmtCtx : declaredSubstatements) {
            if (needToCopyByAugment(originalStmtCtx)) {
                StatementContextBase<?, ?, ?> copy = originalStmtCtx.createCopy(newQNameModule, targetCtx);
                targetCtx.addEffectiveSubstatement(copy);
            } else if (isReusedByAugment(originalStmtCtx)) {
                targetCtx.addEffectiveSubstatement(originalStmtCtx);
            }
        }
    }

    public static void copyEffectiveStmts(StatementContextBase<?, ?, ?> sourceCtx,
            StatementContextBase<?, ?, ?> targetCtx, QNameModule newQNameModule) throws SourceException {
        Collection<? extends StatementContextBase<?, ?, ?>> effectiveSubstatements = sourceCtx.effectiveSubstatements();
        for (StatementContextBase<?, ?, ?> originalStmtCtx : effectiveSubstatements) {
            if (needToCopyByAugment(originalStmtCtx)) {
                StatementContextBase<?, ?, ?> copy = originalStmtCtx.createCopy(newQNameModule, targetCtx);
                targetCtx.addEffectiveSubstatement(copy);
            } else if (isReusedByAugment(originalStmtCtx)) {
                targetCtx.addEffectiveSubstatement(originalStmtCtx);
            }
        }
    }

    public static QNameModule getNewQNameModule(StatementContextBase<?, ?, ?> targetCtx,
            StatementContextBase<?, ?, ?> sourceCtx) {
        Object targetStmtArgument = targetCtx.getStatementArgument();

        final StatementContextBase<?, ?, ?> root = sourceCtx.getRoot();
        final String moduleName = (String) root.getStatementArgument();
        final QNameModule sourceQNameModule = root.getFromNamespace(ModuleNameToModuleQName.class, moduleName);

        if (targetStmtArgument instanceof QName) {
            QName targetQName = (QName) targetStmtArgument;
            QNameModule targetQNameModule = targetQName.getModule();

            if (targetQNameModule.equals(sourceQNameModule))
                return null;
            else
                return targetQNameModule;
        } else
            return null;
    }

    public static boolean needToCopyByAugment(StmtContext<?, ?, ?> stmtContext) {

        HashSet<StatementDefinition> noCopyDefSet = new HashSet<>();
        noCopyDefSet.add(Rfc6020Mapping.Uses);

        StatementDefinition def = stmtContext.getPublicDefinition();
        if (noCopyDefSet.contains(def))
            return false;
        else
            return true;
    }

    public static boolean isReusedByAugment(StmtContext<?, ?, ?> stmtContext) {

        HashSet<StatementDefinition> reusedDefSet = new HashSet<>();
        reusedDefSet.add(Rfc6020Mapping.Typedef);

        StatementDefinition def = stmtContext.getPublicDefinition();
        if (reusedDefSet.contains(def))
            return true;
        else
            return false;
    }

    public static StatementContextBase<?, ?, ?> getAugmentTargetCtx(
            final Mutable<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> augmentNode) {

        final SchemaNodeIdentifier augmentTargetPath = augmentNode.getStatementArgument();

        QNameModule module;
        if (augmentTargetPath != null) {
            module = augmentTargetPath.getPathFromRoot().iterator().next().getModule();
        } else {
            throw new IllegalArgumentException(
                    "Augment argument null, something bad happened in some of previous parsing phases");
        }

        StatementContextBase<?, ?, ?> rootStatementCtx = (StatementContextBase<?, ?, ?>) augmentNode.getFromNamespace(
                NamespaceToModule.class, module);

        return Utils.findCtxOfNodeInRoot(rootStatementCtx, augmentTargetPath);
    }
}
