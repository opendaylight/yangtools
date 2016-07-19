/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

final class UnionTypeCodec extends ReflectionBasedCodec {
    private final ImmutableSet<UnionValueOptionContext> typeCodecs;

    private UnionTypeCodec(final Class<?> unionCls,final Set<UnionValueOptionContext> codecs) {
        super(unionCls);
        typeCodecs = ImmutableSet.copyOf(codecs);
    }

    static Callable<UnionTypeCodec> loader(final Class<?> unionCls, final UnionTypeDefinition unionType,
                                           final BindingCodecContext bindingCodecContext) {
        return () -> {
            final Set<UnionValueOptionContext> values = new LinkedHashSet<>();
            for (TypeDefinition<?> subtype : unionType.getTypes()) {
                Method valueGetter = unionCls.getMethod("get" + BindingMapping.getClassName(subtype.getQName()));
                Class<?> valueType = valueGetter.getReturnType();
                Codec<Object, Object> valueCodec = bindingCodecContext.getCodec(valueType, subtype);
                values.add(new UnionValueOptionContext(unionCls, valueType, valueGetter, valueCodec));
            }
            return new UnionTypeCodec(unionCls, values);
        };
    }

    @Override
    public Object deserialize(final Object input) {
        for (UnionValueOptionContext member : typeCodecs) {
            final Object ret = member.deserializeUnion(input);
            if (ret != null) {
                return ret;
            }
        }

        throw new IllegalArgumentException(String.format("Failed to construct instance of %s for input %s",
            getTypeClass(), input));
    }

    @Override
    public Object serialize(final Object input) {
        if (input != null) {
            for (UnionValueOptionContext valCtx : typeCodecs) {
                final Object domValue = valCtx.serialize(input);
                if (domValue != null) {
                    return domValue;
                }
            }
        }
        return null;
    }
}
