/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Augmentable (extensible) object which could carry additional data defined by third-party extension, without
 * introducing conflict between various extension.
 *
 * @param <T>
 *            Base class which should is target
 *            for augmentations.
 * @author Tony Tkacik
 */
public interface AugmentationHolder<T> {
    /**
     * Returns map of all augmentations.
     *
     * @return map of all augmentations.
     */
    @NonNull Map<Class<? extends Augmentation<T>>, Augmentation<T>> augmentations();
}
