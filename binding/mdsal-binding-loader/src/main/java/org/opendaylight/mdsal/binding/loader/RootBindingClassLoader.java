/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.loader;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// A root codec classloader, binding only whatever is available StaticClassPool
final class RootBindingClassLoader extends BindingClassLoader {
    private static final Logger LOG = LoggerFactory.getLogger(RootBindingClassLoader.class);

    static {
        verify(registerAsParallelCapable());
    }

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<RootBindingClassLoader, ImmutableMap> LOADERS_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(RootBindingClassLoader.class, ImmutableMap.class, "loaders");

    private volatile ImmutableMap<ClassLoader, BindingClassLoader> loaders = ImmutableMap.of();

    RootBindingClassLoader(final ClassLoader parentLoader, final @Nullable File dumpDir) {
        super(parentLoader, dumpDir);
    }

    @Override
    BindingClassLoader findClassLoader(final Class<?> bindingClass) {
        final var target = bindingClass.getClassLoader();
        if (target == null) {
            // No class loader associated ... well, let's use root then
            return this;
        }

        // Cache for update
        var local = loaders;
        final var known = local.get(target);
        if (known != null) {
            return known;
        }

        // Alright, we need to determine if the class is accessible through our hierarchy (in which case we use
        // ourselves) or we need to create a new Leaf.
        final BindingClassLoader found;
        if (!isOurClass(bindingClass)) {
            verifyStaticLinkage(target);
            found = AccessController.doPrivileged(
                (PrivilegedAction<BindingClassLoader>)() -> new LeafBindingClassLoader(this, target));
        } else {
            found = this;
        }

        // Now make sure we cache this result
        while (true) {
            final var builder = ImmutableMap.<ClassLoader, BindingClassLoader>builderWithExpectedSize(local.size() + 1);
            builder.putAll(local);
            builder.put(target, found);

            if (LOADERS_UPDATER.compareAndSet(this, local, builder.build())) {
                return found;
            }

            local = loaders;
            final var recheck = local.get(target);
            if (recheck != null) {
                return recheck;
            }
        }
    }

    @Override
    void appendLoaders(final Set<LeafBindingClassLoader> newLoaders) {
        // Root loader should never see the requirement for other loaders, as that would violate loop-free nature
        // of generated code: if a binding class is hosted in root loader, all its references must be visible from
        // the root loader and hence all the generated code ends up residing in the root loader, too.
        throw new IllegalStateException("Attempted to extend root loader with " + newLoaders);
    }

    private boolean isOurClass(final Class<?> bindingClass) {
        final Class<?> ourClass;
        try {
            ourClass = loadClass(bindingClass.getName(), false);
        } catch (ClassNotFoundException e) {
            LOG.debug("Failed to load {}", bindingClass, e);
            return false;
        }
        return bindingClass.equals(ourClass);
    }

    // Sanity check: target has to resolve yang-binding contents to the same class, otherwise we are in a pickle
    private static void verifyStaticLinkage(final ClassLoader candidate) {
        final Class<?> targetClazz;
        try {
            targetClazz = candidate.loadClass(DataContainer.class.getName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("ClassLoader " + candidate + " cannot load " + DataContainer.class, e);
        }
        verify(DataContainer.class.equals(targetClazz),
            "Class mismatch on DataContainer. Ours is from %s, target %s has %s from %s",
            DataContainer.class.getClassLoader(), candidate, targetClazz, targetClazz.getClassLoader());
    }
}
