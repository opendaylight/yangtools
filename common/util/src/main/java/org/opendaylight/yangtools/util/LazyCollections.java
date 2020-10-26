/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility methods for lazily instantiated collections. These are useful for situations when we start off with an empty
 * collection (where Collections.empty() * can be reused), but need to add more things.
 */
public final class LazyCollections {
    private LazyCollections() {
        // Hidden on purpose
    }

    /**
     * Add an element to a list, potentially transforming the list.
     *
     * @param <E> the type of elements in the list
     * @param list Current list
     * @param obj Object that needs to be added
     * @return new list
     */
    public static <E> List<E> lazyAdd(final List<E> list, final E obj) {
        final List<E> ret;

        switch (list.size()) {
            case 0:
                return Collections.singletonList(obj);
            case 1:
                ret = new ArrayList<>(2);
                ret.addAll(list);
                break;
            default:
                ret = list;
        }

        ret.add(obj);
        return ret;
    }

    /**
     * Add an element to a set, potentially transforming the set.
     *
     * @param <E> the type of elements in the set
     * @param set Current set
     * @param obj Object that needs to be added
     * @return new set
     */
    public static <E> Set<E> lazyAdd(final Set<E> set, final E obj) {
        final Set<E> ret;

        switch (set.size()) {
            case 0:
                return Collections.singleton(obj);
            case 1:
                ret = new HashSet<>(2);
                ret.addAll(set);
                break;
            default:
                ret = set;
        }

        ret.add(obj);
        return ret;
    }
}
