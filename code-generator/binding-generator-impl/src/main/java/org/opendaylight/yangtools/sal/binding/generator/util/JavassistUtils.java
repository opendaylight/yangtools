/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.util;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.ClassPool;

import java.util.Arrays;

import javassist.ClassPath;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.LoaderClassPath;
import javassist.ClassClassPath;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Collections;

public class JavassistUtils {

    private static final Logger LOG = LoggerFactory.getLogger(JavassistUtils.class);

    private final Map<ClassLoader, LoaderClassPath> loaderClassPaths = Collections
            .synchronizedMap(new WeakHashMap<ClassLoader, LoaderClassPath>());
    private ClassPool classPool;
    private final Lock lock = new ReentrantLock();

    public JavassistUtils(ClassPool pool) {
        classPool = pool;
    }

    public Lock getLock() {
        return lock;
    }

    public void method(final CtClass it, final Class<? extends Object> returnType, final String name,
            final Class<? extends Object> parameter, final MethodGenerator function1) {
        try {
            List<CtClass> _asList = Arrays.<CtClass> asList(asCtClass(parameter));
            CtMethod _ctMethod = new CtMethod(asCtClass(returnType), name, ((CtClass[]) Conversions.unwrapArray(
                    _asList, CtClass.class)), it);
            final CtMethod method = _ctMethod;
            function1.process(method);
            it.addMethod(method);
        } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
        }
    }

    public void method(final CtClass it, final Class<? extends Object> returnType, final String name,
            final Class<? extends Object> parameter1, final Class<? extends Object> parameter2,
            final MethodGenerator function1) {
        try {
            List<CtClass> _asList = Arrays.<CtClass> asList(asCtClass(parameter1), asCtClass(parameter2));
            CtMethod method = new CtMethod(asCtClass(returnType), name, ((CtClass[]) Conversions.unwrapArray(_asList,
                    CtClass.class)), it);
            function1.process(method);
            it.addMethod(method);
        } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
        }
    }

    public void staticMethod(final CtClass it, final Class<? extends Object> returnType, final String name,
            final Class<? extends Object> parameter, final MethodGenerator function1) {
        try {
            List<CtClass> _asList = Arrays.<CtClass> asList(asCtClass(parameter));
            CtMethod _ctMethod = new CtMethod(asCtClass(returnType), name, ((CtClass[]) Conversions.unwrapArray(
                    _asList, CtClass.class)), it);
            final CtMethod method = _ctMethod;
            function1.process(method);
            it.addMethod(method);
        } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
        }
    }

    public void implementMethodsFrom(CtClass target, CtClass source, MethodGenerator function1) {
        try {
            for (CtMethod method : source.getMethods()) {
                if (method.getDeclaringClass() == source) {
                    CtMethod redeclaredMethod = new CtMethod(method, target, null);
                    function1.process(redeclaredMethod);
                    target.addMethod(redeclaredMethod);
                }
            }
        } catch (Throwable t) {
            Exceptions.sneakyThrow(t);
        }
    }

    public CtClass createClass(String fqn, ClassGenerator cls) {
        CtClass target = classPool.makeClass(fqn);
        cls.process(target);
        return target;
    }

    public CtClass createClass(String fqn, CtClass superInterface, ClassGenerator cls) {
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

    public CtField field(final CtClass it, final String name, final Class<? extends Object> returnValue) {
        try {
            final CtField field = new CtField(asCtClass(returnValue), name, it);
            field.setModifiers(Modifier.PUBLIC);
            it.addField(field);
            return field;
        } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
        }
    }

    public CtField staticField(final CtClass it, final String name, final Class<? extends Object> returnValue) {
        try {
            final CtField field = new CtField(asCtClass(returnValue), name, it);
            field.setModifiers(Modifier.PUBLIC + Modifier.STATIC);
            it.addField(field);
            return field;
        } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
        }
    }

    public CtClass get(final ClassPool pool, final Class<? extends Object> cls) {
        try {
            return pool.get(cls.getName());
        } catch (NotFoundException nfe1) {
            appendClassLoaderIfMissing(cls.getClassLoader());
            try {
                return pool.get(cls.getName());
            } catch (final NotFoundException nfe2) {
                LOG.warn("Appending ClassClassPath for {}", cls);
                pool.appendClassPath(new ClassClassPath(cls));
                try {
                    return pool.get(cls.getName());
                } catch (Throwable t) {
                    throw Exceptions.sneakyThrow(t);
                }
            } catch (Throwable t1) {
                Exceptions.sneakyThrow(t1);
                return null;
            }
        } catch (Throwable t) {
            Exceptions.sneakyThrow(t);
            return null;
        }
    }

    public void appendClassLoaderIfMissing(ClassLoader loader) {
        if (loaderClassPaths.containsKey(loader)) {
            return;
        }
        ClassPath ctLoader = new LoaderClassPath(loader);
        classPool.appendClassPath(ctLoader);
    }

    public void ensureClassLoader(Class<?> child) {
        appendClassLoaderIfMissing(child.getClassLoader());
    }
}
