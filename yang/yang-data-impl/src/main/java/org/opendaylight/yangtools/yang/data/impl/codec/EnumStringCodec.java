/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.data.api.codec.EnumCodec;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

 class EnumStringCodec extends TypeDefinitionAwareCodec<String, EnumTypeDefinition> implements
        EnumCodec<String> {

    private final Set<String> values;

    protected EnumStringCodec(final Optional<EnumTypeDefinition> typeDef) {
        super(typeDef, String.class);
        if(typeDef.isPresent()) {
            final List<EnumPair> enumValues = typeDef.get().getValues();
            final Set<String> valuesBuilder = Sets.newHashSetWithExpectedSize(enumValues.size());
            for( final EnumPair pair: enumValues ) {
                valuesBuilder.add( pair.getName() );
            }
            values = ImmutableSet.copyOf(valuesBuilder);
        } else {
            values = null;
        }

    }

    static TypeDefinitionAwareCodec<?,EnumTypeDefinition> from(final EnumTypeDefinition normalizedType) {
        return new EnumStringCodec(Optional.fromNullable(normalizedType));
    }

    @Override
    public final String deserialize(final String s) {
        if (values != null) {
            Preconditions.checkArgument(values.contains(s), "Invalid value '%s' for enum type. Allowed values are: %s",
                    s, values);
        }
        return s;
    }

    @Override
    public final String serialize(final String data) {
        return data == null ? "" : data;
    }
}