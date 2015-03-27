/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.EffectiveModel;
import static org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase.FullDeclaration;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.ContextMutation;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.OnNamespaceItemAdded;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.OnPhaseFinished;

class ModifierImpl implements ModelActionBuilder {


    ModifierImpl(ModelProcessingPhase phase) {
        this.phase = Preconditions.checkNotNull(phase);
    }

    private final ModelProcessingPhase phase;

    private Set<AbstractPrerequisite<?>> unsatisfied = new HashSet<>();
    private Set<AbstractPrerequisite<?>> mutations = new HashSet<>();

    private InferenceAction action;
    private boolean actionApplied = false;

    private <D> AbstractPrerequisite<D> addReq(AbstractPrerequisite<D> prereq) {
        unsatisfied.add(prereq);
        return prereq;
    }

    private <T> AbstractPrerequisite<T> addMutation(AbstractPrerequisite<T> mutation) {
        mutations.add(mutation);
        return mutation;
    }



    private void checkNotRegistered() {
        Preconditions.checkState(action == null, "Action was already registered.");
    }

    private IllegalStateException shouldNotHappenProbablyBug(SourceException e) {
        return new IllegalStateException("Source exception during registering prerequisite. This is probably bug.",e);
    }

    private void tryToResolve() throws InferenceException {
        if(action == null) {
            return; // Action was not yet defined
        }
        if(removeSatisfied()) {
            applyAction();
        }
    }

    private boolean removeSatisfied() {
        Iterator<AbstractPrerequisite<?>> prereq = unsatisfied.iterator();
        boolean allSatisfied = true;
        while(prereq.hasNext()) {
            if(prereq.next().isDone()) {
                prereq.remove(); // We are removing current prerequisite from list.
            } else {
                allSatisfied  = false;
            }
        }
        return allSatisfied;
    }

    ModelProcessingPhase getPhase() {
        return phase;
    }

    boolean isApplied() {

        return actionApplied;
    }

    void failModifier() throws InferenceException {
        removeSatisfied();
        action.prerequisiteFailed(unsatisfied);
        action = null;
    }

    private void applyAction() throws InferenceException {

        action.apply();
        // Mark all mutations as performed, so context node could move to next.
        actionApplied = true;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <K, C extends StmtContext<?,?,?>, N extends StatementNamespace<K, ?, ?>> AbstractPrerequisite<C> requiresCtxImpl(StmtContext<?, ?, ?> context, Class<N> namespace, K key,ModelProcessingPhase phase)  {
        checkNotRegistered();
        try {
            AddedToNamespace<C> addedToNs = new AddedToNamespace<C>(phase);
            addReq(addedToNs);
            contextImpl(context).onNamespaceItemAddedAction((Class) namespace,key,addedToNs);
            return addedToNs;
        } catch (SourceException e) {
            throw shouldNotHappenProbablyBug(e);
        }
    }

    private <C extends StmtContext<?, ?, ?>> AbstractPrerequisite<C> requiresCtxImpl(C context, ModelProcessingPhase phase) {
        Preconditions.checkState(action == null, "Action was already registered.");
        try {
            PhaseFinished<C> phaseFin = new PhaseFinished<C>();
            addReq(phaseFin);
            contextImpl(context).addPhaseCompletedListener(FullDeclaration,phaseFin);
            return phaseFin;
        } catch (SourceException e) {
            throw shouldNotHappenProbablyBug(e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <K, C extends StmtContext.Mutable<?, ?, ?> , N extends StatementNamespace<K, ?, ? >> AbstractPrerequisite<C> mutatesCtxImpl(
                StmtContext<?, ?, ?> context, Class<N> namespace, K key, ModelProcessingPhase phase) {
            try {
                PhaseModificationInNamespace<C> mod = new PhaseModificationInNamespace<C>(phase);
                addMutation(mod);
                contextImpl(context).onNamespaceItemAddedAction((Class) namespace,key,mod);
                return mod;
            } catch (SourceException e) {
                throw shouldNotHappenProbablyBug(e);
            }
        }

    private static StatementContextBase<?,?,?> contextImpl(StmtContext<?,?,?> context) {
        Preconditions.checkArgument(context instanceof StatementContextBase,"Supplied context was not provided by this reactor.");
        return StatementContextBase.class.cast(context);
    }

    @Override
    public <C extends Mutable<?, ?, ?>, CT extends C> Prerequisite<C> mutatesCtx(CT context, ModelProcessingPhase phase) {
        try {
            return addMutation(new PhaseMutation<C>(contextImpl(context),phase));
        } catch (InferenceException e) {
            throw shouldNotHappenProbablyBug(e);
        }
    }

    @Override
    public  <A,D extends DeclaredStatement<A>,E extends EffectiveStatement<A, D>> AbstractPrerequisite<StmtContext<A, D, E>> requiresCtx(StmtContext<A, D, E> context, ModelProcessingPhase phase) {
        return requiresCtxImpl(context, phase);
    }


    @Override
    public <K, N extends StatementNamespace<K, ?, ? >> Prerequisite<StmtContext<?,?,?>> requiresCtx(StmtContext<?, ?, ?> context, Class<N> namespace, K key, ModelProcessingPhase phase) {
        return requiresCtxImpl(context, namespace, key, phase);
    }

    @Override
    public <D extends DeclaredStatement<?>> Prerequisite<D> requiresDeclared(StmtContext<?, ? extends D, ?> context) {
        return requiresCtxImpl(context,FullDeclaration).transform(StmtContextUtils.<D>buildDeclared());
    }

    @Override
    public <K, D extends DeclaredStatement<?>, N extends StatementNamespace<K, ? extends D, ?>> AbstractPrerequisite<StmtContext<?, D, ?>> requiresDeclaredCtx(
            StmtContext<?, ?, ?> context, Class<N> namespace, K key) {
        return requiresCtxImpl(context, namespace, key,FullDeclaration);
    }

    @Override
    public <K, D extends DeclaredStatement<?>, N extends StatementNamespace<K, ? extends D, ?>> Prerequisite<D> requiresDeclared(
            StmtContext<?, ?, ?> context, Class<N> namespace, K key) {
        final AbstractPrerequisite<StmtContext<?,D,?>> rawContext = requiresCtxImpl(context, namespace, key,FullDeclaration);
        return rawContext.transform(StmtContextUtils.<D>buildDeclared());
    }

    @Override
    public <E extends EffectiveStatement<?, ?>> Prerequisite<E> requiresEffective(StmtContext<?, ?, ? extends E> stmt) {
        return requiresCtxImpl(stmt,EffectiveModel).transform(StmtContextUtils.<E>buildEffective());
    }

    @Override
    public <K, E extends EffectiveStatement<?, ?>, N extends StatementNamespace<K, ?, ? extends E>> AbstractPrerequisite<StmtContext<?, ?, E>> requiresEffectiveCtx(
            StmtContext<?, ?, ?> context, Class<N> namespace, K key) {
        return requiresCtxImpl(contextImpl(context),namespace,key,EffectiveModel);
    }

    @Override
    public <K, E extends EffectiveStatement<?, ?>, N extends StatementNamespace<K, ?, ? extends E>> Prerequisite<E> requiresEffective(
            StmtContext<?, ?, ?> context, Class<N> namespace, K key) {
        final AbstractPrerequisite<StmtContext<?,?,E>> rawContext = requiresCtxImpl(context, namespace, key,EffectiveModel);
        return rawContext.transform(StmtContextUtils.<E>buildEffective());
    }


    @Override
    public <N extends IdentifierNamespace<?, ?>> Prerequisite<Mutable<?, ?, ?>> mutatesNs(Mutable<?, ?, ?> context,
            Class<N> namespace) {
        try {
            return addMutation(new NamespaceMutation<N>(contextImpl(context),namespace));
        } catch (SourceException e) {
            throw shouldNotHappenProbablyBug(e);
        }
    }

    @Override
    public <T extends Mutable<?, ?, ?>> Prerequisite<T> mutatesEffectiveCtx(T stmt) {
        return mutatesCtx(stmt, EffectiveModel);
    }


   @Override
    public <K, E extends EffectiveStatement<?, ?>, N extends StatementNamespace<K, ?, ? extends E>> AbstractPrerequisite<Mutable<?, ?, E>> mutatesEffectiveCtx(
            StmtContext<?, ?, ?> context, Class<N> namespace, K key) {
        return mutatesCtxImpl(context, namespace, key, EffectiveModel);
    }



    @Override
    public void apply(InferenceAction action) throws InferenceException {
        this.action = Preconditions.checkNotNull(action);
        tryToResolve();
    }

    private abstract class AbstractPrerequisite<T> implements Prerequisite<T> {

        private T value;
        private boolean done = false;

        @Override
        public T get() {
            Preconditions.checkState(isDone());
            return value;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        protected void resolvePrereq(T value) throws InferenceException {
            Preconditions.checkState(!isDone());
            this.value = value;
            this.done = true;
            tryToResolve();
        }

        protected <O> Prerequisite<O> transform(final Function<? super T,O> transformation) {

            return new Prerequisite<O>() {

                @Override
                public O get() {
                    return transformation.apply(AbstractPrerequisite.this.get());
                }

                @Override
                public boolean isDone() {
                    return AbstractPrerequisite.this.isDone();
                }

            };
        }

    }

    private class PhaseMutation<C> extends AbstractPrerequisite<C> implements ContextMutation {

        @SuppressWarnings("unchecked")
        public PhaseMutation(StatementContextBase<?, ?, ?> context, ModelProcessingPhase phase) throws InferenceException {
            context.addMutation(phase, this);
            resolvePrereq((C) context);
        }

        @Override
        public boolean isFinished() {
            return isApplied();
        }


    }
    private class PhaseFinished<C extends StmtContext<?, ?, ?>> extends AbstractPrerequisite<C> implements OnPhaseFinished {

        @SuppressWarnings("unchecked")
        @Override
        public void phaseFinished(StatementContextBase<?, ?, ?> context, ModelProcessingPhase phase) throws SourceException {
            resolvePrereq((C) (context));
        }
    }

    private class NamespaceMutation<N extends IdentifierNamespace<?,?>> extends  AbstractPrerequisite<StmtContext.Mutable<?, ?, ?>>  {

        public NamespaceMutation(StatementContextBase<?, ?, ?> ctx, Class<N> namespace) throws InferenceException {
            resolvePrereq(ctx);
        }

    }

    private class AddedToNamespace<C extends StmtContext<?,?,?>> extends  AbstractPrerequisite<C> implements OnNamespaceItemAdded,OnPhaseFinished {

        private ModelProcessingPhase phase;

        public <K, N extends StatementNamespace<K, ?, ?>> AddedToNamespace(ModelProcessingPhase phase) {
            this.phase = phase;
        }

        @Override
        public void namespaceItemAdded(StatementContextBase<?, ?, ?> context, Class<?> namespace, Object key,
                Object value) throws SourceException {
            StatementContextBase<?, ?, ?> targetContext = (StatementContextBase<?, ?, ?>) value;
            targetContext.addPhaseCompletedListener(phase, this);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void phaseFinished(StatementContextBase<?, ?, ?> context, ModelProcessingPhase phase) throws SourceException {
            resolvePrereq((C) context);
        }

    }

    private class PhaseModificationInNamespace<C extends Mutable<?,?,?>> extends AbstractPrerequisite<C> implements OnNamespaceItemAdded, ContextMutation {

        private final ModelProcessingPhase modPhase;

        public <K, N extends StatementNamespace<K, ?, ?>> PhaseModificationInNamespace(ModelProcessingPhase phase) throws SourceException {
            Preconditions.checkArgument(phase != null, "Model processing phase must not be null");
            this.modPhase = phase;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void namespaceItemAdded(StatementContextBase<?, ?, ?> context, Class<?> namespace, Object key,
                Object value) throws SourceException {
            context.addMutation(modPhase,this);
            resolvePrereq((C) context);
        }

        @Override
        public boolean isFinished() {
            return isApplied();
        }
    }

}
