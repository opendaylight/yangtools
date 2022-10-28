/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import java.io.DataOutput;
import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.data.api.schema.ValueNode;

/**
 * Enumeration of all stream versions this implementation supports on both input and output.
 */
@NonNullByDefault
public enum NormalizedNodeStreamVersion {
    /**
     * Original stream version, as shipped in OpenDaylight Lithium simultaneous release. The caveat here is that this
     * version has augmented in OpenDaylight Oxygen to retrofit a non-null representation of the empty type.
     */
    LITHIUM {
        /**
         * {@inheritDoc}
         *
         * @deprecated This version is a historic one and should not be used in code. It does not support current
         *             mapping of {@code Uint8} et al. and hence results in a stream which needs to be further adapted
         *             to current definition of LeafNode.
         */
        @Override
        @Deprecated(since = "10.0.0", forRemoval = true)
        public NormalizedNodeDataOutput newDataOutput(final DataOutput output) {
            return new LithiumNormalizedNodeOutputStreamWriter(output);
        }
    },
    /**
     * Updated stream version, as shipped in OpenDaylight Neon SR2 release. Improves identifier encoding over
     * {@link #LITHIUM}, so that QName caching is more effective.
     */
    NEON_SR2 {
        /**
         * {@inheritDoc}
         *
         * @deprecated This version is a historic one and should not be used in code. It does not support current
         *             mapping of {@code Uint8} et al. and hence results in a stream which needs to be further adapted
         *             to current definition of LeafNode.
         */
        @Override
        @Deprecated(since = "10.0.0", forRemoval = true)
        public NormalizedNodeDataOutput newDataOutput(final DataOutput output) {
            return new NeonSR2NormalizedNodeOutputStreamWriter(output);
        }
    },
    /**
     * First shipping in Sodium SR1. Improved stream coding to eliminate redundancies present in {@link #NEON_SR2}.
     * Supports {@code Uint8} et al. as well as {@link BigInteger}.
     */
    SODIUM_SR1 {
        @Override
        @Deprecated(since = "10.0.0", forRemoval = true)
        public NormalizedNodeDataOutput newDataOutput(final DataOutput output) {
            return new SodiumSR1DataOutput(output);
        }
    },
    /**
     * First shipping is Magnesium. Does not support {@link BigInteger} mirroring it being superseded by {@link Uint64}
     * in {@link ValueNode#body()}.
     */
    MAGNESIUM {
        @Override
        public NormalizedNodeDataOutput newDataOutput(final DataOutput output) {
            return new MagnesiumDataOutput(output);
        }
    };

    /**
     * Return the current runtime version. Guaranteed to not throw {@link UnsupportedOperationException} from
     * {@link #newDataOutput(DataOutput)}.
     *
     * @return Current runtime version.
     */
    public static NormalizedNodeStreamVersion current() {
        return MAGNESIUM;
    }

    /**
     * Creates a new {@link NormalizedNodeDataOutput} instance that writes to the given output.
     *
     * @param output the DataOutput to write to
     * @return a new {@link NormalizedNodeDataOutput} instance
     * @throws NullPointerException if {@code output} is null
     * @throws UnsupportedOperationException if this version cannot be created in this runtime
     */
    public abstract NormalizedNodeDataOutput newDataOutput(DataOutput output);
}
