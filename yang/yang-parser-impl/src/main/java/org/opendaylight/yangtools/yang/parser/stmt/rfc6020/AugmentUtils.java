/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * <p/>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.NamespaceToModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

public final class AugmentUtils {

    private static final Logger LOG = LoggerFactory
            .getLogger(AugmentUtils.class);

    private static final String REGEX_PATH_REL1 = "\\.\\.?\\s*/(.+)";
    private static final String REGEX_PATH_REL2 = "//.*";

    private AugmentUtils() {
    }

    public static Iterable<QName> parseAugmentPath(StmtContext<?, ?, ?> ctx,
            String path) {

        if (path.matches(REGEX_PATH_REL1) || path.matches(REGEX_PATH_REL2)) {
            throw new IllegalArgumentException(
                    "An argument for augment can be only absolute path; or descendant if used in uses");
        }

        return Utils.parseXPath(ctx, path);
    }

    public static void copyFromSourceToTarget(
            StatementContextBase<?, ?, ?> sourceCtx,
            StatementContextBase<?, ?, ?> targetCtx) throws SourceException {

        copyDeclaredStmts(sourceCtx, targetCtx);
        copyEffectiveStmts(sourceCtx, targetCtx);
    }

    public static void copyDeclaredStmts(
            StatementContextBase<?, ?, ?> sourceCtx,
            StatementContextBase<?, ?, ?> targetCtx) throws SourceException {

        Collection<? extends StatementContextBase<?, ?, ?>> declaredSubStatements = sourceCtx
                .declaredSubstatements();
        final List<StatementContextBase> subStatements = new Builder<StatementContextBase>()
                .addAll(targetCtx.declaredSubstatements())
                .addAll(targetCtx.effectiveSubstatements()).build();
        boolean sourceAndTargetInSameModule = Utils.getRootModuleQName(
                sourceCtx).equals(Utils.getRootModuleQName(targetCtx));

        TypeOfCopy typeOfCopy = sourceCtx.getParentContext()
                .getPublicDefinition().getDeclaredRepresentationClass()
                .equals(UsesStatement.class) ? TypeOfCopy.ADDED_BY_USES_AUGMENTATION
                : TypeOfCopy.ADDED_BY_AUGMENTATION;

        for (StatementContextBase<?, ?, ?> originalStmtCtx : declaredSubStatements) {
            if (needToCopyByAugment(originalStmtCtx)) {
                validateNodeCanBeCopiedByAugment(originalStmtCtx,
                        subStatements, sourceAndTargetInSameModule);

                StatementContextBase<?, ?, ?> copy = originalStmtCtx
                        .createCopy(targetCtx, typeOfCopy);
                targetCtx.addEffectiveSubstatement(copy);
            } else if (isReusedByAugment(originalStmtCtx)) {
                targetCtx.addEffectiveSubstatement(originalStmtCtx);
            }
        }
    }

    public static void copyEffectiveStmts(
            StatementContextBase<?, ?, ?> sourceCtx,
            StatementContextBase<?, ?, ?> targetCtx) throws SourceException {

        Collection<? extends StatementContextBase<?, ?, ?>> effectiveSubstatements = sourceCtx
                .effectiveSubstatements();
        final List<StatementContextBase> subStatements = new Builder<StatementContextBase>()
                .addAll(targetCtx.declaredSubstatements())
                .addAll(targetCtx.effectiveSubstatements()).build();
        boolean sourceAndTargetInSameModule = Utils.getRootModuleQName(
                sourceCtx).equals(Utils.getRootModuleQName(targetCtx));

        TypeOfCopy typeOfCopy = sourceCtx.getParentContext()
                .getPublicDefinition().getDeclaredRepresentationClass()
                .equals(UsesStatement.class) ? TypeOfCopy.ADDED_BY_USES_AUGMENTATION
                : TypeOfCopy.ADDED_BY_AUGMENTATION;

        for (StatementContextBase<?, ?, ?> originalStmtCtx : effectiveSubstatements) {
            if (needToCopyByAugment(originalStmtCtx)) {
                validateNodeCanBeCopiedByAugment(originalStmtCtx,
                        subStatements, sourceAndTargetInSameModule);

                StatementContextBase<?, ?, ?> copy = originalStmtCtx
                        .createCopy(targetCtx, typeOfCopy);
                targetCtx.addEffectiveSubstatement(copy);
            } else if (isReusedByAugment(originalStmtCtx)) {
                targetCtx.addEffectiveSubstatement(originalStmtCtx);
            }
        }
    }

    private static void validateNodeCanBeCopiedByAugment(
            final StatementContextBase<?, ?, ?> sourceCtx,
            final List<StatementContextBase> targetSubStatements,
            boolean sourceAndTargetInSameModule) {

        if (sourceCtx.getPublicDefinition().getDeclaredRepresentationClass()
                .equals(WhenStatement.class)) {
            return;
        }

        if (!sourceAndTargetInSameModule) {
            final List<StatementContextBase> sourceSubStatements = new Builder<StatementContextBase>()
                    .addAll(sourceCtx.declaredSubstatements())
                    .addAll(sourceCtx.effectiveSubstatements()).build();

            for (final StatementContextBase sourceSubStatement : sourceSubStatements) {
                if (sourceSubStatement.getPublicDefinition()
                        .getDeclaredRepresentationClass()
                        .equals(MandatoryStatement.class)) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "An augment cannot add node '%s' because it is mandatory and in module different from target",
                                    sourceCtx.rawStatementArgument()));
                }
            }
        }

        for (final StatementContextBase subStatement : targetSubStatements) {

            final boolean sourceIsDataNode = DataDefinitionStatement.class
                    .isAssignableFrom(sourceCtx.getPublicDefinition()
                            .getDeclaredRepresentationClass());
            final boolean targetIsDataNode = DataDefinitionStatement.class
                    .isAssignableFrom(subStatement.getPublicDefinition()
                            .getDeclaredRepresentationClass());
            boolean qNamesEqual = sourceIsDataNode
                    && targetIsDataNode
                    && Objects.equals(sourceCtx.getStatementArgument(),
                            subStatement.getStatementArgument());

            if (qNamesEqual) {
                throw new IllegalStateException(
                        String.format(
                                "An augment cannot add node named '%s' because this name is already used in target",
                                sourceCtx.rawStatementArgument()));
            }
        }
    }

    public static QNameModule getNewQNameModule(
            StatementContextBase<?, ?, ?> targetCtx,
            StatementContextBase<?, ?, ?> sourceCtx) {
        Object targetStmtArgument = targetCtx.getStatementArgument();

        final StatementContextBase<?, ?, ?> root = sourceCtx.getRoot();
        final QNameModule sourceQNameModule = root.getFromNamespace(
                ModuleCtxToModuleQName.class, root);

        if (targetStmtArgument instanceof QName) {
            QName targetQName = (QName) targetStmtArgument;
            QNameModule targetQNameModule = targetQName.getModule();

            if (targetQNameModule.equals(sourceQNameModule)) {
                return null;
            } else {
                return targetQNameModule;
            }
        } else {
            return null;
        }
    }

    public static boolean needToCopyByAugment(StmtContext<?, ?, ?> stmtContext) {

        Set<StatementDefinition> noCopyDefSet = new HashSet<>();
        noCopyDefSet.add(Rfc6020Mapping.USES);

        StatementDefinition def = stmtContext.getPublicDefinition();
        return !noCopyDefSet.contains(def);
    }

    public static boolean isReusedByAugment(StmtContext<?, ?, ?> stmtContext) {

        Set<StatementDefinition> reusedDefSet = new HashSet<>();
        reusedDefSet.add(Rfc6020Mapping.TYPEDEF);

        StatementDefinition def = stmtContext.getPublicDefinition();

        return reusedDefSet.contains(def);
    }

    public static StatementContextBase<?, ?, ?> getAugmentTargetCtx(
            final Mutable<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> augmentNode) {

        final SchemaNodeIdentifier augmentTargetNode = augmentNode
                .getStatementArgument();
        if (augmentTargetNode == null) {
            throw new IllegalArgumentException(
                    "Augment argument null, something bad happened in some of previous parsing phases");
        }

        List<StatementContextBase<?, ?, ?>> rootStatementCtxList = new LinkedList<>();
        if (augmentTargetNode.isAbsolute()) {

            QNameModule module = augmentTargetNode.getPathFromRoot().iterator()
                    .next().getModule();

            StatementContextBase<?, ?, ?> rootStatementCtx = (StatementContextBase<?, ?, ?>) augmentNode
                    .getFromNamespace(NamespaceToModule.class, module);
            rootStatementCtxList.add(rootStatementCtx);

            final Map<?, ?> subModules = rootStatementCtx
                    .getAllFromNamespace(IncludedModuleContext.class);
            if (subModules != null) {
                rootStatementCtxList
                        .addAll((Collection<? extends StatementContextBase<?, ?, ?>>) subModules
                                .values());
            }

        } else {
            StatementContextBase<?, ?, ?> parent = (StatementContextBase<?, ?, ?>) augmentNode
                    .getParentContext();
            if (StmtContextUtils.producesDeclared(parent, UsesStatement.class)) {
                rootStatementCtxList.add(parent.getParentContext());
            } else {
                // error
            }
        }

        StatementContextBase<?, ?, ?> augmentTargetCtx = null;
        for (final StatementContextBase<?, ?, ?> rootStatementCtx : rootStatementCtxList) {
            augmentTargetCtx = findCtxOfNodeInRoot(rootStatementCtx,
                    augmentTargetNode);
            if (augmentTargetCtx != null)
                break;
        }

        return augmentTargetCtx;
    }

    @Nullable
    public static StatementContextBase<?, ?, ?> findCtxOfNodeInSubstatements(
            StatementContextBase<?, ?, ?> rootStmtCtx,
            final Iterable<QName> path) {

        StatementContextBase<?, ?, ?> parent = rootStmtCtx;

        Iterator<QName> pathIter = path.iterator();
        while (pathIter.hasNext()) {
            QName nextPathQName = pathIter.next();
            StatementContextBase<?, ?, ?> foundSubstatement = getSubstatementByQName(
                    parent, nextPathQName);

            if (foundSubstatement == null) {
                return null;
            }
            if (!pathIter.hasNext()) {
                return foundSubstatement;
            }

            parent = foundSubstatement;
        }

        return null;
    }

    public static StatementContextBase<?, ?, ?> getSubstatementByQName(
            StatementContextBase<?, ?, ?> parent, QName nextPathQName) {

        Collection<StatementContextBase<?, ?, ?>> declaredSubstatement = parent
                .declaredSubstatements();
        Collection<StatementContextBase<?, ?, ?>> effectiveSubstatement = parent
                .effectiveSubstatements();

        Collection<StatementContextBase<?, ?, ?>> allSubstatements = new LinkedList<>();
        allSubstatements.addAll(declaredSubstatement);
        allSubstatements.addAll(effectiveSubstatement);

        for (StatementContextBase<?, ?, ?> substatement : allSubstatements) {
            Object substatementArgument = substatement.getStatementArgument();
            QName substatementQName;
            if (substatementArgument instanceof QName) {
                substatementQName = (QName) substatementArgument;

                if (isSupportedAugmentTarget(substatement)
                        && nextPathQName.getLocalName().equals(
                                substatementQName.getLocalName())) {
                    return substatement;
                }
            } // augment to extesion
            else if (StmtContextUtils.producesDeclared(substatement,
                    UnknownStatementImpl.class)
                    && substatementArgument instanceof String) {
                if (nextPathQName.getLocalName().equals(substatementArgument)) {
                    String message = "Module '"+substatement.getRoot().getStatementArgument()+"': augment into extension '"+substatementArgument+"'.";
                    LOG.warn(message);
                    return substatement;
                }
            }
        }

        return null;
    }

    public static boolean isSupportedAugmentTarget(
            StatementContextBase<?, ?, ?> substatementCtx) {

        /*
         * :TODO Substatement must be allowed augment target type e.g.
         * Container, etc... and must be not for example grouping, identity etc.
         * It is problem in case when more than one substatements have the same
         * QName, for example Grouping and Container are siblings and they have
         * the same QName. We must find the Container and the Grouping must be
         * ignored as disallowed augment target.
         */

        Collection<?> allowedAugmentTargets = substatementCtx.getFromNamespace(
                ValidationBundlesNamespace.class,
                ValidationBundleType.SUPPORTED_AUGMENT_TARGETS);

        return allowedAugmentTargets == null
                || allowedAugmentTargets.isEmpty()
                || allowedAugmentTargets.contains(substatementCtx
                        .getPublicDefinition());
    }

    @Nullable
    public static StatementContextBase<?, ?, ?> findCtxOfNodeInRoot(
            StatementContextBase<?, ?, ?> rootStmtCtx,
            final SchemaNodeIdentifier node) {
        return findCtxOfNodeInSubstatements(rootStmtCtx, node.getPathFromRoot());
    }
}
