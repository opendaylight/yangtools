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
     * Create an optional copy of specified statement as a substatement of parent.
     *
     * <p>
     * Note that while it may be tempting to return the same context, this is not safe in general case. It is only safe
     * if the entire subtree is unaffected by changes to parent/namespace/history. This includes the semantics of this
     * statement (it cannot be a target of any inference effects) as well as any substatements -- an extension statement
     * is allowed pretty much anywhere and if its semantics are context-dependent, a simple instance reuse will not
     * work.
     *
     * @param stmt Context of statement to be copied statement.
     * @param parent Parent statement context
     * @param type Type of copy being performed
     * @param targetModule Target module, if present
     * @return Empty if the statement should be ignored, or present with an instance that should be copied into parent.
     */
    @NonNull Optional<? extends Mutable<?, ?, ?>> copyAsChildOf(Mutable<?, ?, ?> stmt, Mutable<?, ?, ?> parent,
            CopyType type, @Nullable QNameModule targetModule);

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
}
