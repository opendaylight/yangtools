/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.io.ObjectStreamException;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.util.HashCodeBuilder;

final class FixedYangInstanceIdentifier extends YangInstanceIdentifier implements Cloneable {
    static final FixedYangInstanceIdentifier EMPTY_INSTANCE = new FixedYangInstanceIdentifier(ImmutableList.of(),
            new HashCodeBuilder<>().build());
    private static final long serialVersionUID = 1L;

    private final ImmutableList<PathArgument> path;
    private transient volatile YangInstanceIdentifier parent;

    private FixedYangInstanceIdentifier(final ImmutableList<PathArgument> path, final int hash) {
        super(hash);
        this.path = requireNonNull(path, "path must not be null.");
    }

    static FixedYangInstanceIdentifier create(final Iterable<? extends PathArgument> path, final int hash) {
        return new FixedYangInstanceIdentifier(ImmutableList.copyOf(path), hash);
    }

    @Override
    public boolean isEmpty() {
        return path.isEmpty();
    }

    @Override
    public FixedYangInstanceIdentifier clone() {
        try {
            return (FixedYangInstanceIdentifier) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("clone() should be supported", e);
        }
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

    @Nonnull
    @Override
    public YangInstanceIdentifier getAncestor(final int depth) {
        checkArgument(depth >= 0, "Negative depth is not allowed");
        checkArgument(depth <= path.size(), "Depth %s exceeds maximum depth %s", depth, path.size());

        if (depth == path.size()) {
            return this;
        }
        if (depth == path.size() - 1) {
            // Use the parent cache
            return getParent();
        }
        return YangInstanceIdentifier.create(path.subList(0, depth));
    }

    @Override
    public List<PathArgument> getPathArguments() {
        return path;
    }

    @Override
    public List<PathArgument> getReversePathArguments() {
        return path.reverse();
    }

    @Nonnull
    @Override
    List<PathArgument> tryPathArguments() {
        return path;
    }

    @Nonnull
    @Override
    List<PathArgument> tryReversePathArguments() {
        return path.reverse();
    }

    @Override
    public PathArgument getLastPathArgument() {
        return path.isEmpty() ? null : path.get(path.size() - 1);
    }

    @Nonnull
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
        }
        return super.pathArgumentsEqual(other);
    }

    @Override
    public FixedYangInstanceIdentifier toOptimized() {
        return this;
    }
}
