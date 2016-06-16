/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.yangtools.binding.data.codec.util.AugmentationReader;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LazyDataObject<D extends DataObject> implements InvocationHandler, AugmentationReader {

    private static final Logger LOG = LoggerFactory.getLogger(LazyDataObject.class);
    private static final String GET_IMPLEMENTED_INTERFACE = "getImplementedInterface";
    private static final String TO_STRING = "toString";
    private static final String EQUALS = "equals";
    private static final String GET_AUGMENTATION = "getAugmentation";
    private static final String HASHCODE = "hashCode";
    private static final String AUGMENTATIONS = "augmentations";
    private static final Object NULL_VALUE = new Object();

    private final ConcurrentHashMap<Method, Object> cachedData = new ConcurrentHashMap<>();
    private final NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> data;
    private final DataObjectCodecContext<D,?> context;

    private volatile ImmutableMap<Class<? extends Augmentation<?>>, Augmentation<?>> cachedAugmentations = null;
    private volatile Integer cachedHashcode = null;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    LazyDataObject(final DataObjectCodecContext<D,?> ctx, final NormalizedNodeContainer data) {
        this.context = Preconditions.checkNotNull(ctx, "Context must not be null");
        this.data = Preconditions.checkNotNull(data, "Data must not be null");
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (method.getParameterTypes().length == 0) {
            final String name = method.getName();
            if (GET_IMPLEMENTED_INTERFACE.equals(name)) {
                return context.getBindingClass();
            } else if (TO_STRING.equals(name)) {
                return bindingToString();
            } else if (HASHCODE.equals(name)) {
                return bindingHashCode();
            } else if (AUGMENTATIONS.equals(name)) {
                return getAugmentationsImpl();
            }
            return getBindingData(method);
        } else if (GET_AUGMENTATION.equals(method.getName())) {
            return getAugmentationImpl((Class<?>) args[0]);
        } else if (EQUALS.equals(method.getName())) {
            return bindingEquals(args[0]);
        }
        throw new UnsupportedOperationException("Unsupported method " + method);
    }

    private boolean bindingEquals(final Object other) {
        if (other == null) {
            return false;
        }
        if (!context.getBindingClass().isAssignableFrom(other.getClass())) {
            return false;
        }
        try {
            for (final Method m : context.getHashCodeAndEqualsMethods()) {
                final Object thisValue = getBindingData(m);
                final Object otherValue = m.invoke(other);
                /*
                *   added for valid byte array comparison, when list key type is binary
                *   deepEquals is not used since it does excessive amount of instanceof calls.
                */
                if (thisValue instanceof byte[] && otherValue instanceof byte[]) {
                    if (!Arrays.equals((byte[]) thisValue, (byte[]) otherValue)) {
                        return false;
                    }
                } else if (!Objects.equals(thisValue, otherValue)){
                    return false;
                }
            }

            if (Augmentable.class.isAssignableFrom(context.getBindingClass())) {
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

    private static Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAllAugmentations(Object dataObject) {
        if (dataObject instanceof AugmentationReader) {
            return ((AugmentationReader) dataObject).getAugmentations(dataObject);
        } else if (dataObject instanceof Augmentable<?>){
            return BindingReflections.getAugmentations((Augmentable<?>) dataObject);
        }

        throw new IllegalArgumentException("Unable to get all augmentations from " + dataObject);
    }

    private Integer bindingHashCode() {
        final Integer ret = cachedHashcode;
        if (ret != null) {
            return ret;
        }

        final int prime = 31;
        int result = 1;
        for (final Method m : context.getHashCodeAndEqualsMethods()) {
            final Object value = getBindingData(m);
            result = prime * result + Objects.hashCode(value);
        }
        if (Augmentable.class.isAssignableFrom(context.getBindingClass())) {
            result = prime * result + (getAugmentationsImpl().hashCode());
        }
        cachedHashcode = result;
        return result;
    }

    private Object getBindingData(final Method method) {
        Object cached = cachedData.get(method);
        if (cached == null) {
            final Object readedValue = context.getBindingChildValue(method, data);
            if (readedValue == null) {
                cached = NULL_VALUE;
            } else {
                cached = readedValue;
            }
            cachedData.putIfAbsent(method, cached);
        }

        return cached == NULL_VALUE ? null : cached;
    }

    private Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentationsImpl() {
        ImmutableMap<Class<? extends Augmentation<?>>, Augmentation<?>> ret = cachedAugmentations;
        if (ret == null) {
            synchronized (this) {
                ret = cachedAugmentations;
                if (ret == null) {
                    ret = ImmutableMap.copyOf(context.getAllAugmentationsFrom(data));
                    cachedAugmentations = ret;
                }
            }
        }

        return ret;
    }

    @Override
    public Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(final Object obj) {
        Preconditions.checkArgument(this == Proxy.getInvocationHandler(obj),
                "Supplied object is not associated with this proxy handler");

        return getAugmentationsImpl();
    }

    private Object getAugmentationImpl(final Class<?> cls) {
        final ImmutableMap<Class<? extends Augmentation<?>>, Augmentation<?>> aug = cachedAugmentations;
        if (aug != null) {
            return aug.get(cls);
        }
        Preconditions.checkNotNull(cls,"Supplied augmentation must not be null.");

        @SuppressWarnings({"unchecked","rawtypes"})
        final Optional<DataContainerCodecContext<?,?>> augCtx= context.possibleStreamChild((Class) cls);
        if(augCtx.isPresent()) {
            final Optional<NormalizedNode<?, ?>> augData = data.getChild(augCtx.get().getDomPathArgument());
            if (augData.isPresent()) {
                return augCtx.get().deserialize(augData.get());
            }
        }
        return null;
    }

    public String bindingToString() {
        final ToStringHelper helper = MoreObjects.toStringHelper(context.getBindingClass()).omitNullValues();

        for (final Method m :context.getHashCodeAndEqualsMethods()) {
            helper.add(m.getName(), getBindingData(m));
        }
        if (Augmentable.class.isAssignableFrom(context.getBindingClass())) {
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
