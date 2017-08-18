/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

final class StackedYangInstanceIdentifier extends YangInstanceIdentifier implements Cloneable {
    private static final long serialVersionUID = 1L;
    private static final Field PARENT_FIELD;

    static {
        final Field f;
        try {
            f = StackedYangInstanceIdentifier.class.getDeclaredField("parent");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new ExceptionInInitializerError(e);
        }
        f.setAccessible(true);

        PARENT_FIELD = f;
    }

    private final YangInstanceIdentifier parent;
    private final PathArgument pathArgument;

    private transient volatile StackedPathArguments pathArguments;
    private transient volatile StackedReversePathArguments reversePathArguments;

    StackedYangInstanceIdentifier(final YangInstanceIdentifier parent, final PathArgument pathArgument,
            final int hash) {
        super(hash);
        this.parent = requireNonNull(parent);
        this.pathArgument = requireNonNull(pathArgument);
    }

    @Override
    public StackedYangInstanceIdentifier clone() {
        try {
            return (StackedYangInstanceIdentifier) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("clone() should be supported", e);
        }
    }

    @Override
    public YangInstanceIdentifier getParent() {
        return parent;
    }

    @Nonnull
    @Override
    public YangInstanceIdentifier getAncestor(final int depth) {
        checkArgument(depth >= 0, "Steps cannot be negative");

        // Calculate how far up our FixedYangInstanceIdentifier ancestor is
        int stackedDepth = 1;
        YangInstanceIdentifier wlk = getParent();
        while (wlk instanceof StackedYangInstanceIdentifier) {
            wlk = wlk.getParent();
            stackedDepth++;
        }

        // Guaranteed to come from FixedYangInstanceIdentifier
        final int fixedDepth = wlk.getPathArguments().size();
        if (fixedDepth >= depth) {
            return wlk.getAncestor(depth);
        }

        // Calculate our depth and check argument
        final int ourDepth = stackedDepth + fixedDepth;
        checkArgument(depth <= ourDepth, "Depth %s exceeds maximum depth %s", depth, ourDepth);

        // Requested depth is covered by the stack, traverse up for specified number of steps
        final int toWalk = ourDepth - depth;
        YangInstanceIdentifier result = this;
        for (int i = 0; i < toWalk; ++i) {
            result = result.getParent();
        }

        return result;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public List<PathArgument> getPathArguments() {
        StackedPathArguments ret = tryPathArguments();
        if (ret == null) {
            final List<PathArgument> stack = new ArrayList<>();
            YangInstanceIdentifier current = this;
            do {
                verify(current instanceof StackedYangInstanceIdentifier);
                final StackedYangInstanceIdentifier stacked = (StackedYangInstanceIdentifier) current;
                stack.add(stacked.getLastPathArgument());
                current = stacked.getParent();
            } while (current.tryPathArguments() == null);

            ret = new StackedPathArguments(current, Lists.reverse(stack));
            pathArguments = ret;
        }

        return ret;
    }

    @Override
    public List<PathArgument> getReversePathArguments() {
        StackedReversePathArguments ret = tryReversePathArguments();
        if (ret == null) {
            ret = new StackedReversePathArguments(this);
            reversePathArguments = ret;
        }
        return ret;
    }

    @Override
    public PathArgument getLastPathArgument() {
        return pathArgument;
    }

    @Nonnull
    @Override
    StackedPathArguments tryPathArguments() {
        return pathArguments;
    }

    @Nonnull
    @Override
    StackedReversePathArguments tryReversePathArguments() {
        return reversePathArguments;
    }

    @Nonnull
    @Override
    YangInstanceIdentifier createRelativeIdentifier(final int skipFromRoot) {
        // TODO: can we optimize this one?
        return YangInstanceIdentifier.create(Iterables.skip(getPathArguments(), skipFromRoot));
    }

    @Override
    boolean pathArgumentsEqual(final YangInstanceIdentifier other) {
        if (other instanceof StackedYangInstanceIdentifier) {
            final StackedYangInstanceIdentifier stacked = (StackedYangInstanceIdentifier) other;
            return pathArgument.equals(stacked.pathArgument) && parent.equals(stacked.parent);
        }
        return super.pathArgumentsEqual(other);
    }

    private void readObject(final ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();

        final FixedYangInstanceIdentifier p = (FixedYangInstanceIdentifier) inputStream.readObject();
        try {
            PARENT_FIELD.set(this, p);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IOException("Failed to set parent", e);
        }
    }

    private void writeObject(final ObjectOutputStream outputStream) throws IOException {
        outputStream.defaultWriteObject();

        final FixedYangInstanceIdentifier p;
        if (parent instanceof FixedYangInstanceIdentifier) {
            p = (FixedYangInstanceIdentifier) parent;
        } else {
            p = FixedYangInstanceIdentifier.create(parent.getPathArguments(), parent.hashCode());
        }
        outputStream.writeObject(p);
    }

    @Override
    public YangInstanceIdentifier toOptimized() {
        return FixedYangInstanceIdentifier.create(getPathArguments());
    }
}
