/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.LinearPath;

@Beta
public abstract class AbstractStackedLinearPath<P extends LinearPath<P, C>,
        C extends Comparable<C> & Immutable, F extends AbstractFixedLinearPath<P, C>,
        S extends AbstractStackedLinearPath<P, C, F, S>>
        extends AbstractLinearPath<P, C> {
    private static final long serialVersionUID = 1L;

    private final @NonNull P parent;
    private final @NonNull C component;

    private transient volatile PathComponentsFromRoot<C> fromRoot;
    private transient volatile PathComponentsTowardRoot<P, C, F, S> towardRoot;

    protected AbstractStackedLinearPath(final int hash, final P parent, final C component) {
        super(hash);
        this.parent = requireNonNull(parent);
        this.component = requireNonNull(component);
    }

    @Override
    public final boolean isEmpty() {
        return false;
    }

    @Override
    public final P getParent() {
        return parent;
    }

    @Override
    public final @NonNull List<C> getPathFromRoot() {
        PathComponentsFromRoot<C> ret = tryPathFromRoot();
        if (ret == null) {
            final List<C> stack = new ArrayList<>();
            AbstractLinearPath<?, C> current = this;
            do {
                verify(current instanceof AbstractStackedLinearPath);
                final AbstractStackedLinearPath<?, C, F, S> stacked =
                        (AbstractStackedLinearPath<?, C, F, S>) current;
                stack.add(stacked.getLastComponent());
                final LinearPath<?, C> parent = stacked.getParent();
                verify(parent instanceof AbstractLinearPath);
                current = (AbstractLinearPath<?, C>) parent;
            } while (current.tryPathFromRoot() == null);

            ret = new PathComponentsFromRoot<>(current, Lists.reverse(stack));
            fromRoot = ret;
        }

        return ret;
    }

    @Override
    public final @NonNull PathComponentsTowardRoot<P, C, F, S> getPathTowardsRoot() {
        PathComponentsTowardRoot<P, C, F, S> ret = tryPathTowardsRoot();
        if (ret == null) {
            ret = new PathComponentsTowardRoot<>(this);
            towardRoot = ret;
        }
        return ret;
    }

    @Override
    public final C getLastComponent() {
        return component;
    }

    @Override
    protected final PathComponentsFromRoot<C> tryPathFromRoot() {
        return fromRoot;
    }

    @Override
    protected final PathComponentsTowardRoot<P, C, F, S> tryPathTowardsRoot() {
        return towardRoot;
    }

    @Override
    final boolean equalPathFromRoot(final AbstractLinearPath<?, ?> other) {
        if (other instanceof AbstractStackedLinearPath) {
            final AbstractStackedLinearPath<?, ?, ?, ?> stacked = (AbstractStackedLinearPath<?, ?, ?, ?>) other;
            return component.equals(stacked.component) && parent.equals(stacked.parent);
        }
        return super.equalPathFromRoot(other);
    }

    @Override
    protected final @NonNull P createRelativePath(final int skipFromRoot) {
        // TODO: can we optimize this one?
        final List<C> fromRoot = getPathFromRoot();
        return createPath(fromRoot.subList(skipFromRoot, fromRoot.size()));
    }

    @Override
    public final P getAncestor(final int depth) {
        checkArgument(depth >= 0, "Steps cannot be negative");

        // Calculate how far up our AbstractFixedImmutablePath ancestor is
        int stackedDepth = 1;
        P wlk = getParent();
        while (wlk instanceof AbstractStackedLinearPath) {
            wlk = wlk.getParent();
            stackedDepth++;
        }

        // Guaranteed to come from AbstractFixedImmutablePath
        final int fixedDepth = wlk.getPathFromRoot().size();
        if (fixedDepth >= depth) {
            return wlk.getAncestor(depth);
        }

        // Calculate our depth and check argument
        final int ourDepth = stackedDepth + fixedDepth;
        checkArgument(depth <= ourDepth, "Depth %s exceeds maximum depth %s", depth, ourDepth);

        // Requested depth is covered by the stack, traverse up for specified number of steps
        final int toWalk = ourDepth - depth;
        P result = thisPath();
        for (int i = 0; i < toWalk; ++i) {
            result = result.getParent();
        }

        return result;
    }
}
