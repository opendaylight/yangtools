/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.RootStatementContext;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

// FIXME: Move this to the AugmentStatementDefinition#ApplyAction
public final class AugmentUtils {
    private AugmentUtils() {
    }

    public static void copyFromSourceToTarget(final StatementContextBase<?, ?, ?> sourceCtx,
            final StatementContextBase<?, ?, ?> targetCtx) {
        copyDeclaredStmts(sourceCtx, targetCtx);
        copyEffectiveStmts(sourceCtx, targetCtx);
    }

    // FIXME: Declared statements should not be copied.
    private static void copyDeclaredStmts(final StatementContextBase<?, ?, ?> sourceCtx,
            final StatementContextBase<?, ?, ?> targetCtx) {

        final CopyType typeOfCopy = sourceCtx.getParentContext().getPublicDefinition()
                .getDeclaredRepresentationClass().equals(UsesStatement.class) ? CopyType.ADDED_BY_USES_AUGMENTATION
                : CopyType.ADDED_BY_AUGMENTATION;

        for (final StatementContextBase<?, ?, ?> originalStmtCtx : sourceCtx.declaredSubstatements()) {
            if (!StmtContextUtils.areFeaturesSupported(originalStmtCtx)) {
                continue;
            }
            if (needToCopyByAugment(originalStmtCtx)) {
                validateNodeCanBeCopiedByAugment(originalStmtCtx, targetCtx, typeOfCopy);

                final StatementContextBase<?, ?, ?> copy = originalStmtCtx.createCopy(targetCtx, typeOfCopy);
                targetCtx.addEffectiveSubstatement(copy);
            } else if (isReusedByAugment(originalStmtCtx)) {
                targetCtx.addEffectiveSubstatement(originalStmtCtx);
            }
        }
    }

    private static void copyEffectiveStmts(final StatementContextBase<?, ?, ?> sourceCtx,
            final StatementContextBase<?, ?, ?> targetCtx) {
        final CopyType typeOfCopy = sourceCtx.getParentContext().getPublicDefinition()
                .getDeclaredRepresentationClass().equals(UsesStatement.class) ? CopyType.ADDED_BY_USES_AUGMENTATION
                : CopyType.ADDED_BY_AUGMENTATION;

        for (final StatementContextBase<?, ?, ?> originalStmtCtx : sourceCtx.effectiveSubstatements()) {
            if (needToCopyByAugment(originalStmtCtx)) {
                validateNodeCanBeCopiedByAugment(originalStmtCtx, targetCtx, typeOfCopy);

                final StatementContextBase<?, ?, ?> copy = originalStmtCtx.createCopy(targetCtx, typeOfCopy);
                targetCtx.addEffectiveSubstatement(copy);
            } else if (isReusedByAugment(originalStmtCtx)) {
                targetCtx.addEffectiveSubstatement(originalStmtCtx);
            }
        }
    }

    private static void validateNodeCanBeCopiedByAugment(final StatementContextBase<?, ?, ?> sourceCtx,
            final StatementContextBase<?, ?, ?> targetCtx, final CopyType typeOfCopy) {

        if (sourceCtx.getPublicDefinition().getDeclaredRepresentationClass().equals(WhenStatement.class)) {
            return;
        }

        if (typeOfCopy == CopyType.ADDED_BY_AUGMENTATION && reguiredCheckOfMandatoryNodes(sourceCtx, targetCtx)) {
            checkForMandatoryNodes(sourceCtx);
        }

        final List<StatementContextBase<?, ?, ?>> targetSubStatements = new Builder<StatementContextBase<?, ?, ?>>()
                .addAll(targetCtx.declaredSubstatements()).addAll(targetCtx.effectiveSubstatements()).build();

        for (final StatementContextBase<?, ?, ?> subStatement : targetSubStatements) {

            final boolean sourceIsDataNode = DataDefinitionStatement.class.isAssignableFrom(sourceCtx
                    .getPublicDefinition().getDeclaredRepresentationClass());
            final boolean targetIsDataNode = DataDefinitionStatement.class.isAssignableFrom(subStatement
                    .getPublicDefinition().getDeclaredRepresentationClass());
            final boolean qNamesEqual = sourceIsDataNode && targetIsDataNode
                    && Objects.equals(sourceCtx.getStatementArgument(), subStatement.getStatementArgument());

            InferenceException.throwIf(qNamesEqual, sourceCtx.getStatementSourceReference(),
                    "An augment cannot add node named '%s' because this name is already used in target",
                    sourceCtx.rawStatementArgument());
        }
    }

    private static void checkForMandatoryNodes(final StatementContextBase<?, ?, ?> sourceCtx) {
        if (StmtContextUtils.isNonPresenceContainer(sourceCtx)) {
            /*
             * We need to iterate over both declared and effective sub-statements,
             * because a mandatory node can be:
             * a) declared in augment body
             * b) added to augment body also via uses of a grouping and
             * such sub-statements are stored in effective sub-statements collection.
             */
            for (final StatementContextBase<?, ?, ?> sourceSubStatement : Iterables.concat(
                    sourceCtx.declaredSubstatements(), sourceCtx.declaredSubstatements())) {
                checkForMandatoryNodes(sourceSubStatement);
            }
        }

        InferenceException.throwIf(StmtContextUtils.isMandatoryNode(sourceCtx),
                sourceCtx.getStatementSourceReference(),
                "An augment cannot add node '%s' because it is mandatory and in module different than target",
                sourceCtx.rawStatementArgument());
    }

    private static boolean reguiredCheckOfMandatoryNodes(final StatementContextBase<?, ?, ?> sourceCtx,
            StatementContextBase<?, ?, ?> targetCtx) {
        /*
         * If the statement argument is not QName, it cannot be mandatory
         * statement, therefore return false and skip mandatory nodes validation
         */
        if (!(sourceCtx.getStatementArgument() instanceof QName)) {
            return false;
        }
        final QName sourceStmtQName = (QName) sourceCtx.getStatementArgument();

        final RootStatementContext<?, ?, ?> root = targetCtx.getRoot();
        do {
            Verify.verify(targetCtx.getStatementArgument() instanceof QName,
                    "Argument of augment target statement must be QName.");
            final QName targetStmtQName = (QName) targetCtx.getStatementArgument();
            /*
             * If target is from another module, return true and perform
             * mandatory nodes validation
             */
            if (!Utils.belongsToTheSameModule(targetStmtQName, sourceStmtQName)) {
                return true;
            } else {
                /*
                 * If target or one of its parent is a presence container from
                 * the same module, return false and skip mandatory nodes
                 * validation
                 */
                if (StmtContextUtils.isPresenceContainer(targetCtx)) {
                    return false;
                }
            }
        } while ((targetCtx = targetCtx.getParentContext()) != root);

        /*
         * All target node's parents belong to the same module as source node,
         * therefore return false and skip mandatory nodes validation.
         */
        return false;
    }

    private static final Set<Rfc6020Mapping> NOCOPY_DEF_SET = ImmutableSet.of(Rfc6020Mapping.USES, Rfc6020Mapping.WHEN,
            Rfc6020Mapping.DESCRIPTION, Rfc6020Mapping.REFERENCE, Rfc6020Mapping.STATUS);

    public static boolean needToCopyByAugment(final StmtContext<?, ?, ?> stmtContext) {
        return !NOCOPY_DEF_SET.contains(stmtContext.getPublicDefinition());
    }

    private static final Set<Rfc6020Mapping> REUSED_DEF_SET = ImmutableSet.of(Rfc6020Mapping.TYPEDEF);

    public static boolean isReusedByAugment(final StmtContext<?, ?, ?> stmtContext) {
        return REUSED_DEF_SET.contains(stmtContext.getPublicDefinition());
    }

    static boolean isSupportedAugmentTarget(final StatementContextBase<?, ?, ?> substatementCtx) {

        /*
         * :TODO Substatement must be allowed augment target type e.g.
         * Container, etc... and must not be for example grouping, identity etc.
         * It is problem in case when more than one substatements have the same
         * QName, for example Grouping and Container are siblings and they have
         * the same QName. We must find the Container and the Grouping must be
         * ignored as disallowed augment target.
         */

        final Collection<?> allowedAugmentTargets = substatementCtx.getFromNamespace(ValidationBundlesNamespace.class,
                ValidationBundleType.SUPPORTED_AUGMENT_TARGETS);

        // if no allowed target is returned we consider all targets allowed
        return allowedAugmentTargets == null || allowedAugmentTargets.isEmpty()
                || allowedAugmentTargets.contains(substatementCtx.getPublicDefinition());
    }
}
