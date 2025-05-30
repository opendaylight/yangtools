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
public abstract class GenericRegistration<T> extends BaseRegistration {
    private static final Logger LOG = LoggerFactory.getLogger(GenericRegistration.class);
    private static final VarHandle VH;

    // All access needs to go through this handle, really.
    // NOTE: we really would like to use 'boolean' here, but we may have a Serializable subclass and we do not want
    //       to risk breakage for little benefit we would get in terms of our code here.
    private volatile @Nullable T object;

    static {
        try {
            VH = MethodHandles.lookup().findVarHandle(GenericRegistration.class, "obj", Object.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * The sole constructor.
     *
     * @param object the object
     */
    @NonNullByDefault
    protected GenericRegistration(final T object) {
        this.object = requireNonNull(object);
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
        final var obj = (T) VH.getAndSet(this, null);
        if (obj != null) {
            close(obj);
        }
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private void close(final T obj) {
        try {
            clean(obj);
        } catch (Exception e) {
            LOG.warn("{} failed to clean {}", this, obj, e);
        }
    }

    /**
     * Clean the resource. Any reported exceptions will be logged and suppressed.
     *
     * @param obj the resource
     * @throws Exception when an error occurs
     */
    @NonNullByDefault
    protected abstract void clean(T obj) throws Exception;

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
        if (object == VH) {
            object = null;
        }
    }
}
