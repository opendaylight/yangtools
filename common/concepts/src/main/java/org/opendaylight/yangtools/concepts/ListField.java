/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.annotations.Beta;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Modifier;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A Java field containing zero, one or more values.
 *
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public sealed interface ListField<R> {

    sealed interface Readable<R, V> extends ListField<R> permits ReadWrite, PlainGetListField {

        Object initial(List<V> values);

        List<V> readList(R receiver);
    }

    sealed interface Writable<R, V> extends ListField<R> {

        void writeList(R receiver, List<V> values);
    }

    sealed interface ReadWrite<R, V> extends Readable<R, V>, Writable<R, V> permits PlainListField {
        // just a composition
    }

    static <R, V> Readable<R, V> ofPlainGet(final Lookup lookup, final Class<R> receiver, final String fieldName,
            final Class< V> value) throws NoSuchFieldException, IllegalAccessException {
        if (value.isAssignableFrom(List.class)) {
            throw new IllegalArgumentException(value + " represents a List");
        }
        if (!Modifier.isFinal(value.getModifiers()) && !value.isSealed()) {
            throw new IllegalArgumentException(value + " is not final nor not sealed");
        }
        return new PlainGetListField<>(receiver, value, lookup.findVarHandle(receiver, fieldName, Object.class));
    }
}
