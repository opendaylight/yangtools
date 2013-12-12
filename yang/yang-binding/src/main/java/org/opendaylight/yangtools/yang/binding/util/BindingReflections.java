/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.opendaylight.yangtools.concepts.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.QName;

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

    public static final QName findQName(Class<? extends DataContainer> dataType) {
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

}
