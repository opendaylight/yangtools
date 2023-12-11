/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.time.Instant;
import java.util.UUID;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.tree.api.CommitMetadata;

/**
 * An extended {@link Version} which tracks {@link CommitMetadata}.access performance.
 */
final class TrackingVersion extends Version {
    /**
     * Flattened version of {@link CommitMetadata}.
     */
    private final class Metadata implements CommitMetadata {
        @Override
        public UUID uuid() {
            return new UUID(uuidMost, uuidLeast);
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochSecond(instantSecond, instantNano);
        }

        @Override
        public int hashCode() {
            return uuid().hashCode() * 31 + instant().hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return this == obj || obj instanceof CommitMetadata other && uuid().equals(other.uuid())
                && instant().equals(other.instant());
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("uuid", uuid()).add("instant", instant()).toString();
        }
    }

    private static final VarHandle STATE;

    static {
        try {
            STATE = MethodHandles.lookup().findVarHandle(TrackingVersion.class, "state", byte.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final byte STATE_OPEN       = 0;
    private static final byte STATE_COMMITTING = 1;
    private static final byte STATE_COMMITTED  = 2;

    // Accessed via COMMITED only
    @SuppressWarnings("unused")
    private volatile byte state;

    // Inlined CommitMetadata components, values read access guarded by state == STATE_COMMITTED. The desing prioritizes
    // memory footprint over access performance/garbage generation for reads. The reason for that is that versions tend
    // to accumulate in history.
    //
    // This design results in the following overheads:
    //
    //   12b header
    //    1b state
    //   28b inlined fields
    //
    // Resulting in effective size 48 bytes in most cases, dropping to 40 bytes with JEP450
    //
    // Alternatively we could store an atomic reference to CommitMetadata which would hold inlined fields, reducing
    // the instance churn, but that would have the following overheads:
    //
    //   12b header (TrackingVersion)
    //    4b reference (or 8b without compressed oops)
    //
    //   12b header (CommitMetadata)
    //   28b inlined fields
    // i.e. effective size of 56 (or 64) bytes, with JEP450 making it a constant 56 bytes
    //
    // Yet another alternative would be fully-instantiated classes, which hurts even more:
    //
    //   12b header (TrackingVersion)
    //    4b reference (or 8b without compressed oops)
    //
    //   12b header (CommitMetadata)
    //    4b uuid reference (or 8b)
    //    4b instant reference (or 8b)
    //
    //   12b header (UUID)
    //    8b mostBits
    //    8b leastBits
    //
    //   12b header (Instant)
    //    4b nano
    //    8b second
    //
    // i.e. effective size of 96 (or 112) bytes, with JEP450 making it constant 96 bytes
    //
    // This analysis will need to revisited once JEP401 (Value Classes and Objects) takes shape and becomes relevant, as
    // it aimed at eliminating exactly these sorts of 'inlining has better behaviour than proper object model' cases.

    private long instantSecond;
    private int instantNano;
    private long uuidMost;
    private long uuidLeast;

    @Override
    public TrackingVersion next() {
        return new TrackingVersion();
    }

    @Override
    public @NonNull CommitMetadata commitMetadata() {
        final var local = (int) STATE.getAcquire(this);
        if (local == STATE_COMMITTED) {
            return new Metadata();
        }
        throw new IllegalStateException("No metadata available for " + this + " in state " + local);
    }

    @Override
    public void commit() {
        commit(Instant.now(), UUID.randomUUID());
    }

    @Override
    public void commit(final CommitMetadata metadata) {
        commit(requireNonNull(metadata.instant(), "instant"), requireNonNull(metadata.uuid(), "uuid"));
    }

    private void commit(final @NonNull Instant instant, final @NonNull UUID uuid) {
        final var witness = (int) STATE.compareAndExchange(this, STATE_OPEN, STATE_COMMITTING);
        if (witness != STATE_OPEN) {
            throw new IllegalStateException("Cannot set metadata of " + this + " in state " + witness);
        }

        instantSecond = instant.getEpochSecond();
        instantNano = instant.getNano();
        uuidMost = uuid.getMostSignificantBits();
        uuidLeast = uuid.getLeastSignificantBits();
        STATE.setRelease(this, STATE_COMMITTED);
    }
}