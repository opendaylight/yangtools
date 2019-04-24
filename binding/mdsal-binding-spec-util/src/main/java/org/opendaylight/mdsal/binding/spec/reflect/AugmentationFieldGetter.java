/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spec.reflect;

import static java.util.Objects.requireNonNull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AugmentationFieldGetter {

    private static final Logger LOG = LoggerFactory.getLogger(AugmentationFieldGetter.class);

    private static final AugmentationFieldGetter DUMMY = new AugmentationFieldGetter() {
        @Override
        Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(final Object input) {
            return Collections.emptyMap();
        }
    };

    private static final LoadingCache<Class<?>, AugmentationFieldGetter> AUGMENTATION_GETTERS =
            CacheBuilder.newBuilder().weakKeys().build(new AugmentationGetterLoader());

    /**
     * Retrieves augmentations from supplied object.
     *
     * @param input Input Data object, from which augmentations should be extracted
     * @return Map of Augmentation class to augmentation
     */
    abstract Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(Object input);

    static AugmentationFieldGetter getGetter(final Class<? extends Object> clz) {
        return AUGMENTATION_GETTERS.getUnchecked(clz);
    }

    private static final class AugmentationGetterLoader extends CacheLoader<Class<?>, AugmentationFieldGetter> {
        private static final MethodType GETTER_TYPE = MethodType.methodType(Map.class, Object.class);
        private static final Lookup LOOKUP = MethodHandles.lookup();

        @Override
        public AugmentationFieldGetter load(final Class<?> key) throws IllegalAccessException {
            final Field field;
            try {
                field = AccessController.doPrivileged((PrivilegedExceptionAction<Field>) () -> {
                    final Field f = key.getDeclaredField(BindingMapping.AUGMENTATION_FIELD);
                    f.setAccessible(true);
                    return f;
                });
            } catch (PrivilegedActionException e) {
                LOG.warn("Failed to acquire augmentation field {}, ignoring augmentations in class {}",
                    BindingMapping.AUGMENTATION_FIELD, key, e);
                return DUMMY;
            }
            if (!Map.class.isAssignableFrom(field.getType())) {
                LOG.warn("Class {} field {} is not a Map, ignoring augmentations", key,
                    BindingMapping.AUGMENTATION_FIELD);
                return DUMMY;
            }

            return new ReflectionAugmentationFieldGetter(LOOKUP.unreflectGetter(field).asType(GETTER_TYPE));
        }
    }

    private static final class ReflectionAugmentationFieldGetter extends AugmentationFieldGetter {
        private final MethodHandle fieldGetter;

        ReflectionAugmentationFieldGetter(final MethodHandle mh) {
            this.fieldGetter = requireNonNull(mh);
        }

        @Override
        @SuppressWarnings("checkstyle:illegalCatch")
        Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(final Object input) {
            try {
                return (Map<Class<? extends Augmentation<?>>, Augmentation<?>>) fieldGetter.invokeExact(input);
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to access augmentation field on " + input, e);
            }
        }
    }
}
