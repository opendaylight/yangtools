/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

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
         * The source is available by performing local IO, although this
         * represents a sideloaded schema. This value is used to differentiate
         * remote schema from device and local fallback.
         */
        LOCAL_IO_FALLBACK(6),
        /**
         * The source is available by performing remote IO, such as fetching
         * from an HTTP server or similar.
         */
        REMOTE_IO(8);

        private final int value;

        private Costs(final int value) {
            this.value = value;
        }

        /**
         * The the cost value.
         *
         * @return Const constant.
         */
        public int getValue() {
            return this.value;
        }
    }

    private static final ObjectCache CACHE = ObjectCacheFactory.getObjectCache(PotentialSchemaSource.class);
    private final Class<? extends T> representation;
    private final SourceIdentifier sourceIdentifier;
    private final int cost;

    private PotentialSchemaSource(final SourceIdentifier sourceIdentifier, final Class<? extends T> representation, final int cost) {
        this.representation = Preconditions.checkNotNull(representation);
        this.sourceIdentifier = Preconditions.checkNotNull(sourceIdentifier);
        Preconditions.checkArgument(cost >= 0, "cost has to be non-negative");
        this.cost = cost;
    }

    public static final <T extends SchemaSourceRepresentation> PotentialSchemaSource<T> create(final SourceIdentifier sourceIdentifier, final Class<? extends T> representation, final int cost) {
        return new PotentialSchemaSource<>(sourceIdentifier, representation, cost);
    }

    /**
     * Return a cached reference to an object equal to this object.
     *
     * @return A potentially shared reference, not guaranteed to be unique.
     */
    public PotentialSchemaSource<T> cachedReference() {
        return CACHE.getReference(this);
    }

    public SourceIdentifier getSourceIdentifier() {
        return this.sourceIdentifier;
    }

    public Class<? extends T> getRepresentation() {
        return this.representation;
    }

    public int getCost() {
        return this.cost;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + this.cost;
        result = (prime * result) + this.representation.hashCode();
        result = (prime * result) + this.sourceIdentifier.hashCode();
        return result;
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
        if (this.cost != other.cost) {
            return false;
        }
        if (!this.representation.equals(other.representation)) {
            return false;
        }
        if (!this.sourceIdentifier.equals(other.sourceIdentifier)) {
            return false;
        }
        return true;
    }
}
