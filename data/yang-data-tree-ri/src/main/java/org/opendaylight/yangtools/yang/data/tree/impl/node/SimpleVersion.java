/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.tree.api.CommitMetadata;

/**
 * A simple {@link Version} which does not track commit metadata.
 */
public final class SimpleVersion extends Version implements Immutable {
    @Override
    public SimpleVersion next() {
        return new SimpleVersion();
    }

    @Override
    public void commit() {
        // No-op
    }

    @Override
    public void commit(final CommitMetadata metadata) {
        // No-op
    }

    @Override
    public CommitMetadata commitMetadata() {
        return null;
    }
}