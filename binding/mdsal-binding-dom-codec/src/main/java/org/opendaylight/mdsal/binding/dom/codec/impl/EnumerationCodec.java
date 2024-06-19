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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.impl.ValueTypeCodec.SchemaUnawareCodec;
import org.opendaylight.yangtools.yang.binding.Enumeration;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class EnumerationCodec extends ReflectionBasedCodec implements SchemaUnawareCodec {
    private static final Logger LOG = LoggerFactory.getLogger(EnumerationCodec.class);
    /*
     * Use identity comparison for keys and allow classes to be GCd themselves.
     *
     * Since codecs can (and typically do) hold a direct or indirect strong reference to the class, they need to be also
     * accessed via reference. Using a weak reference could be problematic, because the codec would quite often be only
     * weakly reachable. We therefore use a soft reference, whose implementation guidance is suitable to our use case:
     *
     *     "Virtual machine implementations are, however, encouraged to bias against clearing recently-created or
     *      recently-used soft references."
     */
    private static final Cache<Class<?>, @NonNull EnumerationCodec> CACHE = CacheBuilder.newBuilder().weakKeys()
        .softValues().build();

    private final ImmutableBiMap<String, Enum<?>> nameToEnum;

    private EnumerationCodec(final Class<? extends Enum<?>> enumeration, final Map<String, Enum<?>> nameToEnum) {
        super(enumeration);
        this.nameToEnum = ImmutableBiMap.copyOf(nameToEnum);
    }

    static @NonNull EnumerationCodec of(final Class<?> returnType, final EnumTypeDefinition def)
            throws ExecutionException {
        return CACHE.get(returnType, () -> {
            final Class<? extends Enum<?>> enumType = castType(returnType);

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
        });
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Enum<?>> castType(final Class<?> returnType) {
        checkArgument(Enum.class.isAssignableFrom(returnType));
        return (Class<? extends Enum<?>>) returnType;
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