/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * Runtime Context for Java YANG Binding classes. It provides information derived from the backing effective model,
 * which is not captured in generated classes (and hence cannot be obtained from {@code BindingReflections}.
 */
@Beta
public abstract class AbstractBindingRuntimeContext implements BindingRuntimeContext {
    private final LoadingCache<QName, Class<?>> identityClasses = CacheBuilder.newBuilder().weakValues().build(
        new CacheLoader<QName, Class<?>>() {
            @Override
            public Class<?> load(final QName key) {
                final var type = getTypes().findIdentity(key).orElseThrow(
                    () -> new IllegalArgumentException("Supplied QName " + key + " is not a valid identity"));
                try {
                    return loadClass(type.getIdentifier());
                } catch (final ClassNotFoundException e) {
                    throw new IllegalArgumentException("Required class " + type + " was not found.", e);
                }
            }
        });

    @Override
    public final <T extends Augmentation<?>> AugmentRuntimeType getAugmentationDefinition(final Class<T> augClass) {
        return getTypes().findSchema(JavaTypeName.create(augClass))
            .filter(AugmentRuntimeType.class::isInstance)
            .map(AugmentRuntimeType.class::cast)
            .orElse(null);
    }

    @Override
    public final CompositeRuntimeType getSchemaDefinition(final Class<?> cls) {
        checkArgument(!Augmentation.class.isAssignableFrom(cls), "Supplied class must not be an augmentation (%s is)",
            cls);
        checkArgument(!Action.class.isAssignableFrom(cls), "Supplied class must not be an action (%s is)", cls);
        checkArgument(!Notification.class.isAssignableFrom(cls), "Supplied class must not be a notification (%s is)",
            cls);
        return (CompositeRuntimeType) getTypes().findSchema(JavaTypeName.create(cls)).orElse(null);
    }

    @Override
    public final ActionRuntimeType getActionDefinition(final Class<? extends Action<?, ?, ?>> cls) {
        return (ActionRuntimeType) getTypes().findSchema(JavaTypeName.create(cls)).orElse(null);
    }

    @Override
    public final RuntimeType getTypeWithSchema(final Class<?> type) {
        return getTypes().findSchema(JavaTypeName.create(type))
            .orElseThrow(() -> new IllegalArgumentException("Failed to find schema for " + type));
    }

    @Override
    public final Class<?> getClassForSchema(final Absolute schema) {
        final var child = getTypes().schemaTreeChild(schema);
        checkArgument(child != null, "Failed to find binding type for %s", schema);
        return loadClass(child);
    }

    @Override
    public final Class<?> getIdentityClass(final QName input) {
        return identityClasses.getUnchecked(input);
    }

    @Override
    public final Class<? extends RpcInput> getRpcInput(final QName rpcName) {
        return loadClass(getTypes().findRpcInput(rpcName)
            .orElseThrow(() -> new IllegalArgumentException("Failed to find RpcInput for " + rpcName)))
            .asSubclass(RpcInput.class);
    }

    @Override
    public final Class<? extends RpcOutput> getRpcOutput(final QName rpcName) {
        return loadClass(getTypes().findRpcOutput(rpcName)
            .orElseThrow(() -> new IllegalArgumentException("Failed to find RpcOutput for " + rpcName)))
            .asSubclass(RpcOutput.class);
    }

    private Class<?> loadClass(final RuntimeType type) {
        try {
            return loadClass(type.javaType());
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
