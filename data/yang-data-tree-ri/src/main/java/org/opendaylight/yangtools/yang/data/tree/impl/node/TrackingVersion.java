/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.time.Instant;
import java.util.UUID;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.tree.api.CommitMetadata;

/**
 *
 */
public final class TrackingVersion extends Version {
    /**
     * Flattened version of {@link CommitMetadata}.
     */
    private record Metadata(long instantSecond, int instantNano, long uuidMost, long uuidLeast)
            implements CommitMetadata {
        Metadata(final Instant instant, final UUID uuid) {
            this(instant.getEpochSecond(), instant.getNano(),
                uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
        }

        @Override
        public UUID uuid() {
            return new UUID(uuidMost, uuidLeast);
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochSecond(instantSecond, instantNano);
        }
    }

    private static final VarHandle METADATA;

    static {
        try {
            METADATA = MethodHandles.lookup().findVarHandle(TrackingVersion.class, "metadata", Metadata.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private volatile Metadata metadata;

    @Override
    public TrackingVersion next() {
        return new TrackingVersion();
    }

    @Override
    public @NonNull CommitMetadata commitMetadata() {
        final var ret = (CommitMetadata) METADATA.getAcquire(this);
        if (ret != null) {
            return ret;
        }
        throw new IllegalStateException("No metadata set in " + this);
    }

    @Override
    public void commit() {
        commit(new Metadata(Instant.now(), UUID.randomUUID()));
    }

    @Override
    public void commit(final CommitMetadata metadata) {
        commit(new Metadata(metadata.instant(), metadata.uuid()));
    }

    private void commit(final @NonNull Metadata metadata) {
        final var witness = METADATA.compareAndExchangeRelease(this, null, metadata);
        if (witness != null) {
            throw new IllegalStateException("Metadata already set to " + witness + " in " + this);
        }
    }
}