/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * A {@link KeyArgument} containing two {@code node-identifier}s.
 */
@NonNullByDefault
record KeyArgument2(List<QName> asList) implements KeyArgument.OfMore {
    KeyArgument2 {
        requireNonNull(asList);
    }

    @Override
    public int size() {
        return asList.size();
    }

    @Override
    public boolean contains(final QName nodeIdentifier) {
        return asList.contains(requireNonNull(nodeIdentifier));
    }

    @Override
    public Set<QName> asSet() {
        return Set.copyOf(asList);
    }

    @Override
    public Iterator<QName> iterator() {
        return asList.iterator();
    }

    @Override
    public String toString() {
        return asList.toString();
    }
}
