/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.loader;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// A leaf class loader, binding together a root class loader and some other class loader
final class LeafBindingClassLoader extends BindingClassLoader {
    static {
        verify(registerAsParallelCapable());
    }

    private static final Logger LOG = LoggerFactory.getLogger(LeafBindingClassLoader.class);

    private final @NonNull RootBindingClassLoader root;
    private final @NonNull ClassLoader target;

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<LeafBindingClassLoader, ImmutableSet> DEPENDENCIES_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(LeafBindingClassLoader.class, ImmutableSet.class, "dependencies");
    private volatile ImmutableSet<LeafBindingClassLoader> dependencies = ImmutableSet.of();

    LeafBindingClassLoader(final RootBindingClassLoader root, final ClassLoader target) {
        super(root);
        this.root = requireNonNull(root);
        this.target = requireNonNull(target);
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        try {
            return target.loadClass(name);
        } catch (ClassNotFoundException e) {
            LOG.trace("Class {} not found in target, looking through dependencies", name);
            for (LeafBindingClassLoader loader : dependencies) {
                // Careful: a loading operation may be underway, make sure that process has completed
                synchronized (loader.getClassLoadingLock(name)) {
                    final var loaded = loader.findLoadedClass(name);
                    if (loaded != null) {
                        LOG.trace("Class {} found in dependency {}", name, loader);
                        return loaded;
                    }
                }
            }

            throw e;
        }
    }

    @Override
    BindingClassLoader findClassLoader(final Class<?> bindingClass) {
        return target.equals(bindingClass.getClassLoader()) ? this : root.findClassLoader(bindingClass);
    }

    @Override
    void appendLoaders(final Set<LeafBindingClassLoader> newLoaders) {
        while (true) {
            final var local = dependencies;
            final var builder = new ArrayList<LeafBindingClassLoader>(local.size() + newLoaders.size());
            builder.addAll(local);
            builder.addAll(newLoaders);
            final var updated = ImmutableSet.copyOf(builder);
            if (local.equals(updated) || DEPENDENCIES_UPDATER.compareAndSet(this, local, updated)) {
                // No need for an update or the update was successful
                return;
            }
        }
    }
}
