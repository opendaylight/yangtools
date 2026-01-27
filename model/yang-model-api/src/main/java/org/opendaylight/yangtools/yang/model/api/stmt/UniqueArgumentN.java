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
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

/**
 * A {@link Descendant} containing three or more {@link Descendant}s.
 */
@NonNullByDefault
record UniqueArgumentN(ImmutableSet<Descendant> asSet) implements UniqueArgument.OfMore {
    UniqueArgumentN {
        requireNonNull(asSet);
    }

    @Override
    public Iterator<Descendant> iterator() {
        return asSet.iterator();
    }

    @Override
    public Stream<Descendant> stream() {
        return asSet.stream();
    }

    @Override
    public int size() {
        return asSet.size();
    }

    @Override
    public boolean contains(final Descendant descendant) {
        return asSet.contains(requireNonNull(descendant));
    }

    @Override
    public List<Descendant> asList() {
        return asSet.asList();
    }

    @Override
    public String toString() {
        return asSet.toString();
    }
}
