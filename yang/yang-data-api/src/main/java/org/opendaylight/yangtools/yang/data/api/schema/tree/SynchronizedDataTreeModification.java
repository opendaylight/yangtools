/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A {@link DataTreeModification} implementation which delegates all calls to
 * another instance, making sure no method is being invoked from multiple threads
 * concurrently.
 */
public final class SynchronizedDataTreeModification implements DataTreeModification {
    private final DataTreeModification delegate;

    private SynchronizedDataTreeModification(final DataTreeModification delegate) {
        this.delegate = requireNonNull(delegate);
    }

    public static DataTreeModification create(final DataTreeModification delegate) {
        return new SynchronizedDataTreeModification(delegate);
    }

    @Override
    public synchronized Optional<NormalizedNode<?, ?>> readNode(final YangInstanceIdentifier path) {
        return delegate.readNode(path);
    }

    @Override
    public synchronized DataTreeModification newModification() {
        return delegate.newModification();
    }

    @Override
    public synchronized void delete(final YangInstanceIdentifier path) {
        delegate.delete(path);
    }

    @Override
    public synchronized void merge(final YangInstanceIdentifier path, final NormalizedNode<?, ?> data) {
        delegate.merge(path, data);
    }

    @Override
    public synchronized void write(final YangInstanceIdentifier path, final NormalizedNode<?, ?> data) {
        delegate.write(path, data);
    }

    @Override
    public synchronized void ready() {
        delegate.ready();
    }

    @Override
    public synchronized void applyToCursor(@Nonnull final DataTreeModificationCursor cursor) {
        delegate.applyToCursor(cursor);
    }
}
