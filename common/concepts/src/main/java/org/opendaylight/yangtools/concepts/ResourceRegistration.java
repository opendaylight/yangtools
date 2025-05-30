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
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.lang.ref.Reference;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Registration} holding a managed resource that needs to be processed when {@link #close()} is invoked. This
 * processing is done via {@link #clean(Object)}, which is guaranteed to be called at most once. This class is
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

    // Note: these should live in Registration, really

    @NonNullByDefault
    public static final BaseRegistration of(final AutoCloseable autoCloseable) {
        // Note: we do not check for the argument being BaseRegistration on purpose because we guarantee identity-based
        //       equality. That implies the argument to this method and the result of this method could be stored in
        //       the same Set -- in which case they need to be treated as two separate objects.
        return new ResourceRegistration<>(autoCloseable) {
            @Override
            protected void clean(final AutoCloseable resource) throws Exception {
                resource.close();
            }

            @Override
            protected String resourceName() {
                return "resource";
            }
        };
    }

    @NonNullByDefault
    public static final BaseRegistration of(final Cleanable cleanable) {
        return new ResourceRegistration<>(cleanable) {
            @Override
            protected void clean(final Cleanable resource) {
                resource.clean();
            }

            @Override
            protected String resourceName() {
                return "cleanable";
            }
        };
    }

    @NonNullByDefault
    public static final BaseRegistration of(final Cleaner cleaner, final Object obj, final Runnable action) {
        return of(cleaner.register(obj, action));
    }

    @NonNullByDefault
    public static final BaseRegistration of(final Reference<?> reference) {
        return new ResourceRegistration<Reference<?>>(reference) {
            @Override
            protected void clean(final Reference<?> resource) {
                resource.clear();
            }

            @Override
            protected String resourceName() {
                return "reference";
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
            clean(prev);
        } catch (Exception e) {
            LOG.warn("{} failed to clean {}", this, prev, e);
        }
    }

    /**
     * Clean the resource. Any reported exceptions will be logged and suppressed.
     *
     * @param resource the resource
     * @throws Exception when an error occurs
     */
    @NonNullByDefault
    protected abstract void clean(T resource) throws Exception;

    /**
     * {@return the descriptive name of the resource}
     */
    protected abstract String resourceName();

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return super.equals(obj);
    }

    @Override
    protected final ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add(resourceName(), getAquire());
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
