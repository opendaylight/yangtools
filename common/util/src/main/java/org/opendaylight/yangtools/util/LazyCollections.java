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
import java.util.List;

/**
 * Utility methods for lazily instantiated collections. These are useful for
 * situations when we start off with an empty collection (where Collections.empty()
 * can be reused), but need to add more things.
 */
public final class LazyCollections {

    private LazyCollections() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Add an element to a list, potentially transforming the list.
     *
     * @param list Current list
     * @param obj Object that needs to be added
     * @return new list
     */
    public static <T> List<T> lazyAdd(final List<T> list, final T obj) {
        final List<T> ret;

        switch (list.size()) {
        case 0:
            return Collections.singletonList(obj);
        case 1:
            ret = new ArrayList<>();
            ret.addAll(list);
            break;
        default:
            ret = list;
        }

        ret.add(obj);
        return ret;
    }

}
