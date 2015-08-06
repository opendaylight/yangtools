/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.ObjectStreamException;
import java.util.List;
import org.opendaylight.yangtools.util.HashCodeBuilder;

final class FixedYangInstanceIdentifier extends YangInstanceIdentifier {
    static final FixedYangInstanceIdentifier EMPTY_INSTANCE = new FixedYangInstanceIdentifier(ImmutableList.<PathArgument>of(), new HashCodeBuilder<>().build());
    private static final long serialVersionUID = 1L;
    private final ImmutableList<PathArgument> path;
    private transient volatile YangInstanceIdentifier parent;

    private FixedYangInstanceIdentifier(final ImmutableList<PathArgument> path, final int hash) {
        super(hash);
        this.path = Preconditions.checkNotNull(path, "path must not be null.");
    }

    static FixedYangInstanceIdentifier create(final Iterable<? extends PathArgument> path, final int hash) {
        return new FixedYangInstanceIdentifier(ImmutableList.copyOf(path), hash);
    }

    @Override
    public boolean isEmpty() {
        return path.isEmpty();
    }

    @Override
    public YangInstanceIdentifier getParent() {
        if (path.isEmpty()) {
            return null;
        }

        YangInstanceIdentifier ret = parent;
        if (ret == null) {
            ret = YangInstanceIdentifier.create(path.subList(0, path.size() - 1));
            parent = ret;
        }

        return ret;
    }

    @Override
    public List<PathArgument> getPathArguments() {
        return path;
    }

    @Override
    public List<PathArgument> getReversePathArguments() {
        return path.reverse();
    }

    @Override
    List<PathArgument> tryPathArguments() {
        return path;
    }

    @Override
    List<PathArgument> tryReversePathArguments() {
        return path.reverse();
    }

    @Override
    public PathArgument getLastPathArgument() {
        return path.isEmpty()? null : path.get(path.size() - 1);
    }

    @Override
    YangInstanceIdentifier createRelativeIdentifier(final int skipFromRoot) {
        if (skipFromRoot == path.size()) {
            return EMPTY_INSTANCE;
        }

        final ImmutableList<PathArgument> newPath = path.subList(skipFromRoot, path.size());
        final HashCodeBuilder<PathArgument> hash = new HashCodeBuilder<>();
        for (PathArgument a : newPath) {
            hash.addArgument(a);
        }

        return new FixedYangInstanceIdentifier(newPath, hash.build());
    }

    private Object readResolve() throws ObjectStreamException {
        return path.isEmpty() ? EMPTY_INSTANCE : this;
    }

    @Override
    boolean pathArgumentsEqual(final YangInstanceIdentifier other) {
        if (other instanceof FixedYangInstanceIdentifier) {
            return path.equals(((FixedYangInstanceIdentifier) other).path);
        } else {
            return super.pathArgumentsEqual(other);
        }
    }

    @Override
    public YangInstanceIdentifier toOptimized() {
        return this;
    }
}
