/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Mutable tree node index interface.
 *
 * Mutable version of tree node index. It provides set of methods to perform
 * modifications on the index map. If modifications of index are finished,
 * mutable tree node index is sealed via {@link #seal()} method, which returns
 * immutable tree node index copy.
 *
 * @param <K>
 *            index key type
 * @param <V>
 *            index value type
 */
@Beta
public interface MutableTreeNodeIndex<K extends IndexKey<?>, V extends NormalizedNode<?, ?>> extends TreeNodeIndex<K, V> {
    /**
     * Update data in the index.
     *
     * @param data
     *            data to update
     * @throws IllegalArgumentException
     *             if data is unable to update due to index constraints or
     *             invalid data
     */
    void update(NormalizedNode<?, ?> data);

//    /**
//     * Get data associated with specified index key from index.
//     *
//     * @param indexKey
//     *            index key
//     * @return Optional of index data associated with specified index key.
//     */
//    Optional<V> get(K indexKey);

    /**
     * Put data associated with specified index key into index.
     *
     * @param indexKey
     *            index key
     * @param data
     *            data to associate with specified index key.
     * @throws IllegalArgumentException
     *             if data is unable to put into index due to index constraints
     *             or invalid data
     */
    void put(K indexKey, V data);

    /**
     * Remove data associated with specified index key from index.
     *
     * @param indexKey
     *            index key
     */
    void remove(K indexKey);

    /**
     * Finish index modification and return a read-only view of this tree node
     * index. After this method is invoked, any further calls to this object's
     * method result in undefined behavior.
     *
     * @return Read-only view of this node.
     */
    TreeNodeIndex<?, ?> seal();
}
