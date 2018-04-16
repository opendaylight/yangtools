/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
abstract class AbstractCanonicalValueImplementationValidator extends ClassValue<Boolean> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCanonicalValueImplementationValidator.class);

    @Override
    protected final Boolean computeValue(final @Nullable Class<?> type) {
        // Every DerivedString representation class must:
        checkArgument(CanonicalValue.class.isAssignableFrom(type), "%s is not a DerivedString", type);

        // be non-final and public
        final int modifiers = type.getModifiers();
        checkArgument(Modifier.isPublic(modifiers), "%s must be public", type);
        checkArgument(!Modifier.isFinal(modifiers), "%s must not be final", type);

        // have at least one public or protected constructor (for subclasses)
        checkArgument(Arrays.stream(type.getDeclaredConstructors()).mapToInt(Constructor::getModifiers)
            .anyMatch(mod -> Modifier.isProtected(mod) || Modifier.isPublic(mod)),
            "%s must declare at least one protected or public constructor", type);

        try {
            // have a non-final non-abstract validator() method
            final int validator;
            try {
                validator = type.getMethod("validator").getModifiers();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(type + " must have a non-abstract non-final validator() method",
                    e);
            }
            checkArgument(!Modifier.isFinal(validator), "%s must not have final validator()", type);

            // have final toCanonicalString(), support(), hashCode() and equals(Object), compare(T) methods
            checkFinalMethod(type, "toCanonicalString");
            checkFinalMethod(type, "support");
            checkFinalMethod(type, "hashCode");
            checkFinalMethod(type, "equals", Object.class);
        } catch (SecurityException e) {
            LOG.warn("Cannot completely validate {}", type, e);
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    abstract void checkCompareTo(Class<?> type);

    static void checkFinalMethod(final Class<?> type, final String name) {
        try {
            checkFinalMethod(type.getMethod(name).getModifiers(), type, name, "");
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(type + " must have a final " + name + "() method", e);
        }
    }

    static void checkFinalMethod(final Class<?> type, final String name, final Class<?> arg) {
        final String argName = arg.getSimpleName();
        try {
            checkFinalMethod(type.getMethod(name, arg).getModifiers(), type, name, argName);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(type + " must have a final " + name + "(" + argName + ") method", e);
        }
    }

    private static void checkFinalMethod(final int modifiers, final Class<?> type, final String name,
            final String args) {
        checkArgument(Modifier.isFinal(modifiers), "%s must have a final %s(%s) method", type, name, args);
    }
}