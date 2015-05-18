/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

final class StackedYangInstanceIdentifier extends YangInstanceIdentifier {
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

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<StackedYangInstanceIdentifier, ImmutableList> LEGACYPATH_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(StackedYangInstanceIdentifier.class, ImmutableList.class, "legacyPath");

    private final YangInstanceIdentifier parent;
    private final PathArgument pathArgument;

    private transient volatile ImmutableList<PathArgument> legacyPath;
    private transient volatile StackedPathArguments pathArguments;
    private transient volatile StackedReversePathArguments reversePathArguments;

    StackedYangInstanceIdentifier(final YangInstanceIdentifier parent, final PathArgument pathArgument, final int hash) {
        super(hash);
        this.parent = Preconditions.checkNotNull(parent);
        this.pathArgument = Preconditions.checkNotNull(pathArgument);
    }

    @Override
    public YangInstanceIdentifier getParent() {
        return parent;
    }

    @Override
    public List<PathArgument> getPath() {
        // Temporary variable saves a volatile read
        ImmutableList<PathArgument> ret = legacyPath;
        if (ret == null) {
            // We could have used a synchronized block, but the window is quite
            // small and worst that can happen is duplicate object construction.
            ret = ImmutableList.copyOf(getPathArguments());
            LEGACYPATH_UPDATER.lazySet(this, ret);
        }

        return ret;
    }

    @Override
    public Collection<PathArgument> getPathArguments() {
        StackedPathArguments ret = tryPathArguments();
        if (ret == null) {
            List<PathArgument> stack = new ArrayList<>();
            YangInstanceIdentifier current = this;
            while (current.tryPathArguments() == null) {
                Verify.verify(current instanceof StackedYangInstanceIdentifier);

                final StackedYangInstanceIdentifier stacked = (StackedYangInstanceIdentifier) current;
                stack.add(stacked.getLastPathArgument());
                current = stacked.getParent();
            }

            ret = new StackedPathArguments(current, Lists.reverse(stack));
            pathArguments = ret;
        }

        return ret;
    }

    @Override
    public Collection<PathArgument> getReversePathArguments() {
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

    @Override
    StackedPathArguments tryPathArguments() {
        return pathArguments;
    }

    @Override
    StackedReversePathArguments tryReversePathArguments() {
        return reversePathArguments;
    }

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
        } else {
            return super.pathArgumentsEqual(other);
        }
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
}
