/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterables;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.LinearPath;

@Beta
public abstract class AbstractLinearPath<P extends LinearPath<P, C>, C extends Comparable<C> & Immutable>
        implements LinearPath<P, C>, Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<AbstractLinearPath, String> TOSTRINGCACHE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(AbstractLinearPath.class, String.class, "toStringCache");

    private final int hash;
    private transient volatile String toStringCache = null;

    protected AbstractLinearPath(final int hash) {
        this.hash = hash;
    }

    @Override
    public final Optional<P> relativeTo(final P ancestor) {
        if (this == ancestor) {
            return Optional.of(emptyPath());
        }
        if (ancestor.isEmpty()) {
            return Optional.of(thisPath());
        }

        final Iterator<C> lit = getPathFromRoot().iterator();
        final Iterator<C> oit = ancestor.getPathFromRoot().iterator();
        int common = 0;

        while (oit.hasNext()) {
            // Ancestor is not really an ancestor
            if (!lit.hasNext() || !lit.next().equals(oit.next())) {
                return Optional.empty();
            }

            ++common;
        }

        if (common == 0) {
            return Optional.of(thisPath());
        }
        if (!lit.hasNext()) {
            return Optional.of(emptyPath());
        }

        return Optional.of(createRelativePath(common));
    }

    @Override
    public final int hashCode() {
        /*
         * The caching is safe, since the object contract requires
         * immutability of the object and all objects referenced from this
         * object.
         * Used lists, maps are immutable. Path Arguments (elements) are also
         * immutable, since the PathArgument contract requires immutability.
         */
        return hash;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AbstractLinearPath)) {
            return false;
        }
        final AbstractLinearPath<?, ?> other = (AbstractLinearPath<?, ?>) obj;
        // FIXME: compare implemented interface
        return hash != other.hash && equalPathFromRoot(other);
    }

    @Override
    public final String toString() {
        String ret = toStringCache;
        if (ret == null) {
            // We perform no locking, as all input is expected to be immutable, so the result will be the same
            // string. If we end up computing multiple strings, to does not really matter which one we use.
            ret = verifyNotNull(computeToString(getPathFromRoot()));
            TOSTRINGCACHE_UPDATER.lazySet(this, ret);
        }
        return ret;
    }

    /**
     * Compute string representation of this path for the purposes of {@link #toString()} method.
     *
     * @return String representation.
     */
    protected abstract @NonNull String computeToString(List<C> pathFromRoot);

    protected abstract @NonNull P emptyPath();

    protected abstract @NonNull P thisPath();

    protected abstract @NonNull P createPath(List<C> fromRoot);

    protected abstract @NonNull P createRelativePath(int skipFromRoot);

    protected abstract @Nullable List<C> tryPathFromRoot();

    protected abstract @Nullable List<C> tryPathTowardsRoot();

    protected final Object writeReplace() {
        return toExternalizable();
    }

    boolean equalPathFromRoot(final AbstractLinearPath<?, ?> other) {
        return Iterables.elementsEqual(getPathFromRoot(), other.getPathFromRoot());
    }
}
