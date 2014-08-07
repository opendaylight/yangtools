package org.opendaylight.yangtools.binding.data.codec.util;

import java.util.Map;
import org.opendaylight.yangtools.yang.binding.Augmentation;

public interface AugmentationSupplier {

    Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations();
}
