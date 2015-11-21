/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

final class UnionTypeCodec extends ReflectionBasedCodec {

    private final ImmutableSet<UnionValueOptionContext> typeCodecs;
    private final Constructor<?> charConstructor;

    private UnionTypeCodec(final Class<?> unionCls,final Set<UnionValueOptionContext> codecs) {
        super(unionCls);
        try {
            charConstructor = unionCls.getConstructor(char[].class);
            typeCodecs = ImmutableSet.copyOf(codecs);
        } catch (NoSuchMethodException | SecurityException e) {
           throw new IllegalStateException("Required constructor is not available.",e);
        }
    }

    static Callable<UnionTypeCodec> loader(final Class<?> unionCls, final UnionTypeDefinition unionType) {
        return new Callable<UnionTypeCodec>() {
            @Override
            public UnionTypeCodec call() throws NoSuchMethodException, SecurityException {
                Set<UnionValueOptionContext> values = new HashSet<>();
                for(TypeDefinition<?> subtype : unionType.getTypes()) {
                    String methodName = "get" + BindingMapping.getClassName(subtype.getQName());
                    Method valueGetter = unionCls.getMethod(methodName);
                    Class<?> valueType = valueGetter.getReturnType();
                    Codec<Object, Object> valueCodec = UnionTypeCodec.getCodecForType(valueType, subtype);
                    values.add(new UnionValueOptionContext(valueType,valueGetter, valueCodec));
                }
                return new UnionTypeCodec(unionCls, values);
            }
        };
    }

    private static Codec<Object, Object> getCodecForType(final Class<?> valueType, final TypeDefinition<?> subtype) {
        if (subtype.getBaseType() instanceof UnionTypeDefinition) {
            try {
                return UnionTypeCodec.loader(valueType, (UnionTypeDefinition) subtype.getBaseType()).call();
            } catch (final Exception e) {
                throw new IllegalStateException("Could not construct Union Type Codec");
            }
        } else {
            return ValueTypeCodec.getCodecFor(valueType, subtype);
        }
    }

    @Override
    public Object deserialize(final Object input) {
        try {
            return charConstructor.newInstance((input.toString().toCharArray()));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Could not construct instance",e);
        }
    }

    @Override
    public Object serialize(final Object input) {
        if (input != null) {
            for (UnionValueOptionContext valCtx : typeCodecs) {
                Object domValue = valCtx.serialize(input);
                if (domValue != null) {
                    return domValue;
                }
            }
        }
        return null;
    }

}
