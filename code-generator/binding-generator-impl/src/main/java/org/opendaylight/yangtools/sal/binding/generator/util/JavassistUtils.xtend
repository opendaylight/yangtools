/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.util

import javassist.CtClass
import javassist.CtMethod
import javassist.ClassPool
import java.util.Arrays
import static com.google.common.base.Preconditions.*;
import javassist.CtField
import javassist.Modifier
import javassist.NotFoundException
import javassist.LoaderClassPath
import javassist.ClassClassPath
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import org.slf4j.LoggerFactory
import java.util.WeakHashMap
import java.util.Collections

class JavassistUtils {

    private static val LOG = LoggerFactory.getLogger(JavassistUtils);

    private val loaderClassPaths = Collections.synchronizedMap(new WeakHashMap<ClassLoader,LoaderClassPath>());

    ClassPool classPool

    @Property
    val Lock lock = new ReentrantLock();

    new(ClassPool pool) {
        classPool = pool;
    }

    def void method(CtClass it, Class<?> returnType, String name, Class<?> parameter, MethodGenerator function1) {
        val method = new CtMethod(returnType.asCtClass, name, Arrays.asList(parameter.asCtClass), it);
        function1.process(method);
        it.addMethod(method);
    }

        def void method(CtClass it, Class<?> returnType, String name, Class<?> parameter1, Class<?> parameter2,  MethodGenerator function1) {
        val method = new CtMethod(returnType.asCtClass, name, Arrays.asList(parameter1.asCtClass,parameter2.asCtClass), it);
        function1.process(method);
        it.addMethod(method);
    }


    def void staticMethod(CtClass it, Class<?> returnType, String name, Class<?> parameter, MethodGenerator function1) {
        val method = new CtMethod(returnType.asCtClass, name, Arrays.asList(parameter.asCtClass), it);
        function1.process(method);
        it.addMethod(method);
    }

    def void implementMethodsFrom(CtClass target, CtClass source, MethodGenerator function1) {
        for (method : source.methods) {
            if (method.declaringClass == source) {
                val redeclaredMethod = new CtMethod(method, target, null);
                function1.process(redeclaredMethod);
                target.addMethod(redeclaredMethod);
            }
        }
    }

    def CtClass createClass(String fqn, ClassGenerator cls) {

        val target = classPool.makeClass(fqn);
        cls.process(target);
        return target;
    }

    def CtClass createClass(String fqn, CtClass superInterface, ClassGenerator cls) {

        val target = classPool.makeClass(fqn);
        target.implementsType(superInterface);
        cls.process(target);
        return target;
    }

    def void implementsType(CtClass it, CtClass supertype) {
        checkArgument(supertype.interface, "Supertype must be interface");
        addInterface(supertype);
    }

    def asCtClass(Class<?> class1) {
        classPool.get(class1);
    }

    def CtField field(CtClass it, String name, Class<?> returnValue) {
        val field = new CtField(returnValue.asCtClass, name, it);
        field.modifiers = Modifier.PUBLIC
        addField(field);
        return field;
    }

    def CtField staticField(CtClass it, String name, Class<?> returnValue) {
        val field = new CtField(returnValue.asCtClass, name, it);
        field.modifiers = Modifier.PUBLIC + Modifier.STATIC
        addField(field);
        return field;
    }

    def get(ClassPool pool, Class<?> cls) {
        try {
            return pool.get(cls.name)
        } catch (NotFoundException e) {
            appendClassLoaderIfMissing(cls.classLoader)
            try {
                return pool.get(cls.name)
            } catch (NotFoundException ef) {
                LOG.warn("Appending ClassClassPath for {}",cls);
                pool.appendClassPath(new ClassClassPath(cls));

                return pool.get(cls.name)
            }
        }
    }

    def void appendClassLoaderIfMissing(ClassLoader loader) {
        if(loaderClassPaths.containsKey(loader)) {
            return;
        }
        val ctLoader = new LoaderClassPath(loader);
        classPool.appendClassPath(ctLoader);
    }

    def void ensureClassLoader(Class<?> child) {
        appendClassLoaderIfMissing(child.classLoader);
    }
}
