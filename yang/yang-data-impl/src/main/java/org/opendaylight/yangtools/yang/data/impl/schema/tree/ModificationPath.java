/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

@NonNullByDefault
final class ModificationPath extends ArrayDeque<PathArgument> implements Mutable {
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_ALLOC_SIZE = 8;
    private static final int ALLOC_SIZE = Integer.getInteger(
        "org.opendaylight.yangtools.yang.data.impl.schema.tree.ModificationPath.ALLOC_SIZE", DEFAULT_ALLOC_SIZE);

    private final YangInstanceIdentifier root;

    ModificationPath(final YangInstanceIdentifier root) {
        super(ALLOC_SIZE);
        this.root = requireNonNull(root);
    }

    YangInstanceIdentifier toInstanceIdentifier() {
        return YangInstanceIdentifier.builder(root).append(this).build();
    }
}
