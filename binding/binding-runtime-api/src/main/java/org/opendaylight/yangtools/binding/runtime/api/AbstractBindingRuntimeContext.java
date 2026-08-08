/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.binding.Rpc;
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.binding.YangData;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runtime Context for Java YANG Binding classes. It provides information derived from the backing effective model,
 * which is not captured in generated classes (and hence cannot be obtained from {@code BindingReflections}.
 */
@Beta
public abstract class AbstractBindingRuntimeContext implements BindingRuntimeContext {
    private final LoadingCache<@NonNull QName, @NonNull Class<? extends BaseIdentity>> identityClasses =
        CacheBuilder.newBuilder().weakValues().build(new CacheLoader<>() {
            @Override
            public Class<? extends BaseIdentity> load(final QName key) {
                final var type = getTypes().identityChild(key);
                if (type == null) {
                    throw new IllegalArgumentException("Supplied QName " + key + " is not a valid identity");
                }
                try {
                    return loadChecked(BaseIdentity.class, type.getIdentifier());
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Required class " + type + " was not found.", e);
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException(key + " resolves to a non-identity class", e);
                }
            }
        });

    private static final Logger LOG = LoggerFactory.getLogger(AbstractBindingRuntimeContext.class);

    @Override
    public final <T extends Augmentation<?>> AugmentRuntimeType getAugmentationDefinition(final Class<T> augClass) {
        return getTypes().lookupRuntimeType(TypeName.ofClass(augClass)) instanceof AugmentRuntimeType augment
            ? augment : null;
    }

    @Override
    public final CompositeRuntimeType getSchemaDefinition(final Class<?> cls) {
        checkArgument(!Augmentation.class.isAssignableFrom(cls), "Supplied class must not be an augmentation (%s is)",
            cls);
        checkArgument(!Action.class.isAssignableFrom(cls), "Supplied class must not be an action (%s is)", cls);
        checkArgument(!Notification.class.isAssignableFrom(cls), "Supplied class must not be a notification (%s is)",
            cls);
        return (CompositeRuntimeType) getTypes().lookupRuntimeType(TypeName.ofClass(cls));
    }

    @Override
    public final ActionRuntimeType getActionDefinition(final Class<? extends Action<?, ?, ?>> cls) {
        return (ActionRuntimeType) getTypes().lookupRuntimeType(TypeName.ofClass(requireNonNull(cls)));
    }

    @Override
    public final RpcRuntimeType getRpcDefinition(final Class<? extends Rpc<?, ?>> cls) {
        return (RpcRuntimeType) getTypes().lookupRuntimeType(TypeName.ofClass(cls));
    }

    @Override
    public final RuntimeType getTypeWithSchema(final Class<?> type) {
        final var ret = getTypes().lookupRuntimeType(TypeName.ofClass(type));
        if (ret == null) {
            throw new IllegalArgumentException("Failed to find schema for " + type);
        }
        return ret;
    }

    @Override
    public final Class<?> getClassForSchema(final Absolute schema) {
        final var child = getTypes().schemaTreeChild(schema);
        if (child == null) {
            throw new IllegalArgumentException("Failed to find binding type for " + schema);
        }
        try {
            return loadClass(child.javaType());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public final Class<? extends BaseIdentity> getIdentityClass(final QName input) {
        try {
            return identityClasses.get(requireNonNull(input));
        } catch (ExecutionException e) {
            Throwables.throwIfUnchecked(e.getCause());
            throw new IllegalStateException("Unexpected error looking up " + input, e);
        }
    }

    @Override
    public final Class<? extends RpcInput<?>> getRpcInput(final QName rpcName) {
        return loadUnchecked(RpcInput.class, getRpc(rpcName).javaType().input());
    }

    @Override
    public final Class<? extends RpcOutput<?>> getRpcOutput(final QName rpcName) {
        return loadUnchecked(RpcOutput.class, getRpc(rpcName).javaType().output());
    }

    private @NonNull RpcRuntimeType getRpc(final QName rpcName) {
        if (getTypes().schemaTreeChild(rpcName) instanceof RpcRuntimeType rpc) {
            return rpc;
        }
        throw new IllegalArgumentException("Failed to find RPC for " + rpcName);
    }

    @Override
    public final Class<? extends YangData<?>> getYangDataClass(final YangDataName templateName) {
        final var yangData = getTypes().lookupYangData(templateName);
        if (yangData == null) {
            throw new IllegalArgumentException("Failed to find YangData for " + templateName);
        }
        return loadUnchecked(YangData.class, yangData.javaType());
    }

    private <T> @NonNull Class<T> loadUnchecked(final Class<? super T> expected, final @NonNull Archetype type) {
        try {
            return loadChecked(expected, type.name());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private <T> @NonNull Class<T> loadChecked(final Class<? super T> expected, final @NonNull TypeName type)
            throws ClassNotFoundException {
        final Class<T> actual = loadClass(type);
        LOG.trace("Loaded {}", actual.asSubclass(expected));
        return actual;
    }
}
