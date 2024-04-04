/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.RangeSet;
import java.util.Base64;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.data.api.codec.BinaryCodec;
import org.opendaylight.yangtools.yang.data.api.codec.YangInvalidValueException;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;

/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public abstract sealed class BinaryStringCodec extends TypeDefinitionAwareCodec<byte[], BinaryTypeDefinition>
        implements BinaryCodec<String> {
    private static final class Restricted extends BinaryStringCodec {
        private final LengthConstraint lengthConstraint;

        Restricted(final BinaryTypeDefinition typeDef, final LengthConstraint lengthConstraint) {
            super(typeDef);
            this.lengthConstraint = requireNonNull(lengthConstraint);
        }

        @Override
        void validate(final byte[] value) {
            final RangeSet<Integer> ranges = lengthConstraint.getAllowedRanges();
            if (!ranges.contains(value.length)) {
                throw new YangInvalidValueException(ErrorType.APPLICATION, lengthConstraint,
                        "Value length " + value.length + " is not in required ranges " + ranges);
            }
        }
    }

    private static final class Unrestricted extends BinaryStringCodec {
        Unrestricted(final BinaryTypeDefinition typeDef) {
            super(typeDef);
        }

        @Override
        void validate(final byte[] value) {
            // No-op
        }
    }

    BinaryStringCodec(final BinaryTypeDefinition typeDef) {
        super(byte[].class, typeDef);
    }

    public static BinaryStringCodec from(final BinaryTypeDefinition type) {
        final var optConstraint = type.getLengthConstraint();
        return optConstraint.isPresent() ? new Restricted(type, optConstraint.orElseThrow()) : new Unrestricted(type);
    }

    @Override
    public final byte[] deserializeImpl(final String product) {
        // https://www.rfc-editor.org/rfc/rfc4648#section-4 plus lenient to allow for MIME blocks
        final byte[] ret = Base64.getMimeDecoder().decode(product);
        validate(ret);
        return ret;
    }

    @Override
    protected final String serializeImpl(final byte[] data) {
        // We do not split data on 76 characters on output
        return Base64.getEncoder().encodeToString(data);
    }

    abstract void validate(byte[] value);
}
