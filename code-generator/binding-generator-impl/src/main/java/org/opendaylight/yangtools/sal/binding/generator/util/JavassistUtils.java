/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.util;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JavassistUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JavassistUtils.class);

    private static final Map<ClassPool, JavassistUtils> INSTANCES = new WeakHashMap<>();
    private final Map<ClassLoader, ClassPath> loaderClassPaths = new WeakHashMap<>();
    private final Lock lock = new ReentrantLock();
    private final ClassPool classPool;

    /**
     * @deprecated Use {@link #forClassPool(ClassPool)} instead.
     *
     * This class provides auto-loading into the classpool. Unfortunately reusing
     * the same class pool with multiple instances can lead the same classpath
     * being added multiple times, which lowers performance and leaks memory.
     */
    @Deprecated
    public JavassistUtils(final ClassPool pool) {
        this(pool, null);
    }

    private JavassistUtils(final ClassPool pool, final Object dummy) {
        // FIXME: Remove 'dummy' once deprecated constructor is removed
        classPool = Preconditions.checkNotNull(pool);
    }

    /**
     * Get a utility instance for a particular class pool. A new instance is
     * created if this is a new pool. If an instance already exists, is is
     * returned.
     *
     * @param pool Backing class pool
     * @return shared utility instance for specified pool
     */
    public static synchronized JavassistUtils forClassPool(final ClassPool pool) {
        JavassistUtils ret = INSTANCES.get(Preconditions.checkNotNull(pool));
        if (ret == null) {
            ret = new JavassistUtils(pool, null);
            INSTANCES.put(pool, ret);
        }
        return ret;
    }

    /**
     * Get reference to the internal lock.
     *
     * @return Lock object
     *
     * @deprecated Synchronize on an instance of this class instead.
     */
    @Deprecated
    public Lock getLock() {
        return lock;
    }

    public void method(final CtClass it, final Class<? extends Object> returnType, final String name,
            final Class<? extends Object> parameter, final MethodGenerator function1) throws CannotCompileException {
        final CtClass[] pa = new CtClass[] { asCtClass(parameter) };
        final CtMethod _ctMethod = new CtMethod(asCtClass(returnType), name, pa, it);

        final CtMethod method = _ctMethod;
        function1.process(method);
        it.addMethod(method);
    }

    public void method(final CtClass it, final Class<? extends Object> returnType, final String name,
            final Collection<? extends Class<?>> parameters, final MethodGenerator function1) throws CannotCompileException {
        final CtClass[] pa = new CtClass[parameters.size()];

        int i = 0;
        for (Class<? extends Object> parameter : parameters) {
            pa[i] = asCtClass(parameter);
            ++i;
        }

        final CtMethod method = new CtMethod(asCtClass(returnType), name, pa, it);
        function1.process(method);
        it.addMethod(method);
    }

    public void staticMethod(final CtClass it, final Class<? extends Object> returnType, final String name,
            final Class<? extends Object> parameter, final MethodGenerator function1) throws CannotCompileException {
        final CtClass[] pa = new CtClass[] { asCtClass(parameter) };
        final CtMethod _ctMethod = new CtMethod(asCtClass(returnType), name, pa, it);
        final CtMethod method = _ctMethod;
        function1.process(method);
        it.addMethod(method);
    }

    public void implementMethodsFrom(final CtClass target, final CtClass source, final MethodGenerator function1) throws CannotCompileException {
        for (CtMethod method : source.getMethods()) {
            if (method.getDeclaringClass() == source) {
                CtMethod redeclaredMethod = new CtMethod(method, target, null);
                function1.process(redeclaredMethod);
                target.addMethod(redeclaredMethod);
            }
        }
    }

    public CtClass createClass(final String fqn, final ClassGenerator cls) {
        CtClass target = classPool.makeClass(fqn);
        cls.process(target);
        return target;
    }

    public CtClass createClass(final String fqn, final CtClass superInterface, final ClassGenerator cls) {
        CtClass target = classPool.makeClass(fqn);
        implementsType(target, superInterface);
        cls.process(target);
        return target;
    }

    public void implementsType(final CtClass it, final CtClass supertype) {
        Preconditions.checkArgument(supertype.isInterface(), "Supertype must be interface");
        it.addInterface(supertype);
    }

    public CtClass asCtClass(final Class<? extends Object> class1) {
        return get(this.classPool, class1);
    }

    public CtField field(final CtClass it, final String name, final Class<? extends Object> returnValue) throws CannotCompileException {
        final CtField field = new CtField(asCtClass(returnValue), name, it);
        field.setModifiers(Modifier.PUBLIC);
        it.addField(field);
        return field;
    }

    public CtField staticField(final CtClass it, final String name, final Class<? extends Object> returnValue) throws CannotCompileException {
        return staticField(it, name, returnValue, null);
    }

    public CtField staticField(final CtClass it, final String name,
                               final Class<? extends Object> returnValue,
                               SourceCodeGenerator sourceGenerator) throws CannotCompileException {
        final CtField field = new CtField(asCtClass(returnValue), name, it);
        field.setModifiers(Modifier.PUBLIC + Modifier.STATIC);
        it.addField(field);

        if (sourceGenerator != null) {
            sourceGenerator.appendField(field, null);
        }

        return field;
    }

    public CtClass get(final ClassPool pool, final Class<? extends Object> cls) {
        try {
            return pool.get(cls.getName());
        } catch (NotFoundException nfe1) {
            appendClassLoaderIfMissing(cls.getClassLoader());
            try {
                return pool.get(cls.getName());
            } catch (final NotFoundException nfe2) {
                LOG.warn("Appending ClassClassPath for {}", cls, nfe2);
                pool.appendClassPath(new ClassClassPath(cls));
                try {
                    return pool.get(cls.getName());
                } catch (NotFoundException e) {
                    LOG.warn("Failed to load class {} from pool {}", cls, pool, e);
                    throw new IllegalStateException("Failed to load class", e);
                }
            }
        }
    }

    public synchronized void appendClassLoaderIfMissing(final ClassLoader loader) {
        if (!loaderClassPaths.containsKey(loader)) {
            final ClassPath ctLoader = new LoaderClassPath(loader);
            classPool.appendClassPath(ctLoader);
            loaderClassPaths.put(loader, ctLoader);
        }
    }

    public void ensureClassLoader(final Class<?> child) {
        appendClassLoaderIfMissing(child.getClassLoader());
    }
}
