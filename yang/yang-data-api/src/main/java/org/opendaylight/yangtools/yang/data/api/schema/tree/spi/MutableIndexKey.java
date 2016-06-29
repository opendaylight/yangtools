/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.annotations.Beta;
import java.util.Collection;

/**
 * Tree node index key interface.
 * FIXME: add javadoc
 */
@Beta
public interface MutableIndexKey {

    /**
     * Add value to index key
     *
     * @param value
     */
    void add(Object value);

    /**
     * Clear all values from index key
     *
     * @param value
     */
    void clear();

    /**
     * Get current values of index key
     *
     * @return collection of index key values
     */
    Collection<?> getValues();

    /**
     * Finish index key modification and return a read-only view of this key. After
     * this method is invoked, any further calls to this object's method result
     * in undefined behavior.
     *
     * @return read-only view of index key.
     */
    IndexKey seal();
}
