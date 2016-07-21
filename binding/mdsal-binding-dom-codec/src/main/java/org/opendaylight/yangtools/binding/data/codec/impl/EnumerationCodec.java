/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.opendaylight.yangtools.binding.data.codec.impl.ValueTypeCodec.SchemaUnawareCodec;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

final class EnumerationCodec extends ReflectionBasedCodec implements SchemaUnawareCodec {
    private final ImmutableBiMap<String, Enum<?>> yangValueToBinding;

    EnumerationCodec(final Class<? extends Enum<?>> enumeration, final Map<String, Enum<?>> schema) {
        super(enumeration);
        yangValueToBinding = ImmutableBiMap.copyOf(schema);
    }

    static Callable<EnumerationCodec> loader(final Class<?> returnType, final EnumTypeDefinition enumSchema) {
        Preconditions.checkArgument(Enum.class.isAssignableFrom(returnType));
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final Class<? extends Enum<?>> enumType = (Class) returnType;
        return () -> {
            Map<String, Enum<?>> nameToValue = new HashMap<>();
            for (Enum<?> enumValue : enumType.getEnumConstants()) {
                nameToValue.put(enumValue.toString(), enumValue);
            }
            Map<String, Enum<?>> yangNameToBinding = new HashMap<>();
            for (EnumPair yangValue : enumSchema.getValues()) {
                final String bindingName = BindingMapping.getClassName(yangValue.getName());
                final Enum<?> bindingVal = nameToValue.get(bindingName);
                yangNameToBinding.put(yangValue.getName(), bindingVal);
            }
            return new EnumerationCodec(enumType, yangNameToBinding);
        };
    }

    @Override
    public Object deserialize(final Object input) {
        Enum<?> value = yangValueToBinding.get(input);
        Preconditions.checkArgument(value != null, "Invalid enumeration value %s. Valid values are %s", input,
                yangValueToBinding.keySet());
        return value;
    }

    @Override
    public Object serialize(final Object input) {
        Preconditions.checkArgument(getTypeClass().isInstance(input), "Input must be instance of %s", getTypeClass());
        return yangValueToBinding.inverse().get(input);
    }
}