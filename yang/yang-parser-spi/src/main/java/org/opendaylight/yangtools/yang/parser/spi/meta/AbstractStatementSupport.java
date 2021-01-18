/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Parent;

/**
 * Class providing necessary support for processing a YANG statement. This class is intended to be subclassed
 * by developers who want to add semantic support for a statement to a parser reactor.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
public abstract class AbstractStatementSupport<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        implements StatementDefinition, StatementFactory<A, D, E>, StatementSupport<A, D, E> {

    // FIXME: add javadocs
    protected static abstract class EffectiveComparator<A, D extends DeclaredStatement<A>> implements Immutable {
        private static final @NonNull EffectiveComparator<?, ?> CONTEXT_INDEPENDENT =
            new ReusingEffectiveComparator<>(CopyPolicy.CONTEXT_INDEPENDENT);
        private static final @NonNull EffectiveComparator<?, ?> IGNORE =
            new IllegalEffectiveComparator<>(CopyPolicy.IGNORE);
        private static final @NonNull EffectiveComparator<?, ?> REJECT =
            new IllegalEffectiveComparator<>(CopyPolicy.REJECT);
        private static final @NonNull EffectiveComparator<?, ?> ACCEPT =
            new ReusingEffectiveComparator<>(CopyPolicy.REJECT);

        final @NonNull CopyPolicy copyPolicy;

        EffectiveComparator(final CopyPolicy copyPolicy) {
            this.copyPolicy = requireNonNull(copyPolicy);
        }

        @SuppressWarnings("unchecked")
        public static final <A, D extends DeclaredStatement<A>>
                @NonNull EffectiveComparator<A, D> contextIndependent() {
            return (EffectiveComparator<A, D>) CONTEXT_INDEPENDENT;
        }

        @SuppressWarnings("unchecked")
        public static final <A, D extends DeclaredStatement<A>> @NonNull EffectiveComparator<A, D> ignore() {
            return (EffectiveComparator<A, D>) IGNORE;
        }

        @SuppressWarnings("unchecked")
        public static final <A, D extends DeclaredStatement<A>> @NonNull EffectiveComparator<A, D> reject() {
            return (EffectiveComparator<A, D>) REJECT;
        }

        protected abstract boolean canReuseCurrent(@NonNull Current<A, D> copy, @NonNull Current<A, D> current,
            @NonNull Collection<? extends EffectiveStatement<?, ?>> substatements);

        @Deprecated
        static <A, D extends DeclaredStatement<A>> EffectiveComparator<A, D> compat(final CopyPolicy copyPolicy) {
            switch (copyPolicy) {
                case CONTEXT_INDEPENDENT:
                    return contextIndependent();
                case DECLARED_COPY:
                    return new CopyingEffectiveComparator<>(CopyPolicy.DECLARED_COPY);
                case IGNORE:
                    return ignore();
                case REJECT:
                    return reject();
                default:
                    throw new IllegalStateException("Unsupported policy " + copyPolicy);
            }
        }
    }

    private static final class ReusingEffectiveComparator<A, D extends DeclaredStatement<A>>
            extends EffectiveComparator<A, D> {
        ReusingEffectiveComparator(final CopyPolicy copyPolicy) {
            super(copyPolicy);
        }

        @Override
        protected boolean canReuseCurrent(final Current<A, D> copy, final Current<A, D> current,
                final Collection<? extends EffectiveStatement<?, ?>> substatements) {
            return true;
        }
    }

    private static final class CopyingEffectiveComparator<A, D extends DeclaredStatement<A>>
            extends EffectiveComparator<A, D> {
        CopyingEffectiveComparator(final CopyPolicy copyPolicy) {
            super(copyPolicy);
        }

        @Override
        protected boolean canReuseCurrent(final Current<A, D> copy, final Current<A, D> current,
                final Collection<? extends EffectiveStatement<?, ?>> substatements) {
            return false;
        }
    }

    private static final class IllegalEffectiveComparator<A, D extends DeclaredStatement<A>>
            extends EffectiveComparator<A, D> {
        IllegalEffectiveComparator(final CopyPolicy copyPolicy) {
            super(copyPolicy);
        }

        @Override
        protected boolean canReuseCurrent(final Current<A, D> copy, final Current<A, D> current,
                final Collection<? extends EffectiveStatement<?, ?>> substatements) {
            throw new UnsupportedOperationException("This implementation should never be invoked");
        }
    }

    protected static abstract class AbstractEffectiveComparator<A, D extends DeclaredStatement<A>>
            extends EffectiveComparator<A, D> {
        protected AbstractEffectiveComparator() {
            super(CopyPolicy.DECLARED_COPY);
        }
    }

    private final @NonNull EffectiveComparator<A, D> comparator;
    private final @NonNull StatementDefinition type;
    private final @NonNull CopyPolicy copyPolicy;

    @Beta
    protected AbstractStatementSupport(final StatementDefinition publicDefinition,
            final EffectiveComparator<A, D> comparator) {
        this.type = requireNonNull(publicDefinition);
        this.comparator = requireNonNull(comparator);
        this.copyPolicy = comparator.copyPolicy;
        checkArgument(publicDefinition != this);
    }

    @Beta
    @Deprecated
    // FIXME: remove this constructor
    protected AbstractStatementSupport(final StatementDefinition publicDefinition, final CopyPolicy copyPolicy) {
        this(publicDefinition, EffectiveComparator.compat(copyPolicy));
    }

    @Override
    public final StatementDefinition getPublicView() {
        return type;
    }

    @Override
    public final CopyPolicy copyPolicy() {
        return copyPolicy;
    }

    @Override
    public final boolean canReuseCurrent(final Current<A, D> copy, final Current<A, D> current,
             final Collection<? extends EffectiveStatement<?, ?>> substatements) {
        return comparator.canReuseCurrent(copy, current, substatements);
    }

//    @Override
//    public final Current<A, D> effectiveCopyOf(final Current<A, D> stmt, final Parent parent, final CopyType copyType,
//            final QNameModule targetModule) {
//        switch (copyPolicy) {
//            case CONTEXT_INDEPENDENT:
//                return stmt;
//            case DECLARED_COPY:
//                return effectivelyEqual(stmt, parent, copyType, targetModule)
//                    ? stmt : stmt.withParent(parent, copyType, targetModule);
//            default:
//                throw new VerifyException("Attempted to apply " + copyPolicy);
//        }
//    }

    //
    // FIXME: Are these useful?
    //
    protected boolean effectivelyEqual(final Current<A, D> stmt, final Parent parent, final CopyType copyType,
            final QNameModule targetModule) {
        return false;
    }

    protected static final boolean isSameHistory(final CopyHistory copyHistory, final CopyType copyType) {
        // FIXME: compare these ... how exactly? see what childCopyOf() does
        return false;
    }

    protected static final boolean isSameModule(final QNameModule currentModule, final QNameModule targetModule) {
        return targetModule == null || targetModule.equals(currentModule);
    }

    // Semantic comparison of parent
    protected static final boolean isSameParent(final StmtContext<?, ?, ?> parent,
            final StmtContext<?, ?, ?> newParent) {
        // TODO: This should never happen, I think. Perhaps an assertion is in order?
        return newParent.equals(parent);
    }

    // FIXME: see ^^^

    @Override
    public void onStatementAdded(final StmtContext.Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * {@inheritDoc}.
     *
     * <p>
     * Subclasses of this class may override this method to perform actions on this event or register a modification
     * action using {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     */
    @Override
    public void onPreLinkageDeclared(final StmtContext.Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * {@inheritDoc}.
     *
     * <p>
     * Subclasses of this class may override this method to perform actions on this event or register a modification
     * action using {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     */
    @Override
    public void onLinkageDeclared(final StmtContext.Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * {@inheritDoc}.
     *
     * <p>
     * Subclasses of this class may override this method to perform actions on this event or register a modification
     * action using {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     */
    @Override
    public void onStatementDefinitionDeclared(final StmtContext.Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * {@inheritDoc}.
     *
     * <p>
     * Subclasses of this class may override this method to perform actions on this event or register a modification
     * action using {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     */
    @Override
    public void onFullDefinitionDeclared(final StmtContext.Mutable<A, D, E> stmt) {
        final SubstatementValidator validator = getSubstatementValidator();
        if (validator != null) {
            validator.validate(stmt);
        }
    }

    @Override
    public boolean hasArgumentSpecificSupports() {
        // Most of statement supports don't have any argument specific supports, so return 'false'.
        return false;
    }

    @Override
    public StatementSupport<?, ?, ?> getSupportSpecificForArgument(final String argument) {
        // Most of statement supports don't have any argument specific supports, so return null.
        return null;
    }

    /**
     * Returns corresponding substatement validator of a statement support.
     *
     * @return substatement validator or null, if substatement validator is not defined
     */
    protected abstract @Nullable SubstatementValidator getSubstatementValidator();
}
