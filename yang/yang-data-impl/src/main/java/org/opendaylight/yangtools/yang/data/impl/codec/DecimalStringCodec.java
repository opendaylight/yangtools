/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.codec.DecimalCodec;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;

/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public final class DecimalStringCodec extends TypeDefinitionAwareCodec<BigDecimal, DecimalTypeDefinition>
        implements DecimalCodec<String> {

    private DecimalStringCodec(final Optional<DecimalTypeDefinition> typeDef) {
        super(typeDef, BigDecimal.class);
    }

    public static DecimalStringCodec from(final DecimalTypeDefinition type) {
        return new DecimalStringCodec(Optional.of(type));
    }

    @Override
    public String serialize(final BigDecimal data) {
        return Objects.toString(data, "");
    }

    @Override
    public BigDecimal deserialize(final String stringRepresentation) {
        checkArgument(stringRepresentation != null, "Input cannot be null");
        // FIXME: run value validation
        return new BigDecimal(stringRepresentation);
    }
}
