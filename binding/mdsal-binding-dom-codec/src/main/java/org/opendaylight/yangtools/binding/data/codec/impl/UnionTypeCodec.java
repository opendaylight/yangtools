/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.BaseEncoding;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UnionTypeCodec extends ReflectionBasedCodec {
    private static final MethodType CHARARRAY_LOOKUP_TYPE = MethodType.methodType(void.class, char[].class);
    private static final MethodType CHARARRAY_INVOKE_TYPE = MethodType.methodType(Object.class, char[].class);
    private static final Logger LOG = LoggerFactory.getLogger(UnionTypeCodec.class);

    private final ImmutableSet<UnionValueOptionContext> typeCodecs;
    private final MethodHandle charConstructor;

    private UnionTypeCodec(final Class<?> unionCls,final Set<UnionValueOptionContext> codecs) {
        super(unionCls);

        try {
            charConstructor = MethodHandles.publicLookup().findConstructor(unionCls, CHARARRAY_LOOKUP_TYPE)
                    .asType(CHARARRAY_INVOKE_TYPE);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalStateException("Failed to instantiate handle for constructor", e);
        }

        typeCodecs = ImmutableSet.copyOf(codecs);
    }

    static Callable<UnionTypeCodec> loader(final Class<?> unionCls, final UnionTypeDefinition unionType,
                                           final BindingCodecContext bindingCodecContext) {
        return new Callable<UnionTypeCodec>() {
            @Override
            public UnionTypeCodec call() throws NoSuchMethodException, SecurityException {
                Set<UnionValueOptionContext> values = new HashSet<>();
                for (TypeDefinition<?> subtype : unionType.getTypes()) {
                    String methodName = "get" + BindingMapping.getClassName(subtype.getQName());
                    Method valueGetter = unionCls.getMethod(methodName);
                    Class<?> valueType = valueGetter.getReturnType();
                    Codec<Object, Object> valueCodec = bindingCodecContext.getCodec(valueType, subtype);
                    values.add(new UnionValueOptionContext(unionCls, valueType, valueGetter, valueCodec));
                }
                return new UnionTypeCodec(unionCls, values);
            }
        };
    }

    private Object deserializeString(final Object input) {
        final String str = input instanceof byte[] ? BaseEncoding.base64().encode((byte[]) input) : input.toString();
        try {
            return charConstructor.invokeExact(str.toCharArray());
        } catch (Throwable e) {
            throw new IllegalStateException("Could not construct instance", e);
        }
    }

    @Override
    public Object deserialize(final Object input) {
        for (UnionValueOptionContext member : typeCodecs) {
            final Object ret = member.deserializeUnion(input);
            if (ret != null) {
                return ret;
            }
        }

        LOG.warn("Union class {} value {} failed to deserialize efficiently, falling back to String-based instantiation",
            getTypeClass(), input);
        return deserializeString(input);
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
