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
     * First shipping is Magnesium. Does not support {@link BigInteger} mirroring it being superseded by {@link Uint64}
     * in {@link ValueNode#body()}.
     */
    MAGNESIUM {
        @Override
        public NormalizedNodeDataOutput newDataOutput(final DataOutput output) {
            return new MagnesiumDataOutput(output);
        }
    },
    /**
     * First shipping is Potassium. Does not support {@code AugmentationIdentifier} nor {@code AugmentationNode}.
     */
    POTASSIUM {
        @Override
        public NormalizedNodeDataOutput newDataOutput(final DataOutput output) {
            return new PotassiumDataOutput(output);
        }
    };

    /**
     * Return the current runtime version. Guaranteed to not throw {@link UnsupportedOperationException} from
     * {@link #newDataOutput(DataOutput)}.
     *
     * @return Current runtime version.
     */
    public static NormalizedNodeStreamVersion current() {
        return POTASSIUM;
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
