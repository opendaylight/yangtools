/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.codec.EnumCodec;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public final class EnumStringCodec extends TypeDefinitionAwareCodec<String, EnumTypeDefinition>
        implements EnumCodec<String> {
    private final Map<String, String> values;

    private EnumStringCodec(final Optional<EnumTypeDefinition> typeDef) {
        super(typeDef, String.class);
        if (typeDef.isPresent()) {
            final Builder<String, String> b = ImmutableMap.builder();
            for (final EnumPair pair : typeDef.get().getValues()) {
                // Intern the String to get wide reuse
                final String v = pair.getName().intern();
                b.put(v, v);
            }
            values = b.build();
        } else {
            values = null;
        }
    }

    public static EnumStringCodec from(final EnumTypeDefinition normalizedType) {
        return new EnumStringCodec(Optional.of(normalizedType));
    }

    @Override
    public String deserialize(final String s) {
        if (values == null) {
            return s;
        }

        // Lookup the serialized string in the values. Returned string is the interned instance, which we want
        // to use as the result.
        final String result = values.get(s);
        Preconditions.checkArgument(result != null, "Invalid value '%s' for enum type. Allowed values are: %s",
                s, values.keySet());
        return result;
    }

    @Override
    public String serialize(final String data) {
        return Objects.toString(data, "");
    }
}
