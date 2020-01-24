/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.ContextMutation;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.OnPhaseFinished;

/**
 * Listener/Mutation/phase tracker associated with a particular statement context. This logic and state is split out
 * of our base implementation to reduce memory footprint in typical case where no listeners/mutations are registered.
 */
@NonNullByDefault
abstract class StatementPhaseState {
    private static final class Empty extends StatementPhaseState {
        Empty(final @Nullable ModelProcessingPhase phase) {
            super(phase);
        }

        @Override
        Empty withCompletedPhase(final @Nullable ModelProcessingPhase completedPhase) {
            return of(completedPhase);
        }

        @Override
        StatementPhaseState dispatchListeners(final StatementContextBase<?, ?, ?> context) {
            return this;
        }

        @Override
        StatementPhaseState addListener(final ModelProcessingPhase phase, final OnPhaseFinished listener) {
            return new Populated(getCompletedPhase(), newMultimap(phase, listener), ImmutableMultimap.of());
        }

        @Override
        StatementPhaseState addMutation(final ModelProcessingPhase phase, final ContextMutation mutation) {
            return new Populated(getCompletedPhase(), ImmutableMultimap.of(), newMultimap(phase, mutation));
        }

        @Override
        boolean completeMutations(final ModelProcessingPhase phase) {
            return true;
        }

        @Override
        StatementPhaseState trimMutations() {
            return this;
        }
    }

    private static final class Populated extends StatementPhaseState {
        private final Multimap<ModelProcessingPhase, OnPhaseFinished> listeners;
        private final Multimap<ModelProcessingPhase, ContextMutation> mutations;

        Populated(final @Nullable ModelProcessingPhase phase,
                final Multimap<ModelProcessingPhase, OnPhaseFinished> phaseListeners,
                final Multimap<ModelProcessingPhase, ContextMutation> phaseMutation) {
            super(phase);
            this.listeners = phaseListeners;
            this.mutations = phaseMutation;
        }

        @Override
        StatementPhaseState withCompletedPhase(final @Nullable ModelProcessingPhase completedPhase) {
            return completedPhase == getCompletedPhase() ? this
                    : new Populated(completedPhase, listeners, mutations);
        }

        @Override
        StatementPhaseState addListener(final ModelProcessingPhase phase, final OnPhaseFinished listener) {
            if (listeners instanceof ImmutableMultimap) {
                return new Populated(getCompletedPhase(), newMultimap(phase, listener), mutations);
            }

            listeners.put(phase, listener);
            return this;
        }

        @Override
        StatementPhaseState dispatchListeners(final StatementContextBase<?, ?, ?> context) {
            final ModelProcessingPhase phase = nonnullPhase();
            final Collection<OnPhaseFinished> satisfied = listeners.get(phase);
            if (satisfied.isEmpty()) {
                return this;
            }

            final Iterator<OnPhaseFinished> listener = satisfied.iterator();
            while (listener.hasNext()) {
                final OnPhaseFinished next = listener.next();
                if (next.phaseFinished(context, phase)) {
                    listener.remove();
                }
            }

            if (!satisfied.isEmpty()) {
                // FIXME: This check is weird -- we are getting a view after all, so a removal should not be needed
                listeners.removeAll(phase);
                if (listeners.isEmpty()) {
                    return mutations.isEmpty() ? of(phase) : new Populated(phase, ImmutableMultimap.of(),
                        mutations);
                }
            }
            return this;
        }

        @Override
        boolean completeMutations(final ModelProcessingPhase phase) {
            final Collection<ContextMutation> openMutations = mutations.get(phase);
            if (openMutations.isEmpty()) {
                return true;
            }

            boolean finished = true;
            if (!openMutations.isEmpty()) {
                final Iterator<ContextMutation> it = openMutations.iterator();
                while (it.hasNext()) {
                    final ContextMutation current = it.next();
                    if (current.isFinished()) {
                        it.remove();
                    } else {
                        finished = false;
                    }
                }

                if (openMutations.isEmpty()) {
                    // FIXME: This check is weird -- we are getting a view after all, so a removal should not be needed
                    mutations.removeAll(phase);
                }
            }

            return finished;
        }

        private ModelProcessingPhase nonnullPhase() {
            return verifyNotNull(getCompletedPhase());
        }

        @Override
        StatementPhaseState trimMutations() {
            if (mutations.isEmpty()) {
                final ModelProcessingPhase phase = getCompletedPhase();
                return listeners.isEmpty() ? of(phase) : new Populated(phase, listeners, ImmutableMultimap.of());
            }
            return this;
        }

        @Override
        StatementPhaseState addMutation(final ModelProcessingPhase phase, final ContextMutation mutation) {
            if (mutations instanceof ImmutableMultimap) {
                return new Populated(getCompletedPhase(), listeners, newMultimap(phase, mutation));
            }

            mutations.put(phase, mutation);
            return this;
        }
    }

    // Internal cache of empty states
    private static final EnumMap<ModelProcessingPhase, Empty> COMPLETED;

    static {
        final EnumMap<ModelProcessingPhase, Empty> map = new EnumMap<>(ModelProcessingPhase.class);
        for (ModelProcessingPhase phase : ModelProcessingPhase.values()) {
            map.put(phase, new Empty(phase));
        }
        COMPLETED = map;
    }

    private static final Empty INITIAL = new Empty(null);

    private final @Nullable ModelProcessingPhase completedPhase;

    private StatementPhaseState(final @Nullable ModelProcessingPhase completedPhase) {
        this.completedPhase = completedPhase;
    }

    static StatementPhaseState initial() {
        return INITIAL;
    }

    static Empty of(final @Nullable ModelProcessingPhase phase) {
        return phase == null ? INITIAL : verifyNotNull(COMPLETED.get(phase));
    }

    final @Nullable ModelProcessingPhase getCompletedPhase() {
        return completedPhase;
    }

    abstract StatementPhaseState withCompletedPhase(@Nullable ModelProcessingPhase completedPhase);

    abstract StatementPhaseState addListener(ModelProcessingPhase phase, OnPhaseFinished listener);

    abstract StatementPhaseState dispatchListeners(StatementContextBase<?, ?, ?> context);

    abstract StatementPhaseState addMutation(ModelProcessingPhase phase, ContextMutation mutation);

    abstract boolean completeMutations(ModelProcessingPhase phase);

    abstract StatementPhaseState trimMutations();

    private static <V> Multimap<ModelProcessingPhase, V> newMultimap(final ModelProcessingPhase key, final V value) {
        final Multimap<ModelProcessingPhase, V> map =  Multimaps.newListMultimap(
            new EnumMap<>(ModelProcessingPhase.class), () -> new ArrayList<>(1));
        map.put(key, value);
        return map;
    }

}
