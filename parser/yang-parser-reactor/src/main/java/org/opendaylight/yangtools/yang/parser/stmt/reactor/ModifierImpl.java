/*
 * Copyright (c) 2015, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.EFFECTIVE_MODEL;
import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.FULL_DECLARATION;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.ContextMutation;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.OnNamespaceItemAdded;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.OnPhaseFinished;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ModifierImpl implements ModelActionBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ModifierImpl.class);

    private final Set<AbstractPrerequisite<?>> unsatisfied = new HashSet<>(1);
    private final Set<AbstractPrerequisite<?>> mutations = new HashSet<>(1);
    private final InferenceContext ctx = new InferenceContext() { };

    private List<Runnable> bootstraps;
    private InferenceAction action;
    private boolean actionApplied;

    private <D> AbstractPrerequisite<D> addReq(final AbstractPrerequisite<D> prereq) {
        LOG.trace("Modifier {} adding prerequisite {}", this, prereq);
        unsatisfied.add(prereq);
        return prereq;
    }

    private <T> @NonNull AbstractPrerequisite<T> addMutation(final @NonNull AbstractPrerequisite<T> mutation) {
        LOG.trace("Modifier {} adding mutation {}", this, mutation);
        mutations.add(mutation);
        return mutation;
    }

    private void checkNotRegistered() {
        checkState(action == null, "Action was already registered.");
    }

    private boolean removeSatisfied() {
        final var it = unsatisfied.iterator();
        while (it.hasNext()) {
            final var prereq = it.next();
            if (prereq.isDone()) {
                // We are removing current prerequisite from list.
                LOG.trace("Modifier {} prerequisite {} satisfied", this, prereq);
                it.remove();
            }
        }
        return unsatisfied.isEmpty();
    }

    boolean isApplied() {
        return actionApplied;
    }

    void failModifier() {
        removeSatisfied();
        checkState(action != null);
        action.prerequisiteFailed(unsatisfied);
        action = null;
    }

    private <K, C extends StmtContext<?, ?, ?>> @NonNull AbstractPrerequisite<C> requiresCtxImpl(
            final StmtContext<?, ?, ?> context, final ParserNamespace<K, C> namespace, final K key,
            final ModelProcessingPhase phase)  {
        checkNotRegistered();

        final var addedToNs = new AddedToNamespace<C>(this, phase);
        addReq(addedToNs);
        contextImpl(context).onNamespaceItemAddedAction(namespace, key, addedToNs);
        return addedToNs;
    }

    private <K, C extends StmtContext<?, ?, ?>> @NonNull AbstractPrerequisite<C> requiresCtxImpl(
            final StmtContext<?, ?, ?> context, final ParserNamespace<K, C> namespace,
            final NamespaceKeyCriterion<K> criterion, final ModelProcessingPhase phase)  {
        checkNotRegistered();

        final var addedToNs = new AddedToNamespace<C>(this, phase);
        addReq(addedToNs);
        contextImpl(context).onNamespaceItemAddedAction(namespace, phase, criterion, addedToNs);
        return addedToNs;
    }

    private <C extends StmtContext<?, ?, ?>> @NonNull AbstractPrerequisite<C> requiresCtxImpl(final C context,
            final ModelProcessingPhase phase) {
        checkNotRegistered();

        final var phaseFin = new PhaseFinished<C>(this);
        addReq(phaseFin);
        addBootstrap(() -> contextImpl(context).addPhaseCompletedListener(phase, phaseFin));
        return phaseFin;
    }

    private <K, C extends Mutable<?, ?, ?>> AbstractPrerequisite<C> mutatesCtxImpl(final StmtContext<?, ?, ?> context,
            final ParserNamespace<K, ? extends StmtContext<?, ?, ?>> namespace, final K key,
            final ModelProcessingPhase phase) {
        checkNotRegistered();

        final var mod = new PhaseModificationInNamespace<C>(this, EFFECTIVE_MODEL);
        addReq(mod);
        addMutation(mod);
        contextImpl(context).onNamespaceItemAddedAction(namespace, key, mod);
        return mod;
    }

    private static StatementContextBase<?, ?, ?> contextImpl(final Object value) {
        if (value instanceof StatementContextBase<?, ?, ?> impl) {
            return impl;
        }
        throw new IllegalArgumentException("Supplied context " + value + " is not provided by this reactor.");
    }

    boolean tryApply() {
        checkState(action != null, "Action was not defined yet.");

        if (removeSatisfied()) {
            if (!actionApplied) {
                action.apply(ctx);
                actionApplied = true;
            }
            return true;
        }
        return false;
    }

    @Override
    public <C extends Mutable<?, ?, ?>, T extends C> Prerequisite<C> mutatesCtx(final T context,
            final ModelProcessingPhase phase) {
        return addMutation(new PhaseMutation<>(this, contextImpl(context), phase));
    }

    @Override
    public <A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
            AbstractPrerequisite<StmtContext<A, D, E>> requiresCtx(final StmtContext<A, D, E> context,
                final ModelProcessingPhase phase) {
        return requiresCtxImpl(context, phase);
    }

    @Override
    public <K, C extends StmtContext<?, ?, ?>> Prerequisite<C> requiresCtx(final StmtContext<?, ?, ?> context,
            final ParserNamespace<K, C> namespace, final K key, final ModelProcessingPhase phase) {
        return requiresCtxImpl(context, namespace, key, phase);
    }

    @Override
    public <K, C extends StmtContext<?, ?, ?>> Prerequisite<C> requiresCtx(final StmtContext<?, ?, ?> context,
            final ParserNamespace<K, C> namespace, final NamespaceKeyCriterion<K> criterion,
            final ModelProcessingPhase phase) {
        return requiresCtxImpl(context, namespace, criterion, phase);
    }

    @Override
    public <K, C extends StmtContext<?, ?, ?>> Prerequisite<C> requiresEffectiveCtxPath(
            final StmtContext<?, ?, ?> context, final ParserNamespace<K, C> namespace, final Iterable<K> keys) {
        checkNotRegistered();

        final var ret = new PhaseRequirementInNamespacePath<C, K>(this, keys);
        addReq(ret);
        addBootstrap(() -> ret.hookOnto(context, namespace));
        return ret;
    }

    @Override
    public <D extends DeclaredStatement<?>> Prerequisite<D> requiresDeclared(
            final StmtContext<?, ? extends D, ?> context) {
        return requiresCtxImpl(context, FULL_DECLARATION).transform(StmtContext::declared);
    }

    @Override
    @Deprecated
    public <K, D extends DeclaredStatement<?>> Prerequisite<D> requiresDeclared(final StmtContext<?, ?, ?> context,
            final ParserNamespace<K, StmtContext<?, ? extends D, ?>> namespace, final K key) {
        return requiresCtxImpl(context, namespace, key, FULL_DECLARATION).transform(StmtContext::declared);
    }

    @Override
    @Deprecated
    public <K, C extends StmtContext<?, ?, ?>> AbstractPrerequisite<C> requiresDeclaredCtx(
            final StmtContext<?, ?, ?> context, final ParserNamespace<K, C> namespace, final K key) {
        return requiresCtxImpl(context, namespace, key, FULL_DECLARATION);
    }

    @Override
    @Deprecated
    public <E extends EffectiveStatement<?, ?>> Prerequisite<E> requiresEffective(
            final StmtContext<?, ?, ? extends E> stmt) {
        return requiresCtxImpl(stmt, EFFECTIVE_MODEL).transform(StmtContext::buildEffective);
    }

    @Override
    @Deprecated
    public <K, E extends EffectiveStatement<?, ?>> Prerequisite<E> requiresEffective(final StmtContext<?, ?, ?> context,
            final ParserNamespace<K, StmtContext<?, ?, ? extends E>> namespace, final K key) {
        return requiresCtxImpl(context, namespace, key, EFFECTIVE_MODEL).transform(StmtContext::buildEffective);
    }

    @Override
    @Deprecated
    public <K, C extends StmtContext<?, ?, ?>> AbstractPrerequisite<C> requiresEffectiveCtx(
            final StmtContext<?, ?, ?> context, final ParserNamespace<K, C> namespace, final K key) {
        return requiresCtxImpl(contextImpl(context), namespace, key, EFFECTIVE_MODEL);
    }

    @Override
    @Deprecated
    public Prerequisite<Mutable<?, ?, ?>> mutatesNs(final Mutable<?, ?, ?> context,
            final ParserNamespace<?, ?> namespace) {
        return addMutation(new NamespaceMutation(this, contextImpl(context), namespace));
    }

    @Override
    public <K, E extends EffectiveStatement<?, ?>> AbstractPrerequisite<Mutable<?, ?, E>> mutatesEffectiveCtx(
            final StmtContext<?, ?, ?> context, final ParserNamespace<K, ? extends StmtContext<?, ?, ?>> namespace,
            final K key) {
        return mutatesCtxImpl(context, namespace, key, EFFECTIVE_MODEL);
    }

    @Override
    public <K, E extends EffectiveStatement<?, ?>> AbstractPrerequisite<Mutable<?, ?, E>> mutatesEffectiveCtxPath(
            final StmtContext<?, ?, ?> context, final ParserNamespace<K, ? extends StmtContext<?, ?, ?>> namespace,
            final Iterable<K> keys) {
        checkNotRegistered();

        final var ret = new PhaseModificationInNamespacePath<Mutable<?, ?, E>, K>(this, keys);
        addReq(ret);
        addMutation(ret);
        addBootstrap(() -> ret.hookOnto(context, namespace));
        return ret;
    }

    @Override
    @SuppressWarnings("checkstyle:hiddenField")
    public void apply(final InferenceAction action) {
        checkState(this.action == null, "Action already defined to %s", this.action);
        this.action = requireNonNull(action);
        if (bootstraps != null) {
            bootstraps.forEach(Runnable::run);
            bootstraps = null;
        }
    }

    private void addBootstrap(final Runnable bootstrap) {
        if (bootstraps == null) {
            bootstraps = new ArrayList<>(1);
        }
        bootstraps.add(bootstrap);
    }

    private abstract static class AbstractPrerequisite<T> implements Prerequisite<T> {
        final @NonNull ModifierImpl modifier;

        private boolean done = false;
        private T value;

        AbstractPrerequisite(final ModifierImpl modifier) {
            this.modifier = requireNonNull(modifier);
        }

        @Override
        @SuppressWarnings("checkstyle:hiddenField")
        public final T resolve(final InferenceContext ctx) {
            checkState(done);
            checkArgument(ctx == modifier.ctx);
            return verifyNotNull(value, "Attempted to access unavailable prerequisite %s", this);
        }

        final boolean isDone() {
            return done;
        }

        @SuppressWarnings("checkstyle:hiddenField")
        final boolean resolvePrereq(final T value) {
            this.value = value;
            done = true;
            return modifier.isApplied();
        }

        final <O> @NonNull Prerequisite<O> transform(final Function<? super T, O> transformation) {
            return context -> transformation.apply(resolve(context));
        }

        @Override
        public final String toString() {
            return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
        }

        ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
            return toStringHelper.add("value", value);
        }
    }

    private abstract static class AbstractPathPrerequisite<C extends StmtContext<?, ?, ?>, K>
            extends AbstractPrerequisite<C> implements OnNamespaceItemAdded {
        private final Iterable<K> keys;
        private final Iterator<K> it;

        AbstractPathPrerequisite(final ModifierImpl modifier, final Iterable<K> keys) {
            super(modifier);
            this.keys = requireNonNull(keys);
            it = keys.iterator();
        }

        @Override
        public final void namespaceItemAdded(final StatementContextBase<?, ?, ?> context,
                final ParserNamespace<?, ?> namespace, final Object key, final Object value) {
            LOG.debug("Action for {} got key {}", keys, key);

            contextImpl(value).addPhaseCompletedListener(FULL_DECLARATION, (target, ignored) -> {
                if (target.isSupportedByFeatures()) {
                    nextStep(context, target);

                    if (it.hasNext()) {
                        // Make sure target's storage notifies us when the next step becomes available.
                        hookOnto(target, namespace, it.next());
                    } else if (resolvePrereq((C) target)) {
                        modifier.tryApply();
                    }
                } else {
                    LOG.debug("Key {} in {} is not supported", key, keys);
                    resolvePrereq(null);
                    checkState(modifier.action != null);
                    modifier.action.prerequisiteUnavailable(this);
                }

                return true;
            });
        }

        abstract void nextStep(StatementContextBase<?, ?, ?> current, StatementContextBase<?, ?, ?> next);

        @Override
        final ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
            return super.addToStringAttributes(toStringHelper).add("keys", keys);
        }

        final void hookOnto(final StmtContext<?, ?, ?> context, final ParserNamespace<?, ?> namespace) {
            checkArgument(it.hasNext(), "Namespace %s keys may not be empty", namespace);
            hookOnto(contextImpl(context), namespace, it.next());
        }

        @SuppressWarnings("unchecked")
        private void hookOnto(final StatementContextBase<?, ?, ?> context, final ParserNamespace<?, ?> namespace,
                final K key) {
            context.onNamespaceItemAddedAction((ParserNamespace) namespace, requireNonNull(key), this);
        }
    }

    private static final class PhaseMutation<C> extends AbstractPrerequisite<C> implements ContextMutation {
        @SuppressWarnings("unchecked")
        PhaseMutation(final ModifierImpl modifier, final StatementContextBase<?, ?, ?> context,
                final ModelProcessingPhase phase) {
            super(modifier);
            context.addMutation(phase, this);
            resolvePrereq((C) context);
        }

        @Override
        public boolean isFinished() {
            return modifier.isApplied();
        }
    }

    private static final class PhaseFinished<C extends StmtContext<?, ?, ?>> extends AbstractPrerequisite<C>
            implements OnPhaseFinished {
        PhaseFinished(final ModifierImpl modifier) {
            super(modifier);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean phaseFinished(final StatementContextBase<?, ?, ?> context,
                final ModelProcessingPhase finishedPhase) {
            return resolvePrereq((C) context) || modifier.tryApply();
        }
    }

    private static final class NamespaceMutation extends AbstractPrerequisite<Mutable<?, ?, ?>> {
        NamespaceMutation(final ModifierImpl modifier, final StatementContextBase<?, ?, ?> ctx,
                final ParserNamespace<?, ?> namespace) {
            super(modifier);
            resolvePrereq(ctx);
        }
    }

    private static final class AddedToNamespace<C extends StmtContext<?, ?, ?>> extends AbstractPrerequisite<C>
            implements OnNamespaceItemAdded, OnPhaseFinished {
        private final ModelProcessingPhase phase;

        AddedToNamespace(final ModifierImpl modifier, final ModelProcessingPhase phase) {
            super(modifier);
            this.phase = requireNonNull(phase);
        }

        @Override
        public void namespaceItemAdded(final StatementContextBase<?, ?, ?> context,
                final ParserNamespace<?, ?> namespace, final Object key, final Object value) {
            contextImpl(value).addPhaseCompletedListener(phase, this);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean phaseFinished(final StatementContextBase<?, ?, ?> context,
                final ModelProcessingPhase finishedPhase) {
            return resolvePrereq((C) context) || modifier.tryApply();
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
            return super.addToStringAttributes(toStringHelper).add("phase", phase);
        }
    }

    private static final class PhaseRequirementInNamespacePath<C extends StmtContext<?, ?, ?>, K>
            extends AbstractPathPrerequisite<C, K> {
        PhaseRequirementInNamespacePath(final ModifierImpl modifier, final Iterable<K> keys) {
            super(modifier, keys);
        }

        @Override
        void nextStep(final StatementContextBase<?, ?, ?> current, final StatementContextBase<?, ?, ?> next) {
            // No-op
        }
    }

    private static final class PhaseModificationInNamespace<C extends Mutable<?, ?, ?>> extends AbstractPrerequisite<C>
            implements OnNamespaceItemAdded, ContextMutation {
        private final ModelProcessingPhase modPhase;

        PhaseModificationInNamespace(final ModifierImpl modifier, final ModelProcessingPhase phase) {
            super(modifier);
            checkArgument(phase != null, "Model processing phase must not be null");
            modPhase = phase;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void namespaceItemAdded(final StatementContextBase<?, ?, ?> context,
                final ParserNamespace<?, ?> namespace, final Object key, final Object value) {
            final var targetCtx = contextImpl(value);
            targetCtx.addMutation(modPhase, this);
            resolvePrereq((C) targetCtx);
        }

        @Override
        public boolean isFinished() {
            return modifier.isApplied();
        }
    }

    /**
     * This similar to {@link PhaseModificationInNamespace}, but allows recursive descent until it finds the real
     * target. The mechanics is driven as a sequence of prerequisites along a path: first we hook onto namespace to
     * give us the first step. When it does, we hook onto the first item to provide us the second step and so on.
     */
    private static final class PhaseModificationInNamespacePath<C extends Mutable<?, ?, ?>, K>
            extends AbstractPathPrerequisite<C, K> implements ContextMutation {
        PhaseModificationInNamespacePath(final ModifierImpl modifier, final Iterable<K> keys) {
            super(modifier, keys);
        }

        @Override
        public boolean isFinished() {
            return modifier.isApplied();
        }

        @Override
        void nextStep(final StatementContextBase<?, ?, ?> current, final StatementContextBase<?, ?, ?> next) {
            // Hook onto target: we either have a modification of the target itself or one of its children.
            next.addMutation(EFFECTIVE_MODEL, this);
            // We have completed the context -> target step, hence we are no longer directly blocking context from
            // making forward progress.
            current.removeMutation(EFFECTIVE_MODEL, this);
        }
    }
}
