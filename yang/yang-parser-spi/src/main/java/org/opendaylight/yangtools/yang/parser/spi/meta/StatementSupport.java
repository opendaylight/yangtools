/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
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
public interface StatementSupport<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends StatementDefinition, StatementFactory<A, D, E> {
    /**
     * Returns public statement definition, which will be present in built statements.
     *
     * <p>
     * Public statement definition may be used to provide different implementation of statement definition,
     * which will not retain any build specific data or context.
     *
     * @return public statement definition, which will be present in built statements.
     */
    @NonNull StatementDefinition getPublicView();

    /**
     * Parses textual representation of argument in object representation.
     *
     * @param ctx Context, which may be used to access source-specific namespaces required for parsing.
     * @param value String representation of value, as was present in text source.
     * @return Parsed value
     * @throws SourceException when an inconsistency is detected.
     */
    A parseArgumentValue(StmtContext<?, ?, ?> ctx, String value);

    /**
     * Adapts the argument value to match a new module.
     *
     * @param ctx Context, which may be used to access source-specific namespaces required for parsing.
     * @param targetModule Target module, may not be null.
     * @return Adapted argument value. The default implementation returns original value stored in context.
     */
    default A adaptArgumentValue(final StmtContext<A, D, E> ctx, final QNameModule targetModule) {
        return ctx.getStatementArgument();
    }

    /**
     * Invoked when a statement supported by this instance is added to build context. This allows implementations
     * of this interface to start tracking the statement and perform any modifications to the build context hierarchy,
     * accessible via {@link StmtContext#getParentContext()}. One such use is populating the parent's namespaces to
     * allow it to locate this child statement.
     *
     * @param stmt Context of added statement. No substatements are available.
     */
    void onStatementAdded(StmtContext.Mutable<A, D, E> stmt);

    /**
     * Invoked when statement is closed during {@link ModelProcessingPhase#SOURCE_PRE_LINKAGE} phase, only substatements
     * from this and previous phase are available.
     *
     * <p>
     * Implementation may use method to perform actions on this event or register modification action using
     * {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     *
     * @param stmt Context of added statement.
     */
    void onPreLinkageDeclared(StmtContext.Mutable<A, D, E> stmt);

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
    void onLinkageDeclared(StmtContext.Mutable<A, D, E> stmt);

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
    void onStatementDefinitionDeclared(StmtContext.Mutable<A, D, E> stmt);

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
    void onFullDefinitionDeclared(StmtContext.Mutable<A, D, E> stmt);

    /**
     * Returns true if this support has argument specific supports.
     */
    boolean hasArgumentSpecificSupports();

    /**
     * If this support has argument specific supports, the method returns support specific for given argument
     * (e.g. type statement support need to be specialized based on its argument), otherwise returns null.
     *
     * @param argument argument of statement
     * @return statement support specific for supplied argument or null
     */
    @Nullable StatementSupport<?, ?, ?> getSupportSpecificForArgument(String argument);

    /**
     * Determine reactor copy behavior of a statement instance. Statement support classes are required to determine
     * their operations with regard to their statements being replicated into different contexts, so that
     * {@link Mutable} instances are not created when it is evident they are superfluous.
     *
     * @param stmt Context of statement to be copied statement.
     * @param parent Parent statement context
     * @param type Type of copy being performed
     * @param targetModule Target module, if present
     * @return Policy that needs to be applied to the copy operation of this statement.
     */
    // FIXME: YANGTOOLS-694: clarify targetModule semantics (does null mean 'same as declared'?)
    default @NonNull CopyPolicy applyCopyPolicy(final Mutable<?, ?, ?> stmt, final Mutable<?, ?, ?> parent,
            final CopyType type, @Nullable final QNameModule targetModule) {
        // Most of statement supports will just want to copy the statement
        // FIXME: YANGTOOLS-694: that is not strictly true. Subclasses of this should indicate if they are themselves
        //                       copy-sensitive:
        //                       1) if they are not and cannot be targeted by inference, and all their current
        //                          substatements are also non-sensitive, we want to return the same context.
        //                       2) if they are not and their current substatements are sensitive, we want to copy
        //                          as a lazily-instantiated interceptor to let it deal with substatements when needed
        //                          (YANGTOOLS-1067 prerequisite)
        //                       3) otherwise perform this eager copy
        //      return Optional.of(parent.childCopyOf(stmt, copyType, targetModule));
        return CopyPolicy.DECLARED_COPY;
    }

    /**
     * Given a raw string representation of an argument, try to use a shared representation.
     *
     * @param rawArgument Argument string
     * @return A potentially-shard instance
     */
    default String internArgument(final String rawArgument) {
        return rawArgument;
    }

    /**
     * Returns unknown statement form of a regular YANG statement supplied as a parameter to the method.
     *
     * @param yangStmtDef statement definition of a regular YANG statement
     * @return Optional of unknown statement form of a regular YANG statement or empty() if it is not supported by this
     *         statement support
     */
    default Optional<StatementSupport<?, ?, ?>> getUnknownStatementDefinitionOf(final StatementDefinition yangStmtDef) {
        return Optional.empty();
    }

    /**
     * Returns true if this statement support and all its substatements ignore if-feature statements (e.g. yang-data
     * extension defined in <a href="https://tools.ietf.org/html/rfc8040#section-8">RFC 8040</a>). Default
     * implementation returns false.
     *
     * @return true if this statement support ignores if-feature statements,
     *         otherwise false.
     */
    @Beta
    default boolean isIgnoringIfFeatures() {
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
    default boolean isIgnoringConfig() {
        return false;
    }

    @Override
    default QName getStatementName() {
        return getPublicView().getStatementName();
    }

    @Override
    default @NonNull Optional<ArgumentDefinition> getArgumentDefinition() {
        return getPublicView().getArgumentDefinition();
    }

    @Override
    default Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return getPublicView().getDeclaredRepresentationClass();
    }

    @Override
    default Class<? extends EffectiveStatement<?,?>> getEffectiveRepresentationClass() {
        return getPublicView().getEffectiveRepresentationClass();
    }

    /**
     * Statement context copy policy, indicating how should reactor handle statement copy operations. Every statement
     * copied by the reactor is subject to policy check done by
     * {@link StatementSupport#applyCopyPolicy(Mutable, Mutable, CopyType, QNameModule)}.
     *
     */
    enum CopyPolicy {
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
         * Ignore this statement's existence for the purposes of the new place -- it is not impacted. This guidance
         * is left here for completeness, as it can have justifiable uses (but I can't think of any). Any substatements
         * need to be ignored, too.
         */
        IGNORE;
    }
}
