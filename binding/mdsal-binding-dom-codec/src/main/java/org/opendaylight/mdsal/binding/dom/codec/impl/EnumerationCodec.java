/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.dom.codec.impl.ValueTypeCodec.SchemaUnawareCodec;
import org.opendaylight.yangtools.yang.binding.Enumeration;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class EnumerationCodec extends ReflectionBasedCodec implements SchemaUnawareCodec {
    private static final Logger LOG = LoggerFactory.getLogger(EnumerationCodec.class);

    private final ImmutableBiMap<String, Enum<?>> nameToEnum;

    EnumerationCodec(final Class<? extends Enum<?>> enumeration, final Map<String, Enum<?>> nameToEnum) {
        super(enumeration);
        this.nameToEnum = ImmutableBiMap.copyOf(nameToEnum);
    }

    static Callable<EnumerationCodec> loader(final Class<?> returnType, final EnumTypeDefinition def) {
        checkArgument(Enum.class.isAssignableFrom(returnType));
        @SuppressWarnings("unchecked")
        final Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) returnType;
        return () -> {
            final Map<String, Enum<?>> mapping = Maps.uniqueIndex(Arrays.asList(enumType.getEnumConstants()),
                value -> {
                    checkArgument(value instanceof Enumeration,
                        "Enumeration constant %s.%s is not implementing Enumeration", enumType.getName(), value);
                    return ((Enumeration) value).getName();
                });

            // Check if mapping is a bijection
            final Set<String> assignedNames =  def.getValues().stream().map(EnumPair::getName)
                    .collect(Collectors.toSet());
            for (String name : assignedNames) {
                if (!mapping.containsKey(name)) {
                    LOG.warn("Enumeration {} does not contain assigned name '{}' from {}", enumType, name, def);
                }
            }
            for (String name : mapping.keySet()) {
                if (!assignedNames.contains(name)) {
                    LOG.warn("Enumeration {} contains assigned name '{}' not covered by {}", enumType, name, def);
                }
            }

            return new EnumerationCodec(enumType, mapping);
        };
    }

    @Override
    public Enum<?> deserialize(final Object input) {
        checkArgument(input instanceof String, "Input %s is not a String", input);
        final Enum<?> value = nameToEnum.get(input);
        checkArgument(value != null, "Invalid enumeration value %s. Valid values are %s", input, nameToEnum.keySet());
        return value;
    }

    @Override
    public String serialize(final Object input) {
        checkArgument(getTypeClass().isInstance(input), "Input %s is not a instance of %s", input, getTypeClass());
        return requireNonNull(nameToEnum.inverse().get(input));
    }
}