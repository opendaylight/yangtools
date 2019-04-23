/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.loader;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// A root codec classloader, binding only whatever is available StaticClassPool
final class RootCodecClassLoader extends CodecClassLoader {
    private static final Logger LOG = LoggerFactory.getLogger(RootCodecClassLoader.class);

    static {
        verify(registerAsParallelCapable());
    }

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<RootCodecClassLoader, ImmutableMap> LOADERS_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(RootCodecClassLoader.class, ImmutableMap.class, "loaders");

    private volatile ImmutableMap<ClassLoader, CodecClassLoader> loaders = ImmutableMap.of();

    RootCodecClassLoader() {
        super();
    }

    @Override
    CodecClassLoader findClassLoader(final Class<?> bindingClass) {
        final ClassLoader target = bindingClass.getClassLoader();
        if (target == null) {
            // No class loader associated ... well, let's use root then
            return this;
        }

        // Cache for update
        ImmutableMap<ClassLoader, CodecClassLoader> local = loaders;
        final CodecClassLoader known = local.get(target);
        if (known != null) {
            return known;
        }

        // Alright, we need to determine if the class is accessible through our hierarchy (in which case we use
        // ourselves) or we need to create a new Leaf.
        final CodecClassLoader found;
        if (!isOurClass(bindingClass)) {
            StaticClassPool.verifyStaticLinkage(target);
            found = AccessController.doPrivileged(
                (PrivilegedAction<CodecClassLoader>)() -> new LeafCodecClassLoader(this, target));
        } else {
            found = this;
        }

        // Now make sure we cache this result
        while (true) {
            final Builder<ClassLoader, CodecClassLoader> builder = ImmutableMap.builderWithExpectedSize(
                local.size() + 1);
            builder.putAll(local);
            builder.put(target, found);

            if (LOADERS_UPDATER.compareAndSet(this, local, builder.build())) {
                return found;
            }

            local = loaders;
            final CodecClassLoader recheck = local.get(target);
            if (recheck != null) {
                return recheck;
            }
        }
    }

    @Override
    void appendLoaders(final Set<LeafCodecClassLoader> newLoaders) {
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
}
