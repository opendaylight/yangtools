/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.opendaylight.yangtools.yang.common.Bits;
import org.opendaylight.yangtools.yang.data.api.codec.BitsCodec;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public final class BitsStringCodec extends TypeDefinitionAwareCodec<Bits, BitsTypeDefinition>
        implements BitsCodec<String> {

    private static final Comparator<Bit> BIT_COMPARATOR = Comparator.comparing(Bit::getPosition);
    private final Map<String, Integer> bitNameToOffsetMap;

    @SuppressWarnings("unchecked")
    private BitsStringCodec(final BitsTypeDefinition typeDef) {
        super(requireNonNull(typeDef), Bits.class);
        final AtomicInteger counter = new AtomicInteger(0);
        bitNameToOffsetMap = typeDef.getBits().stream().sorted(BIT_COMPARATOR)
            .collect(ImmutableMap.toImmutableMap(Bit::getName, bit -> counter.getAndIncrement()));
    }

    public static BitsStringCodec from(final BitsTypeDefinition type) {
        return new BitsStringCodec(type);
    }

    @Override
    protected Bits deserializeImpl(final String input) {
        return Bits.of(bitNameToOffsetMap, input);
    }

    @Override
    protected String serializeImpl(final Bits input) {
        return requireNonNull(input).toStringValue();
    }
}