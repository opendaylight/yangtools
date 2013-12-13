/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import java.io.ObjectInputStream.GetField;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.Checksum;

import org.opendaylight.yangtools.concepts.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class BindingReflections {

    private static final long EXPIRATION_TIME = 60;

    private static final LoadingCache<Class<?>, Optional<QName>> classToQName = CacheBuilder.newBuilder() //
            .weakKeys() //
            .expireAfterAccess(EXPIRATION_TIME, TimeUnit.SECONDS) //
            .build(new ClassToQNameLoader());

    /**
     * 
     * @param augmentation
     *            {@link Augmentation} subclass for which we want to determine
     *            augmentation target.
     * @return Augmentation target - class which augmentation provides
     *         additional extensions.
     */
    public static Class<? extends Augmentable<?>> findAugmentationTarget(
            final Class<? extends Augmentation<?>> augmentation) {
        return ClassLoaderUtils.findFirstGenericArgument(augmentation, Augmentation.class);
    }

    /**
     * 
     * @param augmentation
     *            {@link Augmentation} subclass for which we want to determine
     *            augmentation target.
     * @return Augmentation target - class which augmentation provides
     *         additional extensions.
     */
    public static Class<?> findHierarchicalParent(final Class<? extends ChildOf<?>> childClass) {
        return ClassLoaderUtils.findFirstGenericArgument(childClass, ChildOf.class);
    }

    /**
     * 
     * @param augmentation
     *            {@link Augmentation} subclass for which we want to determine
     *            augmentation target.
     * @return Augmentation target - class which augmentation provides
     *         additional extensions.
     */
    public static Class<?> findHierarchicalParent(DataObject childClass) {
        if (childClass instanceof ChildOf) {
            return ClassLoaderUtils.findFirstGenericArgument(childClass.getImplementedInterface(), ChildOf.class);
        }
        return null;
    }

    public static final QName findQName(Class<?> dataType) {
        return classToQName.getUnchecked(dataType).orNull();
    }

    private static class ClassToQNameLoader extends CacheLoader<Class<?>, Optional<QName>> {

        @Override
        public Optional<QName> load(Class<?> key) throws Exception {
            try {
                Field field = key.getField(BindingMapping.QNAME_STATIC_FIELD_NAME);
                Object obj = field.get(null);
                if (obj instanceof QName) {
                    return Optional.of((QName) obj);
                }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // NOOP
            }
            return Optional.absent();
        }
    }

    public static boolean isRpcMethod(Method possibleMethod) {
        return possibleMethod != null &&
        RpcService.class.isAssignableFrom(possibleMethod.getDeclaringClass())
        && Future.class.isAssignableFrom(possibleMethod.getReturnType())
        && possibleMethod.getParameterTypes().length <= 1;
    }

    @SuppressWarnings("rawtypes")
    public static Optional<Class<?>> resolveRpcOutputClass(Method targetMethod) {
        checkState(isRpcMethod(targetMethod),"Supplied method is not Rpc invocation method");
        Type futureType = targetMethod.getGenericReturnType();
        Type rpcResultType = ClassLoaderUtils.getFirstGenericParameter(futureType);
        Type rpcResultArgument = ClassLoaderUtils.getFirstGenericParameter(rpcResultType);
        if(rpcResultArgument instanceof Class && !Void.class.equals(rpcResultArgument)) {
            return Optional.<Class<?>>of((Class) rpcResultArgument);
        }
        return Optional.absent();
    }

    @SuppressWarnings("unchecked")
    public static Optional<Class<? extends DataContainer>> resolveRpcInputClass(Method targetMethod) {
        @SuppressWarnings("rawtypes")
        Class[] types = targetMethod.getParameterTypes();
        if(types.length == 0) {
            return Optional.absent();
        }
        if(types.length == 1) {
            return Optional.<Class<? extends DataContainer>>of(types[0]);
        }
        throw new IllegalArgumentException("Method has 2 or more arguments.");
    }

    public static QName getQName(Class<? extends BaseIdentity> context) {
        // TODO Auto-generated method stub
        return null;
    }
}
