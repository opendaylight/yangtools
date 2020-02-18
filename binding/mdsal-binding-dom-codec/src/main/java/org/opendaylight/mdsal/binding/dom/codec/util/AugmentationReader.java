/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.util;

import java.util.Map;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;

/**
 * Interface which sould be implemented by proxy {@link java.lang.reflect.InvocationHandler} to obtain augmentations
 * from proxy implementations of {@link org.opendaylight.yangtools.yang.binding.Augmentable} object.
 *
 * <p>
 * If implemented proxy does not implement this interface, its augmentations are not properly serialized / deserialized.
 *
 * @deprecated Use {@link AugmentationHolder} instead.
 */
@Deprecated(forRemoval = true)
public interface AugmentationReader {

    Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(Object obj);
}
