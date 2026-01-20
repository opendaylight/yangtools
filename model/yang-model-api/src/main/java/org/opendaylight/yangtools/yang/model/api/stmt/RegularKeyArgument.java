/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * A {@link KeyArgument} containing two or more {@code node-identifier}s.
 */
@NonNullByDefault
record RegularKeyArgument(ImmutableSet<QName> asSet) implements KeyArgument.OfMore {
    RegularKeyArgument {
        requireNonNull(asSet);
    }

    @Override
    public int size() {
        return asSet.size();
    }

    @Override
    public List<QName> asList() {
        return asSet.asList();
    }

    @Override
    public Iterator<QName> iterator() {
        return asSet.iterator();
    }

    @Override
    public String toString() {
        return asSet.toString();
    }
}
