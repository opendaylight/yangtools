/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.lang.reflect.Modifier;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base implementation of {@link DerivedStringSupport}. This class should be used as superclass to all implementations
 * of {@link DerivedStringSupport}, as doing so provides a simpler base and enforces some aspects of the subclass.
 *
 * @param <T> derived string type
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
@ThreadSafe
public abstract class AbstractDerivedStringSupport<T extends DerivedString<T>> implements DerivedStringSupport<T> {
    private static final ClassValue<Boolean> VALIDATED_INSTANCES = new ClassValue<Boolean>() {
        @Override
        protected Boolean computeValue(final @Nullable Class<?> type) {
            // Every DerivedStringSupport representation class must:
            checkArgument(DerivedStringSupport.class.isAssignableFrom(type), "%s is not a DerivedStringSupport", type);

            // be final
            final int modifiers = type.getModifiers();
            checkArgument(Modifier.isFinal(modifiers), "%s must be final", type);

            return Boolean.TRUE;
        }
    };

    private final Class<T> representationClass;

    protected AbstractDerivedStringSupport(final Class<T> representationClass) {
        this.representationClass = DerivedString.validateRepresentationClass(representationClass);
        VALIDATED_INSTANCES.get(getClass());
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
    public final T validateRepresentation(final T value) {
        return requireNonNull(value);
    }

    @Override
    public final T validateRepresentation(final T value, final String canonicalString) {
        return requireNonNull(value);
    }
}
