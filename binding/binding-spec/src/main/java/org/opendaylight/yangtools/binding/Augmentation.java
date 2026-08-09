/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import org.opendaylight.yangtools.binding.lib.JavaDataContainer;

/**
 * Augmentation (extension) of other interface. This interface uniquely bounds Augmentation to generated interface.
 *
 * <p>All interfaces generated from YANG Augmentation statement must implement this interface with parameter {@code P}
 * which uniquely points to it's target class.
 *
 * @param <A> Class to which this implementation is extension.
 * @param <T> concrete augmentation type
 */
public interface Augmentation<A extends Augmentable<A> & DataContainer, T extends Augmentation<A, T>>
        extends DataObject, JavaDataContainer<T> {
    @Override
    Class<T> implementedInterface();
}
