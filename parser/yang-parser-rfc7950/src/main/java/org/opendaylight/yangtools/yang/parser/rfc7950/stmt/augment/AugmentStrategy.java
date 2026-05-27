/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.Iterators;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
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
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.RootStmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A strategy for copying statements from a {@code augment} statement to its {@code target node}.
 */
abstract sealed class AugmentStrategy {
    /**
     * Common semantics when the {@code augment} is free to add mandatory nodes anywhere.
     */
    @NonNullByDefault
    private static final class Disregard extends AugmentStrategy {
        /**
         * Instance servicing the case when the {@code augment} is a substatement to either {@code module} or
         * {@code submodule} statement.
         */
        static final Disregard INSTANCE = new Disregard(CopyType.ADDED_BY_AUGMENTATION);
        /**
         * Instance servicing the case when {@code augment} statement is a substatement to the {@code uses} statement.
         */
        static final Disregard USES = new Disregard(CopyType.ADDED_BY_USES_AUGMENTATION);

        private Disregard(final CopyType copyType) {
            super(copyType);
        }

        @Override
        boolean computeRejectMandatory(final QNameModule augmentModule, final SchemaNodeIdentifier augmentArg,
                final StmtContext<?, ?, ?> target) {
            return false;
        }

        @Override
        Iterator<CommonStmtCtx> mandatoryNodesOf(final StmtContext<?, ?, ?> stmt) {
            // should never be called
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Common superclass for strategies which are potentially rejecting mandatory nodes.
     */
    @NonNullByDefault
    private abstract static sealed class Reject extends AugmentStrategy {
        Reject() {
            super(CopyType.ADDED_BY_AUGMENTATION);
        }

        @Override
        final boolean computeRejectMandatory(final QNameModule augmentModule, final SchemaNodeIdentifier augmentArg,
                final StmtContext<?, ?, ?> target) {
            // we are walking from target towards root, looking for a statement which implies mandatory nodes can be
            // introduced
            final var identifiers = augmentArg.getNodeIdentifiers().reversed().iterator();
            var arg = identifiers.next();
            verifyArgument(arg, target);
            var current = target;

            // first deal with the case of this augment being layered on top of a previous augmentation from the same
            // module
            while (augmentModule.equals(arg.getModule())) {
                if (impliesPermitMandatory(current)) {
                    return false;
                }

                // This could be an augmentation stacked on top of a previous augmentation from the same module, which
                // is conditional -- in which case we do not run further checks
                if (current.history().getLastOperation() == CopyType.ADDED_BY_AUGMENTATION) {
                    final var original = current.previousCopyCtx();
                    if (original != null) {
                        final var origArg = original.getArgument();
                        if (!(origArg instanceof QName origQName)) {
                            throw new VerifyException("Unexpected statement argument " + origArg);
                        }

                        if (augmentModule.equals(origQName.getModule())) {
                            final var parentAugmentation = getParentAugmentation(original);
                            // FIXME: defer to resolver as this check is only applicable if the augmentation is hosted
                            //        in a RFC7950 source
                            if (parentAugmentation.hasSubstatement(WhenEffectiveStatement.class)) {
                                return false;
                            }
                        }
                    }
                }

                final var next = identifiers.next();
                final var parent = current.coerceParentContext();
                if (parent instanceof RootStmtContext) {
                    throw new VerifyException("reached root " + parent + " from " + target);
                }
                verifyArgument(next, parent);
                arg = next;
                current = parent;
            }

            // we have reached a node from a module that is not the augment's home module: defer to subclass to
            // determine the policy
            return computeRejectMandatory(current);
        }

        abstract boolean computeRejectMandatory(StmtContext<?, ?, ?> stmt);

        boolean impliesPermitMandatory(final StmtContext<?, ?, ?> stmt) {
            // if target or one of the target's ancestors from the same namespace
            //   - is a presence container, or
            //   - is non-mandatory list, or
            //   - is non-mandatory choice
            // we can terminate early as it is not a mandatory node container as per RFC6020 section 3.1
            if (stmt.produces(ContainerStatement.DEF)) {
                return containsPresenceSubStmt(stmt);
            }
            if (stmt.produces(ListStatement.DEF)) {
                // FIXME: YANGTOOLS-1894: this check is unstable when deviations are in play
                final var minElements = firstSubstatementAttributeOf(stmt, MinElementsStatement.DEF);
                return minElements == null || minElements.matchesAll();
            }
            if (stmt.produces(ChoiceStatement.DEF)) {
                // FIXME: YANGTOOLS-1894: this check is unstable when deviations are in play
                final var mandatory = firstSubstatementAttributeOf(stmt, MandatoryStatement.DEF);
                return mandatory == null || !mandatory;
            }
            return false;
        }

        private static StmtContext<?, ?, ?> getParentAugmentation(final StmtContext<?, ?, ?> child) {
            var parent = child.coerceParentContext();
            while (!parent.produces(AugmentStatement.DEF)) {
                parent = parent.coerceParentContext();
            }
            return parent;
        }

        private static void verifyArgument(final QName expected, final BoundStmtCtx<?> stmt) {
            final var targetArg = stmt.argument();
            if (!expected.equals(targetArg)) {
                throw new VerifyException(stmt + " does not match " + expected);
            }
        }

        @Override
        final Iterator<CommonStmtCtx> mandatoryNodesOf(final StmtContext<?, ?, ?> stmt) {
            final var nodes = recMandatoryNodesOf(stmt);
            return nodes != null ? nodes : Collections.emptyIterator();
        }

        private static @Nullable Iterator<CommonStmtCtx> recMandatoryNodesOf(final StmtContext<?, ?, ?> stmt) {
            final var mandatory = tryMandatoryNode(stmt);
            return mandatory != null ? mandatory : tryNonPresenceContainer(stmt);
        }

        private static @Nullable Iterator<CommonStmtCtx> tryMandatoryNode(final StmtContext<?, ?, ?> stmt) {
            if (stmt.producesAnyOf(LeafStatement.DEF, ChoiceStatement.DEF, AnyxmlStatement.DEF)) {
                return Boolean.TRUE.equals(firstSubstatementAttributeOf(stmt, MandatoryStatement.DEF))
                    ? Iterators.singletonIterator(stmt) : Collections.emptyIterator();
            }
            if (stmt.producesAnyOf(ListStatement.DEF, LeafListStatement.DEF)) {
                final var minElements = firstSubstatementAttributeOf(stmt, MinElementsStatement.DEF);
                return minElements != null && minElements.lowerInt() > -1
                    ? Iterators.singletonIterator(stmt) : Collections.emptyIterator();
            }
            return null;
        }

        private static @Nullable Iterator<CommonStmtCtx> tryNonPresenceContainer(final StmtContext<?, ?, ?> stmt) {
            if (stmt.produces(ContainerStatement.DEF) && !containsPresenceSubStmt(stmt)) {
                final var ret = new ArrayList<CommonStmtCtx>();
                // We need to iterate over both declared and effective sub-statements, because a mandatory node can
                // be either
                //   a) declared in augment body, or
                //   b) added to augment body also via uses of a grouping and such sub-statements are stored in
                //      effective sub-statements collection.
                for (var sub : stmt.allSubstatements()) {
                    final var nodes = recMandatoryNodesOf(sub);
                    if (nodes != null) {
                        nodes.forEachRemaining(ret::add);
                    }
                }
                return ret.iterator();
            }
            return null;
        }

        private static boolean containsPresenceSubStmt(final StmtContext<?, ?, ?> stmtCtx) {
            return stmtCtx.hasSubstatement(PresenceEffectiveStatement.class);
        }

        private static <A, D extends DeclaredStatement<A>> @Nullable A firstSubstatementAttributeOf(
                final StmtContext<?, ?, ?> ctx, final StatementDefinition<A, D, ?> def) {
            return StmtContextUtils.firstAttributeOf(ctx.allSubstatements(), def.declaredRepresentation());
        }
    }

    /**
     * Common semantics when the {@code augment} must not add mandatory nodes anywhere.
     */
    @NonNullByDefault
    private static final class RejectAll extends Reject {
        static final RejectAll INSTANCE = new RejectAll();

        private RejectAll() {
            // Hidden on purpose
        }

        @Override
        boolean computeRejectMandatory(final StmtContext<?, ?, ?> stmt) {
            // if current is from another module we need to perform mandatory nodes validation
            return true;
        }
    }

    /**
     * Semantics for RFC7950 unconditional {@code augment}, which may introduce non-configuration mandatory nodes.
     */
    @NonNullByDefault
    private static final class RejectNonConfig extends Reject {
        static final RejectNonConfig INSTANCE = new RejectNonConfig();

        private RejectNonConfig() {
            // Hidden on purpose
        }

        @Override
        boolean impliesPermitMandatory(final StmtContext<?, ?, ?> stmt) {
            return super.impliesPermitMandatory(stmt) || isNonConfig(stmt);
        }

        @Override
        boolean computeRejectMandatory(final StmtContext<?, ?, ?> stmt) {
            var current = stmt;
            while (true) {
                if (isNonConfig(current)) {
                    return false;
                }

                final var parent = current.coerceParentContext();
                if (parent instanceof RootStmtContext) {
                    return true;
                }
                current = parent;
            }
        }

        private static boolean isNonConfig(final StmtContext<?, ?, ?> stmt) {
            if (stmt.inStructure()) {
                return true;
            }
            for (var sub : stmt.allSubstatements()) {
                final var cfg = sub.tryDeclaring(ConfigStatement.class);
                if (cfg != null && cfg.isSupportedToBuildEffective()) {
                    return !cfg.getArgument();
                }
            }
            return false;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AugmentStrategy.class);
    /**
     * YANG statements that apply to the {@code augment} statement itself, not to the {@code target node}.
     */
    private static final Set<StatementDefinition<?, ?, ?>> NOCOPY_DEF_SET = Set.of(DescriptionStatement.DEF,
        ReferenceStatement.DEF, StatusStatement.DEF, UsesStatement.DEF, WhenStatement.DEF);

    private final @NonNull CopyType copyType;

    @NonNullByDefault
    private AugmentStrategy(final CopyType copyType) {
        this.copyType = requireNonNull(copyType);
    }

    /**
     * RFC6020 semantics: mandatory nodes must not be introduced.
     */
    @NonNullByDefault
    static final AugmentStrategy rfc6020() {
        return RejectAll.INSTANCE;
    }

    /**
     * RFC7950 semantics and the augmentation is conditional via {@code when}: mandatory nodes may be introduced to any
     * target node.
     */
    @NonNullByDefault
    static final AugmentStrategy conditional() {
        return Disregard.INSTANCE;
    }

    /**
     * RFC7950 semantics and the augmentation is unconditional: mandatory nodes may be introduced only to target nodes
     * which do not represent configuration.
     */
    @NonNullByDefault
    static final AugmentStrategy unconditional() {
        return RejectNonConfig.INSTANCE;
    }

    static final void apply(final @NonNull AugmentStrategyResolver strategyResolver,
            final @NonNull StmtContext<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> augment,
            final @NonNull Mutable<?, ?, ?> target) {
        final var augmentParent = augment.coerceParentContext();

        // 'augment' statement in a 'uses' statement
        if (augmentParent.produces(UsesStatement.DEF)) {
            Disregard.USES.apply(augment, target, false);
            return;
        }

        // 'augment' statement in a 'module' or 'submodule', with target node being ...
        final var augmentModule = augmentParent.currentModule();
        if (augmentModule.equals(target.currentModule())) {
            // ... in the same module
            Disregard.INSTANCE.apply(augment, target, false);
            return;
        }

        // ... in another module
        final var strategy = strategyResolver.strategyFor(augment);
        strategy.apply(augment, target, strategy.computeRejectMandatory(augmentModule, augment.getArgument(), target));
    }

    final void apply(
            final @NonNull StmtContext<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> augment,
            final @NonNull Mutable<?, ?, ?> target, final boolean rejectMandatory) {
        final var unsupported = !augment.isSupportedByFeatures();
        final var declared = augment.declaredSubstatements();
        final var effective = augment.effectiveSubstatements();
        final var buffer = new ArrayList<Mutable<?, ?, ?>>(declared.size() + effective.size());

        for (var stmt : declared) {
            copyStatement(buffer, target, rejectMandatory, stmt, unsupported || !stmt.isSupportedByFeatures());
        }
        for (var stmt : effective) {
            copyStatement(buffer, target, rejectMandatory, stmt, unsupported);
        }

        target.addEffectiveSubstatements(buffer);
    }

    private void copyStatement(final @NonNull List<Mutable<?, ?, ?>> buffer, final @NonNull Mutable<?, ?, ?> target,
            final boolean rejectMandatory, final StmtContext<?, ?, ?> stmt, final boolean unsupported) {
        // do not copy statements that pertain to the augment itself
        if (stmt.producesAnyOf(NOCOPY_DEF_SET)) {
            return;
        }

        // FIXME: These two checks are competing with each other for what gets reported: we can have an augment
        //        overlapping with an on schema tree namespace and the node cannot be legally introduced, for example
        //        because it is introducing a mandatory node into another module.
        //
        //        The problem is that the determination of whether an introduced node is effectively mandatory, depends
        //        on ancestor hierarchy, which can be modified after this code runs by 'deviate' -- and thus can only
        //        be reliably ascertained only after the entire the top-most schema node has transitioned to
        //        EFFECTIVE_MODEL.

        if (rejectMandatory) {
            final var mandatoryNodes = mandatoryNodesOf(stmt);
            if (mandatoryNodes.hasNext()) {
                final var ex = newMandatoryViolation(mandatoryNodes.next());
                mandatoryNodes.forEachRemaining(mandatoryNode -> {
                    LOG.debug("Additional mandatory violation at {}", mandatoryNode.sourceReference());
                    ex.addSuppressed(newMandatoryViolation(mandatoryNode));
                });
                throw ex;
            }
        }

        // data definition statements must not collide on schema tree namespace
        final var dataDef = stmt.tryDeclaring(DataDefinitionStatement.class);
        if (dataDef != null) {
            final var arg = dataDef.getArgument();
            final var existing = target.namespaceItem(ParserNamespaces.schemaTree(), arg);
            if (existing != null) {
                throw new InferenceException(dataDef, """
                    Cannot add %s statement named '%s' because augment target already contains a %s statement with the \
                    same name (originating from %s)""", dataDef.publicDefinition().humanName(), arg.getLocalName(),
                    existing.publicDefinition().humanName(), existing.sourceReference());
            }
        }

        // We always copy statements, but if either the source statement or the augmentation which causes it are not
        // supported to build we also mark the target as such.
        final var copy = target.childCopyOf(stmt, copyType);
        if (unsupported) {
            copy.setUnsupported();
        }
        buffer.add(copy);
    }

    @NonNullByDefault
    abstract boolean computeRejectMandatory(QNameModule augmentModule, SchemaNodeIdentifier augmentArg,
        StmtContext<?, ?, ?> target);

    /**
     * {@return all statements which is causing specified statement to be considered mandatory}
     * @param stmt the statement
     */
    @NonNullByDefault
    abstract Iterator<CommonStmtCtx> mandatoryNodesOf(StmtContext<?, ?, ?> stmt);

    @NonNullByDefault
    private static InferenceException newMandatoryViolation(final CommonStmtCtx mandatoryNode) {
        return new InferenceException(mandatoryNode,
            "An augment cannot add node '%s' because it is mandatory and in module different than target",
            mandatoryNode.rawArgument());
    }
}
