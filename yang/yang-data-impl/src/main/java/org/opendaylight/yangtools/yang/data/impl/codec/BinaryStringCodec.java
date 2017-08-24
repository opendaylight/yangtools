/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.RangeMap;
import com.google.common.io.BaseEncoding;
import javax.xml.bind.DatatypeConverter;
import org.opendaylight.yangtools.yang.data.api.codec.BinaryCodec;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;


/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public abstract class BinaryStringCodec extends TypeDefinitionAwareCodec<byte[], BinaryTypeDefinition>
        implements BinaryCodec<String> {
    private static final class Restricted extends BinaryStringCodec {
        private final RangeMap<Integer, ConstraintMetaDefinition> ranges;

        Restricted(final BinaryTypeDefinition typeDef) {
            super(typeDef);
            ranges = typeDef.getLengthConstraints();
        }

        @Override
        void validate(final byte[] value) {
            final ConstraintMetaDefinition constraint = ranges.get(value.length);
            Preconditions.checkArgument(constraint != null,
                "Value length %s does not match constraints %s", value.length, ranges);
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
        return type.getLengthConstraints().asMapOfRanges().isEmpty() ? new Unrestricted(type) : new Restricted(type);
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
