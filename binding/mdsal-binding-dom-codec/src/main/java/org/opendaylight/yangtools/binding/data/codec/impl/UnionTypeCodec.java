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
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

final class UnionTypeCodec extends ReflectionBasedCodec {

    private final Codec<Object, Object> identityrefCodec;
    private final ImmutableSet<UnionValueOptionContext> typeCodecs;
    private final Constructor<?> charConstructor;

    private UnionTypeCodec(final Class<?> unionCls,final Set<UnionValueOptionContext> codecs,
                           @Nullable final Codec<Object, Object> identityrefCodec) {
        super(unionCls);
        this.identityrefCodec = identityrefCodec;
        try {
            charConstructor = unionCls.getConstructor(char[].class);
            typeCodecs = ImmutableSet.copyOf(codecs);
        } catch (NoSuchMethodException | SecurityException e) {
           throw new IllegalStateException("Required constructor is not available.",e);
        }
    }

    static Callable<UnionTypeCodec> loader(final Class<?> unionCls, final UnionTypeDefinition unionType,
                                           final BindingCodecContext bindingCodecContext) {
        return new Callable<UnionTypeCodec>() {
            @Override
            public UnionTypeCodec call() throws NoSuchMethodException, SecurityException {
                Codec<Object, Object> identityrefCodec = null;
                Set<UnionValueOptionContext> values = new HashSet<>();
                for (TypeDefinition<?> subtype : unionType.getTypes()) {
                    String methodName = "get" + BindingMapping.getClassName(subtype.getQName());
                    Method valueGetter = unionCls.getMethod(methodName);
                    Class<?> valueType = valueGetter.getReturnType();
                    Codec<Object, Object> valueCodec = bindingCodecContext.getCodec(valueType, subtype);
                    if (Class.class.equals(valueType)) {
                        identityrefCodec = valueCodec;
                    }
                    values.add(new UnionValueOptionContext(valueType,valueGetter, valueCodec));
                }
                return new UnionTypeCodec(unionCls, values, identityrefCodec);
            }
        };
    }

    @Override
    public Object deserialize(final Object input) {
        if (identityrefCodec != null) {
            try {
                Object identityref = identityrefCodec.deserialize(input);
                return getTypeClass().getConstructor(Class.class).newInstance(identityref);
            } catch (UncheckedExecutionException | ExecutionError e) {
                // ignore this exception caused by deserialize()
            } catch (NoSuchMethodException e) {
                // caused by getContructor(). this case shouldn't happen.
                throw new IllegalStateException("Could not construct instance", e);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                // ignore this exception caused by newInstance()
            }
        }
        try {
            if (input instanceof byte[]) {
                return charConstructor.newInstance(BaseEncoding.base64().encode((byte[]) input).toCharArray());
            } else {
                return charConstructor.newInstance((input.toString().toCharArray()));
            }
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
