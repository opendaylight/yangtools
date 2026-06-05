/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import static com.google.common.base.Verify.verifyNotNull;
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
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.RootStmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

/**
 * A strategy for copying statements from a {@code augment} statement to its {@code target node}.
 */
// TODO: refactor to a sealed class so RFC* members and SAME_MODULE/USES members have two disjuct superclasses
enum AugmentStrategy {
    /**
     * RFC6020 semantics: mandatory nodes must not be introduced.
     */
    RFC6020(Extractor.REGULAR, CopyType.ADDED_BY_AUGMENTATION),
    /**
     * RFC7950 semantics and the augmentation is unconditional: mandatory nodes may be introduced only to target nodes
     * which do not represent configuration.
     */
    RFC7950_UNCONDITIONAL(Extractor.REGULAR, CopyType.ADDED_BY_AUGMENTATION),
    /**
     * RFC7950 semantics and the augmentation is conditional via {@code when}: mandatory nodes may be introduced to any
     * target node.
     */
    RFC7950_CONDITIONAL(Extractor.NOOP, CopyType.ADDED_BY_AUGMENTATION),
    /**
     * Common semantics when the {@code augment} target resides in the same module as the {@code augment} statement.
     */
    SAME_MODULE(Extractor.NOOP, CopyType.ADDED_BY_AUGMENTATION),
    /**
     * Common semantics when the {@code augment} statement is a substatement to the {@code uses} statement.
     */
    USES(Extractor.NOOP, CopyType.ADDED_BY_USES_AUGMENTATION);

    @NonNullByDefault
    private enum Extractor {
        NOOP() {
            @Override
            Iterator<CommonStmtCtx> mandatoryNodesOf(final StmtContext<?, ?, ?> stmt) {
                return Collections.emptyIterator();
            }
        },
        REGULAR() {
            @Override
            Iterator<CommonStmtCtx> mandatoryNodesOf(final StmtContext<?, ?, ?> stmt) {
                final var nodes = recMandatoryNodesOf(stmt);
                return nodes != null ? nodes : Collections.emptyIterator();
            }

            private static @Nullable Iterator<CommonStmtCtx> recMandatoryNodesOf(final StmtContext<?, ?, ?> stmt) {
                final var mandatory = tryMandatoryNode(stmt);
                return mandatory != null ? mandatory : tryNonPresenceContainer(stmt);
            }

            private static @Nullable Iterator<CommonStmtCtx> tryMandatoryNode(final StmtContext<?, ?, ?> stmt) {
                final var def = stmt.publicDefinition();
                if (def == YangStmtMapping.LEAF || def == YangStmtMapping.CHOICE || def == YangStmtMapping.ANYXML) {
                    return Boolean.TRUE.equals(StmtContextUtils.firstSubstatementAttributeOf(stmt,
                        MandatoryStatement.class)) ? Iterators.singletonIterator(stmt) : Collections.emptyIterator();
                }
                if (def == YangStmtMapping.LIST || def == YangStmtMapping.LEAF_LIST) {
                    final var minElements = StmtContextUtils.firstSubstatementAttributeOf(stmt,
                        MinElementsStatement.class);
                    return minElements != null && minElements > 0
                        ? Iterators.singletonIterator(stmt) : Collections.emptyIterator();
                }
                return null;
            }

            private static @Nullable Iterator<CommonStmtCtx> tryNonPresenceContainer(final StmtContext<?, ?, ?> stmt) {
                if (stmt.publicDefinition() == YangStmtMapping.CONTAINER
                    && !stmt.hasSubstatement(PresenceEffectiveStatement.class)) {
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
        };

        /**
         * {@return all statements which is causing specified statement to be considered mandatory}
         * @param stmt the statement
         */
        abstract Iterator<CommonStmtCtx> mandatoryNodesOf(StmtContext<?, ?, ?> stmt);
    }

    /**
     * YANG statements that apply to the {@code augment} statement itself, not to the {@code target node}.
     */
    private static final Set<YangStmtMapping> NOCOPY_DEF_SET = Set.of(YangStmtMapping.DESCRIPTION,
        YangStmtMapping.REFERENCE, YangStmtMapping.STATUS, YangStmtMapping.USES, YangStmtMapping.WHEN);

    private final @NonNull Extractor extractor;
    private final @NonNull CopyType copyType;

    @NonNullByDefault
    AugmentStrategy(final Extractor extractor, final CopyType copyType) {
        this.extractor = requireNonNull(extractor);
        this.copyType = requireNonNull(copyType);
    }

    final void apply(
            final @NonNull StmtContext<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> augment,
            final @NonNull Mutable<?, ?, ?> target) {
        final var augmentModule = augment.definingModule();
        final var unsupported = !augment.isSupportedByFeatures();
        final var declared = augment.declaredSubstatements();
        final var effective = augment.effectiveSubstatements();
        final var buffer = new ArrayList<Mutable<?, ?, ?>>(declared.size() + effective.size());

        for (var stmt : declared) {
            copyStatement(buffer, target, augmentModule, stmt, unsupported || !stmt.isSupportedByFeatures());
        }
        for (var stmt : effective) {
            copyStatement(buffer, target, augmentModule, stmt, unsupported);
        }

        target.addEffectiveSubstatements(buffer);
    }

    private void copyStatement(final @NonNull List<Mutable<?, ?, ?>> buffer, final @NonNull Mutable<?, ?, ?> target,
            final @NonNull QNameModule augmentModule,final StmtContext<?, ?, ?> stmt, final boolean unsupported) {
        // do not copy statements that pertain to the augment itself
        if (NOCOPY_DEF_SET.contains(stmt.publicDefinition())) {
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

        final var mandatoryNodes = extractor.mandatoryNodesOf(stmt);
        if (mandatoryNodes.hasNext() && needMandatoryCheckIn(augmentModule, target)) {
            final var ex = newMandatoryViolation(mandatoryNodes.next());
            mandatoryNodes.forEachRemaining(mandatoryNode -> ex.addSuppressed(newMandatoryViolation(mandatoryNode)));
            throw ex;
        }

        // data definition statements must not collide on schema tree namespace
        if (stmt.producesDeclared(DataDefinitionStatement.class)) {
            final var arg = (QName) stmt.getArgument();
            final var existing = target.namespaceItem(ParserNamespaces.schemaTree(), arg);
            if (existing != null) {
                throw new InferenceException(stmt, """
                    Cannot add %s statement named '%s' because augment target already contains a %s statement with the \
                    same name (originating from %s)""", stmt.publicDefinition().getStatementName().getLocalName(),
                    arg.getLocalName(), existing.publicDefinition().getStatementName().getLocalName(),
                    existing.sourceReference());
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
    private static InferenceException newMandatoryViolation(final CommonStmtCtx mandatoryNode) {
        return new InferenceException(mandatoryNode,
            "An augment cannot add node '%s' because it is mandatory and in module different than target",
            mandatoryNode.rawArgument());
    }

    // FIXME: YANGTOOLS-1890: this method should be provided AugmentInferenceAction by when it determines the target
    //                        node has the same namespace. In other cases it should just return true -- as is implied
    //                        by the first loop iteration
    @NonNullByDefault
    private static boolean needMandatoryCheckIn(final QNameModule augmentModule, final StmtContext<?, ?, ?> target) {
        var current = target;
        while (true) {
            final var currentArg = current.argument();
            if (!(currentArg instanceof QName currentQName)) {
                throw new VerifyException("Argument of augment target statement must be QName, not " + currentArg);
            }

            // if current is from another module we need to perform mandatory nodes validation
            if (!currentQName.getModule().equals(augmentModule)) {
                return true;
            }

            // if target or one of the target's ancestors from the same namespace
            //   - is a presence container, or
            //   - is non-mandatory list, or
            //   - is non-mandatory choice
            // we can terminate early as it is not a mandatory node container as per RFC6020 section 3.1
            if (current.publicDefinition() == YangStmtMapping.CONTAINER) {
                if (current.hasSubstatement(PresenceEffectiveStatement.class)) {
                    return false;
                }
            } else if (current.publicDefinition() == YangStmtMapping.LIST) {
                // FIXME: YANGTOOLS-1894: this check is unstable when deviations are in play
                final var minElements = StmtContextUtils.firstSubstatementAttributeOf(current,
                    MinElementsStatement.class);
                if (minElements == null || minElements == 0) {
                    return false;
                }
            } else if (current.publicDefinition() == YangStmtMapping.CHOICE) {
                // FIXME: YANGTOOLS-1894: this check is unstable when deviations are in play
                final var mandatory = StmtContextUtils.firstSubstatementAttributeOf(current, MandatoryStatement.class);
                if (mandatory == null || !mandatory) {
                    return false;
                }
            }

            // This could be an augmentation stacked on top of a previous augmentation from the same module, which is
            // conditional -- in which case we do not run further checks
            if (current.history().getLastOperation() == CopyType.ADDED_BY_AUGMENTATION) {
                final var optPrevCopy = current.getPreviousCopyCtx();
                if (optPrevCopy.isPresent()) {
                    final var original = optPrevCopy.orElseThrow();
                    final var origArg = original.getArgument();
                    if (!(origArg instanceof QName origQName)) {
                        throw new VerifyException("Unexpected statement argument " + origArg);
                    }

                    if (augmentModule.equals(origQName.getModule())
                        // FIXME: defer to resolver as this check is only applicable if the augmentation is hosted in a
                        //        RFC7950 source
                        && AugmentStatementSupport.hasWhenSubstatement(getParentAugmentation(original))) {
                        return false;
                    }
                }
            }

            final var parent = current.coerceParentContext();
            if (parent instanceof RootStmtContext) {
                throw new VerifyException("reached root " + parent + " from " + target);
            }
            current = parent;
        }
    }

    private static StmtContext<?, ?, ?> getParentAugmentation(final StmtContext<?, ?, ?> child) {
        var parent = verifyNotNull(child.getParentContext(), "Child %s has not parent", child);
        while (parent.publicDefinition() != YangStmtMapping.AUGMENT) {
            parent = verifyNotNull(parent.getParentContext(), "Failed to find augmentation parent of %s", child);
        }
        return parent;
    }
}
