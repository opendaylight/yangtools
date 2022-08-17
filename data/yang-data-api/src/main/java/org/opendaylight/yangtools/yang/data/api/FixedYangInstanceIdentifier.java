/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.io.Serial;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.HashCodeBuilder;

final class FixedYangInstanceIdentifier extends YangInstanceIdentifier implements Cloneable {
    static final @NonNull FixedYangInstanceIdentifier EMPTY_INSTANCE = new FixedYangInstanceIdentifier(
        ImmutableList.of());
    @Serial
    private static final long serialVersionUID = 1L;

    private final ImmutableList<PathArgument> path;

    private transient volatile YangInstanceIdentifier parent = null;

    FixedYangInstanceIdentifier(final ImmutableList<PathArgument> path) {
        this.path = requireNonNull(path, "path must not be null.");
    }

    static @NonNull FixedYangInstanceIdentifier of(final ImmutableList<PathArgument> path) {
        return path.isEmpty() ? EMPTY_INSTANCE : new FixedYangInstanceIdentifier(path);
    }

    static @NonNull FixedYangInstanceIdentifier of(final List<PathArgument> path) {
        return path.isEmpty() ? EMPTY_INSTANCE : new FixedYangInstanceIdentifier(ImmutableList.copyOf(path));
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

    @Override
    public YangInstanceIdentifier coerceParent() {
        return verifyNotNull(getParent(), "Empty instance identifier does not have a parent");
    }

    @Override
    public YangInstanceIdentifier getAncestor(final int depth) {
        checkArgument(depth >= 0, "Negative depth is not allowed");
        checkArgument(depth <= path.size(), "Depth %s exceeds maximum depth %s", depth, path.size());

        if (depth == path.size()) {
            return this;
        }
        if (depth == path.size() - 1) {
            // Use the parent cache
            return verifyNotNull(getParent());
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

    @Override
    @NonNull List<PathArgument> tryPathArguments() {
        return path;
    }

    @Override
    @NonNull List<PathArgument> tryReversePathArguments() {
        return path.reverse();
    }

    @Override
    public PathArgument getLastPathArgument() {
        return path.isEmpty() ? null : path.get(path.size() - 1);
    }

    @Override
    YangInstanceIdentifier createRelativeIdentifier(final int skipFromRoot) {
        return skipFromRoot == path.size() ? EMPTY_INSTANCE
            : new FixedYangInstanceIdentifier(path.subList(skipFromRoot, path.size()));
    }

    @Override
    int computeHashCode() {
        int ret = 1;
        for (PathArgument arg : path) {
            ret = HashCodeBuilder.nextHashCode(ret, arg);
        }
        return ret;
    }

    @Override
    public FixedYangInstanceIdentifier toOptimized() {
        return this;
    }
}
