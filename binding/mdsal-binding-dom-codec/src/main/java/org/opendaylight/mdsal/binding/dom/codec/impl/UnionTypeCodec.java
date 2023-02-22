/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeGeneratedUnion;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

final class UnionTypeCodec implements ValueCodec<Object, Object> {
    private final ImmutableSet<UnionValueOptionContext> typeCodecs;
    private final Class<?> unionClass;

    private UnionTypeCodec(final Class<?> unionClass, final List<UnionValueOptionContext> typeCodecs) {
        this.unionClass = requireNonNull(unionClass);
        // Squashes duplicates
        this.typeCodecs = ImmutableSet.copyOf(typeCodecs);
    }

    static UnionTypeCodec of(final Class<?> unionCls, final UnionTypeDefinition unionType,
            final BindingCodecContext codecContext) throws Exception {
        final List<String> unionProperties = extractUnionProperties(codecContext.getRuntimeContext()
            .getTypeWithSchema(unionCls).javaType());
        final List<TypeDefinition<?>> unionTypes = unionType.getTypes();
        verify(unionTypes.size() == unionProperties.size(), "Mismatched union types %s and properties %s",
            unionTypes, unionProperties);

        final List<UnionValueOptionContext> values = new ArrayList<>(unionTypes.size());
        final Iterator<String> it = unionProperties.iterator();
        for (final TypeDefinition<?> subtype : unionTypes) {
            final String getterName = Naming.GETTER_PREFIX + Naming.toFirstUpper(it.next());
            final Method valueGetter = unionCls.getMethod(getterName);
            final Class<?> valueType = valueGetter.getReturnType();
            final ValueCodec<Object, Object> codec = codecContext.getCodec(valueType, subtype);

            values.add(new UnionValueOptionContext(unionCls, valueType, valueGetter, codec));
        }

        return new UnionTypeCodec(unionCls, values);
    }

    private static List<String> extractUnionProperties(final Type type) {
        verify(type instanceof GeneratedTransferObject, "Unexpected runtime type %s", type);

        GeneratedTransferObject gto = (GeneratedTransferObject) type;
        while (true) {
            if (gto instanceof RuntimeGeneratedUnion) {
                return ((RuntimeGeneratedUnion) gto).typePropertyNames();
            }
            gto = verifyNotNull(gto.getSuperType(), "Cannot find union type information for %s", type);
        }
    }

    @Override
    public Object deserialize(final Object input) {
        for (final UnionValueOptionContext member : typeCodecs) {
            final Object ret = member.deserializeUnion(input);
            if (ret != null) {
                return ret;
            }
        }

        throw new IllegalArgumentException(String.format("Failed to construct instance of %s for input %s",
            unionClass, input));
    }

    @Override
    public Object serialize(final Object input) {
        for (final UnionValueOptionContext valCtx : typeCodecs) {
            final Object domValue = valCtx.serialize(input);
            if (domValue != null) {
                return domValue;
            }
        }
        throw new IllegalStateException("No codec matched value " + input);
    }
}
