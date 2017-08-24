/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.util.Objects;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * A potential schema source. Instances of this class track the various
 * representations of a schema source and the cost attached to obtaining
 * the source from them.
 */
@Beta
public final class PotentialSchemaSource<T extends SchemaSourceRepresentation> {
    /**
     * Each registered source has a cost associated with it. Since a particular
     * representation can be acquired by various means, here are general constants
     * for common cases.
     */
    public enum Costs {
        /**
         * The source is immediately available, via a lookup or similar.
         */
        IMMEDIATE(0),
        /**
         * The source is available via a computation. For transformation-type
         * computation, the cost of acquiring the cost needs to be added, too.
         */
        COMPUTATION(1),
        /**
         * The source is available by performing local IO, such that reading
         * from a disk.
         */
        LOCAL_IO(4),
        /**
         * The source is available by performing remote IO, such as fetching
         * from an HTTP server or similar.
         */
        REMOTE_IO(8);

        private final int value;

        Costs(final int value) {
            this.value = value;
        }

        /**
         * The the cost value.
         *
         * @return Const constant.
         */
        public int getValue() {
            return value;
        }
    }

    private static final Interner<PotentialSchemaSource<?>> INTERNER = Interners.newWeakInterner();
    private final Class<? extends T> representation;
    private final SourceIdentifier sourceIdentifier;
    private final int cost;

    private PotentialSchemaSource(final SourceIdentifier sourceIdentifier, final Class<? extends T> representation,
            final int cost) {
        this.representation = Preconditions.checkNotNull(representation);
        this.sourceIdentifier = Preconditions.checkNotNull(sourceIdentifier);
        Preconditions.checkArgument(cost >= 0, "cost has to be non-negative");
        this.cost = cost;
    }

    public static <T extends SchemaSourceRepresentation> PotentialSchemaSource<T> create(
            final SourceIdentifier sourceIdentifier, final Class<? extends T> representation, final int cost) {
        return new PotentialSchemaSource<>(sourceIdentifier, representation, cost);
    }

    /**
     * Return a cached reference to an object equal to this object.
     *
     * @return A potentially shared reference, not guaranteed to be unique.
     */
    @SuppressWarnings("unchecked")
    public PotentialSchemaSource<T> cachedReference() {
        return (PotentialSchemaSource<T>) INTERNER.intern(this);
    }

    public SourceIdentifier getSourceIdentifier() {
        return sourceIdentifier;
    }

    public Class<? extends T> getRepresentation() {
        return representation;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cost, representation, sourceIdentifier);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PotentialSchemaSource)) {
            return false;
        }
        final PotentialSchemaSource<?> other = (PotentialSchemaSource<?>) obj;
        return cost == other.cost && representation.equals(other.representation)
                && sourceIdentifier.equals(other.sourceIdentifier);
    }
}
