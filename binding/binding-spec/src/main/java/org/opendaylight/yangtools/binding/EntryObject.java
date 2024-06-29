/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

/**
 * A {@link DataContainer} which represents a single entry in a {@code list} with a {@code key} -- and hence it has a
 * {@link #key()}.
 *
 * @apiNote
 *     This interface's name is derived from 'Entry' as in 'Map.Entry', which is a connection we want to make when
 *     someone is faced with this interface. We forego the 'Map' part, as it is implied.
 *     The logic here is that {@code EntryObject} is an entry on a {@code Map<Key, EntryObject>}, whereas {@code list}
 *     statements are mapped to {@code List<ElementObject>}.
 */
public non-sealed interface EntryObject<T extends EntryObject<T, K>, K extends Key<T>>
    extends DataContainer.Addressable.Multiple, KeyAware<K> {
    // Nothing else
}
