/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import org.opendaylight.yangtools.yang.data.api.codec.EnumCodec;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public final class EnumStringCodec extends TypeDefinitionAwareCodec<String, EnumTypeDefinition>
        implements EnumCodec<String> {
    private final ImmutableMap<String, String> values;

    private EnumStringCodec(final EnumTypeDefinition typeDef) {
        super(String.class, typeDef);
        values = typeDef.getValues().stream()
                // Intern the String to get wide reuse
                .map(pair -> pair.getName().intern())
                .collect(ImmutableMap.toImmutableMap(Functions.identity(), Functions.identity()));
    }

    public static EnumStringCodec from(final EnumTypeDefinition typeDef) {
        return new EnumStringCodec(typeDef);
    }

    @Override
    protected String deserializeImpl(final String product) {
        // Lookup the serialized string in the values. Returned string is the interned instance, which we want
        // to use as the result.
        final String result = values.get(product);
        checkArgument(result != null, "Invalid value '%s' for enum type. Allowed values are: %s", product,
                values.keySet());
        return result;
    }

    @Override
    protected String serializeImpl(final String input) {
        return input;
    }
}
