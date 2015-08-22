/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import java.util.AbstractMap;

/**
 * Abstract base class for maps which support lazy value object instantiation. This is useful in situations where a
 * subset of values stored in the map are simple wrappers around some value. Using this abstraction can make the
 * memory/CPU tradeoff by storing the bare minimum to re-create the value object, thus saving memory.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@Beta
public abstract class AbstractLazyValueMap<K, V> extends AbstractMap<K, V> {
    /**
     * Derive the value stored for a key/object pair. Subclasses can override this method and {@link #valueToObject(Object)}
     * to improve memory efficiency.
     *
     * @param key Key being looked up
     * @param obj Internally-stored object for the key
     * @return Value to be returned to the user.
     */
    @SuppressWarnings("unchecked")
    protected V objectToValue(final K key, final Object obj) {
        return (V)obj;
    }

    /**
     * Derive the object to be stored in the backing array. Subclasses can override this method and {@link #objectToValue(Object, Object)}
     * to instantiate value objects as they are looked up without storing them in the map.
     *
     * @param value Value being looked up
     * @return Stored object which corresponds to the value.
     */
    protected Object valueToObject(final V value) {
        return value;
    }
}
