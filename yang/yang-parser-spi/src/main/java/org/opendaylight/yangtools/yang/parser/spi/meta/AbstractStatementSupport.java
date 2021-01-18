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
import com.google.common.base.VerifyException;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

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
    /**
     * A baseline class for implementing the {@link StatementFactory#canReuseCurrent(Current, Current, Collection)}
     * contract in a manner which is consistent with a statement's {@link CopyPolicy}.
     *
     * @param <A> Argument type
     * @param <D> Declared Statement representation
     */
    public abstract static class StatementPolicy<A, D extends DeclaredStatement<A>> implements Immutable {
        final @NonNull CopyPolicy copyPolicy;

        StatementPolicy(final CopyPolicy copyPolicy) {
            this.copyPolicy = requireNonNull(copyPolicy);
        }

        /**
         * Return an {@link StatementPolicy} for {@link CopyPolicy#CONTEXT_INDEPENDENT}.
         *
         * @param <A> Argument type
         * @param <D> Declared Statement representation
         * @return Context-independent policy
         */
        @SuppressWarnings("unchecked")
        public static final <A, D extends DeclaredStatement<A>> @NonNull StatementPolicy<A, D> contextIndependent() {
            return (StatementPolicy<A, D>) AlwaysReuse.CONTEXT_INDEPENDENT;
        }

        /**
         * Return an {@link StatementPolicy} for {@link CopyPolicy#IGNORE}.
         *
         * @param <A> Argument type
         * @param <D> Declared Statement representation
         * @return Ignoring policy
         */
        @SuppressWarnings("unchecked")
        public static final <A, D extends DeclaredStatement<A>> @NonNull StatementPolicy<A, D> ignore() {
            return (StatementPolicy<A, D>) AlwaysFail.IGNORE;
        }

        /**
         * Return an {@link StatementPolicy} for {@link CopyPolicy#REJECT}.
         *
         * @param <A> Argument type
         * @param <D> Declared Statement representation
         * @return Rejecting statement policy
         */
        @SuppressWarnings("unchecked")
        public static final <A, D extends DeclaredStatement<A>> @NonNull StatementPolicy<A, D> reject() {
            return (StatementPolicy<A, D>) AlwaysFail.REJECT;
        }

        /**
         * Return an {@link StatementPolicy} for {@link CopyPolicy#DECLARED_COPY}, deferring to a
         * {@link StatementEquality} for individual decisions.
         *
         * @param <A> Argument type
         * @param <D> Declared Statement representation
         * @param equality {@link StatementEquality} to apply to effective statements
         * @return Rejecting statement policy
         */
        public static final <A, D extends DeclaredStatement<A>> @NonNull StatementPolicy<A, D> copyDeclared(
                final @NonNull StatementEquality<A, D> equality) {
            return new EqualSemantics<>(equality);
        }

        abstract boolean canReuseCurrent(@NonNull Current<A, D> copy, @NonNull Current<A, D> current,
            @NonNull Collection<? extends EffectiveStatement<?, ?>> substatements);

        @Deprecated
        @SuppressWarnings("unchecked")
        static <A, D extends DeclaredStatement<A>> StatementPolicy<A, D> compat(final CopyPolicy copyPolicy) {
            switch (copyPolicy) {
                case CONTEXT_INDEPENDENT:
                    return contextIndependent();
                case DECLARED_COPY:
                    return (StatementPolicy<A, D>) AlwaysCopy.DECLARED_COPY;
                case IGNORE:
                    return ignore();
                case REJECT:
                    return reject();
                default:
                    throw new IllegalStateException("Unsupported policy " + copyPolicy);
            }
        }

        private static final class AlwaysCopy<A, D extends DeclaredStatement<A>> extends StatementPolicy<A, D> {
            @Deprecated
            static final @NonNull AlwaysCopy<?, ?> DECLARED_COPY = new AlwaysCopy<>(CopyPolicy.DECLARED_COPY);

            AlwaysCopy(final CopyPolicy copyPolicy) {
                super(copyPolicy);
            }

            @Override
            boolean canReuseCurrent(final Current<A, D> copy, final Current<A, D> current,
                    final Collection<? extends EffectiveStatement<?, ?>> substatements) {
                return false;
            }
        }

        private static final class AlwaysReuse<A, D extends DeclaredStatement<A>> extends StatementPolicy<A, D> {
            static final @NonNull AlwaysReuse<?, ?> CONTEXT_INDEPENDENT =
                new AlwaysReuse<>(CopyPolicy.CONTEXT_INDEPENDENT);

            private AlwaysReuse(final CopyPolicy copyPolicy) {
                super(copyPolicy);
            }

            @Override
            boolean canReuseCurrent(final Current<A, D> copy, final Current<A, D> current,
                    final Collection<? extends EffectiveStatement<?, ?>> substatements) {
                return true;
            }
        }

        private static final class AlwaysFail<A, D extends DeclaredStatement<A>> extends StatementPolicy<A, D> {
            static final @NonNull AlwaysFail<?, ?> IGNORE = new AlwaysFail<>(CopyPolicy.IGNORE);
            static final @NonNull AlwaysFail<?, ?> REJECT = new AlwaysFail<>(CopyPolicy.REJECT);

            private AlwaysFail(final CopyPolicy copyPolicy) {
                super(copyPolicy);
            }

            @Override
            boolean canReuseCurrent(final Current<A, D> copy, final Current<A, D> current,
                    final Collection<? extends EffectiveStatement<?, ?>> substatements) {
                throw new VerifyException("This implementation should never be invoked");
            }
        }

        private static final class EqualSemantics<A, D extends DeclaredStatement<A>> extends StatementPolicy<A, D> {
            private final @NonNull StatementEquality<A, D> equality;

            EqualSemantics(final @NonNull StatementEquality<A, D> equality) {
                super(CopyPolicy.DECLARED_COPY);
                this.equality = requireNonNull(equality);
            }

            @Override
            boolean canReuseCurrent(final Current<A, D> copy, final Current<A, D> current,
                    final Collection<? extends EffectiveStatement<?, ?>> substatements) {
                return equality.canReuseCurrent(copy, current, substatements);
            }
        }
    }

    /**
     * Abstract base class for comparators associated with statements with a {@link CopyPolicy#DECLARED_COPY} copy
     * policy.
     *
     * @param <A> Argument type
     * @param <D> Declared Statement representation
     */
    @FunctionalInterface
    public interface StatementEquality<A, D extends DeclaredStatement<A>> {
        /**
         * Determine whether {@code current} statement has the same semantics as the provided copy. See the contract
         * specification of {@link StatementFactory#canReuseCurrent(Current, Current, Collection)}.
         *
         * @param copy Copy of current effective context
         * @param current Current effective context
         * @param substatements Current effective substatements
         * @return True if {@code current} can be reused in place of {@code copy}, false if the copy needs to be used.
         */
        boolean canReuseCurrent(@NonNull Current<A, D> copy, @NonNull Current<A, D> current,
            @NonNull Collection<? extends EffectiveStatement<?, ?>> substatements);
    }

    private final @NonNull StatementPolicy<A, D> policy;
    private final @NonNull StatementDefinition type;
    private final @NonNull CopyPolicy copyPolicy;

    @Beta
    protected AbstractStatementSupport(final StatementDefinition publicDefinition, final StatementPolicy<A, D> policy) {
        this.type = requireNonNull(publicDefinition);
        this.policy = requireNonNull(policy);
        this.copyPolicy = policy.copyPolicy;
        checkArgument(publicDefinition != this);
    }

    @Beta
    @Deprecated
    // FIXME: remove this constructor
    protected AbstractStatementSupport(final StatementDefinition publicDefinition, final CopyPolicy copyPolicy) {
        this(publicDefinition, StatementPolicy.compat(copyPolicy));
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
        return policy.canReuseCurrent(copy, current, substatements);
    }

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
