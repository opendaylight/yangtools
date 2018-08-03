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
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.EFFECTIVE_MODEL;
import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.FULL_DECLARATION;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceKeyCriterion;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.ContextMutation;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.OnNamespaceItemAdded;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.OnPhaseFinished;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ModifierImpl implements ModelActionBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ModifierImpl.class);

    private final InferenceContext ctx = new InferenceContext() { };

    private final Set<AbstractPrerequisite<?>> unsatisfied = new HashSet<>(1);
    private final Set<AbstractPrerequisite<?>> mutations = new HashSet<>(1);

    private InferenceAction action;
    private boolean actionApplied = false;

    private <D> AbstractPrerequisite<D> addReq(final AbstractPrerequisite<D> prereq) {
        LOG.trace("Modifier {} adding prerequisite {}", this, prereq);
        unsatisfied.add(prereq);
        return prereq;
    }

    private <T> AbstractPrerequisite<T> addMutation(final AbstractPrerequisite<T> mutation) {
        LOG.trace("Modifier {} adding mutation {}", this, mutation);
        mutations.add(mutation);
        return mutation;
    }

    private void checkNotRegistered() {
        checkState(action == null, "Action was already registered.");
    }

    private boolean removeSatisfied() {
        final Iterator<AbstractPrerequisite<?>> it = unsatisfied.iterator();
        while (it.hasNext()) {
            final AbstractPrerequisite<?> prereq = it.next();
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
        action.prerequisiteFailed(unsatisfied);
        action = null;
    }

    private void applyAction() {
        checkState(!actionApplied);
        action.apply(ctx);
        actionApplied = true;
    }

    private <K, C extends StmtContext<?, ?, ?>, N extends StatementNamespace<K, ?, ?>> AbstractPrerequisite<C>
            requiresCtxImpl(final StmtContext<?, ?, ?> context, final Class<N> namespace, final K key,
                    final ModelProcessingPhase phase)  {
        checkNotRegistered();

        AddedToNamespace<C> addedToNs = new AddedToNamespace<>(phase);
        addReq(addedToNs);
        contextImpl(context).onNamespaceItemAddedAction(namespace, key, addedToNs);
        return addedToNs;
    }

    private <K, C extends StmtContext<?, ?, ?>, N extends StatementNamespace<K, ?, ?>> AbstractPrerequisite<C>
            requiresCtxImpl(final StmtContext<?, ?, ?> context, final Class<N> namespace,
                    final NamespaceKeyCriterion<K> criterion, final ModelProcessingPhase phase)  {
        checkNotRegistered();

        AddedToNamespace<C> addedToNs = new AddedToNamespace<>(phase);
        addReq(addedToNs);
        contextImpl(context).onNamespaceItemAddedAction(namespace, phase, criterion, addedToNs);
        return addedToNs;
    }

    private <C extends StmtContext<?, ?, ?>> AbstractPrerequisite<C> requiresCtxImpl(final C context,
            final ModelProcessingPhase phase) {
        checkNotRegistered();

        PhaseFinished<C> phaseFin = new PhaseFinished<>();
        addReq(phaseFin);
        contextImpl(context).addPhaseCompletedListener(phase, phaseFin);
        return phaseFin;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <K, C extends Mutable<?, ?, ?>, N extends IdentifierNamespace<K, ? extends StmtContext<?, ?, ?>>>
            AbstractPrerequisite<C> mutatesCtxImpl(final StmtContext<?, ?, ?> context, final Class<N> namespace,
                    final K key, final ModelProcessingPhase phase) {
        checkNotRegistered();

        final PhaseModificationInNamespace<C> mod = new PhaseModificationInNamespace<>(EFFECTIVE_MODEL);
        addReq(mod);
        addMutation(mod);
        contextImpl(context).onNamespaceItemAddedAction((Class) namespace, key, mod);
        return mod;
    }

    private static StatementContextBase<?, ?, ?> contextImpl(final Object value) {
        checkArgument(value instanceof StatementContextBase, "Supplied context %s is not provided by this reactor.",
            value);
        return StatementContextBase.class.cast(value);
    }

    boolean tryApply() {
        checkState(action != null, "Action was not defined yet.");

        if (removeSatisfied()) {
            applyAction();
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public <C extends Mutable<?, ?, ?>, T extends C> Prerequisite<C> mutatesCtx(final T context,
            final ModelProcessingPhase phase) {
        return addMutation(new PhaseMutation<>(contextImpl(context), phase));
    }

    @Nonnull
    @Override
    public <A,D extends DeclaredStatement<A>,E extends EffectiveStatement<A, D>>
            AbstractPrerequisite<StmtContext<A, D, E>> requiresCtx(final StmtContext<A, D, E> context,
                    final ModelProcessingPhase phase) {
        return requiresCtxImpl(context, phase);
    }


    @Nonnull
    @Override
    public <K, N extends StatementNamespace<K, ?, ?>> Prerequisite<StmtContext<?, ?, ?>> requiresCtx(
            final StmtContext<?, ?, ?> context, final Class<N> namespace, final K key,
            final ModelProcessingPhase phase) {
        return requiresCtxImpl(context, namespace, key, phase);
    }

    @Nonnull
    @Override
    public <K, N extends StatementNamespace<K, ?, ?>> Prerequisite<StmtContext<?, ?, ?>> requiresCtx(
            final StmtContext<?, ?, ?> context, final Class<N> namespace, final NamespaceKeyCriterion<K> criterion,
            final ModelProcessingPhase phase) {
        return requiresCtxImpl(context, namespace, criterion, phase);
    }

    @Nonnull
    @Override
    public <D extends DeclaredStatement<?>> Prerequisite<D> requiresDeclared(
            final StmtContext<?, ? extends D, ?> context) {
        return requiresCtxImpl(context, FULL_DECLARATION).transform(StmtContext::buildDeclared);
    }

    @Nonnull
    @Override
    public <K, D extends DeclaredStatement<?>, N extends StatementNamespace<K, ? extends D, ?>> Prerequisite<D>
            requiresDeclared(final StmtContext<?, ?, ?> context, final Class<N> namespace, final K key) {
        final AbstractPrerequisite<StmtContext<?, D, ?>> rawContext = requiresCtxImpl(context, namespace, key,
            FULL_DECLARATION);
        return rawContext.transform(StmtContext::buildDeclared);
    }

    @Nonnull
    @Override
    public <K, D extends DeclaredStatement<?>, N extends StatementNamespace<K, ? extends D, ?>>
            AbstractPrerequisite<StmtContext<?, D, ?>> requiresDeclaredCtx(final StmtContext<?, ?, ?> context,
                    final Class<N> namespace, final K key) {
        return requiresCtxImpl(context, namespace, key, FULL_DECLARATION);
    }

    @Nonnull
    @Override
    public <E extends EffectiveStatement<?, ?>> Prerequisite<E> requiresEffective(
            final StmtContext<?, ?, ? extends E> stmt) {
        return requiresCtxImpl(stmt, EFFECTIVE_MODEL).transform(StmtContext::buildEffective);
    }

    @Nonnull
    @Override
    public <K, E extends EffectiveStatement<?, ?>, N extends StatementNamespace<K, ?, ? extends E>> Prerequisite<E>
            requiresEffective(final StmtContext<?, ?, ?> context, final Class<N> namespace, final K key) {
        final AbstractPrerequisite<StmtContext<?, ?, E>> rawContext = requiresCtxImpl(context, namespace, key,
            EFFECTIVE_MODEL);
        return rawContext.transform(StmtContext::buildEffective);
    }

    @Nonnull
    @Override
    public <K, E extends EffectiveStatement<?, ?>, N extends StatementNamespace<K, ?, ? extends E>>
            AbstractPrerequisite<StmtContext<?, ?, E>> requiresEffectiveCtx(final StmtContext<?, ?, ?> context,
                    final Class<N> namespace, final K key) {
        return requiresCtxImpl(contextImpl(context), namespace, key, EFFECTIVE_MODEL);
    }

    @Nonnull
    @Override
    public <N extends IdentifierNamespace<?, ?>> Prerequisite<Mutable<?, ?, ?>> mutatesNs(
            final Mutable<?, ?, ?> context, final Class<N> namespace) {
        return addMutation(new NamespaceMutation<>(contextImpl(context), namespace));
    }

    @Nonnull
    @Override
    public <K, E extends EffectiveStatement<?, ?>, N extends IdentifierNamespace<K, ? extends StmtContext<?, ?, ?>>>
            AbstractPrerequisite<Mutable<?, ?, E>> mutatesEffectiveCtx(final StmtContext<?, ?, ?> context,
                    final Class<N> namespace, final K key) {
        return mutatesCtxImpl(context, namespace, key, EFFECTIVE_MODEL);
    }

    @Nonnull
    @Override
    public <K, E extends EffectiveStatement<?, ?>, N extends IdentifierNamespace<K, ? extends StmtContext<?, ?, ?>>>
            AbstractPrerequisite<Mutable<?, ?, E>> mutatesEffectiveCtxPath(final StmtContext<?, ?, ?> context,
                    final Class<N> namespace, final Iterable<K> keys) {
        checkNotRegistered();

        final PhaseModificationInNamespacePath<Mutable<?, ?, E>, K, N> ret = new PhaseModificationInNamespacePath<>(
                EFFECTIVE_MODEL, keys);
        addReq(ret);
        addMutation(ret);

        ret.hookOnto(context, namespace);
        return ret;
    }

    @Override
    @SuppressWarnings("checkstyle:hiddenField")
    public void apply(final InferenceAction action) {
        checkState(this.action == null, "Action already defined to %s", this.action);
        this.action = requireNonNull(action);
    }

    private abstract class AbstractPrerequisite<T> implements Prerequisite<T> {
        private boolean done = false;
        private T value;

        @Override
        @SuppressWarnings("checkstyle:hiddenField")
        public final T resolve(final InferenceContext ctx) {
            checkState(done);
            checkArgument(ctx == ModifierImpl.this.ctx);
            return value;
        }

        final boolean isDone() {
            return done;
        }

        @SuppressWarnings("checkstyle:hiddenField")
        final boolean resolvePrereq(final T value) {
            this.value = value;
            this.done = true;
            return isApplied();
        }

        final <O> Prerequisite<O> transform(final Function<? super T, O> transformation) {
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

    private final class PhaseMutation<C> extends AbstractPrerequisite<C> implements ContextMutation {
        @SuppressWarnings("unchecked")
        PhaseMutation(final StatementContextBase<?, ?, ?> context, final ModelProcessingPhase phase) {
            context.addMutation(phase, this);
            resolvePrereq((C) context);
        }

        @Override
        public boolean isFinished() {
            return isApplied();
        }
    }

    private final class PhaseFinished<C extends StmtContext<?, ?, ?>> extends AbstractPrerequisite<C>
            implements OnPhaseFinished {
        @SuppressWarnings("unchecked")
        @Override
        public boolean phaseFinished(final StatementContextBase<?, ?, ?> context,
                final ModelProcessingPhase finishedPhase) {
            return resolvePrereq((C) context);
        }
    }

    private final class NamespaceMutation<N extends IdentifierNamespace<?, ?>>
            extends AbstractPrerequisite<Mutable<?, ?, ?>>  {
        NamespaceMutation(final StatementContextBase<?, ?, ?> ctx, final Class<N> namespace) {
            resolvePrereq(ctx);
        }
    }

    private final class AddedToNamespace<C extends StmtContext<?, ?, ?>> extends AbstractPrerequisite<C>
            implements OnNamespaceItemAdded, OnPhaseFinished {
        private final ModelProcessingPhase phase;

        AddedToNamespace(final ModelProcessingPhase phase) {
            this.phase = requireNonNull(phase);
        }

        @Override
        public void namespaceItemAdded(final StatementContextBase<?, ?, ?> context, final Class<?> namespace,
                final Object key, final Object value) {
            ((StatementContextBase<?, ?, ?>) value).addPhaseCompletedListener(phase, this);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean phaseFinished(final StatementContextBase<?, ?, ?> context,
                final ModelProcessingPhase finishedPhase) {
            return resolvePrereq((C) context);
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
            return super.addToStringAttributes(toStringHelper).add("phase", phase);
        }
    }

    private final class PhaseModificationInNamespace<C extends Mutable<?, ?, ?>> extends AbstractPrerequisite<C>
            implements OnNamespaceItemAdded, ContextMutation {
        private final ModelProcessingPhase modPhase;

        PhaseModificationInNamespace(final ModelProcessingPhase phase) {
            checkArgument(phase != null, "Model processing phase must not be null");
            this.modPhase = phase;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void namespaceItemAdded(final StatementContextBase<?, ?, ?> context, final Class<?> namespace,
                final Object key, final Object value) {
            StatementContextBase<?, ?, ?> targetCtx = contextImpl(value);
            targetCtx.addMutation(modPhase, this);
            resolvePrereq((C) targetCtx);
        }

        @Override
        public boolean isFinished() {
            return isApplied();
        }
    }

    private final class PhaseModificationInNamespacePath<C extends Mutable<?, ?, ?>, K,
            N extends IdentifierNamespace<K, ? extends StmtContext<?, ?, ?>>> extends AbstractPrerequisite<C>
            implements OnNamespaceItemAdded, ContextMutation {
        private final ModelProcessingPhase modPhase;
        private final Iterable<K> keys;
        private final Iterator<K> it;

        PhaseModificationInNamespacePath(final ModelProcessingPhase phase, final Iterable<K> keys) {
            this.modPhase = requireNonNull(phase);
            this.keys = requireNonNull(keys);
            it = keys.iterator();
        }

        @Override
        public boolean isFinished() {
            return isApplied();
        }

        @Override
        public void namespaceItemAdded(final StatementContextBase<?, ?, ?> context, final Class<?> namespace,
                final Object key, final Object value) {
            LOG.debug("Action for {} got key {}", keys, key);

            final StatementContextBase<?, ?, ?> target = contextImpl(value);
            if (!target.isSupportedByFeatures()) {
                LOG.debug("Key {} in {} is not supported", key, keys);
                resolvePrereq(null);
                action.prerequisiteUnavailable(this);
                return;
            }

            if (!it.hasNext()) {
                target.addMutation(modPhase, this);
                resolvePrereq((C) value);
                return;
            }

            hookOnto(target, namespace, it.next());
        }

        void hookOnto(final StmtContext<?, ?, ?> context, final Class<?> namespace) {
            checkArgument(it.hasNext(), "Namespace %s keys may not be empty", namespace);
            hookOnto(contextImpl(context), namespace, it.next());
        }

        @SuppressWarnings("unchecked")
        private void hookOnto(final StatementContextBase<?, ?, ?> context, final Class<?> namespace, final K key) {
            context.onNamespaceItemAddedAction((Class) namespace, requireNonNull(key), this);
        }
    }
}
