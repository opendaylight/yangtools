/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.EFFECTIVE_MODEL;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * Builder for effective model inference action. Model inference action is core principle of transforming
 * declared model into effective model.
 *
 * <p>Since YANG allows forward references, some inference actions need to be taken at a later point, where reference is
 * actually resolved. Referenced objects are not retrieved directly but are represented as {@link Prerequisite}
 * (prerequisite) for inference action to be taken.
 *
 * <p>Some existing YANG statements are more complex and also object, for which effective model may be inferred is also
 * represented as a {@link Prerequisite} which, when reference is available, will contain target context, which may be
 * used for inference action.
 *
 * <h2>Implementing inference action</h2>
 * Effective inference action could always be splitted into two separate tasks:
 * <ol>
 * <li>Declaration of inference action and its prerequisites</li>
 * <li>Execution of inference action</li>
 * </ol>
 *
 * <p>In order to declare inference action following steps needs to be taken:
 * <ol>
 * <li>Use {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)} to obtain
 * {@link ModelActionBuilder}.
 * <li>Use builder to specify concrete prerequisites of inference action
 * (other statements, values from identifier namespaces)
 * <li>Use builder to specify concrete set of nodes (or forward references to nodes)
 * which will inference action mutate.
 * <li>Use {@link #apply(InferenceAction)} with {@link InferenceAction} implementation
 * to register inference action.
 * </ol>
 *
 * <p>An action will be executed when:
 * <ul>
 * <li> {@link InferenceAction#apply(InferenceContext)} - all prerequisites (and declared forward references) are met,
 * action could dereference them and start applying changes.
 * </li>
 * <li>{@link InferenceAction#prerequisiteFailed(Collection)} - semantic parser finished all other satisfied
 * inference actions and some of declared prerequisites was still not met.
 * </li>
 * </ul>
 *
 * <p>TODO: Insert real word example
 *
 * <h2>Design notes</h2>
 * {@link java.util.concurrent.Future} seems as viable and more standard alternative to {@link Prerequisite}, but
 * Futures also carries promise that resolution of it is carried in other thread, which will actually put additional
 * constraints on semantic parser.
 *
 * <p>Also listening on multiple futures is costly, so we opted out of future and designed API, which later may
 * introduce futures.
 */
public interface ModelActionBuilder {
    interface InferenceContext {

    }

    @FunctionalInterface
    interface Prerequisite<T> {
        /**
         * Returns associated prerequisite once it is resolved.
         *
         * @param ctx Inference context in which the prerequisite was satisfied
         * @return associated prerequisite once it is resolved.
         */
        @NonNull T resolve(InferenceContext ctx);
    }

    /**
     * User-defined inference action.
     */
    interface InferenceAction {

        /**
         * Invoked once all prerequisites were met and forward references were resolved and inference action should be
         * applied. Implementors may perform necessary changes to mutable objects which were declared.
         *
         * @throws InferenceException If inference action can not be processed. Note that this exception be used for
         *         user to debug YANG sources, so should provide helpful context to fix issue in sources.
         */
        void apply(InferenceContext ctx);

        /**
         * Invoked once one of prerequisites was not met, even after all other satisfiable inference actions were
         * processed.
         *
         * <p>Implementors MUST throw {@link InferenceException} if semantic processing of model should be stopped
         * and failed.
         *
         * <p>List of failed prerequisites should be used to select right message / error type to debug problem in YANG
         * sources.
         *
         * @param failed collection of prerequisites which were not met
         * @throws InferenceException If inference action can not be processed. Note that this exception be used
         *                            by user to debug YANG sources, hence it should provide helpful context to fix
         *                            the issue in sources.
         */
        void prerequisiteFailed(Collection<? extends Prerequisite<?>> failed);

        /**
         * Invoked once the prerequisite is deemed unavailable due to conformance reasons. This typically happens when
         * a feature-dependent prerequisite does not have the appropriate feature activated.
         *
         * <p>The default implementation invokes {@link #prerequisiteFailed(Collection)}, implementations should
         * override this method if they wish, for example, to ignore the missing prerequisite.
         *
         * @param unavail Unavailable prerequisite
         */
        @Beta
        default void prerequisiteUnavailable(final Prerequisite<?> unavail) {
            prerequisiteFailed(ImmutableList.of(unavail));
        }
    }

    /**
     * Action requires that the specified context transition to complete {@link ModelProcessingPhase#FULL_DECLARATION}
     * phase and produce a declared statement.
     *
     * @param context Statement context which needs to complete the transition.
     * @return A {@link Prerequisite} returning the declared statement of the requested context.
     */
    <D extends DeclaredStatement<?>> @NonNull Prerequisite<D> requiresDeclared(StmtContext<?, ? extends D, ?> context);

    /**
     * Create a requirement on specified statement to be declared.
     *
     * @deprecated Undocumented method. Use at your own risk.
     */
    @Deprecated
    <K, D extends DeclaredStatement<?>> @NonNull Prerequisite<D> requiresDeclared(StmtContext<?, ?, ?> context,
        ParserNamespace<K, StmtContext<?, ? extends D, ?>> namespace, K key);

    /**
     * Action requires that the specified context completes specified phase before {@link #apply(InferenceAction)}
     * may be invoked.
     *
     * @param context Statement context which needs to complete the transition.
     * @param phase ModelProcessingPhase which must have completed
     * @return A {@link Prerequisite} returning the requested context.
     */
    <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> @NonNull Prerequisite<StmtContext<A, D, E>>
        requiresCtx(StmtContext<A, D, E> context, ModelProcessingPhase phase);

    <K, C extends StmtContext<?, ?, ?>> @NonNull Prerequisite<C> requiresCtx(StmtContext<?, ?, ?> context,
        @NonNull ParserNamespace<K, C> namespace, K key, ModelProcessingPhase phase);

    <K, C extends StmtContext<?, ?, ?>> @NonNull Prerequisite<C> requiresCtx(StmtContext<?, ?, ?> context,
        @NonNull ParserNamespace<K, C> namespace, NamespaceKeyCriterion<K> criterion, ModelProcessingPhase phase);

    <K, C extends StmtContext<?, ?, ?>> @NonNull Prerequisite<C> requiresEffectiveCtxPath(StmtContext<?, ?, ?> context,
        ParserNamespace<K, C> namespace, Iterable<K> keys);

    /**
     * Action mutates the effective model of specified statement. This is a shorthand for
     * {@code mutatesCtx(context, EFFECTIVE_MODEL}.
     *
     * @param context Target statement context
     * @return A {@link Prerequisite} returning the requested context.
     */
    default <T extends Mutable<?, ?, ?>> @NonNull Prerequisite<T> mutatesEffectiveCtx(final T context) {
        return mutatesCtx(context, EFFECTIVE_MODEL);
    }

    <K, E extends EffectiveStatement<?, ?>> @NonNull Prerequisite<Mutable<?, ?, E>> mutatesEffectiveCtx(
        StmtContext<?, ?, ?> context, ParserNamespace<K, ? extends StmtContext<?, ?, ?>> namespace, K key);

    <K, E extends EffectiveStatement<?, ?>> @NonNull Prerequisite<Mutable<?, ?, E>> mutatesEffectiveCtxPath(
        StmtContext<?, ?, ?> context, ParserNamespace<K, ? extends StmtContext<?, ?, ?>> namespace, Iterable<K> keys);

    /**
     * Action mutates the specified statement in the specified phase. Target statement cannot complete specified
     * phase before this action is applier.
     *
     * @param context Target statement context
     * @return A {@link Prerequisite} returning the requested context.
     */
    <C extends Mutable<?, ?, ?>, T extends C> @NonNull Prerequisite<C> mutatesCtx(T context,
            ModelProcessingPhase phase);

    /**
     * Apply an {@link InferenceAction} when this action's prerequisites are resolved.
     *
     * @param action Inference action to apply
     * @throws InferenceException if the action fails
     * @throws NullPointerException if {@code action is null}
     * @throws IllegalStateException if this action has an inference action already associated.
     */
    void apply(InferenceAction action);

    /**
     * Create a requirement on specified statement context to be declared.
     *
     * @deprecated Undocumented method. Use at your own risk.
     */
    @Deprecated
    <K, C extends StmtContext<?, ?, ?>> @NonNull Prerequisite<C> requiresDeclaredCtx(StmtContext<?, ?, ?> context,
        ParserNamespace<K, C> namespace, K key);

    /**
     * Create a requirement on specified statement to become effective.
     *
     * @deprecated Undocumented method. Use at your own risk.
     */
    @Deprecated
    <E extends EffectiveStatement<?, ?>> @NonNull Prerequisite<E> requiresEffective(
            StmtContext<?, ?, ? extends E> stmt);

    /**
     * Create a requirement on specified statement to become effective.
     *
     * @deprecated Undocumented method. Use at your own risk.
     */
    @Deprecated
    <K, E extends EffectiveStatement<?, ?>> @NonNull Prerequisite<E> requiresEffective(StmtContext<?, ?, ?> context,
        ParserNamespace<K, StmtContext<?, ?, ? extends E>> namespace, K key);

    /**
     * Create a requirement on specified statement context to become effective.
     *
     * @deprecated Undocumented method. Use at your own risk.
     */
    @Deprecated
    <K, C extends StmtContext<?, ?, ?>> @NonNull Prerequisite<C> requiresEffectiveCtx(StmtContext<?, ?, ?> context,
        ParserNamespace<K, C> namespace, K key);

    /**
     * Mark the fact that this action is mutating a namespace.
     *
     * @deprecated Undocumented method. Use at your own risk.
     */
    @Deprecated
    @NonNull Prerequisite<Mutable<?, ?, ?>> mutatesNs(Mutable<?, ?, ?> ctx, ParserNamespace<?, ?> namespace);
}
