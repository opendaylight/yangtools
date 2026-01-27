/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

/**
 * A {@link KeyArgument} containing a single {@link Descendant}.
 */
@NonNullByDefault
record UniqueArgument1(Descendant item) implements UniqueArgument.OfOne {
    UniqueArgument1 {
        requireNonNull(item);
    }

    @Override
    public boolean contains(final Descendant descendant) {
        return descendant.equals(item);
    }

    @Override
    public String toString() {
        return "[" + item + "]";
    }
}
