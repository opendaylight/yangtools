/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.osgi.impl;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.binding.runtime.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.dom.codec.osgi.BindingRuntimeContextListener;
import org.opendaylight.mdsal.binding.dom.codec.osgi.BindingRuntimeContextService;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingRuntimeGenerator;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

@Deprecated
final class SimpleBindingRuntimeContextService extends
        ServiceTracker<BindingRuntimeContextListener, BindingRuntimeContextListener>
        implements BindingRuntimeContextService {
    private final SchemaSourceProvider<YangTextSchemaSource> sourceProvider;
    private final ClassLoadingStrategy strategy;
    private final Object lock = new Object();

    @GuardedBy("lock")
    private BindingRuntimeContext current;

    SimpleBindingRuntimeContextService(final BundleContext context, final ClassLoadingStrategy strategy,
        final SchemaSourceProvider<YangTextSchemaSource> sourceProvider) {
        super(context, BindingRuntimeContextListener.class, null);
        this.sourceProvider = requireNonNull(sourceProvider);
        this.strategy = requireNonNull(strategy);
    }

    @Override
    public ListenableFuture<? extends YangTextSchemaSource> getSource(final SourceIdentifier sourceIdentifier) {
        return sourceProvider.getSource(sourceIdentifier);
    }

    @Override
    public BindingRuntimeContext getBindingRuntimeContext() {
        synchronized (lock) {
            checkState(current != null, "Runtime context is not initialized yet");
            return current;
        }
    }

    void updateBindingRuntimeContext(final SchemaContext schemaContext) {
        final BindingRuntimeContext next = BindingRuntimeContext.create(
            new DefaultBindingRuntimeGenerator().generateTypeMapping(schemaContext), strategy);

        final BindingRuntimeContextListener[] listeners;
        synchronized (lock) {
            current = next;
            listeners = this.getServices(new BindingRuntimeContextListener[0]);
        }

        for (BindingRuntimeContextListener l : listeners) {
            l.onBindingRuntimeContextUpdated(next);
        }
    }

    @Override
    public BindingRuntimeContextListener addingService(
            final ServiceReference<BindingRuntimeContextListener> reference) {
        final BindingRuntimeContextListener listener = super.addingService(reference);

        synchronized (lock) {
            listener.onBindingRuntimeContextUpdated(current);
        }

        return listener;
    }
}
