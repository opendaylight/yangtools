/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;
import javax.xml.bind.DatatypeConverter;
import org.opendaylight.yangtools.yang.data.api.codec.BinaryCodec;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;

/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public abstract class BinaryStringCodec extends TypeDefinitionAwareCodec<byte[], BinaryTypeDefinition>
        implements BinaryCodec<String> {
    private static final class Restricted extends BinaryStringCodec {
        private final LengthConstraint lengthConstraint;

        Restricted(final BinaryTypeDefinition typeDef, final LengthConstraint lengthConstraint) {
            super(typeDef);
            this.lengthConstraint = requireNonNull(lengthConstraint);
        }

        @Override
        void validate(final byte[] value) {
            // FIXME: throw an exception capturing the constraint violation
            checkArgument(lengthConstraint.getAllowedRanges().contains(value.length),
                "Value length %s does not match constraint %s", value.length, lengthConstraint);
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
        super(Optional.of(typeDef), byte[].class);
    }

    public static BinaryStringCodec from(final BinaryTypeDefinition type) {
        final java.util.Optional<LengthConstraint> optConstraint = type.getLengthConstraint();
        return optConstraint.isPresent() ? new Restricted(type, optConstraint.get()) : new Unrestricted(type);
    }

    @Override
    public String serialize(final byte[] data) {
        return data == null ? "" : BaseEncoding.base64().encode(data);
    }

    @Override
    public byte[] deserialize(final String stringRepresentation) {
        if (stringRepresentation == null) {
            return null;
        }

        final byte[] ret = DatatypeConverter.parseBase64Binary(stringRepresentation);
        validate(ret);
        return ret;
    }

    abstract void validate(byte[] value);
}
