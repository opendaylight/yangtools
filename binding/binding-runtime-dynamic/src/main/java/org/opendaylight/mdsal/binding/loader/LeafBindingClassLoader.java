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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// A leaf class loader, binding together a root class loader and some other class loader
final class LeafBindingClassLoader extends BindingClassLoader {
    static {
        verify(registerAsParallelCapable());
    }

    private static final Logger LOG = LoggerFactory.getLogger(LeafBindingClassLoader.class);
    private static final VarHandle DEPENDENCIES;

    static {
        try {
            DEPENDENCIES = MethodHandles.lookup()
                .findVarHandle(LeafBindingClassLoader.class, "dependencies", ImmutableSet.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull RootBindingClassLoader root;
    private final @NonNull ClassLoader target;

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
        var local = dependencies;

        while (true) {
            final var updated = ImmutableSet.builderWithExpectedSize(local.size() + newLoaders.size())
                .addAll(local)
                .addAll(newLoaders)
                .build();

            if (local.equals(updated)) {
                // No update needed, bail out
                return;
            }

            final var witness = (ImmutableSet<LeafBindingClassLoader>)
                DEPENDENCIES.compareAndExchange(this, local, updated);
            if (witness == local) {
                // Successful update, we are done
                return;
            }

            local = witness;
        }
    }
}
