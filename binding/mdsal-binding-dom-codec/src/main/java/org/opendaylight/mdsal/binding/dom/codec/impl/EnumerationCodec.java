/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.dom.codec.impl.ValueTypeCodec.SchemaUnawareCodec;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

final class EnumerationCodec extends ReflectionBasedCodec implements SchemaUnawareCodec {
    private final ImmutableBiMap<String, Enum<?>> yangValueToBinding;

    EnumerationCodec(final Class<? extends Enum<?>> enumeration, final ImmutableBiMap<String, Enum<?>> schema) {
        super(enumeration);
        yangValueToBinding = requireNonNull(schema);
    }

    static Callable<EnumerationCodec> loader(final Class<?> returnType, final EnumTypeDefinition enumSchema) {
        checkArgument(Enum.class.isAssignableFrom(returnType));
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final Class<? extends Enum<?>> enumType = (Class) returnType;
        return () -> {
            final BiMap<String, String> identifierToYang = BindingMapping.mapEnumAssignedNames(
                enumSchema.getValues().stream().map(EnumPair::getName).collect(Collectors.toList())).inverse();

            final Builder<String, Enum<?>> builder = ImmutableBiMap.builder();
            for (Enum<?> enumValue : enumType.getEnumConstants()) {
                final String yangName = identifierToYang.get(enumValue.name());
                checkState(yangName != null, "Failed to find enumeration constant %s in mapping %s", enumValue,
                        identifierToYang);
                builder.put(yangName, enumValue);
            }

            return new EnumerationCodec(enumType, builder.build());
        };
    }

    @Override
    public Object deserialize(final Object input) {
        Enum<?> value = yangValueToBinding.get(input);
        checkArgument(value != null, "Invalid enumeration value %s. Valid values are %s", input,
                yangValueToBinding.keySet());
        return value;
    }

    @Override
    public Object serialize(final Object input) {
        checkArgument(getTypeClass().isInstance(input), "Input must be instance of %s", getTypeClass());
        return yangValueToBinding.inverse().get(input);
    }
}