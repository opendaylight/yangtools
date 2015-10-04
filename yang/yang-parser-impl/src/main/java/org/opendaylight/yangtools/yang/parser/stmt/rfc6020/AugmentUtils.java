/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

// FIXME: Move this to the AugmentStatementDefinition#ApplyAction
public final class AugmentUtils {
    private AugmentUtils() {
    }

    public static void copyFromSourceToTarget(final StatementContextBase<?, ?, ?> sourceCtx,
            final StatementContextBase<?, ?, ?> targetCtx) throws SourceException {
        copyDeclaredStmts(sourceCtx, targetCtx);
        copyEffectiveStmts(sourceCtx, targetCtx);
    }

    // FIXME: Declared statements should not be copied.
    private static void copyDeclaredStmts(final StatementContextBase<?, ?, ?> sourceCtx,
            final StatementContextBase<?, ?, ?> targetCtx) throws SourceException {

        final List<StatementContextBase<?, ?, ?>> subStatements = new Builder<StatementContextBase<?, ?, ?>>()
                .addAll(targetCtx.declaredSubstatements()).addAll(targetCtx.effectiveSubstatements()).build();
        boolean sourceAndTargetInSameModule = Utils.getRootModuleQName(sourceCtx).equals(
                Utils.getRootModuleQName(targetCtx));

        TypeOfCopy typeOfCopy = sourceCtx.getParentContext().getPublicDefinition().getDeclaredRepresentationClass()
                .equals(UsesStatement.class) ? TypeOfCopy.ADDED_BY_USES_AUGMENTATION : TypeOfCopy.ADDED_BY_AUGMENTATION;

        for (StatementContextBase<?, ?, ?> originalStmtCtx : sourceCtx.declaredSubstatements()) {
            if (needToCopyByAugment(originalStmtCtx)) {
                validateNodeCanBeCopiedByAugment(originalStmtCtx, subStatements, sourceAndTargetInSameModule);

                StatementContextBase<?, ?, ?> copy = originalStmtCtx.createCopy(targetCtx, typeOfCopy);
                targetCtx.addEffectiveSubstatement(copy);
            } else if (isReusedByAugment(originalStmtCtx)) {
                targetCtx.addEffectiveSubstatement(originalStmtCtx);
            }
        }
    }

    private static void copyEffectiveStmts(final StatementContextBase<?, ?, ?> sourceCtx,
            final StatementContextBase<?, ?, ?> targetCtx) throws SourceException {

        final List<StatementContextBase<?, ?, ?>> subStatements = new Builder<StatementContextBase<?, ?, ?>>()
                .addAll(targetCtx.declaredSubstatements()).addAll(targetCtx.effectiveSubstatements()).build();
        boolean sourceAndTargetInSameModule = Utils.getRootModuleQName(sourceCtx).equals(
                Utils.getRootModuleQName(targetCtx));

        TypeOfCopy typeOfCopy = sourceCtx.getParentContext().getPublicDefinition().getDeclaredRepresentationClass()
                .equals(UsesStatement.class) ? TypeOfCopy.ADDED_BY_USES_AUGMENTATION : TypeOfCopy.ADDED_BY_AUGMENTATION;

        for (StatementContextBase<?, ?, ?> originalStmtCtx : sourceCtx.effectiveSubstatements()) {
            if (needToCopyByAugment(originalStmtCtx)) {
                validateNodeCanBeCopiedByAugment(originalStmtCtx, subStatements, sourceAndTargetInSameModule);

                StatementContextBase<?, ?, ?> copy = originalStmtCtx.createCopy(targetCtx, typeOfCopy);
                targetCtx.addEffectiveSubstatement(copy);
            } else if (isReusedByAugment(originalStmtCtx)) {
                targetCtx.addEffectiveSubstatement(originalStmtCtx);
            }
        }
    }

    private static void validateNodeCanBeCopiedByAugment(final StatementContextBase<?, ?, ?> sourceCtx,
            final List<StatementContextBase<?, ?, ?>> targetSubStatements, final boolean sourceAndTargetInSameModule) {

        if (sourceCtx.getPublicDefinition().getDeclaredRepresentationClass().equals(WhenStatement.class)) {
            return;
        }

        if (!sourceAndTargetInSameModule) {
            final List<StatementContextBase<?, ?, ?>> sourceSubStatements = new Builder<StatementContextBase<?, ?, ?>>()
                    .addAll(sourceCtx.declaredSubstatements()).addAll(sourceCtx.effectiveSubstatements()).build();

            for (final StatementContextBase<?, ?, ?> sourceSubStatement : sourceSubStatements) {
                if (sourceSubStatement.getPublicDefinition().getDeclaredRepresentationClass()
                        .equals(MandatoryStatement.class)) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "An augment cannot add node '%s' because it is mandatory and in module different from target",
                                    sourceCtx.rawStatementArgument()));
                }
            }
        }

        for (final StatementContextBase<?, ?, ?> subStatement : targetSubStatements) {

            final boolean sourceIsDataNode = DataDefinitionStatement.class.isAssignableFrom(sourceCtx
                    .getPublicDefinition().getDeclaredRepresentationClass());
            final boolean targetIsDataNode = DataDefinitionStatement.class.isAssignableFrom(subStatement
                    .getPublicDefinition().getDeclaredRepresentationClass());
            boolean qNamesEqual = sourceIsDataNode && targetIsDataNode
                    && Objects.equals(sourceCtx.getStatementArgument(), subStatement.getStatementArgument());

            if (qNamesEqual) {
                throw new IllegalStateException(String.format(
                        "An augment cannot add node named '%s' because this name is already used in target",
                        sourceCtx.rawStatementArgument()));
            }
        }
    }

    private static final Set<Rfc6020Mapping> NOCOPY_DEV_SET = ImmutableSet.of(Rfc6020Mapping.USES);

    public static boolean needToCopyByAugment(final StmtContext<?, ?, ?> stmtContext) {
        return !NOCOPY_DEV_SET.contains(stmtContext.getPublicDefinition());
    }

    private static final Set<Rfc6020Mapping> REUSED_DEF_SET = ImmutableSet.of(Rfc6020Mapping.TYPEDEF);

    public static boolean isReusedByAugment(final StmtContext<?, ?, ?> stmtContext) {
        return REUSED_DEF_SET.contains(stmtContext.getPublicDefinition());
    }

    static boolean isSupportedAugmentTarget(final StatementContextBase<?, ?, ?> substatementCtx) {

        /*
         * :TODO Substatement must be allowed augment target type e.g. Container, etc... and must be not for example
         * grouping, identity etc. It is problem in case when more than one substatements have the same QName, for
         * example Grouping and Container are siblings and they have the same QName. We must find the Container and the
         * Grouping must be ignored as disallowed augment target.
         */

        Collection<?> allowedAugmentTargets = substatementCtx.getFromNamespace(ValidationBundlesNamespace.class,
                ValidationBundleType.SUPPORTED_AUGMENT_TARGETS);

        // if no allowed target is returned we consider all targets allowed
        return allowedAugmentTargets == null || allowedAugmentTargets.isEmpty()
                || allowedAugmentTargets.contains(substatementCtx.getPublicDefinition());
    }
}
