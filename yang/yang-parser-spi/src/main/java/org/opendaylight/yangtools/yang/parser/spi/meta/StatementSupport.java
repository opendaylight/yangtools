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
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Support for processing concrete YANG statement.
 *
 * <p>
 * This interface is intended to be implemented by developers, which want to introduce support of statement to parser.
 * Consider subclassing {@link AbstractStatementSupport} for easier implementation of this interface.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
public abstract class StatementSupport<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        implements StatementDefinition, StatementFactory<A, D, E> {

    private final @NonNull StatementDefinition def;
    private final @NonNull CopyPolicy copyPolicy;

    @Beta
    protected StatementSupport(final StatementDefinition publicDefinition, final CopyPolicy copyPolicy) {
        checkArgument(publicDefinition != this);
        this.def = requireNonNull(publicDefinition);
        this.copyPolicy = requireNonNull(copyPolicy);
    }

    /**
     * Returns public statement definition, which will be present in built statements.
     *
     * <p>
     * Public statement definition may be used to provide different implementation of statement definition,
     * which will not retain any build specific data or context.
     *
     * @return public statement definition, which will be present in built statements.
     */
    public final @NonNull StatementDefinition getPublicView() {
        return def;
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
     * <p>
     * In this scenario, it is the reactor's job to figure out what to do (like talking to substatements).
     *
     * @return This statement's copy policy
     */
    public final @NonNull CopyPolicy copyPolicy() {
        return copyPolicy;
    }

    /**
     * Parses textual representation of argument in object representation.
     *
     * @param ctx Context, which may be used to access source-specific namespaces required for parsing.
     * @param value String representation of value, as was present in text source.
     * @return Parsed value
     * @throws SourceException when an inconsistency is detected.
     */
    public abstract A parseArgumentValue(StmtContext<?, ?, ?> ctx, String value);

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
     * <p>
     * Implementation may use method to perform actions on this event or register modification action using
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
     * <p>
     * Implementation may use method to perform actions on this event or register modification action using
     * {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
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
     * <p>
     * Implementation may use method to perform actions on this event or register modification action using
     * {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     *
     * @param stmt Context of added statement. Argument and statement parent is accessible.
     * @throws SourceException when an inconsistency is detected.
     */
    public void onStatementDefinitionDeclared(final Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * Invoked when statement is closed during {@link ModelProcessingPhase#FULL_DECLARATION} phase,
     * only substatements from this phase are available.
     *
     * <p>
     * Implementation may use method to perform actions on this event or register modification action using
     * {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     *
     * @param stmt Context of added statement. Argument and statement parent is accessible.
     * @throws SourceException when an inconsistency is detected.
     */
    public void onFullDefinitionDeclared(final StmtContext.Mutable<A, D, E> stmt) {
        final SubstatementValidator validator = getSubstatementValidator();
        if (validator != null) {
            validator.validate(stmt);
        }
    }

    /**
     * Returns corresponding substatement validator of a statement support.
     *
     * @return substatement validator or null, if substatement validator is not defined
     */
    // FIXME: rename to 'substatementValidator' and perhaps let it be passed in?
    protected abstract @Nullable SubstatementValidator getSubstatementValidator();

    /**
     * Returns true if this support has argument specific supports.
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
     * Determine reactor copy behaviour of a statement instance. Statement support classes are required to determine
     * their operations with regard to their statements being replicated into different contexts, so that
     * {@link Mutable} instances are not created when it is evident they are superfluous.
     *
     * <p>
     * The copy operation has three intrinsic parts:
     * <ul>
     *   <li>target {@code parent}, i.e. new parent statement for the copy. This determines things like default value
     *       of the {@code config} statement and similar</li>
     *   <li>copy operation type</li>
     *   <li>{@code target module}, which defines the default namespace for the statement copy. This might not be always
     *       present, in which case the namespace is retained from the source. As an example, {@code uses} changes
     *       the default namespace to parent's namespace, whereas {@code augment} does not.</li>
     * </ul>
     *
     * <p>
     * Implementations should return the context to use -- returning {@code stmt} if there is no change or a copy of it.
     *
     * @param stmt Context of statement to be copied statement
     * @param parent Parent statement context
     * @param copyType Type of copy being performed
     * @param targetModule Target module, if present
     * @return StmtContext holding the effective state
     */
    public final @NonNull StmtContext<?, ?, ?> effectiveCopyOf(final StmtContext<?, ?, ?> stmt,
            final Mutable<?, ?, ?> parent, final CopyType copyType, final @Nullable QNameModule targetModule) {
        switch (copyPolicy) {
            case CONTEXT_INDEPENDENT:
                return stmt;
            case DECLARED_COPY:
                // FIXME: YANGTOOLS-1195: this is too harsh, we need to make a callout to subclass methods so they
                //                        actually examine the differences.
                return parent.childCopyOf(stmt, copyType, targetModule);
            default:
                throw new VerifyException("Attempted to apply " + copyPolicy);
        }
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
     * Returns unknown statement form of a regular YANG statement supplied as a parameter to the method. Default
     * implementation does nothing.
     *
     * @param yangStmtDef statement definition of a regular YANG statement
     * @return Optional of unknown statement form of a regular YANG statement or empty() if it is not supported by this
     *         statement support
     */
    public Optional<StatementSupport<?, ?, ?>> getUnknownStatementDefinitionOf(final StatementDefinition yangStmtDef) {
        return Optional.empty();
    }

    /**
     * Returns true if this statement support and all its substatements ignore if-feature statements (e.g. yang-data
     * extension defined in <a href="https://tools.ietf.org/html/rfc8040#section-8">RFC 8040</a>). Default
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
     * extension defined in <a href="https://tools.ietf.org/html/rfc8040#section-8">RFC 8040</a>). Default
     * implementation returns false.
     *
     * @return true if this statement support ignores config statements,
     *         otherwise false.
     */
    @Beta
    public boolean isIgnoringConfig() {
        return false;
    }

    @Override
    public final QName getStatementName() {
        return def.getStatementName();
    }

    @Override
    public final Optional<ArgumentDefinition> getArgumentDefinition() {
        return def.getArgumentDefinition();
    }

    @Override
    // Non-final for compatible extensions
    public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return def.getDeclaredRepresentationClass();
    }

    @Override
    // Non-final for compatible extensions
    public Class<? extends EffectiveStatement<?,?>> getEffectiveRepresentationClass() {
        return def.getEffectiveRepresentationClass();
    }

    /**
     * Statement context copy policy, indicating how should reactor handle statement copy operations. Every statement
     * copied by the reactor is subject to policy check done by
     * {@link StatementSupport#effectiveCopyOf(StmtContext, Mutable, CopyType, QNameModule)}.
     */
    public enum CopyPolicy {
        /**
         * Reuse the source statement context in the new place, as it cannot be affected by any further operations. This
         * implies that the semantics of the effective statement are not affected by any of its substatements. Each
         * of the substatements is free to make its own policy.
         *
         * <p>
         * This policy is typically used by static constant statements such as {@code description} or {@code length},
         * where the baseline RFC7950 does not allow any impact. A {@code description} could hold an extension statement
         * in which case this interaction would come into play. Normal YANG will see empty substatements, so the reactor
         * will be free to complete reuse the context.
         *
         * <p>
         * In case any substatement is of stronger policy, it is up to the reactor to handle correct handling of
         * resulting subobjects.
         */
        // TODO: does this mean source must have transitioned to ModelProcessingPhase.EFFECTIVE_MODEL?
        CONTEXT_INDEPENDENT,
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
