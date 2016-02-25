/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for instantiating value-type generated objects with String being the base type. Unlike the normal
 * constructor, instances of this class bypass string validation.
 *
 * THE USE OF THIS CLASS IS DANGEROUS AND SHOULD ONLY BE USED TO IMPLEMENT WELL-AUDITED AND CORRECT UTILITY METHODS
 * SHIPPED WITH MODELS TO PROVIDE INSTANTIATION FROM TYPES DIFFERENT THAN STRING.
 *
 * APPLICATION CODE <em>MUST NOT</em> USE THIS CLASS DIRECTLY. VIOLATING THIS CONSTRAINT HAS SECURITY AND CORRECTNESS
 * IMPLICATIONS ON EVERY USER INTERACTING WITH THE RESULTING OBJECTS.
 *
 * @param <T> Resulting object type
 */
@Beta
public final class StringValueObjectFactory<T> {
    private static final MethodType CONSTRUCTOR_METHOD_TYPE = MethodType.methodType(Object.class, Object.class);
    private static final MethodType SETTER_METHOD_TYPE = MethodType.methodType(void.class, Object.class, String.class);
    private static final Logger LOG = LoggerFactory.getLogger(StringValueObjectFactory.class);
    private static final Lookup LOOKUP = MethodHandles.lookup();

    private final MethodHandle constructor;
    private final MethodHandle setter;
    private final T template;

    private StringValueObjectFactory(final T template, final MethodHandle constructor, final MethodHandle setter) {
        this.template = Preconditions.checkNotNull(template);
        this.constructor = constructor.bindTo(template);
        this.setter = Preconditions.checkNotNull(setter);
    }

    public static <T> StringValueObjectFactory<T> create(final Class<T> clazz, final String templateString) {
        final Constructor<T> stringConstructor;
        try {
            stringConstructor = clazz.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("%s does not have a String constructor", clazz), e);
        }

        final T template;
        try {
            template = stringConstructor.newInstance(templateString);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Failed to instantiate template %s for '%s'", clazz,
                templateString), e);
        }

        final Constructor<T> copyConstructor;
        try {
            copyConstructor = clazz.getConstructor(clazz);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("%s does not have a copy constructor", clazz), e);
        }

        final Field f;
        try {
            f = clazz.getDeclaredField("_value");
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(String.format("%s does not have required internal field", clazz), e);
        }
        f.setAccessible(true);

        final StringValueObjectFactory<T> ret;
        try {
            ret = new StringValueObjectFactory<>(template,
                    LOOKUP.unreflectConstructor(copyConstructor).asType(CONSTRUCTOR_METHOD_TYPE),
                    LOOKUP.unreflectSetter(f).asType(SETTER_METHOD_TYPE));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to instantiate method handles", e);
        }

        // Let us be very defensive and scream loudly if the invocation does not come from the same package. This
        // is far from perfect, but better than nothing.
        final Throwable t = new Throwable("Invocation stack");
        t.fillInStackTrace();
        if (matchesPackage(clazz.getPackage().getName(), t.getStackTrace())) {
            LOG.info("Instantiated factory for {}", clazz);
        } else {
            LOG.warn("Instantiated factory for {} outside its package", clazz, t);
        }

        return ret;
    }

    private static boolean matchesPackage(final String pkg, final StackTraceElement[] stackTrace) {
        for (StackTraceElement e : stackTrace) {
            final String sp = e.getClassName();
            if (sp.startsWith(pkg) && sp.lastIndexOf('.') == pkg.length()) {
                return true;
            }
        }

        return false;
    }

    public T newInstance(final String string) {
        Preconditions.checkNotNull(string, "Argument may not be null");

        try {
            final T ret = (T) constructor.invokeExact();
            setter.invokeExact(ret, string);
            LOG.trace("Instantiated new object {} value {}", ret.getClass(), string);
            return ret;
        } catch (Throwable e) {
            throw Throwables.propagate(e);
        }
    }

    public T getTemplate() {
        return template;
    }
}
