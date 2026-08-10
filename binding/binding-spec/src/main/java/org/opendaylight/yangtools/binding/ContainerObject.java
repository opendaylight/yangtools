/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

/**
 * A {@link DataObject} which represents a {@code container} with a {@code key}.
 *
 * @param <P> Parent container
 * @param <T> concrete container type
 * @since 16.0.0
 */
public non-sealed interface ContainerObject<P extends DataContainer, T extends ContainerObject<P, T>>
    extends Augmentable<T>, ChildOf<P>, ParentObject<T> {
    // nothing else
}
