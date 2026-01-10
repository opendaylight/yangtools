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
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Support for processing concrete YANG statement.
 *
 * <p>This interface is intended to be implemented by developers, which want to introduce support of statement to
 * parser. Consider subclassing {@link AbstractStatementSupport} for easier implementation of this interface.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
public abstract class StatementSupport<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        implements StatementFactory<A, D, E> {
    /**
     * A baseline class for implementing the {@link StatementFactory#canReuseCurrent(Current, Current, List)}
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
         * Return a {@link StatementPolicy} for {@link CopyPolicy#CONTEXT_INDEPENDENT}.
         *
         * @param <A> Argument type
         * @param <D> Declared Statement representation
         * @return Context-independent policy
         */
        @SuppressWarnings("unchecked")
        public static final <A, D extends DeclaredStatement<A>> @NonNull StatementPolicy<A, D> contextIndependent() {
            return (StatementPolicy<A, D>) EqualSemantics.CONTEXT_INDEPENDENT;
        }

        /**
         * Return a {@link StatementPolicy} for {@link CopyPolicy#EXACT_REPLICA}.
         *
         * @param <A> Argument type
         * @param <D> Declared Statement representation
         * @return Exact-replica policy
         */
        @SuppressWarnings("unchecked")
        public static final <A, D extends DeclaredStatement<A>> @NonNull StatementPolicy<A, D> exactReplica() {
            return (StatementPolicy<A, D>) EqualSemantics.EXACT_REPLICA;
        }

        /**
         * Return a {@link StatementPolicy} for {@link CopyPolicy#IGNORE}.
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
         * Return a {@link StatementPolicy} for {@link CopyPolicy#REJECT}.
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
         * Return a {@link StatementPolicy} for {@link CopyPolicy#DECLARED_COPY}, deferring to a
         * {@link StatementEquality} for individual decisions.
         *
         * @param <A> Argument type
         * @param <D> Declared Statement representation
         * @param equality {@link StatementEquality} to apply to effective statements
         * @return Equality-based statement policy
         */
        public static final <A, D extends DeclaredStatement<A>> @NonNull StatementPolicy<A, D> copyDeclared(
                final @NonNull StatementEquality<A, D> equality) {
            return new EqualSemantics<>(equality);
        }

        /**
         * Return a {@link StatementPolicy} for {@link CopyPolicy#DECLARED_COPY}, always performing a copy operation.
         *
         * @param <A> Argument type
         * @param <D> Declared Statement representation
         * @return Rejecting statement policy
         */
        @SuppressWarnings("unchecked")
        public static final <A, D extends DeclaredStatement<A>> @NonNull StatementPolicy<A, D> alwaysCopyDeclared() {
            return (StatementPolicy<A, D>) EqualSemantics.ALWAYS_COPY;
        }

        abstract boolean canReuseCurrent(@NonNull Current<A, D> copy, @NonNull Current<A, D> current,
            @NonNull List<? extends EffectiveStatement<?, ?>> substatements);

        private static final class AlwaysFail<A, D extends DeclaredStatement<A>> extends StatementPolicy<A, D> {
            static final @NonNull AlwaysFail<?, ?> IGNORE = new AlwaysFail<>(CopyPolicy.IGNORE);
            static final @NonNull AlwaysFail<?, ?> REJECT = new AlwaysFail<>(CopyPolicy.REJECT);

            private AlwaysFail(final CopyPolicy copyPolicy) {
                super(copyPolicy);
            }

            @Override
            boolean canReuseCurrent(final Current<A, D> copy, final Current<A, D> current,
                    final List<? extends EffectiveStatement<?, ?>> substatements) {
                throw new VerifyException("This implementation should never be invoked");
            }
        }

        private static final class EqualSemantics<A, D extends DeclaredStatement<A>> extends StatementPolicy<A, D> {
            static final @NonNull EqualSemantics<?, ?> ALWAYS_COPY =
                new EqualSemantics<>((copy, stmt, substatements) -> false);
            static final @NonNull EqualSemantics<?, ?> CONTEXT_INDEPENDENT =
                new EqualSemantics<>(CopyPolicy.CONTEXT_INDEPENDENT, (copy, stmt, substatements) -> true);
            static final @NonNull EqualSemantics<?, ?> EXACT_REPLICA =
                new EqualSemantics<>(CopyPolicy.EXACT_REPLICA, (copy, stmt, substatements) -> true);

            private final @NonNull StatementEquality<A, D> equality;

            private EqualSemantics(final CopyPolicy copyPolicy, final StatementEquality<A, D> equality) {
                super(copyPolicy);
                this.equality = requireNonNull(equality);
            }

            EqualSemantics(final StatementEquality<A, D> equality) {
                this(CopyPolicy.DECLARED_COPY, equality);
            }

            @Override
            boolean canReuseCurrent(final Current<A, D> copy, final Current<A, D> current,
                    final List<? extends EffectiveStatement<?, ?>> substatements) {
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
         * specification of {@link StatementFactory#canReuseCurrent(Current, Current, List)}.
         *
         * @param copy Copy of current effective context
         * @param current Current effective context
         * @param substatements Current effective substatements
         * @return True if {@code current} can be reused in place of {@code copy}, false if the copy needs to be used.
         */
        boolean canReuseCurrent(@NonNull Current<A, D> copy, @NonNull Current<A, D> current,
            @NonNull List<? extends EffectiveStatement<?, ?>> substatements);
    }

    /**
     * Projection of {@link StatementSupport}s available within a particular source. This namespace is purely virtual
     * and its behaviour corresponds to {@link NamespaceBehaviour#rootStatementLocal(ParserNamespace)} and is always
     * available.
     * Its contents are derived from {@link StatementSupportBundle}s active in the current {@link ModelProcessingPhase}
     * as well as {@link StatementDefinitions} and {@link StmtContext#yangVersion()} of the source root statement.
     */
    @Beta
    public static final @NonNull ParserNamespace<QName, StatementSupport<?, ?, ?>> NAMESPACE =
        new ParserNamespace<>("statementSupports");

    private final @NonNull StatementPolicy<A, D> policy;
    private final @NonNull StatementDefinition publicDefinition;
    private final @NonNull CopyPolicy copyPolicy;

    protected StatementSupport(final StatementSupport<A, D, E> delegate) {
        checkArgument(delegate != this);
        publicDefinition = delegate.publicDefinition;
        policy = delegate.policy;
        copyPolicy = delegate.copyPolicy;
    }

    protected StatementSupport(final StatementDefinition publicDefinition, final StatementPolicy<A, D> policy) {
        this.publicDefinition = requireNonNull(publicDefinition);
        this.policy = requireNonNull(policy);
        copyPolicy = policy.copyPolicy;
    }

    /**
     * Returns public statement definition, which will be present in built statements.
     *
     * <p>Public statement definition may be used to provide different implementation of statement definition, which
     * will not retain any build specific data or context.
     *
     * @return public statement definition, which will be present in built statements.
     */
    public final @NonNull StatementDefinition getPublicView() {
        return publicDefinition;
    }

    // Appropriate to most definitions
    // Non-final for compatible extensions
    public @NonNull StatementDefinition definition() {
        return publicDefinition;
    }

    /**
     * Return this statement's {@link CopyPolicy}. This is a static value, reflecting how this statement reacts to being
     * replicated to a different context, without reflecting on behaviour of potential substatements, which would come
     * into play in something like:
     *
     * <pre>
     *   <code>
     *     module foo {
     *       namespace foo;
     *       prefix foo;
     *
     *       extension note {
     *         argument string {
     *           type string {
     *             length 1..max;
     *           }
     *         }
     *         description "Can be used in description/reference statements to attach additional notes";
     *       }
     *
     *       description "A nice module extending description statement semantics" {
     *         foo:note "We can now attach description/reference a note.";
     *         foo:note "Also another note";
     *       }
     *     }
     *   </code>
     * </pre>
     *
     * <p>In this scenario, it is the reactor's job to figure out what to do (like talking to substatements).
     *
     * @return This statement's copy policy
     */
    public final @NonNull CopyPolicy copyPolicy() {
        return copyPolicy;
    }

    @Override
    public final boolean canReuseCurrent(final Current<A, D> copy, final Current<A, D> current,
            final List<? extends EffectiveStatement<?, ?>> substatements) {
        return policy.canReuseCurrent(copy, current, substatements);
    }

    @Override
    public EffectiveStatementState extractEffectiveState(final E stmt) {
        // Not implemented for most statements
        return null;
    }

    /**
     * Parses textual representation of argument in object representation.
     *
     * @param ctx Context, which may be used to access source-specific namespaces required for parsing.
     * @param value String representation of value, as was present in text source.
     * @return Parsed value
     * @throws SourceException when an inconsistency is detected.
     */
    public abstract A parseArgumentValue(@NonNull StmtContext<?, ?, ?> ctx, String value);

    /**
     * Adapts the argument value to match a new module. Default implementation returns original value stored in context,
     * which is appropriate for most implementations.
     *
     * @param ctx Context, which may be used to access source-specific namespaces required for parsing.
     * @param targetModule Target module, may not be null.
     * @return Adapted argument value.
     */
    public A adaptArgumentValue(final @NonNull StmtContext<A, D, E> ctx, final @NonNull QNameModule targetModule) {
        return ctx.argument();
    }

    /**
     * Invoked when a statement supported by this instance is added to build context. This allows implementations
     * of this interface to start tracking the statement and perform any modifications to the build context hierarchy,
     * accessible via {@link StmtContext#getParentContext()}. One such use is populating the parent's namespaces to
     * allow it to locate this child statement.
     *
     * @param stmt Context of added statement. No substatements are available.
     */
    public void onStatementAdded(final @NonNull Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * Invoked when statement is closed during {@link ModelProcessingPhase#SOURCE_PRE_LINKAGE} phase, only substatements
     * from this and previous phase are available.
     *
     * <p>Implementation may use method to perform actions on this event or register modification action using
     * {@link Mutable#newInferenceAction(ModelProcessingPhase)}.
     *
     * @param stmt Context of added statement.
     */
    public void onPreLinkageDeclared(final @NonNull Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * Invoked when statement is closed during {@link ModelProcessingPhase#SOURCE_LINKAGE} phase, only substatements
     * from this and previous phase are available.
     *
     * <p>Implementation may use method to perform actions on this event or register modification action using
     * {@link Mutable#newInferenceAction(ModelProcessingPhase)}.
     *
     * @param stmt Context of added statement.
     * @throws SourceException when an inconsistency is detected.
     */
    public void onLinkageDeclared(final @NonNull Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * Invoked when statement is closed during {@link ModelProcessingPhase#STATEMENT_DEFINITION} phase,
     * only substatements from this phase are available.
     *
     * <p>Implementation may use method to perform actions on this event or register modification action using
     * {@link Mutable#newInferenceAction(ModelProcessingPhase)}.
     *
     * @param stmt Context of added statement. Argument and statement parent is accessible.
     * @throws SourceException when an inconsistency is detected.
     */
    public void onStatementDefinitionDeclared(final @NonNull Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * Invoked when statement is closed during {@link ModelProcessingPhase#FULL_DECLARATION} phase,
     * only substatements from this phase are available.
     *
     * <p>Implementation may use method to perform actions on this event or register modification action using
     * {@link Mutable#newInferenceAction(ModelProcessingPhase)}.
     *
     * @param stmt Context of added statement. Argument and statement parent is accessible.
     * @throws SourceException when an inconsistency is detected.
     */
    public void onFullDefinitionDeclared(final @NonNull Mutable<A, D, E> stmt) {
        final SubstatementValidator validator = substatementValidator();
        if (validator != null) {
            validator.validate(stmt);
        }
    }

    /**
     * Returns corresponding substatement validator of a statement support.
     *
     * @return substatement validator or null, if substatement validator is not defined
     */
    protected abstract @Nullable SubstatementValidator substatementValidator();

    /**
     * Returns true if this support has argument specific supports.
     *
     * @return true if this support has argument specific supports.
     */
    public boolean hasArgumentSpecificSupports() {
        // Most of statement supports don't have any argument specific supports, so return 'false'.
        return false;
    }

    /**
     * If this support has argument specific supports, the method returns support specific for given argument
     * (e.g. type statement support need to be specialized based on its argument), otherwise returns null.
     *
     * @param argument argument of statement
     * @return statement support specific for supplied argument or null
     */
    public @Nullable StatementSupport<?, ?, ?> getSupportSpecificForArgument(final String argument) {
        // Most of statement supports don't have any argument specific supports, so return null.
        return null;
    }

    /**
     * Given a raw string representation of an argument, try to use a shared representation. Default implementation
     * does nothing.
     *
     * @param rawArgument Argument string
     * @return A potentially-shard instance
     */
    public String internArgument(final String rawArgument) {
        return rawArgument;
    }

    /**
     * Returns true if this statement support and all its substatements ignore if-feature statements (e.g. yang-data
     * extension defined in <a href="https://www.rfc-editor.org/rfc/rfc8040#section-8">RFC 8040</a>). Default
     * implementation returns false.
     *
     * @return true if this statement support ignores if-feature statements, otherwise false.
     */
    @Beta
    public boolean isIgnoringIfFeatures() {
        return false;
    }

    /**
     * Returns true if this statement support and all its substatements ignore config statements (e.g. yang-data
     * extension defined in <a href="https://www.rfc-editor.org/rfc/rfc8040#section-8">RFC 8040</a>). Default
     * implementation returns false.
     *
     * @return true if this statement support ignores config statements,
     *         otherwise false.
     */
    @Beta
    public boolean isIgnoringConfig() {
        return false;
    }

    public final @NonNull QName statementName() {
        return publicDefinition.statementName();
    }

    public final @Nullable QName argumentName() {
        return publicDefinition.findArgumentDefinition().map(ArgumentDefinition::argumentName).orElse(null);
    }

    public final @NonNull Optional<ArgumentDefinition> getArgumentDefinition() {
        return publicDefinition.findArgumentDefinition();
    }

    /**
     * Statement context copy policy, indicating how should reactor handle statement copy operations. Every statement
     * copied by the reactor is subject to this policy.
     */
    public enum CopyPolicy {
        /**
         * Reuse the source statement context in the new place, as it cannot be affected by any further operations. This
         * implies that the semantics of the effective statement are not affected by any of its substatements. Each
         * of the substatements is free to make its own policy.
         *
         * <p>This policy is typically used by static constant statements such as {@code description} or {@code length},
         * where the baseline RFC7950 does not allow any impact. A {@code description} could hold an extension statement
         * in which case this interaction would come into play. Normal YANG will see empty substatements, so the reactor
         * will be free to complete reuse the context.
         *
         * <p>In case any substatement is of stronger policy, it is up to the reactor to handle correct handling of
         * resulting subobjects.
         */
        // TODO: does this mean source must have transitioned to ModelProcessingPhase.EFFECTIVE_MODEL?
        CONTEXT_INDEPENDENT,
        /**
         * Reuse the source statement context in the new place completely. This policy is more stringent than
         * {@link #CONTEXT_INDEPENDENT} in that the statement is dependent on circumstances of its original definition
         * and any copy operation must replicate it exactly as is. This implies ignoring the usual policy of its
         * substatements. A typical example of such a statement is {@code type}.
         */
        EXACT_REPLICA,
        /**
         * Create a copy sharing declared instance, but otherwise having a separate disconnected lifecycle.
         */
        // TODO: will the copy transition to ModelProcessingPhase.FULL_DECLARATION or which phase?
        DECLARED_COPY,
        /**
         * Reject any attempt to copy this statement. This is useful for statements that are defined as top-level
         * constructs, such as {@code contact}, {@code deviation} and similar.
         */
        REJECT,
        /**
         * Ignore this statement's existence for the purposes of the new place -- it is not impacted. This guidance
         * is left here for completeness, as it can have justifiable uses (but I can't think of any). Any substatements
         * need to be ignored, too.
         */
        IGNORE;
    }
}
