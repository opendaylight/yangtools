package org.opendaylight.yangtools.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

public abstract class AbstractFixedImmutablePath<P extends ImmutablePath<P, C>, C extends Comparable<C> & Immutable>
        extends AbstractImmutablePath<P, C> {
    private static final long serialVersionUID = 1L;

    private final @NonNull ImmutableList<C> fromRoot;

    private transient volatile @Nullable P parent;

    protected AbstractFixedImmutablePath(final int hash, final ImmutableList<C> fromRoot) {
        super(hash);
        this.fromRoot = requireNonNull(fromRoot);
    }

    @Override
    public final boolean isEmpty() {
        return fromRoot.isEmpty();
    }

    @Override
    public final @Nullable C getLastComponent() {
        return fromRoot.isEmpty() ? null : fromRoot.get(fromRoot.size() - 1);
    }

    @Override
    public final @Nullable P getParent() {
        if (fromRoot.isEmpty()) {
            return null;
        }

        P ret = parent;
        if (ret == null) {
            ret = verifyNotNull(createPath(fromRoot.subList(0, fromRoot.size() - 1)));
            parent = ret;
        }

        return ret;
    }

    @Override
    public final @NonNull ImmutableList<C> getPathFromRoot() {
        return fromRoot;
    }

    @Override
    public final @NonNull ImmutableList<C> getPathTowardsRoot() {
        return fromRoot.reverse();
    }

    @Override
    protected final ImmutableList<C> tryPathFromRoot() {
        return getPathFromRoot();
    }

    @Override
    protected final ImmutableList<C> tryPathTowardsRoot() {
        return getPathTowardsRoot();
    }

    @Override
    protected final @NonNull P createRelativePath(final int skipFromRoot) {
        return skipFromRoot == fromRoot.size() ? emptyPath()
                : createPath(fromRoot.subList(skipFromRoot, fromRoot.size()));
    }

    @Override
    public final P getAncestor(final int depth) {
        checkArgument(depth >= 0, "Negative depth is not allowed");
        checkArgument(depth <= fromRoot.size(), "Depth %s exceeds maximum depth %s", depth, fromRoot.size());

        if (depth == fromRoot.size()) {
            return thisPath();
        }
        if (depth == fromRoot.size() - 1) {
            // Use the parent cache
            return getParent();
        }
        return createPath(fromRoot.subList(0, depth));
    }

    @Override
    final boolean equalPathFromRoot(final AbstractImmutablePath<?, ?> other) {
        return other instanceof AbstractFixedImmutablePath
                ? fromRoot.equals(((AbstractFixedImmutablePath<?, ?>) other).fromRoot)
                        : super.equalPathFromRoot(other);
    }
}
