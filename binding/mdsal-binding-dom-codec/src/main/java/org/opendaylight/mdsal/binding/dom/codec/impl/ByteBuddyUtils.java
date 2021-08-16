/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.bytebuddy.description.field.FieldDescription.ForLoadedField;
import net.bytebuddy.description.method.MethodDescription.ForLoadedMethod;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.FieldAccess.Defined;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.matcher.ElementMatchers;

final class ByteBuddyUtils {
    private ByteBuddyUtils() {
        // Hidden on purpose
    }

    static StackManipulation invokeMethod(final Method method) {
        return MethodInvocation.invoke(describe(method));
    }

    static StackManipulation invokeMethod(final Class<?> clazz, final String name, final Class<?>... args) {
        return MethodInvocation.invoke(describe(clazz, name, args));
    }

    static StackManipulation getField(final Field field) {
        return FieldAccess.forField(new ForLoadedField(field).asDefined()).read();
    }

    static StackManipulation getField(final TypeDescription instrumentedType, final String fieldName) {
        return fieldAccess(instrumentedType, fieldName).read();
    }

    static StackManipulation putField(final TypeDescription instrumentedType, final String fieldName) {
        return fieldAccess(instrumentedType, fieldName).write();
    }

    private static ForLoadedMethod describe(final Method method) {
        return new ForLoadedMethod(method);
    }

    private static ForLoadedMethod describe(final Class<?> clazz, final String name, final Class<?>... args) {
        return describe(getMethod(clazz, name, args));
    }

    private static Defined fieldAccess(final TypeDescription instrumentedType, final String fieldName) {
        return FieldAccess.forField(instrumentedType.getDeclaredFields().filter(ElementMatchers.named(fieldName))
            .getOnly());
    }

    private static Method getMethod(final Class<?> clazz, final String name, final Class<?>... args) {
        try {
            return clazz.getDeclaredMethod(name, args);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
}
