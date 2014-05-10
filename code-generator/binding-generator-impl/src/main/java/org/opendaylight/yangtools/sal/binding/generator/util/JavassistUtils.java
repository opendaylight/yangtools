/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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

import org.eclipse.xtext.xbase.lib.Conversions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class JavassistUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JavassistUtils.class);

    private final Map<ClassLoader, ClassPath> loaderClassPaths = new WeakHashMap<>();
    private final Lock lock = new ReentrantLock();
    private final ClassPool classPool;

    public JavassistUtils(final ClassPool pool) {
        classPool = Preconditions.checkNotNull(pool);
    }

    public Lock getLock() {
        return lock;
    }

    public void method(final CtClass it, final Class<? extends Object> returnType, final String name,
            final Class<? extends Object> parameter, final MethodGenerator function1) throws CannotCompileException {
        List<CtClass> _asList = Arrays.<CtClass> asList(asCtClass(parameter));
        CtMethod _ctMethod = new CtMethod(asCtClass(returnType), name, ((CtClass[]) Conversions.unwrapArray(
                _asList, CtClass.class)), it);

        final CtMethod method = _ctMethod;
        function1.process(method);
        it.addMethod(method);
    }

    public void method(final CtClass it, final Class<? extends Object> returnType, final String name,
            final Collection<? extends Class<?>> parameters, final MethodGenerator function1) throws CannotCompileException {
        List<CtClass> _asList = new ArrayList<>();
        for (Class<? extends Object> parameter : parameters) {
            _asList.add(asCtClass(parameter));
        }
        CtMethod method = new CtMethod(asCtClass(returnType), name, ((CtClass[]) Conversions.unwrapArray(_asList,
                CtClass.class)), it);
        function1.process(method);
        it.addMethod(method);
    }

    public void staticMethod(final CtClass it, final Class<? extends Object> returnType, final String name,
            final Class<? extends Object> parameter, final MethodGenerator function1) throws CannotCompileException {
        List<CtClass> _asList = Arrays.<CtClass> asList(asCtClass(parameter));
        CtMethod _ctMethod = new CtMethod(asCtClass(returnType), name, ((CtClass[]) Conversions.unwrapArray(
                _asList, CtClass.class)), it);
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
        final CtField field = new CtField(asCtClass(returnValue), name, it);
        field.setModifiers(Modifier.PUBLIC + Modifier.STATIC);
        it.addField(field);
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
        // FIXME: this works as long as the ClassPool is not shared between instances of this class
        //        How is synchronization across multiple instances done? The ClassPool itself just
        //        keeps on adding the loaders and does not check for duplicates!
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
