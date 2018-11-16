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
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.AUGMENTABLE_AUGMENTATION_NAME;
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.DATA_CONTAINER_GET_IMPLEMENTED_INTERFACE_NAME;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.util.AugmentationReader;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LazyDataObject<D extends DataObject> implements InvocationHandler, AugmentationReader {

    private static final Logger LOG = LoggerFactory.getLogger(LazyDataObject.class);
    private static final String TO_STRING = "toString";
    private static final String EQUALS = "equals";
    private static final String HASHCODE = "hashCode";
    private static final String AUGMENTATIONS = "augmentations";
    private static final @NonNull Object NULL_VALUE = new Object();

    // Method.getName() is guaranteed to be interned and all getter methods have zero arguments, name is sufficient to
    // identify the data, skipping Method.hashCode() computation.
    private final ConcurrentHashMap<String, Object> cachedData = new ConcurrentHashMap<>();
    private final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> data;
    private final DataObjectCodecContext<D,?> context;

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<LazyDataObject, ImmutableMap> CACHED_AUGMENTATIONS_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(LazyDataObject.class, ImmutableMap.class, "cachedAugmentations");
    private volatile ImmutableMap<Class<? extends Augmentation<?>>, Augmentation<?>> cachedAugmentations = null;
    private volatile Integer cachedHashcode = null;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    LazyDataObject(final DataObjectCodecContext<D,?> ctx, final NormalizedNodeContainer data) {
        this.context = requireNonNull(ctx, "Context must not be null");
        this.data = requireNonNull(data, "Data must not be null");
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        switch (method.getParameterCount()) {
            case 0:
                final String methodName = method.getName();
                switch (methodName) {
                    case DATA_CONTAINER_GET_IMPLEMENTED_INTERFACE_NAME:
                        return context.getBindingClass();
                    case TO_STRING:
                        return bindingToString();
                    case HASHCODE:
                        return bindingHashCode();
                    case AUGMENTATIONS:
                        return getAugmentationsImpl();
                    default:
                        return method.isDefault() ? nonnullBindingData(methodName) : getBindingData(methodName);
                }
            case 1:
                switch (method.getName()) {
                    case AUGMENTABLE_AUGMENTATION_NAME:
                        return getAugmentationImpl((Class<?>) args[0]);
                    case EQUALS:
                        return bindingEquals(args[0]);
                    default:
                        break;
                }
                break;
            default:
                break;
        }

        throw new UnsupportedOperationException("Unsupported method " + method);
    }

    private boolean bindingEquals(final Object other) {
        if (other == null) {
            return false;
        }
        final Class<D> bindingClass = context.getBindingClass();
        if (!bindingClass.isAssignableFrom(other.getClass())) {
            return false;
        }
        try {
            for (final Method m : context.propertyMethods()) {
                final Object thisValue = getBindingData(m.getName());
                final Object otherValue = m.invoke(other);
                /*
                 *   added for valid byte array comparison, when list key type is binary
                 *   deepEquals is not used since it does excessive amount of instanceof calls.
                 */
                if (thisValue instanceof byte[] && otherValue instanceof byte[]) {
                    if (!Arrays.equals((byte[]) thisValue, (byte[]) otherValue)) {
                        return false;
                    }
                } else if (!Objects.equals(thisValue, otherValue)) {
                    return false;
                }
            }

            if (Augmentable.class.isAssignableFrom(bindingClass)) {
                if (!getAugmentationsImpl().equals(getAllAugmentations(other))) {
                    return false;
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOG.warn("Can not determine equality of {} and {}", this, other, e);
            return false;
        }
        return true;
    }

    private static Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAllAugmentations(final Object dataObject) {
        if (dataObject instanceof AugmentationReader) {
            return ((AugmentationReader) dataObject).getAugmentations(dataObject);
        } else if (dataObject instanceof Augmentable<?>) {
            return BindingReflections.getAugmentations((Augmentable<?>) dataObject);
        }

        throw new IllegalArgumentException("Unable to get all augmentations from " + dataObject);
    }

    private Integer bindingHashCode() {
        final Integer cached = cachedHashcode;
        if (cached != null) {
            return cached;
        }

        final int prime = 31;
        int result = 1;
        for (final Method m : context.propertyMethods()) {
            final Object value = getBindingData(m.getName());
            result = prime * result + Objects.hashCode(value);
        }
        if (Augmentable.class.isAssignableFrom(context.getBindingClass())) {
            result = prime * result + getAugmentationsImpl().hashCode();
        }
        final Integer ret = result;
        cachedHashcode = ret;
        return ret;
    }

    private Object nonnullBindingData(final String methodName) {
        final Object value = getBindingData(context.getterNameForNonnullName(methodName));
        return value != null ? value : ImmutableList.of();
    }

    // Internal invocation, can only target getFoo() methods
    private Object getBindingData(final String methodName) {
        final Object cached = cachedData.get(methodName);
        if (cached != null) {
            return unmaskNull(cached);
        }

        final Object value = context.getBindingChildValue(methodName, data);
        final Object raced = cachedData.putIfAbsent(methodName, value == null ? NULL_VALUE : value);
        // If we raced we need to return previously-stored value
        return raced != null ? unmaskNull(raced) : value;
    }

    private static Object unmaskNull(final @NonNull Object masked) {
        return masked == NULL_VALUE ? null : masked;
    }

    private Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentationsImpl() {
        ImmutableMap<Class<? extends Augmentation<?>>, Augmentation<?>> local = cachedAugmentations;
        if (local != null) {
            return local;
        }

        local = ImmutableMap.copyOf(context.getAllAugmentationsFrom(data));
        return CACHED_AUGMENTATIONS_UPDATER.compareAndSet(this, null, local) ? local : cachedAugmentations;
    }

    @Override
    public Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(final Object obj) {
        checkArgument(this == Proxy.getInvocationHandler(obj),
                "Supplied object is not associated with this proxy handler");

        return getAugmentationsImpl();
    }

    private Object getAugmentationImpl(final Class<?> cls) {
        requireNonNull(cls, "Supplied augmentation must not be null.");

        final ImmutableMap<Class<? extends Augmentation<?>>, Augmentation<?>> aug = cachedAugmentations;
        if (aug != null) {
            return aug.get(cls);
        }

        @SuppressWarnings({"unchecked","rawtypes"})
        final Optional<DataContainerCodecContext<?, ?>> optAugCtx = context.possibleStreamChild((Class) cls);
        if (optAugCtx.isPresent()) {
            final DataContainerCodecContext<?, ?> augCtx = optAugCtx.get();
            // Due to binding specification not representing grouping instantiations we can end up having the same
            // augmentation applied to a grouping multiple times. While these augmentations have the same shape, they
            // are still represented by distinct binding classes and therefore we need to make sure the result matches
            // the augmentation the user is requesting -- otherwise a strict receiver would end up with a cryptic
            // ClassCastException.
            if (cls.isAssignableFrom(augCtx.getBindingClass())) {
                final Optional<NormalizedNode<?, ?>> augData = data.getChild(augCtx.getDomPathArgument());
                if (augData.isPresent()) {
                    return augCtx.deserialize(augData.get());
                }
            }
        }
        return null;
    }

    public String bindingToString() {
        final Class<D> bindingClass = context.getBindingClass();
        final ToStringHelper helper = MoreObjects.toStringHelper(bindingClass).omitNullValues();

        for (final Method m : context.propertyMethods()) {
            final String methodName = m.getName();
            helper.add(methodName, getBindingData(methodName));
        }
        if (Augmentable.class.isAssignableFrom(bindingClass)) {
            helper.add("augmentations", getAugmentationsImpl());
        }
        return helper.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + context.hashCode();
        result = prime * result + data.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LazyDataObject<?> other = (LazyDataObject<?>) obj;
        return Objects.equals(context, other.context) && Objects.equals(data, other.data);
    }
}
