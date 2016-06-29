/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Tree node index interface.
 * FIXME: add javadoc
 */
@Beta
public interface TreeNodeIndex {
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

    /**
     * Get data associated with specified index key from index.
     *
     * @param indexKey
     *            index key
     * @return Optional of index data associated with specified index key.
     */
    Optional<NormalizedNode<?, ?>> get(IndexKey indexKey);

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
    void put(IndexKey indexKey, MapEntryNode data);

    /**
     * Remove data associated with specified index key from index.
     *
     * @param indexKey
     *            index key
     */
    void remove(IndexKey indexKey);
}
