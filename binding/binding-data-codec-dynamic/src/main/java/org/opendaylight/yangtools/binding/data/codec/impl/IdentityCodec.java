/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.data.codec.api.BindingIdentityCodec;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.yangtools.binding.runtime.api.IdentityRuntimeType;
import org.opendaylight.yangtools.yang.common.QName;

final class IdentityCodec extends AbstractValueCodec<QName, BaseIdentity> implements BindingIdentityCodec {
    private final LoadingCache<@NonNull QName, @NonNull BaseIdentity> values = CacheBuilder.newBuilder()
        .build(new CacheLoader<>() {
            @Override
            public BaseIdentity load(final QName key) {
                final var clazz = context.getIdentityClass(key);
                final Field field;
                try {
                    field = clazz.getField(Naming.VALUE_STATIC_FIELD_NAME);
                } catch (NoSuchFieldException e) {
                    throw new LinkageError(clazz + " does not define required field " + Naming.VALUE_STATIC_FIELD_NAME,
                        e);
                }
                if (!Modifier.isStatic(field.getModifiers())) {
                    throw new LinkageError(field + " is not static");
                }

                final Object value;
                try {
                    value = clazz.cast(field.get(null));
                } catch (IllegalAccessException e) {
                    throw new LinkageError(field + " is not accesssible", e);
                }
                if (value == null) {
                    throw new LinkageError(field + " is null");
                }
                try {
                    return clazz.cast(value);
                } catch (ClassCastException e) {
                    throw new LinkageError(field + " value " + value + " has illegal type", e);
                }
            }
        });
    private final LoadingCache<@NonNull Class<? extends BaseIdentity>, @NonNull QName> qnames =
        // Note: weak keys because it is the user who is supplying implemented contract
        CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<>() {
            @Override
            public QName load(final Class<? extends BaseIdentity> key) {
                final var schema = context.getTypeWithSchema(key);
                if (schema instanceof IdentityRuntimeType identitySchema) {
                    return identitySchema.statement().argument();
                }
                throw new IllegalStateException("Unexpected schema " + schema + " for " + key);
            }
        });

    private final BindingRuntimeContext context;

    IdentityCodec(final BindingRuntimeContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    protected BaseIdentity deserializeImpl(final QName input) {
        return toBinding(input);
    }

    @Override
    protected QName serializeImpl(final BaseIdentity input) {
        return fromBinding(input);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BaseIdentity> T toBinding(final QName qname) {
        try {
            return (T) values.get(requireNonNull(qname));
        } catch (ExecutionException e) {
            Throwables.throwIfUnchecked(e.getCause());
            throw new IllegalStateException("Unexpected error translating " + qname, e);
        }
    }

    @Override
    public QName fromBinding(final BaseIdentity bindingValue) {
        try {
            return qnames.get(bindingValue.implementedInterface());
        } catch (ExecutionException e) {
            Throwables.throwIfUnchecked(e.getCause());
            throw new IllegalStateException("Unexpected error translating " + bindingValue, e);
        }
    }
}
