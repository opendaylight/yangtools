/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netconf.databind;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.security.Principal;
import java.util.UUID;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract base class for {@link Request} implementations. Each instance is automatically assigned a
 * <a href="https://www.rfc-editor.org/rfc/rfc4122#section-4.4">type 4 UUID</a>.
 *
 * @param <R> type of reported result
 */
public abstract class AbstractRequest<R> implements Request<R> {
    private static final VarHandle UUID_VH;

    static {
        try {
            UUID_VH = MethodHandles.lookup().findVarHandle(AbstractRequest.class, "uuid", UUID.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @Nullable Principal principal;

    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile UUID uuid;

    /**
     * Default constructor.
     *
     * @param principal the {@link Principal} making the request, {@code null} if not authenticated.
     */
    protected AbstractRequest(final @Nullable Principal principal) {
        this.principal = principal;
    }

    @Override
    public final UUID uuid() {
        final var existing = (UUID) UUID_VH.getAcquire(this);
        return existing != null ? existing : loadUuid();
    }

    private @NonNull UUID loadUuid() {
        final var created = UUID.randomUUID();
        final var witness = (UUID) UUID_VH.compareAndExchangeRelease(this, null, created);
        return witness != null ? witness : created;
    }

    @Override
    public final Principal principal() {
        return principal;
    }

    @Override
    public final void completeWith(final R result) {
        onSuccess(requireNonNull(result));
    }

    @Override
    public final void failWith(final RequestException failure) {
        onFailure(requireNonNull(failure));
    }

    protected abstract void onSuccess(@NonNull R result);

    protected abstract void onFailure(@NonNull RequestException failure);

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        return super.equals(this);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    /**
     * Add attributes to a {@link ToStringHelper}.
     *
     * @param helper the helper
     * @return the helper
     */
    protected @NonNull ToStringHelper addToStringAttributes(final @NonNull ToStringHelper helper) {
        helper.add("uuid", uuid());

        final var tmp = principal;
        if (tmp != null) {
            helper.add("principal", tmp.getName());
        }

        return helper;
    }
}
