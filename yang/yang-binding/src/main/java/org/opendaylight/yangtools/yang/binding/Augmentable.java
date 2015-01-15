/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * Augmentable (extensible) object which could carry additional data defined by
 * third-party extension, without introducing conflict between various
 * extension.
 * 
 * This interface uses extended version of ExtensibleInterface pattern which
 * also adds marker interface for augmentations (extensions) -
 * {@link Augmentable}
 * 
 * @author Tony Tkacik
 * @param <T>
 *            Base class which should implements this interface and is target
 *            for augmentation.
 */
public interface Augmentable<T> {

    /**
     * Returns instance of augmentation.
     * 
     * @param augmentationType
     *            Type of augmentation to be returned.
     * @return instance of augmentation.
     */
    <E extends Augmentation<T>> E getAugmentation(Class<E> augmentationType);
}
