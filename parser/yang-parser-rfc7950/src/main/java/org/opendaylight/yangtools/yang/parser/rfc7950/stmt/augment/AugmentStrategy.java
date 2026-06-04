/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
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
    RFC6020(CopyType.ADDED_BY_AUGMENTATION, false),
    /**
     * RFC7950 semantics and the augmentation is unconditional: mandatory nodes may be introduced only to target nodes
     * which do not represent configuration.
     */
    RFC7950_UNCONDITIONAL(CopyType.ADDED_BY_AUGMENTATION, false),
    /**
     * RFC7950 semantics and the augmentation is conditional via {@code when}: mandatory nodes may be introduced to any
     * target node.
     */
    RFC7950_CONDITIONAL(CopyType.ADDED_BY_AUGMENTATION, true),
    /**
     * Common semantics when the {@code augment} target resides in the same module as the {@code augment} statement.
     */
    SAME_MODULE(CopyType.ADDED_BY_AUGMENTATION, true),
    /**
     * Common semantics when the {@code augment} statement is a substatement to the {@code uses} statement.
     */
    USES(CopyType.ADDED_BY_USES_AUGMENTATION, true);

    /**
     * YANG statements that apply to the {@code augment} statement itself, not to the {@code target node}.
     */
    private static final Set<StatementDefinition<?, ?, ?>> NOCOPY_DEF_SET = Set.of(
        DescriptionStatement.DEF,
        ReferenceStatement.DEF,
        StatusStatement.DEF,
        UsesStatement.DEF,
        WhenStatement.DEF);

    // FIXME: YANGTOOLS-1890: correct the logic around this boolean
    private final boolean skipCheckOfMandatoryNodes;
    private final @NonNull CopyType copyType;

    @NonNullByDefault
    AugmentStrategy(final CopyType copyType, final boolean skipCheckOfMandatoryNodes) {
        this.copyType = requireNonNull(copyType);
        this.skipCheckOfMandatoryNodes = skipCheckOfMandatoryNodes;
    }

    void apply(final @NonNull StmtContext<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> augment,
            final @NonNull Mutable<?, ?, ?> target) {
        final var unsupported = !augment.isSupportedByFeatures();
        final var declared = augment.declaredSubstatements();
        final var effective = augment.effectiveSubstatements();
        final var buffer = new ArrayList<Mutable<?, ?, ?>>(declared.size() + effective.size());

        for (var stmt : declared) {
            copyStatement(stmt, target, buffer, unsupported || !stmt.isSupportedByFeatures());
        }
        for (var stmt : effective) {
            copyStatement(stmt, target, buffer, unsupported);
        }

        target.addEffectiveSubstatements(buffer);
    }

    private void copyStatement(final StmtContext<?, ?, ?> stmt, final Mutable<?, ?, ?> target,
            final List<Mutable<?, ?, ?>> buffer, final boolean unsupported) {
        // We always copy statements, but if either the source statement or the augmentation which causes it are not
        // supported to build we also mark the target as such.
        if (!stmt.producesAnyOf(NOCOPY_DEF_SET)) {
            validateNodeCanBeCopiedByAugment(stmt, target);

            final var copy = target.childCopyOf(stmt, copyType);
            if (unsupported) {
                copy.setUnsupported();
            }
            buffer.add(copy);
        } else if (!unsupported && stmt.produces(TypedefStatement.DEF)) {
            // FIXME: what is this branch doing, really?
            //        Typedef's policy would imply a replica, hence normal target.childCopyOf(original, typeOfCopy)
            //        would suffice.
            //        What does the !unsupported thing want to do?
            buffer.add(stmt.replicaAsChildOf(target));
        }
    }

    private void validateNodeCanBeCopiedByAugment(final StmtContext<?, ?, ?> sourceCtx,
            final Mutable<?, ?, ?> targetCtx) {
        if (!skipCheckOfMandatoryNodes && requireCheckOfMandatoryNodes(sourceCtx, targetCtx)) {
            checkForMandatoryNodes(sourceCtx);
        }

        // Data definition statements must not collide on their namespace
        if (sourceCtx.producesDeclared(DataDefinitionStatement.class)) {
            for (var subStatement : targetCtx.allSubstatements()) {
                final var declaring = subStatement.tryDeclaring(DataDefinitionStatement.class);
                if (declaring != null && Objects.equals(sourceCtx.argument(), declaring.argument())) {
                    throw new InferenceException(sourceCtx,
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

            // If target or one of the target's ancestors from the same namespace
            // - is a presence container, or
            // - is non-mandatory choice, or
            // - is non-mandatory list
            // we can terminate early as it is not a mandatory node container as per RFC6020 section 3.1.
            if (isPresenceContainer(targetCtx) || isNotMandatoryNodeOfType(targetCtx, ChoiceStatement.DEF)
                || isNotMandatoryNodeOfType(targetCtx, ListStatement.DEF)) {
                return false;
            }

            // This could be an augmentation stacked on top of a previous augmentation from the same module, which is
            // conditional -- in which case we do not run further checks
            if (targetCtx.history().getLastOperation() == CopyType.ADDED_BY_AUGMENTATION) {
                final var original = targetCtx.previousCopyCtx();
                if (original != null) {
                    final var origArg = original.getArgument();
                    verify(origArg instanceof QName, "Unexpected statement argument %s", origArg);

                    if (sourceStmtQName.getModule().equals(((QName) origArg).getModule())
                        && getParentAugmentation(original).hasSubstatement(WhenEffectiveStatement.class)) {
                        return false;
                    }
                }
            }
        } while ((targetCtx = targetCtx.getParentContext()) != root);

        // FIXME: we should never reach here

        /*
         * All target node's parents belong to the same module as source node,
         * therefore return false and skip mandatory nodes validation.
         */
        return false;
    }

    private static void checkForMandatoryNodes(final StmtContext<?, ?, ?> sourceCtx) {
        if (isNonPresenceContainer(sourceCtx)) {
            /*
             * We need to iterate over both declared and effective sub-statements,
             * because a mandatory node can be:
             * a) declared in augment body
             * b) added to augment body also via uses of a grouping and
             * such sub-statements are stored in effective sub-statements collection.
             */
            sourceCtx.allSubstatementsStream().forEach(AugmentStrategy::checkForMandatoryNodes);
        }

        InferenceException.throwIf(isMandatoryNode(sourceCtx), sourceCtx,
            "An augment cannot add node '%s' because it is mandatory and in module different than target",
            sourceCtx.rawArgument());
    }

    /**
     * Checks whether statement context is a mandatory leaf, choice, anyxml, list or leaf-list according to RFC6020 or
     * not.
     *
     * @param stmtCtx statement context
     * @return true if it is a mandatory leaf, choice, anyxml, list or leaf-list according to RFC6020.
     */
    private static boolean isMandatoryNode(final StmtContext<?, ?, ?> stmtCtx) {
        // FIXME: check for MandatoryStatementAwareDeclaredStatement, renamed to MandatoryStatement.Parent via
        //        producesDeclared()
        if (stmtCtx.producesAnyOf(LeafStatement.DEF, ChoiceStatement.DEF, AnyxmlStatement.DEF)) {
            return Boolean.TRUE.equals(firstSubstatementAttributeOf(stmtCtx, MandatoryStatement.DEF));
        }
        // FIXME: check for MultipleElementsDeclaredStatement via producesDeclared()
        if (stmtCtx.producesAnyOf(ListStatement.DEF, LeafListStatement.DEF)) {
            final var minElements = firstSubstatementAttributeOf(stmtCtx, MinElementsStatement.DEF);
            return minElements != null && minElements.lowerInt() > -1;
        }
        return false;
    }

    /**
     * Checks whether a statement context is a statement of supplied statement definition and whether it is not
     * mandatory leaf, choice, anyxml, list or leaf-list according to RFC6020.
     *
     * @param stmtCtx statement context
     * @param stmtDef statement definition
     * @return true if supplied statement context is a statement of supplied statement definition and if it is not
     *         a mandatory leaf, choice, anyxml, list or leaf-list according to RFC6020
     */
    private static boolean isNotMandatoryNodeOfType(final StmtContext<?, ?, ?> stmtCtx,
            final StatementDefinition<?, ?, ?> stmtDef) {
        return stmtCtx.produces(stmtDef) && !isMandatoryNode(stmtCtx);
    }

    /**
     * Checks whether statement context is a non-presence container or not.
     *
     * @param stmtCtx statement context
     * @return true if it is a non-presence container
     */
    private static boolean isNonPresenceContainer(final StmtContext<?, ?, ?> stmtCtx) {
        return stmtCtx.produces(ContainerStatement.DEF) && !containsPresenceSubStmt(stmtCtx);
    }

    /**
     * Checks whether statement context is a presence container or not.
     *
     * @param stmtCtx statement context
     * @return true if it is a presence container
     */
    private static boolean isPresenceContainer(final StmtContext<?, ?, ?> stmtCtx) {
        return stmtCtx.produces(ContainerStatement.DEF) && containsPresenceSubStmt(stmtCtx);
    }

    private static boolean containsPresenceSubStmt(final StmtContext<?, ?, ?> stmtCtx) {
        return stmtCtx.hasSubstatement(PresenceEffectiveStatement.class);
    }

    private static <A, D extends DeclaredStatement<A>> @Nullable A firstSubstatementAttributeOf(
            final StmtContext<?, ?, ?> ctx, final StatementDefinition<A, D, ?> def) {
        return StmtContextUtils.firstAttributeOf(ctx.allSubstatements(), def.declaredRepresentation());
    }

    private static StmtContext<?, ?, ?> getParentAugmentation(final StmtContext<?, ?, ?> child) {
        var parent = child.coerceParentContext();
        while (!parent.produces(AugmentStatement.DEF)) {
            parent = parent.coerceParentContext();
        }
        return parent;
    }
}
