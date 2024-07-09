/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

/**
 * An {@code notification} which is defined within the schema tree and is thus tied to a data tree instance and
 * additionally its parent is a {code list} with a {@code key} statement. A concrete KeyedListNotification and its
 * implementations may choose to also extend/implement the {@link EventInstantAware} interface. In case they do,
 * {@link EventInstantAware#eventInstant()} returns the time when this notification was generated.
 *
 * @param <N> Concrete notification type
 * @param <T> Parent data tree instance type
 * @param <K> Parent data tree key type
 */
public interface KeyedListNotification<N extends KeyedListNotification<N, T, K>, T extends EntryObject<T, K>,
    K extends Key<T>> extends InstanceNotification<N, T> {

}
