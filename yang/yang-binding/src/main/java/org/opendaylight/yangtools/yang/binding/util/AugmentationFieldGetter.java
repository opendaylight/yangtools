package org.opendaylight.yangtools.yang.binding.util;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
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

    abstract Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(final Object input);

    private static final LoadingCache<Class<?>, AugmentationFieldGetter> AUGMENTATION_GETTERS =
            CacheBuilder.newBuilder().weakKeys().softValues().build(new AugmentationGetterLoader());

    public static AugmentationFieldGetter getGetter(final Class<? extends Object> clz) {
        return AUGMENTATION_GETTERS.getUnchecked(clz);
    }

    private static final class AugmentationGetterLoader extends CacheLoader<Class<?>, AugmentationFieldGetter> {

        @Override
        public AugmentationFieldGetter load(final Class<?> key) throws Exception {
            Field field;
            try {
                field = key.getDeclaredField("augmentation");
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
        Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(final Object input) {
            try {
                return (Map<Class<? extends Augmentation<?>>, Augmentation<?>>) augmentationField.get(input);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new IllegalStateException("Failed to access augmentation field", e);
            }
        }
    }


}
