/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.VarHandle;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link ListField.ReadWrite} using {@link VarHandle#get(Object...)} and {@link VarHandle#set(Object...)}.
 */
@NonNullByDefault
record PlainListField<R, V>(Class<R> recv, Class<V> val, VarHandle vh) implements ListField.ReadWrite<R, V> {
    PlainListField {
        requireNonNull(recv);
        requireNonNull(val);
        requireNonNull(vh);
    }

    @Override
    public Object initial(final List<V> values) {
        return ListFieldSupport.maskList(val, values);
    }

    @Override
    public List<V> readList(final R receiver) {
        return ListFieldSupport.unmaskList(val, vh.get(requireNonNull(receiver)));
    }

    @Override
    public void writeList(final R receiver, final List<V> values) {
        vh.set(requireNonNull(receiver), initial(values));
    }
}
