/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import com.google.common.collect.ImmutableList;
import java.io.Externalizable;
import java.io.ObjectStreamException;
import java.util.List;
import org.opendaylight.yangtools.util.AbstractFixedImmutablePath;
import org.opendaylight.yangtools.util.HashCodeBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class FixedYangInstanceIdentifier extends AbstractFixedImmutablePath<YangInstanceIdentifier, PathArgument>
        implements YangInstanceIdentifier {

    static final FixedYangInstanceIdentifier EMPTY_INSTANCE = new FixedYangInstanceIdentifier(ImmutableList.of(),
            new HashCodeBuilder<>().build());
    private static final long serialVersionUID = 1L;

    private FixedYangInstanceIdentifier(final ImmutableList<PathArgument> path, final int hash) {
        super(hash, path);
    }

    static FixedYangInstanceIdentifier create(final Iterable<? extends PathArgument> path, final int hash) {
        return new FixedYangInstanceIdentifier(ImmutableList.copyOf(path), hash);
    }

    @Override
    public FixedYangInstanceIdentifier toOptimized() {
        return this;
    }

    @Override
    public Externalizable toExternalizable() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected YangInstanceIdentifier createPath(final List<PathArgument> fromRoot) {
        return YangInstanceIdentifier.create(fromRoot);
    }

    @Override
    protected String computeToString(final List<PathArgument> pathFromRoot) {
        return AbstractPathArgument.computeToString(getPathFromRoot());
    }

    @Override
    protected YangInstanceIdentifier emptyPath() {
        return EMPTY_INSTANCE;
    }

    @Override
    protected YangInstanceIdentifier thisPath() {
        return this;
    }

    private Object readResolve() throws ObjectStreamException {
        return isEmpty() ? EMPTY_INSTANCE : this;
    }
}
