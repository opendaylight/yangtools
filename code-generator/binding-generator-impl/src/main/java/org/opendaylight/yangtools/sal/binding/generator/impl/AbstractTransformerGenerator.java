/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import javassist.ClassPool;

import org.eclipse.xtext.xbase.lib.Extension;
import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.util.ClassLoaderUtils;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.BindingCodec;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import com.google.common.base.Preconditions;

/**
 * Abstract base class which defines the baseline for the real {@link TransformerGenerator}.
 * This class exists to expose the basic interface and common interactions with the rest
 * of the package.
 */
abstract class AbstractTransformerGenerator {
    private static final Map<SchemaPath, InstanceIdentifier<?>> PATH_TO_BINDING_IDENTIFIER = new ConcurrentHashMap<>();

    /*
     * The generator has to always use this strategy, otherwise we may end up
     * will VerificationErrors.
     */
    @Extension
    protected static final ClassLoadingStrategy CLASS_LOADING_STRATEGY =
            GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy();
    @Extension
    protected final TypeResolver typeResolver;
    @Extension
    protected final JavassistUtils javAssist;

    /*
     * This is effectively final, but we have an implementation circle, where this
     * class notifies LazyGeneratedCodecRegistry and it calls our methods. The
     * listener is initialized to non-null before it is exposed.
     */
    private GeneratorListener listener;

    protected AbstractTransformerGenerator(final TypeResolver typeResolver, final ClassPool pool) {
        this.typeResolver = Preconditions.checkNotNull(typeResolver);
        this.javAssist = JavassistUtils.forClassPool(pool);
    }

    protected final GeneratorListener getListener() {
        if (listener == null) {
            synchronized (this) {
                Preconditions.checkState(listener != null, "Implementation not fully initialized");
            }
        }

        return listener;
    }

    synchronized final void setListener(final GeneratorListener listener) {
        Preconditions.checkState(this.listener == null, "Implementation already initialized");
        this.listener = Preconditions.checkNotNull(listener);
    }

    protected final <V> V runOnClassLoader(final ClassLoader cls, final Callable<V> function) throws Exception {
        final Lock lock = javAssist.getLock();

        lock.lock();
        try {
            javAssist.appendClassLoaderIfMissing(cls);
            return ClassLoaderUtils.withClassLoader(cls, function);
        } finally {
            lock.unlock();
        }
    }

    private static final Map<SchemaPath, InstanceIdentifier<?>> pathToBindingIdentifier = new ConcurrentHashMap<>();

    protected final InstanceIdentifier<?> getBindingIdentifierByPath(final SchemaPath path) {
        return pathToBindingIdentifier.get(path);
    }

    protected final void putPathToBindingIdentifier(final SchemaPath path, final InstanceIdentifier<?> bindingIdentifier) {
        pathToBindingIdentifier.put(path, bindingIdentifier);
    }

    protected final InstanceIdentifier<?> putPathToBindingIdentifier(final SchemaPath path,
            final InstanceIdentifier<?> bindingIdentifier, final Class<?> childClass) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        InstanceIdentifier<?> newId = bindingIdentifier.builder().child((Class) childClass).build();
        pathToBindingIdentifier.put(path, newId);
        return newId;
    }

    // Called from LazyGeneratedCodecRegistry
    abstract Class<? extends BindingCodec<Map<QName, Object>, Object>> augmentationTransformerFor(Class<?> inputType);
    abstract Class<? extends BindingCodec<Object, Object>> caseCodecFor(Class<?> inputType, ChoiceCaseNode node);
    abstract Class<? extends BindingCodec<Map<QName, Object>, Object>> keyTransformerForIdentifiable(Class<?> parentType);
    abstract Class<? extends BindingCodec<Map<QName, Object>, Object>> keyTransformerForIdentifier(Class<?> inputType);
    abstract Class<? extends BindingCodec<Map<QName, Object>, Object>> transformerFor(Class<?> inputType);
}
