/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.loader;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Static class pool, bound to the class loader of binding-dom-codec. It can be used to acquire CtClass instances that
 * reside within the binding-dom-codec artifact or any of its direct mandatory dependencies. It can also instantiate
 * {@link CodecClassLoader} instances for use with code generation.
 *
 * @author Robert Varga
 */
@Beta
public final class StaticClassPool {
    static final ClassLoader LOADER = verifyNotNull(StaticClassPool.class.getClassLoader());
    static final ClassPool POOL;

    static {
        final ClassPool pool = new ClassPool();
        pool.appendClassPath(new LoaderClassPath(LOADER));
        POOL = pool;
    }

    private StaticClassPool() {
        // Utility class
    }

    /**
     * Instantiate a new CodecClassLoader.
     *
     * @return A new CodecClassLoader.
     */
    public static @NonNull CodecClassLoader createLoader() {
        return AccessController.doPrivileged((PrivilegedAction<CodecClassLoader>)() -> new RootCodecClassLoader());
    }

    /**
     * Resolve a binding-dom-codec class to its {@link CtClass} counterpart.
     *
     * @param clazz Class to resolve
     * @return A CtClass instance
     * @throws IllegalStateException if the class cannot be resolved
     * @throws NullPointerException if {@code clazz} is null
     */
    public static synchronized @NonNull CtClass findClass(final @NonNull Class<?> clazz) {
        final CtClass ret;
        try {
            ret = POOL.get(clazz.getName());
        } catch (NotFoundException e) {
            throw new IllegalStateException("Failed to find " + clazz, e);
        }
        ret.freeze();
        return ret;
    }

    // Sanity check: target has to resolve yang-binding contents to the same class, otherwise we are in a pickle
    static void verifyStaticLinkage(final ClassLoader candidate) {
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
