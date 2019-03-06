package org.opendaylight.yangtools.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

public abstract class AbstractStackedImmutablePath<P extends ImmutablePath<P, C>,
        C extends Comparable<C> & Immutable, F extends AbstractFixedImmutablePath<P, C>,
        S extends AbstractStackedImmutablePath<P, C, F, S>>
        extends AbstractImmutablePath<P, C> {
    private static final long serialVersionUID = 1L;

    private final @NonNull P parent;
    private final @NonNull C component;

    private transient volatile PathComponentsFromRoot<C> fromRoot;
    private transient volatile PathComponentsTowardRoot<P, C, F, S> towardRoot;

    protected AbstractStackedImmutablePath(final int hash, final P parent, final C component) {
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
            AbstractImmutablePath<?, C> current = this;
            do {
                verify(current instanceof AbstractStackedImmutablePath);
                final AbstractStackedImmutablePath<?, C, F, S> stacked =
                        (AbstractStackedImmutablePath<?, C, F, S>) current;
                stack.add(stacked.getLastComponent());
                final ImmutablePath<?, C> parent = stacked.getParent();
                verify(parent instanceof AbstractImmutablePath);
                current = (AbstractImmutablePath<?, C>) parent;
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
    final boolean equalPathFromRoot(final AbstractImmutablePath<?, ?> other) {
        if (other instanceof AbstractStackedImmutablePath) {
            final AbstractStackedImmutablePath<?, ?, ?, ?> stacked = (AbstractStackedImmutablePath<?, ?, ?, ?>) other;
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
        while (wlk instanceof AbstractStackedImmutablePath) {
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
