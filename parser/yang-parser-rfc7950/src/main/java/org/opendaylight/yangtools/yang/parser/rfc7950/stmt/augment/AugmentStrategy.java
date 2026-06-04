/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

/**
 * A strategy for copying statements from a {@code augment} statement to its {@code target node}.
 */
enum AugmentStrategy {
    /**
     * RFC6020 semantics: mandatory nodes must not be introduced.
     */
    RFC6020(false),
    /**
     * RFC7950 semantics and the augmentation is unconditional: mandatory nodes may be introduced only to target nodes
     * which do not represent configuration.
     */
    RFC7950_UNCONDITIONAL(false),
    /**
     * RFC7950 semantics and the augmentation is conditional via {@code when}: mandatory nodes may be introduced to any
     * target node.
     */
    RFC7950_CONDITIONAL(true);

    /**
     * YANG statements that apply to the {@code augment} statement itself, not to the {@code target node}.
     */
    private static final Set<YangStmtMapping> NOCOPY_DEF_SET = Set.of(YangStmtMapping.DESCRIPTION,
        YangStmtMapping.REFERENCE, YangStmtMapping.STATUS, YangStmtMapping.USES, YangStmtMapping.WHEN);

    // FIXME: YANGTOOLS-1890: correct the logic around this boolean
    private final boolean skipCheckOfMandatoryNodes;

    AugmentStrategy(final boolean skipCheckOfMandatoryNodes) {
        this.skipCheckOfMandatoryNodes = skipCheckOfMandatoryNodes;
    }

    @NonNullByDefault
    void copyFromSourceToTarget(final StmtContext<?, ?, ?> sourceCtx, final Mutable<?, ?, ?> targetCtx) {
        final var typeOfCopy = sourceCtx.coerceParentContext().producesDeclared(UsesStatement.class)
            ? CopyType.ADDED_BY_USES_AUGMENTATION : CopyType.ADDED_BY_AUGMENTATION;
        final boolean unsupported = !sourceCtx.isSupportedByFeatures();

        final var declared = sourceCtx.declaredSubstatements();
        final var effective = sourceCtx.effectiveSubstatements();
        final var buffer = new ArrayList<Mutable<?, ?, ?>>(declared.size() + effective.size());

        for (var originalStmtCtx : declared) {
            copyStatement(originalStmtCtx, targetCtx, typeOfCopy, buffer,
                unsupported || !originalStmtCtx.isSupportedByFeatures());
        }
        for (var originalStmtCtx : effective) {
            copyStatement(originalStmtCtx, targetCtx, typeOfCopy, buffer, unsupported);
        }

        targetCtx.addEffectiveSubstatements(buffer);
    }

    private void copyStatement(final StmtContext<?, ?, ?> original, final Mutable<?, ?, ?> target,
            final CopyType typeOfCopy, final Collection<Mutable<?, ?, ?>> buffer, final boolean unsupported) {
        // We always copy statements, but if either the source statement or the augmentation which causes it are not
        // supported to build we also mark the target as such.
        if (!NOCOPY_DEF_SET.contains(original.publicDefinition())) {
            validateNodeCanBeCopiedByAugment(original, target, typeOfCopy);

            final Mutable<?, ?, ?> copy = target.childCopyOf(original, typeOfCopy);
            if (unsupported) {
                copy.setUnsupported();
            }
            buffer.add(copy);
        } else if (!unsupported && original.publicDefinition() == YangStmtMapping.TYPEDEF) {
            // FIXME: what is this branch doing, really?
            //        Typedef's policy would imply a replica, hence normal target.childCopyOf(original, typeOfCopy)
            //        would suffice.
            //        What does the !unsupported thing want to do?
            buffer.add(original.replicaAsChildOf(target));
        }
    }

    private void validateNodeCanBeCopiedByAugment(final StmtContext<?, ?, ?> sourceCtx,
            final Mutable<?, ?, ?> targetCtx, final CopyType typeOfCopy) {
        if (!skipCheckOfMandatoryNodes && typeOfCopy == CopyType.ADDED_BY_AUGMENTATION
                && requireCheckOfMandatoryNodes(sourceCtx, targetCtx)) {
            checkForMandatoryNodes(sourceCtx);
        }

        // Data definition statements must not collide on their namespace
        if (sourceCtx.producesDeclared(DataDefinitionStatement.class)) {
            for (StmtContext<?, ?, ?> subStatement : targetCtx.allSubstatements()) {
                if (subStatement.producesDeclared(DataDefinitionStatement.class)) {
                    InferenceException.throwIf(Objects.equals(sourceCtx.argument(), subStatement.argument()), sourceCtx,
                        "An augment cannot add node named '%s' because this name is already used in target",
                        sourceCtx.rawArgument());
                }
            }
        }
    }

    private static boolean requireCheckOfMandatoryNodes(final StmtContext<?, ?, ?> sourceCtx,
            Mutable<?, ?, ?> targetCtx) {
        /*
         * If the statement argument is not QName, it cannot be mandatory
         * statement, therefore return false and skip mandatory nodes validation
         */
        final Object arg = sourceCtx.argument();
        if (!(arg instanceof QName sourceStmtQName)) {
            return false;
        }
        // RootStatementContext, for example
        final Mutable<?, ?, ?> root = targetCtx.getRoot();
        do {
            final Object targetArg = targetCtx.argument();
            verify(targetArg instanceof QName, "Argument of augment target statement must be QName, not %s", targetArg);
            final QName targetStmtQName = (QName) targetArg;
            /*
             * If target is from another module, return true and perform mandatory nodes validation
             */
            if (!targetStmtQName.getModule().equals(sourceStmtQName.getModule())) {
                return true;
            }

            /*
             * If target or one of the target's ancestors from the same namespace
             * is a presence container
             * or is non-mandatory choice
             * or is non-mandatory list
             * return false and skip mandatory nodes validation, because these nodes
             * are not mandatory node containers according to RFC 6020 section 3.1.
             */
            if (StmtContextUtils.isPresenceContainer(targetCtx)
                || StmtContextUtils.isNotMandatoryNodeOfType(targetCtx, YangStmtMapping.CHOICE)
                || StmtContextUtils.isNotMandatoryNodeOfType(targetCtx, YangStmtMapping.LIST)) {
                return false;
            }

            // This could be an augmentation stacked on top of a previous augmentation from the same module, which is
            // conditional -- in which case we do not run further checks
            if (targetCtx.history().getLastOperation() == CopyType.ADDED_BY_AUGMENTATION) {
                final var optPrevCopy = targetCtx.getPreviousCopyCtx();
                if (optPrevCopy.isPresent()) {
                    final var original = optPrevCopy.orElseThrow();
                    final var origArg = original.getArgument();
                    verify(origArg instanceof QName, "Unexpected statement argument %s", origArg);

                    if (sourceStmtQName.getModule().equals(((QName) origArg).getModule())
                        && AbstractAugmentStatementSupport.hasWhenSubstatement(getParentAugmentation(original))) {
                        return false;
                    }
                }
            }
        } while ((targetCtx = targetCtx.getParentContext()) != root);

        /*
         * All target node's parents belong to the same module as source node,
         * therefore return false and skip mandatory nodes validation.
         */
        return false;
    }

    private static void checkForMandatoryNodes(final StmtContext<?, ?, ?> sourceCtx) {
        if (StmtContextUtils.isNonPresenceContainer(sourceCtx)) {
            /*
             * We need to iterate over both declared and effective sub-statements,
             * because a mandatory node can be:
             * a) declared in augment body
             * b) added to augment body also via uses of a grouping and
             * such sub-statements are stored in effective sub-statements collection.
             */
            sourceCtx.allSubstatementsStream().forEach(AugmentStrategy::checkForMandatoryNodes);
        }

        InferenceException.throwIf(StmtContextUtils.isMandatoryNode(sourceCtx), sourceCtx,
            "An augment cannot add node '%s' because it is mandatory and in module different than target",
            sourceCtx.rawArgument());
    }

    private static StmtContext<?, ?, ?> getParentAugmentation(final StmtContext<?, ?, ?> child) {
        StmtContext<?, ?, ?> parent = verifyNotNull(child.getParentContext(), "Child %s has not parent", child);
        while (parent.publicDefinition() != YangStmtMapping.AUGMENT) {
            parent = verifyNotNull(parent.getParentContext(), "Failed to find augmentation parent of %s", child);
        }
        return parent;
    }
}
