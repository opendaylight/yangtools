/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

@NonNullByDefault
final class ModificationPath implements Mutable {
    private static final int DEFAULT_ALLOC_SIZE = 8;
    private static final int ALLOC_SIZE = Integer.getInteger(
        "org.opendaylight.yangtools.yang.data.impl.schema.tree.ModificationPath.ALLOC_SIZE", DEFAULT_ALLOC_SIZE);

    private final YangInstanceIdentifier root;

    private PathArgument[] entries = new PathArgument[ALLOC_SIZE];
    private int used;

    ModificationPath(final YangInstanceIdentifier root) {
        this.root = requireNonNull(root);
    }

    void push(final PathArgument arg) {
        if (entries.length == used) {
            final int grow = used <= 32 ? used : used / 2;
            entries = Arrays.copyOf(entries, used + grow);
        }
        entries[used++] = requireNonNull(arg);
    }

    void pop() {
        checkState(used > 0, "No elements left");
        used--;
    }

    YangInstanceIdentifier toInstanceIdentifier() {
        return YangInstanceIdentifier.builder(root).append(Arrays.asList(entries).subList(0, used)).build();
    }

    @Override
    public String toString() {
        return toInstanceIdentifier().toString();
    }
}
