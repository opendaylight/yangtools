package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 *
 * Support for processing concrete YANG statement.
 *
 * This interface is intended to be implemented by developers, which want to
 * introduce support of statement to parser. Consider subclassing
 * {@link AbstractStatementSupport} for easier implementation of this interface.
 *
 * @param <A>
 *            Argument type
 * @param <D>
 *            Declared Statement representation
 * @param <E>
 *            Effective Statement representation
 */
public interface StatementSupport<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> extends StatementDefinition, StatementFactory<A, D, E> {

    /**
     * Returns public statement definition, which will be present in builded statements.
     *
     * Public statement definition may be used to provide different implementation
     * of statement definition, which will not retain any build specific data
     * or context.
     *
     * @return public statement definition, which will be present in builded statements.
     */
     StatementDefinition getPublicView();

    /**
     *
     * Parses textual representation of argument in object representation.
     *
     * @param ctx Context, which may be used to access source-specific namespaces
     * required for parsing.
     * @param value String representation of value, as was present in text source.
     * @return Parsed value
     */
     A parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) throws SourceException;

    /**
     *
     * Invoked when statement is added to build context.
     *
     * @param stmt
     *            Context of added statement. No substatement are available.
     */
     void onStatementAdded(StmtContext.Mutable<A, D, E> stmt);

    /**
     *
     * Invoked when statement is closed during
     * {@link ModelProcessingPhase#STATEMENT_DEFINITION} phase.
     *
     * Invoked when statement is closed during
     * {@link ModelProcessingPhase#STATEMENT_DEFINITION} phase, only substatements from
     * this and previous phase are available.
     *
     * Implementation may use method to perform actions on this event or
     * register modification action using
     * {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     *
     * @param stmt
     *            Context of added statement.
     */
     void onLinkageDeclared(StmtContext.Mutable<A, D, E> stmt) throws InferenceException,
            SourceException;

    /**
     *
     * Invoked when statement is closed during
     * {@link ModelProcessingPhase#STATEMENT_DEFINITION} phase.
     *
     * Invoked when statement is closed during
     * {@link ModelProcessingPhase#STATEMENT_DEFINITION} phase, only substatements from
     * this phase are available.
     *
     * Implementation may use method to perform actions on this event or
     * register modification action using
     * {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     *
     * @param stmt
     *            Context of added statement. Argument and statement parent is
     *            accessible.
     */
     void onStatementDefinitionDeclared(StmtContext.Mutable<A, D, E> stmt) throws InferenceException,
            SourceException;

    /**
     *
     * Invoked when statement is closed during
     * {@link ModelProcessingPhase#FULL_DECLARATION} phase.
     *
     * Invoked when statement is closed during
     * {@link ModelProcessingPhase#FULL_DECLARATION} phase, only substatements
     * from this phase are available.
     *
     * Implementation may use method to perform actions on this event or
     * register modification action using
     * {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     *
     *
     * @param stmt
     *            Context of added statement. Argument and statement parent is
     *            accessible.
     */
     void onFullDefinitionDeclared(StmtContext.Mutable<A, D, E> stmt) throws InferenceException,
            SourceException;

}