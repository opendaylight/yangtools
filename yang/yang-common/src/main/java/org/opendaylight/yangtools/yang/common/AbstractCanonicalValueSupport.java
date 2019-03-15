/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import java.lang.reflect.Modifier;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Variant;

/**
 * Base implementation of {@link CanonicalValueSupport}. This class should be used as superclass to all implementations
 * of {@link CanonicalValueSupport}, as doing so provides a simpler base and enforces some aspects of the subclass.
 *
 * @param <T> canonical value type
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public abstract class AbstractCanonicalValueSupport<T extends CanonicalValue<T>> implements CanonicalValueSupport<T> {
    private static final ClassValue<Boolean> SUPPORTS = new ClassValue<Boolean>() {
        @Override
        protected Boolean computeValue(final @Nullable Class<?> type) {
            // Every DerivedStringSupport representation class must:
            checkArgument(CanonicalValueSupport.class.isAssignableFrom(type), "%s is not a CanonicalValueSupport",
                type);

            // be final
            final int modifiers = type.getModifiers();
            checkArgument(Modifier.isFinal(modifiers), "%s must be final", type);

            return Boolean.TRUE;
        }
    };
    private static final ClassValue<Boolean> VALUES = new AbstractCanonicalValueImplementationValidator() {
        @Override
        void checkCompareTo(@NonNull final Class<?> type) {
            checkFinalMethod(type, "compareTo", type);
        }
    };

    private final Class<T> representationClass;

    protected AbstractCanonicalValueSupport(final Class<T> representationClass) {
        VALUES.get(representationClass);
        this.representationClass = representationClass;
        SUPPORTS.get(getClass());
    }

    @Override
    public final Class<T> getRepresentationClass() {
        return representationClass;
    }

    @Override
    public final Class<T> getValidatedRepresentationClass() {
        return representationClass;
    }

    @Override
    public final Variant<T, CanonicalValueViolation> validateRepresentation(final T value) {
        return Variant.ofFirst(value);
    }

    @Override
    public final Variant<T, CanonicalValueViolation> validateRepresentation(final T value,
            final String canonicalString) {
        return Variant.ofFirst(value);
    }
}
