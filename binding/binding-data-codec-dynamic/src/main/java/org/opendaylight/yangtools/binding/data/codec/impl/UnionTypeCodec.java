/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
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
            final BindingCodecContext codecContext) throws NoSuchMethodException {
        final var runtimeType = codecContext.getRuntimeContext().getTypeWithSchema(unionCls).javaType();
        if (!(runtimeType instanceof UnionTypeObjectArchetype unionRuntimeType)) {
            throw new VerifyException("Unexpected runtime type " + runtimeType);
        }
        final var unionProperties = unionRuntimeType.typePropertyNames();
        final var unionTypes = unionType.getTypes();
        verify(unionTypes.size() == unionProperties.size(), "Mismatched union types %s and properties %s",
            unionTypes, unionProperties);

        final var values = new ArrayList<UnionValueOptionContext>(unionTypes.size());
        final var it = unionProperties.iterator();
        for (var subtype : unionTypes) {
            final var getterName = Naming.GETTER_PREFIX + Naming.toFirstUpper(it.next());
            final var valueGetter = unionCls.getMethod(getterName);
            final var valueType = valueGetter.getReturnType();
            final var codec = codecContext.getCodec(valueType, subtype);

            values.add(new UnionValueOptionContext(unionCls, valueType, valueGetter, codec));
        }

        return new UnionTypeCodec(unionCls, values);
    }

    @Override
    public Object deserialize(final Object input) {
        for (var member : typeCodecs) {
            final var ret = member.deserializeUnion(input);
            if (ret != null) {
                return ret;
            }
        }

        throw new IllegalArgumentException("Failed to construct instance of %s for input %s".formatted(
            unionClass, input));
    }

    @Override
    public Object serialize(final Object input) {
        for (var valCtx : typeCodecs) {
            final var domValue = valCtx.serialize(input);
            if (domValue != null) {
                return domValue;
            }
        }
        throw new IllegalStateException("No codec matched value " + input);
    }
}
