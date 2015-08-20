/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import org.opendaylight.yangtools.yang.binding.AugmentationHolder;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AugmentationFieldGetter {

    private static final Logger LOG = LoggerFactory.getLogger(AugmentationFieldGetter.class);

    private static final AugmentationFieldGetter DUMMY = new AugmentationFieldGetter() {
        @Override
        protected Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(final Object input) {
            return Collections.emptyMap();
        }
    };

    private static final AugmentationFieldGetter AUGMENTATION_HOLDER_GETTER = new AugmentationFieldGetter() {

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(final Object input) {
            return (Map) ((AugmentationHolder<?>) input).augmentations();
        }
    };

    /**
     *
     * Retrieves augmentations from supplied object
     *
     * @param input Input Data object, from which augmentations should be extracted
     * @return Map of Augmentation class to augmentation
     */
    protected abstract Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(final Object input);

    private static final LoadingCache<Class<?>, AugmentationFieldGetter> AUGMENTATION_GETTERS = CacheBuilder
            .newBuilder().weakKeys().softValues().build(new AugmentationGetterLoader());

    public static AugmentationFieldGetter getGetter(final Class<? extends Object> clz) {
        if(AugmentationHolder.class.isAssignableFrom(clz)) {
            return AUGMENTATION_HOLDER_GETTER;
        }
        return AUGMENTATION_GETTERS.getUnchecked(clz);
    }

    private static final class AugmentationGetterLoader extends CacheLoader<Class<?>, AugmentationFieldGetter> {

        @Override
        public AugmentationFieldGetter load(final Class<?> key) throws Exception {
            Field field;
            try {
                field = key.getDeclaredField(BindingMapping.AUGMENTATION_FIELD);
            } catch (NoSuchFieldException | SecurityException e) {
                LOG.debug("Failed to acquire augmentation field", e);
                return DUMMY;
            }
            field.setAccessible(true);

            return new ReflectionAugmentationFieldGetter(field);
        }
    }

    private static final class ReflectionAugmentationFieldGetter extends AugmentationFieldGetter {
        private final Field augmentationField;

        ReflectionAugmentationFieldGetter(final Field augmentationField) {
            this.augmentationField = Preconditions.checkNotNull(augmentationField);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(final Object input) {
            try {
                return (Map<Class<? extends Augmentation<?>>, Augmentation<?>>) augmentationField.get(input);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new IllegalStateException("Failed to access augmentation field", e);
            }
        }
    }

}
