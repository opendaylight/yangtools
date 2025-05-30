/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.Cleaner.Cleanable;
import java.lang.ref.Reference;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Registration} holding a managed resource that needs to be processed when {@link #close()} is invoked. This
 * processing is done via {@link #cleanResource(Object)}, which is guaranteed to be called at most once. This class is
 * guaranteed not to retain the reference once {@link #close()} is invoked, irrespective of whether
 *
 * @param <T> resource type
 */
public abstract class ResourceRegistration<T> extends BaseRegistration {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceRegistration.class);
    private static final VarHandle VH;

    // All access needs to go through this handle, really.
    // NOTE: we really would like to use 'boolean' here, but we may have a Serializable subclass and we do not want
    //       to risk breakage for little benefit we would get in terms of our code here.
    private volatile @Nullable T resource;

    static {
        try {
            VH = MethodHandles.lookup().findVarHandle(AbstractRegistration.class, "closed", byte.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * The sole constructor.
     *
     * @param resource the resource
     */
    @NonNullByDefault
    protected ResourceRegistration(final T resource) {
        this.resource = requireNonNull(resource);
    }

    @NonNullByDefault
    public static final BaseRegistration of(final Cleanable cleanable) {
        return new ResourceRegistration<>(cleanable) {
            @Override
            protected void cleanResource(final Cleanable resource) {
                resource.clean();
            }
        };
    }

    @NonNullByDefault
    public static final BaseRegistration of(final Runnable runnable) {
        return new ResourceRegistration<>(runnable) {
            @Override
            protected void cleanResource(final Runnable resource) {
                resource.run();
            }
        };
    }

    @NonNullByDefault
    public static final BaseRegistration of(final Reference<?> reference) {
        return new ResourceRegistration<Reference<?>>(reference) {
            @Override
            protected void cleanResource(final Reference<?> resource) {
                resource.clear();
            }
        };
    }

    @Override
    public final boolean isClosed() {
        return getAquire() == null;
    }

    @Override
    public final boolean notClosed() {
        return getAquire() != null;
    }

    @Override
    public final void close() {
        // we want full setVolatile() memory semantics here, as all state before calling this method needs to be visible
        final var prev = (T) VH.getAndSet(this, null);
        if (prev != null) {
            close(prev);
        }
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private void close(final T prev) {
        try {
            cleanResource(prev);
        } catch (RuntimeException e) {
            LOG.warn("{} failed to clean {}", this, prev, e);
        }
    }

    /**
     * Clean the resource.
     *
     * @param resource the resource
     */
    @NonNullByDefault
    protected abstract void cleanResource(T resource);

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("resource", getAquire());
    }

    private @Nullable T getAquire() {
        return (T) VH.getAcquire(this);
    }

    /**
     * Dance around <a href=""https://github.com/spotbugs/spotbugs/issues/2749">underlying issue</a>.
     */
    @Deprecated(forRemoval = true)
    final void spotbugs2749() {
        // This should never happen
        if (resource == VH) {
            resource = null;
        }
    }
}
