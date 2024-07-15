/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Base64;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.data.api.codec.AbstractStringConverter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizationException;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;

abstract sealed class BinaryStringConverter extends AbstractStringConverter<byte[], BinaryTypeDefinition> {
    static final class Restricted extends BinaryStringConverter {
        private final @NonNull LengthConstraint lengthConstraint;

        Restricted(final BinaryTypeDefinition typeDef, final LengthConstraint lengthConstraint) {
            super(typeDef);
            this.lengthConstraint = requireNonNull(lengthConstraint);
        }

        @Override
        void validate(final byte[] value) throws NormalizationException {
            final var ranges = lengthConstraint.getAllowedRanges();
            if (!ranges.contains(value.length)) {
                throw NormalizationException.ofConstraint(
                    "Value length " + value.length + " is not in required ranges " + ranges,
                    ErrorType.APPLICATION, lengthConstraint);
            }
        }
    }

    static final class Unrestricted extends BinaryStringConverter {
        Unrestricted(final BinaryTypeDefinition typeDef) {
            super(typeDef);
        }

        @Override
        void validate(final byte[] value) {
            // No-op
        }
    }

    private BinaryStringConverter(final BinaryTypeDefinition typeDef) {
        super(byte[].class, typeDef);
    }

    @Override
    protected final byte[] normalizeFromString(final BinaryTypeDefinition typedef, final String str)
            throws NormalizationException {
        final byte[] ret;
        try {
            // https://www.rfc-editor.org/rfc/rfc4648#section-4 plus lenient to allow for MIME blocks
            ret = Base64.getMimeDecoder().decode(str);
        } catch (IllegalArgumentException e) {
            throw NormalizationException.ofCause(e);
        }
        validate(ret);
        return ret;
    }

    @Override
    protected final String canonizeToString(final BinaryTypeDefinition typedef, final byte[] obj)
            throws NormalizationException {
        // We do not split data on 76 characters on output
        validate(obj);
        return Base64.getEncoder().encodeToString(obj);
    }

    abstract void validate(byte[] value) throws NormalizationException;
}
