/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

/**
 * A {@link DataObject} which represents a single entry in a {@code list} with a {@code key}.
 */
public non-sealed interface EntryObject<T extends EntryObject<T, K>, K extends Key<T>> extends DataObject, KeyAware<K> {

}
